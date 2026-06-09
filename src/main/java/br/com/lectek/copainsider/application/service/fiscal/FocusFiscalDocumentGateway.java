package br.com.lectek.copainsider.application.service.fiscal;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.FiscalDocumentEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.FiscalEmitterConfigEntity;
import br.com.lectek.copainsider.domain.fiscal.FiscalDocumentModel;
import br.com.lectek.copainsider.domain.fiscal.FiscalDocumentStatus;
import br.com.lectek.copainsider.domain.fiscal.FiscalProvider;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class FocusFiscalDocumentGateway implements FiscalDocumentGateway {

    private static final int TIMEOUT_MS = 10000;

    private final ObjectMapper objectMapper;

    public FocusFiscalDocumentGateway(final ObjectMapper objectMapperValue) {
        this.objectMapper = objectMapperValue;
    }

    @Override
    public FiscalProvider provider() {
        return FiscalProvider.FOCUS_NFE;
    }

    @Override
    public GatewayStatusSnapshot submitDocument(
            final FiscalEmitterConfigEntity config,
            final FiscalDocumentEntity document,
            final String requestPayload
    ) {
        validateSubmitRequest(config, document, requestPayload);

        try {
            final String rawResponse = restClient().post()
                    .uri(buildSubmitUri(config, document))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .headers(headers -> headers.setBasicAuth(
                            config.getApiToken(),
                            ""
                    ))
                    .body(requestPayload)
                    .retrieve()
                    .body(String.class);
            return parseProviderResponse(
                    rawResponse,
                    FiscalDocumentStatus.SUBMITTED
            );
        } catch (RestClientResponseException ex) {
            return parseProviderError(ex);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Falha ao enviar documento ao Focus: " + ex.getMessage(),
                    ex
            );
        }
    }

    @Override
    public GatewayStatusSnapshot queryStatus(
            final FiscalEmitterConfigEntity config,
            final FiscalDocumentEntity document
    ) {
        if (config == null) {
            throw new IllegalArgumentException(
                    "Configuracao fiscal do provedor obrigatoria."
            );
        }
        if (document == null || isBlank(document.getExternalReference())) {
            throw new IllegalArgumentException(
                    "Documento fiscal sem referencia externa."
            );
        }
        if (isBlank(config.getApiToken())) {
            throw new IllegalArgumentException(
                    "Token da API fiscal nao configurado."
            );
        }

        try {
            final String rawResponse = restClient().get()
                    .uri(buildStatusUri(config, document))
                    .headers(headers -> {
                        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
                        headers.setBasicAuth(config.getApiToken(), "");
                    })
                    .retrieve()
                    .body(String.class);
            return parseProviderResponse(
                    rawResponse,
                    FiscalDocumentStatus.SUBMITTED
            );
        } catch (RestClientResponseException ex) {
            throw new IllegalStateException(
                    "Focus retornou HTTP "
                            + ex.getStatusCode().value()
                            + " ao consultar documento fiscal."
            );
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Falha ao consultar status no Focus: " + ex.getMessage(),
                    ex
            );
        }
    }

    private void validateSubmitRequest(
            final FiscalEmitterConfigEntity config,
            final FiscalDocumentEntity document,
            final String requestPayload
    ) {
        if (config == null) {
            throw new IllegalArgumentException(
                    "Configuracao fiscal do provedor obrigatoria."
            );
        }
        if (document == null || isBlank(document.getExternalReference())) {
            throw new IllegalArgumentException(
                    "Documento fiscal sem referencia externa."
            );
        }
        if (isBlank(config.getApiToken())) {
            throw new IllegalArgumentException(
                    "Token da API fiscal nao configurado."
            );
        }
        if (isBlank(requestPayload)) {
            throw new IllegalArgumentException(
                    "Payload fiscal obrigatorio para envio."
            );
        }
    }

    private URI buildSubmitUri(
            final FiscalEmitterConfigEntity config,
            final FiscalDocumentEntity document
    ) {
        final String baseUrl = trimTrailingSlash(config.getApiBaseUrl());
        final String path = document.getModel() == FiscalDocumentModel.NFCE_65
                ? "/v2/nfce"
                : "/v2/nfe";
        return URI.create(
                baseUrl
                        + path
                        + "?ref="
                        + URLEncoder.encode(
                                document.getExternalReference(),
                                StandardCharsets.UTF_8
                        )
        );
    }

    private URI buildStatusUri(
            final FiscalEmitterConfigEntity config,
            final FiscalDocumentEntity document
    ) {
        final String baseUrl = trimTrailingSlash(config.getApiBaseUrl());
        final String path = document.getModel() == FiscalDocumentModel.NFCE_65
                ? "/v2/nfce/"
                : "/v2/nfe/";
        return URI.create(baseUrl + path + document.getExternalReference());
    }

    private GatewayStatusSnapshot parseProviderResponse(
            final String rawResponse,
            final FiscalDocumentStatus defaultStatus
    ) {
        final Map<String, Object> payload = parsePayload(rawResponse);
        if (payload == null || payload.isEmpty()) {
            return new GatewayStatusSnapshot(
                    defaultStatus,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    blankToNull(rawResponse),
                    null,
                    null,
                    null,
                    LocalDateTime.now()
            );
        }
        return toSnapshot(payload, defaultStatus, rawResponse);
    }

    private GatewayStatusSnapshot parseProviderError(
            final RestClientResponseException ex
    ) {
        final String rawResponse = ex.getResponseBodyAsString();
        final Map<String, Object> payload = parsePayload(rawResponse);
        final FiscalDocumentStatus defaultStatus =
                ex.getStatusCode().is4xxClientError()
                        ? FiscalDocumentStatus.REJECTED
                        : FiscalDocumentStatus.ERROR;
        if (payload == null || payload.isEmpty()) {
            return new GatewayStatusSnapshot(
                    defaultStatus,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "Focus retornou HTTP " + ex.getStatusCode().value(),
                    "Focus retornou HTTP " + ex.getStatusCode().value(),
                    null,
                    blankToNull(rawResponse),
                    null,
                    null,
                    null,
                    LocalDateTime.now()
            );
        }
        return toSnapshot(payload, defaultStatus, rawResponse);
    }

    private GatewayStatusSnapshot toSnapshot(
            final Map<String, Object> payload,
            final FiscalDocumentStatus defaultStatus,
            final String rawPayload
    ) {
        if (payload == null || payload.isEmpty()) {
            throw new IllegalStateException(
                    "Resposta vazia ao consultar documento fiscal."
            );
        }
        final String statusText = firstNonBlank(
                payload,
                "status",
                "status_sefaz",
                "situacao",
                "state"
        );
        final String message = firstNonBlank(
                payload,
                "mensagem_sefaz",
                "message",
                "mensagem",
                "motivo"
        );

        final FiscalDocumentStatus normalizedStatus = mapStatus(
                statusText,
                defaultStatus
        );
        final String normalizedPayload = rawPayload == null || rawPayload.isBlank()
                ? writeJson(payload)
                : rawPayload;
        final LocalDateTime processedAt = firstDateTime(
                payload,
                "data_evento",
                "updated_at",
                "data_atualizacao"
        );

        return new GatewayStatusSnapshot(
                normalizedStatus,
                firstNonBlank(payload, "id", "reference", "referencia"),
                firstNonBlank(
                        payload,
                        "chave_nfe",
                        "chave",
                        "chave_acesso"
                ),
                parseInteger(payload.get("serie")),
                parseInteger(firstNonNull(
                        payload,
                        "numero",
                        "numero_nfe",
                        "numero_nf"
                )),
                firstNonBlank(payload, "protocolo", "protocolo_autorizacao"),
                firstNonBlank(payload, "url_xml", "caminho_xml_nota_fiscal"),
                firstNonBlank(payload, "url_danfe", "caminho_danfe"),
                message,
                normalizedStatus == FiscalDocumentStatus.REJECTED
                        || normalizedStatus == FiscalDocumentStatus.ERROR
                        ? message
                        : null,
                firstNonBlank(
                        payload,
                        "codigo_status_sefaz",
                        "status_sefaz",
                        "codigo"
                ),
                normalizedPayload,
                firstDateTime(payload, "data_emissao", "emitido_em"),
                firstDateTime(payload, "data_autorizacao", "autorizado_em"),
                firstDateTime(payload, "data_cancelamento", "cancelado_em"),
                processedAt == null ? LocalDateTime.now() : processedAt
        );
    }

    private RestClient restClient() {
        final SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(TIMEOUT_MS);
        factory.setReadTimeout(TIMEOUT_MS);
        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    private FiscalDocumentStatus mapStatus(
            final String rawStatus,
            final FiscalDocumentStatus defaultStatus
    ) {
        final String normalized = rawStatus == null
                ? ""
                : rawStatus.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return defaultStatus;
        }
        if (normalized.contains("autoriz")) {
            return FiscalDocumentStatus.AUTHORIZED;
        }
        if (normalized.contains("cancel")) {
            return FiscalDocumentStatus.CANCELLED;
        }
        if (normalized.contains("rejeit")) {
            return FiscalDocumentStatus.REJECTED;
        }
        if (normalized.contains("erro") || normalized.contains("falha")) {
            return FiscalDocumentStatus.ERROR;
        }
        if (normalized.contains("process")
                || normalized.contains("emit")
                || normalized.contains("pend")) {
            return FiscalDocumentStatus.SUBMITTED;
        }
        return defaultStatus;
    }

    private LocalDateTime firstDateTime(
            final Map<String, Object> payload,
            final String... keys
    ) {
        for (String key : keys) {
            final Object value = payload.get(key);
            final LocalDateTime parsed = parseDateTime(value);
            if (parsed != null) {
                return parsed;
            }
        }
        return null;
    }

    private LocalDateTime parseDateTime(final Object rawValue) {
        if (rawValue == null) {
            return null;
        }
        final String text = rawValue.toString().trim();
        if (text.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(text).toLocalDateTime();
        } catch (Exception ignored) {
        }
        try {
            return LocalDateTime.parse(text);
        } catch (Exception ignored) {
        }
        try {
            return LocalDateTime.ofInstant(
                    Instant.parse(text),
                    ZoneId.systemDefault()
            );
        } catch (Exception ignored) {
        }
        return null;
    }

    private Integer parseInteger(final Object rawValue) {
        if (rawValue == null) {
            return null;
        }
        try {
            return Integer.parseInt(rawValue.toString().trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private Map<String, Object> parsePayload(final String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(
                    rawResponse,
                    new TypeReference<Map<String, Object>>() { }
            );
        } catch (Exception ex) {
            return null;
        }
    }

    private Object firstNonNull(
            final Map<String, Object> payload,
            final String... keys
    ) {
        for (String key : keys) {
            final Object value = payload.get(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String firstNonBlank(
            final Map<String, Object> payload,
            final String... keys
    ) {
        for (String key : keys) {
            final Object value = payload.get(key);
            if (value == null) {
                continue;
            }
            final String text = value.toString().trim();
            if (!text.isBlank()) {
                return text;
            }
        }
        return null;
    }

    private String trimTrailingSlash(final String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Base URL fiscal nao configurada.");
        }
        String normalized = value.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }

    private String blankToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private String writeJson(final Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(
                    "Falha ao serializar resposta do provedor fiscal.",
                    ex
            );
        }
    }
}
