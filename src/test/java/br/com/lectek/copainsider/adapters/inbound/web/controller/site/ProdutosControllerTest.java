package br.com.lectek.copainsider.adapters.inbound.web.controller.site;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.lectek.copainsider.application.service.delivery.PublicDeliveryEstimateService;
import br.com.lectek.copainsider.application.view.DeliveryEstimateVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.ui.ExtendedModelMap;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProdutosControllerTest {

    private ProdutoRepository repo;
    private PublicDeliveryEstimateService deliveryEstimateService;
    private ProdutosController controller;

    @BeforeEach
    void setUp() {
        repo = mock(ProdutoRepository.class);
        deliveryEstimateService = mock(PublicDeliveryEstimateService.class);
        controller = new ProdutosController(repo, deliveryEstimateService);
    }

    @Test
    void listPublicUsaCategoriasPublicasSemFiltroCategoria() {
        var pageable = PageRequest.of(0, 18);
        var page = new PageImpl<>(List.of(produto(1L)));
        var model = new ExtendedModelMap();

        when(repo.searchPublicPage(eq("dipirona"), eq(pageable))).thenReturn(page);
        when(repo.findDistinctCategoriasPublicas()).thenReturn(List.of("ANALGESICOS"));
        when(deliveryEstimateService.estimateFor(null)).thenReturn(
                DeliveryEstimateVM.unavailable("Veja a entrega no seu CEP", "Entre para calcular.")
        );

        String view = controller.listPublic("dipirona", null, pageable, null, model);

        assertThat(view).isEqualTo("pages/cliente/produtos/lista");
        assertThat(model.getAttribute("categorias")).isEqualTo(List.of("ANALGESICOS"));
        assertThat(model.getAttribute("deliveryEstimate")).isEqualTo(
                DeliveryEstimateVM.unavailable("Veja a entrega no seu CEP", "Entre para calcular.")
        );
        verify(repo).searchPublicPage("dipirona", pageable);
        verify(repo).findDistinctCategoriasPublicas();
        verify(deliveryEstimateService).estimateFor(null);
    }

    @Test
    void listPublicUsaBuscaPorCategoriaQuandoCatPresente() {
        var pageable = PageRequest.of(0, 18);
        var page = new PageImpl<>(List.of(produto(2L)));
        var model = new ExtendedModelMap();

        when(repo.searchPublicPageByCategoria("dipirona", "ANALGESICOS", pageable)).thenReturn(page);
        when(repo.findDistinctCategoriasPublicas()).thenReturn(List.of("ANALGESICOS"));
        when(deliveryEstimateService.estimateFor(null)).thenReturn(
                DeliveryEstimateVM.unavailable("Veja a entrega no seu CEP", "Entre para calcular.")
        );

        String view = controller.listPublic("dipirona", " ANALGESICOS ", pageable, null, model);

        assertThat(view).isEqualTo("pages/cliente/produtos/lista");
        verify(repo).searchPublicPageByCategoria("dipirona", "ANALGESICOS", pageable);
        verify(repo).findDistinctCategoriasPublicas();
        verify(deliveryEstimateService).estimateFor(null);
    }

    @Test
    void listPublicRedirecionaParaVitrineQuandoBuscaVaziaESemCategoria() {
        var pageable = PageRequest.of(0, 18);
        var model = new ExtendedModelMap();

        String view = controller.listPublic("   ", null, pageable, null, model);

        assertThat(view).isEqualTo("redirect:/produtos");
    }

    private static ProdutoEntity produto(Long id) {
        ProdutoEntity entity = new ProdutoEntity();
        entity.setId(id);
        entity.setNome("Produto " + id);
        return entity;
    }
}
