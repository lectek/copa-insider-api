package br.com.redemaisfarma.application.service.automation;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaign;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaignQueue;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailCampaignQueueRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailCampaignRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.redemaisfarma.application.service.validation.CartValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CartAbandonmentAutomationServiceTest {

    @Mock
    private CartSnapshotProvider snapshotProvider;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private EmailCampaignRepository campaignRepository;

    @Mock
    private EmailCampaignQueueRepository queueRepository;

    @Mock
    private CartValidationService cartValidationService;

    @Captor
    private ArgumentCaptor<EmailCampaignQueue> queueCaptor;

    private CartAbandonmentAutomationService service;

    @BeforeEach
    void setUp() {
        when(cartValidationService.validate(any())).thenReturn(new CartValidationService.CartValidationResult(true, ""));
        service = new CartAbandonmentAutomationService(snapshotProvider, clienteRepository, campaignRepository, queueRepository, cartValidationService, new SimpleMeterRegistry(), new ObjectMapper());
    }

    @Test
    void shouldSkipWhenNoSnapshots() {
        when(snapshotProvider.findAbandonedCarts(any(Duration.class), anyInt())).thenReturn(List.of());
        service.processAbandonedCarts();
        verify(queueRepository, times(0)).save(any());
    }

    @Test
    void shouldEnqueueForSnapshot() {
        CartSnapshot.CartSnapshotItem item = new CartSnapshot.CartSnapshotItem(10L, 2);
        CartSnapshot snapshot = new CartSnapshot(1L, Instant.now(), BigDecimal.valueOf(150), List.of(item));
        when(snapshotProvider.findAbandonedCarts(any(Duration.class), anyInt()))
                .thenReturn(List.of(snapshot))
                .thenReturn(List.of());

        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(1L);
        cliente.setEmail("user@example.com");
        cliente.setNome("User");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        when(campaignRepository.findByTemplateKey("mail/cart-abandon")).thenReturn(Optional.empty());
        when(campaignRepository.save(any())).thenAnswer(invocation -> {
            EmailCampaign campaign = invocation.getArgument(0);
            campaign.setId(5L);
            return campaign;
        });
        when(queueRepository.findByCampaignIdAndStatusIn(any(), any())).thenReturn(Collections.emptyList());

        service.processAbandonedCarts();

        verify(queueRepository).save(queueCaptor.capture());
        EmailCampaignQueue queued = queueCaptor.getValue();
        assertThat(queued.getRecipientEmail()).isEqualTo("user@example.com");
        assertThat(queued.getCampaignId()).isEqualTo(5L);
        assertThat(queued.getPayloadJson()).contains("\"clienteId\":1");
    }
}
