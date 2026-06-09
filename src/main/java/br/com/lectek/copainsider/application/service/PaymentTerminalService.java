package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class PaymentTerminalService {

    private static final String KEY_POS_CONFIG = "pg.pos_config";
    private static final String DESC_POS_CONFIG = "Configuracao da maquineta";

    private static final String MODE_MOCK = "mock";
    private static final String MODE_WEBHOOK = "webhook";
    private static final Set<String> ALLOWED_MODES = Set.of(
            MODE_MOCK,
            MODE_WEBHOOK
    );

    private static final int DEFAULT_TIMEOUT_MS = 10000;
    private static final int MIN_TIMEOUT_MS = 1000;
    private static final int MAX_TIMEOUT_MS = 30000;

    private final AppSettingService settings;
    private final ObjectMapper objectMapper;

    public PaymentTerminalService(
            final AppSettingService appSettingService,
            final ObjectMapper objectMapperValue
    ) {
        this.settings = appSettingService;
        this.objectMapper = objectMapperValue;
    }

    @Transactional(readOnly = true)
    public TerminalConfig loadConfig() {
        return toPublicConfig(loadPayload());
    }

    @Transactional
    public TerminalConfig saveConfig(final TerminalConfigInput input) {
        if (input == null) {
            throw new IllegalArgumentException("Configuracao da maquineta obrigatoria.");
        }
        final StoredTerminalConfig current = loadPayload();
        final StoredTerminalConfig merged = mergeAndValidate(current, input);
        savePayload(merged);
        return toPublicConfig(merged);
    }

    @Transactional(readOnly = true)
    public TerminalPaymentResult authorize(final TerminalPaymentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Solicitacao de pagamento obrigatoria.");
        }
        if (request.amount() == null || request.amount().signum() <= 0) {
            throw new IllegalArgumentException("Valor da transacao deve ser maior que zero.");
        }

        final StoredTerminalConfig config = loadPayload();
        if (!config.enabled()) {
            return new TerminalPaymentResult(
                    true,
                    "manual",
                    null,
                    "Integracao da maquineta desativada."
            );
        }

        if (MODE_MOCK.equals(config.mode())) {
            return new TerminalPaymentResult(
                    true,
                    "approved",
                    "MOCK-" + UUID.randomUUID().toString().substring(0, 8)
                            .toUpperCase(Locale.ROOT),
                    "Pagamento aprovado no modo de teste."
            );
        }

        return authorizeByWebhook(config, request);
    }

    private TerminalPaymentResult authorizeByWebhook(
            final StoredTerminalConfig config,
            final TerminalPaymentRequest request
    ) {
        if (config.endpointUrl().isBlank()) {
            throw new IllegalArgumentException(
                    "Informe o endpoint de webhook da maquineta."
            );
        }

        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("amount", request.amount());
        payload.put("amountCents", request.amount().movePointRight(2).longValue());
        payload.put("paymentType", normalizeText(request.paymentType()));
        payload.put("reference", normalizeText(request.reference()));
        payload.put("source", normalizeText(request.source()));
        payload.put("provider", config.provider());
        payload.put("terminalId", config.terminalId());
        payload.put("merchantId", config.merchantId());
        payload.put("requestedAt", Instant.now().toString());
        payload.put("metadata", request.metadata() == null ? Map.of() : request.metadata());

        final RestClient client = restClient(config.timeoutMs());
        try {
            @SuppressWarnings("unchecked")
            final Map<String, Object> response = client.post()
                    .uri(config.endpointUrl())
                    .headers(headers -> {
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        if (!config.secret().isBlank()) {
                            headers.setBearerAuth(config.secret());
                        }
                        if (!config.terminalId().isBlank()) {
                            headers.add("X-Terminal-Id", config.terminalId());
                        }
                        if (!config.merchantId().isBlank()) {
                            headers.add("X-Merchant-Id", config.merchantId());
                        }
                    })
                    .body(payload)
                    .retrieve()
                    .body(Map.class);
            return parseWebhookResponse(response);
        } catch (RestClientResponseException ex) {
            return new TerminalPaymentResult(
                    false,
                    "error",
                    null,
                    "Webhook da maquineta retornou HTTP " + ex.getStatusCode().value()
            );
        } catch (Exception ex) {
            return new TerminalPaymentResult(
                    false,
                    "error",
                    null,
                    "Falha ao conectar na maquineta: " + ex.getMessage()
            );
        }
    }

    private TerminalPaymentResult parseWebhookResponse(
            final Map<String, Object> response
    ) {
        if (response == null || response.isEmpty()) {
            return new TerminalPaymentResult(
                    false,
                    "error",
                    null,
                    "Webhook sem resposta valida."
            );
        }

        final Boolean approvedField = asBoolean(response.get("approved"));
        final String statusRaw = firstNonBlank(
                response,
                "status",
                "paymentStatus",
                "result"
        );
        final boolean approved = approvedField != null
                ? approvedField
                : isApprovedStatus(statusRaw);
        final String normalizedStatus = approved ? "approved" : "declined";

        String message = firstNonBlank(
                response,
                "message",
                "detail",
                "reason",
                "description"
        );
        if (message.isBlank()) {
            message = approved
                    ? "Pagamento aprovado pela maquineta."
                    : "Pagamento recusado pela maquineta.";
        }

        final String transactionId = firstNonBlank(
                response,
                "transactionId",
                "transaction_id",
                "nsu",
                "authorizationCode",
                "authorization_code",
                "id"
        );

        return new TerminalPaymentResult(
                approved,
                normalizedStatus,
                transactionId.isBlank() ? null : transactionId,
                message
        );
    }

    private StoredTerminalConfig mergeAndValidate(
            final StoredTerminalConfig current,
            final TerminalConfigInput input
    ) {
        final boolean enabled = input.enabled() != null
                ? input.enabled()
                : current.enabled();
        final String mode = normalizeMode(
                input.mode() == null ? current.mode() : input.mode()
        );
        final String provider = nonBlankOrDefault(
                input.provider(),
                current.provider(),
                "custom"
        );
        final String endpointUrl = nonBlankOrDefault(
                input.endpointUrl(),
                current.endpointUrl(),
                ""
        );
        final String terminalId = nonBlankOrDefault(
                input.terminalId(),
                current.terminalId(),
                ""
        );
        final String merchantId = nonBlankOrDefault(
                input.merchantId(),
                current.merchantId(),
                ""
        );
        final int timeoutMs = normalizeTimeout(
                input.timeoutMs() == null ? current.timeoutMs() : input.timeoutMs()
        );

        String secret = current.secret();
        if (Boolean.TRUE.equals(input.clearSecret())) {
            secret = "";
        } else if (input.secret() != null) {
            secret = normalizeText(input.secret());
        }

        if (MODE_WEBHOOK.equals(mode) && endpointUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "Endpoint obrigatorio para modo webhook."
            );
        }
        if (!endpointUrl.isBlank()) {
            validateUrl(endpointUrl);
        }

        return new StoredTerminalConfig(
                enabled,
                mode,
                provider,
                endpointUrl,
                terminalId,
                merchantId,
                timeoutMs,
                secret
        );
    }

    private StoredTerminalConfig loadPayload() {
        final String raw = settings.getOrDefault(KEY_POS_CONFIG, "");
        if (raw == null || raw.isBlank()) {
            return defaultConfig();
        }
        try {
            final StoredTerminalConfig parsed = objectMapper.readValue(
                    raw,
                    StoredTerminalConfig.class
            );
            if (parsed == null) {
                return defaultConfig();
            }
            return mergeAndValidate(defaultConfig(), new TerminalConfigInput(
                    parsed.enabled(),
                    parsed.mode(),
                    parsed.provider(),
                    parsed.endpointUrl(),
                    parsed.terminalId(),
                    parsed.merchantId(),
                    parsed.timeoutMs(),
                    parsed.secret(),
                    false
            ));
        } catch (Exception ex) {
            return defaultConfig();
        }
    }

    private void savePayload(final StoredTerminalConfig payload) {
        try {
            final String json = objectMapper.writeValueAsString(payload);
            settings.upsert(KEY_POS_CONFIG, json, DESC_POS_CONFIG);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Falha ao salvar configuracao da maquineta.",
                    ex
            );
        }
    }

    private TerminalConfig toPublicConfig(final StoredTerminalConfig payload) {
        return new TerminalConfig(
                payload.enabled(),
                payload.mode(),
                payload.provider(),
                payload.endpointUrl(),
                payload.terminalId(),
                payload.merchantId(),
                payload.timeoutMs(),
                !payload.secret().isBlank()
        );
    }

    private StoredTerminalConfig defaultConfig() {
        return new StoredTerminalConfig(
                false,
                MODE_MOCK,
                "custom",
                "",
                "",
                "",
                DEFAULT_TIMEOUT_MS,
                ""
        );
    }

    private RestClient restClient(final int timeoutMs) {
        final SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    private String normalizeMode(final String mode) {
        final String normalized = normalizeText(mode);
        final String result = normalized.isBlank() ? MODE_MOCK : normalized;
        if (!ALLOWED_MODES.contains(result)) {
            throw new IllegalArgumentException(
                    "Modo invalido. Use mock ou webhook."
            );
        }
        return result;
    }

    private int normalizeTimeout(final Integer timeoutMs) {
        final int value = timeoutMs == null ? DEFAULT_TIMEOUT_MS : timeoutMs;
        if (value < MIN_TIMEOUT_MS || value > MAX_TIMEOUT_MS) {
            throw new IllegalArgumentException(
                    "Timeout deve estar entre 1000 e 30000 ms."
            );
        }
        return value;
    }

    private void validateUrl(final String rawUrl) {
        try {
            final URI uri = URI.create(rawUrl);
            final String scheme = uri.getScheme();
            if (scheme == null) {
                throw new IllegalArgumentException("URL sem esquema.");
            }
            final String normalizedScheme = scheme.toLowerCase(Locale.ROOT);
            if (!"http".equals(normalizedScheme)
                    && !"https".equals(normalizedScheme)) {
                throw new IllegalArgumentException("URL deve usar http ou https.");
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new IllegalArgumentException("URL sem host.");
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Endpoint invalido para maquineta."
            );
        }
    }

    private String nonBlankOrDefault(
            final String value,
            final String fallback,
            final String defaultValue
    ) {
        final String current = normalizeText(value);
        if (!current.isBlank()) {
            return current;
        }
        final String normalizedFallback = normalizeText(fallback);
        if (!normalizedFallback.isBlank()) {
            return normalizedFallback;
        }
        return defaultValue;
    }

    private String firstNonBlank(
            final Map<String, Object> payload,
            final String... keys
    ) {
        if (payload == null || keys == null) {
            return "";
        }
        for (String key : keys) {
            final Object value = payload.get(key);
            final String text = normalizeText(value == null ? "" : value.toString());
            if (!text.isBlank()) {
                return text;
            }
        }
        return "";
    }

    private boolean isApprovedStatus(final String rawStatus) {
        final String status = normalizeText(rawStatus);
        return status.equals("approved")
                || status.equals("aprovado")
                || status.equals("success")
                || status.equals("ok")
                || status.equals("paid")
                || status.equals("pago");
    }

    private Boolean asBoolean(final Object value) {
        if (value instanceof Boolean boolValue) {
            return boolValue;
        }
        if (value == null) {
            return null;
        }
        final String raw = normalizeText(value.toString());
        if (raw.equals("true") || raw.equals("1")) {
            return true;
        }
        if (raw.equals("false") || raw.equals("0")) {
            return false;
        }
        return null;
    }

    private String normalizeText(final String value) {
        return value == null ? "" : value.trim();
    }

    public record TerminalConfig(
            boolean enabled,
            String mode,
            String provider,
            String endpointUrl,
            String terminalId,
            String merchantId,
            int timeoutMs,
            boolean secretConfigured
    ) {
    }

    public record TerminalConfigInput(
            Boolean enabled,
            String mode,
            String provider,
            String endpointUrl,
            String terminalId,
            String merchantId,
            Integer timeoutMs,
            String secret,
            Boolean clearSecret
    ) {
    }

    public record TerminalPaymentRequest(
            BigDecimal amount,
            String paymentType,
            String reference,
            String source,
            Map<String, Object> metadata
    ) {
    }

    public record TerminalPaymentResult(
            boolean approved,
            String status,
            String transactionId,
            String message
    ) {
    }

    private record StoredTerminalConfig(
            Boolean enabled,
            String mode,
            String provider,
            String endpointUrl,
            String terminalId,
            String merchantId,
            Integer timeoutMs,
            String secret
    ) {
        private StoredTerminalConfig {
            enabled = enabled != null && enabled;
            mode = mode == null ? "" : mode;
            provider = provider == null ? "" : provider;
            endpointUrl = endpointUrl == null ? "" : endpointUrl;
            terminalId = terminalId == null ? "" : terminalId;
            merchantId = merchantId == null ? "" : merchantId;
            timeoutMs = timeoutMs == null ? DEFAULT_TIMEOUT_MS : timeoutMs;
            secret = secret == null ? "" : secret;
        }
    }
}
