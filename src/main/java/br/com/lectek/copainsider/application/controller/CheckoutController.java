package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.adapters.outbound.auth.jwt.model.JwtPrincipal;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.PedidoJPARepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.lectek.copainsider.application.service.CartService;
import br.com.lectek.copainsider.application.service.PaymentMethodService;
import br.com.lectek.copainsider.application.service.delivery.DeliveryPricingService;
import br.com.lectek.copainsider.application.service.fiscal.PedidoFiscalSnapshotService;
import br.com.lectek.copainsider.application.support.DeliveryCodeGenerator;
import br.com.lectek.copainsider.application.support.NavigationLinkSupport;
import br.com.lectek.copainsider.application.support.PedidoStatusSupport;
import br.com.lectek.copainsider.application.view.DeliveryQuoteVM;
import br.com.lectek.copainsider.domain.enums.ModoEntrega;
import br.com.lectek.copainsider.domain.enums.TipoPagamento;
import br.com.lectek.copainsider.domain.financeiro.mercadopago.MercadoPagoCheckoutService;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;

@Controller
public class CheckoutController {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final String SESSION_TENANT_CONTEXT_ID = "tenantContextId";
    private static final String SESSION_PAYMENT_VALUE = "checkoutPaymentValue";
    private static final String SESSION_PAYMENT_LABEL = "checkoutPaymentLabel";
    private static final String SESSION_PEDIDO_ID = "checkoutPedidoId";
    private static final String SESSION_DELIVERY_CODE = "checkoutDeliveryCode";
    private static final String SESSION_DELIVERY_ADDRESS = "checkoutDeliveryAddress";
    private static final String SESSION_FULFILLMENT_MODE = "checkoutFulfillmentMode";
    private static final String SESSION_FULFILLMENT_LABEL = "checkoutFulfillmentLabel";
    private static final String SESSION_SHIPPING_LABEL = "checkoutShippingLabel";
    private static final String SESSION_SHIPPING_VALUE = "checkoutShippingValue";
    private static final String SESSION_ORDER_TOTAL = "checkoutOrderTotal";
    private static final String SESSION_FISCAL_EMAIL = "checkoutFiscalEmail";
    private static final String SESSION_GATEWAY_CHECKOUT_URL =
            "checkoutGatewayCheckoutUrl";
    private static final String SESSION_GATEWAY_PAYMENT_STATUS =
            "checkoutGatewayPaymentStatus";
    private static final String SESSION_GATEWAY_PAYMENT_TICKET_URL =
            "checkoutGatewayPaymentTicketUrl";
    private static final String SESSION_GATEWAY_PIX_QR_CODE =
            "checkoutGatewayPixQrCode";
    private static final String SESSION_GATEWAY_PIX_QR_CODE_BASE64 =
            "checkoutGatewayPixQrCodeBase64";
    private static final String SESSION_PAYMENT_WARNING =
            "checkoutPaymentWarning";
    private static final String SHIPPING_STANDARD = "padrao";
    private static final String SHIPPING_PRIORITY = "prioritario";
    private static final BigDecimal PRIORITY_SHIPPING_SURCHARGE =
            BigDecimal.valueOf(20L);

    private final PaymentMethodService paymentMethodService;
    private final CartService cartService;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final PedidoJPARepository pedidoRepository;
    private final PedidoFiscalSnapshotService pedidoFiscalSnapshotService;
    private final DeliveryPricingService deliveryPricingService;
    private final MercadoPagoCheckoutService mercadoPagoCheckoutService;

    public CheckoutController(PaymentMethodService paymentMethodService,
                              CartService cartService,
                              ClienteRepository clienteRepository,
                              UsuarioRepository usuarioRepository,
                              PedidoJPARepository pedidoRepository,
                              PedidoFiscalSnapshotService pedidoFiscalSnapshotService,
                              DeliveryPricingService deliveryPricingService,
                              MercadoPagoCheckoutService mercadoPagoCheckoutService) {
        this.paymentMethodService = paymentMethodService;
        this.cartService = cartService;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.pedidoRepository = pedidoRepository;
        this.pedidoFiscalSnapshotService = pedidoFiscalSnapshotService;
        this.deliveryPricingService = deliveryPricingService;
        this.mercadoPagoCheckoutService = mercadoPagoCheckoutService;
    }

    @PostMapping("/pedido/finalizar")
    public String finalizarPedido(
            @RequestParam(value = "nome", required = false) String nome,
            @RequestParam(value = "cpf", required = false) String cpf,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "endereco", required = false) String endereco,
            @RequestParam(value = "recebimento", required = false) String recebimento,
            @RequestParam(value = "frete", required = false) String frete,
            @RequestParam(value = "pagamento", required = false) String pagamento,
            @RequestParam(value = "precisaTroco", required = false) String precisaTroco,
            @RequestParam(value = "trocoPara", required = false) String trocoPara,
            @RequestParam(value = "tenantId", required = false) String tenantId,
            Authentication auth,
            HttpSession session,
            RedirectAttributes ra) {

        String nomeValue = normalize(nome);
        String cpfValue = normalizeCpf(cpf);
        String emailValue = normalize(email);
        String enderecoValue = normalizeAddress(endereco);
        String recebimentoValue = normalizeFulfillment(recebimento);
        String freteValue = normalizeShipping(frete);
        String pagamentoValue = normalize(pagamento);
        String precisaTrocoValue = normalize(precisaTroco);
        String tenantIdValue = resolveTenantId(tenantId, auth, session);

        if (!tenantIdValue.isBlank()) {
            ra.addFlashAttribute("checkoutInputTenantId", tenantIdValue);
        }

        Optional<ClienteEntity> clienteLogado = resolveClienteLogado(auth, "", "");
        if (clienteLogado.isPresent()) {
            ClienteEntity cliente = clienteLogado.get();
            if (nomeValue.isBlank()) {
                nomeValue = normalize(cliente.getNome());
            }
            if (cpfValue.isBlank()) {
                cpfValue = normalizeCpf(cliente.getCpf());
            }
            if (emailValue.isBlank()) {
                emailValue = normalize(cliente.getEmail());
            }
        }

        if (pagamentoValue.isBlank()) {
            preserveCheckoutState(
                    ra,
                    nomeValue,
                    cpfValue,
                    emailValue,
                    enderecoValue,
                    recebimentoValue,
                    freteValue,
                    pagamentoValue,
                    precisaTrocoValue,
                    trocoPara
            );
            ra.addFlashAttribute("error", "Selecione a forma de pagamento para finalizar.");
            return "redirect:/checkout";
        }

        if (!paymentMethodService.isActiveValue(pagamentoValue)) {
            preserveCheckoutState(
                    ra,
                    nomeValue,
                    cpfValue,
                    emailValue,
                    enderecoValue,
                    recebimentoValue,
                    freteValue,
                    pagamentoValue,
                    precisaTrocoValue,
                    trocoPara
            );
            ra.addFlashAttribute("error", "Metodo de pagamento invalido ou indisponivel.");
            return "redirect:/checkout";
        }

        String label = paymentMethodService.resolveLabel(pagamentoValue);
        TipoPagamento tipoPagamento = resolveTipoPagamento(pagamentoValue);
        if (tipoPagamento == null) {
            preserveCheckoutState(
                    ra,
                    nomeValue,
                    cpfValue,
                    emailValue,
                    enderecoValue,
                    recebimentoValue,
                    freteValue,
                    pagamentoValue,
                    precisaTrocoValue,
                    trocoPara
            );
            ra.addFlashAttribute("error", "Metodo de pagamento invalido.");
            return "redirect:/checkout";
        }

        if (requiresCustomerCardData(tipoPagamento)
                && (nomeValue.isBlank() || cpfValue.isBlank() || emailValue.isBlank())) {
            preserveCheckoutState(
                    ra,
                    nomeValue,
                    cpfValue,
                    emailValue,
                    enderecoValue,
                    recebimentoValue,
                    freteValue,
                    pagamentoValue,
                    precisaTrocoValue,
                    trocoPara
            );
            ra.addFlashAttribute(
                    "error",
                    "Preencha nome, CPF e e-mail para pagamentos em cartao."
            );
            return "redirect:/checkout";
        }

        if (!isValidFulfillment(recebimentoValue)) {
            preserveCheckoutState(
                    ra,
                    nomeValue,
                    cpfValue,
                    emailValue,
                    enderecoValue,
                    recebimentoValue,
                    freteValue,
                    pagamentoValue,
                    precisaTrocoValue,
                    trocoPara
            );
            ra.addFlashAttribute("error", "Selecione se o pedido sera entregue ou retirado na loja.");
            return "redirect:/checkout";
        }
        if (isPickup(recebimentoValue)) {
            enderecoValue = "";
        } else {
            if (!isValidShippingOption(freteValue)) {
                preserveCheckoutState(
                        ra,
                        nomeValue,
                        cpfValue,
                        emailValue,
                        enderecoValue,
                        recebimentoValue,
                        freteValue,
                        pagamentoValue,
                        precisaTrocoValue,
                        trocoPara
                );
                ra.addFlashAttribute("error", "Selecione uma opcao valida de frete.");
                return "redirect:/checkout";
            }
            if (enderecoValue.isBlank()) {
                enderecoValue = resolveEnderecoEntrega(auth);
            }
            if (enderecoValue.isBlank()) {
                preserveCheckoutState(
                        ra,
                        nomeValue,
                        cpfValue,
                        emailValue,
                        enderecoValue,
                        recebimentoValue,
                        freteValue,
                        pagamentoValue,
                        precisaTrocoValue,
                        trocoPara
                );
                ra.addFlashAttribute("error", "Informe o endereco para entrega.");
                return "redirect:/checkout";
            }
        }

        final DeliveryQuoteVM deliveryQuote;
        if (isPickup(recebimentoValue)) {
            deliveryQuote = null;
        } else {
            deliveryQuote = deliveryPricingService.quoteForAddress(enderecoValue);
            if (!deliveryQuote.available()) {
                preserveCheckoutState(
                        ra,
                        nomeValue,
                        cpfValue,
                        emailValue,
                        enderecoValue,
                        recebimentoValue,
                        freteValue,
                        pagamentoValue,
                        precisaTrocoValue,
                        trocoPara
                );
                ra.addFlashAttribute(
                        "error",
                        "Nao foi possivel calcular o frete para esse endereco. Revise a rua e tente novamente."
                );
                return "redirect:/checkout";
            }
        }

        Optional<ClienteEntity> clienteOpt = resolveClienteLogado(auth, emailValue, cpfValue);
        if (clienteOpt.isEmpty()) {
            preserveCheckoutState(
                    ra,
                    nomeValue,
                    cpfValue,
                    emailValue,
                    enderecoValue,
                    recebimentoValue,
                    freteValue,
                    pagamentoValue,
                    precisaTrocoValue,
                    trocoPara
            );
            ra.addFlashAttribute("error", "Cliente nao encontrado. Faca login novamente.");
            return "redirect:/checkout";
        }

        CartService.CartOrderData orderData = cartService.buildOrderData(session);
        if (orderData.getInvalidItems() != null && !orderData.getInvalidItems().isEmpty()) {
            ra.addFlashAttribute("error", "Existem itens indisponiveis no carrinho. Ajuste antes de continuar.");
            return "redirect:/carrinho";
        }
        if (orderData.getItems().isEmpty()) {
            ra.addFlashAttribute("error", "Seu carrinho esta vazio.");
            return "redirect:/carrinho";
        }

        if (!paymentMethodService.isOfflineValue(pagamentoValue)) {
            try {
                mercadoPagoCheckoutService.assertReadyForOnlineCheckout(
                        new MercadoPagoCheckoutService.CheckoutRequest(
                                "",
                                "",
                                "",
                                tenantIdValue,
                                ""
                        )
                );
            } catch (IllegalStateException ex) {
                preserveCheckoutState(
                        ra,
                        nomeValue,
                        cpfValue,
                        emailValue,
                        enderecoValue,
                        recebimentoValue,
                        freteValue,
                        pagamentoValue,
                        precisaTrocoValue,
                        trocoPara
                );
                ra.addFlashAttribute("error", ex.getMessage());
                return "redirect:/checkout";
            }
        }

        BigDecimal shippingAmount = resolveShippingAmount(
                recebimentoValue,
                freteValue,
                deliveryQuote
        );
        BigDecimal finalOrderTotal = orderData.getTotal().add(shippingAmount);

        BigDecimal trocoParaValue = null;
        if (TipoPagamento.DINHEIRO.equals(tipoPagamento)
                && "sim".equalsIgnoreCase(precisaTrocoValue)) {
            trocoParaValue = parseCurrency(trocoPara);
            if (trocoParaValue == null || trocoParaValue.compareTo(BigDecimal.ZERO) <= 0) {
                preserveCheckoutState(
                        ra,
                        nomeValue,
                        cpfValue,
                        emailValue,
                        enderecoValue,
                        recebimentoValue,
                        freteValue,
                        pagamentoValue,
                        precisaTrocoValue,
                        trocoPara
                );
                ra.addFlashAttribute("error", "Informe quanto sera pago em dinheiro para calcular o troco.");
                return "redirect:/checkout";
            }
            if (trocoParaValue.compareTo(finalOrderTotal) < 0) {
                preserveCheckoutState(
                        ra,
                        nomeValue,
                        cpfValue,
                        emailValue,
                        enderecoValue,
                        recebimentoValue,
                        freteValue,
                        pagamentoValue,
                        precisaTrocoValue,
                        trocoPara
                );
                ra.addFlashAttribute(
                        "error",
                        "O valor em dinheiro deve ser maior ou igual ao total do pedido."
                );
                return "redirect:/checkout";
            }
        }

        String paymentLabel = buildPaymentLabel(label, tipoPagamento, trocoParaValue);
        String paymentStorageValue = buildPaymentStorageValue(pagamentoValue, paymentLabel, trocoParaValue);

        ClienteEntity cliente = clienteOpt.get();
        PedidoEntity pedido = new PedidoEntity();
        pedido.setCliente(cliente);
        pedido.setStatus(PedidoStatusSupport.initialStatus(
                paymentMethodService.isOfflineValue(pagamentoValue)
        ));
        pedido.setTotal(finalOrderTotal);
        pedido.setTipoPagamento(tipoPagamento);
        pedido.setModoEntrega(resolveModoEntrega(recebimentoValue));
        pedido.setMetodoPagamento(paymentStorageValue);
        pedido.setEnderecoEntrega(enderecoValue.isBlank() ? null : enderecoValue);
        pedido.setCodigoEntrega(DeliveryCodeGenerator.nextCode());
        pedido.setCodigoEntregaGeradoEm(LocalDateTime.now());
        pedido.setCodigoEntregaConfirmadoEm(null);
        orderData.getItems().forEach(pedido::addItem);
        PedidoEntity saved = pedidoRepository.save(pedido);
        pedidoFiscalSnapshotService.capture(
                saved,
                new PedidoFiscalSnapshotService.SnapshotRequest(
                        "CHECKOUT_WEB",
                        nomeValue,
                        cpfValue,
                        emailValue,
                        cliente.getTelefone(),
                        saved.getEnderecoEntrega(),
                        shippingAmount
                )
        );

        if (!paymentMethodService.isOfflineValue(pagamentoValue)) {
            try {
                mercadoPagoCheckoutService.createCheckoutForPedido(
                        saved,
                        new MercadoPagoCheckoutService.CheckoutRequest(
                                nomeValue,
                                emailValue,
                                cpfValue,
                                tenantIdValue,
                                ""
                        )
                );
            } catch (IllegalStateException ex) {
                session.setAttribute(SESSION_PAYMENT_WARNING, ex.getMessage());
            }
        }

        session.setAttribute(SESSION_PAYMENT_VALUE, pagamentoValue);
        session.setAttribute(SESSION_PAYMENT_LABEL, paymentLabel);
        session.setAttribute(SESSION_PEDIDO_ID, saved.getId());
        session.setAttribute(SESSION_DELIVERY_CODE, saved.getCodigoEntrega());
        session.setAttribute(SESSION_DELIVERY_ADDRESS, saved.getEnderecoEntrega());
        session.setAttribute(SESSION_FULFILLMENT_MODE, recebimentoValue);
        session.setAttribute(
                SESSION_FULFILLMENT_LABEL,
                buildFulfillmentLabel(recebimentoValue)
        );
        session.setAttribute(
                SESSION_SHIPPING_LABEL,
                buildShippingLabel(recebimentoValue, freteValue)
        );
        session.setAttribute(SESSION_SHIPPING_VALUE, shippingAmount);
        session.setAttribute(SESSION_ORDER_TOTAL, finalOrderTotal);
        session.setAttribute(SESSION_FISCAL_EMAIL, emailValue);
        session.setAttribute(
                SESSION_GATEWAY_CHECKOUT_URL,
                saved.getGatewayCheckoutUrl()
        );
        session.setAttribute(
                SESSION_GATEWAY_PAYMENT_STATUS,
                mercadoPagoCheckoutService.resolvePaymentStatusLabel(saved)
        );
        session.setAttribute(
                SESSION_GATEWAY_PAYMENT_TICKET_URL,
                saved.getGatewayPaymentTicketUrl()
        );
        session.setAttribute(
                SESSION_GATEWAY_PIX_QR_CODE,
                saved.getGatewayPixQrCode()
        );
        session.setAttribute(
                SESSION_GATEWAY_PIX_QR_CODE_BASE64,
                saved.getGatewayPixQrCodeBase64()
        );
        cartService.clear(session);
        ra.addFlashAttribute("success", "Pedido registrado com o metodo de pagamento.");
        return "redirect:/checkout/confirmacao";
    }

    @GetMapping("/checkout/confirmacao")
    public String confirmacao(Model model, HttpSession session) {
        Object value = session.getAttribute(SESSION_PAYMENT_VALUE);
        Object label = session.getAttribute(SESSION_PAYMENT_LABEL);
        if (value != null) {
            model.addAttribute("paymentMethodValue", value.toString());
        }
        if (label != null) {
            model.addAttribute("paymentMethodLabel", label.toString());
        }
        Object pedidoId = session.getAttribute(SESSION_PEDIDO_ID);
        if (pedidoId != null) {
            model.addAttribute("pedidoId", pedidoId.toString());
        }
        Object deliveryCode = session.getAttribute(SESSION_DELIVERY_CODE);
        if (deliveryCode != null) {
            model.addAttribute("codigoEntrega", deliveryCode.toString());
        }
        Object deliveryAddress = session.getAttribute(SESSION_DELIVERY_ADDRESS);
        if (deliveryAddress != null) {
            final String enderecoEntrega = deliveryAddress.toString();
            model.addAttribute("enderecoEntrega", enderecoEntrega);
            model.addAttribute(
                    "googleMapsUrl",
                    NavigationLinkSupport.googleMapsDirections(enderecoEntrega)
            );
            model.addAttribute(
                    "wazeUrl",
                    NavigationLinkSupport.wazeNavigate(enderecoEntrega)
            );
        }
        Object fulfillmentMode = session.getAttribute(SESSION_FULFILLMENT_MODE);
        if (fulfillmentMode != null) {
            model.addAttribute("recebimentoMode", fulfillmentMode.toString());
        }
        Object fulfillmentLabel = session.getAttribute(SESSION_FULFILLMENT_LABEL);
        if (fulfillmentLabel != null) {
            model.addAttribute("recebimentoLabel", fulfillmentLabel.toString());
        }
        Object shippingLabel = session.getAttribute(SESSION_SHIPPING_LABEL);
        if (shippingLabel != null) {
            model.addAttribute("shippingLabel", shippingLabel.toString());
        }
        Object shippingValue = session.getAttribute(SESSION_SHIPPING_VALUE);
        if (shippingValue != null) {
            model.addAttribute("shippingValue", shippingValue);
        }
        Object orderTotal = session.getAttribute(SESSION_ORDER_TOTAL);
        if (orderTotal != null) {
            model.addAttribute("orderTotalValue", orderTotal);
        }
        Object fiscalEmail = session.getAttribute(SESSION_FISCAL_EMAIL);
        if (fiscalEmail != null) {
            model.addAttribute("fiscalEmail", fiscalEmail.toString());
        }
        Object gatewayCheckoutUrl = session.getAttribute(
                SESSION_GATEWAY_CHECKOUT_URL
        );
        if (gatewayCheckoutUrl != null) {
            model.addAttribute("gatewayCheckoutUrl", gatewayCheckoutUrl.toString());
        }
        Object gatewayPaymentStatus = session.getAttribute(
                SESSION_GATEWAY_PAYMENT_STATUS
        );
        if (gatewayPaymentStatus != null) {
            model.addAttribute(
                    "gatewayPaymentStatusLabel",
                    gatewayPaymentStatus.toString()
            );
        }
        Object gatewayPaymentTicketUrl = session.getAttribute(
                SESSION_GATEWAY_PAYMENT_TICKET_URL
        );
        if (gatewayPaymentTicketUrl != null) {
            model.addAttribute(
                    "gatewayPaymentTicketUrl",
                    gatewayPaymentTicketUrl.toString()
            );
        }
        Object gatewayPixQrCode = session.getAttribute(
                SESSION_GATEWAY_PIX_QR_CODE
        );
        if (gatewayPixQrCode != null) {
            model.addAttribute("gatewayPixQrCode", gatewayPixQrCode.toString());
        }
        Object gatewayPixQrCodeBase64 = session.getAttribute(
                SESSION_GATEWAY_PIX_QR_CODE_BASE64
        );
        if (gatewayPixQrCodeBase64 != null) {
            model.addAttribute(
                    "gatewayPixQrCodeBase64",
                    gatewayPixQrCodeBase64.toString()
            );
        }
        Object paymentWarning = session.getAttribute(SESSION_PAYMENT_WARNING);
        if (paymentWarning != null) {
            model.addAttribute("paymentWarning", paymentWarning.toString());
        }
        session.removeAttribute(SESSION_PAYMENT_VALUE);
        session.removeAttribute(SESSION_PAYMENT_LABEL);
        session.removeAttribute(SESSION_PEDIDO_ID);
        session.removeAttribute(SESSION_DELIVERY_CODE);
        session.removeAttribute(SESSION_DELIVERY_ADDRESS);
        session.removeAttribute(SESSION_FULFILLMENT_MODE);
        session.removeAttribute(SESSION_FULFILLMENT_LABEL);
        session.removeAttribute(SESSION_SHIPPING_LABEL);
        session.removeAttribute(SESSION_SHIPPING_VALUE);
        session.removeAttribute(SESSION_ORDER_TOTAL);
        session.removeAttribute(SESSION_FISCAL_EMAIL);
        session.removeAttribute(SESSION_GATEWAY_CHECKOUT_URL);
        session.removeAttribute(SESSION_GATEWAY_PAYMENT_STATUS);
        session.removeAttribute(SESSION_GATEWAY_PAYMENT_TICKET_URL);
        session.removeAttribute(SESSION_GATEWAY_PIX_QR_CODE);
        session.removeAttribute(SESSION_GATEWAY_PIX_QR_CODE_BASE64);
        session.removeAttribute(SESSION_PAYMENT_WARNING);
        return "pages/cliente/checkout/confirmacao";
    }

    private Optional<ClienteEntity> resolveClienteLogado(Authentication auth, String email, String cpf) {
        if (auth == null || auth.getName() == null) {
            return Optional.empty();
        }
        String ident = normalize(auth.getName());
        String emailValue = normalize(email);
        String cpfValue = normalizeCpf(cpf);

        Optional<ClienteEntity> cliente = clienteRepository.findByEmailIgnoreCase(ident);
        if (cliente.isEmpty()) {
            String identCpf = normalizeCpf(ident);
            if (!identCpf.isBlank()) {
                cliente = clienteRepository.findByCpf(identCpf);
            }
        }
        if (cliente.isEmpty() && !emailValue.isBlank()) {
            cliente = clienteRepository.findByEmailIgnoreCase(emailValue);
        }
        if (cliente.isEmpty() && !cpfValue.isBlank()) {
            cliente = clienteRepository.findByCpf(cpfValue);
        }
        if (cliente.isPresent()) {
            return cliente;
        }

        return usuarioRepository.findByEmailOrCpf(ident)
                .flatMap(this::upsertClienteFromUsuario);
    }

    private Optional<ClienteEntity> upsertClienteFromUsuario(UsuarioEntity usuario) {
        if (usuario == null) {
            return Optional.empty();
        }

        String email = normalize(usuario.getEmail()).toLowerCase();
        String cpf = normalizeCpf(usuario.getCpf());
        if (email.isBlank() || cpf.isBlank()) {
            return Optional.empty();
        }

        Optional<ClienteEntity> byEmail = clienteRepository.findByEmailIgnoreCase(email);
        Optional<ClienteEntity> byCpf = clienteRepository.findByCpf(cpf);
        ClienteEntity cliente = byEmail.or(() -> byCpf).orElseGet(ClienteEntity::new);

        String nome = normalize(usuario.getNome());
        cliente.setNome(nome.isBlank() ? "Cliente" : nome);
        cliente.setEmail(email);
        cliente.setCpf(cpf);
        cliente.setTelefone(normalize(usuario.getTelefone()));
        cliente.setAtivo(true);
        if (normalize(cliente.getSenha()).isBlank()) {
            cliente.setSenha(usuario.getSenha());
        }

        return Optional.of(clienteRepository.save(cliente));
    }

    private TipoPagamento resolveTipoPagamento(String value) {
        if (value == null) return null;
        return switch (value) {
            case "pix" -> TipoPagamento.PIX;
            case "boleto" -> TipoPagamento.BOLETO;
            case "credito" -> TipoPagamento.CARTAO_CREDITO;
            case "debito" -> TipoPagamento.CARTAO_DEBITO;
            case "dinheiro" -> TipoPagamento.DINHEIRO;
            default -> value.startsWith("custom:") ? TipoPagamento.CUSTOM : null;
        };
    }

    private String buildPaymentLabel(String label, TipoPagamento tipoPagamento, BigDecimal trocoPara) {
        if (!TipoPagamento.DINHEIRO.equals(tipoPagamento) || trocoPara == null) {
            return label;
        }
        return truncate(label + " (troco para " + formatCurrency(trocoPara) + ")", 80);
    }

    private String buildPaymentStorageValue(String pagamentoValue, String paymentLabel, BigDecimal trocoPara) {
        if (trocoPara == null) {
            return pagamentoValue;
        }
        return truncate(paymentLabel, 80);
    }

    private boolean requiresCustomerCardData(TipoPagamento tipoPagamento) {
        return TipoPagamento.CARTAO_CREDITO.equals(tipoPagamento)
                || TipoPagamento.CARTAO_DEBITO.equals(tipoPagamento);
    }

    private boolean isValidFulfillment(String value) {
        return "entrega".equals(value) || "retirada".equals(value);
    }

    private boolean isValidShippingOption(String value) {
        return SHIPPING_STANDARD.equals(value) || SHIPPING_PRIORITY.equals(value);
    }

    private boolean isPickup(String value) {
        return "retirada".equals(value);
    }

    private String buildFulfillmentLabel(String value) {
        if (isPickup(value)) {
            return "Retirada na loja";
        }
        return "Entrega";
    }

    private ModoEntrega resolveModoEntrega(String value) {
        return isPickup(value) ? ModoEntrega.RETIRADA : ModoEntrega.ENTREGA;
    }

    private BigDecimal resolveShippingAmount(
            String fulfillment,
            String shippingOption,
            DeliveryQuoteVM deliveryQuote
    ) {
        if (isPickup(fulfillment)) {
            return BigDecimal.ZERO;
        }
        BigDecimal standardShipping = deliveryQuote != null
                ? deliveryQuote.standardShippingAmount()
                : BigDecimal.ZERO;
        if (SHIPPING_PRIORITY.equals(shippingOption)) {
            return deliveryQuote != null
                    ? deliveryQuote.priorityShippingAmount()
                    : standardShipping.add(PRIORITY_SHIPPING_SURCHARGE);
        }
        return standardShipping;
    }

    private String buildShippingLabel(String fulfillment, String shippingOption) {
        if (isPickup(fulfillment)) {
            return "";
        }
        if (SHIPPING_PRIORITY.equals(shippingOption)) {
            return "Frete prioritario";
        }
        return "Frete padrao";
    }

    private BigDecimal parseCurrency(String raw) {
        String normalized = normalize(raw)
                .replace("R$", "")
                .replace(" ", "")
                .replace(".", "")
                .replace(",", ".");
        if (normalized.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String formatCurrency(BigDecimal value) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(value);
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeCpf(String value) {
        return value == null ? "" : value.replaceAll("[^0-9]", "");
    }

    private String normalizeAddress(String value) {
        String normalized = normalize(value);
        if (normalized.isBlank()) {
            return "";
        }
        return normalized.length() > 255 ? normalized.substring(0, 255) : normalized;
    }

    private String normalizeFulfillment(String value) {
        String normalized = normalize(value).toLowerCase(PT_BR);
        return normalized.isBlank() ? "entrega" : normalized;
    }

    private String normalizeShipping(String value) {
        String normalized = normalize(value).toLowerCase(PT_BR);
        return normalized.isBlank() ? SHIPPING_STANDARD : normalized;
    }

    private String resolveTenantId(
            String explicitTenantId,
            Authentication auth,
            HttpSession session
    ) {
        String tenantId = normalize(explicitTenantId);
        if (tenantId.isBlank()
                && auth != null
                && auth.getPrincipal() instanceof JwtPrincipal jwtPrincipal) {
            tenantId = normalize(jwtPrincipal.getTenant());
        }
        if (tenantId.isBlank() && session != null) {
            Object tenantFromSession = session.getAttribute(SESSION_TENANT_CONTEXT_ID);
            tenantId = tenantFromSession == null
                    ? ""
                    : normalize(tenantFromSession.toString());
        }
        if (!tenantId.isBlank() && session != null) {
            session.setAttribute(SESSION_TENANT_CONTEXT_ID, tenantId);
        }
        return tenantId;
    }

    private void preserveCheckoutState(
            RedirectAttributes ra,
            String nome,
            String cpf,
            String email,
            String endereco,
            String recebimento,
            String frete,
            String pagamento,
            String precisaTroco,
            String trocoPara
    ) {
        ra.addFlashAttribute("checkoutInputNome", nome);
        ra.addFlashAttribute("checkoutInputCpf", cpf);
        ra.addFlashAttribute("checkoutInputEmail", email);
        ra.addFlashAttribute("checkoutInputEndereco", endereco);
        ra.addFlashAttribute("checkoutInputRecebimento", recebimento);
        ra.addFlashAttribute("checkoutInputFrete", frete);
        ra.addFlashAttribute("checkoutInputPagamento", pagamento);
        ra.addFlashAttribute("checkoutInputPrecisaTroco", precisaTroco);
        ra.addFlashAttribute("checkoutInputTrocoPara", normalize(trocoPara));
    }

    private String resolveEnderecoEntrega(Authentication auth) {
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            return "";
        }
        return usuarioRepository.findByEmailOrCpf(auth.getName())
                .map(UsuarioEntity::getEndereco)
                .map(this::normalizeAddress)
                .filter(v -> !v.isBlank())
                .orElse("");
    }
}
