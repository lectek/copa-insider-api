package br.com.redemaisfarma.application.service.automation;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaign;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaignQueue;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailCampaignQueueRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailCampaignRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReengagementAutomationServiceTest {
    @Mock
    private EmailCampaignRepository campaignRepository;

    @Mock
    private EmailCampaignQueueRepository queueRepository;

    @Mock
    private ClienteRepository clienteRepository;

    private ReengagementAutomationService service;

    @BeforeEach
    void setUp() {
        service = new ReengagementAutomationService(
                campaignRepository,
                queueRepository,
                clienteRepository,
                new ObjectMapper()
        );
    }

    @Test
    void processThresholdEnqueuesClients() {
        EmailCampaign campaign = new EmailCampaign();
        campaign.setId(42L);
        doReturn(Optional.empty()).when(campaignRepository).findByTemplateKey("mail/promo");
        doReturn(campaign).when(campaignRepository).save(any());

        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(1L);
        cliente.setEmail("user@example.com");
        cliente.setNome("Usuário");
        doReturn(List.of(cliente)).when(clienteRepository).findInativosAntesDe(any(LocalDateTime.class));
        doReturn(List.of()).when(queueRepository).findByCampaignIdAndStatusIn(campaign.getId(), List.of("PENDING", "SENDING", "SENT"));

        service.processThreshold(30);

        ArgumentCaptor<EmailCampaignQueue> captor = ArgumentCaptor.forClass(EmailCampaignQueue.class);
        verify(queueRepository).save(captor.capture());
        EmailCampaignQueue queue = captor.getValue();
        assertThat(queue.getCampaignId()).isEqualTo(42L);
        assertThat(queue.getRecipientEmail()).isEqualTo("user@example.com");
        assertThat(queue.getStatus()).isEqualTo("PENDING");
        assertThat(queue.getScheduledAt()).isNotNull();
    }

}
