package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.MetodoLeituraCodigoBarras;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.lectek.copainsider.application.service.validation.CartValidationService;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpSession;

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CartServiceTest {

    @Mock(answer = Answers.RETURNS_SMART_NULLS)
    private ProdutoRepository produtoRepository;

    private CartService cartService;
    private Map<Long, ProdutoEntity> productCatalog;
    private CartValidationService validationService;

    @BeforeEach
    void setUp() {
        productCatalog = new HashMap<>();
        validationService = new CartValidationService(produtoRepository);
        cartService = new CartService(produtoRepository, validationService);
        when(produtoRepository.findAllByIdIn(anyCollection())).thenAnswer(invocation -> {
            Collection<Long> ids = invocation.getArgument(0);
            return ids.stream()
                    .map(productCatalog::get)
                    .filter(p -> p != null)
                    .toList();
        });
    }

    @Test
    void shouldRejectWhenProductQuantityLimitExceeded() {
        HttpSession session = new MockHttpSession();
        stubProduct(1L, BigDecimal.valueOf(100));
        cartService.addItem(session, 1L, 8);

        CartValidationService.CartValidationResult validation = cartService.validateAdd(session, 1L, 3);

        Assertions.assertThat(validation.valid()).isFalse();
        Assertions.assertThat(validation.message()).contains("Limite de " + CartValidationService.MAX_QUANTITY_PER_PRODUCT);
    }

    @Test
    void shouldRejectWhenCartValueExceedsLimit() {
        HttpSession session = new MockHttpSession();
        registerCatalogProduct(1L, BigDecimal.valueOf(2000));
        registerCatalogProduct(2L, BigDecimal.valueOf(2000));
        stubProduct(3L, BigDecimal.valueOf(1500));
        cartService.addItem(session, 1L, 1);
        cartService.addItem(session, 2L, 1);

        CartValidationService.CartValidationResult validation = cartService.validateAdd(session, 3L, 1);

        Assertions.assertThat(validation.valid()).isFalse();
        Assertions.assertThat(validation.message()).contains("Valor maximo do carrinho");
    }

    @Test
    void shouldRejectWhenDistinctProductLimitReached() {
        HttpSession session = new MockHttpSession();
        for (long id = 1; id <= CartValidationService.MAX_DISTINCT_ITEMS; id++) {
            registerCatalogProduct(id, BigDecimal.valueOf(50));
            cartService.addItem(session, id, 1);
        }
        stubProduct(CartValidationService.MAX_DISTINCT_ITEMS + 1L, BigDecimal.valueOf(50));

        CartValidationService.CartValidationResult validation = cartService.validateAdd(session, CartValidationService.MAX_DISTINCT_ITEMS + 1L, 1);

        Assertions.assertThat(validation.valid()).isFalse();
        Assertions.assertThat(validation.message()).contains("Limite de " + CartValidationService.MAX_DISTINCT_ITEMS + " produtos diferentes");
    }

    @Test
    void shouldRejectWhenProductIsNotPubliclySellable() {
        HttpSession session = new MockHttpSession();
        ProdutoEntity produto = registerCatalogProduct(1L, BigDecimal.valueOf(80));
        produto.setStatus(ProdutoStatus.IMPORTADO);

        CartValidationService.CartValidationResult validation = cartService.validateAdd(session, 1L, 1);

        Assertions.assertThat(validation.valid()).isFalse();
        Assertions.assertThat(validation.message()).contains("Produto indisponivel para venda.");
    }

    @Test
    void shouldPopulateUnitPriceWhenBuildingOrderData() {
        HttpSession session = new MockHttpSession();
        registerCatalogProduct(1L, new BigDecimal("12.34"));
        cartService.addItem(session, 1L, 3);

        CartService.CartOrderData orderData = cartService.buildOrderData(session);

        Assertions.assertThat(orderData.getItems()).hasSize(1);
        Assertions.assertThat(orderData.getItems().get(0).getPrecoUnitario())
                .isEqualByComparingTo("12.34");
        Assertions.assertThat(orderData.getItems().get(0).getSubtotal())
                .isEqualByComparingTo("37.02");
    }

    private ProdutoEntity stubProduct(Long id, BigDecimal preco) {
        ProdutoEntity produto = registerCatalogProduct(id, preco);
        when(produtoRepository.findById(eq(id))).thenReturn(Optional.of(produto));
        return produto;
    }

    private ProdutoEntity registerCatalogProduct(Long id, BigDecimal preco) {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(id);
        produto.setPrecoVenda(preco);
        produto.setDisponivel(true);
        produto.setEstoque(100);
        produto.setStatus(ProdutoStatus.PUBLICADO);
        produto.setMetodoLeituraCodigoBarras(
                MetodoLeituraCodigoBarras.MANUAL
        );
        produto.setPublicadoEm(LocalDateTime.now().minusMinutes(5));
        produto.setDespublicadoEm(LocalDateTime.now().plusDays(1));
        productCatalog.put(id, produto);
        return produto;
    }
}
