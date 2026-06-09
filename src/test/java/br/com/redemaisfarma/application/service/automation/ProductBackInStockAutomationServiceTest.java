package br.com.redemaisfarma.application.service.automation;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaign;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaignQueue;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProductStockSubscriptionEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailCampaignQueueRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailCampaignRepository;
import br.com.redemaisfarma.application.service.ProductStockSubscriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductBackInStockAutomationServiceTest {

    @Mock
    private EmailCampaignRepository campaignRepository;
    @Mock
    private EmailCampaignQueueRepository queueRepository;
    @Mock
    private ProductStockSubscriptionService subscriptionService;

    private ProductBackInStockAutomationService service;

    @BeforeEach
    void setUp() {
        service = new ProductBackInStockAutomationService(
                campaignRepository,
                queueRepository,
                subscriptionService,
                new SimpleMeterRegistry(),
                new ObjectMapper()
        );
    }

    @Test
    void shouldQueueWhenProductBackInStock() {
        ProductStockSubscriptionEntity subscription = buildSubscription(1L, "user@example.com");
        when(subscriptionService.findPending(200)).thenReturn(List.of(subscription));

        when(campaignRepository.findByTemplateKey("mail/back-in-stock")).thenReturn(Optional.empty());
        when(campaignRepository.save(any())).thenAnswer(invocation -> {
            EmailCampaign campaign = invocation.getArgument(0);
            campaign.setId(101L);
            return campaign;
        });

        service.processSubscriptions();

        ArgumentCaptor<EmailCampaignQueue> captor = ArgumentCaptor.forClass(EmailCampaignQueue.class);
        verify(queueRepository).save(captor.capture());
        EmailCampaignQueue queue = captor.getValue();
        assertThat(queue.getRecipientEmail()).isEqualTo("user@example.com");
        assertThat(queue.getCampaignId()).isEqualTo(101L);
        verify(subscriptionService).markNotified(subscription);
    }

    @Test
    void shouldSkipWhenOutOfStock() {
        ProductStockSubscriptionEntity subscription = buildSubscription(2L, "skip@example.com");
        subscription.getProduto().setEstoque(0);
        when(subscriptionService.findPending(200)).thenReturn(List.of(subscription));

        service.processSubscriptions();

        verify(queueRepository, never()).save(any());
        verify(subscriptionService, never()).markNotified(subscription);
    }

    private ProductStockSubscriptionEntity buildSubscription(Long produtoId, String email) {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(produtoId);
        produto.setNome("Produto " + produtoId);
        produto.setCategoria("Medicamento");
        produto.setPrecoVenda(BigDecimal.valueOf(10));
        produto.setDisponivel(true);
        produto.setEstoque(5);

        ProductStockSubscriptionEntity subscription = new ProductStockSubscriptionEntity();
        subscription.setProduto(produto);
        subscription.setRecipientEmail(email);
        subscription.setRecipientName("User");
        subscription.setProductSnapshot("{\"produtoId\":" + produtoId + "}");
        return subscription;
    }
}
