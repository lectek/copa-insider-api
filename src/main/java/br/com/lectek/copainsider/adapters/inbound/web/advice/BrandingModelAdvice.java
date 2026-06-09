package br.com.lectek.copainsider.adapters.inbound.web.advice;

import br.com.lectek.copainsider.adapters.outbound.auth.jwt.model.JwtPrincipal;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.CustomerEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.CustomerRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.lectek.copainsider.adapters.inbound.web.support.TenantScopedSettingsService;
import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@Component
@ControllerAdvice
public class BrandingModelAdvice {

    private static final Logger log = LoggerFactory.getLogger(BrandingModelAdvice.class);

    private static final String KEY_LOGO_URL = "branding.logo_url";
    private static final String KEY_FAVICON_URL = "branding.favicon_url";
    private static final String KEY_HOME_HERO_URL = "branding.home_hero_url";
    private static final String KEY_HOME_HERO_TEXTO = "branding.home_hero_texto";
    private static final String KEY_HOME_HERO_ALT = "branding.home_hero_alt";
    private static final String KEY_HOME_HERO_VIDEO_URL = "branding.home_hero_video_url";
    private static final String KEY_HOME_HERO_MEDIA_MODE = "branding.home_hero_media_mode";
    private static final String KEY_HOME_HERO_DESKTOP_RATIO = "branding.home_hero_desktop_ratio";
    private static final String KEY_HOME_HERO_MOBILE_RATIO = "branding.home_hero_mobile_ratio";
    private static final String KEY_HOME_HERO_FOCUS = "branding.home_hero_focus";
    private static final String KEY_HOME_HERO_SURFACE = "branding.home_hero_surface";
    private static final String KEY_FRONTEND_FONT_FAMILY = "cliente.frontend.font_family";
    private static final String KEY_FRONTEND_TEXT_LIGHT = "cliente.frontend.text_light";
    private static final String KEY_FRONTEND_TEXT_DARK = "cliente.frontend.text_dark";
    private static final String KEY_FRONTEND_BG_LIGHT = "cliente.frontend.bg_light";
    private static final String KEY_FRONTEND_BG_DARK = "cliente.frontend.bg_dark";
    private static final String KEY_CART_MASCOT_URL = "cliente.cart.mascot_url";
    private static final String KEY_CART_MASCOT_ALT = "cliente.cart.mascot_alt";
    private static final String KEY_CART_MASCOT_CAPTION = "cliente.cart.mascot_caption";
    private static final String KEY_CART_EMPTY_TITLE = "cliente.cart.empty_title";
    private static final String KEY_CART_EMPTY_MESSAGE = "cliente.cart.empty_message";
    private static final String KEY_BRANDING_FONT_FAMILY = "branding.font_family";
    private static final String KEY_DESIGN_FONT_FAMILY = "design.font_family";
    private static final String KEY_LOGO_SIZE = "branding.logo_size";
    private static final String KEY_COLOR_PRIMARY = "branding.cor_primaria";
    private static final String KEY_COLOR_SECONDARY = "branding.cor_secundaria";
    private static final String KEY_COLOR_ACCENT = "branding.cor_acento";
    private static final String KEY_AI_ASSISTANT_NAME = "ai.assistant.name";
    private static final String KEY_UI_NAV_HOME_LABEL = "ui.nav.home_label";
    private static final String KEY_UI_NAV_ASSISTANT_LABEL = "ui.nav.assistant_label";
    private static final String KEY_UI_NAV_ACCOUNT_LABEL = "ui.nav.account_label";
    private static final String KEY_UI_NAV_CATALOG_LABEL = "ui.nav.catalog_label";
    private static final String KEY_UI_FOOTER_ABOUT_TITLE = "ui.footer.about_title";
    private static final String KEY_LOGIN_ANIMATION_URL = "login.animation_url";
    private static final String KEY_LOGIN_CLIENT_BG_START = "login.client.bg_start";
    private static final String KEY_LOGIN_CLIENT_BG_MID = "login.client.bg_mid";
    private static final String KEY_LOGIN_CLIENT_BG_END = "login.client.bg_end";
    private static final String KEY_LOGIN_ADMIN_BG_START = "login.admin.bg_start";
    private static final String KEY_LOGIN_ADMIN_BG_MID = "login.admin.bg_mid";
    private static final String KEY_LOGIN_ADMIN_BG_END = "login.admin.bg_end";
    private static final String LEGACY_LOGO_URL = "GERAL.logo_inicial_url";
    private static final String LEGACY_FAVICON_URL = "GERAL.favicon_url";
    private static final String LEGACY_HOME_HERO_URL = "GERAL.home_hero_imagem_url";
    private static final String LEGACY_HOME_HERO_TEXTO = "GERAL.home_hero_texto";

    private static final String FALLBACK_LOGO = "/images/logonova.png";
    private static final String FALLBACK_FAVICON = "/images/favicon.png";
    private static final String FALLBACK_HOME_HERO = "/images/absorventepronto.png";
    private static final String FALLBACK_CART_MASCOT = "/images/mascote-carrinho.png";

    private static final String DEFAULT_FRONTEND_FONT =
            "'Source Sans 3', system-ui, Segoe UI, Roboto, Arial, sans-serif";
    private static final String DEFAULT_FRONTEND_TEXT_LIGHT = "#0f172a";
    private static final String DEFAULT_FRONTEND_TEXT_DARK = "#f8fafc";
    private static final String DEFAULT_FRONTEND_BG_LIGHT = "#f8fafc";
    private static final String DEFAULT_FRONTEND_BG_DARK = "#0b1220";
    private static final String DEFAULT_CART_MASCOT_ALT = "Mascote CopaInsider";
    private static final String DEFAULT_CART_MASCOT_CAPTION = "Seu pedido com carinho e seguranca.";
    private static final String DEFAULT_CART_EMPTY_TITLE = "Seu carrinho esta vazio";
    private static final String DEFAULT_CART_EMPTY_MESSAGE =
            "Volte ao catalogo para adicionar produtos.";
    private static final String DEFAULT_LOGO_SIZE = "medium";
    private static final String DEFAULT_COLOR_PRIMARY = "#1f6feb";
    private static final String DEFAULT_COLOR_SECONDARY = "#0ea5a4";
    private static final String DEFAULT_COLOR_ACCENT = "#0ea5e9";
    private static final String DEFAULT_AI_ASSISTANT_NAME = "Alysson";
    private static final String DEFAULT_UI_NAV_HOME_LABEL = "Inicio";
    private static final String DEFAULT_UI_NAV_ACCOUNT_LABEL = "Minha conta";
    private static final String DEFAULT_UI_NAV_CATALOG_LABEL = "Catalogo";
    private static final String DEFAULT_UI_FOOTER_ABOUT_TITLE = "Sobre a loja";
    private static final String DEFAULT_LOGIN_ANIMATION_URL = "/animations/mascote-flow-loop.mp4";
    private static final String DEFAULT_LOGIN_CLIENT_BG_START = "#04020f";
    private static final String DEFAULT_LOGIN_CLIENT_BG_MID = "#071536";
    private static final String DEFAULT_LOGIN_CLIENT_BG_END = "#0b2d64";
    private static final String DEFAULT_LOGIN_ADMIN_BG_START = "#1f0828";
    private static final String DEFAULT_LOGIN_ADMIN_BG_MID = "#2b1452";
    private static final String DEFAULT_LOGIN_ADMIN_BG_END = "#163a6b";
    private static final String QUERY_TENANT_ID = "tenantId";
    private static final String SESSION_TENANT_CONTEXT_ID = "tenantContextId";
    private static final String ATTR_TENANT_CONTEXT_ACTIVE = "tenantContextActive";
    private static final String ATTR_HEADER_USER_AUTHENTICATED = "headerUserAuthenticated";
    private static final String ATTR_HEADER_USER_NAME = "headerUserName";
    private static final String ATTR_HEADER_USER_AVATAR_URL = "headerUserAvatarUrl";
    private static final String ATTR_HEADER_USER_INITIAL = "headerUserInitial";
    private static final String DEFAULT_HEADER_USER_INITIAL = "U";
    private static final String REQUEST_ATTR_JWT_CLAIMS = "jwt.claims";
    private static final String CLAIM_TENANT = "tenant";
    private static final String CLAIM_EMAIL = "email";

    private final TenantScopedSettingsService tenantScopedSettings;
    private final AppSettingService appSettings;
    private final ResourceLoader resourceLoader;
    private final UsuarioRepository usuarios;
    private final CustomerRepository customers;

    public BrandingModelAdvice(ObjectProvider<TenantScopedSettingsService> tenantScopedSettingsProvider,
                               ObjectProvider<AppSettingService> appSettingsProvider,
                               ResourceLoader resourceLoader,
                               ObjectProvider<UsuarioRepository> usuariosProvider,
                               ObjectProvider<CustomerRepository> customersProvider) {
        this.tenantScopedSettings = tenantScopedSettingsProvider.getIfAvailable();
        this.appSettings = appSettingsProvider.getIfAvailable();
        this.resourceLoader = resourceLoader;
        this.usuarios = usuariosProvider.getIfAvailable();
        this.customers = customersProvider.getIfAvailable();
    }

    @ModelAttribute
    public void addBranding(HttpServletRequest request, Model model) {
        String uri = request != null ? request.getRequestURI() : null;
        if (uri != null && shouldSkipBranding(uri)) {
            return;
        }
        final String tenantContextId = resolveTenantContextId(request);

        String logoUrl = firstNonBlank(
                settingForTenant(tenantContextId, KEY_LOGO_URL, ""),
                settingForTenant(tenantContextId, LEGACY_LOGO_URL, ""),
                FALLBACK_LOGO
        );
        String faviconUrl = firstNonBlank(
                settingForTenant(tenantContextId, KEY_FAVICON_URL, ""),
                settingForTenant(tenantContextId, LEGACY_FAVICON_URL, ""),
                FALLBACK_FAVICON
        );
        String heroSetting = firstNonBlank(
                settingForTenant(tenantContextId, KEY_HOME_HERO_URL, ""),
                settingForTenant(tenantContextId, LEGACY_HOME_HERO_URL, ""),
                ""
        );
        String heroUrl = resolveHeroUrl(heroSetting);
        String heroTexto = firstNonBlank(
                settingForTenant(tenantContextId, KEY_HOME_HERO_TEXTO, ""),
                settingForTenant(tenantContextId, LEGACY_HOME_HERO_TEXTO, ""),
                ""
        );
        String heroAlt = firstNonBlank(
                settingForTenant(tenantContextId, KEY_HOME_HERO_ALT, ""),
                "",
                "Campanha institucional CopaInsider"
        );

        Resource fallback = resourceLoader.getResource("classpath:static" + FALLBACK_HOME_HERO);
        boolean fallbackAvailable = fallback.exists();
        boolean fallbackInUse = FALLBACK_HOME_HERO.equals(heroUrl) && !looksLikeImage(heroSetting);

        model.addAttribute("brandingLogoUrl", logoUrl);
        model.addAttribute("brandingFaviconUrl", faviconUrl);
        model.addAttribute("brandingHomeHeroUrl", heroSetting);
        model.addAttribute("brandingHomeHeroImageUrl", heroUrl);
        String heroVideoSetting =
                settingForTenant(tenantContextId, KEY_HOME_HERO_VIDEO_URL, "");
        String heroVideoUrl = looksLikeVideo(heroVideoSetting) ? heroVideoSetting : "";
        String heroMediaMode = sanitizeChoice(
                settingForTenant(tenantContextId, KEY_HOME_HERO_MEDIA_MODE, ""),
                "contain",
                "cover"
        );
        String heroDesktopRatio = sanitizeChoice(
                settingForTenant(tenantContextId, KEY_HOME_HERO_DESKTOP_RATIO, ""),
                "wide",
                "cinema",
                "poster"
        );
        String heroMobileRatio = sanitizeChoice(
                settingForTenant(tenantContextId, KEY_HOME_HERO_MOBILE_RATIO, ""),
                "square",
                "poster",
                "tall"
        );
        String heroPosition = resolveHeroPosition(
                settingForTenant(tenantContextId, KEY_HOME_HERO_FOCUS, "")
        );
        String heroSurface = sanitizeColor(
                settingForTenant(tenantContextId, KEY_HOME_HERO_SURFACE, ""),
                "#ffffff"
        );
        String frontendFontFamily = resolveFrontendFontFamily(tenantContextId);
        String frontendTextLight = sanitizeColor(
                settingForTenant(tenantContextId, KEY_FRONTEND_TEXT_LIGHT, ""),
                DEFAULT_FRONTEND_TEXT_LIGHT
        );
        String frontendTextDark = sanitizeColor(
                settingForTenant(tenantContextId, KEY_FRONTEND_TEXT_DARK, ""),
                DEFAULT_FRONTEND_TEXT_DARK
        );
        String frontendBgLight = sanitizeColor(
                settingForTenant(tenantContextId, KEY_FRONTEND_BG_LIGHT, ""),
                DEFAULT_FRONTEND_BG_LIGHT
        );
        String frontendBgDark = sanitizeColor(
                settingForTenant(tenantContextId, KEY_FRONTEND_BG_DARK, ""),
                DEFAULT_FRONTEND_BG_DARK
        );
        String frontendPrimary = sanitizeColor(
                settingForTenant(tenantContextId, KEY_COLOR_PRIMARY, ""),
                DEFAULT_COLOR_PRIMARY
        );
        String frontendSecondary = sanitizeColor(
                settingForTenant(tenantContextId, KEY_COLOR_SECONDARY, ""),
                DEFAULT_COLOR_SECONDARY
        );
        String frontendAccent = sanitizeColor(
                settingForTenant(tenantContextId, KEY_COLOR_ACCENT, ""),
                DEFAULT_COLOR_ACCENT
        );
        final String defaultAssistantName = defaultAiAssistantName(tenantContextId);
        String assistantName = firstNonBlank(
                settingForTenant(tenantContextId, KEY_AI_ASSISTANT_NAME, ""),
                "",
                defaultAssistantName
        );
        String navHomeLabel = firstNonBlank(
                settingForTenant(tenantContextId, KEY_UI_NAV_HOME_LABEL, ""),
                "",
                DEFAULT_UI_NAV_HOME_LABEL
        );
        String navAssistantLabel = firstNonBlank(
                settingForTenant(tenantContextId, KEY_UI_NAV_ASSISTANT_LABEL, ""),
                "",
                assistantName
        );
        String navAccountLabel = firstNonBlank(
                settingForTenant(tenantContextId, KEY_UI_NAV_ACCOUNT_LABEL, ""),
                "",
                DEFAULT_UI_NAV_ACCOUNT_LABEL
        );
        String navCatalogLabel = firstNonBlank(
                settingForTenant(tenantContextId, KEY_UI_NAV_CATALOG_LABEL, ""),
                "",
                DEFAULT_UI_NAV_CATALOG_LABEL
        );
        String footerAboutTitle = firstNonBlank(
                settingForTenant(tenantContextId, KEY_UI_FOOTER_ABOUT_TITLE, ""),
                "",
                DEFAULT_UI_FOOTER_ABOUT_TITLE
        );
        String loginAnimationCandidate =
                settingForTenant(tenantContextId, KEY_LOGIN_ANIMATION_URL, "");
        String loginAnimationUrl = looksLikeVideo(loginAnimationCandidate)
                ? loginAnimationCandidate
                : DEFAULT_LOGIN_ANIMATION_URL;
        String loginClientBgStart = sanitizeColor(
                settingForTenant(tenantContextId, KEY_LOGIN_CLIENT_BG_START, ""),
                DEFAULT_LOGIN_CLIENT_BG_START
        );
        String loginClientBgMid = sanitizeColor(
                settingForTenant(tenantContextId, KEY_LOGIN_CLIENT_BG_MID, ""),
                DEFAULT_LOGIN_CLIENT_BG_MID
        );
        String loginClientBgEnd = sanitizeColor(
                settingForTenant(tenantContextId, KEY_LOGIN_CLIENT_BG_END, ""),
                DEFAULT_LOGIN_CLIENT_BG_END
        );
        String loginAdminBgStart = sanitizeColor(
                settingForTenant(tenantContextId, KEY_LOGIN_ADMIN_BG_START, ""),
                DEFAULT_LOGIN_ADMIN_BG_START
        );
        String loginAdminBgMid = sanitizeColor(
                settingForTenant(tenantContextId, KEY_LOGIN_ADMIN_BG_MID, ""),
                DEFAULT_LOGIN_ADMIN_BG_MID
        );
        String loginAdminBgEnd = sanitizeColor(
                settingForTenant(tenantContextId, KEY_LOGIN_ADMIN_BG_END, ""),
                DEFAULT_LOGIN_ADMIN_BG_END
        );
        String heroVideoType = detectVideoMimeType(heroVideoUrl);
        String cartMascotSetting = firstNonBlank(
                settingForTenant(tenantContextId, KEY_CART_MASCOT_URL, ""),
                "",
                FALLBACK_CART_MASCOT
        );
        String cartMascotUrl = looksLikeImage(cartMascotSetting)
                ? cartMascotSetting
                : FALLBACK_CART_MASCOT;
        String cartMascotAlt = firstNonBlank(
                settingForTenant(tenantContextId, KEY_CART_MASCOT_ALT, ""),
                "",
                DEFAULT_CART_MASCOT_ALT
        );
        String cartMascotCaption = firstNonBlank(
                settingForTenant(tenantContextId, KEY_CART_MASCOT_CAPTION, ""),
                "",
                DEFAULT_CART_MASCOT_CAPTION
        );
        String cartEmptyTitle = firstNonBlank(
                settingForTenant(tenantContextId, KEY_CART_EMPTY_TITLE, ""),
                "",
                DEFAULT_CART_EMPTY_TITLE
        );
        String cartEmptyMessage = firstNonBlank(
                settingForTenant(tenantContextId, KEY_CART_EMPTY_MESSAGE, ""),
                "",
                DEFAULT_CART_EMPTY_MESSAGE
        );
        model.addAttribute("brandingHomeHeroTexto", heroTexto);
        model.addAttribute("brandingHomeHeroAlt", heroAlt);
        model.addAttribute("brandingHomeHeroVideoUrl", heroVideoUrl);
        model.addAttribute("brandingHomeHeroVideoPoster", heroUrl);
        model.addAttribute("brandingHomeHeroVideoType", heroVideoType);
        model.addAttribute("brandingHomeHeroMediaMode", heroMediaMode);
        model.addAttribute("brandingHomeHeroDesktopRatio", heroDesktopRatio);
        model.addAttribute("brandingHomeHeroMobileRatio", heroMobileRatio);
        model.addAttribute("brandingHomeHeroPosition", heroPosition);
        model.addAttribute("brandingHomeHeroSurface", heroSurface);
        model.addAttribute(
                "brandingLogoSize",
                parseLogoSize(settingForTenant(tenantContextId, KEY_LOGO_SIZE, DEFAULT_LOGO_SIZE))
        );
        model.addAttribute("clientFrontendFontFamily", frontendFontFamily);
        model.addAttribute("clientFrontendTextLight", frontendTextLight);
        model.addAttribute("clientFrontendTextDark", frontendTextDark);
        model.addAttribute("clientFrontendBgLight", frontendBgLight);
        model.addAttribute("clientFrontendBgDark", frontendBgDark);
        model.addAttribute("clientFrontendPrimary", frontendPrimary);
        model.addAttribute("clientFrontendSecondary", frontendSecondary);
        model.addAttribute("clientFrontendAccent", frontendAccent);
        model.addAttribute("aiAssistantName", assistantName);
        model.addAttribute("uiNavHomeLabel", navHomeLabel);
        model.addAttribute("uiNavAssistantLabel", navAssistantLabel);
        model.addAttribute("uiNavAccountLabel", navAccountLabel);
        model.addAttribute("uiNavCatalogLabel", navCatalogLabel);
        model.addAttribute("uiFooterAboutTitle", footerAboutTitle);
        model.addAttribute("loginAnimationUrl", loginAnimationUrl);
        model.addAttribute("loginClientBgStart", loginClientBgStart);
        model.addAttribute("loginClientBgMid", loginClientBgMid);
        model.addAttribute("loginClientBgEnd", loginClientBgEnd);
        model.addAttribute("loginAdminBgStart", loginAdminBgStart);
        model.addAttribute("loginAdminBgMid", loginAdminBgMid);
        model.addAttribute("loginAdminBgEnd", loginAdminBgEnd);
        model.addAttribute("brandingCartMascotUrl", cartMascotUrl);
        model.addAttribute("brandingCartMascotAlt", cartMascotAlt);
        model.addAttribute("brandingCartMascotCaption", cartMascotCaption);
        model.addAttribute("brandingCartEmptyTitle", cartEmptyTitle);
        model.addAttribute("brandingCartEmptyMessage", cartEmptyMessage);
        model.addAttribute("brandingHeroPublic", true);
        model.addAttribute("brandingHeroFallbackUsed", fallbackInUse);
        model.addAttribute("brandingHeroFallbackAvailable", fallbackAvailable);
        model.addAttribute(SESSION_TENANT_CONTEXT_ID, tenantContextId);
        model.addAttribute(ATTR_TENANT_CONTEXT_ACTIVE, !tenantContextId.isBlank());
        addHeaderUser(request, model);

        log.debug("Hero available publicly (logged-out) {} (fallbackUsed={}, fileExists={}, heroSetting={})",
                heroUrl, fallbackInUse, fallbackAvailable, heroSetting);
    }

    private void addHeaderUser(HttpServletRequest request, Model model) {
        String identity = request != null && request.getUserPrincipal() != null
                ? nonBlank(request.getUserPrincipal().getName())
                : "";
        String authEmail = extractEmail(request);
        if (identity.isBlank()) {
            applyAnonymousHeaderUser(model);
            return;
        }

        model.addAttribute(ATTR_HEADER_USER_AUTHENTICATED, true);
        UsuarioEntity usuario = null;
        if (usuarios != null) {
            usuario = usuarios.findByEmailOrCpf(identity).orElse(null);
            if (usuario == null && !authEmail.isBlank()) {
                usuario = usuarios.findByEmailIgnoreCase(authEmail).orElse(null);
            }
        }
        if (usuario != null) {
            String displayName = firstNonBlank(usuario.getNome(), usuario.getEmail(), identity);
            applyHeaderUser(model, displayName, nonBlank(usuario.getAvatarUrl()));
            return;
        }

        String customerEmail = resolveCustomerEmail(authEmail, identity);
        if (!customerEmail.isBlank() && customers != null) {
            CustomerEntity customer = customers.findByEmail(customerEmail).orElse(null);
            if (customer != null) {
                String displayName = firstNonBlank(customer.getNome(), customer.getEmail(), customerEmail);
                applyHeaderUser(model, displayName, nonBlank(customer.getAvatarUrl()));
                return;
            }
        }

        applyHeaderUser(model, identity, "");
    }

    private String resolveTenantContextId(final HttpServletRequest request) {
        if (request == null) {
            return "";
        }

        final String explicitTenantId = nonBlank(request.getParameter(QUERY_TENANT_ID));
        if (!explicitTenantId.isBlank()) {
            storeTenantContextId(request, explicitTenantId);
            return explicitTenantId;
        }

        final String authenticatedTenantId = extractTenantId(request);
        if (!authenticatedTenantId.isBlank()) {
            storeTenantContextId(request, authenticatedTenantId);
            return authenticatedTenantId;
        }

        return readTenantContextId(request.getSession(false));
    }

    private void storeTenantContextId(
            final HttpServletRequest request,
            final String tenantContextId
    ) {
        if (request == null || tenantContextId.isBlank()) {
            return;
        }
        request.getSession().setAttribute(SESSION_TENANT_CONTEXT_ID, tenantContextId);
    }

    private String readTenantContextId(final HttpSession session) {
        if (session == null) {
            return "";
        }
        final Object tenantContextId = session.getAttribute(SESSION_TENANT_CONTEXT_ID);
        return tenantContextId == null ? "" : nonBlank(tenantContextId.toString());
    }

    private String extractTenantId(final HttpServletRequest request) {
        if (request == null) {
            return "";
        }

        final Principal principal = request.getUserPrincipal();
        if (principal instanceof Authentication authentication
                && authentication.getPrincipal() instanceof JwtPrincipal jwtPrincipal) {
            final String tenant = nonBlank(jwtPrincipal.getTenant());
            if (!tenant.isBlank()) {
                return tenant;
            }
        }

        final Object claimsAttr = request.getAttribute(REQUEST_ATTR_JWT_CLAIMS);
        if (claimsAttr instanceof Map<?, ?> rawClaims) {
            Object tenant = rawClaims.get(CLAIM_TENANT);
            if (tenant == null) {
                tenant = rawClaims.get(QUERY_TENANT_ID);
            }
            return tenant == null ? "" : nonBlank(tenant.toString());
        }

        return "";
    }

    private String extractEmail(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return "";
        }
        String email = normalizeEmailCandidate(principal.getName());
        if (!email.isBlank()) {
            return email;
        }
        if (principal instanceof Authentication authentication) {
            return extractEmail(authentication);
        }
        return "";
    }

    private String extractEmail(final Authentication authentication) {
        if (authentication == null) {
            return "";
        }
        final Object authPrincipal = authentication.getPrincipal();
        final String userDetailsEmail = extractUserDetailsEmail(authPrincipal);
        if (!userDetailsEmail.isBlank()) {
            return userDetailsEmail;
        }
        return extractOAuthEmail(authPrincipal);
    }

    private String extractUserDetailsEmail(final Object authPrincipal) {
        if (!(authPrincipal instanceof UserDetails userDetails)) {
            return "";
        }
        return normalizeEmailCandidate(userDetails.getUsername());
    }

    private String extractOAuthEmail(final Object authPrincipal) {
        if (!(authPrincipal instanceof OAuth2User oauth2User)) {
            return "";
        }
        final Object email = oauth2User.getAttributes().get(CLAIM_EMAIL);
        return email == null ? "" : normalizeEmailCandidate(email.toString());
    }

    private static boolean shouldSkipBranding(String uri) {
        return uri.startsWith("/actuator")
                || uri.startsWith("/api/")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/swagger-ui")
                || uri.startsWith("/docs");
    }

    private String resolveHeroUrl(String heroSetting) {
        if (heroSetting == null || heroSetting.isBlank()) {
            return FALLBACK_HOME_HERO;
        }
        if (looksLikeImage(heroSetting)) {
            return heroSetting;
        }
        return FALLBACK_HOME_HERO;
    }

    private static boolean looksLikeImage(String url) {
        if (url == null) {
            return false;
        }
        return url.matches("(?i).*\\.(png|jpg|jpeg|webp|gif|svg|avif)(\\?.*)?$");
    }

    private static boolean looksLikeVideo(String url) {
        if (url == null) {
            return false;
        }
        return url.matches("(?i).*\\.(mp4|mov|webm|ogg|m4v)(\\?.*)?$");
    }

    private static String detectVideoMimeType(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        String normalized = url.toLowerCase();
        if (normalized.matches(".*\\.(mp4)(\\?.*)?$")) {
            return "video/mp4";
        }
        if (normalized.matches(".*\\.(webm)(\\?.*)?$")) {
            return "video/webm";
        }
        if (normalized.matches(".*\\.(mov|m4v)(\\?.*)?$")) {
            return "video/quicktime";
        }
        if (normalized.matches(".*\\.(ogg)(\\?.*)?$")) {
            return "video/ogg";
        }
        return "";
    }

    private String resolveFrontendFontFamily(final String tenantContextId) {
        return firstNonBlank(
                settingForTenant(tenantContextId, KEY_FRONTEND_FONT_FAMILY, ""),
                settingForTenant(tenantContextId, KEY_BRANDING_FONT_FAMILY, ""),
                settingForTenant(tenantContextId, KEY_DESIGN_FONT_FAMILY, ""),
                DEFAULT_FRONTEND_FONT
        );
    }

    private String settingForTenant(
            final String tenantContextId,
            final String key,
            final String defaultValue
    ) {
        if (tenantScopedSettings != null) {
            return tenantScopedSettings.getOrDefault(
                    tenantContextId,
                    key,
                    defaultValue
            );
        }
        if (appSettings == null) {
            return defaultValue;
        }
        final String normalizedTenant = nonBlank(tenantContextId);
        if (!normalizedTenant.isBlank()) {
            final String tenantScopedValue = appSettings.getOrDefault(
                    "tenant." + normalizedTenant + "." + key,
                    ""
            );
            if (!tenantScopedValue.isBlank()) {
                return tenantScopedValue;
            }
        }
        return appSettings.getOrDefault(key, defaultValue);
    }

    private static String sanitizeChoice(String value, String... allowed) {
        if (value == null || value.isBlank()) {
            return allowed[0];
        }
        String normalized = value.trim().toLowerCase();
        for (String candidate : allowed) {
            if (candidate.equals(normalized)) {
                return normalized;
            }
        }
        return allowed[0];
    }

    private static String resolveHeroPosition(String value) {
        String normalized = sanitizeChoice(value, "center", "top", "left", "right");
        return switch (normalized) {
            case "top" -> "center top";
            case "left" -> "left center";
            case "right" -> "right center";
            default -> "center center";
        };
    }

    private static String sanitizeColor(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        String normalized = value.trim();
        if (normalized.matches("(?i)^#[0-9a-f]{6}$")) {
            return normalized;
        }
        return defaultValue;
    }

    private static String firstNonBlank(String primary, String fallback, String defaultValue) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return defaultValue;
    }

    private static String firstNonBlank(
            String primary,
            String fallback,
            String third,
            String defaultValue
    ) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        if (third != null && !third.isBlank()) {
            return third;
        }
        return defaultValue;
    }

    private static String nonBlank(String value) {
        return value == null ? "" : value.trim();
    }

    private static String defaultAiAssistantName(final String tenantContextId) {
        final String normalized = nonBlank(tenantContextId).toLowerCase();
        if (normalized.contains("embalando")) {
            return "Livoninho";
        }
        if (normalized.contains("saude")) {
            return DEFAULT_AI_ASSISTANT_NAME;
        }
        return DEFAULT_AI_ASSISTANT_NAME;
    }

    private static String normalizeEmailCandidate(final String value) {
        final String normalized = nonBlank(value);
        return normalized.contains("@") ? normalized.toLowerCase() : "";
    }

    private static String resolveCustomerEmail(
            final String authEmail,
            final String identity
    ) {
        if (!authEmail.isBlank()) {
            return authEmail;
        }
        return normalizeEmailCandidate(identity);
    }

    private static void applyAnonymousHeaderUser(final Model model) {
        model.addAttribute(ATTR_HEADER_USER_AUTHENTICATED, false);
        model.addAttribute(ATTR_HEADER_USER_NAME, "");
        model.addAttribute(ATTR_HEADER_USER_AVATAR_URL, "");
        model.addAttribute(ATTR_HEADER_USER_INITIAL, DEFAULT_HEADER_USER_INITIAL);
    }

    private static void applyHeaderUser(
            final Model model,
            final String displayName,
            final String avatarUrl
    ) {
        model.addAttribute(ATTR_HEADER_USER_NAME, displayName);
        model.addAttribute(ATTR_HEADER_USER_AVATAR_URL, avatarUrl);
        model.addAttribute(ATTR_HEADER_USER_INITIAL, firstLetter(displayName));
    }

    private static String firstLetter(String value) {
        String normalized = nonBlank(value);
        if (normalized.isBlank()) {
            return DEFAULT_HEADER_USER_INITIAL;
        }
        return normalized.substring(0, 1).toUpperCase();
    }

    private static String parseLogoSize(String raw) {
        if (raw == null) {
            return DEFAULT_LOGO_SIZE;
        }
        String normalized = raw.trim().toLowerCase();
        return switch (normalized) {
            case "small", "compact" -> "small";
            case "large", "expanded" -> "large";
            default -> DEFAULT_LOGO_SIZE;
        };
    }
}
