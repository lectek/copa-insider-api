package br.com.redemaisfarma.adapters.inbound.web.controller.site;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.application.config.AppProps;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.application.service.ProductCategorySectionService;
import br.com.redemaisfarma.application.service.delivery.PublicDeliveryEstimateService;
import br.com.redemaisfarma.application.view.DeliveryEstimateVM;
import br.com.redemaisfarma.application.view.ProductCardVM;
import br.com.redemaisfarma.application.view.ProductCategorySectionVM;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = HomePageController.class)
@AutoConfigureMockMvc(addFilters = false)
class HomePageCatalogViewWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProdutoRepository produtoRepository;

    @MockitoBean
    private PublicDeliveryEstimateService deliveryEstimateService;

    @MockitoBean
    private ProductCategorySectionService productCategorySectionService;

    @MockitoBean
    private AppSettingService appSettingService;

    @MockitoBean(name = "appProps")
    private AppProps appProps;

    @BeforeEach
    void setUp() {
        when(appSettingService.get(anyString())).thenReturn(Optional.empty());
        when(deliveryEstimateService.estimateFor(any())).thenReturn(
                DeliveryEstimateVM.unavailable(
                        "Veja a entrega no seu CEP",
                        "Entre na sua conta e salve o endereco para calcular a rota."
                )
        );
        when(appProps.getWhatsappLink()).thenReturn("https://wa.me/5583988853265");
        when(appProps.getWhatsappDisplay()).thenReturn("(83) 98885-3265");
        when(appProps.getInstagramUrl()).thenReturn("https://instagram.com/saudemaisfarmaa");
        when(appProps.getInstagramDisplay()).thenReturn("@saudemaisfarmaa");
        when(appProps.getPhoneLink()).thenReturn("tel:+5583988853265");
        when(appProps.getPhoneDisplay()).thenReturn("(83) 98885-3265");
        when(appProps.getAddressMapsUrl()).thenReturn("https://maps.google.com/?q=Joao+Pessoa");
        when(appProps.getAddressDisplay()).thenReturn("Joao Pessoa/PB");
        when(appProps.getFacebookUrl()).thenReturn("https://facebook.com/saudemaisfarma");
        when(appProps.getStoreDisplayName()).thenReturn("SaudeMais Farma");
    }

    @Test
    void produtosRenderizaCardsDaVitrineSemQuebrarTemplate() throws Exception {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(10L);
        produto.setNome("Dipirona 500mg");
        produto.setCategoria("Catalogo local");
        produto.setImagem("/img/produtos/dipirona.png");
        produto.setPrecoVenda(BigDecimal.valueOf(12.50));
        produto.setDisponivel(true);
        produto.setEstoque(8);
        produto.setDataCadastro(LocalDate.now());

        when(produtoRepository.searchPublicPage(any(), any())).thenReturn(
                new PageImpl<>(List.of(produto), PageRequest.of(0, 12), 1)
        );
        when(productCategorySectionService.loadPublicSections(6)).thenReturn(List.of(
                new ProductCategorySectionVM(
                        "catalogo-local",
                        "Catalogo local",
                        List.of("Catalogo local"),
                        6,
                        List.of(new ProductCardVM(
                                10L,
                                "Dipirona 500mg",
                                "/img/produtos/dipirona.png",
                                BigDecimal.valueOf(12.50),
                                null,
                                null,
                                true,
                                8,
                                "Catalogo local"
                        ))
                )
        ));
        when(produtoRepository.findDistinctCategoriasPublicas())
                .thenReturn(List.of("Catalogo local"));

        mockMvc.perform(get("/produtos"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/cliente/produtos/lista"))
                .andExpect(content().string(containsString("Grade por categoria")))
                .andExpect(content().string(containsString("Catalogo local")))
                .andExpect(content().string(containsString("Dipirona 500mg")))
                .andExpect(content().string(containsString("Abrir Alysson")))
                .andExpect(content().string(containsString("/produto/10")));
    }
}
