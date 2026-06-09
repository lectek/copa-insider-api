package br.com.lectek.copainsider.domain.financeiro.mercadopago;

import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MercadoPagoOAuthServiceTest {

    @Mock
    private AppSettingService settings;

    @Mock
    private MercadoPagoSellerConnectionRepository repository;

    @Mock
    private MercadoPagoOAuthClient oauthClient;

    private MercadoPagoOAuthService service;

    @BeforeEach
    void setUp() {
        service = new MercadoPagoOAuthService(
                settings,
                repository,
                oauthClient,
                new ObjectMapper()
        );

        lenient().when(settings.getOrDefault(anyString(), anyString()))
                .thenAnswer(invocation -> invocation.getArgument(1));
        lenient().when(settings.getOrDefault("pg.mp.client_id", ""))
                .thenReturn("client-id");
        lenient().when(settings.getOrDefault("pg.mp.client_secret", ""))
                .thenReturn("client-secret");
        lenient().when(settings.getOrDefault("pg.mp.redirect_uri", ""))
                .thenReturn("https://api.exemplo.com/admin/configuracoes/pagamentos/mercadopago/oauth/callback");
    }

    @Test
    void buildAuthorizationUrlStoresOauthStateInSession() {
        HttpSession session = new MockHttpSession();

        String redirectUrl = service.buildAuthorizationUrl("loja-centro", session);

        Assertions.assertThat(redirectUrl)
                .contains("https://auth.mercadopago.com/authorization")
                .contains("client_id=client-id")
                .contains("response_type=code")
                .contains("platform_id=mp")
                .contains("redirect_uri=https://api.exemplo.com/admin/configuracoes/pagamentos/mercadopago/oauth/callback")
                .contains("code_challenge_method=S256");
        Assertions.assertThat(session.getAttribute("mercadopago.oauth.state"))
                .isNotNull();
        Assertions.assertThat(session.getAttribute("mercadopago.oauth.owner_reference"))
                .isEqualTo("loja-centro");
        Assertions.assertThat(session.getAttribute("mercadopago.oauth.code_verifier"))
                .isNotNull();
    }

    @Test
    void handleCallbackPersistsConnectionForOwnerReference() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("mercadopago.oauth.state", "state-123");
        session.setAttribute(
                "mercadopago.oauth.owner_reference",
                "tenant-001"
        );
        session.setAttribute(
                "mercadopago.oauth.code_verifier",
                "verifier-123"
        );
        when(repository.findByOwnerReferenceIgnoreCase("tenant-001"))
                .thenReturn(Optional.empty());
        when(oauthClient.exchangeAuthorizationCode(
                "client-id",
                "client-secret",
                "https://api.exemplo.com/admin/configuracoes/pagamentos/mercadopago/oauth/callback",
                "oauth-code",
                "verifier-123"
        )).thenReturn(new MercadoPagoOAuthClient.TokenResponse(
                "access-token",
                "refresh-token",
                "123456789",
                "APP_USR-public",
                "bearer",
                "offline_access read write",
                true,
                3600L,
                Map.of("user_id", 123456789)
        ));
        when(repository.save(any(MercadoPagoSellerConnection.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MercadoPagoOAuthService.CallbackResult result = service.handleCallback(
                "oauth-code",
                "state-123",
                null,
                null,
                session
        );

        ArgumentCaptor<MercadoPagoSellerConnection> captor =
                ArgumentCaptor.forClass(MercadoPagoSellerConnection.class);
        verify(repository).save(captor.capture());
        MercadoPagoSellerConnection saved = captor.getValue();
        Assertions.assertThat(saved.getOwnerReference()).isEqualTo("tenant-001");
        Assertions.assertThat(saved.getSellerUserId()).isEqualTo("123456789");
        Assertions.assertThat(saved.getAccessToken()).isEqualTo("access-token");
        Assertions.assertThat(saved.getRefreshToken()).isEqualTo("refresh-token");
        Assertions.assertThat(saved.getStatus())
                .isEqualTo(MercadoPagoConnectionStatus.CONNECTED);
        Assertions.assertThat(saved.getExpiresAt()).isAfter(OffsetDateTime.now());
        Assertions.assertThat(result.ownerReference()).isEqualTo("tenant-001");
        Assertions.assertThat(result.sellerUserId()).isEqualTo("123456789");
        Assertions.assertThat(session.getAttribute("mercadopago.oauth.state"))
                .isNull();
    }

    @Test
    void handleCallbackRejectsInvalidState() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("mercadopago.oauth.state", "state-ok");
        session.setAttribute("mercadopago.oauth.owner_reference", "tenant-001");
        session.setAttribute("mercadopago.oauth.code_verifier", "verifier-123");

        Assertions.assertThatThrownBy(() -> service.handleCallback(
                        "oauth-code",
                        "wrong-state",
                        null,
                        null,
                        session
                ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Fluxo OAuth invalido");
        verify(oauthClient, never()).exchangeAuthorizationCode(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
        );
    }

    @Test
    void disconnectMarksConnectionAsDisconnected() {
        MercadoPagoSellerConnection connection = new MercadoPagoSellerConnection();
        connection.setId(5L);
        connection.setOwnerReference("tenant-001");
        connection.setSellerUserId("123");
        connection.setAccessToken("token");
        connection.setRefreshToken("refresh");
        connection.setStatus(MercadoPagoConnectionStatus.CONNECTED);
        when(repository.findById(5L)).thenReturn(Optional.of(connection));

        service.disconnect(5L);

        Assertions.assertThat(connection.getStatus())
                .isEqualTo(MercadoPagoConnectionStatus.DISCONNECTED);
        Assertions.assertThat(connection.getAccessToken()).isEmpty();
        Assertions.assertThat(connection.getRefreshToken()).isEmpty();
        Assertions.assertThat(connection.getRevokedAt()).isNotNull();
        verify(repository).save(connection);
    }

    @Test
    void markWebhookReceivedPersistsWebhookTimestamp() {
        MercadoPagoSellerConnection connection = new MercadoPagoSellerConnection();
        connection.setId(8L);
        connection.setOwnerReference("loja-centro");
        connection.setSellerUserId("123456789");
        connection.setStatus(MercadoPagoConnectionStatus.CONNECTED);
        when(repository.findBySellerUserId("123456789"))
                .thenReturn(Optional.of(connection));

        service.markWebhookReceived("123456789", "pay-789");

        Assertions.assertThat(connection.getLastWebhookReceivedAt()).isNotNull();
        Assertions.assertThat(connection.getLastWebhookPaymentId())
                .isEqualTo("pay-789");
        verify(repository).save(connection);
    }

    @Test
    void listConnectionsExposesWebhookReceiptStatus() {
        MercadoPagoSellerConnection connection = new MercadoPagoSellerConnection();
        connection.setId(8L);
        connection.setOwnerReference("loja-centro");
        connection.setSellerUserId("123456789");
        connection.setStatus(MercadoPagoConnectionStatus.CONNECTED);
        connection.setConnectedAt(OffsetDateTime.parse("2026-03-18T10:00:00-03:00"));
        connection.setLastWebhookReceivedAt(
                OffsetDateTime.parse("2026-03-18T10:06:00-03:00")
        );
        when(repository.findAllByOrderByUpdatedAtDesc())
                .thenReturn(List.of(connection));

        MercadoPagoOAuthService.ConnectionView view =
                service.listConnections().get(0);

        Assertions.assertThat(view.webhookReceived()).isTrue();
        Assertions.assertThat(view.lastWebhookReceivedAtLabel())
                .isEqualTo("18/03/2026 10:06");
    }
}
