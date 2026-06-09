package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.EmailCampaign;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.EmailCampaignLog;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.EmailCampaignQueue;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.EmailCampaignLogRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.EmailCampaignQueueRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.EmailCampaignRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailCampaignQueueWorkerTest {

    @Mock
    private EmailCampaignQueueRepository queueRepository;

    @Mock
    private EmailCampaignRepository campaignRepository;

    @Mock
    private EmailCampaignLogRepository logRepository;

    @Mock
    private MailService mailService;

    private EmailCampaignQueueWorker worker;
    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        worker = new EmailCampaignQueueWorker(
                queueRepository,
                campaignRepository,
                logRepository,
                mailService,
                meterRegistry,
                new ObjectMapper()
        );
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("email.campaign.worker.max-attempts");
    }

    @Test
    void processBatchSendsCampaignAndLogs() {
        EmailCampaignQueue payload = buildQueue(1L, "user@example.com");
        EmailCampaign campaign = new EmailCampaign();
        campaign.setId(1L);
        campaign.setAssunto("Campanha");
        campaign.setTemplateKey("mail/promo");

        when(queueRepository.findReady(eq("PENDING"), any(Instant.class), any(PageRequest.class)))
                .thenReturn(List.of(payload));
        when(campaignRepository.findById(payload.getCampaignId())).thenReturn(Optional.of(campaign));

        worker.processBatch();

        Assertions.assertThat(payload.getStatus()).isEqualTo("SENT");
        verify(mailService).sendTemplate(
                eq("user@example.com"),
                eq("Campanha"),
                eq("mail/promo"),
                anyMap(),
                eq(null)
        );
        verify(logRepository).save(any(EmailCampaignLog.class));
        Assertions.assertThat(meterRegistry.get("email_campaign.worker.processed").counter().count()).isEqualTo(1.0);
        Assertions.assertThat(meterRegistry.get("email_campaign.worker.sent").counter().count()).isEqualTo(1.0);
    }

    @Test
    void processBatchRetriesWhenSendFails() {
        EmailCampaignQueue payload = buildQueue(2L, "fail@example.com");
        EmailCampaign campaign = new EmailCampaign();
        campaign.setId(2L);
        campaign.setAssunto("Campanha erro");
        campaign.setTemplateKey("mail/promo");

        when(queueRepository.findReady(eq("PENDING"), any(Instant.class), any(PageRequest.class)))
                .thenReturn(List.of(payload));
        when(campaignRepository.findById(payload.getCampaignId())).thenReturn(Optional.of(campaign));
        doThrow(new IllegalStateException("SMTP indisponivel"))
                .when(mailService)
                .sendTemplate(anyString(), anyString(), anyString(), anyMap(), eq(null));

        worker.processBatch();

        Assertions.assertThat(payload.getAttempts()).isEqualTo(1);
        Assertions.assertThat(payload.getScheduledAt()).isNotNull();

        ArgumentCaptor<EmailCampaignLog> captor = ArgumentCaptor.forClass(EmailCampaignLog.class);
        verify(logRepository).save(captor.capture());
        Assertions.assertThat(captor.getValue().getStatus()).isEqualTo("FAILED");
        Assertions.assertThat(captor.getValue().getErrorText()).contains("SMTP indisponivel");
        Assertions.assertThat(meterRegistry.get("email_campaign.worker.processed").counter().count()).isEqualTo(1.0);
        Assertions.assertThat(meterRegistry.get("email_campaign.worker.retry").counter().count()).isEqualTo(1.0);
        Assertions.assertThat(meterRegistry.get("email_campaign.worker.failed").counter().count()).isEqualTo(0.0);
        Assertions.assertThat(meterRegistry.get("email_campaign.worker.sent").counter().count()).isEqualTo(0.0);
    }

    @Test
    void processBatchMarksFailedAfterMaxAttempts() {
        System.setProperty("email.campaign.worker.max-attempts", "1");
        EmailCampaignQueue payload = buildQueue(3L, "final@example.com");
        EmailCampaign campaign = new EmailCampaign();
        campaign.setId(3L);
        campaign.setAssunto("Campanha final");
        campaign.setTemplateKey("mail/final");

        when(queueRepository.findReady(eq("PENDING"), any(Instant.class), any(PageRequest.class)))
                .thenReturn(List.of(payload));
        when(campaignRepository.findById(payload.getCampaignId())).thenReturn(Optional.of(campaign));
        doThrow(new IllegalStateException("SMTP indisponivel"))
                .when(mailService)
                .sendTemplate(anyString(), anyString(), anyString(), anyMap(), eq(null));

        worker.processBatch();

        Assertions.assertThat(meterRegistry.get("email_campaign.worker.processed").counter().count()).isEqualTo(1.0);
        Assertions.assertThat(meterRegistry.get("email_campaign.worker.failed").counter().count()).isEqualTo(1.0);
        Assertions.assertThat(meterRegistry.get("email_campaign.worker.retry").counter().count()).isEqualTo(0.0);
    }

    private EmailCampaignQueue buildQueue(Long campaignId, String email) {
        EmailCampaignQueue queue = new EmailCampaignQueue();
        queue.setCampaignId(campaignId);
        queue.setRecipientEmail(email);
        queue.setRecipientName("Test");
        queue.setPayloadJson("{\"foo\":\"bar\"}");
        queue.setStatus("PENDING");
        queue.setScheduledAt(Instant.now());
        return queue;
    }
}
