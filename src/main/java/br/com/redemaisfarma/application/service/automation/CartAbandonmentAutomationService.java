package br.com.redemaisfarma.application.service.automation;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaign;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaignQueue;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailCampaignQueueRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailCampaignRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.redemaisfarma.application.service.validation.CartValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CartAbandonmentAutomationService {
    private static final Logger log = LoggerFactory.getLogger(CartAbandonmentAutomationService.class);
    private static final String DEFAULT_TEMPLATE = "mail/cart-abandon";
    private static final String STATUS_PENDING = "PENDING";
    private static final List<String> ACTIVE_QUEUE_STATUSES = List.of(STATUS_PENDING, "SENDING", "SENT");

    private final CartSnapshotProvider snapshotProvider;
    private final ClienteRepository clienteRepository;
    private final EmailCampaignRepository campaignRepository;
    private final EmailCampaignQueueRepository queueRepository;
    private final CartValidationService cartValidationService;
    private final Counter processedCounter;
    private final Counter skippedCounter;
    private final Counter enqueuedCounter;
    private final ObjectMapper objectMapper;

    @Value("${cart.abandonment.scan-ms:300000}")
    private long scanDelay = 300000;

    @Value("${cart.abandonment.initial-delay-ms:10000}")
    private long initialDelay = 10000;

    @Value("${cart.abandonment.threshold-hours:1,24}")
    private String thresholdConfig;

    @Value("${cart.abandonment.limit:20}")
    private int limit = 20;

    private List<Integer> thresholds;

    public CartAbandonmentAutomationService(
            CartSnapshotProvider snapshotProvider,
            ClienteRepository clienteRepository,
            EmailCampaignRepository campaignRepository,
            EmailCampaignQueueRepository queueRepository,
            CartValidationService cartValidationService,
            MeterRegistry meterRegistry,
            ObjectMapper objectMapper
    ) {
        this.snapshotProvider = snapshotProvider;
        this.clienteRepository = clienteRepository;
        this.campaignRepository = campaignRepository;
        this.queueRepository = queueRepository;
        this.cartValidationService = cartValidationService;
        this.processedCounter = Counter.builder("cart_abandonment.automation.processed")
                .description("Snapshots avaliados pelo automation de carrinho abandonado")
                .register(meterRegistry);
        this.skippedCounter = Counter.builder("cart_abandonment.automation.skipped")
                .description("Snapshots ignorados por invalidade")
                .register(meterRegistry);
        this.enqueuedCounter = Counter.builder("cart_abandonment.automation.enqueued")
                .description("E-mails de carrinho abandonado enviados")
                .register(meterRegistry);
        this.objectMapper = objectMapper;
        initThresholds();
    }

    @PostConstruct
    private void initThresholds() {
        thresholds = new ArrayList<>();
        String rawConfig = thresholdConfig;
        if (rawConfig == null || rawConfig.isBlank()) {
            rawConfig = "1";
        }
        for (String raw : rawConfig.split(",")) {
            try {
                thresholds.add(Integer.parseInt(raw.trim()));
            } catch (NumberFormatException ex) {
                log.warn("[automation] valor invalido em cart.abandonment.threshold-hours: '{}'", raw);
            }
        }
        if (thresholds.isEmpty()) {
            thresholds.add(1);
        }
    }

    @Scheduled(fixedDelayString = "${cart.abandonment.scan-ms:300000}", initialDelayString = "${cart.abandonment.initial-delay-ms:10000}")
    @Transactional
    public void processAbandonedCarts() {
        for (int hours : thresholds) {
            try {
                processThreshold(hours);
            } catch (DataAccessException dex) {
                log.warn("[automation] falha ao consultar carrinhos abandonados para {}h", hours, dex);
            } catch (Exception ex) {
                log.error("[automation] erro ao processar carrinhos abandonados para {}h", hours, ex);
            }
        }
    }

    protected void processThreshold(int hours) {
        Duration olderThan = Duration.ofHours(hours);
        List<CartSnapshot> snapshots = snapshotProvider.findAbandonedCarts(olderThan, limit);
        if (snapshots.isEmpty()) {
            return;
        }
        EmailCampaign campaign = ensureCampaign(hours);
        Set<String> existingEmails = queueRepository.findByCampaignIdAndStatusIn(
                        campaign.getId(),
                        ACTIVE_QUEUE_STATUSES
                ).stream()
                .map(queue -> queue.getRecipientEmail().toLowerCase())
                .collect(Collectors.toSet());

        for (CartSnapshot snapshot : snapshots) {
            processedCounter.increment();
            CartValidationService.CartValidationResult validation = validateSnapshot(snapshot);
            if (!validation.valid()) {
                log.info("[automation] carrinho abandonado {}h ignorado: {}", hours, validation.message());
                skippedCounter.increment();
                continue;
            }
            if (enqueue(campaign, snapshot, hours, existingEmails)) {
                enqueuedCounter.increment();
            }
        }
    }

    private CartValidationService.CartValidationResult validateSnapshot(CartSnapshot snapshot) {
        List<CartValidationService.CartEntry> entries = snapshot.getItems().stream()
                .map(item -> new CartValidationService.CartEntry(item.getProdutoId(), item.getQuantidade()))
                .toList();
        return cartValidationService.validate(entries);
    }

    private EmailCampaign ensureCampaign(int hours) {
        String templateKey = DEFAULT_TEMPLATE;
        Optional<EmailCampaign> existing = campaignRepository.findByTemplateKey(templateKey);
        if (existing.isPresent()) {
            return existing.get();
        }
        EmailCampaign campaign = new EmailCampaign();
        campaign.setNome("Automação - Carrinho abandonado " + hours + "h");
        campaign.setAssunto(subjectFor(hours));
        campaign.setTemplateKey(templateKey);
        campaign.setStatus("SCHEDULED");
        campaign.setSegmentJson("{}");
        campaign.setScheduledZone("UTC");
        campaign.setValidationStatus(STATUS_PENDING);
        return campaignRepository.save(campaign);
    }

    private String subjectFor(int hours) {
        if (hours <= 1) {
            return "Você esqueceu itens no carrinho";
        }
        return "Volte ao carrinho e finalize sua compra";
    }

    private boolean enqueue(EmailCampaign campaign, CartSnapshot snapshot, int hours, Set<String> existingEmails) {
        if (snapshot.getClienteId() == null) {
            return false;
        }
        ClienteEntity cliente = clienteRepository.findById(snapshot.getClienteId()).orElse(null);
        if (cliente == null || cliente.getEmail() == null || cliente.getEmail().isBlank()) {
            return false;
        }
        String email = cliente.getEmail().toLowerCase();
        if (existingEmails.contains(email)) {
            return false;
        }
        EmailCampaignQueue queue = new EmailCampaignQueue();
        queue.setCampaignId(campaign.getId());
        queue.setRecipientEmail(email);
        queue.setRecipientName(cliente.getNome());
        queue.setStatus(STATUS_PENDING);
        queue.setScheduledAt(Instant.now());
        queue.setPayloadJson(buildPayload(snapshot, hours));
        queueRepository.save(queue);
        log.info("[automation] carrinho abandonado {}h enfileirado para {}", hours, email);
        existingEmails.add(email);
        return true;
    }

    private String buildPayload(CartSnapshot snapshot, int hours) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("clienteId", snapshot.getClienteId());
            payload.put("total", snapshot.getTotal());
            payload.put("updatedAt", snapshot.getUpdatedAt() == null ? null : snapshot.getUpdatedAt().toString());
            payload.put("hours", hours);
            payload.put("items", snapshot.getItems().stream().map(item -> Map.<String, Object>of(
                    "produtoId", item.getProdutoId(),
                    "quantidade", item.getQuantidade()))
                    .toList());
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            log.warn("[automation] falha ao serializar payload do carrinho abandonado {}h", hours, ex);
            return "{}";
        }
    }
}
