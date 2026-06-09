package br.com.lectek.copainsider.adapters.inbound.web.controller.admin;

import br.com.lectek.copainsider.adapters.inbound.web.support.TenantScopedSettingsService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
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
@RequestMapping("/admin/configuracoes/branding")
@PreAuthorize("hasRole('ADMIN')")
public class AdminConfiguracoesBrandingController {

    /**
     * Branding key for default logo URL.
     */
    private static final String KEY_LOGO_URL = "branding.logo_url";

    /**
     * Branding key for dark logo URL.
     */
    private static final String KEY_LOGO_URL_DARK = "branding.logo_url_dark";

    /**
     * Branding key for favicon URL.
     */
    private static final String KEY_FAVICON_URL = "branding.favicon_url";

    /**
     * Branding key for primary color.
     */
    private static final String KEY_COLOR_PRIMARY = "branding.cor_primaria";

    /**
     * Branding key for secondary color.
     */
    private static final String KEY_COLOR_SECONDARY = "branding.cor_secundaria";

    /**
     * Branding key for accent color.
     */
    private static final String KEY_COLOR_ACCENT = "branding.cor_acento";

    /**
     * Branding key for promo banner URL.
     */
    private static final String KEY_BANNER_PROMO_URL =
            "branding.banner_promocao_url";

    /**
     * Branding key for font family.
     */
    private static final String KEY_FONT_FAMILY = "branding.font_family";

    /**
     * Branding key for theme.
     */
    private static final String KEY_THEME = "branding.tema";

    /**
     * Branding key for border radius.
     */
    private static final String KEY_RADIUS = "branding.radius";

    /**
     * Branding key for logo size.
     */
    private static final String KEY_LOGO_SIZE = "branding.logo_size";

    /**
     * Legacy key for logo URL.
     */
    private static final String LEGACY_LOGO_URL = "GERAL.logo_inicial_url";

    /**
     * Legacy key for favicon URL.
     */
    private static final String LEGACY_FAVICON_URL = "GERAL.favicon_url";

    /**
     * All keys loaded from settings store.
     */
    private static final Set<String> ALL_KEYS = Set.of(
            KEY_LOGO_URL,
            KEY_LOGO_URL_DARK,
            KEY_FAVICON_URL,
            KEY_COLOR_PRIMARY,
            KEY_COLOR_SECONDARY,
            KEY_COLOR_ACCENT,
            KEY_BANNER_PROMO_URL,
            KEY_FONT_FAMILY,
            KEY_THEME,
            KEY_RADIUS,
            KEY_LOGO_SIZE,
            LEGACY_LOGO_URL,
            LEGACY_FAVICON_URL
    );

    private final TenantScopedSettingsService tenantScopedSettings;

    /**
     * Creates controller with settings dependency.
     *
     * @param appSettingService settings service
     */
    public AdminConfiguracoesBrandingController(
            final TenantScopedSettingsService tenantScopedSettingsService
    ) {
        this.tenantScopedSettings = tenantScopedSettingsService;
    }

    /**
     * Renders branding configuration form.
     *
     * @param model view model
     * @return branding page
     */
    @GetMapping
    public String form(final Model model, final HttpServletRequest request) {
        final String tenantContextId = tenantScopedSettings.resolveTenantContextId(request);
        final Map<String, String> cfg = tenantScopedSettings.getAllByKeys(
                tenantContextId,
                ALL_KEYS
        );
        final BrandingForm branding = loadForm(cfg);
        model.addAttribute("branding", branding);
        model.addAttribute("brandingAssets", buildAssetUsage(branding));
        model.addAttribute(
                "tenantContextId",
                TenantScopedSettingsService.normalizeTenantId(tenantContextId)
        );
        model.addAttribute(
                "tenantScoped",
                !TenantScopedSettingsService.normalizeTenantId(tenantContextId).isBlank()
        );
        return "pages/admin/configuracoes/branding";
    }

    /**
     * Persists branding settings and optional uploaded logo.
     *
     * @param branding branding payload
     * @param logoFile optional logo file
     * @param ra redirect attributes
     * @return redirect to branding page
     */
    @PostMapping
    public String salvar(
            @ModelAttribute("branding") final BrandingForm branding,
            @RequestParam(name = "logoFile", required = false)
            final MultipartFile logoFile,
            @RequestParam(name = "tenantId", required = false)
            final String tenantId,
            final RedirectAttributes ra
    ) {
        final String tenantContextId = TenantScopedSettingsService.normalizeTenantId(tenantId);
        boolean uploadFailed = false;
        if (logoFile != null && !logoFile.isEmpty()) {
            try {
                branding.setLogoUrl(storeUpload(logoFile, "logo"));
            } catch (IOException ex) {
                uploadFailed = true;
            }
        }

        saveSetting(tenantContextId, KEY_LOGO_URL, branding.getLogoUrl(), "Logo principal");
        saveSetting(
                tenantContextId,
                KEY_LOGO_URL_DARK,
                branding.getLogoUrlDark(),
                "Logo para fundo escuro"
        );
        saveSetting(tenantContextId, KEY_FAVICON_URL, branding.getFaviconUrl(), "Favicon");
        saveSetting(
                tenantContextId,
                KEY_COLOR_PRIMARY,
                branding.getCorPrimaria(),
                "Cor primaria"
        );
        saveSetting(
                tenantContextId,
                KEY_COLOR_SECONDARY,
                branding.getCorSecundaria(),
                "Cor secundaria"
        );
        saveSetting(
                tenantContextId,
                KEY_COLOR_ACCENT,
                branding.getCorAcento(),
                "Cor acento"
        );
        saveSetting(
                tenantContextId,
                KEY_BANNER_PROMO_URL,
                branding.getBannerPromocao(),
                "Banner promocao"
        );
        saveSetting(
                tenantContextId,
                KEY_FONT_FAMILY,
                branding.getFontFamily(),
                "Font-family"
        );
        saveSetting(tenantContextId, KEY_THEME, branding.getTema(), "Tema");
        saveSetting(
                tenantContextId,
                KEY_RADIUS,
                branding.getRadius(),
                "Raio de borda"
        );
        saveSetting(
                tenantContextId,
                KEY_LOGO_SIZE,
                branding.getLogoSize(),
                "Tamanho da logo"
        );

        if (uploadFailed) {
            ra.addFlashAttribute(
                    "warning",
                    "Alguns uploads falharam; verifique os arquivos enviados."
            );
        } else {
            ra.addFlashAttribute(
                    "success",
                    tenantContextId.isBlank()
                            ? "Branding global atualizado."
                            : "Branding do tenant '" + tenantContextId + "' atualizado."
            );
        }
        if (tenantContextId.isBlank()) {
            return "redirect:/admin/configuracoes/branding";
        }
        return "redirect:/admin/configuracoes/branding?tenantId=" + tenantContextId;
    }

    /**
     * Saves one setting with null-safe value.
     *
     * @param key setting key
     * @param value setting value
     * @param description setting description
     */
    private void saveSetting(
            final String tenantId,
            final String key,
            final String value,
            final String description
    ) {
        tenantScopedSettings.upsert(tenantId, key, nullSafe(value), description);
    }

    /**
     * Loads branding form values from current settings.
     *
     * @return populated form
     */
    private BrandingForm loadForm(final Map<String, String> cfg) {
        final BrandingForm form = new BrandingForm();
        form.setLogoUrl(firstValue(cfg, KEY_LOGO_URL, LEGACY_LOGO_URL));
        form.setLogoUrlDark(cfg.getOrDefault(KEY_LOGO_URL_DARK, ""));
        form.setFaviconUrl(
                firstValue(cfg, KEY_FAVICON_URL, LEGACY_FAVICON_URL)
        );
        form.setCorPrimaria(cfg.getOrDefault(KEY_COLOR_PRIMARY, ""));
        form.setCorSecundaria(cfg.getOrDefault(KEY_COLOR_SECONDARY, ""));
        form.setCorAcento(cfg.getOrDefault(KEY_COLOR_ACCENT, ""));
        form.setBannerPromocao(cfg.getOrDefault(KEY_BANNER_PROMO_URL, ""));
        form.setFontFamily(cfg.getOrDefault(KEY_FONT_FAMILY, ""));
        form.setTema(cfg.getOrDefault(KEY_THEME, "auto"));
        form.setRadius(cfg.getOrDefault(KEY_RADIUS, ""));
        form.setLogoSize(cfg.getOrDefault(KEY_LOGO_SIZE, "medium"));
        return form;
    }

    private List<AssetUsageView> buildAssetUsage(final BrandingForm branding) {
        final String logoAtual = resolveAssetUrl(branding.getLogoUrl(), "/images/logonova.png");
        final String logoEscuraAtual = resolveAssetUrl(branding.getLogoUrlDark(), logoAtual);
        final String faviconAtual = resolveAssetUrl(branding.getFaviconUrl(), "/images/favicon.png");
        final String bannerAtual = resolveAssetUrl(branding.getBannerPromocao(), "");
        return List.of(
                new AssetUsageView(
                        "Logo ativa",
                        emptyToNull(logoAtual),
                        describeAssetSource(branding.getLogoUrl(), logoAtual)
                ),
                new AssetUsageView(
                        "Logo para fundo escuro",
                        emptyToNull(logoEscuraAtual),
                        describeAssetSource(branding.getLogoUrlDark(), logoEscuraAtual)
                ),
                new AssetUsageView(
                        "Favicon",
                        emptyToNull(faviconAtual),
                        describeAssetSource(branding.getFaviconUrl(), faviconAtual)
                ),
                new AssetUsageView(
                        "Banner promocional",
                        emptyToNull(bannerAtual),
                        describeAssetSource(branding.getBannerPromocao(), bannerAtual)
                )
        );
    }

    /**
     * Returns empty string when value is null.
     *
     * @param value source value
     * @return null-safe value
     */
    private static String nullSafe(final String value) {
        return value == null ? "" : value;
    }

    private static String emptyToNull(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }

    /**
     * Gets primary value or fallback key when primary is blank.
     *
     * @param cfg settings map
     * @param primary primary key
     * @param fallback fallback key
     * @return resolved value
     */
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

    private static String resolveAssetUrl(
            final String configuredUrl,
            final String fallbackUrl
    ) {
        if (configuredUrl != null && !configuredUrl.isBlank()) {
            return configuredUrl.trim();
        }
        return nullSafe(fallbackUrl).trim();
    }

    private static String describeAssetSource(
            final String configuredUrl,
            final String resolvedUrl
    ) {
        if (resolvedUrl == null || resolvedUrl.isBlank()) {
            return "Nenhum arquivo configurado.";
        }
        if (configuredUrl == null || configuredUrl.isBlank()) {
            return "Usando arquivo padrao interno da aplicacao.";
        }
        if (resolvedUrl.startsWith("/media/")) {
            return "Upload local salvo na API em " + resolvedUrl + ".";
        }
        if (resolvedUrl.startsWith("/")) {
            return "Arquivo interno publicado pela aplicacao em " + resolvedUrl + ".";
        }
        return "URL externa configurada diretamente no campo.";
    }

    /**
     * Stores uploaded branding file and returns public path.
     *
     * @param file uploaded file
     * @param prefix filename prefix
     * @return public file URL
     * @throws IOException when upload fails
     */
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

    /**
     * Form payload for branding settings page.
     */
    @Getter
    @Setter
    public static final class BrandingForm {

        /**
         * Main logo URL.
         */
        private String logoUrl;

        /**
         * Dark mode logo URL.
         */
        private String logoUrlDark;

        /**
         * Favicon URL.
         */
        private String faviconUrl;

        /**
         * Primary color.
         */
        private String corPrimaria;

        /**
         * Secondary color.
         */
        private String corSecundaria;

        /**
         * Accent color.
         */
        private String corAcento;

        /**
         * Promo banner URL.
         */
        private String bannerPromocao;

        /**
         * Font family.
         */
        private String fontFamily;

        /**
         * Theme option.
         */
        private String tema;

        /**
         * Border radius option.
         */
        private String radius;

        /**
         * Logo size option.
         */
        private String logoSize;
    }

    public record AssetUsageView(
            String label,
            String resolvedUrl,
            String sourceDescription
    ) {
    }
}
