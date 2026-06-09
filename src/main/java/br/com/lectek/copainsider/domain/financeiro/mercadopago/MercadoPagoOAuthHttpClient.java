package br.com.lectek.copainsider.domain.financeiro.mercadopago;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class MercadoPagoOAuthHttpClient implements MercadoPagoOAuthClient {

    private static final String TOKEN_ENDPOINT =
            "https://api.mercadopago.com/oauth/token";

    @Override
    public TokenResponse exchangeAuthorizationCode(
            final String clientId,
            final String clientSecret,
            final String redirectUri,
            final String authorizationCode,
            final String codeVerifier
    ) {
        final Map<String, Object> body = new LinkedHashMap<>();
        body.put("client_id", clientId);
        body.put("client_secret", clientSecret);
        body.put("grant_type", "authorization_code");
        body.put("code", authorizationCode);
        body.put("redirect_uri", redirectUri);
        if (codeVerifier != null && !codeVerifier.isBlank()) {
            body.put("code_verifier", codeVerifier);
        }

        try {
            @SuppressWarnings("unchecked")
            final Map<String, Object> response = RestClient.create()
                    .post()
                    .uri(TOKEN_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            return toTokenResponse(response);
        } catch (RestClientResponseException ex) {
            throw new IllegalStateException(
                    "Mercado Pago recusou a troca do codigo OAuth: HTTP "
                            + ex.getStatusCode().value(),
                    ex
            );
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Falha ao trocar o codigo OAuth no Mercado Pago.",
                    ex
            );
        }
    }

    @Override
    public TokenResponse refreshAccessToken(
            final String clientId,
            final String clientSecret,
            final String refreshToken
    ) {
        final Map<String, Object> body = new LinkedHashMap<>();
        body.put("client_id", clientId);
        body.put("client_secret", clientSecret);
        body.put("grant_type", "refresh_token");
        body.put("refresh_token", refreshToken);

        try {
            @SuppressWarnings("unchecked")
            final Map<String, Object> response = RestClient.create()
                    .post()
                    .uri(TOKEN_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
            return toTokenResponse(response);
        } catch (RestClientResponseException ex) {
            throw new IllegalStateException(
                    "Mercado Pago recusou a renovacao do token OAuth: HTTP "
                            + ex.getStatusCode().value(),
                    ex
            );
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Falha ao renovar o token OAuth no Mercado Pago.",
                    ex
            );
        }
    }

    private TokenResponse toTokenResponse(final Map<String, Object> response) {
        if (response == null || response.isEmpty()) {
            throw new IllegalStateException(
                    "Mercado Pago retornou uma resposta OAuth vazia."
            );
        }

        final String accessToken = text(response.get("access_token"));
        final String sellerUserId = text(response.get("user_id"));
        if (accessToken.isBlank() || sellerUserId.isBlank()) {
            throw new IllegalStateException(
                    "Mercado Pago nao retornou os campos essenciais da conexao."
            );
        }

        return new TokenResponse(
                accessToken,
                text(response.get("refresh_token")),
                sellerUserId,
                text(response.get("public_key")),
                text(response.get("token_type")),
                text(response.get("scope")),
                asBoolean(response.get("live_mode")),
                asLong(response.get("expires_in")),
                response
        );
    }

    private String text(final Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private boolean asBoolean(final Object value) {
        if (value instanceof Boolean boolValue) {
            return boolValue;
        }
        final String normalized = text(value).toLowerCase(Locale.ROOT);
        return "true".equals(normalized) || "1".equals(normalized);
    }

    private long asLong(final Object value) {
        if (value instanceof Number numberValue) {
            return numberValue.longValue();
        }
        try {
            return Long.parseLong(text(value));
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }
}
