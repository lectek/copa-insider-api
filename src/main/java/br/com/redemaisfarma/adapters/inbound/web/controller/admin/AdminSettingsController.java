package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.AppSettingEntity;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/settings")
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class AdminSettingsController {

    /**
     * First page index.
     */
    private static final int PAGE_ZERO = 0;

    /**
     * Minimum page size.
     */
    private static final int PAGE_SIZE_MIN = 1;

    /**
     * Success flash key.
     */
    private static final String SUCCESS_ATTR = "success";

    /**
     * Redirect to settings listing.
     */
    private static final String REDIRECT_SETTINGS = "redirect:/admin/settings";

    /**
     * View for settings listing.
     */
    private static final String VIEW_SETTINGS = "pages/admin/settings";

    /**
     * View for create/update form.
     */
    private static final String VIEW_SETTINGS_FORM =
            "pages/admin/settings-form";

    /**
     * Domain service for app settings.
     */
    private final AppSettingService service;

    /**
     * Creates controller with required service.
     *
     * @param appSettingService settings service
     */
    public AdminSettingsController(final AppSettingService appSettingService) {
        this.service = appSettingService;
    }

    /**
     * Lists persisted settings with filter and pagination.
     *
     * @param q optional search term
     * @param page page index
     * @param size page size
     * @param model view model
     * @return list page
     */
    @GetMapping
    public String list(
            @RequestParam(value = "q", required = false) final String q,
            @RequestParam(value = "page", defaultValue = "0") final int page,
            @RequestParam(value = "size", defaultValue = "20") final int size,
            final Model model
    ) {
        final PageRequest pageable = PageRequest.of(
                Math.max(page, PAGE_ZERO),
                Math.max(size, PAGE_SIZE_MIN)
        );
        final Page<AppSettingEntity> pageData = service.list(q, pageable);
        model.addAttribute("pageData", pageData);
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("size", size);
        return VIEW_SETTINGS;
    }

    /**
     * Renders form for creating a new setting.
     *
     * @param model view model
     * @return settings form page
     */
    @GetMapping("/new")
    public String newForm(final Model model) {
        model.addAttribute("form", new Form());
        return VIEW_SETTINGS_FORM;
    }

    /**
     * Renders form for editing an existing setting.
     *
     * @param id setting id
     * @param model view model
     * @return settings form page
     */
    @GetMapping("/edit/{id}")
    public String editForm(
            @PathVariable("id") final Long id,
            final Model model
    ) {
        final AppSettingEntity entity = service.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Config nao encontrada")
        );
        model.addAttribute("form", Form.from(entity));
        return VIEW_SETTINGS_FORM;
    }

    /**
     * Persists a new setting.
     *
     * @param form form payload
     * @param ra redirect attributes
     * @return redirect to settings listing
     */
    @PostMapping
    public String create(
            @ModelAttribute("form") @Validated final Form form,
            final RedirectAttributes ra
    ) {
        service.create(
                form.getSettingKey().trim(),
                nullSafe(form.getSettingValue()),
                nullSafe(form.getDescription())
        );
        ra.addFlashAttribute(SUCCESS_ATTR, "Configuracao criada com sucesso.");
        return REDIRECT_SETTINGS;
    }

    /**
     * Updates an existing setting.
     *
     * @param id setting id
     * @param form form payload
     * @param ra redirect attributes
     * @return redirect to settings listing
     */
    @PostMapping("/{id}")
    public String update(
            @PathVariable("id") final Long id,
            @ModelAttribute("form") @Validated final Form form,
            final RedirectAttributes ra
    ) {
        service.update(
                id,
                form.getSettingKey().trim(),
                nullSafe(form.getSettingValue()),
                nullSafe(form.getDescription())
        );
        ra.addFlashAttribute(SUCCESS_ATTR, "Configuracao atualizada.");
        return REDIRECT_SETTINGS;
    }

    /**
     * Deletes a setting.
     *
     * @param id setting id
     * @param ra redirect attributes
     * @return redirect to settings listing
     */
    @PostMapping("/delete/{id}")
    public String delete(
            @PathVariable("id") final Long id,
            final RedirectAttributes ra
    ) {
        service.delete(id);
        ra.addFlashAttribute(SUCCESS_ATTR, "Configuracao removida.");
        return REDIRECT_SETTINGS;
    }

    /**
     * Returns empty string for null values.
     *
     * @param value source value
     * @return null-safe string
     */
    private static String nullSafe(final String value) {
        return value == null ? "" : value;
    }

    /**
     * Form payload for app setting create/update.
     */
    @Getter
    @Setter
    public static final class Form {

        /**
         * Entity id.
         */
        private Long id;

        /**
         * Unique key of setting.
         */
        @NotBlank(message = "Chave e obrigatoria")
        private String settingKey;

        /**
         * Setting value.
         */
        private String settingValue;

        /**
         * Human-readable description.
         */
        private String description;

        /**
         * Creates form from persisted entity.
         *
         * @param entity persisted setting
         * @return populated form
         */
        public static Form from(final AppSettingEntity entity) {
            final Form f = new Form();
            f.setId(entity.getId());
            f.setSettingKey(entity.getSettingKey());
            f.setSettingValue(entity.getSettingValue());
            f.setDescription(entity.getDescription());
            return f;
        }
    }
}
