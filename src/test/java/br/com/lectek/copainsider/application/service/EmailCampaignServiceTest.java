package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.EmailCampaign;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.EmailCampaignQueue;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.EmailCampaignLogRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.EmailCampaignQueueRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.EmailCampaignRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.UsuarioJpaRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.lectek.copainsider.application.dto.request.EmailCampaignRequest;
import br.com.lectek.copainsider.application.dto.request.EmailCampaignValidationRequest;
import br.com.lectek.copainsider.application.dto.response.EmailCampaignResponse;
import br.com.lectek.copainsider.application.dto.response.CampaignQueuePreviewResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailCampaignServiceTest {
    @Mock
    private EmailCampaignRepository campaignRepository;

    @Mock
    private EmailCampaignQueueRepository queueRepository;

    @Mock
    private EmailCampaignLogRepository logRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private UsuarioJpaRepository usuarioRepository;

    @Mock
    private MailService mailService;

    private EmailCampaignService service;

    @BeforeEach
    void setUp() {
        service = new EmailCampaignService(
                campaignRepository,
                queueRepository,
                logRepository,
                clienteRepository,
                usuarioRepository,
                new ObjectMapper(),
                mailService
        );
        when(campaignRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldRebuildQueueWhenScheduleChanges() {
        EmailCampaign campaign = new EmailCampaign();
        campaign.setId(1L);
        campaign.setStatus("SCHEDULED");
        campaign.setScheduledAt(Instant.now().minusSeconds(3600));

        EmailCampaignQueue queued = new EmailCampaignQueue();
        queued.setId(5L);
        queued.setCampaignId(campaign.getId());
        queued.setStatus("PENDING");

        when(campaignRepository.findById(eq(campaign.getId()))).thenReturn(Optional.of(campaign));
        when(queueRepository.findByCampaignIdAndStatusIn(eq(campaign.getId()), eq(List.of("PENDING", "SENDING"))))
                .thenReturn(List.of(queued));

        ClienteEntity cliente = new ClienteEntity();
        cliente.setEmail("user@example.com");
        cliente.setNome("User");
        cliente.setAtivo(true);
        when(clienteRepository.findAll()).thenReturn(List.of(cliente));

        EmailCampaignRequest request = new EmailCampaignRequest();
        request.setEnvioImediato(true);

        EmailCampaignResponse response = service.update(campaign.getId(), request);

        Assertions.assertThat(response.getStatus()).isEqualTo("SCHEDULED");
        Assertions.assertThat(queued.getStatus()).isEqualTo("CANCELLED");

        ArgumentCaptor<EmailCampaignQueue> queueCaptor = ArgumentCaptor.forClass(EmailCampaignQueue.class);
        verify(queueRepository, times(1)).save(queueCaptor.capture());
        Assertions.assertThat(queueCaptor.getValue().getRecipientEmail()).isEqualTo("user@example.com");
        Assertions.assertThat(queueCaptor.getValue().getStatus()).isEqualTo("PENDING");

        verify(queueRepository, times(1)).saveAll(any());
    }

    @Test
    void shouldCaptureSegmentDetailTimezoneAndValidationOnCreate() {
        EmailCampaignRequest request = new EmailCampaignRequest();
        request.setNome("Det");
        request.setAssunto("Assunto");
        request.setTemplateKey("mail/promo");
        request.setSegmento("VIP");
        request.setSegmentoDetalhado("completo");
        request.setAgendarPara(LocalDateTime.now().plusDays(1));
        request.setAgendarTimezone("America/Sao_Paulo");
        request.setValidationStatus("APPROVED");

        EmailCampaignResponse response = service.create(request);

        Assertions.assertThat(response.getSegmentoDetalhado()).isEqualTo("completo");
        Assertions.assertThat(response.getScheduledZone()).isEqualTo("America/Sao_Paulo");
        Assertions.assertThat(response.getValidationStatus()).isEqualTo("APPROVED");
    }

    @Test
    void shouldCancelQueueWhenScheduleCleared() {
        EmailCampaign campaign = new EmailCampaign();
        campaign.setId(2L);
        campaign.setStatus("SCHEDULED");
        campaign.setScheduledAt(Instant.now().plusSeconds(3600));

        EmailCampaignQueue queued = new EmailCampaignQueue();
        queued.setCampaignId(campaign.getId());
        queued.setStatus("PENDING");

        when(campaignRepository.findById(eq(campaign.getId()))).thenReturn(Optional.of(campaign));
        when(queueRepository.findByCampaignIdAndStatusIn(eq(campaign.getId()), eq(List.of("PENDING", "SENDING"))))
                .thenReturn(List.of(queued));

        EmailCampaignRequest request = new EmailCampaignRequest();

        EmailCampaignResponse response = service.update(campaign.getId(), request);

        Assertions.assertThat(response.getStatus()).isEqualTo("DRAFT");
        Assertions.assertThat(queued.getStatus()).isEqualTo("CANCELLED");

        verify(queueRepository, never()).save(any());
        verify(queueRepository, times(1)).saveAll(any());
    }

    @Test
    void shouldPauseScheduledCampaign() {
        EmailCampaign campaign = new EmailCampaign();
        campaign.setId(3L);
        campaign.setStatus("SCHEDULED");
        campaign.setScheduledAt(Instant.now().plusSeconds(60));

        EmailCampaignQueue queued = new EmailCampaignQueue();
        queued.setId(10L);
        queued.setCampaignId(campaign.getId());
        queued.setStatus("PENDING");

        when(campaignRepository.findById(eq(campaign.getId()))).thenReturn(Optional.of(campaign));
        when(queueRepository.findByCampaignIdAndStatusIn(eq(campaign.getId()), eq(List.of("PENDING", "SENDING"))))
                .thenReturn(List.of(queued));

        EmailCampaignResponse response = service.pause(campaign.getId());

        Assertions.assertThat(campaign.getStatus()).isEqualTo("PAUSED");
        Assertions.assertThat(response.getStatus()).isEqualTo("PAUSED");
        Assertions.assertThat(queued.getStatus()).isEqualTo("CANCELLED");
        verify(queueRepository).saveAll(List.of(queued));
    }

    @Test
    void shouldResumePausedCampaign() {
        EmailCampaign campaign = new EmailCampaign();
        campaign.setId(4L);
        campaign.setStatus("PAUSED");
        campaign.setScheduledAt(Instant.now().minusSeconds(600));
        campaign.setSegmentJson("{\"segmento\":\"TODOS\"}");

        EmailCampaignQueue queued = new EmailCampaignQueue();
        queued.setCampaignId(campaign.getId());
        queued.setStatus("PENDING");

        ClienteEntity cliente = new ClienteEntity();
        cliente.setEmail("resume@example.com");
        cliente.setNome("Resume");
        cliente.setAtivo(true);

        when(campaignRepository.findById(eq(campaign.getId()))).thenReturn(Optional.of(campaign));
        when(queueRepository.findByCampaignIdAndStatusIn(eq(campaign.getId()), eq(List.of("PENDING", "SENDING"))))
                .thenReturn(List.of(queued));
        when(clienteRepository.findAll()).thenReturn(List.of(cliente));

        EmailCampaignResponse response = service.resume(campaign.getId());

        Assertions.assertThat(campaign.getStatus()).isEqualTo("SCHEDULED");
        Assertions.assertThat(response.getStatus()).isEqualTo("SCHEDULED");
        Assertions.assertThat(queued.getStatus()).isEqualTo("CANCELLED");

        ArgumentCaptor<EmailCampaignQueue> captor = ArgumentCaptor.forClass(EmailCampaignQueue.class);
        verify(queueRepository, times(1)).save(captor.capture());
        EmailCampaignQueue saved = captor.getValue();
        Assertions.assertThat(saved.getRecipientEmail()).isEqualTo("resume@example.com");
        Assertions.assertThat(saved.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void shouldUpdateValidationStatus() {
        EmailCampaign campaign = new EmailCampaign();
        campaign.setId(6L);
        campaign.setValidationStatus("PENDING");
        when(campaignRepository.findById(eq(campaign.getId()))).thenReturn(Optional.of(campaign));

        EmailCampaignValidationRequest request = new EmailCampaignValidationRequest();
        request.setStatus("approved");

        EmailCampaignResponse response = service.updateValidationStatus(campaign.getId(), request);

        Assertions.assertThat(response.getValidationStatus()).isEqualTo("APPROVED");
        Assertions.assertThat(campaign.getValidationStatus()).isEqualTo("APPROVED");
    }

    @Test
    void queuePreviewMetricsFiltersByStatusDateSegment() {
        EmailCampaign campaign = new EmailCampaign();
        campaign.setId(7L);
        campaign.setScheduledZone("UTC");
        campaign.setSegmentJson("{\"segmento\":\"VIP\"}");
        when(campaignRepository.findById(eq(campaign.getId()))).thenReturn(Optional.of(campaign));

        EmailCampaignQueue pending = new EmailCampaignQueue();
        pending.setCampaignId(7L);
        pending.setStatus("PENDING");
        pending.setScheduledAt(Instant.now().minusSeconds(3600));
        pending.setPayloadJson("{\"segmento\":\"VIP\"}");

        EmailCampaignQueue failed = new EmailCampaignQueue();
        failed.setCampaignId(7L);
        failed.setStatus("FAILED");
        failed.setScheduledAt(Instant.now());
        failed.setPayloadJson("{\"segmento\":\"VIP\"}");

        when(queueRepository.findByCampaignIdAndStatusIn(eq(7L), any()))
                .thenReturn(List.of(pending, failed));

        CampaignQueuePreviewResponse response = service.queuePreviewMetrics(
                campaign.getId(),
                List.of("PENDING", "FAILED"),
                Instant.now().minusSeconds(7200),
                Instant.now().plusSeconds(3600),
                "vip"
        );

        Assertions.assertThat(response.getTotal()).isEqualTo(2);
        Assertions.assertThat(response.getFailureRate()).isEqualTo(0.5);
        Assertions.assertThat(response.getStatusCounts().get("FAILED")).isEqualTo(1);
        Assertions.assertThat(response.getSample()).hasSize(2);
    }
}
