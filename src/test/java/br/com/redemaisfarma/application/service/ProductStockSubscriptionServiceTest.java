package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProductStockSubscriptionEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ProductStockSubscriptionRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductStockSubscriptionServiceTest {

    @Mock
    private ProductStockSubscriptionRepository repository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ProductStockSubscriptionService service;

    @Test
    void subscribeReativaAssinaturaExistenteQuandoJaExisteEmail() throws Exception {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(12L);
        produto.setNome("Dipirona");
        produto.setCategoria("Medicacao");
        produto.setPrecoVenda(BigDecimal.valueOf(10));

        ProductStockSubscriptionEntity existente = new ProductStockSubscriptionEntity();
        existente.setId(44L);
        existente.setProduto(produto);
        existente.setRecipientEmail("user@example.com");
        existente.setStatus("NOTIFIED");
        existente.setNotifiedAt(LocalDateTime.now().minusDays(1));

        when(produtoRepository.findStockSubscribableById(12L)).thenReturn(Optional.of(produto));
        when(repository.existsByProdutoIdAndRecipientEmailIgnoreCase(12L, "user@example.com"))
                .thenReturn(true);
        when(repository.findFirstByProdutoIdAndRecipientEmailIgnoreCaseAndStatus(
                12L,
                "user@example.com",
                "SUBSCRIBED"
        )).thenReturn(Optional.empty());
        when(repository.findFirstByProdutoIdAndRecipientEmailIgnoreCase(12L, "user@example.com"))
                .thenReturn(Optional.of(existente));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"produtoId\":12}");
        when(repository.save(any(ProductStockSubscriptionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductStockSubscriptionEntity result = service.subscribe(12L, "USER@example.com", "Cliente");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(44L);
        assertThat(result.getStatus()).isEqualTo("SUBSCRIBED");
        assertThat(result.getNotifiedAt()).isNull();
        assertThat(result.getProductSnapshot()).contains("produtoId");
    }

    @Test
    void subscribeFalhaQuandoProdutoNaoEstaElegivelParaAssinatura() {
        when(produtoRepository.findStockSubscribableById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.subscribe(99L, "cliente@exemplo.com", "Cliente"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("indisponivel para assinatura de estoque");
    }

    @Test
    void markNotifiedAtualizaStatusETimestamp() {
        ProductStockSubscriptionEntity entity = new ProductStockSubscriptionEntity();
        entity.setStatus("SUBSCRIBED");

        service.markNotified(entity);

        ArgumentCaptor<ProductStockSubscriptionEntity> captor =
                ArgumentCaptor.forClass(ProductStockSubscriptionEntity.class);
        verify(repository).save(captor.capture());
        ProductStockSubscriptionEntity saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo("NOTIFIED");
        assertThat(saved.getNotifiedAt()).isNotNull();
    }
}
