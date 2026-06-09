package br.com.redemaisfarma.adapters.inbound.web.controller.site;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.TarjaMedicacao;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.application.service.delivery.PublicDeliveryEstimateService;
import br.com.redemaisfarma.application.view.DeliveryEstimateVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProdutoDetalheControllerTest {

    private ProdutoRepository produtoRepository;
    private PublicDeliveryEstimateService deliveryEstimateService;
    private ProdutoDetalheController controller;

    @BeforeEach
    void setUp() {
        produtoRepository = mock(ProdutoRepository.class);
        deliveryEstimateService = mock(PublicDeliveryEstimateService.class);
        controller = new ProdutoDetalheController(produtoRepository, deliveryEstimateService);
    }

    @Test
    void detalheRetorna404QuandoProdutoNaoEstaPublico() {
        when(produtoRepository.findPublicById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> controller.detalhe(99L, null, new ExtendedModelMap())
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void detalhePreencheModeloQuandoProdutoEhPublico() {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(10L);
        produto.setNome("Dipirona");
        produto.setEstoque(0);
        produto.setExigeReceita(true);
        produto.setTarjaMedicacao(TarjaMedicacao.TARJA_VERMELHA);
        produto.setCategoria("Analgésico");
        when(produtoRepository.findPublicById(10L)).thenReturn(Optional.of(produto));
        when(deliveryEstimateService.estimateFor(null)).thenReturn(
                DeliveryEstimateVM.available("Entrega hoje por volta de 14:35", "Parada 2 de 5 na rota.")
        );
        ProdutoEntity relacionado = new ProdutoEntity();
        relacionado.setId(11L);
        relacionado.setNome("Dipirona Gotas");
        relacionado.setCategoria("Analgésico");
        when(produtoRepository.searchPublicPageByCategoria(
                null,
                "Analgésico",
                PageRequest.of(0, 7)
        )).thenReturn(new PageImpl<>(List.of(produto, relacionado)));
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.detalhe(10L, null, model);

        assertThat(view).isEqualTo("pages/cliente/produtos/detalhe");
        assertThat(model.getAttribute("produto")).isEqualTo(produto);
        assertThat(model.getAttribute("outOfStock")).isEqualTo(true);
        assertThat(model.getAttribute("exigeReceita")).isEqualTo(true);
        assertThat(model.getAttribute("tarjaMedicacao")).isEqualTo(TarjaMedicacao.TARJA_VERMELHA);
        assertThat(model.getAttribute("tarjaDescricao")).isEqualTo(
                TarjaMedicacao.TARJA_VERMELHA.getDescricaoRegulatoria()
        );
        assertThat(model.getAttribute("deliveryEstimate")).isEqualTo(
                DeliveryEstimateVM.available("Entrega hoje por volta de 14:35", "Parada 2 de 5 na rota.")
        );
        assertThat((java.util.List<?>) model.getAttribute("relatedCards")).hasSize(1);
    }
}
