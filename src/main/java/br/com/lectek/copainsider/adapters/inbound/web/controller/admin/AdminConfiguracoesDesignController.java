package br.com.lectek.copainsider.adapters.inbound.web.controller.admin;

import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/configuracoes/design")
@PreAuthorize("hasRole('ADMIN')")
public class AdminConfiguracoesDesignController {

    /**
     * Settings key for dark theme default.
     */
    private static final String KEY_DARK_DEFAULT = "design.dark_default";

    /**
     * Settings key for rounded borders option.
     */
    private static final String KEY_ROUNDED = "design.rounded";

    /**
     * Settings key for animation option.
     */
    private static final String KEY_ANIMACOES = "design.animacoes";

    /**
     * Settings key for font family.
     */
    private static final String KEY_FONT_FAMILY = "design.font_family";

    /**
     * Settings key for border radius token.
     */
    private static final String KEY_RADIUS = "design.radius";

    /**
     * Settings key for spacing token.
     */
    private static final String KEY_SPACING = "design.spacing";

    /**
     * Service that stores app settings.
     */
    private final AppSettingService settings;

    /**
     * Creates controller with settings service dependency.
     *
     * @param service settings service
     */
    public AdminConfiguracoesDesignController(final AppSettingService service) {
        this.settings = service;
    }

    /**
     * Renders design settings form.
     *
     * @param model view model
     * @return design settings page
     */
    @GetMapping
    public String form(final Model model) {
        final DesignForm form = new DesignForm();
        form.setDarkByDefault(settings.getBoolean(KEY_DARK_DEFAULT, false));
        form.setUsarBordasArredondadas(settings.getBoolean(KEY_ROUNDED, true));
        form.setAnimacoesAtivas(settings.getBoolean(KEY_ANIMACOES, true));
        form.setFontFamily(settings.getOrDefault(KEY_FONT_FAMILY, ""));
        form.setRadius(settings.getOrDefault(KEY_RADIUS, ""));
        form.setSpacing(settings.getOrDefault(KEY_SPACING, ""));
        model.addAttribute("design", form);
        return "pages/admin/configuracoes/design";
    }

    /**
     * Persists design settings.
     *
     * @param form form payload
     * @param ra redirect attributes
     * @return redirect to design settings page
     */
    @PostMapping
    public String salvar(
            @ModelAttribute("design") final DesignForm form,
            final RedirectAttributes ra
    ) {
        settings.upsert(
                KEY_DARK_DEFAULT,
                bool(form.getDarkByDefault()),
                "Tema escuro por padrao"
        );
        settings.upsert(
                KEY_ROUNDED,
                bool(form.getUsarBordasArredondadas()),
                "Bordas arredondadas"
        );
        settings.upsert(
                KEY_ANIMACOES,
                bool(form.getAnimacoesAtivas()),
                "Animacoes ativas"
        );
        settings.upsert(
                KEY_FONT_FAMILY,
                nullSafe(form.getFontFamily()),
                "Font family"
        );
        settings.upsert(
                KEY_RADIUS,
                nullSafe(form.getRadius()),
                "Raio de borda"
        );
        settings.upsert(
                KEY_SPACING,
                nullSafe(form.getSpacing()),
                "Base de espacamento"
        );
        ra.addFlashAttribute("success", "Design atualizado.");
        return "redirect:/admin/configuracoes/design";
    }

    private static String nullSafe(final String value) {
        return value == null ? "" : value.trim();
    }

    private static String bool(final Boolean value) {
        return Boolean.TRUE.equals(value) ? "true" : "false";
    }

    /**
     * Form payload for design settings page.
     */
    @Getter
    @Setter
    public static final class DesignForm {

        /**
         * Dark mode default flag.
         */
        private Boolean darkByDefault;

        /**
         * Rounded borders flag.
         */
        private Boolean usarBordasArredondadas;

        /**
         * Animation enabled flag.
         */
        private Boolean animacoesAtivas;

        /**
         * Font family token.
         */
        private String fontFamily;

        /**
         * Radius token.
         */
        private String radius;

        /**
         * Spacing token.
         */
        private String spacing;
    }
}
