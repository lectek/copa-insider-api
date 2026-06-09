package br.com.lectek.copainsider.domain.financeiro.mercadopago;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import br.com.lectek.copainsider.domain.enums.StatusPedido;
import br.com.lectek.copainsider.domain.enums.TipoPagamento;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MercadoPagoCheckoutServiceTest {

    @Mock
    private AppSettingService settings;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private MercadoPagoOAuthService oauthService;

    @Mock
    private MercadoPagoCheckoutClient checkoutClient;

    private MercadoPagoCheckoutService service;

    @BeforeEach
    void setUp() {
        service = new MercadoPagoCheckoutService(
                settings,
                pedidoRepository,
                oauthService,
                checkoutClient,
                "https://api.exemplo.com"
        );

        lenient().when(settings.getOrDefault(anyString(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        lenient().when(settings.getOrDefault("pg.gateway", "mercadopago"))
                .thenReturn("mercadopago");
        lenient().when(settings.getOrDefault("pg.mp.owner_reference_default", ""))
                .thenReturn("loja-centro");
        lenient().when(settings.getOrDefault("pg.webhook_url", ""))
                .thenReturn("https://api.exemplo.com/webhooks/mercadopago");
        lenient().when(pedidoRepository.save(any(PedidoEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(oauthService.requireAuthorizedConnection("loja-centro"))
                .thenReturn(new MercadoPagoOAuthService.AuthorizedConnection(
                        "loja-centro",
                        "123456789",
                        "access-token",
                        true
                ));
    }

    @Test
    void createCheckoutForPedidoPersistsGatewayFields() {
        PedidoEntity pedido = pedido();
        when(checkoutClient.createPreference(anyString(), any()))
                .thenReturn(new MercadoPagoCheckoutClient.PreferenceResponse(
                        "pref-123",
                        "https://mercadopago.com/pay",
                        "https://sandbox.mercadopago.com/pay",
                        Map.of("id", "pref-123")
                ));

        MercadoPagoCheckoutService.CheckoutPreferenceResult result =
                service.createCheckoutForPedido(
                        pedido,
                        new MercadoPagoCheckoutService.CheckoutRequest(
                                "Cliente Teste",
                                "cliente@example.com",
                                "12345678901",
                                "",
                                ""
                        )
                );

        Assertions.assertThat(result.preferenceId()).isEqualTo("pref-123");
        Assertions.assertThat(result.checkoutUrl())
                .isEqualTo("https://mercadopago.com/pay");
        Assertions.assertThat(pedido.getGatewayProvider())
                .isEqualTo("mercadopago");
        Assertions.assertThat(pedido.getGatewayOwnerReference())
                .isEqualTo("loja-centro");
        Assertions.assertThat(pedido.getGatewayPreferenceId())
                .isEqualTo("pref-123");
        Assertions.assertThat(pedido.getGatewayExternalReference())
                .isEqualTo("pedido:10");
        Assertions.assertThat(pedido.getGatewayCheckoutUrl())
                .isEqualTo("https://mercadopago.com/pay");
        verify(pedidoRepository, times(2)).save(pedido);
    }

    @Test
    void assertReadyForOnlineCheckoutAcceptsLegacyGatewayAlias() {
        when(settings.getOrDefault("pg.gateway", "mercadopago"))
                .thenReturn("mercado_pago");

        service.assertReadyForOnlineCheckout();

        verify(oauthService).requireAuthorizedConnection("loja-centro");
    }

    @Test
    void syncPaymentForPedidoMarksPedidoAsPaidWhenApproved() {
        PedidoEntity pedido = pedido();
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        pedido.setGatewayOwnerReference("loja-centro");
        when(checkoutClient.fetchPayment("access-token", "pay-123"))
                .thenReturn(new MercadoPagoCheckoutClient.PaymentResponse(
                        "pay-123",
                        "pedido:10",
                        "approved",
                        "accredited",
                        "pix",
                        "bank_transfer",
                        OffsetDateTime.parse("2026-03-14T10:15:00-03:00"),
                        OffsetDateTime.parse("2026-03-14T10:15:03-03:00"),
                        null,
                        null,
                        null,
                        Map.of("id", "pay-123")
                ));

        MercadoPagoCheckoutService.PaymentSyncResult result =
                service.syncPaymentForPedido(pedido, "pay-123");

        Assertions.assertThat(result.updated()).isTrue();
        Assertions.assertThat(pedido.getStatus()).isEqualTo(StatusPedido.PAGO);
        Assertions.assertThat(pedido.getGatewayPaymentId()).isEqualTo("pay-123");
        Assertions.assertThat(pedido.getGatewayPaymentStatus()).isEqualTo("approved");
        Assertions.assertThat(pedido.getGatewayPaymentStatusDetail())
                .isEqualTo("accredited");
        Assertions.assertThat(pedido.getPagamentoRecebidoEm())
                .isEqualTo(LocalDateTime.of(2026, 3, 14, 10, 15));
        Assertions.assertThat(pedido.getFormaPagamentoRecebida())
                .isEqualTo("bank_transfer:pix");
        Assertions.assertThat(pedido.isPagamentoDivergente()).isFalse();
    }

    @Test
    void handleWebhookNotificationUpdatesPedidoByExternalReference() {
        PedidoEntity pedido = pedido();
        pedido.setGatewayExternalReference("pedido:10");
        when(oauthService.requireAuthorizedConnectionBySellerUserId("123456789"))
                .thenReturn(new MercadoPagoOAuthService.AuthorizedConnection(
                        "loja-centro",
                        "123456789",
                        "access-token",
                        true
                ));
        when(checkoutClient.fetchPayment("access-token", "pay-456"))
                .thenReturn(new MercadoPagoCheckoutClient.PaymentResponse(
                        "pay-456",
                        "pedido:10",
                        "pending",
                        "pending_waiting_transfer",
                        "pix",
                        "bank_transfer",
                        null,
                        OffsetDateTime.parse("2026-03-14T11:00:00-03:00"),
                        null,
                        null,
                        null,
                        Map.of("id", "pay-456")
                ));
        when(pedidoRepository.findByGatewayPaymentId("pay-456"))
                .thenReturn(Optional.empty());
        when(pedidoRepository.findByGatewayExternalReference("pedido:10"))
                .thenReturn(Optional.of(pedido));

        MercadoPagoCheckoutService.PaymentSyncResult result =
                service.handleWebhookNotification(
                        "payment",
                        "payment",
                        "123456789",
                        "pay-456"
                );

        Assertions.assertThat(result.updated()).isTrue();
        Assertions.assertThat(result.pedidoId()).isEqualTo(10L);
        Assertions.assertThat(pedido.getGatewayPaymentStatus()).isEqualTo("pending");
        Assertions.assertThat(pedido.getStatus())
                .isEqualTo(StatusPedido.AGUARDANDO_PAGAMENTO);
        verify(oauthService).markWebhookReceived("123456789", "pay-456");
    }

    @Test
    void createCheckoutForPedidoUsesTenantMappingWhenProvided() {
        PedidoEntity pedido = pedido();
        when(settings.getOrDefault("pg.mp.owner_reference_by_tenant", ""))
                .thenReturn("tenant-a=loja-bairro");
        when(oauthService.requireAuthorizedConnection("loja-bairro"))
                .thenReturn(new MercadoPagoOAuthService.AuthorizedConnection(
                        "loja-bairro",
                        "987654321",
                        "tenant-token",
                        true
                ));
        when(checkoutClient.createPreference(eq("tenant-token"), any()))
                .thenReturn(new MercadoPagoCheckoutClient.PreferenceResponse(
                        "pref-tenant",
                        "https://mercadopago.com/pay/tenant",
                        "",
                        Map.of("id", "pref-tenant")
                ));

        MercadoPagoCheckoutService.CheckoutPreferenceResult result =
                service.createCheckoutForPedido(
                        pedido,
                        new MercadoPagoCheckoutService.CheckoutRequest(
                                "Cliente Tenant",
                                "tenant@example.com",
                                "98765432100",
                                "tenant-a",
                                ""
                        )
                );

        Assertions.assertThat(result.preferenceId()).isEqualTo("pref-tenant");
        Assertions.assertThat(pedido.getGatewayOwnerReference())
                .isEqualTo("loja-bairro");
        Assertions.assertThat(pedido.getGatewayCheckoutUrl())
                .isEqualTo("https://mercadopago.com/pay/tenant");
    }

    @Test
    void runCheckoutSmokeTestCreatesPreferenceForConnectedAccount() {
        when(checkoutClient.createPreference(eq("access-token"), any()))
                .thenReturn(new MercadoPagoCheckoutClient.PreferenceResponse(
                        "pref-smoke",
                        "https://mercadopago.com/pay/smoke",
                        "https://sandbox.mercadopago.com/pay/smoke",
                        Map.of("id", "pref-smoke")
                ));

        MercadoPagoCheckoutService.SmokeTestResult result =
                service.runCheckoutSmokeTest("loja-centro");

        Assertions.assertThat(result.ownerReference()).isEqualTo("loja-centro");
        Assertions.assertThat(result.sellerUserId()).isEqualTo("123456789");
        Assertions.assertThat(result.liveMode()).isTrue();
        Assertions.assertThat(result.preferenceId()).isEqualTo("pref-smoke");
        Assertions.assertThat(result.checkoutUrl())
                .isEqualTo("https://mercadopago.com/pay/smoke");
        Assertions.assertThat(result.notificationUrl())
                .isEqualTo("https://api.exemplo.com/webhooks/mercadopago");
        Assertions.assertThat(result.webhookConfigured()).isTrue();
    }

    @Test
    void hasNotificationUrlConfiguredReturnsFalseWhenBaseUrlIsNotHttps() {
        MercadoPagoCheckoutService localService = new MercadoPagoCheckoutService(
                settings,
                pedidoRepository,
                oauthService,
                checkoutClient,
                "http://localhost:8080"
        );
        when(settings.getOrDefault("pg.webhook_url", "")).thenReturn("");

        Assertions.assertThat(localService.hasNotificationUrlConfigured())
                .isFalse();
    }

    private PedidoEntity pedido() {
        ClienteEntity cliente = new ClienteEntity();
        cliente.setNome("Cliente Teste");
        cliente.setEmail("cliente@example.com");
        cliente.setCpf("12345678901");

        PedidoEntity pedido = new PedidoEntity();
        pedido.setId(10L);
        pedido.setCliente(cliente);
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        pedido.setTipoPagamento(TipoPagamento.PIX);
        pedido.setTotal(new BigDecimal("39.90"));
        return pedido;
    }
}
