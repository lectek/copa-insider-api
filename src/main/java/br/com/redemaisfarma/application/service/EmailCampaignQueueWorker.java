package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaign;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaignLog;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaignQueue;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailCampaignLogRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailCampaignQueueRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailCampaignRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailCampaignQueueWorker {
    private static final Logger log = LoggerFactory.getLogger(EmailCampaignQueueWorker.class);
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_SENDING = "SENDING";
    private static final String STATUS_SENT = "SENT";
    private static final String STATUS_FAILED = "FAILED";
    private static final String PROVIDER_SMTP = "SMTP";

    private final EmailCampaignQueueRepository queueRepository;
    private final EmailCampaignRepository campaignRepository;
    private final EmailCampaignLogRepository logRepository;
    private final MailService mailService;
    private final ObjectMapper objectMapper;
    private final Counter processedCounter;
    private final Counter sentCounter;
    private final Counter failedCounter;
    private final Counter retryCounter;

    public EmailCampaignQueueWorker(
            EmailCampaignQueueRepository queueRepository,
            EmailCampaignRepository campaignRepository,
            EmailCampaignLogRepository logRepository,
            MailService mailService,
            MeterRegistry meterRegistry,
            ObjectMapper objectMapper
    ) {
        this.queueRepository = queueRepository;
        this.campaignRepository = campaignRepository;
        this.logRepository = logRepository;
        this.mailService = mailService;
        this.objectMapper = objectMapper;
        this.processedCounter = Counter.builder("email_campaign.worker.processed")
                .description("Total de registros processados pelo worker de campanhas")
                .register(meterRegistry);
        this.sentCounter = Counter.builder("email_campaign.worker.sent")
                .description("Emails enviados com sucesso pelo worker")
                .register(meterRegistry);
        this.failedCounter = Counter.builder("email_campaign.worker.failed")
                .description("Falhas definitivas do worker de campanhas")
                .register(meterRegistry);
        this.retryCounter = Counter.builder("email_campaign.worker.retry")
                .description("Reenfileiramentos realizados após falha")
                .register(meterRegistry);
    }

    @Scheduled(fixedDelayString = "${email.campaign.worker.fixed-delay-ms:60000}")
    @Transactional
    public void processBatch() {
        int batchSize = Math.max(1, Integer.getInteger("email.campaign.worker.batch-size", 50));
        List<EmailCampaignQueue> batch = queueRepository.findReady(
                STATUS_PENDING,
                Instant.now(),
                PageRequest.of(0, batchSize)
        );
        if (batch.isEmpty()) {
            return;
        }
        for (EmailCampaignQueue item : batch) {
            processItem(item);
            throttle();
        }
    }

    private void processItem(EmailCampaignQueue item) {
        processedCounter.increment();
        item.setStatus(STATUS_SENDING);
        queueRepository.save(item);

        EmailCampaign campaign = campaignRepository.findById(item.getCampaignId()).orElse(null);
        if (campaign == null) {
            markFailed(item, "Campanha nao encontrada.");
            return;
        }

        try {
            Map<String, Object> model = readPayload(item.getPayloadJson());
            model.putIfAbsent("recipientEmail", item.getRecipientEmail());
            model.putIfAbsent("recipientName", item.getRecipientName());
            mailService.sendTemplate(
                    item.getRecipientEmail(),
                    campaign.getAssunto(),
                    campaign.getTemplateKey(),
                    model,
                    null
            );
            item.setStatus(STATUS_SENT);
            queueRepository.save(item);
            logRepository.save(buildLog(item, campaign.getId(), STATUS_SENT, null, null));
            sentCounter.increment();
        } catch (Exception ex) {
            String error = ex.getMessage() == null ? "Falha ao enviar email." : ex.getMessage();
            markFailed(item, error);
            logRepository.save(buildLog(item, campaign.getId(), STATUS_FAILED, error, null));
            log.warn("[email-campaign] falha ao enviar para {}: {}", item.getRecipientEmail(), error);
        }
    }

    private void markFailed(EmailCampaignQueue item, String error) {
        int attempts = item.getAttempts() == null ? 0 : item.getAttempts();
        int maxAttempts = Math.max(1, Integer.getInteger("email.campaign.worker.max-attempts", 3));
        attempts += 1;
        item.setAttempts(attempts);
        item.setLastError(error);
        if (attempts >= maxAttempts) {
            item.setStatus(STATUS_FAILED);
            queueRepository.save(item);
            failedCounter.increment();
            return;
        }
        retryCounter.increment();
        item.setStatus(STATUS_PENDING);
        item.setScheduledAt(nextRetry(attempts));
        queueRepository.save(item);
    }

    private EmailCampaignLog buildLog(EmailCampaignQueue item, Long campaignId, String status, String error, String messageId) {
        EmailCampaignLog logItem = new EmailCampaignLog();
        logItem.setCampaignId(campaignId);
        logItem.setRecipientEmail(item.getRecipientEmail());
        logItem.setStatus(status);
        logItem.setProvider(PROVIDER_SMTP);
        logItem.setMessageId(messageId);
        logItem.setErrorText(error);
        logItem.setSentAt(STATUS_SENT.equals(status) ? Instant.now() : null);
        return logItem;
    }

    private Map<String, Object> readPayload(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            return new HashMap<>();
        }
        try {
            Map<String, Object> map = objectMapper.readValue(payloadJson, new TypeReference<Map<String, Object>>() {});
            return map == null ? new HashMap<>() : map;
        } catch (Exception ex) {
            return new HashMap<>();
        }
    }

    private Instant nextRetry(int attempts) {
        long baseSeconds = 60L;
        long delay = Math.min(3600L, baseSeconds * attempts);
        return Instant.now().plusSeconds(delay);
    }

    private void throttle() {
        long delayMs = Long.getLong("email.campaign.worker.throttle-ms", 0L);
        if (delayMs <= 0) {
            return;
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
