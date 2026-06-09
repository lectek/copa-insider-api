package br.com.redemaisfarma.domain.financeiro.mercadopago;

import java.util.Map;

public interface MercadoPagoOAuthClient {

    TokenResponse exchangeAuthorizationCode(
            String clientId,
            String clientSecret,
            String redirectUri,
            String authorizationCode,
            String codeVerifier
    );

    TokenResponse refreshAccessToken(
            String clientId,
            String clientSecret,
            String refreshToken
    );

    record TokenResponse(
            String accessToken,
            String refreshToken,
            String sellerUserId,
            String publicKey,
            String tokenType,
            String scope,
            boolean liveMode,
            long expiresInSeconds,
            Map<String, Object> rawPayload
    ) {
    }
}
