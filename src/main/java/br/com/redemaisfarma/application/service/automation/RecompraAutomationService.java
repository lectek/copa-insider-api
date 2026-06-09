package br.com.redemaisfarma.application.service.automation;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaign;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaignQueue;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ItemPedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailCampaignQueueRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailCampaignRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ItemPedidoJpaRepository;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class RecompraAutomationService {
    private static final Logger log = LoggerFactory.getLogger(RecompraAutomationService.class);
    private static final String TEMPLATE_KEY = "mail/recompra";
    private static final List<String> QUEUE_STATUSES = List.of("PENDING", "SENDING", "SENT");

    private final ItemPedidoJpaRepository itemRepository;
    private final EmailCampaignRepository campaignRepository;
    private final EmailCampaignQueueRepository queueRepository;
    private final ObjectMapper objectMapper;
    private final Counter processedCounter;
    private final Counter skippedCounter;
    private final Counter enqueuedCounter;

    @Value("${recompra.thresholds:30}")
    private String thresholdConfig;

    @Value("${recompra.window-days:7}")
    private int windowDays = 7;

    @Value("${recompra.limit:100}")
    private int limit = 100;

    @Value("${recompra.categories:medicamento}")
    private String categoryConfig;

    private List<Integer> thresholds;
    private List<String> categoryKeywords;

    public RecompraAutomationService(
            ItemPedidoJpaRepository itemRepository,
            EmailCampaignRepository campaignRepository,
            EmailCampaignQueueRepository queueRepository,
            MeterRegistry meterRegistry,
            ObjectMapper objectMapper
    ) {
        this.itemRepository = itemRepository;
        this.campaignRepository = campaignRepository;
        this.queueRepository = queueRepository;
        this.objectMapper = objectMapper;
        this.processedCounter = Counter.builder("recompra.automation.processed")
                .description("Pedidos avaliados pela automação de recompra")
                .register(meterRegistry);
        this.skippedCounter = Counter.builder("recompra.automation.skipped")
                .description("Pedidos ignorados pela automação de recompra")
                .register(meterRegistry);
        this.enqueuedCounter = Counter.builder("recompra.automation.enqueued")
                .description("E-mails de recompra enfileirados")
                .register(meterRegistry);
    }

    @PostConstruct
    void init() {
        thresholds = parseThresholds(thresholdConfig);
        categoryKeywords = parseKeywords(categoryConfig);
    }

    @Scheduled(cron = "${recompra.cron:0 15 4 * * *}")
    @Transactional
    public void runRecompra() {
        EmailCampaign campaign = ensureCampaign();
        List<EmailCampaignQueue> existingQueue = queueRepository.findByCampaignIdAndStatusIn(
                campaign.getId(),
                QUEUE_STATUSES
        );
        Map<String, Set<Long>> queuedProductsByEmail = buildQueuedIndex(existingQueue);

        LocalDateTime now = LocalDateTime.now();
        for (int days : thresholds) {
            try {
                processThreshold(days, now, campaign, queuedProductsByEmail);
            } catch (Exception ex) {
                log.error("[automation] recompra {}d falhou", days, ex);
            }
        }
    }

    private void processThreshold(int days,
                                  LocalDateTime now,
                                  EmailCampaign campaign,
                                  Map<String, Set<Long>> queuedProductsByEmail) {
        LocalDateTime recentCutoff = now.minusDays(days);
        LocalDateTime olderCutoff = recentCutoff.minusDays(windowDays);
        List<ItemPedidoEntity> candidates = itemRepository.findByPedidoStatusAndPedidoDataBetweenAndProdutoDisponivelTrueOrderByPedidoDataDesc(
                StatusPedido.ENTREGUE,
                olderCutoff,
                recentCutoff,
                PageRequest.of(0, limit)
        );
        if (candidates.isEmpty()) {
            return;
        }
        log.info("[automation] recompra {}d processando {} itens", days, candidates.size());
        for (ItemPedidoEntity item : candidates) {
            processedCounter.increment();
            if (!eligible(item)) {
                skippedCounter.increment();
                continue;
            }
            String email = item.getPedido().getCliente().getEmail().toLowerCase();
            Long productId = item.getProduto().getId();
            if (alreadyQueued(queuedProductsByEmail, email, productId)) {
                skippedCounter.increment();
                continue;
            }
            EmailCampaignQueue queue = buildQueueEntry(campaign, item, days);
            queueRepository.save(queue);
            enqueuedCounter.increment();
            log.info("[automation] recompra {}d enfileirou {} ({})", days, email, item.getProduto().getNome());
            queuedProductsByEmail.computeIfAbsent(email, key -> new HashSet<>()).add(productId);
        }
    }

    private boolean eligible(ItemPedidoEntity item) {
        if (item.getPedido() == null || item.getPedido().getCliente() == null) {
            return false;
        }
        if (item.getProduto() == null || !matchesCategory(item.getProduto().getCategoria())) {
            return false;
        }
        String email = item.getPedido().getCliente().getEmail();
        if (email == null || email.isBlank()) {
            return false;
        }
        Integer estoque = item.getProduto().getEstoque();
        return estoque != null && estoque > 0;
    }

    private EmailCampaignQueue buildQueueEntry(EmailCampaign campaign, ItemPedidoEntity item, int days) {
        EmailCampaignQueue queue = new EmailCampaignQueue();
        queue.setCampaignId(campaign.getId());
        String email = item.getPedido().getCliente().getEmail().toLowerCase();
        queue.setRecipientEmail(email);
        queue.setRecipientName(item.getPedido().getCliente().getNome());
        queue.setStatus("PENDING");
        queue.setScheduledAt(java.time.Instant.now());
        queue.setPayloadJson(buildPayload(item, days));
        return queue;
    }

    private String buildPayload(ItemPedidoEntity item, int days) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("clienteId", item.getPedido().getCliente().getId());
            payload.put("pedidoId", item.getPedido().getId());
            payload.put("produtoId", item.getProduto().getId());
            payload.put("produtoNome", item.getProduto().getNome());
            payload.put("thresholdDays", days);
            payload.put("ultimaCompra", item.getPedido().getData().toString());
            payload.put("totalPedido", item.getPedido().getTotal());
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            log.warn("[automation] recompra payload falhou", ex);
            return "{}";
        }
    }

    private EmailCampaign ensureCampaign() {
        return campaignRepository.findByTemplateKey(TEMPLATE_KEY)
                .orElseGet(this::createCampaign);
    }

    private EmailCampaign createCampaign() {
        EmailCampaign campaign = new EmailCampaign();
        campaign.setNome("Automação - Recompra automática");
        campaign.setAssunto("Está na hora de recomprar seus medicamentos");
        campaign.setTemplateKey(TEMPLATE_KEY);
        campaign.setStatus("SCHEDULED");
        campaign.setSegmentJson("{\"segmento\":\"RECOMPRA\"}");
        campaign.setValidationStatus("PENDING");
        campaign.setScheduledAt(java.time.Instant.now());
        campaign.setScheduledZone("UTC");
        return campaignRepository.save(campaign);
    }

    private Map<String, Set<Long>> buildQueuedIndex(List<EmailCampaignQueue> existingQueue) {
        Map<String, Set<Long>> map = new HashMap<>();
        for (EmailCampaignQueue queue : existingQueue) {
            String email = queue.getRecipientEmail();
            if (email == null || email.isBlank()) {
                continue;
            }
            Long productId = extractProductId(queue.getPayloadJson());
            if (productId == null) {
                continue;
            }
            map.computeIfAbsent(email.toLowerCase(), key -> new HashSet<>()).add(productId);
        }
        return map;
    }

    private boolean alreadyQueued(Map<String, Set<Long>> existing, String email, Long productId) {
        if (productId == null) {
            return false;
        }
        Set<Long> products = existing.get(email);
        return products != null && products.contains(productId);
    }

    private Long extractProductId(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            return null;
        }
        try {
            Map<?, ?> payload = objectMapper.readValue(payloadJson, Map.class);
            Object value = payload.get("produtoId");
            if (value instanceof Number number) {
                return number.longValue();
            }
            if (value instanceof String str) {
                return Long.parseLong(str);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private boolean matchesCategory(String categoria) {
        if (categoria == null) {
            return false;
        }
        String normalized = categoria.toLowerCase().trim();
        return categoryKeywords.stream().anyMatch(normalized::contains);
    }

    private List<Integer> parseThresholds(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of(30);
        }
        List<Integer> values = new ArrayList<>();
        for (String part : raw.split(",")) {
            try {
                values.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return values.isEmpty() ? List.of(30) : values;
    }

    private List<String> parseKeywords(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of("medicamento");
        }
        List<String> keywords = new ArrayList<>();
        for (String part : raw.split(",")) {
            String trimmed = part.trim().toLowerCase();
            if (!trimmed.isBlank()) {
                keywords.add(trimmed);
            }
        }
        return keywords.isEmpty() ? List.of("medicamento") : keywords;
    }
}
