package br.com.redemaisfarma.domain.financeiro.mercadopago;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ItemPedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import br.com.redemaisfarma.domain.enums.TipoPagamento;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class MercadoPagoCheckoutService {

    public static final String PROVIDER_NAME = "mercadopago";
    public static final String KEY_GATEWAY = "pg.gateway";
    public static final String KEY_WEBHOOK_URL = "pg.webhook_url";
    public static final String KEY_OWNER_REFERENCE_DEFAULT =
            "pg.mp.owner_reference_default";
    public static final String KEY_OWNER_REFERENCE_BY_TENANT =
            "pg.mp.owner_reference_by_tenant";
    private static final String PAYMENT_STATUS_APPROVED = "approved";
    private static final String PAYMENT_STATUS_PENDING = "pending";
    private static final String PAYMENT_STATUS_IN_PROCESS = "in_process";
    private static final String PIX_PAYMENT_METHOD = "pix";
    private static final String PIX_PAYMENT_TYPE = "bank_transfer";

    private final AppSettingService settings;
    private final PedidoRepository pedidoRepository;
    private final MercadoPagoOAuthService oauthService;
    private final MercadoPagoCheckoutClient checkoutClient;
    private final String appBaseUrl;

    public MercadoPagoCheckoutService(
            final AppSettingService settingsValue,
            final PedidoRepository pedidoRepositoryValue,
            final MercadoPagoOAuthService oauthServiceValue,
            final MercadoPagoCheckoutClient checkoutClientValue,
            @Value("${app.web.base-url:http://localhost:8080}")
            final String appBaseUrlValue
    ) {
        this.settings = settingsValue;
        this.pedidoRepository = pedidoRepositoryValue;
        this.oauthService = oauthServiceValue;
        this.checkoutClient = checkoutClientValue;
        this.appBaseUrl = appBaseUrlValue;
    }

    @Transactional
    public void assertReadyForOnlineCheckout() {
        assertReadyForOnlineCheckout(CheckoutRequest.empty());
    }

    @Transactional
    public void assertReadyForOnlineCheckout(final CheckoutRequest checkoutRequest) {
        if (!isMercadoPagoGatewaySelected()) {
            throw new IllegalStateException(
                    "O gateway online ativo nao esta configurado para Mercado Pago."
            );
        }
        oauthService.requireAuthorizedConnection(
                resolveOwnerReference(null, checkoutRequest)
        );
    }

    @Transactional
    public SmokeTestResult runCheckoutSmokeTest(final String ownerReference) {
        if (!isMercadoPagoGatewaySelected()) {
            throw new IllegalStateException(
                    "Selecione Mercado Pago como gateway ativo antes de testar a conexao."
            );
        }

        final String resolvedOwnerReference = resolveOwnerReference(
                null,
                new CheckoutRequest("", "", "", "", ownerReference)
        );
        final MercadoPagoOAuthService.AuthorizedConnection connection =
                oauthService.requireAuthorizedConnection(resolvedOwnerReference);
        final String notificationUrl = resolveNotificationUrl();
        final String callbackUrl = buildAdminPaymentsUrl();
        final MercadoPagoCheckoutClient.PreferenceResponse response =
                checkoutClient.createPreference(
                        connection.accessToken(),
                        new MercadoPagoCheckoutClient.PreferenceRequest(
                                "smoke:" + resolvedOwnerReference + ":" + System.currentTimeMillis(),
                                notificationUrl,
                                callbackUrl,
                                callbackUrl,
                                callbackUrl,
                                List.of(new MercadoPagoCheckoutClient.PreferenceItem(
                                        "mp-smoke-test",
                                        "Teste de integracao RedeMaisFarma",
                                        1,
                                        new BigDecimal("1.00"),
                                        "BRL"
                                )),
                                new MercadoPagoCheckoutClient.PreferencePayer(
                                        "Teste RedeMaisFarma",
                                        "",
                                        ""
                                )
                        )
                );

        return new SmokeTestResult(
                resolvedOwnerReference,
                connection.sellerUserId(),
                connection.liveMode(),
                response.preferenceId(),
                resolveCheckoutUrl(response, connection),
                notificationUrl
        );
    }

    @Transactional
    public Optional<CheckoutPreferenceResult> ensureCheckoutForPedido(
            final PedidoEntity pedido
    ) {
        if (!isEligibleOnlineOrder(pedido) || !isPendingPaymentStatus(pedido)) {
            return Optional.empty();
        }
        if (isPixOrder(pedido)) {
            if (hasPixPresentation(pedido)) {
                return Optional.of(buildCheckoutResultFromPedido(pedido));
            }
            if (!text(pedido.getGatewayPaymentId()).isBlank()) {
                try {
                    syncPaymentForPedido(pedido, pedido.getGatewayPaymentId());
                } catch (IllegalStateException ignored) {
                    // Keep the original payment and let the page render.
                }
                return Optional.of(buildCheckoutResultFromPedido(pedido));
            }
        } else if (!text(pedido.getGatewayCheckoutUrl()).isBlank()) {
            return Optional.of(buildCheckoutResultFromPedido(pedido));
        }
        return Optional.of(createCheckoutForPedido(
                pedido,
                CheckoutRequest.fromCliente(pedido.getCliente())
        ));
    }

    @Transactional
    public CheckoutPreferenceResult createCheckoutForPedido(
            final PedidoEntity pedido,
            final CheckoutRequest checkoutRequest
    ) {
        if (!isEligibleOnlineOrder(pedido)) {
            throw new IllegalArgumentException(
                    "O pedido nao esta apto para checkout online."
            );
        }
        if (!isPendingPaymentStatus(pedido)) {
            throw new IllegalArgumentException(
                    "O pedido nao esta aguardando pagamento online."
            );
        }

        final String resolvedOwnerReference = resolveOwnerReference(
                pedido,
                checkoutRequest
        );
        final MercadoPagoOAuthService.AuthorizedConnection connection =
                oauthService.requireAuthorizedConnection(resolvedOwnerReference);
        final String externalReference = resolveExternalReference(pedido);
        final String detailUrl = buildOrderDetailUrl(pedido.getId());
        final String notificationUrl = resolveNotificationUrl();

        pedido.setGatewayProvider(PROVIDER_NAME);
        pedido.setGatewayOwnerReference(resolvedOwnerReference);
        pedido.setGatewayExternalReference(externalReference);
        pedidoRepository.save(pedido);

        if (isPixOrder(pedido)) {
            final MercadoPagoCheckoutClient.PaymentResponse payment =
                    checkoutClient.createPixPayment(
                            connection.accessToken(),
                            new MercadoPagoCheckoutClient.PixPaymentRequest(
                                    externalReference,
                                    notificationUrl,
                                    buildPixDescription(pedido),
                                    safeTransactionAmount(pedido),
                                    buildPayer(checkoutRequest, pedido),
                                    buildPixIdempotencyKey(
                                            resolvedOwnerReference,
                                            externalReference
                                    )
                            )
                    );
            applyPaymentToPedido(pedido, resolvedOwnerReference, payment);
            pedido.setGatewayPreferenceId(null);
            pedidoRepository.save(pedido);
            return buildCheckoutResultFromPedido(pedido);
        }

        final MercadoPagoCheckoutClient.PreferenceResponse response =
                checkoutClient.createPreference(
                        connection.accessToken(),
                        new MercadoPagoCheckoutClient.PreferenceRequest(
                                externalReference,
                                notificationUrl,
                                detailUrl,
                                detailUrl,
                                detailUrl,
                                buildPreferenceItems(pedido),
                                new MercadoPagoCheckoutClient.PreferencePayer(
                                        checkoutRequest.payerName(),
                                        checkoutRequest.payerEmail(),
                                        normalizeCpf(checkoutRequest.payerCpf())
                                )
                        )
                );

        pedido.setGatewayPreferenceId(response.preferenceId());
        pedido.setGatewayCheckoutUrl(resolveCheckoutUrl(response, connection));
        pedido.setGatewayPaymentTicketUrl(null);
        pedido.setGatewayPixQrCode(null);
        pedido.setGatewayPixQrCodeBase64(null);
        pedidoRepository.save(pedido);

        return buildCheckoutResultFromPedido(pedido);
    }

    @Transactional
    public PaymentSyncResult syncPaymentForPedido(
            final PedidoEntity pedido,
            final String paymentId
    ) {
        if (pedido == null || text(paymentId).isBlank()) {
            return PaymentSyncResult.ignored();
        }
        final String ownerReference = text(pedido.getGatewayOwnerReference());
        if (ownerReference.isBlank()) {
            return PaymentSyncResult.ignored();
        }
        final MercadoPagoOAuthService.AuthorizedConnection connection =
                oauthService.requireAuthorizedConnection(ownerReference);
        final MercadoPagoCheckoutClient.PaymentResponse payment =
                checkoutClient.fetchPayment(connection.accessToken(), paymentId);
        applyPaymentToPedido(pedido, connection.ownerReference(), payment);
        pedidoRepository.save(pedido);
        return PaymentSyncResult.updated(
                pedido.getId(),
                payment.paymentId(),
                payment.status()
        );
    }

    @Transactional
    public PaymentSyncResult handleWebhookNotification(
            final String type,
            final String topic,
            final String sellerUserId,
            final String paymentId
    ) {
        if (!isPaymentNotification(type, topic) || text(paymentId).isBlank()) {
            return PaymentSyncResult.ignored();
        }
        if (text(sellerUserId).isBlank()) {
            return PaymentSyncResult.ignored();
        }
        final MercadoPagoOAuthService.AuthorizedConnection connection =
                oauthService.requireAuthorizedConnectionBySellerUserId(
                        sellerUserId
                );
        final MercadoPagoCheckoutClient.PaymentResponse payment =
                checkoutClient.fetchPayment(connection.accessToken(), paymentId);
        oauthService.markWebhookReceived(
                connection.sellerUserId(),
                firstNonBlank(payment.paymentId(), paymentId)
        );
        final PedidoEntity pedido = resolvePedidoForPayment(payment);
        if (pedido == null) {
            return PaymentSyncResult.ignored();
        }
        applyPaymentToPedido(pedido, connection.ownerReference(), payment);
        pedidoRepository.save(pedido);
        return PaymentSyncResult.updated(
                pedido.getId(),
                payment.paymentId(),
                payment.status()
        );
    }

    public boolean canPayOnline(final PedidoEntity pedido) {
        return pedido != null
                && isPendingPaymentStatus(pedido)
                && PROVIDER_NAME.equalsIgnoreCase(text(pedido.getGatewayProvider()));
    }

    public boolean hasPaymentAction(final PedidoEntity pedido) {
        return canPayOnline(pedido)
                && (!text(pedido.getGatewayCheckoutUrl()).isBlank()
                || hasPixPresentation(pedido));
    }

    public boolean hasPixPayload(final PedidoEntity pedido) {
        return pedido != null
                && isPixOrder(pedido)
                && hasPixPresentation(pedido);
    }

    public String resolvePaymentStatusLabel(final PedidoEntity pedido) {
        if (pedido == null) {
            return "";
        }
        final String paymentStatus = text(pedido.getGatewayPaymentStatus())
                .toLowerCase(Locale.ROOT);
        if (paymentStatus.isBlank()
                && isPendingPaymentStatus(pedido)
                && PROVIDER_NAME.equalsIgnoreCase(
                        text(pedido.getGatewayProvider())
                )) {
            return isPixOrder(pedido)
                    ? "Aguardando pagamento Pix"
                    : "Aguardando pagamento online";
        }
        return switch (paymentStatus) {
            case PAYMENT_STATUS_APPROVED -> "Pagamento aprovado";
            case PAYMENT_STATUS_PENDING -> isPixOrder(pedido)
                    ? "Aguardando pagamento Pix"
                    : "Pagamento pendente";
            case PAYMENT_STATUS_IN_PROCESS -> "Pagamento em analise";
            case "rejected" -> "Pagamento recusado";
            case "cancelled" -> "Pagamento cancelado";
            case "refunded" -> "Pagamento estornado";
            default -> paymentStatus.isBlank() ? "" : paymentStatus;
        };
    }

    public boolean hasNotificationUrlConfigured() {
        return !resolveNotificationUrl().isBlank();
    }

    public String getNotificationUrlPreview() {
        return resolveNotificationUrl();
    }

    public String getOauthRedirectUrlPreview() {
        return buildAbsoluteUrl(
                "/admin/configuracoes/pagamentos/mercadopago/oauth/callback"
        );
    }

    public String getAppBaseUrlPreview() {
        return normalizeBaseUrl(appBaseUrl);
    }

    private PedidoEntity resolvePedidoForPayment(
            final MercadoPagoCheckoutClient.PaymentResponse payment
    ) {
        final String paymentId = text(payment.paymentId());
        if (!paymentId.isBlank()) {
            final Optional<PedidoEntity> byPaymentId =
                    pedidoRepository.findByGatewayPaymentId(paymentId);
            if (byPaymentId.isPresent()) {
                return byPaymentId.get();
            }
        }
        final String externalReference = text(payment.externalReference());
        if (!externalReference.isBlank()) {
            return pedidoRepository.findByGatewayExternalReference(
                    externalReference
            ).orElse(null);
        }
        return null;
    }

    private void applyPaymentToPedido(
            final PedidoEntity pedido,
            final String ownerReference,
            final MercadoPagoCheckoutClient.PaymentResponse payment
    ) {
        pedido.setGatewayProvider(PROVIDER_NAME);
        pedido.setGatewayOwnerReference(ownerReference);
        pedido.setGatewayPaymentId(payment.paymentId());
        pedido.setGatewayExternalReference(
                firstNonBlank(payment.externalReference(), pedido.getGatewayExternalReference())
        );
        pedido.setGatewayPaymentStatus(payment.status());
        pedido.setGatewayPaymentStatusDetail(payment.statusDetail());
        pedido.setGatewayPaymentUpdatedAt(toLocalDateTime(payment.updatedAt()));
        pedido.setGatewayPaymentTicketUrl(payment.ticketUrl());
        pedido.setGatewayPixQrCode(payment.qrCode());
        pedido.setGatewayPixQrCodeBase64(payment.qrCodeBase64());
        if (isPixOrder(pedido)) {
            pedido.setGatewayCheckoutUrl(
                    firstNonBlank(payment.ticketUrl(), pedido.getGatewayCheckoutUrl())
            );
        }
        pedido.setFormaPagamentoRecebida(buildReceivedPaymentLabel(payment));
        pedido.setPagamentoDivergente(isPaymentMethodDivergent(pedido, payment));

        final String status = text(payment.status()).toLowerCase(Locale.ROOT);
        if (PAYMENT_STATUS_APPROVED.equals(status)) {
            if (pedido.getStatus() == StatusPedido.AGUARDANDO_PAGAMENTO
                    || pedido.getStatus() == StatusPedido.ABERTO) {
                pedido.setStatus(StatusPedido.PAGO);
            }
            if (pedido.getPagamentoRecebidoEm() == null) {
                pedido.setPagamentoRecebidoEm(
                        firstNonNull(
                                toLocalDateTime(payment.approvedAt()),
                                LocalDateTime.now()
                        )
                );
            }
            return;
        }

        if ((PAYMENT_STATUS_PENDING.equals(status)
                || PAYMENT_STATUS_IN_PROCESS.equals(status))
                && pedido.getStatus() == StatusPedido.ABERTO) {
            pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
            return;
        }

        if (("rejected".equals(status)
                || "cancelled".equals(status))
                && pedido.getStatus() == StatusPedido.AGUARDANDO_PAGAMENTO) {
            pedido.setStatus(StatusPedido.CANCELADO);
        }
    }

    private List<MercadoPagoCheckoutClient.PreferenceItem> buildPreferenceItems(
            final PedidoEntity pedido
    ) {
        final List<MercadoPagoCheckoutClient.PreferenceItem> items =
                new ArrayList<>();
        BigDecimal itemsTotal = BigDecimal.ZERO;
        if (pedido.getItens() != null) {
            for (ItemPedidoEntity item : pedido.getItens()) {
                if (item == null) {
                    continue;
                }
                final String title = item.getProduto() != null
                        ? text(item.getProduto().getNome())
                        : "Item do pedido";
                final BigDecimal unitPrice = item.getPrecoUnitario() == null
                        ? BigDecimal.ZERO
                        : item.getPrecoUnitario();
                final int quantity = item.getQuantidade() == null
                        ? 1
                        : Math.max(item.getQuantidade(), 1);
                items.add(new MercadoPagoCheckoutClient.PreferenceItem(
                        item.getProduto() != null
                                ? String.valueOf(item.getProduto().getId())
                                : String.valueOf(items.size() + 1),
                        title.isBlank() ? "Item do pedido" : title,
                        quantity,
                        unitPrice,
                        "BRL"
                ));
                itemsTotal = itemsTotal.add(
                        unitPrice.multiply(BigDecimal.valueOf(quantity))
                );
            }
        }

        final BigDecimal orderTotal = pedido.getTotal() == null
                ? BigDecimal.ZERO
                : pedido.getTotal();
        final BigDecimal difference = orderTotal.subtract(itemsTotal);
        if (difference.compareTo(BigDecimal.ZERO) > 0) {
            items.add(new MercadoPagoCheckoutClient.PreferenceItem(
                    "pedido-frete",
                    "Frete do pedido",
                    1,
                    difference,
                    "BRL"
            ));
        }
        if (items.isEmpty()) {
            items.add(new MercadoPagoCheckoutClient.PreferenceItem(
                    "pedido-" + pedido.getId(),
                    "Pedido #" + pedido.getId(),
                    1,
                    orderTotal,
                    "BRL"
            ));
        }
        return items;
    }

    private String resolveOwnerReference() {
        final String ownerReference = text(
                settings.getOrDefault(KEY_OWNER_REFERENCE_DEFAULT, "")
        );
        if (ownerReference.isBlank()) {
            throw new IllegalStateException(
                    "Configure o owner reference padrao do checkout Mercado Pago."
            );
        }
        return ownerReference;
    }

    private String resolveOwnerReference(
            final PedidoEntity pedido,
            final CheckoutRequest checkoutRequest
    ) {
        final String pedidoOwnerReference = pedido == null
                ? ""
                : text(pedido.getGatewayOwnerReference());
        if (!pedidoOwnerReference.isBlank()) {
            return pedidoOwnerReference;
        }

        final String explicitOwnerReference = checkoutRequest == null
                ? ""
                : text(checkoutRequest.ownerReference());
        if (!explicitOwnerReference.isBlank()) {
            return explicitOwnerReference;
        }

        final String tenantId = normalizeTenantId(
                checkoutRequest == null ? "" : checkoutRequest.tenantId()
        );
        if (!tenantId.isBlank()) {
            final String mappedOwnerReference =
                    resolveOwnerReferenceByTenant(tenantId);
            if (!mappedOwnerReference.isBlank()) {
                return mappedOwnerReference;
            }
            if (oauthService.hasActiveConnection(tenantId)) {
                return tenantId;
            }
            throw new IllegalStateException(
                    "Nao existe owner reference Mercado Pago configurado para o tenant "
                            + tenantId
                            + "."
            );
        }

        return resolveOwnerReference();
    }

    private String resolveOwnerReferenceByTenant(final String tenantId) {
        final String rawMappings = settings.getOrDefault(
                KEY_OWNER_REFERENCE_BY_TENANT,
                ""
        );
        if (text(rawMappings).isBlank()) {
            return "";
        }
        final String normalizedTenantId = normalizeTenantId(tenantId);
        for (String line : rawMappings.split("\\r?\\n")) {
            final String trimmedLine = text(line);
            if (trimmedLine.isBlank() || trimmedLine.startsWith("#")) {
                continue;
            }
            final int separatorIndex = findSeparator(trimmedLine);
            if (separatorIndex <= 0
                    || separatorIndex >= trimmedLine.length() - 1) {
                continue;
            }
            final String key = normalizeTenantId(
                    trimmedLine.substring(0, separatorIndex)
            );
            final String value = text(
                    trimmedLine.substring(separatorIndex + 1)
            );
            if (normalizedTenantId.equals(key) && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private int findSeparator(final String line) {
        final int equalsIndex = line.indexOf('=');
        if (equalsIndex >= 0) {
            return equalsIndex;
        }
        return line.indexOf(':');
    }

    private String resolveExternalReference(final PedidoEntity pedido) {
        final String current = text(pedido.getGatewayExternalReference());
        if (!current.isBlank()) {
            return current;
        }
        return "pedido:" + pedido.getId();
    }

    private String resolveCheckoutUrl(
            final MercadoPagoCheckoutClient.PreferenceResponse response,
            final MercadoPagoOAuthService.AuthorizedConnection connection
    ) {
        if (connection.liveMode()) {
            return firstNonBlank(
                    response.initPoint(),
                    response.sandboxInitPoint()
            );
        }
        return firstNonBlank(
                response.sandboxInitPoint(),
                response.initPoint()
        );
    }

    private String resolveNotificationUrl() {
        final String configuredWebhook = text(
                settings.getOrDefault(KEY_WEBHOOK_URL, "")
        );
        if (!configuredWebhook.isBlank()) {
            return configuredWebhook;
        }
        final String normalizedBaseUrl = normalizeBaseUrl(appBaseUrl);
        if (!normalizedBaseUrl.startsWith("https://")) {
            return "";
        }
        return normalizedBaseUrl + "/webhooks/mercadopago";
    }

    private String buildOrderDetailUrl(final Long pedidoId) {
        return UriComponentsBuilder.fromUriString(normalizeBaseUrl(appBaseUrl))
                .path("/cliente/pedidos/{id}")
                .buildAndExpand(pedidoId)
                .toUriString();
    }

    private String buildAdminPaymentsUrl() {
        return UriComponentsBuilder.fromUriString(normalizeBaseUrl(appBaseUrl))
                .path("/admin/configuracoes/pagamentos")
                .toUriString();
    }

    private String buildAbsoluteUrl(final String path) {
        final String normalizedBaseUrl = normalizeBaseUrl(appBaseUrl);
        if (normalizedBaseUrl.isBlank()) {
            return "";
        }
        return UriComponentsBuilder.fromUriString(normalizedBaseUrl)
                .path(path)
                .toUriString();
    }

    private boolean isMercadoPagoGatewaySelected() {
        return PROVIDER_NAME.equals(
                normalizeGatewayValue(
                        settings.getOrDefault(KEY_GATEWAY, PROVIDER_NAME)
                )
        );
    }

    private boolean isEligibleOnlineOrder(final PedidoEntity pedido) {
        if (pedido == null || pedido.getId() == null) {
            return false;
        }
        if (pedido.getTipoPagamento() == null) {
            return false;
        }
        return switch (pedido.getTipoPagamento()) {
            case PIX, BOLETO, CARTAO_CREDITO, CARTAO_DEBITO -> true;
            default -> false;
        };
    }

    private boolean isPaymentNotification(
            final String type,
            final String topic
    ) {
        final String normalizedType = text(type).toLowerCase(Locale.ROOT);
        final String normalizedTopic = text(topic).toLowerCase(Locale.ROOT);
        return "payment".equals(normalizedType)
                || "payment".equals(normalizedTopic)
                || normalizedType.startsWith("payment.")
                || normalizedTopic.startsWith("payment.");
    }

    private boolean isPaymentMethodDivergent(
            final PedidoEntity pedido,
            final MercadoPagoCheckoutClient.PaymentResponse payment
    ) {
        final String paymentType = text(payment.paymentTypeId())
                .toLowerCase(Locale.ROOT);
        final String paymentMethod = text(payment.paymentMethodId())
                .toLowerCase(Locale.ROOT);
        if (paymentType.isBlank() && paymentMethod.isBlank()) {
            return false;
        }
        return switch (pedido.getTipoPagamento()) {
            case PIX -> !PIX_PAYMENT_METHOD.equals(paymentMethod)
                    && !PIX_PAYMENT_TYPE.equals(paymentType);
            case BOLETO -> !"ticket".equals(paymentType);
            case CARTAO_CREDITO -> !"credit_card".equals(paymentType);
            case CARTAO_DEBITO -> !"debit_card".equals(paymentType);
            default -> false;
        };
    }

    private String buildReceivedPaymentLabel(
            final MercadoPagoCheckoutClient.PaymentResponse payment
    ) {
        final String paymentType = text(payment.paymentTypeId());
        final String paymentMethod = text(payment.paymentMethodId());
        if (paymentType.isBlank()) {
            return truncate(paymentMethod, 30);
        }
        if (paymentMethod.isBlank()) {
            return truncate(paymentType, 30);
        }
        return truncate(paymentType + ":" + paymentMethod, 30);
    }

    private String normalizeBaseUrl(final String value) {
        final String normalized = text(value);
        if (normalized.endsWith("/")) {
            return normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private boolean isPendingPaymentStatus(final PedidoEntity pedido) {
        return pedido != null
                && (pedido.getStatus() == StatusPedido.AGUARDANDO_PAGAMENTO
                || pedido.getStatus() == StatusPedido.ABERTO);
    }

    private MercadoPagoCheckoutClient.PreferencePayer buildPayer(
            final CheckoutRequest checkoutRequest,
            final PedidoEntity pedido
    ) {
        final String payerEmail = firstNonBlank(
                checkoutRequest == null ? "" : checkoutRequest.payerEmail(),
                pedido != null && pedido.getCliente() != null
                        ? pedido.getCliente().getEmail()
                        : ""
        );
        if (text(payerEmail).isBlank()) {
            throw new IllegalStateException(
                    "Informe um e-mail valido para gerar o Pix do Mercado Pago."
            );
        }
        return new MercadoPagoCheckoutClient.PreferencePayer(
                firstNonBlank(
                        checkoutRequest == null ? "" : checkoutRequest.payerName(),
                        pedido != null && pedido.getCliente() != null
                                ? pedido.getCliente().getNome()
                                : ""
                ),
                payerEmail,
                normalizeCpf(
                        firstNonBlank(
                                checkoutRequest == null ? "" : checkoutRequest.payerCpf(),
                                pedido != null && pedido.getCliente() != null
                                        ? pedido.getCliente().getCpf()
                                        : ""
                        )
                )
        );
    }

    private boolean isPixOrder(final PedidoEntity pedido) {
        return pedido != null && pedido.getTipoPagamento() == TipoPagamento.PIX;
    }

    private boolean hasPixPresentation(final PedidoEntity pedido) {
        return !text(pedido == null ? null : pedido.getGatewayPixQrCode()).isBlank()
                || !text(pedido == null ? null : pedido.getGatewayPixQrCodeBase64()).isBlank()
                || !text(pedido == null ? null : pedido.getGatewayPaymentTicketUrl()).isBlank();
    }

    private CheckoutPreferenceResult buildCheckoutResultFromPedido(
            final PedidoEntity pedido
    ) {
        return new CheckoutPreferenceResult(
                pedido.getGatewayCheckoutUrl(),
                pedido.getGatewayPreferenceId(),
                pedido.getGatewayPaymentTicketUrl(),
                pedido.getGatewayPixQrCode(),
                pedido.getGatewayPixQrCodeBase64()
        );
    }

    private String buildPixDescription(final PedidoEntity pedido) {
        return "Pedido #" + pedido.getId() + " - RedeMaisFarma";
    }

    private BigDecimal safeTransactionAmount(final PedidoEntity pedido) {
        final BigDecimal amount = pedido == null ? null : pedido.getTotal();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "O pedido precisa ter um valor valido para gerar o Pix."
            );
        }
        return amount;
    }

    private String buildPixIdempotencyKey(
            final String ownerReference,
            final String externalReference
    ) {
        final String seed = text(ownerReference) + ":" + text(externalReference) + ":pix";
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8))
                .toString();
    }

    private String normalizeCpf(final String value) {
        return text(value).replaceAll("\\D", "");
    }

    private String normalizeTenantId(final String value) {
        return text(value).toLowerCase(Locale.ROOT);
    }

    public static String normalizeGatewayValue(final String value) {
        final String trimmed = value == null ? "" : value.trim();
        if (trimmed.isBlank()) {
            return "";
        }
        final String compact = trimmed
                .toLowerCase(Locale.ROOT)
                .replace("_", "")
                .replace("-", "")
                .replace(" ", "");
        if (PROVIDER_NAME.equals(compact)) {
            return PROVIDER_NAME;
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }

    private String truncate(final String value, final int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private LocalDateTime toLocalDateTime(final OffsetDateTime value) {
        return value == null ? null : value.toLocalDateTime();
    }

    private LocalDateTime firstNonNull(
            final LocalDateTime first,
            final LocalDateTime second
    ) {
        return first != null ? first : second;
    }

    private String firstNonBlank(final String first, final String second) {
        return text(first).isBlank() ? text(second) : first.trim();
    }

    private String text(final Object value) {
        return value == null ? "" : value.toString().trim();
    }

    public record CheckoutRequest(
            String payerName,
            String payerEmail,
            String payerCpf,
            String tenantId,
            String ownerReference
    ) {
        public static CheckoutRequest fromCliente(final ClienteEntity cliente) {
            if (cliente == null) {
                return empty();
            }
            return new CheckoutRequest(
                    cliente.getNome(),
                    cliente.getEmail(),
                    cliente.getCpf(),
                    "",
                    ""
            );
        }

        public static CheckoutRequest empty() {
            return new CheckoutRequest("", "", "", "", "");
        }
    }

    public record CheckoutPreferenceResult(
            String checkoutUrl,
            String preferenceId,
            String pixTicketUrl,
            String pixQrCode,
            String pixQrCodeBase64
    ) {
    }

    public record SmokeTestResult(
            String ownerReference,
            String sellerUserId,
            boolean liveMode,
            String preferenceId,
            String checkoutUrl,
            String notificationUrl
    ) {
        public boolean webhookConfigured() {
            return notificationUrl != null && !notificationUrl.isBlank();
        }
    }

    public record PaymentSyncResult(
            Long pedidoId,
            String paymentId,
            String paymentStatus,
            boolean updated
    ) {
        public static PaymentSyncResult updated(
                final Long pedidoId,
                final String paymentId,
                final String paymentStatus
        ) {
            return new PaymentSyncResult(
                    pedidoId,
                    paymentId,
                    paymentStatus,
                    true
            );
        }

        public static PaymentSyncResult ignored() {
            return new PaymentSyncResult(null, "", "", false);
        }
    }
}
