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
@RequestMapping("/admin/configuracoes/integracoes")
@PreAuthorize("hasRole('ADMIN')")
public class AdminConfiguracoesIntegracoesController {

    /**
     * Settings key for API key.
     */
    private static final String KEY_API_KEY = "integracoes.api_key";

    /**
     * Settings key for API secret.
     */
    private static final String KEY_API_SECRET = "integracoes.api_secret";

    /**
     * Settings key for webhook URL.
     */
    private static final String KEY_WEBHOOK_URL = "integracoes.webhook_url";

    /**
     * Service that manages settings storage.
     */
    private final AppSettingService settings;

    /**
     * Creates controller with settings service dependency.
     *
     * @param appSettingService settings service
     */
    public AdminConfiguracoesIntegracoesController(
            final AppSettingService appSettingService
    ) {
        this.settings = appSettingService;
    }

    /**
     * Renders integrations settings form.
     *
     * @param model view model
     * @return integrations settings view
     */
    @GetMapping
    public String form(final Model model) {
        final IntegracoesForm form = new IntegracoesForm();
        form.setApiKey(settings.getOrDefault(KEY_API_KEY, ""));
        form.setApiSecret(settings.getOrDefault(KEY_API_SECRET, ""));
        form.setWebhookUrl(settings.getOrDefault(KEY_WEBHOOK_URL, ""));
        model.addAttribute("cfg", form);
        model.addAttribute(
                "webhookConfigured",
                form.getWebhookUrl() != null
                        && !form.getWebhookUrl().isBlank()
        );
        return "pages/admin/configuracoes/integracoes";
    }

    /**
     * Persists integrations settings.
     *
     * @param form form payload
     * @param ra redirect attributes
     * @return redirect to integrations settings
     */
    @PostMapping
    public String salvar(
            @ModelAttribute("cfg") final IntegracoesForm form,
            final RedirectAttributes ra
    ) {
        settings.upsert(
                KEY_API_KEY,
                nullSafe(form.getApiKey()),
                "API key integracoes"
        );
        settings.upsert(
                KEY_API_SECRET,
                nullSafe(form.getApiSecret()),
                "API secret integracoes"
        );
        settings.upsert(
                KEY_WEBHOOK_URL,
                nullSafe(form.getWebhookUrl()),
                "Webhook integracoes"
        );
        ra.addFlashAttribute("success", "Integracoes atualizadas.");
        return "redirect:/admin/configuracoes/integracoes";
    }

    /**
     * Returns a trimmed value or empty string when null.
     *
     * @param value source value
     * @return normalized value
     */
    private static String nullSafe(final String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Form payload for integrations settings.
     */
    @Getter
    @Setter
    public static final class IntegracoesForm {

        /**
         * API key value.
         */
        private String apiKey;

        /**
         * API secret value.
         */
        private String apiSecret;

        /**
         * Webhook URL value.
         */
        private String webhookUrl;
    }
}
