package br.com.redemaisfarma.adapters.inbound.web.controller.site;

import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.domain.financeiro.mercadopago.MercadoPagoCheckoutService;
import br.com.redemaisfarma.domain.financeiro.mercadopago.MercadoPagoOAuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MercadoPagoConnectionGuideController {

    private static final String ADMIN_ASSISTANT_PATH =
            "/admin/configuracoes/pagamentos/mercadopago/assistente";

    private final AppSettingService settings;
    private final MercadoPagoCheckoutService mercadoPagoCheckoutService;

    public MercadoPagoConnectionGuideController(
            final AppSettingService settingsValue,
            final MercadoPagoCheckoutService mercadoPagoCheckoutServiceValue
    ) {
        this.settings = settingsValue;
        this.mercadoPagoCheckoutService = mercadoPagoCheckoutServiceValue;
    }

    @GetMapping("/mercadopago/guia")
    public String guia(final Model model) {
        final String configuredRedirectUri = text(
                settings.getOrDefault(MercadoPagoOAuthService.KEY_REDIRECT_URI, "")
        );
        model.addAttribute(
                "mercadoPagoRedirectUriPreview",
                configuredRedirectUri.isBlank()
                        ? mercadoPagoCheckoutService.getOauthRedirectUrlPreview()
                        : configuredRedirectUri
        );
        model.addAttribute(
                "mercadoPagoNotificationUrlPreview",
                mercadoPagoCheckoutService.getNotificationUrlPreview()
        );
        model.addAttribute(
                "mercadoPagoBaseUrlPreview",
                mercadoPagoCheckoutService.getAppBaseUrlPreview()
        );
        model.addAttribute(
                "mercadoPagoAdminAssistantPath",
                ADMIN_ASSISTANT_PATH
        );
        model.addAttribute(
                "mercadoPagoAdminAssistantLoginPath",
                "/auth/login?redirect=" + ADMIN_ASSISTANT_PATH
        );
        return "pages/site/mercadopago-guia";
    }

    private String text(final String value) {
        return value == null ? "" : value.trim();
    }
}
