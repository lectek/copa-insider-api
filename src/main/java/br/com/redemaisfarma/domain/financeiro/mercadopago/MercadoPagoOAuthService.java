package br.com.redemaisfarma.domain.financeiro.mercadopago;

import br.com.redemaisfarma.application.core.settings.AppSettingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class MercadoPagoOAuthService {

    public static final String KEY_CLIENT_ID = "pg.mp.client_id";
    public static final String KEY_CLIENT_SECRET = "pg.mp.client_secret";
    public static final String KEY_REDIRECT_URI = "pg.mp.redirect_uri";

    private static final String AUTHORIZATION_ENDPOINT =
            "https://auth.mercadopago.com/authorization";
    private static final String SESSION_STATE =
            "mercadopago.oauth.state";
    private static final String SESSION_OWNER_REFERENCE =
            "mercadopago.oauth.owner_reference";
    private static final String SESSION_CODE_VERIFIER =
            "mercadopago.oauth.code_verifier";
    private static final int OWNER_REFERENCE_MAX_LENGTH = 120;
    private static final int STATE_BYTE_LENGTH = 24;
    private static final int VERIFIER_BYTE_LENGTH = 64;
    private static final long TOKEN_REFRESH_THRESHOLD_MINUTES = 5L;
    private static final DateTimeFormatter VIEW_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR"));

    private final AppSettingService settings;
    private final MercadoPagoSellerConnectionRepository repository;
    private final MercadoPagoOAuthClient oauthClient;
    private final ObjectMapper objectMapper;
    private final SecureRandom secureRandom;

    public MercadoPagoOAuthService(
            final AppSettingService appSettingService,
            final MercadoPagoSellerConnectionRepository repositoryValue,
            final MercadoPagoOAuthClient oauthClientValue,
            final ObjectMapper objectMapperValue
    ) {
        this.settings = appSettingService;
        this.repository = repositoryValue;
        this.oauthClient = oauthClientValue;
        this.objectMapper = objectMapperValue;
        this.secureRandom = new SecureRandom();
    }

    @Transactional(readOnly = true)
    public boolean isReady() {
        final AppCredentials credentials = loadCredentials();
        return credentials.isConfigured();
    }

    @Transactional(readOnly = true)
    public List<ConnectionView> listConnections() {
        return repository.findAllByOrderByUpdatedAtDesc()
                .stream()
                .map(this::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public WebhookReceiptView getWebhookReceipt(final String ownerReference) {
        final String normalizedOwnerReference = text(ownerReference);
        if (normalizedOwnerReference.isBlank()) {
            return WebhookReceiptView.empty();
        }
        return repository.findByOwnerReferenceIgnoreCase(normalizedOwnerReference)
                .map(this::toWebhookReceiptView)
                .orElseGet(WebhookReceiptView::empty);
    }

    @Transactional(readOnly = true)
    public boolean hasActiveConnection(final String ownerReference) {
        final String normalizedOwnerReference = text(ownerReference);
        if (normalizedOwnerReference.isBlank()) {
            return false;
        }
        return repository.findByOwnerReferenceIgnoreCase(normalizedOwnerReference)
                .filter(connection ->
                        connection.getStatus()
                                == MercadoPagoConnectionStatus.CONNECTED
                )
                .isPresent();
    }

    public String buildAuthorizationUrl(
            final String ownerReference,
            final HttpSession session
    ) {
        final AppCredentials credentials = requireCredentials();
        final String normalizedOwnerReference =
                normalizeOwnerReference(ownerReference);
        final String state = randomToken(STATE_BYTE_LENGTH);
        final String codeVerifier = randomToken(VERIFIER_BYTE_LENGTH);
        final String codeChallenge = buildCodeChallenge(codeVerifier);

        session.setAttribute(SESSION_STATE, state);
        session.setAttribute(SESSION_OWNER_REFERENCE, normalizedOwnerReference);
        session.setAttribute(SESSION_CODE_VERIFIER, codeVerifier);

        return UriComponentsBuilder.fromUriString(AUTHORIZATION_ENDPOINT)
                .queryParam("client_id", credentials.clientId())
                .queryParam("response_type", "code")
                .queryParam("platform_id", "mp")
                .queryParam("state", state)
                .queryParam("redirect_uri", credentials.redirectUri())
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", "S256")
                .build(true)
                .toUriString();
    }

    @Transactional
    public CallbackResult handleCallback(
            final String code,
            final String state,
            final String error,
            final String errorDescription,
            final HttpSession session
    ) {
        if (error != null && !error.isBlank()) {
            clearSession(session);
            final String suffix = errorDescription == null
                    || errorDescription.isBlank()
                    ? ""
                    : ": " + errorDescription.trim();
            throw new IllegalArgumentException(
                    "Mercado Pago nao concluiu a autorizacao" + suffix
            );
        }

        final String sessionState = text(session.getAttribute(SESSION_STATE));
        final String ownerReference = text(
                session.getAttribute(SESSION_OWNER_REFERENCE)
        );
        final String codeVerifier = text(
                session.getAttribute(SESSION_CODE_VERIFIER)
        );
        clearSession(session);

        if (sessionState.isBlank() || !sessionState.equals(state)) {
            throw new IllegalArgumentException(
                    "Fluxo OAuth invalido ou expirado. Inicie a conexao novamente."
            );
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException(
                    "Mercado Pago nao retornou o codigo de autorizacao."
            );
        }

        final AppCredentials credentials = requireCredentials();
        final MercadoPagoOAuthClient.TokenResponse tokenResponse =
                oauthClient.exchangeAuthorizationCode(
                        credentials.clientId(),
                        credentials.clientSecret(),
                        credentials.redirectUri(),
                        code.trim(),
                        codeVerifier
                );

        final MercadoPagoSellerConnection connection = repository
                .findByOwnerReferenceIgnoreCase(ownerReference)
                .orElseGet(MercadoPagoSellerConnection::new);

        connection.setOwnerReference(ownerReference);
        applyTokenResponse(connection, tokenResponse, true);
        repository.save(connection);

        return new CallbackResult(
                ownerReference,
                tokenResponse.sellerUserId(),
                connection.getExpiresAt() == null
                        ? ""
                        : formatDateTime(connection.getExpiresAt())
        );
    }

    @Transactional
    public void disconnect(final Long id) {
        final MercadoPagoSellerConnection connection = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Conexao Mercado Pago nao encontrada: id=" + id
                ));
        connection.setAccessToken("");
        connection.setRefreshToken("");
        connection.setStatus(MercadoPagoConnectionStatus.DISCONNECTED);
        connection.setRevokedAt(OffsetDateTime.now());
        repository.save(connection);
    }

    @Transactional
    public void markWebhookReceived(
            final String sellerUserId,
            final String paymentId
    ) {
        final String normalizedSellerUserId = text(sellerUserId);
        if (normalizedSellerUserId.isBlank()) {
            return;
        }
        repository.findBySellerUserId(normalizedSellerUserId)
                .ifPresent(connection -> {
                    connection.setLastWebhookReceivedAt(OffsetDateTime.now());
                    connection.setLastWebhookPaymentId(text(paymentId));
                    repository.save(connection);
                });
    }

    @Transactional
    public AuthorizedConnection requireAuthorizedConnection(
            final String ownerReference
    ) {
        final MercadoPagoSellerConnection connection = repository
                .findByOwnerReferenceIgnoreCase(normalizeOwnerReference(ownerReference))
                .orElseThrow(() -> new NoSuchElementException(
                        "Nao existe conta Mercado Pago conectada para "
                                + ownerReference
                                + "."
                ));
        ensureUsableConnection(connection);
        refreshAccessTokenIfNecessary(connection);
        return toAuthorizedConnection(connection);
    }

    @Transactional
    public AuthorizedConnection requireAuthorizedConnectionBySellerUserId(
            final String sellerUserId
    ) {
        final String normalizedSellerUserId = text(sellerUserId);
        if (normalizedSellerUserId.isBlank()) {
            throw new IllegalArgumentException(
                    "Seller user id do Mercado Pago nao informado."
            );
        }
        final MercadoPagoSellerConnection connection = repository
                .findBySellerUserId(normalizedSellerUserId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Nao existe conta Mercado Pago conectada para seller "
                                + normalizedSellerUserId
                                + "."
                ));
        ensureUsableConnection(connection);
        refreshAccessTokenIfNecessary(connection);
        return toAuthorizedConnection(connection);
    }

    private ConnectionView toView(final MercadoPagoSellerConnection connection) {
        final OffsetDateTime now = OffsetDateTime.now();
        final String statusLabel;
        if (connection.getStatus() == MercadoPagoConnectionStatus.DISCONNECTED) {
            statusLabel = "Desconectada";
        } else if (connection.getExpiresAt() != null
                && connection.getExpiresAt().isBefore(now)) {
            statusLabel = "Token expirado";
        } else {
            statusLabel = "Conectada";
        }

        return new ConnectionView(
                connection.getId(),
                connection.getOwnerReference(),
                connection.getSellerUserId(),
                connection.getSellerNickname(),
                connection.isLiveMode() ? "Producao" : "Teste",
                statusLabel,
                formatDateTime(connection.getConnectedAt()),
                formatDateTime(connection.getExpiresAt()),
                formatDateTime(connection.getUpdatedAt()),
                connection.getLastWebhookReceivedAt() != null,
                formatDateTime(connection.getLastWebhookReceivedAt())
        );
    }

    private WebhookReceiptView toWebhookReceiptView(
            final MercadoPagoSellerConnection connection
    ) {
        return new WebhookReceiptView(
                connection.getLastWebhookReceivedAt() != null,
                formatDateTime(connection.getLastWebhookReceivedAt()),
                text(connection.getLastWebhookPaymentId())
        );
    }

    private AuthorizedConnection toAuthorizedConnection(
            final MercadoPagoSellerConnection connection
    ) {
        return new AuthorizedConnection(
                connection.getOwnerReference(),
                connection.getSellerUserId(),
                connection.getAccessToken(),
                connection.isLiveMode()
        );
    }

    private AppCredentials requireCredentials() {
        final AppCredentials credentials = loadCredentials();
        if (!credentials.isConfigured()) {
            throw new IllegalStateException(
                    "Configure Client ID, Client Secret e Redirect URI do Mercado Pago antes de conectar uma conta."
            );
        }
        return credentials;
    }

    private void ensureUsableConnection(
            final MercadoPagoSellerConnection connection
    ) {
        if (connection.getStatus() != MercadoPagoConnectionStatus.CONNECTED) {
            throw new IllegalStateException(
                    "A conta Mercado Pago "
                            + connection.getOwnerReference()
                            + " esta desconectada."
            );
        }
        if (text(connection.getAccessToken()).isBlank()) {
            throw new IllegalStateException(
                    "A conta Mercado Pago "
                            + connection.getOwnerReference()
                            + " nao possui access token valido."
            );
        }
    }

    private void refreshAccessTokenIfNecessary(
            final MercadoPagoSellerConnection connection
    ) {
        if (!shouldRefresh(connection)) {
            return;
        }
        final String refreshToken = text(connection.getRefreshToken());
        if (refreshToken.isBlank()) {
            return;
        }
        final AppCredentials credentials = requireCredentials();
        final MercadoPagoOAuthClient.TokenResponse tokenResponse =
                oauthClient.refreshAccessToken(
                        credentials.clientId(),
                        credentials.clientSecret(),
                        refreshToken
                );
        applyTokenResponse(connection, tokenResponse, false);
        repository.save(connection);
    }

    private boolean shouldRefresh(final MercadoPagoSellerConnection connection) {
        if (connection.getExpiresAt() == null) {
            return false;
        }
        final OffsetDateTime threshold = OffsetDateTime.now()
                .plus(TOKEN_REFRESH_THRESHOLD_MINUTES, ChronoUnit.MINUTES);
        return !connection.getExpiresAt().isAfter(threshold);
    }

    private void applyTokenResponse(
            final MercadoPagoSellerConnection connection,
            final MercadoPagoOAuthClient.TokenResponse tokenResponse,
            final boolean resetConnectionTimestamp
    ) {
        connection.setSellerUserId(
                text(tokenResponse.sellerUserId()).isBlank()
                        ? connection.getSellerUserId()
                        : tokenResponse.sellerUserId()
        );
        connection.setAccessToken(tokenResponse.accessToken());
        connection.setRefreshToken(
                text(tokenResponse.refreshToken()).isBlank()
                        ? connection.getRefreshToken()
                        : tokenResponse.refreshToken()
        );
        connection.setPublicKey(
                text(tokenResponse.publicKey()).isBlank()
                        ? connection.getPublicKey()
                        : tokenResponse.publicKey()
        );
        connection.setTokenType(tokenResponse.tokenType());
        connection.setScope(tokenResponse.scope());
        connection.setLiveMode(tokenResponse.liveMode());
        connection.setStatus(MercadoPagoConnectionStatus.CONNECTED);
        if (resetConnectionTimestamp || connection.getConnectedAt() == null) {
            connection.setConnectedAt(OffsetDateTime.now());
        }
        if (resetConnectionTimestamp) {
            connection.setLastWebhookReceivedAt(null);
            connection.setLastWebhookPaymentId("");
        }
        connection.setRevokedAt(null);
        connection.setExpiresAt(resolveExpiration(tokenResponse.expiresInSeconds()));
        connection.setRawResponse(serialize(tokenResponse.rawPayload()));
    }

    private AppCredentials loadCredentials() {
        return new AppCredentials(
                settings.getOrDefault(KEY_CLIENT_ID, "").trim(),
                settings.getOrDefault(KEY_CLIENT_SECRET, "").trim(),
                settings.getOrDefault(KEY_REDIRECT_URI, "").trim()
        );
    }

    private String normalizeOwnerReference(final String rawValue) {
        final String value = rawValue == null ? "" : rawValue.trim();
        if (value.isBlank()) {
            throw new IllegalArgumentException(
                    "Informe um identificador para a conta que sera conectada."
            );
        }
        if (value.length() > OWNER_REFERENCE_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "O identificador da conta pode ter no maximo 120 caracteres."
            );
        }
        return value;
    }

    private OffsetDateTime resolveExpiration(final long expiresInSeconds) {
        if (expiresInSeconds <= 0L) {
            return null;
        }
        return OffsetDateTime.now().plusSeconds(expiresInSeconds);
    }

    private String serialize(final Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(
                    payload == null ? Map.of() : payload
            );
        } catch (Exception ex) {
            return "";
        }
    }

    private String randomToken(final int byteLength) {
        final byte[] bytes = new byte[byteLength];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String buildCodeChallenge(final String codeVerifier) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(
                    codeVerifier.getBytes(StandardCharsets.US_ASCII)
            );
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Nao foi possivel gerar o PKCE para o Mercado Pago.",
                    ex
            );
        }
    }

    private void clearSession(final HttpSession session) {
        session.removeAttribute(SESSION_STATE);
        session.removeAttribute(SESSION_OWNER_REFERENCE);
        session.removeAttribute(SESSION_CODE_VERIFIER);
    }

    private String formatDateTime(final OffsetDateTime value) {
        if (value == null) {
            return "-";
        }
        return VIEW_TIME_FORMATTER.format(value);
    }

    private String text(final Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private record AppCredentials(
            String clientId,
            String clientSecret,
            String redirectUri
    ) {
        private boolean isConfigured() {
            return clientId != null
                    && !clientId.isBlank()
                    && clientSecret != null
                    && !clientSecret.isBlank()
                    && redirectUri != null
                    && !redirectUri.isBlank();
        }
    }

    public record CallbackResult(
            String ownerReference,
            String sellerUserId,
            String expiresAtLabel
    ) {
    }

    public record ConnectionView(
            Long id,
            String ownerReference,
            String sellerUserId,
            String sellerNickname,
            String environment,
            String statusLabel,
            String connectedAtLabel,
            String expiresAtLabel,
            String updatedAtLabel,
            boolean webhookReceived,
            String lastWebhookReceivedAtLabel
    ) {
    }

    public record WebhookReceiptView(
            boolean received,
            String lastReceivedAtLabel,
            String paymentId
    ) {
        public static WebhookReceiptView empty() {
            return new WebhookReceiptView(false, "-", "");
        }
    }

    public record AuthorizedConnection(
            String ownerReference,
            String sellerUserId,
            String accessToken,
            boolean liveMode
    ) {
    }
}
