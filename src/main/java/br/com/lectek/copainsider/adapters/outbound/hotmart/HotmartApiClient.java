package br.com.lectek.copainsider.adapters.outbound.hotmart;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Component
public class HotmartApiClient {

    private static final Logger log = LoggerFactory.getLogger(HotmartApiClient.class);

    private static final String TOKEN_URL =
            "https://api-sec-vlc.hotmart.com/security/oauth/token";
    private static final String PRODUCTS_URL =
            "https://developers.hotmart.com/payments/api/v1/products";

    @Value("${hotmart.api.client-id:}")
    private String clientId;

    @Value("${hotmart.api.client-secret:}")
    private String clientSecret;

    @Value("${hotmart.api.basic-token:}")
    private String basicToken;

    private final RestTemplate rest = new RestTemplate();

    public List<HotmartProduct> listarProdutos() {
        if (clientId.isBlank() || basicToken.isBlank()) {
            log.warn("[hotmart-api] credenciais não configuradas — sync ignorado");
            return Collections.emptyList();
        }

        String accessToken = obterToken();
        if (accessToken == null) return Collections.emptyList();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        try {
            ResponseEntity<ProductsResponse> resp = rest.exchange(
                    PRODUCTS_URL, HttpMethod.GET,
                    new HttpEntity<>(headers), ProductsResponse.class);

            if (resp.getBody() == null || resp.getBody().items() == null) {
                return Collections.emptyList();
            }
            return resp.getBody().items();
        } catch (Exception e) {
            log.error("[hotmart-api] erro ao listar produtos: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private String obterToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + basicToken);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        try {
            ResponseEntity<TokenResponse> resp = rest.exchange(
                    TOKEN_URL, HttpMethod.POST,
                    new HttpEntity<>(body, headers), TokenResponse.class);
            return resp.getBody() != null ? resp.getBody().accessToken() : null;
        } catch (Exception e) {
            log.error("[hotmart-api] erro ao obter token: {}", e.getMessage());
            return null;
        }
    }

    // ── response records ─────────────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TokenResponse(
            @JsonProperty("access_token") String accessToken
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProductsResponse(
            @JsonProperty("items") List<HotmartProduct> items
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record HotmartProduct(
            @JsonProperty("id") Long id,
            @JsonProperty("name") String name,
            @JsonProperty("checkout_url") String checkoutUrl
    ) {}
}
