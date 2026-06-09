package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoCategoriaRepository;
import br.com.lectek.copainsider.application.config.AppProps;
import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import br.com.lectek.copainsider.application.service.CartService;
import br.com.lectek.copainsider.application.service.HomeCarouselConfigService;
import br.com.lectek.copainsider.application.service.HomeLayoutConfigService;
import br.com.lectek.copainsider.application.service.PaymentMethodService;
import br.com.lectek.copainsider.application.service.ProductCategorySectionService;
import br.com.lectek.copainsider.application.service.delivery.DeliveryPricingService;
import br.com.lectek.copainsider.application.view.CartItemVM;
import br.com.lectek.copainsider.application.view.CartSummaryVM;
import br.com.lectek.copainsider.application.view.DeliveryQuoteVM;
import br.com.lectek.copainsider.application.view.PaymentMethodVM;
import br.com.lectek.copainsider.domain.catalogo.HomepageCatalogFacade;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = HomeController.class)
@AutoConfigureMockMvc(addFilters = false)
class HomeControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HomepageCatalogFacade facade;

    @MockitoBean
    private PaymentMethodService paymentMethodService;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private ProdutoCategoriaRepository categoriaRepository;

    @MockitoBean
    private HomeCarouselConfigService homeCarouselConfigService;

    @MockitoBean
    private HomeLayoutConfigService homeLayoutConfigService;

    @MockitoBean
    private ProductCategorySectionService productCategorySectionService;

    @MockitoBean
    private DeliveryPricingService deliveryPricingService;

    @MockitoBean
    private AppSettingService appSettingService;

    @MockitoBean(name = "appProps")
    private AppProps appProps;

    @BeforeEach
    void setUp() {
        when(facade.buildHomepage()).thenThrow(new IllegalStateException("falha de vitrine"));
        when(cartService.buildSummary(any())).thenReturn(
                new CartSummaryVM(List.of(), BigDecimal.ZERO, BigDecimal.ZERO, false)
        );
        when(categoriaRepository.findAllNomes()).thenReturn(List.of());
        when(homeCarouselConfigService.resolveStyles()).thenReturn(Map.of());
        when(homeLayoutConfigService.load()).thenReturn(
                new HomeLayoutConfigService.HomeLayoutConfig(
                        true,
                        true,
                        true,
                        List.of("Entrega local"),
                        true,
                        8,
                        List.of()
                )
        );
        when(productCategorySectionService.loadPublicSections(any())).thenReturn(List.of());
        when(appSettingService.get(anyString())).thenReturn(Optional.empty());
        when(appSettingService.getOrDefault(anyString(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        when(appSettingService.getDecimal(anyString(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(deliveryPricingService.quoteForCheckout(any(), any())).thenReturn(
                DeliveryQuoteVM.unavailable(
                        new BigDecimal("5.00"),
                        new BigDecimal("2.00"),
                        new BigDecimal("20.00"),
                        "Informe o endereco para calcular o frete",
                        "Ate 5 km a entrega e gratis. Depois cobramos R$ 2,00 por km excedente."
                )
        );
        when(paymentMethodService.listActiveMethods()).thenReturn(
                List.of(new PaymentMethodVM("pix", "PIX", "online"))
        );
        when(appProps.getWhatsapp()).thenReturn("(83) 98885-3265");
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
    void homeRenderizaEstadoVazioQuandoVmFalha() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/cliente/index"))
                .andExpect(content().string(containsString("Catalogo em atualizacao")))
                .andExpect(content().string(containsString("Ainda nao ha produtos publicados para esta loja.")))
                .andExpect(content().string(containsString("Abrir Alysson")))
                .andExpect(content().string(containsString("site-footer")));
    }

    @Test
    void checkoutRenderizaOpcoesDeFrete() throws Exception {
        when(cartService.buildSummary(any())).thenReturn(cartSummaryWithItem());

        mockMvc.perform(get("/checkout").param("tenantId", "tenant-centro"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/cliente/checkout/checkout"))
                .andExpect(request().sessionAttribute("tenantContextId", "tenant-centro"))
                .andExpect(content().string(containsString("Frete prioritario")))
                .andExpect(content().string(containsString("Endereco para retirada")))
                .andExpect(content().string(containsString("SaudeMais Farma")))
                .andExpect(content().string(containsString("Joao Pessoa/PB")))
                .andExpect(content().string(containsString("Forma de Pagamento")))
                .andExpect(content().string(containsString("name=\"tenantId\"")))
                .andExpect(content().string(containsString("value=\"tenant-centro\"")));
    }

    @Test
    void checkoutMantemTenantDaSessaoQuandoRequestSeguinteNaoInformar() throws Exception {
        MockHttpSession session = new MockHttpSession();
        when(cartService.buildSummary(any())).thenReturn(cartSummaryWithItem());

        MvcResult firstResult = mockMvc.perform(
                        get("/checkout")
                                .param("tenantId", "tenant-centro")
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(request().sessionAttribute("tenantContextId", "tenant-centro"))
                .andReturn();

        mockMvc.perform(get("/checkout").session((MockHttpSession) firstResult.getRequest().getSession(false)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"tenantId\"")))
                .andExpect(content().string(containsString("value=\"tenant-centro\"")));
    }

    private CartSummaryVM cartSummaryWithItem() {
        return new CartSummaryVM(
                List.of(
                        new CartItemVM(
                                1L,
                                "Dipirona",
                                "/img/produtos/dipirona.png",
                                BigDecimal.TEN,
                                1,
                                BigDecimal.TEN,
                                false,
                                null,
                                10
                        )
                ),
                BigDecimal.TEN,
                BigDecimal.TEN,
                false
        );
    }
}
