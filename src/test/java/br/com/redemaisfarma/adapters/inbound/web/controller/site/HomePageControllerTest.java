package br.com.redemaisfarma.adapters.inbound.web.controller.site;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.application.service.ProductCategorySectionService;
import br.com.redemaisfarma.application.service.delivery.PublicDeliveryEstimateService;
import br.com.redemaisfarma.application.view.DeliveryEstimateVM;
import br.com.redemaisfarma.application.view.ProductCategorySectionVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.ui.ExtendedModelMap;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HomePageControllerTest {

    private ProdutoRepository repo;
    private ProductCategorySectionService productCategorySectionService;
    private PublicDeliveryEstimateService deliveryEstimateService;
    private HomePageController controller;

    @BeforeEach
    void setUp() {
        repo = mock(ProdutoRepository.class);
        productCategorySectionService = mock(ProductCategorySectionService.class);
        deliveryEstimateService = mock(PublicDeliveryEstimateService.class);
        controller = new HomePageController(
                repo,
                productCategorySectionService,
                deliveryEstimateService
        );
    }

    @Test
    void listarProdutosUsaCatalogoPublicoComTotalReal() {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(10L);
        produto.setNome("Dipirona");
        PageRequest pageable = PageRequest.of(0, 12);
        PageImpl<ProdutoEntity> page = new PageImpl<>(List.of(produto), pageable, 1000);

        when(repo.searchPublicPage(null, pageable)).thenReturn(page);
        when(repo.findDistinctCategoriasPublicas()).thenReturn(List.of("ANALGESICOS"));
        when(productCategorySectionService.loadPublicSections(6)).thenReturn(List.of(
                new ProductCategorySectionVM(
                        "analgesicos",
                        "Analgesicos",
                        List.of("ANALGESICOS"),
                        6,
                        List.of()
                )
        ));
        when(deliveryEstimateService.estimateFor(null)).thenReturn(
                DeliveryEstimateVM.unavailable("Veja a entrega no seu CEP", "Entre para calcular.")
        );

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.listarProdutos(null, pageable, null, model);

        assertThat(view).isEqualTo("pages/cliente/produtos/lista");
        assertThat(model.getAttribute("categorias")).isEqualTo(List.of("ANALGESICOS"));
        assertThat(model.getAttribute("hasDisponiveis")).isEqualTo(true);
        assertThat(model.getAttribute("page")).isEqualTo(page);
        assertThat(model.getAttribute("showCategorySections")).isEqualTo(true);
        assertThat(page.getTotalElements()).isEqualTo(1000);
        verify(repo).searchPublicPage(null, pageable);
        verify(repo).findDistinctCategoriasPublicas();
        verify(productCategorySectionService).loadPublicSections(6);
    }

    @Test
    void listarProdutosUsaFiltroCategoriaSemBusca() {
        PageRequest pageable = PageRequest.of(0, 12);
        PageImpl<ProdutoEntity> page = new PageImpl<>(List.of(produto(11L)), pageable, 42);

        when(repo.searchPublicPageByCategoria(null, "HIGIENE", pageable)).thenReturn(page);
        when(repo.findDistinctCategoriasPublicas()).thenReturn(List.of("HIGIENE"));
        when(deliveryEstimateService.estimateFor(null)).thenReturn(
                DeliveryEstimateVM.unavailable("Veja a entrega no seu CEP", "Entre para calcular.")
        );

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.listarProdutos(" HIGIENE ", pageable, null, model);

        assertThat(view).isEqualTo("pages/cliente/produtos/lista");
        assertThat(model.getAttribute("cat")).isEqualTo("HIGIENE");
        assertThat(model.getAttribute("showCategorySections")).isEqualTo(false);
        verify(repo).searchPublicPageByCategoria(null, "HIGIENE", pageable);
        verify(repo).findDistinctCategoriasPublicas();
    }

    private static ProdutoEntity produto(Long id) {
        ProdutoEntity entity = new ProdutoEntity();
        entity.setId(id);
        entity.setNome("Produto " + id);
        entity.setPrecoVenda(BigDecimal.TEN);
        entity.setDisponivel(true);
        entity.setEstoque(10);
        return entity;
    }
}
