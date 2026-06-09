package br.com.redemaisfarma.application.service.automation;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaign;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaignQueue;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProductStockSubscriptionEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailCampaignQueueRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailCampaignRepository;
import br.com.redemaisfarma.application.service.ProductStockSubscriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ProductBackInStockAutomationService {
    private static final Logger log = LoggerFactory.getLogger(ProductBackInStockAutomationService.class);
    private static final String TEMPLATE_KEY = "mail/back-in-stock";

    private final EmailCampaignRepository campaignRepository;
    private final EmailCampaignQueueRepository queueRepository;
    private final ProductStockSubscriptionService subscriptionService;
    private final Counter processedCounter;
    private final Counter enqueuedCounter;
    private final Counter skippedCounter;
    private final ObjectMapper objectMapper;

    @Value("${backinstock.cron:0 */10 * * * *}")
    private String cron;

    @Value("${backinstock.batch-size:200}")
    private int batchSize = 200;

    public ProductBackInStockAutomationService(
            EmailCampaignRepository campaignRepository,
            EmailCampaignQueueRepository queueRepository,
            ProductStockSubscriptionService subscriptionService,
            MeterRegistry meterRegistry,
            ObjectMapper objectMapper
    ) {
        this.campaignRepository = campaignRepository;
        this.queueRepository = queueRepository;
        this.subscriptionService = subscriptionService;
        this.objectMapper = objectMapper;
        this.processedCounter = Counter.builder("back_in_stock.automation.processed")
                .description("Assinaturas avaliadas pela automação de volta ao estoque")
                .register(meterRegistry);
        this.enqueuedCounter = Counter.builder("back_in_stock.automation.enqueued")
                .description("E-mails de volta ao estoque enviados")
                .register(meterRegistry);
        this.skippedCounter = Counter.builder("back_in_stock.automation.skipped")
                .description("Assinaturas ignoradas pela automação de volta ao estoque")
                .register(meterRegistry);
    }

    @Scheduled(cron = "${backinstock.cron:0 */10 * * * *}")
    @Transactional
    public void processSubscriptions() {
        List<ProductStockSubscriptionEntity> subscriptions = subscriptionService.findPending(batchSize);
        if (subscriptions.isEmpty()) {
            return;
        }
        EmailCampaign campaign = ensureCampaign();
        for (ProductStockSubscriptionEntity subscription : subscriptions) {
            processedCounter.increment();
            ProdutoEntity produto = subscription.getProduto();
            if (produto == null || produto.getEstoque() == null || produto.getEstoque() <= 0 || !Boolean.TRUE.equals(produto.getDisponivel())) {
                skippedCounter.increment();
                continue;
            }
            enqueueNotification(campaign, subscription);
            subscriptionService.markNotified(subscription);
        }
    }

    private void enqueueNotification(EmailCampaign campaign, ProductStockSubscriptionEntity subscription) {
        EmailCampaignQueue queue = new EmailCampaignQueue();
        queue.setCampaignId(campaign.getId());
        queue.setRecipientEmail(subscription.getRecipientEmail());
        queue.setRecipientName(subscription.getRecipientName());
        queue.setStatus("PENDING");
        queue.setScheduledAt(Instant.now());
        queue.setPayloadJson(buildPayload(subscription));
        queueRepository.save(queue);
        enqueuedCounter.increment();
        log.info("[automation] volta ao estoque enqueue para {} (produto {})", subscription.getRecipientEmail(), subscription.getProduto().getNome());
    }

    private String buildPayload(ProductStockSubscriptionEntity subscription) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("produtoId", subscription.getProduto().getId());
            payload.put("produtoNome", subscription.getProduto().getNome());
            payload.put("categoria", subscription.getProduto().getCategoria());
            payload.put("email", subscription.getRecipientEmail());
            payload.put("subscriptionId", subscription.getId());
            payload.put("snapshot", subscription.getProductSnapshot());
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            log.warn("[automation] payload volta estoque falhou", ex);
            return "{}";
        }
    }

    private EmailCampaign ensureCampaign() {
        return campaignRepository.findByTemplateKey(TEMPLATE_KEY)
                .orElseGet(this::createCampaign);
    }

    private EmailCampaign createCampaign() {
        EmailCampaign campaign = new EmailCampaign();
        campaign.setNome("Automação - Volta ao estoque");
        campaign.setAssunto("Seu produto voltou ao estoque");
        campaign.setTemplateKey(TEMPLATE_KEY);
        campaign.setStatus("SCHEDULED");
        campaign.setSegmentJson("{\"segmento\":\"BACK_IN_STOCK\"}");
        campaign.setValidationStatus("PENDING");
        campaign.setScheduledAt(Instant.now());
        campaign.setScheduledZone("UTC");
        return campaignRepository.save(campaign);
    }
}
