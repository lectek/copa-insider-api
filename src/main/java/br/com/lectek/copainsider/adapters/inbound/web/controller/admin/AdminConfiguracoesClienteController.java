package br.com.lectek.copainsider.adapters.inbound.web.controller.admin;

import br.com.lectek.copainsider.adapters.inbound.web.support.TenantScopedSettingsService;
import br.com.lectek.copainsider.application.service.HomeCarouselConfigService;
import br.com.lectek.copainsider.application.service.HomeLayoutConfigService;
import br.com.lectek.copainsider.application.service.ProductCategorySectionService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/configuracoes/cliente")
@PreAuthorize("hasRole('ADMIN')")
public class AdminConfiguracoesClienteController {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AdminConfiguracoesClienteController.class);

    private static final String VIEW_PATH = "pages/admin/configuracoes/cliente";
    private static final String REDIRECT_URL =
            "redirect:/admin/configuracoes/cliente";

    private static final String KEY_HOME_HERO_URL = "branding.home_hero_url";
    private static final String KEY_HOME_HERO_TEXTO =
            "branding.home_hero_texto";
    private static final String KEY_HOME_HERO_ALT = "branding.home_hero_alt";
    private static final String KEY_HOME_HERO_VIDEO_URL =
            "branding.home_hero_video_url";
    private static final String KEY_HOME_HERO_MEDIA_MODE =
            "branding.home_hero_media_mode";
    private static final String KEY_HOME_HERO_DESKTOP_RATIO =
            "branding.home_hero_desktop_ratio";
    private static final String KEY_HOME_HERO_MOBILE_RATIO =
            "branding.home_hero_mobile_ratio";
    private static final String KEY_HOME_HERO_FOCUS =
            "branding.home_hero_focus";
    private static final String KEY_HOME_HERO_SURFACE =
            "branding.home_hero_surface";
    private static final String KEY_FRONTEND_FONT_FAMILY =
            "cliente.frontend.font_family";
    private static final String KEY_FRONTEND_TEXT_LIGHT =
            "cliente.frontend.text_light";
    private static final String KEY_FRONTEND_TEXT_DARK =
            "cliente.frontend.text_dark";
    private static final String KEY_FRONTEND_BG_LIGHT =
            "cliente.frontend.bg_light";
    private static final String KEY_FRONTEND_BG_DARK =
            "cliente.frontend.bg_dark";
    private static final String KEY_CART_MASCOT_URL =
            "cliente.cart.mascot_url";
    private static final String KEY_CART_MASCOT_ALT =
            "cliente.cart.mascot_alt";
    private static final String KEY_CART_MASCOT_CAPTION =
            "cliente.cart.mascot_caption";
    private static final String KEY_CART_EMPTY_TITLE =
            "cliente.cart.empty_title";
    private static final String KEY_CART_EMPTY_MESSAGE =
            "cliente.cart.empty_message";
    private static final String KEY_BRANDING_FONT_FAMILY =
            "branding.font_family";
    private static final String KEY_DESIGN_FONT_FAMILY =
            "design.font_family";
    private static final String KEY_LOGIN_ANIMATION_URL =
            "login.animation_url";
    private static final String KEY_LOGIN_CLIENT_BG_START =
            "login.client.bg_start";
    private static final String KEY_LOGIN_CLIENT_BG_MID =
            "login.client.bg_mid";
    private static final String KEY_LOGIN_CLIENT_BG_END =
            "login.client.bg_end";
    private static final String KEY_LOGIN_ADMIN_BG_START =
            "login.admin.bg_start";
    private static final String KEY_LOGIN_ADMIN_BG_MID =
            "login.admin.bg_mid";
    private static final String KEY_LOGIN_ADMIN_BG_END =
            "login.admin.bg_end";
    private static final String KEY_AI_ASSISTANT_NAME =
            "ai.assistant.name";
    private static final String KEY_UI_NAV_HOME_LABEL =
            "ui.nav.home_label";
    private static final String KEY_UI_NAV_ASSISTANT_LABEL =
            "ui.nav.assistant_label";
    private static final String KEY_UI_NAV_ACCOUNT_LABEL =
            "ui.nav.account_label";
    private static final String KEY_UI_NAV_CATALOG_LABEL =
            "ui.nav.catalog_label";
    private static final String KEY_UI_FOOTER_ABOUT_TITLE =
            "ui.footer.about_title";
    private static final String KEY_STORE_DISPLAY_NAME =
            "loja.nome_exibicao";

    private static final String LEGACY_HOME_HERO_URL =
            "GERAL.home_hero_imagem_url";
    private static final String LEGACY_HOME_HERO_TEXTO =
            "GERAL.home_hero_texto";

    private static final String DEFAULT_MEDIA_MODE = "contain";
    private static final String DEFAULT_DESKTOP_RATIO = "wide";
    private static final String DEFAULT_MOBILE_RATIO = "square";
    private static final String DEFAULT_FOCUS = "center";
    private static final String DEFAULT_SURFACE = "#ffffff";
    private static final String DEFAULT_FRONTEND_FONT =
            "'Source Sans 3', system-ui, Segoe UI, Roboto, Arial, sans-serif";
    private static final String DEFAULT_TEXT_LIGHT = "#0f172a";
    private static final String DEFAULT_TEXT_DARK = "#f8fafc";
    private static final String DEFAULT_BG_LIGHT = "#f8fafc";
    private static final String DEFAULT_BG_DARK = "#0b1220";
    private static final String DEFAULT_CART_MASCOT_URL =
            "/images/mascote-carrinho.png";
    private static final String DEFAULT_CART_MASCOT_ALT =
            "Mascote CopaInsider";
    private static final String DEFAULT_CART_MASCOT_CAPTION =
            "Seu pedido com carinho e seguranca.";
    private static final String DEFAULT_CART_EMPTY_TITLE =
            "Seu carrinho esta vazio";
    private static final String DEFAULT_CART_EMPTY_MESSAGE =
            "Volte ao catalogo para adicionar produtos.";
    private static final String DEFAULT_LOGIN_ANIMATION_URL =
            "/animations/mascote-flow-loop.mp4";
    private static final String DEFAULT_LOGIN_CLIENT_BG_START = "#04020f";
    private static final String DEFAULT_LOGIN_CLIENT_BG_MID = "#071536";
    private static final String DEFAULT_LOGIN_CLIENT_BG_END = "#0b2d64";
    private static final String DEFAULT_LOGIN_ADMIN_BG_START = "#1f0828";
    private static final String DEFAULT_LOGIN_ADMIN_BG_MID = "#2b1452";
    private static final String DEFAULT_LOGIN_ADMIN_BG_END = "#163a6b";
    private static final String DEFAULT_AI_ASSISTANT_NAME = "Alysson";
    private static final String DEFAULT_UI_NAV_HOME_LABEL = "Inicio";
    private static final String DEFAULT_UI_NAV_ASSISTANT_LABEL = "Alysson";
    private static final String DEFAULT_UI_NAV_ACCOUNT_LABEL = "Minha conta";
    private static final String DEFAULT_UI_NAV_CATALOG_LABEL = "Catalogo";
    private static final String DEFAULT_UI_FOOTER_ABOUT_TITLE = "Sobre a loja";
    private static final String DEFAULT_STORE_DISPLAY_NAME =
            "Embalando Solucoes em Enbalagens e Bomboniere";

    private static final Set<String> ALL_KEYS = Set.of(
            KEY_HOME_HERO_URL,
            KEY_HOME_HERO_TEXTO,
            KEY_HOME_HERO_ALT,
            KEY_HOME_HERO_VIDEO_URL,
            KEY_HOME_HERO_MEDIA_MODE,
            KEY_HOME_HERO_DESKTOP_RATIO,
            KEY_HOME_HERO_MOBILE_RATIO,
            KEY_HOME_HERO_FOCUS,
            KEY_HOME_HERO_SURFACE,
            KEY_FRONTEND_FONT_FAMILY,
            KEY_FRONTEND_TEXT_LIGHT,
            KEY_FRONTEND_TEXT_DARK,
            KEY_FRONTEND_BG_LIGHT,
            KEY_FRONTEND_BG_DARK,
            KEY_CART_MASCOT_URL,
            KEY_CART_MASCOT_ALT,
            KEY_CART_MASCOT_CAPTION,
            KEY_CART_EMPTY_TITLE,
            KEY_CART_EMPTY_MESSAGE,
            KEY_BRANDING_FONT_FAMILY,
            KEY_DESIGN_FONT_FAMILY,
            KEY_LOGIN_ANIMATION_URL,
            KEY_LOGIN_CLIENT_BG_START,
            KEY_LOGIN_CLIENT_BG_MID,
            KEY_LOGIN_CLIENT_BG_END,
            KEY_LOGIN_ADMIN_BG_START,
            KEY_LOGIN_ADMIN_BG_MID,
            KEY_LOGIN_ADMIN_BG_END,
            KEY_AI_ASSISTANT_NAME,
            KEY_UI_NAV_HOME_LABEL,
            KEY_UI_NAV_ASSISTANT_LABEL,
            KEY_UI_NAV_ACCOUNT_LABEL,
            KEY_UI_NAV_CATALOG_LABEL,
            KEY_UI_FOOTER_ABOUT_TITLE,
            KEY_STORE_DISPLAY_NAME,
            LEGACY_HOME_HERO_URL,
            LEGACY_HOME_HERO_TEXTO
    );

    private static final Set<String> ALLOWED_MEDIA_MODES = Set.of(
            "contain",
            "cover"
    );

    private static final Set<String> ALLOWED_DESKTOP_RATIOS = Set.of(
            "wide",
            "cinema",
            "poster"
    );

    private static final Set<String> ALLOWED_MOBILE_RATIOS = Set.of(
            "square",
            "poster",
            "tall"
    );

    private static final Set<String> ALLOWED_FOCUS = Set.of(
            "center",
            "top",
            "left",
            "right"
    );

    private final TenantScopedSettingsService tenantScopedSettings;
    private final HomeCarouselConfigService homeCarouselConfigService;
    private final HomeLayoutConfigService homeLayoutConfigService;
    private final ProductCategorySectionService productCategorySectionService;

    public AdminConfiguracoesClienteController(
            final TenantScopedSettingsService tenantScopedSettingsService,
            final HomeCarouselConfigService carouselConfigService,
            final HomeLayoutConfigService homeLayoutConfigService,
            final ProductCategorySectionService productCategorySectionService
    ) {
        this.tenantScopedSettings = tenantScopedSettingsService;
        this.homeCarouselConfigService = carouselConfigService;
        this.homeLayoutConfigService = homeLayoutConfigService;
        this.productCategorySectionService = productCategorySectionService;
    }

    @GetMapping
    public String form(final Model model, final HttpServletRequest request) {
        final String tenantContextId =
                tenantScopedSettings.resolveTenantContextId(request);
        if (!model.containsAttribute("clienteArea")) {
            model.addAttribute("clienteArea", loadForm(tenantContextId));
        }
        model.addAttribute("tenantContextId", tenantContextId);
        model.addAttribute(
                "tenantScoped",
                tenantContextId != null && !tenantContextId.isBlank()
        );
        model.addAttribute(
                "publicCategoryOptions",
                productCategorySectionService.loadAvailableCategories()
        );
        model.addAttribute(
                "homeCarouselDefinitions",
                homeCarouselConfigService.definitions()
        );
        model.addAttribute(
                "homeCarouselStyleOptions",
                homeCarouselConfigService.styleOptions()
        );
        return VIEW_PATH;
    }

    @PostMapping
    public String salvar(
            @ModelAttribute("clienteArea") final ClienteAreaForm form,
            @RequestParam(name = "homeHeroImagem", required = false)
            final MultipartFile homeHeroImagem,
            @RequestParam(name = "cartMascoteImagem", required = false)
            final MultipartFile cartMascoteImagem,
            final RedirectAttributes ra,
            final HttpServletRequest request
    ) {
        final String tenantContextId =
                tenantScopedSettings.resolveTenantContextId(request);
        final ArrayList<String> warnings = new ArrayList<>();
        if (homeHeroImagem != null && !homeHeroImagem.isEmpty()) {
            try {
                form.setHomeHeroUrl(storeUpload(homeHeroImagem, "home-hero"));
            } catch (IOException ex) {
                warnings.add(
                        "Falha ao salvar a nova imagem do hero. As demais configuracoes foram atualizadas."
                );
                LOGGER.warn("Falha ao salvar imagem do hero do cliente.", ex);
            }
        }
        if (cartMascoteImagem != null && !cartMascoteImagem.isEmpty()) {
            try {
                form.setCartMascotUrl(storeUpload(cartMascoteImagem, "cart-mascot"));
            } catch (IOException ex) {
                warnings.add(
                        "Falha ao salvar a nova imagem do carrinho. As demais configuracoes foram atualizadas."
                );
                LOGGER.warn("Falha ao salvar mascote do carrinho.", ex);
            }
        }

        saveSetting(tenantContextId, KEY_HOME_HERO_URL, form.getHomeHeroUrl(),
                "Imagem principal da home");
        saveSetting(tenantContextId, KEY_HOME_HERO_TEXTO, form.getHomeHeroTexto(),
                "Titulo principal da home");
        saveSetting(tenantContextId, KEY_HOME_HERO_ALT, form.getHomeHeroAlt(),
                "Texto alternativo do hero");
        saveSetting(tenantContextId, KEY_HOME_HERO_VIDEO_URL, form.getHomeHeroVideoUrl(),
                "Video principal da home");
        saveSetting(
                tenantContextId,
                KEY_HOME_HERO_MEDIA_MODE,
                sanitizeChoice(
                        form.getHomeHeroMediaMode(),
                        ALLOWED_MEDIA_MODES,
                        DEFAULT_MEDIA_MODE
                ),
                "Modo de exibicao da arte principal"
        );
        saveSetting(
                tenantContextId,
                KEY_HOME_HERO_DESKTOP_RATIO,
                sanitizeChoice(
                        form.getHomeHeroDesktopRatio(),
                        ALLOWED_DESKTOP_RATIOS,
                        DEFAULT_DESKTOP_RATIO
                ),
                "Proporcao do hero no desktop"
        );
        saveSetting(
                tenantContextId,
                KEY_HOME_HERO_MOBILE_RATIO,
                sanitizeChoice(
                        form.getHomeHeroMobileRatio(),
                        ALLOWED_MOBILE_RATIOS,
                        DEFAULT_MOBILE_RATIO
                ),
                "Proporcao do hero no mobile"
        );
        saveSetting(
                tenantContextId,
                KEY_HOME_HERO_FOCUS,
                sanitizeChoice(
                        form.getHomeHeroFocus(),
                        ALLOWED_FOCUS,
                        DEFAULT_FOCUS
                ),
                "Posicao focal da arte do hero"
        );
        saveSetting(
                tenantContextId,
                KEY_HOME_HERO_SURFACE,
                sanitizeColor(form.getHomeHeroSurface(), DEFAULT_SURFACE),
                "Cor de apoio do hero"
        );
        saveSetting(
                tenantContextId,
                KEY_FRONTEND_FONT_FAMILY,
                sanitizeFontFamily(
                        form.getFrontendFontFamily(),
                        DEFAULT_FRONTEND_FONT
                ),
                "Fonte base do frontend cliente"
        );
        saveSetting(
                tenantContextId,
                KEY_FRONTEND_TEXT_LIGHT,
                sanitizeColor(form.getFrontendTextLight(), DEFAULT_TEXT_LIGHT),
                "Cor do texto no modo claro"
        );
        saveSetting(
                tenantContextId,
                KEY_FRONTEND_TEXT_DARK,
                sanitizeColor(form.getFrontendTextDark(), DEFAULT_TEXT_DARK),
                "Cor do texto no modo escuro"
        );
        saveSetting(
                tenantContextId,
                KEY_FRONTEND_BG_LIGHT,
                sanitizeColor(form.getFrontendBgLight(), DEFAULT_BG_LIGHT),
                "Cor de fundo no modo claro"
        );
        saveSetting(
                tenantContextId,
                KEY_FRONTEND_BG_DARK,
                sanitizeColor(form.getFrontendBgDark(), DEFAULT_BG_DARK),
                "Cor de fundo no modo escuro"
        );
        saveSetting(
                tenantContextId,
                KEY_CART_MASCOT_URL,
                firstNonBlank(form.getCartMascotUrl(), DEFAULT_CART_MASCOT_URL),
                "Imagem do mascote do carrinho"
        );
        saveSetting(
                tenantContextId,
                KEY_CART_MASCOT_ALT,
                firstNonBlank(form.getCartMascotAlt(), DEFAULT_CART_MASCOT_ALT),
                "Texto alternativo do mascote do carrinho"
        );
        saveSetting(
                tenantContextId,
                KEY_CART_MASCOT_CAPTION,
                firstNonBlank(form.getCartMascotCaption(), DEFAULT_CART_MASCOT_CAPTION),
                "Legenda do mascote do carrinho"
        );
        saveSetting(
                tenantContextId,
                KEY_CART_EMPTY_TITLE,
                firstNonBlank(form.getCartEmptyTitle(), DEFAULT_CART_EMPTY_TITLE),
                "Titulo do estado vazio do carrinho"
        );
        saveSetting(
                tenantContextId,
                KEY_CART_EMPTY_MESSAGE,
                firstNonBlank(form.getCartEmptyMessage(), DEFAULT_CART_EMPTY_MESSAGE),
                "Mensagem do estado vazio do carrinho"
        );
        saveSetting(
                tenantContextId,
                KEY_LOGIN_ANIMATION_URL,
                sanitizeVideoUrl(
                        form.getLoginAnimationUrl(),
                        DEFAULT_LOGIN_ANIMATION_URL
                ),
                "Animacao do login"
        );
        saveSetting(
                tenantContextId,
                KEY_LOGIN_CLIENT_BG_START,
                sanitizeColor(
                        form.getLoginClientBgStart(),
                        DEFAULT_LOGIN_CLIENT_BG_START
                ),
                "Cor inicial do fundo do login cliente"
        );
        saveSetting(
                tenantContextId,
                KEY_LOGIN_CLIENT_BG_MID,
                sanitizeColor(
                        form.getLoginClientBgMid(),
                        DEFAULT_LOGIN_CLIENT_BG_MID
                ),
                "Cor intermediaria do fundo do login cliente"
        );
        saveSetting(
                tenantContextId,
                KEY_LOGIN_CLIENT_BG_END,
                sanitizeColor(
                        form.getLoginClientBgEnd(),
                        DEFAULT_LOGIN_CLIENT_BG_END
                ),
                "Cor final do fundo do login cliente"
        );
        saveSetting(
                tenantContextId,
                KEY_LOGIN_ADMIN_BG_START,
                sanitizeColor(
                        form.getLoginAdminBgStart(),
                        DEFAULT_LOGIN_ADMIN_BG_START
                ),
                "Cor inicial do fundo do login admin"
        );
        saveSetting(
                tenantContextId,
                KEY_LOGIN_ADMIN_BG_MID,
                sanitizeColor(
                        form.getLoginAdminBgMid(),
                        DEFAULT_LOGIN_ADMIN_BG_MID
                ),
                "Cor intermediaria do fundo do login admin"
        );
        saveSetting(
                tenantContextId,
                KEY_LOGIN_ADMIN_BG_END,
                sanitizeColor(
                        form.getLoginAdminBgEnd(),
                        DEFAULT_LOGIN_ADMIN_BG_END
                ),
                "Cor final do fundo do login admin"
        );
        saveSetting(
                tenantContextId,
                KEY_AI_ASSISTANT_NAME,
                firstNonBlank(form.getAiAssistantName(), DEFAULT_AI_ASSISTANT_NAME),
                "Nome do assistente virtual"
        );
        saveSetting(
                tenantContextId,
                KEY_UI_NAV_HOME_LABEL,
                firstNonBlank(form.getUiNavHomeLabel(), DEFAULT_UI_NAV_HOME_LABEL),
                "Rotulo Inicio do menu principal"
        );
        saveSetting(
                tenantContextId,
                KEY_UI_NAV_ASSISTANT_LABEL,
                firstNonBlank(form.getUiNavAssistantLabel(), DEFAULT_UI_NAV_ASSISTANT_LABEL),
                "Rotulo do assistente no menu principal"
        );
        saveSetting(
                tenantContextId,
                KEY_UI_NAV_ACCOUNT_LABEL,
                firstNonBlank(form.getUiNavAccountLabel(), DEFAULT_UI_NAV_ACCOUNT_LABEL),
                "Rotulo Minha conta do menu principal"
        );
        saveSetting(
                tenantContextId,
                KEY_UI_NAV_CATALOG_LABEL,
                firstNonBlank(form.getUiNavCatalogLabel(), DEFAULT_UI_NAV_CATALOG_LABEL),
                "Rotulo Catalogo no menu e rodape"
        );
        saveSetting(
                tenantContextId,
                KEY_UI_FOOTER_ABOUT_TITLE,
                firstNonBlank(form.getUiFooterAboutTitle(), DEFAULT_UI_FOOTER_ABOUT_TITLE),
                "Titulo da coluna Sobre no rodape"
        );
        saveSetting(
                tenantContextId,
                KEY_STORE_DISPLAY_NAME,
                firstNonBlank(form.getStoreDisplayName(), DEFAULT_STORE_DISPLAY_NAME),
                "Nome de exibicao da loja"
        );
        homeCarouselConfigService.save(
                form.getHomeCarouselDefaultStyle(),
                form.getHomeCarouselStyles()
        );
        homeLayoutConfigService.save(buildHomeLayoutConfig(form));
        productCategorySectionService.saveEditorConfigs(
                buildCategorySectionConfigs(form)
        );

        if (!warnings.isEmpty()) {
            ra.addFlashAttribute(
                    "warning",
                    String.join(" ", warnings)
            );
        } else {
            ra.addFlashAttribute(
                    "success",
                    "Area do cliente atualizada."
            );
        }
        return REDIRECT_URL;
    }

    private void saveSetting(
            final String tenantContextId,
            final String key,
            final String value,
            final String description
    ) {
        tenantScopedSettings.upsert(
                tenantContextId,
                key,
                nullSafe(value),
                description
        );
    }

    private ClienteAreaForm loadForm(final String tenantContextId) {
        final Map<String, String> cfg =
                tenantScopedSettings.getAllByKeys(tenantContextId, ALL_KEYS);
        final HomeCarouselConfigService.HomeCarouselConfig carouselConfig =
                homeCarouselConfigService.load();
        final ClienteAreaForm form = new ClienteAreaForm();
        form.setHomeHeroUrl(firstValue(cfg, KEY_HOME_HERO_URL, LEGACY_HOME_HERO_URL));
        form.setHomeHeroTexto(
                firstValue(cfg, KEY_HOME_HERO_TEXTO, LEGACY_HOME_HERO_TEXTO)
        );
        form.setHomeHeroAlt(cfg.getOrDefault(KEY_HOME_HERO_ALT, ""));
        form.setHomeHeroVideoUrl(cfg.getOrDefault(KEY_HOME_HERO_VIDEO_URL, ""));
        form.setHomeHeroMediaMode(sanitizeChoice(
                cfg.get(KEY_HOME_HERO_MEDIA_MODE),
                ALLOWED_MEDIA_MODES,
                DEFAULT_MEDIA_MODE
        ));
        form.setHomeHeroDesktopRatio(sanitizeChoice(
                cfg.get(KEY_HOME_HERO_DESKTOP_RATIO),
                ALLOWED_DESKTOP_RATIOS,
                DEFAULT_DESKTOP_RATIO
        ));
        form.setHomeHeroMobileRatio(sanitizeChoice(
                cfg.get(KEY_HOME_HERO_MOBILE_RATIO),
                ALLOWED_MOBILE_RATIOS,
                DEFAULT_MOBILE_RATIO
        ));
        form.setHomeHeroFocus(sanitizeChoice(
                cfg.get(KEY_HOME_HERO_FOCUS),
                ALLOWED_FOCUS,
                DEFAULT_FOCUS
        ));
        form.setHomeHeroSurface(sanitizeColor(
                cfg.get(KEY_HOME_HERO_SURFACE),
                DEFAULT_SURFACE
        ));
        form.setFrontendFontFamily(resolveFontFamily(cfg));
        form.setFrontendTextLight(sanitizeColor(
                cfg.get(KEY_FRONTEND_TEXT_LIGHT),
                DEFAULT_TEXT_LIGHT
        ));
        form.setFrontendTextDark(sanitizeColor(
                cfg.get(KEY_FRONTEND_TEXT_DARK),
                DEFAULT_TEXT_DARK
        ));
        form.setFrontendBgLight(sanitizeColor(
                cfg.get(KEY_FRONTEND_BG_LIGHT),
                DEFAULT_BG_LIGHT
        ));
        form.setFrontendBgDark(sanitizeColor(
                cfg.get(KEY_FRONTEND_BG_DARK),
                DEFAULT_BG_DARK
        ));
        form.setCartMascotUrl(firstNonBlank(
                cfg.get(KEY_CART_MASCOT_URL),
                DEFAULT_CART_MASCOT_URL
        ));
        form.setCartMascotAlt(firstNonBlank(
                cfg.get(KEY_CART_MASCOT_ALT),
                DEFAULT_CART_MASCOT_ALT
        ));
        form.setCartMascotCaption(firstNonBlank(
                cfg.get(KEY_CART_MASCOT_CAPTION),
                DEFAULT_CART_MASCOT_CAPTION
        ));
        form.setCartEmptyTitle(firstNonBlank(
                cfg.get(KEY_CART_EMPTY_TITLE),
                DEFAULT_CART_EMPTY_TITLE
        ));
        form.setCartEmptyMessage(firstNonBlank(
                cfg.get(KEY_CART_EMPTY_MESSAGE),
                DEFAULT_CART_EMPTY_MESSAGE
        ));
        form.setLoginAnimationUrl(firstNonBlank(
                cfg.get(KEY_LOGIN_ANIMATION_URL),
                DEFAULT_LOGIN_ANIMATION_URL
        ));
        form.setLoginClientBgStart(sanitizeColor(
                cfg.get(KEY_LOGIN_CLIENT_BG_START),
                DEFAULT_LOGIN_CLIENT_BG_START
        ));
        form.setLoginClientBgMid(sanitizeColor(
                cfg.get(KEY_LOGIN_CLIENT_BG_MID),
                DEFAULT_LOGIN_CLIENT_BG_MID
        ));
        form.setLoginClientBgEnd(sanitizeColor(
                cfg.get(KEY_LOGIN_CLIENT_BG_END),
                DEFAULT_LOGIN_CLIENT_BG_END
        ));
        form.setLoginAdminBgStart(sanitizeColor(
                cfg.get(KEY_LOGIN_ADMIN_BG_START),
                DEFAULT_LOGIN_ADMIN_BG_START
        ));
        form.setLoginAdminBgMid(sanitizeColor(
                cfg.get(KEY_LOGIN_ADMIN_BG_MID),
                DEFAULT_LOGIN_ADMIN_BG_MID
        ));
        form.setLoginAdminBgEnd(sanitizeColor(
                cfg.get(KEY_LOGIN_ADMIN_BG_END),
                DEFAULT_LOGIN_ADMIN_BG_END
        ));
        form.setAiAssistantName(firstNonBlank(
                cfg.get(KEY_AI_ASSISTANT_NAME),
                DEFAULT_AI_ASSISTANT_NAME
        ));
        form.setUiNavHomeLabel(firstNonBlank(
                cfg.get(KEY_UI_NAV_HOME_LABEL),
                DEFAULT_UI_NAV_HOME_LABEL
        ));
        form.setUiNavAssistantLabel(firstNonBlank(
                cfg.get(KEY_UI_NAV_ASSISTANT_LABEL),
                DEFAULT_UI_NAV_ASSISTANT_LABEL
        ));
        form.setUiNavAccountLabel(firstNonBlank(
                cfg.get(KEY_UI_NAV_ACCOUNT_LABEL),
                DEFAULT_UI_NAV_ACCOUNT_LABEL
        ));
        form.setUiNavCatalogLabel(firstNonBlank(
                cfg.get(KEY_UI_NAV_CATALOG_LABEL),
                DEFAULT_UI_NAV_CATALOG_LABEL
        ));
        form.setUiFooterAboutTitle(firstNonBlank(
                cfg.get(KEY_UI_FOOTER_ABOUT_TITLE),
                DEFAULT_UI_FOOTER_ABOUT_TITLE
        ));
        form.setStoreDisplayName(firstNonBlank(
                cfg.get(KEY_STORE_DISPLAY_NAME),
                DEFAULT_STORE_DISPLAY_NAME
        ));
        form.setHomeCarouselDefaultStyle(carouselConfig.defaultStyle());
        form.setHomeCarouselStyles(
                new LinkedHashMap<>(carouselConfig.selectedStyles())
        );
        applyHomeLayoutConfig(form, homeLayoutConfigService.load());
        applyCategorySectionConfig(
                form,
                productCategorySectionService.loadEditorConfigs()
        );
        return form;
    }

    private void applyHomeLayoutConfig(
            final ClienteAreaForm form,
            final HomeLayoutConfigService.HomeLayoutConfig config
    ) {
        form.setHeroSearchEnabled(config.heroSearchEnabled());
        form.setHeroPrincipalEnabled(config.heroPrincipalEnabled());
        form.setTrustStripEnabled(config.trustStripEnabled());
        form.setQuickCategoriesEnabled(config.quickCategoriesEnabled());
        form.setQuickCategoriesLimit(config.quickCategoriesLimit());

        final List<String> trustItems = config.trustItems();
        form.setTrustItemPrimary(valueAt(trustItems, 0));
        form.setTrustItemSecondary(valueAt(trustItems, 1));
        form.setTrustItemTertiary(valueAt(trustItems, 2));
        form.setTrustItemQuaternary(valueAt(trustItems, 3));

        final ArrayList<HomeSectionForm> sections = new ArrayList<>();
        for (HomeLayoutConfigService.SectionDefinition definition
                : homeLayoutConfigService.definitions()) {
            final HomeLayoutConfigService.HomeSectionConfig configSection =
                    config.sections().stream()
                            .filter(section -> definition.key().equals(section.key()))
                            .findFirst()
                            .orElse(null);
            sections.add(new HomeSectionForm(
                    definition.key(),
                    definition.label(),
                    definition.description(),
                    configSection != null && configSection.enabled(),
                    configSection != null
                            ? configSection.layout()
                            : definition.defaultLayout(),
                    configSection != null ? configSection.order() : definition.defaultOrder(),
                    configSection != null ? configSection.title() : definition.defaultTitle(),
                    configSection != null
                            ? configSection.subtitle()
                            : definition.defaultSubtitle(),
                    configSection != null ? configSection.ctaLabel() : "Ver todos",
                    configSection != null ? configSection.ctaHref() : "/produtos"
            ));
        }
        form.setHomeSections(sections);
    }

    private void applyCategorySectionConfig(
            final ClienteAreaForm form,
            final List<ProductCategorySectionService.CategorySectionConfig> configs
    ) {
        final List<ProductCategorySectionService.CategorySectionConfig> safeConfigs =
                configs == null ? List.of() : List.copyOf(configs);
        form.setCategorySectionsAutoEnabled(safeConfigs.isEmpty());
        form.setCategorySections(
                safeConfigs.stream()
                        .map(config -> new CategorySectionForm(
                                config.chave(),
                                config.titulo(),
                                String.join(", ", config.categorias()),
                                config.limite()
                        ))
                        .toList()
        );
    }

    private HomeLayoutConfigService.HomeLayoutConfig buildHomeLayoutConfig(
            final ClienteAreaForm form
    ) {
        final ArrayList<String> trustItems = new ArrayList<>();
        trustItems.add(form.getTrustItemPrimary());
        trustItems.add(form.getTrustItemSecondary());
        trustItems.add(form.getTrustItemTertiary());
        trustItems.add(form.getTrustItemQuaternary());

        final List<HomeLayoutConfigService.HomeSectionConfig> sections =
                form.getHomeSections() == null
                        ? List.of()
                        : form.getHomeSections().stream()
                                .map(section -> new HomeLayoutConfigService.HomeSectionConfig(
                                        section.getKey(),
                                        section.isEnabled(),
                                        section.getLayout(),
                                        section.getOrder(),
                                        section.getTitle(),
                                        section.getSubtitle(),
                                        section.getCtaLabel(),
                                        section.getCtaHref()
                                ))
                                .sorted(Comparator.comparingInt(
                                        section -> section.order() == null
                                                ? Integer.MAX_VALUE
                                                : section.order()
                                ))
                                .toList();

        return new HomeLayoutConfigService.HomeLayoutConfig(
                form.isHeroSearchEnabled(),
                form.isHeroPrincipalEnabled(),
                form.isTrustStripEnabled(),
                trustItems,
                form.isQuickCategoriesEnabled(),
                resolveQuickCategoriesLimit(form.getQuickCategoriesLimit()),
                sections
        );
    }

    private List<ProductCategorySectionService.CategorySectionConfig> buildCategorySectionConfigs(
            final ClienteAreaForm form
    ) {
        if (form.isCategorySectionsAutoEnabled()
                || form.getCategorySections() == null
                || form.getCategorySections().isEmpty()) {
            return List.of();
        }

        return form.getCategorySections().stream()
                .map(section -> new ProductCategorySectionService.CategorySectionConfig(
                        nullSafe(section.getKey()),
                        nullSafe(section.getTitle()),
                        splitCategories(section.getCategoriesText()),
                        section.getLimit()
                ))
                .toList();
    }

    private List<String> splitCategories(final String rawCategories) {
        if (rawCategories == null || rawCategories.isBlank()) {
            return List.of();
        }

        return Arrays.stream(rawCategories.split("[,\\n;]+"))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    private static String valueAt(final List<String> values, final int index) {
        if (values == null || index < 0 || index >= values.size()) {
            return "";
        }
        return values.get(index);
    }

    private int resolveQuickCategoriesLimit(final Integer requestedLimit) {
        return requestedLimit == null
                ? homeLayoutConfigService.defaults().quickCategoriesLimit()
                : requestedLimit;
    }

    private static String resolveFontFamily(final Map<String, String> cfg) {
        final String value = cfg.get(KEY_FRONTEND_FONT_FAMILY);
        if (value != null && !value.isBlank()) {
            return value;
        }
        final String branding = cfg.get(KEY_BRANDING_FONT_FAMILY);
        if (branding != null && !branding.isBlank()) {
            return branding;
        }
        final String design = cfg.get(KEY_DESIGN_FONT_FAMILY);
        if (design != null && !design.isBlank()) {
            return design;
        }
        return DEFAULT_FRONTEND_FONT;
    }

    private static String firstValue(
            final Map<String, String> cfg,
            final String primary,
            final String fallback
    ) {
        final String value = cfg.get(primary);
        if (value != null && !value.isBlank()) {
            return value;
        }
        return cfg.getOrDefault(fallback, "");
    }

    private static String firstNonBlank(
            final String value,
            final String defaultValue
    ) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }

    private static String sanitizeChoice(
            final String value,
            final Set<String> allowed,
            final String defaultValue
    ) {
        if (value == null) {
            return defaultValue;
        }
        final String normalized = value.trim().toLowerCase();
        return allowed.contains(normalized) ? normalized : defaultValue;
    }

    private static String sanitizeColor(
            final String value,
            final String defaultValue
    ) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        final String normalized = value.trim();
        if (normalized.matches("(?i)^#[0-9a-f]{6}$")) {
            return normalized;
        }
        return defaultValue;
    }

    private static String sanitizeFontFamily(
            final String value,
            final String defaultValue
    ) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        final String normalized = value.trim();
        if (normalized.length() > 140) {
            return defaultValue;
        }
        return normalized;
    }

    private static String sanitizeVideoUrl(
            final String value,
            final String defaultValue
    ) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        final String normalized = value.trim();
        if (normalized.matches("(?i).*(\\.mp4|\\.mov|\\.webm|\\.ogg|\\.m4v)(\\?.*)?$")) {
            return normalized;
        }
        return defaultValue;
    }

    private static String nullSafe(final String value) {
        return value == null ? "" : value;
    }

    private static String storeUpload(
            final MultipartFile file,
            final String prefix
    ) throws IOException {
        final String original = StringUtils.cleanPath(
                Objects.requireNonNullElse(file.getOriginalFilename(), "upload")
        );
        String ext = "";
        final int dot = original.lastIndexOf('.');
        if (dot > -1 && dot < original.length() - 1) {
            ext = original.substring(dot);
        }

        final String filename = prefix + "-" + System.currentTimeMillis() + ext;
        final Path dir = Paths.get("media", "branding");
        Files.createDirectories(dir);
        final Path target = dir.resolve(filename);
        Files.copy(
                file.getInputStream(),
                target,
                StandardCopyOption.REPLACE_EXISTING
        );
        return "/media/branding/" + filename;
    }

    @Getter
    @Setter
    public static final class ClienteAreaForm {

        private String homeHeroUrl;
        private String homeHeroTexto;
        private String homeHeroAlt;
        private String homeHeroVideoUrl;
        private String homeHeroMediaMode;
        private String homeHeroDesktopRatio;
        private String homeHeroMobileRatio;
        private String homeHeroFocus;
        private String homeHeroSurface;
        private String frontendFontFamily;
        private String frontendTextLight;
        private String frontendTextDark;
        private String frontendBgLight;
        private String frontendBgDark;
        private String cartMascotUrl;
        private String cartMascotAlt;
        private String cartMascotCaption;
        private String cartEmptyTitle;
        private String cartEmptyMessage;
        private String loginAnimationUrl;
        private String loginClientBgStart;
        private String loginClientBgMid;
        private String loginClientBgEnd;
        private String loginAdminBgStart;
        private String loginAdminBgMid;
        private String loginAdminBgEnd;
        private String aiAssistantName;
        private String uiNavHomeLabel;
        private String uiNavAssistantLabel;
        private String uiNavAccountLabel;
        private String uiNavCatalogLabel;
        private String uiFooterAboutTitle;
        private String storeDisplayName;
        private boolean heroSearchEnabled;
        private boolean heroPrincipalEnabled;
        private boolean trustStripEnabled;
        private boolean quickCategoriesEnabled;
        private Integer quickCategoriesLimit;
        private String trustItemPrimary;
        private String trustItemSecondary;
        private String trustItemTertiary;
        private String trustItemQuaternary;
        private String homeCarouselDefaultStyle;
        private Map<String, String> homeCarouselStyles = new LinkedHashMap<>();
        private List<HomeSectionForm> homeSections = new ArrayList<>();
        private boolean categorySectionsAutoEnabled;
        private List<CategorySectionForm> categorySections = new ArrayList<>();
    }

    @Getter
    @Setter
    public static final class HomeSectionForm {

        private String key;
        private String label;
        private String description;
        private boolean enabled;
        private String layout;
        private Integer order;
        private String title;
        private String subtitle;
        private String ctaLabel;
        private String ctaHref;

        public HomeSectionForm() {
            // Spring binding constructor.
        }

        public HomeSectionForm(
                final String key,
                final String label,
                final String description,
                final boolean enabled,
                final String layout,
                final Integer order,
                final String title,
                final String subtitle,
                final String ctaLabel,
                final String ctaHref
        ) {
            this.key = key;
            this.label = label;
            this.description = description;
            this.enabled = enabled;
            this.layout = layout;
            this.order = order;
            this.title = title;
            this.subtitle = subtitle;
            this.ctaLabel = ctaLabel;
            this.ctaHref = ctaHref;
        }
    }

    @Getter
    @Setter
    public static final class CategorySectionForm {

        private String key;
        private String title;
        private String categoriesText;
        private Integer limit;

        public CategorySectionForm() {
            // Spring binding constructor.
        }

        public CategorySectionForm(
                final String key,
                final String title,
                final String categoriesText,
                final Integer limit
        ) {
            this.key = key;
            this.title = title;
            this.categoriesText = categoriesText;
            this.limit = limit;
        }
    }
}
