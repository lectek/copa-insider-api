package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.domain.financeiro.mercadopago.MercadoPagoCheckoutService;
import br.com.redemaisfarma.domain.financeiro.mercadopago.MercadoPagoOAuthService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/configuracoes/pagamentos")
@PreAuthorize("hasRole('ADMIN')")
@Validated
public class PagamentosConfigController {

    /**
     * Settings key for active gateway.
     */
    private static final String KEY_GATEWAY = "pg.gateway";

    /**
     * Settings key for PIX enablement.
     */
    private static final String KEY_PIX_ATIVO = "pg.pix_ativo";

    /**
     * Settings key for card enablement.
     */
    private static final String KEY_CARTAO_ATIVO = "pg.cartao_ativo";

    /**
     * Settings key for boleto enablement.
     */
    private static final String KEY_BOLETO_ATIVO = "pg.boleto_ativo";

    /**
     * Settings key for cash enablement.
     */
    private static final String KEY_DINHEIRO_ATIVO = "pg.dinheiro_ativo";

    /**
     * Settings key for card fee.
     */
    private static final String KEY_TAXA_CARTAO = "pg.taxa_cartao";

    /**
     * Settings key for max installments.
     */
    private static final String KEY_MAX_PARCELAS = "pg.max_parcelas";

    /**
     * Settings key for minimum installment amount.
     */
    private static final String KEY_PARCELA_MIN = "pg.parcela_min";

    /**
     * Settings key for gateway public key.
     */
    private static final String KEY_PUBLIC_KEY = "pg.public_key";

    /**
     * Settings key for gateway secret key.
     */
    private static final String KEY_SECRET_KEY = "pg.secret_key";

    /**
     * Settings key for gateway webhook URL.
     */
    private static final String KEY_WEBHOOK_URL = "pg.webhook_url";

    /**
     * Settings key for Mercado Pago OAuth client id.
     */
    private static final String KEY_MP_CLIENT_ID =
            MercadoPagoOAuthService.KEY_CLIENT_ID;

    /**
     * Settings key for Mercado Pago OAuth client secret.
     */
    private static final String KEY_MP_CLIENT_SECRET =
            MercadoPagoOAuthService.KEY_CLIENT_SECRET;

    /**
     * Settings key for Mercado Pago OAuth redirect uri.
     */
    private static final String KEY_MP_REDIRECT_URI =
            MercadoPagoOAuthService.KEY_REDIRECT_URI;

    /**
     * Settings key for default Mercado Pago owner reference.
     */
    private static final String KEY_MP_OWNER_REFERENCE_DEFAULT =
            MercadoPagoCheckoutService.KEY_OWNER_REFERENCE_DEFAULT;

    /**
     * Settings key for tenant-to-owner reference mapping.
     */
    private static final String KEY_MP_OWNER_REFERENCE_BY_TENANT =
            MercadoPagoCheckoutService.KEY_OWNER_REFERENCE_BY_TENANT;

    /**
     * Settings key for PIX key value.
     */
    private static final String KEY_PIX_CHAVE = "pg.pix_chave";

    /**
     * Settings key for PIX key type.
     */
    private static final String KEY_PIX_TIPO = "pg.pix_tipo";

    /**
     * Settings key for minimum cash order.
     */
    private static final String KEY_DINHEIRO_MIN = "pg.dinheiro_min";

    /**
     * Settings key for max cash change.
     */
    private static final String KEY_DINHEIRO_TROCO_MAX =
            "pg.dinheiro_troco_max";

    /**
     * Settings key for custom payment methods JSON.
     */
    private static final String KEY_CUSTOM_METHODS = "pg.custom_methods";

    /**
     * Redirect URL after any action.
     */
    private static final String REDIRECT_PAGAMENTOS =
            "redirect:/admin/configuracoes/pagamentos";

    /**
     * Redirect URL after Mercado Pago assistant actions.
     */
    private static final String REDIRECT_MERCADO_PAGO_ASSISTENTE =
            "redirect:/admin/configuracoes/pagamentos/mercadopago/assistente";

    /**
     * View path for payments settings page.
     */
    private static final String VIEW_PAGAMENTOS =
            "pages/admin/configuracoes/pagamentos";

    /**
     * View path for Mercado Pago guided connection page.
     */
    private static final String VIEW_MERCADO_PAGO_ASSISTENTE =
            "pages/admin/configuracoes/mercadopago-assistente";

    /**
     * Public guide route for Mercado Pago setup.
     */
    private static final String PUBLIC_MERCADO_PAGO_GUIDE_PATH =
            "/mercadopago/guia";

    /**
     * Route identifier used to return from OAuth callback to the assistant.
     */
    private static final String REDIRECT_TARGET_ASSISTENTE = "assistant";

    /**
     * Session attribute that stores the preferred post-callback return path.
     */
    private static final String SESSION_MP_RETURN_PATH =
            "mercadopago.oauth.return_path";

    /**
     * Default gateway name.
     */
    private static final String DEFAULT_GATEWAY = "mercadopago";

    /**
     * Default PIX type.
     */
    private static final String DEFAULT_PIX_TIPO = "aleatoria";

    /**
     * Default max installments.
     */
    private static final int DEFAULT_MAX_PARCELAS = 6;

    /**
     * Default minimum installment amount.
     */
    private static final BigDecimal DEFAULT_PARCELA_MIN =
            BigDecimal.valueOf(20L);

    /**
     * Service used to persist settings.
     */
    private final AppSettingService settings;

    /**
     * Object mapper for custom methods payload.
     */
    private final ObjectMapper objectMapper;

    /**
     * Mercado Pago OAuth orchestration service.
     */
    private final MercadoPagoOAuthService mercadoPagoOAuthService;

    /**
     * Mercado Pago checkout orchestration service.
     */
    private final MercadoPagoCheckoutService mercadoPagoCheckoutService;

    /**
     * Creates controller with dependencies.
     *
     * @param settingsService app settings service
     * @param objectMapperValue object mapper
     */
    public PagamentosConfigController(
            final AppSettingService settingsService,
            final ObjectMapper objectMapperValue,
            final MercadoPagoOAuthService mercadoPagoOAuthServiceValue,
            final MercadoPagoCheckoutService mercadoPagoCheckoutServiceValue
    ) {
        this.settings = settingsService;
        this.objectMapper = objectMapperValue;
        this.mercadoPagoOAuthService = mercadoPagoOAuthServiceValue;
        this.mercadoPagoCheckoutService = mercadoPagoCheckoutServiceValue;
    }

    /**
     * Renders payments configuration screen.
     *
     * @param model view model
     * @return settings view path
     */
    @GetMapping
    public String editar(final Model model) {
        final PagamentosForm cfg = loadPagamentosForm();
        populatePagamentosModel(model, cfg);
        return VIEW_PAGAMENTOS;
    }

    /**
     * Renders a guided assistant focused only on Mercado Pago connection.
     *
     * @param model view model
     * @return assistant view path
     */
    @GetMapping("/mercadopago/assistente")
    public String assistenteMercadoPago(final Model model) {
        final PagamentosForm cfg = loadPagamentosForm();
        populatePagamentosModel(model, cfg);
        model.addAttribute("assistente", buildMercadoPagoAssistenteForm(cfg));
        model.addAttribute(
                "mercadoPagoRedirectUrlPreview",
                resolveMercadoPagoRedirectUrlPreview(cfg)
        );
        model.addAttribute(
                "mercadoPagoBaseUrlPreview",
                mercadoPagoCheckoutService.getAppBaseUrlPreview()
        );
        model.addAttribute(
                "mercadoPagoPublicGuidePath",
                PUBLIC_MERCADO_PAGO_GUIDE_PATH
        );
        return VIEW_MERCADO_PAGO_ASSISTENTE;
    }

    /**
     * Saves main payments configuration.
     *
     * @param cfg form payload
     * @param ra redirect attributes
     * @return redirect URL
     */
    @PostMapping
    public String salvar(
            @ModelAttribute("cfg") final PagamentosForm cfg,
            final RedirectAttributes ra
    ) {
        saveText(KEY_GATEWAY, normalizeGateway(cfg.getGateway()), "Gateway ativo");
        saveBoolean(KEY_PIX_ATIVO, cfg.getPixAtivo(), "PIX ativo");
        saveBoolean(KEY_CARTAO_ATIVO, cfg.getCartaoAtivo(), "Cartao ativo");
        saveBoolean(KEY_BOLETO_ATIVO, cfg.getBoletoAtivo(), "Boleto ativo");
        saveBoolean(
                KEY_DINHEIRO_ATIVO,
                cfg.getDinheiroAtivo(),
                "Dinheiro ativo"
        );
        saveDecimal(KEY_TAXA_CARTAO, cfg.getTaxacartao(), "Taxa de cartao");
        saveInteger(
                KEY_MAX_PARCELAS,
                cfg.getMaxParcelas(),
                "Maximo de parcelas"
        );
        saveDecimal(KEY_PARCELA_MIN, cfg.getParcelaMin(), "Parcela minima");
        saveText(KEY_PUBLIC_KEY, cfg.getPublicKey(), "Public key gateway");
        saveText(KEY_SECRET_KEY, cfg.getSecretKey(), "Secret key gateway");
        saveText(KEY_WEBHOOK_URL, cfg.getWebhookUrl(), "Webhook gateway");
        saveText(
                KEY_MP_CLIENT_ID,
                cfg.getMercadoPagoClientId(),
                "Mercado Pago OAuth client id"
        );
        saveText(
                KEY_MP_CLIENT_SECRET,
                cfg.getMercadoPagoClientSecret(),
                "Mercado Pago OAuth client secret"
        );
        saveText(
                KEY_MP_REDIRECT_URI,
                cfg.getMercadoPagoRedirectUri(),
                "Mercado Pago OAuth redirect uri"
        );
        saveText(
                KEY_MP_OWNER_REFERENCE_DEFAULT,
                cfg.getMercadoPagoOwnerReferenceDefault(),
                "Mercado Pago owner reference padrao"
        );
        saveText(
                KEY_MP_OWNER_REFERENCE_BY_TENANT,
                cfg.getMercadoPagoOwnerReferenceByTenant(),
                "Mercado Pago owner reference por tenant"
        );
        saveText(KEY_PIX_CHAVE, cfg.getPixChave(), "Chave PIX");
        saveText(KEY_PIX_TIPO, cfg.getPixTipo(), "Tipo da chave PIX");
        saveDecimal(
                KEY_DINHEIRO_MIN,
                cfg.getDinheiroMinimo(),
                "Valor minimo para dinheiro"
        );
        saveDecimal(
                KEY_DINHEIRO_TROCO_MAX,
                cfg.getDinheiroTrocoMax(),
                "Troco maximo permitido"
        );
        ra.addFlashAttribute(
                "success",
                "Configuracoes de pagamento atualizadas."
        );
        return REDIRECT_PAGAMENTOS;
    }

    /**
     * Saves only the Mercado Pago assistant fields.
     *
     * @param form assistant payload
     * @param ra redirect attributes
     * @return redirect URL
     */
    @PostMapping("/mercadopago/assistente")
    public String salvarAssistenteMercadoPago(
            @ModelAttribute("assistente")
            final MercadoPagoAssistenteForm form,
            final RedirectAttributes ra
    ) {
        persistMercadoPagoAssistente(form);
        ra.addFlashAttribute(
                "success",
                "Assistente Mercado Pago salvo. Agora voce ja pode conectar a conta."
        );
        return REDIRECT_MERCADO_PAGO_ASSISTENTE;
    }

    /**
     * Saves the Mercado Pago assistant fields and immediately starts OAuth.
     *
     * @param form assistant payload
     * @param session http session used to track OAuth state
     * @param ra redirect attributes
     * @return redirect URL
     */
    @PostMapping("/mercadopago/assistente/conectar")
    public String salvarEConectarAssistenteMercadoPago(
            @ModelAttribute("assistente")
            final MercadoPagoAssistenteForm form,
            final HttpSession session,
            final RedirectAttributes ra
    ) {
        try {
            persistMercadoPagoAssistente(form);
            final String ownerReference = resolveOwnerReferenceToConnect(form);
            storeOauthReturnPath(session, REDIRECT_TARGET_ASSISTENTE);
            return "redirect:" + mercadoPagoOAuthService.buildAuthorizationUrl(
                    ownerReference,
                    session
            );
        } catch (IllegalArgumentException | IllegalStateException ex) {
            if (session != null) {
                session.removeAttribute(SESSION_MP_RETURN_PATH);
            }
            ra.addFlashAttribute("error", ex.getMessage());
            return REDIRECT_MERCADO_PAGO_ASSISTENTE;
        }
    }

    /**
     * Adds one custom payment method.
     *
     * @param form method form payload
     * @param br binding result
     * @param ra redirect attributes
     * @return redirect URL
     */
    @PostMapping("/metodos")
    public String adicionarMetodo(
            @ModelAttribute("novoMetodo") @Validated final MetodoForm form,
            final BindingResult br,
            final RedirectAttributes ra
    ) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("error", "Preencha o nome do metodo.");
            return REDIRECT_PAGAMENTOS;
        }

        final String nome = form.getNome().trim();
        final List<MetodoPagamento> methods = loadCustomMethods();
        final boolean exists = methods.stream().anyMatch(
                method -> method.getNome().equalsIgnoreCase(nome)
        );
        if (exists) {
            ra.addFlashAttribute("error", "Metodo ja existe.");
            return REDIRECT_PAGAMENTOS;
        }

        final MetodoPagamento novo = new MetodoPagamento(
                UUID.randomUUID().toString(),
                nome,
                nullSafe(form.getTipo()),
                form.getTaxa(),
                form.getAtivo() == null || form.getAtivo()
        );
        methods.add(novo);
        saveCustomMethods(methods);
        ra.addFlashAttribute("success", "Metodo adicionado.");
        return REDIRECT_PAGAMENTOS;
    }

    /**
     * Removes one custom payment method.
     *
     * @param id method id
     * @param ra redirect attributes
     * @return redirect URL
     */
    @PostMapping("/metodos/{id}/remover")
    public String removerMetodo(
            @PathVariable("id") final String id,
            final RedirectAttributes ra
    ) {
        final List<MetodoPagamento> methods = loadCustomMethods();
        methods.removeIf(method -> Objects.equals(method.getId(), id));
        saveCustomMethods(methods);
        ra.addFlashAttribute("success", "Metodo removido.");
        return REDIRECT_PAGAMENTOS;
    }

    /**
     * Starts Mercado Pago OAuth flow for one seller account.
     *
     * @param ownerReference internal store or tenant identifier
     * @param session http session used to track OAuth state
     * @param ra redirect attributes
     * @return redirect to Mercado Pago or back to settings page on error
     */
    @GetMapping("/mercadopago/oauth/conectar")
    public String conectarMercadoPago(
            @RequestParam("ownerReference") final String ownerReference,
            @RequestParam(name = "redirectTo", required = false)
            final String redirectTo,
            final HttpSession session,
            final RedirectAttributes ra
    ) {
        try {
            storeOauthReturnPath(session, redirectTo);
            return "redirect:" + mercadoPagoOAuthService.buildAuthorizationUrl(
                    ownerReference,
                    session
            );
        } catch (IllegalArgumentException | IllegalStateException ex) {
            if (session != null) {
                session.removeAttribute(SESSION_MP_RETURN_PATH);
            }
            ra.addFlashAttribute("error", ex.getMessage());
            return resolveMercadoPagoRedirectTarget(redirectTo);
        }
    }

    public String conectarMercadoPago(
            final String ownerReference,
            final HttpSession session,
            final RedirectAttributes ra
    ) {
        return conectarMercadoPago(ownerReference, "", session, ra);
    }

    /**
     * Handles Mercado Pago OAuth callback.
     *
     * @param code authorization code
     * @param state returned state
     * @param error provider error code
     * @param errorDescription provider error description
     * @param session http session used to validate OAuth state
     * @param ra redirect attributes
     * @return redirect to settings page
     */
    @GetMapping("/mercadopago/oauth/callback")
    public String callbackMercadoPago(
            @RequestParam(name = "code", required = false) final String code,
            @RequestParam(name = "state", required = false) final String state,
            @RequestParam(name = "error", required = false) final String error,
            @RequestParam(name = "error_description", required = false)
            final String errorDescription,
            final HttpSession session,
            final RedirectAttributes ra
    ) {
        final String redirectTarget = resolveAndClearOauthReturnPath(session);
        try {
            final MercadoPagoOAuthService.CallbackResult result =
                    mercadoPagoOAuthService.handleCallback(
                            code,
                            state,
                            error,
                            errorDescription,
                            session
                    );
            ra.addFlashAttribute(
                    "success",
                    "Conta Mercado Pago conectada para "
                            + result.ownerReference()
                            + " (seller "
                            + result.sellerUserId()
                            + ")."
            );
        } catch (IllegalArgumentException | IllegalStateException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return redirectTarget;
    }

    /**
     * Removes one Mercado Pago connection.
     *
     * @param id connection identifier
     * @param ra redirect attributes
     * @return redirect to settings page
     */
    @PostMapping("/mercadopago/conexoes/{id}/remover")
    public String removerConexaoMercadoPago(
            @PathVariable("id") final Long id,
            @RequestParam(name = "redirectTo", required = false)
            final String redirectTo,
            final RedirectAttributes ra
    ) {
        try {
            mercadoPagoOAuthService.disconnect(id);
            ra.addFlashAttribute(
                    "success",
                    "Conexao Mercado Pago removida."
            );
        } catch (NoSuchElementException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return resolveMercadoPagoRedirectTarget(redirectTo);
    }

    public String removerConexaoMercadoPago(
            final Long id,
            final RedirectAttributes ra
    ) {
        return removerConexaoMercadoPago(id, "", ra);
    }

    /**
     * Runs a real Mercado Pago checkout smoke test for one connected account.
     *
     * @param ownerReference internal account identifier
     * @param ra redirect attributes
     * @return redirect to settings page
     */
    @PostMapping("/mercadopago/conexoes/testar")
    public String testarConexaoMercadoPago(
            @RequestParam("ownerReference") final String ownerReference,
            @RequestParam(name = "redirectTo", required = false)
            final String redirectTo,
            final RedirectAttributes ra
    ) {
        try {
            final MercadoPagoCheckoutService.SmokeTestResult result =
                    mercadoPagoCheckoutService.runCheckoutSmokeTest(
                            ownerReference
                    );
            ra.addFlashAttribute(
                    "success",
                    "Teste de checkout Mercado Pago concluido com sucesso."
            );
            ra.addFlashAttribute("mercadoPagoSmokeTest", result);
        } catch (IllegalArgumentException
                 | IllegalStateException
                 | NoSuchElementException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return resolveMercadoPagoRedirectTarget(redirectTo);
    }

    public String testarConexaoMercadoPago(
            final String ownerReference,
            final RedirectAttributes ra
    ) {
        return testarConexaoMercadoPago(ownerReference, "", ra);
    }

    /**
     * Reads custom methods list from JSON settings value.
     *
     * @return list of custom methods
     */
    private List<MetodoPagamento> loadCustomMethods() {
        final String raw = settings.getOrDefault(KEY_CUSTOM_METHODS, "[]");
        try {
            final List<MetodoPagamento> list = objectMapper.readValue(
                    raw,
                    new TypeReference<List<MetodoPagamento>>() {
                    }
            );
            return list == null ? new ArrayList<>() : list;
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    /**
     * Persists custom methods list as JSON.
     *
     * @param methods methods list
     */
    private void saveCustomMethods(final List<MetodoPagamento> methods) {
        try {
            final String json = objectMapper.writeValueAsString(
                    methods == null ? List.of() : methods
            );
            settings.upsert(
                    KEY_CUSTOM_METHODS,
                    json,
                    "Metodos de pagamento personalizados"
            );
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Falha ao salvar metodos de pagamento",
                    ex
            );
        }
    }

    private String normalizeGateway(final String value) {
        final String normalized =
                MercadoPagoCheckoutService.normalizeGatewayValue(value);
        return normalized.isBlank() ? DEFAULT_GATEWAY : normalized;
    }

    private MercadoPagoOperationalStatus buildMercadoPagoOperationalStatus(
            final PagamentosForm cfg
    ) {
        final String ownerReference = nullSafe(
                cfg.getMercadoPagoOwnerReferenceDefault()
        );
        final boolean gatewaySelected = DEFAULT_GATEWAY.equals(
                normalizeGateway(cfg.getGateway())
        );
        final boolean oauthConfigured = mercadoPagoOAuthService.isReady();
        final boolean webhookConfigured =
                mercadoPagoCheckoutService.hasNotificationUrlConfigured();
        final boolean ownerReferenceConfigured = !ownerReference.isBlank();
        final boolean ownerReferenceConnected = ownerReferenceConfigured
                && mercadoPagoOAuthService.hasActiveConnection(ownerReference);
        final MercadoPagoOAuthService.WebhookReceiptView webhookReceipt =
                ownerReferenceConnected
                        ? mercadoPagoOAuthService.getWebhookReceipt(ownerReference)
                        : MercadoPagoOAuthService.WebhookReceiptView.empty();
        final boolean tenantMappingConfigured = !nullSafe(
                cfg.getMercadoPagoOwnerReferenceByTenant()
        ).isBlank();
        final boolean readyForCheckout = gatewaySelected
                && oauthConfigured
                && webhookConfigured
                && (ownerReferenceConnected || tenantMappingConfigured);
        final boolean endToEndValidated = readyForCheckout
                && webhookReceipt.received();
        return new MercadoPagoOperationalStatus(
                gatewaySelected,
                oauthConfigured,
                webhookConfigured,
                ownerReference,
                ownerReferenceConfigured,
                ownerReferenceConnected,
                webhookReceipt.received(),
                webhookReceipt.lastReceivedAtLabel(),
                tenantMappingConfigured,
                readyForCheckout,
                endToEndValidated
        );
    }

    private PagamentosForm loadPagamentosForm() {
        final PagamentosForm cfg = new PagamentosForm();
        cfg.setGateway(
                normalizeGateway(
                        settings.getOrDefault(KEY_GATEWAY, DEFAULT_GATEWAY)
                )
        );
        cfg.setPixAtivo(settings.getBoolean(KEY_PIX_ATIVO, true));
        cfg.setCartaoAtivo(settings.getBoolean(KEY_CARTAO_ATIVO, true));
        cfg.setBoletoAtivo(settings.getBoolean(KEY_BOLETO_ATIVO, false));
        cfg.setDinheiroAtivo(settings.getBoolean(KEY_DINHEIRO_ATIVO, true));
        cfg.setTaxacartao(
                settings.getDecimal(KEY_TAXA_CARTAO, BigDecimal.ZERO)
        );
        cfg.setMaxParcelas(
                settings.getInt(KEY_MAX_PARCELAS, DEFAULT_MAX_PARCELAS)
        );
        cfg.setParcelaMin(
                settings.getDecimal(KEY_PARCELA_MIN, DEFAULT_PARCELA_MIN)
        );
        cfg.setPublicKey(settings.getOrDefault(KEY_PUBLIC_KEY, ""));
        cfg.setSecretKey(settings.getOrDefault(KEY_SECRET_KEY, ""));
        cfg.setWebhookUrl(settings.getOrDefault(KEY_WEBHOOK_URL, ""));
        cfg.setMercadoPagoClientId(settings.getOrDefault(KEY_MP_CLIENT_ID, ""));
        cfg.setMercadoPagoClientSecret(
                settings.getOrDefault(KEY_MP_CLIENT_SECRET, "")
        );
        cfg.setMercadoPagoRedirectUri(
                settings.getOrDefault(KEY_MP_REDIRECT_URI, "")
        );
        cfg.setMercadoPagoOwnerReferenceDefault(
                settings.getOrDefault(KEY_MP_OWNER_REFERENCE_DEFAULT, "")
        );
        cfg.setMercadoPagoOwnerReferenceByTenant(
                settings.getOrDefault(KEY_MP_OWNER_REFERENCE_BY_TENANT, "")
        );
        cfg.setPixChave(settings.getOrDefault(KEY_PIX_CHAVE, ""));
        cfg.setPixTipo(settings.getOrDefault(KEY_PIX_TIPO, DEFAULT_PIX_TIPO));
        cfg.setDinheiroMinimo(
                settings.getDecimal(KEY_DINHEIRO_MIN, BigDecimal.ZERO)
        );
        cfg.setDinheiroTrocoMax(
                settings.getDecimal(KEY_DINHEIRO_TROCO_MAX, BigDecimal.ZERO)
        );
        return cfg;
    }

    private void populatePagamentosModel(
            final Model model,
            final PagamentosForm cfg
    ) {
        model.addAttribute("cfg", cfg);
        model.addAttribute("customMethods", loadCustomMethods());
        model.addAttribute("novoMetodo", new MetodoForm());
        model.addAttribute(
                "webhookConfigured",
                cfg.getWebhookUrl() != null && !cfg.getWebhookUrl().isBlank()
        );
        model.addAttribute(
                "mercadoPagoOauthConfigured",
                mercadoPagoOAuthService.isReady()
        );
        model.addAttribute(
                "mercadoPagoConnections",
                mercadoPagoOAuthService.listConnections()
        );
        model.addAttribute(
                "mercadoPagoOperationalStatus",
                buildMercadoPagoOperationalStatus(cfg)
        );
        model.addAttribute(
                "mercadoPagoNotificationUrlPreview",
                mercadoPagoCheckoutService.getNotificationUrlPreview()
        );
    }

    private MercadoPagoAssistenteForm buildMercadoPagoAssistenteForm(
            final PagamentosForm cfg
    ) {
        final MercadoPagoAssistenteForm form = new MercadoPagoAssistenteForm();
        form.setClientId(cfg.getMercadoPagoClientId());
        form.setClientSecret(cfg.getMercadoPagoClientSecret());
        form.setRedirectUri(resolveMercadoPagoRedirectUrlPreview(cfg));
        form.setWebhookUrl(cfg.getWebhookUrl());
        form.setOwnerReferenceDefault(cfg.getMercadoPagoOwnerReferenceDefault());
        form.setOwnerReferenceByTenant(
                cfg.getMercadoPagoOwnerReferenceByTenant()
        );
        form.setOwnerReferenceToConnect(
                firstNonBlank(
                        cfg.getMercadoPagoOwnerReferenceDefault(),
                        ""
                )
        );
        return form;
    }

    private String resolveMercadoPagoRedirectUrlPreview(
            final PagamentosForm cfg
    ) {
        final String configuredRedirectUri = cfg == null
                ? ""
                : nullSafe(cfg.getMercadoPagoRedirectUri());
        if (!configuredRedirectUri.isBlank()) {
            return configuredRedirectUri;
        }
        return mercadoPagoCheckoutService.getOauthRedirectUrlPreview();
    }

    private void persistMercadoPagoAssistente(
            final MercadoPagoAssistenteForm form
    ) {
        saveText(KEY_GATEWAY, DEFAULT_GATEWAY, "Gateway ativo");
        saveText(KEY_MP_CLIENT_ID, form.getClientId(),
                "Mercado Pago OAuth client id");
        saveText(KEY_MP_CLIENT_SECRET, form.getClientSecret(),
                "Mercado Pago OAuth client secret");
        saveText(
                KEY_MP_REDIRECT_URI,
                firstNonBlank(
                        form.getRedirectUri(),
                        mercadoPagoCheckoutService.getOauthRedirectUrlPreview()
                ),
                "Mercado Pago OAuth redirect uri"
        );
        saveText(
                KEY_MP_OWNER_REFERENCE_DEFAULT,
                form.getOwnerReferenceDefault(),
                "Mercado Pago owner reference padrao"
        );
        saveText(
                KEY_MP_OWNER_REFERENCE_BY_TENANT,
                form.getOwnerReferenceByTenant(),
                "Mercado Pago owner reference por tenant"
        );
        saveText(
                KEY_WEBHOOK_URL,
                form.getWebhookUrl(),
                "Webhook gateway"
        );
    }

    private String resolveOwnerReferenceToConnect(
            final MercadoPagoAssistenteForm form
    ) {
        final String ownerReference = firstNonBlank(
                form.getOwnerReferenceToConnect(),
                form.getOwnerReferenceDefault()
        );
        if (ownerReference.isBlank()) {
            throw new IllegalArgumentException(
                    "Informe o owner reference que sera conectado agora."
            );
        }
        return ownerReference;
    }

    private void storeOauthReturnPath(
            final HttpSession session,
            final String redirectTo
    ) {
        if (session == null) {
            return;
        }
        session.setAttribute(
                SESSION_MP_RETURN_PATH,
                resolveMercadoPagoRedirectTarget(redirectTo)
        );
    }

    private String resolveAndClearOauthReturnPath(final HttpSession session) {
        if (session == null) {
            return REDIRECT_PAGAMENTOS;
        }
        final Object rawValue = session.getAttribute(SESSION_MP_RETURN_PATH);
        final String value = rawValue == null ? "" : nullSafe(rawValue.toString());
        session.removeAttribute(SESSION_MP_RETURN_PATH);
        if (value.isBlank()) {
            return REDIRECT_PAGAMENTOS;
        }
        return value;
    }

    private String resolveMercadoPagoRedirectTarget(final String redirectTo) {
        return REDIRECT_TARGET_ASSISTENTE.equalsIgnoreCase(nullSafe(redirectTo))
                ? REDIRECT_MERCADO_PAGO_ASSISTENTE
                : REDIRECT_PAGAMENTOS;
    }

    /**
     * Saves one text setting.
     *
     * @param key setting key
     * @param value text value
     * @param description setting description
     */
    private void saveText(
            final String key,
            final String value,
            final String description
    ) {
        settings.upsert(key, nullSafe(value), description);
    }

    /**
     * Saves one boolean setting.
     *
     * @param key setting key
     * @param value boolean value
     * @param description setting description
     */
    private void saveBoolean(
            final String key,
            final Boolean value,
            final String description
    ) {
        settings.upsert(key, bool(value), description);
    }

    /**
     * Saves one integer setting.
     *
     * @param key setting key
     * @param value integer value
     * @param description setting description
     */
    private void saveInteger(
            final String key,
            final Integer value,
            final String description
    ) {
        settings.upsert(key, intVal(value), description);
    }

    /**
     * Saves one decimal setting.
     *
     * @param key setting key
     * @param value decimal value
     * @param description setting description
     */
    private void saveDecimal(
            final String key,
            final BigDecimal value,
            final String description
    ) {
        settings.upsert(key, decimal(value), description);
    }

    /**
     * Converts boolean to setting string value.
     *
     * @param value source value
     * @return setting value
     */
    private String bool(final Boolean value) {
        return value != null && value ? "true" : "false";
    }

    /**
     * Converts integer to setting string value.
     *
     * @param value source value
     * @return setting value
     */
    private String intVal(final Integer value) {
        return value == null ? "" : String.valueOf(value);
    }

    /**
     * Converts decimal to setting string value.
     *
     * @param value source value
     * @return setting value
     */
    private String decimal(final BigDecimal value) {
        return value == null ? "" : value.toPlainString();
    }

    /**
     * Returns trimmed value or empty string.
     *
     * @param value source value
     * @return null-safe string
     */
    private String nullSafe(final String value) {
        return value == null ? "" : value.trim();
    }

    private String firstNonBlank(final String first, final String second) {
        return nullSafe(first).isBlank() ? nullSafe(second) : nullSafe(first);
    }

    /**
     * Form payload for main payment settings.
     */
    @Getter
    @Setter
    public static final class PagamentosForm {

        /**
         * PIX enablement flag.
         */
        private Boolean pixAtivo;

        /**
         * Card enablement flag.
         */
        private Boolean cartaoAtivo;

        /**
         * Boleto enablement flag.
         */
        private Boolean boletoAtivo;

        /**
         * Cash enablement flag.
         */
        private Boolean dinheiroAtivo;

        /**
         * Active gateway.
         */
        private String gateway;

        /**
         * Card fee percentage.
         */
        private BigDecimal taxacartao;

        /**
         * Maximum installments.
         */
        private Integer maxParcelas;

        /**
         * Minimum installment amount.
         */
        private BigDecimal parcelaMin;

        /**
         * Gateway public key.
         */
        private String publicKey;

        /**
         * Gateway secret key.
         */
        private String secretKey;

        /**
         * Gateway webhook URL.
         */
        private String webhookUrl;

        /**
         * Mercado Pago OAuth client id.
         */
        private String mercadoPagoClientId;

        /**
         * Mercado Pago OAuth client secret.
         */
        private String mercadoPagoClientSecret;

        /**
         * Mercado Pago OAuth redirect uri.
         */
        private String mercadoPagoRedirectUri;

        /**
         * Default owner reference used by checkout.
         */
        private String mercadoPagoOwnerReferenceDefault;

        /**
         * Tenant-to-owner reference mapping.
         */
        private String mercadoPagoOwnerReferenceByTenant;

        /**
         * PIX key value.
         */
        private String pixChave;

        /**
         * PIX key type.
         */
        private String pixTipo;

        /**
         * Minimum cash amount.
         */
        private BigDecimal dinheiroMinimo;

        /**
         * Maximum cash change amount.
         */
        private BigDecimal dinheiroTrocoMax;
    }

    /**
     * Form payload for the Mercado Pago guided assistant.
     */
    @Getter
    @Setter
    public static final class MercadoPagoAssistenteForm {

        /**
         * Mercado Pago OAuth client id.
         */
        private String clientId;

        /**
         * Mercado Pago OAuth client secret.
         */
        private String clientSecret;

        /**
         * Mercado Pago OAuth redirect uri.
         */
        private String redirectUri;

        /**
         * Optional webhook override.
         */
        private String webhookUrl;

        /**
         * Default owner reference used by checkout.
         */
        private String ownerReferenceDefault;

        /**
         * Tenant-to-owner reference mapping.
         */
        private String ownerReferenceByTenant;

        /**
         * Owner reference selected to connect immediately.
         */
        private String ownerReferenceToConnect;
    }

    /**
     * Form payload for custom method creation.
     */
    @Getter
    @Setter
    public static final class MetodoForm {

        /**
         * Method display name.
         */
        @NotBlank(message = "Nome do metodo obrigatorio")
        private String nome;

        /**
         * Method type.
         */
        private String tipo;

        /**
         * Method fee.
         */
        private BigDecimal taxa;

        /**
         * Method active flag.
         */
        private Boolean ativo;
    }

    /**
     * Persisted custom payment method.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static final class MetodoPagamento {

        /**
         * Method identifier.
         */
        private String id;

        /**
         * Method name.
         */
        private String nome;

        /**
         * Method type.
         */
        private String tipo;

        /**
         * Method fee.
         */
        private BigDecimal taxa;

        /**
         * Method active flag.
         */
        private boolean ativo;

        /**
         * Creates a full method payload.
         *
         * @param idValue identifier
         * @param nomeValue name
         * @param tipoValue type
         * @param taxaValue fee
         * @param ativoValue active flag
         */
        public MetodoPagamento(
                final String idValue,
                final String nomeValue,
                final String tipoValue,
                final BigDecimal taxaValue,
                final boolean ativoValue
        ) {
            this.id = idValue;
            this.nome = nomeValue;
            this.tipo = tipoValue;
            this.taxa = taxaValue;
            this.ativo = ativoValue;
        }
    }

    public record MercadoPagoOperationalStatus(
            boolean gatewaySelected,
            boolean oauthConfigured,
            boolean webhookConfigured,
            String ownerReference,
            boolean ownerReferenceConfigured,
            boolean ownerReferenceConnected,
            boolean webhookReceived,
            String lastWebhookReceivedAtLabel,
            boolean tenantMappingConfigured,
            boolean readyForCheckout,
            boolean endToEndValidated
    ) {
    }
}
