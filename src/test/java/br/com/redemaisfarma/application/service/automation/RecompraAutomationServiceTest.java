package br.com.redemaisfarma.application.service.automation;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaign;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaignQueue;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ItemPedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailCampaignQueueRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailCampaignRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ItemPedidoJpaRepository;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecompraAutomationServiceTest {
    @Mock
    private ItemPedidoJpaRepository itemRepository;
    @Mock
    private EmailCampaignRepository campaignRepository;
    @Mock
    private EmailCampaignQueueRepository queueRepository;

    private RecompraAutomationService service;

    @BeforeEach
    void setUp() {
        service = new RecompraAutomationService(
                itemRepository,
                campaignRepository,
                queueRepository,
                new SimpleMeterRegistry(),
                new ObjectMapper()
        );
        service.init();
    }

    @Test
    void shouldEnqueueCandidate() {
        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(1L);
        cliente.setEmail("user@example.com");
        cliente.setNome("Usuário");

        PedidoEntity pedido = new PedidoEntity();
        pedido.setId(2L);
        pedido.setCliente(cliente);
        pedido.setTotal(BigDecimal.valueOf(120));
        pedido.setData(LocalDateTime.now().minusDays(31));
        pedido.setStatus(StatusPedido.ENTREGUE);

        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(5L);
        produto.setNome("Dipirona 500mg");
        produto.setCategoria("Medicamentos");
        produto.setDisponivel(true);
        produto.setEstoque(10);

        ItemPedidoEntity item = new ItemPedidoEntity();
        item.setPedido(pedido);
        item.setProduto(produto);
        item.setQuantidade(1);
        item.setSubtotal(BigDecimal.valueOf(120));

        when(itemRepository.findByPedidoStatusAndPedidoDataBetweenAndProdutoDisponivelTrueOrderByPedidoDataDesc(
                any(), any(), any(), any()))
                .thenReturn(List.of(item));

        when(campaignRepository.findByTemplateKey("mail/recompra"))
                .thenReturn(Optional.empty());
        when(campaignRepository.save(any())).thenAnswer(invocation -> {
            EmailCampaign campaign = invocation.getArgument(0);
            campaign.setId(42L);
            return campaign;
        });
        when(queueRepository.findByCampaignIdAndStatusIn(anyLong(), anyList()))
                .thenReturn(List.of());

        service.runRecompra();

        ArgumentCaptor<EmailCampaignQueue> captor = ArgumentCaptor.forClass(EmailCampaignQueue.class);
        verify(queueRepository).save(captor.capture());
        EmailCampaignQueue queued = captor.getValue();
        assertThat(queued.getRecipientEmail()).isEqualTo("user@example.com");
        assertThat(queued.getCampaignId()).isEqualTo(42L);
        assertThat(queued.getPayloadJson()).contains("Dipirona");
        assertThat(queued.getPayloadJson()).contains("\"thresholdDays\":30");
    }

    @Test
    void shouldSkipAlreadyQueuedProduct() {
        ItemPedidoEntity item = new ItemPedidoEntity();
        ClienteEntity cliente = new ClienteEntity();
        cliente.setEmail("user@example.com");
        cliente.setNome("Usuário");

        PedidoEntity pedido = new PedidoEntity();
        pedido.setCliente(cliente);
        pedido.setTotal(BigDecimal.valueOf(90));
        pedido.setData(LocalDateTime.now().minusDays(31));
        pedido.setStatus(StatusPedido.ENTREGUE);
        item.setPedido(pedido);

        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(8L);
        produto.setNome("Medicamento Uso Contínuo");
        produto.setCategoria("Medicamentos");
        produto.setDisponivel(true);
        produto.setEstoque(5);
        item.setProduto(produto);
        item.setQuantidade(1);
        item.setSubtotal(BigDecimal.valueOf(90));

        when(itemRepository.findByPedidoStatusAndPedidoDataBetweenAndProdutoDisponivelTrueOrderByPedidoDataDesc(
                any(), any(), any(), any()))
                .thenReturn(List.of(item));

        EmailCampaign campaign = new EmailCampaign();
        campaign.setId(77L);
        when(campaignRepository.findByTemplateKey("mail/recompra"))
                .thenReturn(Optional.of(campaign));
        when(queueRepository.findByCampaignIdAndStatusIn(anyLong(), anyList()))
                .thenReturn(List.of(createExistingQueue(77L, "user@example.com", produto.getId())));

        service.runRecompra();

        verify(queueRepository, never()).save(any());
    }

    private EmailCampaignQueue createExistingQueue(Long campaignId, String email, Long productId) {
        EmailCampaignQueue queue = new EmailCampaignQueue();
        queue.setCampaignId(campaignId);
        queue.setRecipientEmail(email);
        queue.setStatus("PENDING");
        queue.setPayloadJson("{\"produtoId\":" + productId + "}");
        return queue;
    }
}
