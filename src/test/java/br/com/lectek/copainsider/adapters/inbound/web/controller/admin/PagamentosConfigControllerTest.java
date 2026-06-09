package br.com.lectek.copainsider.adapters.inbound.web.controller.admin;

import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import br.com.lectek.copainsider.domain.financeiro.mercadopago.MercadoPagoCheckoutService;
import br.com.lectek.copainsider.domain.financeiro.mercadopago.MercadoPagoOAuthService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpSession;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagamentosConfigControllerTest {

    @Mock
    private AppSettingService settings;

    @Mock
    private MercadoPagoOAuthService mercadoPagoOAuthService;

    @Mock
    private MercadoPagoCheckoutService mercadoPagoCheckoutService;

    private ObjectMapper objectMapper;
    private PagamentosConfigController controller;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        controller = new PagamentosConfigController(
                settings,
                objectMapper,
                mercadoPagoOAuthService,
                mercadoPagoCheckoutService
        );

        lenient().when(settings.getOrDefault(anyString(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        lenient().when(settings.getBoolean(anyString(), anyBoolean()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        lenient().when(settings.getInt(anyString(), anyInt()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        lenient().when(settings.getDecimal(anyString(), any(BigDecimal.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));
        lenient().when(mercadoPagoOAuthService.isReady()).thenReturn(false);
        lenient().when(mercadoPagoOAuthService.listConnections())
                .thenReturn(List.of());
        lenient().when(mercadoPagoOAuthService.getWebhookReceipt(anyString()))
                .thenReturn(MercadoPagoOAuthService.WebhookReceiptView.empty());
        lenient().when(mercadoPagoCheckoutService.hasNotificationUrlConfigured())
                .thenReturn(false);
        lenient().when(mercadoPagoCheckoutService.getNotificationUrlPreview())
                .thenReturn("");
        lenient().when(mercadoPagoCheckoutService.getOauthRedirectUrlPreview())
                .thenReturn("https://api.exemplo.com/admin/configuracoes/pagamentos/mercadopago/oauth/callback");
        lenient().when(mercadoPagoCheckoutService.getAppBaseUrlPreview())
                .thenReturn("https://api.exemplo.com");
    }

    @Test
    void editarPopulatesModelUsingStoredSettings() {
        when(settings.getOrDefault("pg.gateway", "mercadopago")).thenReturn("pagarme");
        when(settings.getBoolean("pg.pix_ativo", true)).thenReturn(false);
        when(settings.getBoolean("pg.boleto_ativo", false)).thenReturn(true);
        when(settings.getDecimal("pg.taxa_cartao", BigDecimal.ZERO)).thenReturn(new BigDecimal("2.49"));
        when(settings.getInt("pg.max_parcelas", 6)).thenReturn(10);
        when(settings.getDecimal("pg.parcela_min", BigDecimal.valueOf(20L))).thenReturn(new BigDecimal("35.00"));
        when(settings.getOrDefault("pg.webhook_url", "")).thenReturn("https://api.exemplo.com/webhook");
        when(settings.getOrDefault("pg.mp.client_id", "")).thenReturn("client-id");
        when(settings.getOrDefault("pg.mp.redirect_uri", ""))
                .thenReturn("https://api.exemplo.com/mp/callback");
        when(settings.getOrDefault("pg.mp.owner_reference_default", ""))
                .thenReturn("loja-centro");
        when(settings.getOrDefault("pg.mp.owner_reference_by_tenant", ""))
                .thenReturn("tenant-a=loja-centro");
        when(settings.getOrDefault("pg.custom_methods", "[]")).thenReturn(
                "[{\"id\":\"m1\",\"nome\":\"Convenio\",\"tipo\":\"voucher\",\"taxa\":1.5,\"ativo\":true}]"
        );
        when(mercadoPagoOAuthService.isReady()).thenReturn(true);
        when(mercadoPagoOAuthService.hasActiveConnection("loja-centro"))
                .thenReturn(true);
        when(mercadoPagoOAuthService.getWebhookReceipt("loja-centro"))
                .thenReturn(new MercadoPagoOAuthService.WebhookReceiptView(
                        true,
                        "18/03/2026 10:06",
                        "pay-456"
                ));
        when(mercadoPagoCheckoutService.hasNotificationUrlConfigured())
                .thenReturn(true);
        when(mercadoPagoCheckoutService.getNotificationUrlPreview())
                .thenReturn("https://api.exemplo.com/webhook");

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.editar(model);

        Assertions.assertThat(view).isEqualTo("pages/admin/configuracoes/pagamentos");
        PagamentosConfigController.PagamentosForm cfg =
                (PagamentosConfigController.PagamentosForm) model.get("cfg");
        Assertions.assertThat(cfg).isNotNull();
        Assertions.assertThat(cfg.getGateway()).isEqualTo("pagarme");
        Assertions.assertThat(cfg.getPixAtivo()).isFalse();
        Assertions.assertThat(cfg.getBoletoAtivo()).isTrue();
        Assertions.assertThat(cfg.getTaxacartao()).isEqualByComparingTo("2.49");
        Assertions.assertThat(cfg.getMaxParcelas()).isEqualTo(10);
        Assertions.assertThat(cfg.getParcelaMin()).isEqualByComparingTo("35.00");
        Assertions.assertThat(cfg.getWebhookUrl()).isEqualTo("https://api.exemplo.com/webhook");
        Assertions.assertThat(cfg.getMercadoPagoClientId()).isEqualTo("client-id");
        Assertions.assertThat(cfg.getMercadoPagoRedirectUri())
                .isEqualTo("https://api.exemplo.com/mp/callback");
        Assertions.assertThat(cfg.getMercadoPagoOwnerReferenceDefault())
                .isEqualTo("loja-centro");
        Assertions.assertThat(cfg.getMercadoPagoOwnerReferenceByTenant())
                .isEqualTo("tenant-a=loja-centro");
        Assertions.assertThat(model.get("webhookConfigured")).isEqualTo(true);
        Assertions.assertThat(model.get("mercadoPagoOauthConfigured"))
                .isEqualTo(true);
        Assertions.assertThat(model.get("mercadoPagoNotificationUrlPreview"))
                .isEqualTo("https://api.exemplo.com/webhook");
        PagamentosConfigController.MercadoPagoOperationalStatus status =
                (PagamentosConfigController.MercadoPagoOperationalStatus)
                        model.get("mercadoPagoOperationalStatus");
        Assertions.assertThat(status).isNotNull();
        Assertions.assertThat(status.gatewaySelected()).isFalse();
        Assertions.assertThat(status.oauthConfigured()).isTrue();
        Assertions.assertThat(status.webhookConfigured()).isTrue();
        Assertions.assertThat(status.ownerReferenceConnected()).isTrue();
        Assertions.assertThat(status.webhookReceived()).isTrue();
        Assertions.assertThat(status.lastWebhookReceivedAtLabel())
                .isEqualTo("18/03/2026 10:06");
        Assertions.assertThat(status.endToEndValidated()).isFalse();
        Assertions.assertThat(model.get("novoMetodo"))
                .isInstanceOf(PagamentosConfigController.MetodoForm.class);
        List<?> customMethods = (List<?>) model.get("customMethods");
        Assertions.assertThat(customMethods).hasSize(1);
        PagamentosConfigController.MetodoPagamento metodo =
                (PagamentosConfigController.MetodoPagamento) customMethods.get(0);
        Assertions.assertThat(metodo.getNome()).isEqualTo("Convenio");
    }

    @Test
    void editarNormalizesLegacyMercadoPagoGatewayAlias() {
        when(settings.getOrDefault("pg.gateway", "mercadopago"))
                .thenReturn("mercado_pago");
        when(mercadoPagoCheckoutService.hasNotificationUrlConfigured())
                .thenReturn(true);

        ExtendedModelMap model = new ExtendedModelMap();
        controller.editar(model);

        PagamentosConfigController.PagamentosForm cfg =
                (PagamentosConfigController.PagamentosForm) model.get("cfg");
        Assertions.assertThat(cfg).isNotNull();
        Assertions.assertThat(cfg.getGateway()).isEqualTo("mercadopago");
    }

    @Test
    void editarReturnsEmptyCustomMethodsWhenJsonIsInvalid() {
        when(settings.getOrDefault("pg.custom_methods", "[]")).thenReturn("{json-invalido}");

        ExtendedModelMap model = new ExtendedModelMap();
        controller.editar(model);

        List<?> customMethods = (List<?>) model.get("customMethods");
        Assertions.assertThat(customMethods).isEmpty();
    }

    @Test
    void assistenteMercadoPagoPopulatesGuideModelWithPreviewUrls() {
        when(settings.getOrDefault("pg.mp.client_id", "")).thenReturn("client-id");
        when(settings.getOrDefault("pg.mp.owner_reference_default", ""))
                .thenReturn("loja-centro");
        when(mercadoPagoOAuthService.hasActiveConnection("loja-centro"))
                .thenReturn(true);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.assistenteMercadoPago(model);

        Assertions.assertThat(view)
                .isEqualTo("pages/admin/configuracoes/mercadopago-assistente");
        PagamentosConfigController.MercadoPagoAssistenteForm assistente =
                (PagamentosConfigController.MercadoPagoAssistenteForm)
                        model.get("assistente");
        Assertions.assertThat(assistente).isNotNull();
        Assertions.assertThat(assistente.getClientId()).isEqualTo("client-id");
        Assertions.assertThat(assistente.getRedirectUri())
                .isEqualTo("https://api.exemplo.com/admin/configuracoes/pagamentos/mercadopago/oauth/callback");
        Assertions.assertThat(assistente.getOwnerReferenceToConnect())
                .isEqualTo("loja-centro");
        Assertions.assertThat(model.get("mercadoPagoPublicGuidePath"))
                .isEqualTo("/mercadopago/guia");
        Assertions.assertThat(model.get("mercadoPagoBaseUrlPreview"))
                .isEqualTo("https://api.exemplo.com");
        Assertions.assertThat(model.get("mercadoPagoRedirectUrlPreview"))
                .isEqualTo("https://api.exemplo.com/admin/configuracoes/pagamentos/mercadopago/oauth/callback");
    }

    @Test
    void salvarPersistsAllPaymentSettings() {
        PagamentosConfigController.PagamentosForm cfg =
                new PagamentosConfigController.PagamentosForm();
        cfg.setGateway("  pagarme  ");
        cfg.setPixAtivo(true);
        cfg.setCartaoAtivo(false);
        cfg.setBoletoAtivo(true);
        cfg.setDinheiroAtivo(null);
        cfg.setTaxacartao(new BigDecimal("2.35"));
        cfg.setMaxParcelas(12);
        cfg.setParcelaMin(new BigDecimal("30.50"));
        cfg.setPublicKey(" pub-key ");
        cfg.setSecretKey(null);
        cfg.setWebhookUrl("https://hooks.exemplo.com");
        cfg.setMercadoPagoClientId("mp-client-id");
        cfg.setMercadoPagoClientSecret("mp-client-secret");
        cfg.setMercadoPagoRedirectUri("https://api.exemplo.com/mp/callback");
        cfg.setMercadoPagoOwnerReferenceDefault("loja-centro");
        cfg.setMercadoPagoOwnerReferenceByTenant("tenant-a=loja-centro");
        cfg.setPixChave("pix-chave");
        cfg.setPixTipo("cpf");
        cfg.setDinheiroMinimo(new BigDecimal("15.00"));
        cfg.setDinheiroTrocoMax(BigDecimal.ZERO);
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

        String redirect = controller.salvar(cfg, attrs);

        Assertions.assertThat(redirect).isEqualTo("redirect:/admin/configuracoes/pagamentos");
        Assertions.assertThat(attrs.getFlashAttributes()).containsKey("success");
        Assertions.assertThat(attrs.getFlashAttributes().get("success"))
                .isEqualTo("Configuracoes de pagamento atualizadas.");
        verify(settings, times(20)).upsert(anyString(), anyString(), anyString());
        verify(settings).upsert("pg.gateway", "pagarme", "Gateway ativo");
        verify(settings).upsert("pg.pix_ativo", "true", "PIX ativo");
        verify(settings).upsert("pg.cartao_ativo", "false", "Cartao ativo");
        verify(settings).upsert("pg.dinheiro_ativo", "false", "Dinheiro ativo");
        verify(settings).upsert("pg.max_parcelas", "12", "Maximo de parcelas");
        verify(settings).upsert("pg.taxa_cartao", "2.35", "Taxa de cartao");
        verify(settings).upsert("pg.secret_key", "", "Secret key gateway");
        verify(settings).upsert("pg.mp.client_id", "mp-client-id", "Mercado Pago OAuth client id");
        verify(settings).upsert("pg.mp.client_secret", "mp-client-secret", "Mercado Pago OAuth client secret");
        verify(settings).upsert("pg.mp.redirect_uri", "https://api.exemplo.com/mp/callback", "Mercado Pago OAuth redirect uri");
        verify(settings).upsert("pg.mp.owner_reference_default", "loja-centro", "Mercado Pago owner reference padrao");
        verify(settings).upsert("pg.mp.owner_reference_by_tenant", "tenant-a=loja-centro", "Mercado Pago owner reference por tenant");
        verify(settings).upsert("pg.dinheiro_min", "15.00", "Valor minimo para dinheiro");
    }

    @Test
    void salvarCanonicalizesMercadoPagoAliasBeforePersisting() {
        PagamentosConfigController.PagamentosForm cfg =
                new PagamentosConfigController.PagamentosForm();
        cfg.setGateway(" Mercado_Pago ");
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

        controller.salvar(cfg, attrs);

        verify(settings).upsert("pg.gateway", "mercadopago", "Gateway ativo");
    }

    @Test
    void salvarAssistenteMercadoPagoPersistsMercadoPagoConfigAndForcesGateway() {
        PagamentosConfigController.MercadoPagoAssistenteForm form =
                new PagamentosConfigController.MercadoPagoAssistenteForm();
        form.setClientId("client-id");
        form.setClientSecret("client-secret");
        form.setRedirectUri("");
        form.setWebhookUrl("");
        form.setOwnerReferenceDefault("loja-centro");
        form.setOwnerReferenceByTenant("tenant-a=loja-centro");
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

        String redirect = controller.salvarAssistenteMercadoPago(form, attrs);

        Assertions.assertThat(redirect)
                .isEqualTo("redirect:/admin/configuracoes/pagamentos/mercadopago/assistente");
        Assertions.assertThat(attrs.getFlashAttributes().get("success"))
                .isEqualTo("Assistente Mercado Pago salvo. Agora voce ja pode conectar a conta.");
        verify(settings).upsert("pg.gateway", "mercadopago", "Gateway ativo");
        verify(settings).upsert("pg.mp.client_id", "client-id", "Mercado Pago OAuth client id");
        verify(settings).upsert("pg.mp.client_secret", "client-secret", "Mercado Pago OAuth client secret");
        verify(settings).upsert(
                "pg.mp.redirect_uri",
                "https://api.exemplo.com/admin/configuracoes/pagamentos/mercadopago/oauth/callback",
                "Mercado Pago OAuth redirect uri"
        );
    }

    @Test
    void salvarEConectarAssistenteMercadoPagoPersistsAndRedirectsToOauth() {
        PagamentosConfigController.MercadoPagoAssistenteForm form =
                new PagamentosConfigController.MercadoPagoAssistenteForm();
        form.setClientId("client-id");
        form.setClientSecret("client-secret");
        form.setOwnerReferenceDefault("loja-centro");
        form.setOwnerReferenceToConnect("loja-bairro");
        MockHttpSession session = new MockHttpSession();
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();
        when(mercadoPagoOAuthService.buildAuthorizationUrl("loja-bairro", session))
                .thenReturn("https://auth.mercadopago.com/authorization?state=abc");

        String redirect = controller.salvarEConectarAssistenteMercadoPago(
                form,
                session,
                attrs
        );

        Assertions.assertThat(redirect)
                .isEqualTo("redirect:https://auth.mercadopago.com/authorization?state=abc");
        Assertions.assertThat(session.getAttribute("mercadopago.oauth.return_path"))
                .isEqualTo("redirect:/admin/configuracoes/pagamentos/mercadopago/assistente");
        verify(settings).upsert("pg.gateway", "mercadopago", "Gateway ativo");
    }

    @Test
    void callbackMercadoPagoReturnsToAssistantWhenFlowStartedFromAssistant() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(
                "mercadopago.oauth.return_path",
                "redirect:/admin/configuracoes/pagamentos/mercadopago/assistente"
        );
        when(mercadoPagoOAuthService.handleCallback(
                "oauth-code",
                "state-123",
                null,
                null,
                session
        )).thenReturn(new MercadoPagoOAuthService.CallbackResult(
                "loja-centro",
                "123456789",
                "18/03/2026 12:00"
        ));
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

        String redirect = controller.callbackMercadoPago(
                "oauth-code",
                "state-123",
                null,
                null,
                session,
                attrs
        );

        Assertions.assertThat(redirect)
                .isEqualTo("redirect:/admin/configuracoes/pagamentos/mercadopago/assistente");
        Assertions.assertThat(session.getAttribute("mercadopago.oauth.return_path"))
                .isNull();
    }

    @Test
    void testarConexaoMercadoPagoAddsSmokeTestToFlashAttributes() {
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();
        MercadoPagoCheckoutService.SmokeTestResult result =
                new MercadoPagoCheckoutService.SmokeTestResult(
                        "loja-centro",
                        "123456789",
                        true,
                        "pref-123",
                        "https://mercadopago.com/pay",
                        "https://api.exemplo.com/webhooks/mercadopago"
                );
        when(mercadoPagoCheckoutService.runCheckoutSmokeTest("loja-centro"))
                .thenReturn(result);

        String redirect = controller.testarConexaoMercadoPago(
                "loja-centro",
                attrs
        );

        Assertions.assertThat(redirect)
                .isEqualTo("redirect:/admin/configuracoes/pagamentos");
        Assertions.assertThat(attrs.getFlashAttributes().get("success"))
                .isEqualTo("Teste de checkout Mercado Pago concluido com sucesso.");
        Assertions.assertThat(attrs.getFlashAttributes().get("mercadoPagoSmokeTest"))
                .isEqualTo(result);
    }

    @Test
    void testarConexaoMercadoPagoReturnsErrorWhenSmokeTestFails() {
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();
        when(mercadoPagoCheckoutService.runCheckoutSmokeTest("loja-centro"))
                .thenThrow(new IllegalStateException("Falha ao criar a preferencia."));

        String redirect = controller.testarConexaoMercadoPago(
                "loja-centro",
                attrs
        );

        Assertions.assertThat(redirect)
                .isEqualTo("redirect:/admin/configuracoes/pagamentos");
        Assertions.assertThat(attrs.getFlashAttributes().get("error"))
                .isEqualTo("Falha ao criar a preferencia.");
    }

    @Test
    void adicionarMetodoReturnsErrorWhenBindingHasErrors() {
        PagamentosConfigController.MetodoForm form =
                new PagamentosConfigController.MetodoForm();
        form.setNome("");
        BindingResult br = org.mockito.Mockito.mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(true);
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

        String redirect = controller.adicionarMetodo(form, br, attrs);

        Assertions.assertThat(redirect).isEqualTo("redirect:/admin/configuracoes/pagamentos");
        Assertions.assertThat(attrs.getFlashAttributes()).containsKey("error");
        Assertions.assertThat(attrs.getFlashAttributes().get("error"))
                .isEqualTo("Preencha o nome do metodo.");
        verify(settings, never()).upsert(anyString(), anyString(), anyString());
    }

    @Test
    void adicionarMetodoReturnsErrorWhenMethodAlreadyExists() {
        when(settings.getOrDefault("pg.custom_methods", "[]")).thenReturn(
                "[{\"id\":\"m1\",\"nome\":\"Pix parcelado\",\"tipo\":\"pix\",\"taxa\":0,\"ativo\":true}]"
        );
        PagamentosConfigController.MetodoForm form =
                new PagamentosConfigController.MetodoForm();
        form.setNome("  pix PARCELADO ");
        BindingResult br = org.mockito.Mockito.mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

        String redirect = controller.adicionarMetodo(form, br, attrs);

        Assertions.assertThat(redirect).isEqualTo("redirect:/admin/configuracoes/pagamentos");
        Assertions.assertThat(attrs.getFlashAttributes()).containsKey("error");
        Assertions.assertThat(attrs.getFlashAttributes().get("error"))
                .isEqualTo("Metodo ja existe.");
        verify(settings, never()).upsert(anyString(), anyString(), anyString());
    }

    @Test
    void adicionarMetodoPersistsNewMethodWithDefaultAtivo() throws Exception {
        PagamentosConfigController.MetodoForm form =
                new PagamentosConfigController.MetodoForm();
        form.setNome("  Carteira Digital ");
        form.setTipo(null);
        form.setTaxa(new BigDecimal("1.99"));
        form.setAtivo(null);
        BindingResult br = org.mockito.Mockito.mock(BindingResult.class);
        when(br.hasErrors()).thenReturn(false);
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

        String redirect = controller.adicionarMetodo(form, br, attrs);

        Assertions.assertThat(redirect).isEqualTo("redirect:/admin/configuracoes/pagamentos");
        Assertions.assertThat(attrs.getFlashAttributes()).containsKey("success");
        Assertions.assertThat(attrs.getFlashAttributes().get("success"))
                .isEqualTo("Metodo adicionado.");
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(settings).upsert(
                org.mockito.Mockito.eq("pg.custom_methods"),
                jsonCaptor.capture(),
                org.mockito.Mockito.eq("Metodos de pagamento personalizados")
        );
        List<PagamentosConfigController.MetodoPagamento> saved = objectMapper.readValue(
                jsonCaptor.getValue(),
                new TypeReference<List<PagamentosConfigController.MetodoPagamento>>() {
                }
        );
        Assertions.assertThat(saved).hasSize(1);
        PagamentosConfigController.MetodoPagamento method = saved.get(0);
        Assertions.assertThat(method.getId()).isNotBlank();
        Assertions.assertThat(method.getNome()).isEqualTo("Carteira Digital");
        Assertions.assertThat(method.getTipo()).isEmpty();
        Assertions.assertThat(method.getTaxa()).isEqualByComparingTo("1.99");
        Assertions.assertThat(method.isAtivo()).isTrue();
    }

    @Test
    void removerMetodoRemovesMatchingIdAndPersistsRemainingOnes() throws Exception {
        when(settings.getOrDefault("pg.custom_methods", "[]")).thenReturn(
                "[{\"id\":\"m1\",\"nome\":\"PIX\",\"tipo\":\"pix\",\"taxa\":0,\"ativo\":true},"
                        + "{\"id\":\"m2\",\"nome\":\"Dinheiro\",\"tipo\":\"cash\",\"taxa\":0,\"ativo\":true}]"
        );
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

        String redirect = controller.removerMetodo("m1", attrs);

        Assertions.assertThat(redirect).isEqualTo("redirect:/admin/configuracoes/pagamentos");
        Assertions.assertThat(attrs.getFlashAttributes()).containsKey("success");
        Assertions.assertThat(attrs.getFlashAttributes().get("success"))
                .isEqualTo("Metodo removido.");
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(settings).upsert(
                org.mockito.Mockito.eq("pg.custom_methods"),
                jsonCaptor.capture(),
                org.mockito.Mockito.eq("Metodos de pagamento personalizados")
        );
        List<PagamentosConfigController.MetodoPagamento> saved = objectMapper.readValue(
                jsonCaptor.getValue(),
                new TypeReference<List<PagamentosConfigController.MetodoPagamento>>() {
                }
        );
        Assertions.assertThat(saved).hasSize(1);
        Assertions.assertThat(saved.get(0).getId()).isEqualTo("m2");
    }
}
