package br.com.lectek.copainsider.adapters.inbound.web;

import br.com.lectek.copainsider.application.view.ProductCardVM;
import br.com.lectek.copainsider.domain.catalogo.ProdutoQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PublicProdutoControllerTest {

    private ProdutoQueryService service;
    private PublicProdutoController controller;

    @BeforeEach
    void setUp() {
        service = mock(ProdutoQueryService.class);
        controller = new PublicProdutoController(service);
    }

    @Test
    void novidadesLimitaPaginaEm24() {
        when(service.newArrivals(any(), eq(false))).thenReturn(List.of(card(1L)));

        controller.novidades(50);

        verify(service).newArrivals(PageRequest.of(0, 24), false);
    }

    @Test
    void paraVoceMantemLimiteSolicitadoQuandoValido() {
        when(service.recommended(any(), eq(false))).thenReturn(List.of(card(2L)));

        controller.paraVoce(7);

        verify(service).recommended(PageRequest.of(0, 7), false);
    }

    @Test
    void maisVendidosUsaLimitePadraoQuandoRecebeZeroOuNegativo() {
        when(service.topSellers(any(), eq(false))).thenReturn(List.of(card(3L)));

        controller.maisVendidos(0);

        verify(service).topSellers(PageRequest.of(0, 12), false);
    }

    private ProductCardVM card(Long id) {
        return new ProductCardVM(
                id,
                "Produto " + id,
                "/img/" + id + ".png",
                BigDecimal.TEN,
                null,
                null,
                true,
                5,
                "MEDICACOES"
        );
    }
}
