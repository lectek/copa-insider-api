package br.com.lectek.copainsider.application.service.automation;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.EmailCampaign;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.EmailCampaignQueue;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.EmailCampaignQueueRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.EmailCampaignRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ClienteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReengagementAutomationService {
    private static final Logger log = LoggerFactory.getLogger(ReengagementAutomationService.class);
    private static final String TEMPLATE_KEY = "mail/promo";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_SCHEDULED = "SCHEDULED";

    private final EmailCampaignRepository campaignRepository;
    private final EmailCampaignQueueRepository queueRepository;
    private final ClienteRepository clienteRepository;
    private final ObjectMapper objectMapper;

    private final int[] thresholds = {30, 60, 90};

    public ReengagementAutomationService(
            EmailCampaignRepository campaignRepository,
            EmailCampaignQueueRepository queueRepository,
            ClienteRepository clienteRepository,
            ObjectMapper objectMapper
    ) {
        this.campaignRepository = campaignRepository;
        this.queueRepository = queueRepository;
        this.clienteRepository = clienteRepository;
        this.objectMapper = objectMapper;
    }

    @Scheduled(cron = "${reengagement.scheduler.cron:0 5 3 * * *}")
    public void runReengagements() {
        for (int days : thresholds) {
            try {
                processThreshold(days);
            } catch (Exception ex) {
                log.error("[automation] reengagement {}d falhou", days, ex);
            }
        }
    }

    @Transactional
    protected void processThreshold(int days) {
        Instant scheduledAt = Instant.now();
        EmailCampaign campaign = ensureCampaign(days);
        if (campaign == null) {
            log.warn("[automation] reengagement {}d nao possui campanha", days);
            return;
        }
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        List<ClienteEntity> clientes = clienteRepository.findInativosAntesDe(cutoff);
        Set<String> queuedEmails = queueRepository.findByCampaignIdAndStatusIn(
                        campaign.getId(),
                        List.of(STATUS_PENDING, "SENDING", "SENT")
                ).stream()
                .map(EmailCampaignQueue::getRecipientEmail)
                .collect(Collectors.toSet());

        int queued = 0;
        for (ClienteEntity cliente : clientes) {
            if (cliente.getEmail() == null || cliente.getEmail().isBlank()) {
                continue;
            }
            if (queuedEmails.contains(cliente.getEmail().toLowerCase())) {
                continue;
            }
            EmailCampaignQueue queue = new EmailCampaignQueue();
            queue.setCampaignId(campaign.getId());
            queue.setRecipientEmail(cliente.getEmail().toLowerCase());
            queue.setRecipientName(cliente.getNome());
            queue.setStatus(STATUS_PENDING);
            queue.setScheduledAt(scheduledAt);
            queue.setPayloadJson(buildPayload(campaign.getNome(), days));
            queueRepository.save(queue);
            queued++;
        }
        if (queued > 0) {
            log.info("[automation] reengagement {}d enfileirou {} clientes", days, queued);
        }
    }

    private EmailCampaign ensureCampaign(int days) {
        String key = TEMPLATE_KEY;
        String name = "Automação - Reengajamento " + days + "d";
        return campaignRepository.findByTemplateKey(key).orElseGet(() -> {
            EmailCampaign campaign = new EmailCampaign();
            campaign.setNome(name);
            campaign.setAssunto("Sentimos sua falta! Confira o que preparamos");
            campaign.setTemplateKey(key);
            campaign.setStatus(STATUS_SCHEDULED);
            campaign.setSegmentJson(buildSegmentJson(days));
            campaign.setScheduledAt(Instant.now());
            campaign.setScheduledZone("UTC");
            campaign.setValidationStatus(STATUS_PENDING);
            return campaignRepository.save(campaign);
        });
    }

    private String buildPayload(String campaignName, int days) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("headline", campaignName);
            payload.put("message", "Volte hoje e aproveite descontos especiais para clientes inativos há " + days + " dias.");
            payload.put("days", days);
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            return "{}";
        }
    }

    private String buildSegmentJson(int days) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("segmento", "REENGAGEMENT");
            payload.put("days", days);
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            return "{}";
        }
    }
}
