package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.inbound.web.support.TenantScopedSettingsService;
import br.com.redemaisfarma.application.service.HomeCarouselConfigService;
import br.com.redemaisfarma.application.service.HomeLayoutConfigService;
import br.com.redemaisfarma.application.service.ProductCategorySectionService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminConfiguracoesClienteControllerTest {

    @Mock
    private TenantScopedSettingsService tenantScopedSettings;

    @Mock
    private HomeCarouselConfigService homeCarouselConfigService;

    @Mock
    private HomeLayoutConfigService homeLayoutConfigService;

    @Mock
    private ProductCategorySectionService productCategorySectionService;
    @Mock
    private HttpServletRequest request;

    private AdminConfiguracoesClienteController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminConfiguracoesClienteController(
                tenantScopedSettings,
                homeCarouselConfigService,
                homeLayoutConfigService,
                productCategorySectionService
        );
        lenient().when(tenantScopedSettings.resolveTenantContextId(request)).thenReturn("acme");
        lenient().when(homeLayoutConfigService.defaults()).thenReturn(
                new HomeLayoutConfigService.HomeLayoutConfig(
                        true,
                        true,
                        true,
                        List.of(
                                "Entrega local",
                                "Retirada na loja",
                                "Compra segura",
                                "Atendimento farmaceutico"
                        ),
                        true,
                        8,
                        List.of()
                )
        );
    }

    @Test
    void formLoadsLegacyHeroAndFrontendFallbacks() {
        Map<String, String> persisted = new HashMap<>();
        persisted.put("GERAL.home_hero_imagem_url", "/media/branding/hero.png");
        persisted.put("GERAL.home_hero_texto", "Ofertas locais");
        persisted.put("cliente.frontend.text_dark", "#f5f5f5");
        persisted.put("branding.font_family", "'Manrope', sans-serif");
        when(homeCarouselConfigService.load()).thenReturn(
                new HomeCarouselConfigService.HomeCarouselConfig(
                        "classic",
                        new LinkedHashMap<>(),
                        new LinkedHashMap<>()
                )
        );
        when(homeLayoutConfigService.load()).thenReturn(
                new HomeLayoutConfigService.HomeLayoutConfig(
                        true,
                        false,
                        true,
                        List.of(
                                "Entrega local",
                                "Retirada na loja",
                                "Compra segura",
                                "Atendimento farmaceutico"
                        ),
                        true,
                        6,
                        List.of(
                                new HomeLayoutConfigService.HomeSectionConfig(
                                        "destaque",
                                        true,
                                        "carousel",
                                        10,
                                        "Destaques da semana",
                                        "Ofertas e lancamentos selecionados",
                                        "Ver todos",
                                        "/produtos"
                                )
                        )
                )
        );
        when(homeLayoutConfigService.definitions()).thenReturn(
                List.of(
                        new HomeLayoutConfigService.SectionDefinition(
                                "destaque",
                                "Destaques da semana",
                                "Bloco principal com produtos priorizados na home.",
                                "carousel",
                                "Destaques da semana",
                                "Ofertas e lancamentos selecionados",
                                10
                        )
                )
        );
        when(homeCarouselConfigService.definitions()).thenReturn(List.of());
        when(homeCarouselConfigService.styleOptions()).thenReturn(List.of());
        when(productCategorySectionService.loadEditorConfigs()).thenReturn(
                List.of(
                        new ProductCategorySectionService.CategorySectionConfig(
                                "medicamentos",
                                "Medicamentos",
                                List.of("ANALGESICOS"),
                                8
                        )
                )
        );
        when(tenantScopedSettings.getAllByKeys(anyString(), anyCollection())).thenReturn(persisted);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.form(model, request);

        Assertions.assertThat(view).isEqualTo("pages/admin/configuracoes/cliente");
        AdminConfiguracoesClienteController.ClienteAreaForm form =
                (AdminConfiguracoesClienteController.ClienteAreaForm) model.get("clienteArea");
        Assertions.assertThat(form).isNotNull();
        Assertions.assertThat(form.getHomeHeroUrl()).isEqualTo("/media/branding/hero.png");
        Assertions.assertThat(form.getHomeHeroTexto()).isEqualTo("Ofertas locais");
        Assertions.assertThat(form.getHomeHeroMediaMode()).isEqualTo("contain");
        Assertions.assertThat(form.getHomeHeroDesktopRatio()).isEqualTo("wide");
        Assertions.assertThat(form.getHomeHeroMobileRatio()).isEqualTo("square");
        Assertions.assertThat(form.getFrontendFontFamily()).isEqualTo("'Manrope', sans-serif");
        Assertions.assertThat(form.getFrontendTextDark()).isEqualTo("#f5f5f5");
        Assertions.assertThat(form.getFrontendBgDark()).isEqualTo("#0b1220");
        Assertions.assertThat(form.getHomeCarouselDefaultStyle()).isEqualTo("classic");
        Assertions.assertThat(form.isHeroSearchEnabled()).isTrue();
        Assertions.assertThat(form.isHeroPrincipalEnabled()).isFalse();
        Assertions.assertThat(form.getQuickCategoriesLimit()).isEqualTo(6);
        Assertions.assertThat(form.isCategorySectionsAutoEnabled()).isFalse();
        Assertions.assertThat(form.getCategorySections())
                .extracting(AdminConfiguracoesClienteController.CategorySectionForm::getKey)
                .containsExactly("medicamentos");
    }

    @Test
    void salvarPersistsFrontendAndHeroSettings() {
        AdminConfiguracoesClienteController.ClienteAreaForm form =
                new AdminConfiguracoesClienteController.ClienteAreaForm();
        form.setHomeHeroUrl("/media/branding/hero.png");
        form.setHomeHeroTexto("Ofertas locais");
        form.setHomeHeroAlt("Banner promocional");
        form.setHomeHeroVideoUrl("https://cdn.exemplo.com/hero.mp4");
        form.setHomeHeroMediaMode("contain");
        form.setHomeHeroDesktopRatio("cinema");
        form.setHomeHeroMobileRatio("poster");
        form.setHomeHeroFocus("top");
        form.setHomeHeroSurface("#f8fafc");
        form.setFrontendFontFamily("'Sora', sans-serif");
        form.setFrontendTextLight("#111827");
        form.setFrontendTextDark("#f8fafc");
        form.setFrontendBgLight("#ffffff");
        form.setFrontendBgDark("#0f172a");
        form.setHeroSearchEnabled(true);
        form.setHeroPrincipalEnabled(true);
        form.setTrustStripEnabled(true);
        form.setQuickCategoriesEnabled(true);
        form.setQuickCategoriesLimit(8);
        form.setTrustItemPrimary("Entrega local");
        form.setTrustItemSecondary("Retirada na loja");
        form.setTrustItemTertiary("Compra segura");
        form.setTrustItemQuaternary("Atendimento farmaceutico");
        form.setHomeCarouselDefaultStyle("cards");
        form.setHomeCarouselStyles(Map.of("destaques", "cards"));
        form.setHomeSections(List.of(
                new AdminConfiguracoesClienteController.HomeSectionForm(
                        "destaque",
                        "Destaques da semana",
                        "Bloco principal com produtos priorizados na home.",
                        true,
                        "grid",
                        15,
                        "Destaques da semana",
                        "Ofertas e lancamentos selecionados",
                        "Ver todos",
                        "/produtos"
                )
        ));
        form.setCategorySectionsAutoEnabled(false);
        form.setCategorySections(List.of(
                new AdminConfiguracoesClienteController.CategorySectionForm(
                        "medicamentos",
                        "Medicamentos",
                        "ANALGESICOS, VITAMINAS",
                        8
                )
        ));
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

        String redirect = controller.salvar(form, null, null, attrs, request);

        Assertions.assertThat(redirect).isEqualTo("redirect:/admin/configuracoes/cliente");
        Assertions.assertThat(attrs.getFlashAttributes().get("success"))
                .isEqualTo("Area do cliente atualizada.");
        verify(tenantScopedSettings, times(33))
                .upsert(anyString(), anyString(), anyString(), anyString());
        verify(tenantScopedSettings).upsert(
                "acme",
                "branding.home_hero_desktop_ratio",
                "cinema",
                "Proporcao do hero no desktop"
        );
        verify(tenantScopedSettings).upsert(
                "acme",
                "cliente.frontend.text_dark",
                "#f8fafc",
                "Cor do texto no modo escuro"
        );
        verify(homeCarouselConfigService).save("cards", Map.of("destaques", "cards"));
        verify(homeLayoutConfigService).save(new HomeLayoutConfigService.HomeLayoutConfig(
                true,
                true,
                true,
                List.of(
                        "Entrega local",
                        "Retirada na loja",
                        "Compra segura",
                        "Atendimento farmaceutico"
                ),
                true,
                8,
                List.of(
                        new HomeLayoutConfigService.HomeSectionConfig(
                                "destaque",
                                true,
                                "grid",
                                15,
                                "Destaques da semana",
                                "Ofertas e lancamentos selecionados",
                                "Ver todos",
                                "/produtos"
                        )
                )
        ));
        verify(productCategorySectionService).saveEditorConfigs(List.of(
                new ProductCategorySectionService.CategorySectionConfig(
                        "medicamentos",
                        "Medicamentos",
                        List.of("ANALGESICOS", "VITAMINAS"),
                        8
                )
        ));
    }

    @Test
    void salvarAddsWarningWhenHeroUploadFails() throws IOException {
        AdminConfiguracoesClienteController.ClienteAreaForm form =
                new AdminConfiguracoesClienteController.ClienteAreaForm();
        MultipartFile hero = org.mockito.Mockito.mock(MultipartFile.class);
        when(hero.isEmpty()).thenReturn(false);
        when(hero.getOriginalFilename()).thenReturn("hero.png");
        when(hero.getInputStream()).thenThrow(new IOException("falha no disco"));
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

        String redirect = controller.salvar(form, hero, null, attrs, request);

        Assertions.assertThat(redirect).isEqualTo("redirect:/admin/configuracoes/cliente");
        Assertions.assertThat(attrs.getFlashAttributes()).containsKey("warning");
        verify(tenantScopedSettings, times(33))
                .upsert(anyString(), anyString(), anyString(), anyString());
        verify(homeCarouselConfigService).save(null, Map.of());
        verify(homeLayoutConfigService).save(new HomeLayoutConfigService.HomeLayoutConfig(
                false,
                false,
                false,
                java.util.Arrays.asList(null, null, null, null),
                false,
                8,
                List.of()
        ));
        verify(productCategorySectionService).saveEditorConfigs(List.of());
    }

    @Test
    void salvarMantemSecoesDeCategoriaEmModoAutomaticoQuandoSolicitado() {
        AdminConfiguracoesClienteController.ClienteAreaForm form =
                new AdminConfiguracoesClienteController.ClienteAreaForm();
        form.setCategorySectionsAutoEnabled(true);
        form.setCategorySections(List.of(
                new AdminConfiguracoesClienteController.CategorySectionForm(
                        "medicamentos",
                        "Medicamentos",
                        "ANALGESICOS",
                        8
                )
        ));
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

        String redirect = controller.salvar(form, null, null, attrs, request);

        Assertions.assertThat(redirect).isEqualTo("redirect:/admin/configuracoes/cliente");
        Assertions.assertThat(attrs.getFlashAttributes().get("success"))
                .isEqualTo("Area do cliente atualizada.");
        verify(tenantScopedSettings, times(33))
                .upsert(anyString(), anyString(), anyString(), anyString());
        verify(homeCarouselConfigService).save(null, Map.of());
        verify(homeLayoutConfigService).save(new HomeLayoutConfigService.HomeLayoutConfig(
                false,
                false,
                false,
                java.util.Arrays.asList(null, null, null, null),
                false,
                8,
                List.of()
        ));
        verify(productCategorySectionService).saveEditorConfigs(List.of());
    }
}
