package br.com.lectek.copainsider.adapters.inbound.web.controller.site;

import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import br.com.lectek.copainsider.domain.financeiro.mercadopago.MercadoPagoCheckoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MercadoPagoConnectionGuideControllerTest {

    private AppSettingService settings;
    private MercadoPagoCheckoutService mercadoPagoCheckoutService;
    private MercadoPagoConnectionGuideController controller;

    @BeforeEach
    void setUp() {
        settings = mock(AppSettingService.class);
        mercadoPagoCheckoutService = mock(MercadoPagoCheckoutService.class);
        controller = new MercadoPagoConnectionGuideController(
                settings,
                mercadoPagoCheckoutService
        );
    }

    @Test
    void guiaUsesStoredRedirectUriWhenAvailable() {
        when(settings.getOrDefault("pg.mp.redirect_uri", ""))
                .thenReturn("https://app.exemplo.com/admin/configuracoes/pagamentos/mercadopago/oauth/callback");
        when(mercadoPagoCheckoutService.getNotificationUrlPreview())
                .thenReturn("https://app.exemplo.com/webhooks/mercadopago");
        when(mercadoPagoCheckoutService.getAppBaseUrlPreview())
                .thenReturn("https://app.exemplo.com");

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.guia(model);

        assertThat(view).isEqualTo("pages/site/mercadopago-guia");
        assertThat(model.getAttribute("mercadoPagoRedirectUriPreview"))
                .isEqualTo("https://app.exemplo.com/admin/configuracoes/pagamentos/mercadopago/oauth/callback");
        assertThat(model.getAttribute("mercadoPagoNotificationUrlPreview"))
                .isEqualTo("https://app.exemplo.com/webhooks/mercadopago");
        assertThat(model.getAttribute("mercadoPagoAdminAssistantLoginPath"))
                .isEqualTo("/auth/login?redirect=/admin/configuracoes/pagamentos/mercadopago/assistente");
    }

    @Test
    void guiaFallsBackToPreviewWhenRedirectUriIsNotSaved() {
        when(settings.getOrDefault("pg.mp.redirect_uri", "")).thenReturn("");
        when(mercadoPagoCheckoutService.getOauthRedirectUrlPreview())
                .thenReturn("https://preview.exemplo.com/admin/configuracoes/pagamentos/mercadopago/oauth/callback");
        when(mercadoPagoCheckoutService.getNotificationUrlPreview())
                .thenReturn("");
        when(mercadoPagoCheckoutService.getAppBaseUrlPreview())
                .thenReturn("https://preview.exemplo.com");

        ExtendedModelMap model = new ExtendedModelMap();
        controller.guia(model);

        assertThat(model.getAttribute("mercadoPagoRedirectUriPreview"))
                .isEqualTo("https://preview.exemplo.com/admin/configuracoes/pagamentos/mercadopago/oauth/callback");
        assertThat(model.getAttribute("mercadoPagoBaseUrlPreview"))
                .isEqualTo("https://preview.exemplo.com");
    }
}
