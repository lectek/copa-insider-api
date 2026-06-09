package br.com.redemaisfarma.adapters.outbound.ai.openai;

import br.com.redemaisfarma.adapters.inbound.web.dto.ImageGenRequestDTO;
import br.com.redemaisfarma.application.config.AppAiOpenAiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Iterator;
import org.springframework.stereotype.Component;

@Component
public class OpenAiResponsesClient {

    private final AppAiOpenAiProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OpenAiResponsesClient(
            final AppAiOpenAiProperties properties,
            final ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(Math.max(1000, properties.getTimeoutMs())))
                .build();
    }

    public ResponseText chat(
            final String message,
            final String previousResponseId
    ) throws IOException, InterruptedException {
        final ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", blankToDefault(properties.getChatModel(), "gpt-5-mini"));
        payload.put("store", properties.isStoreResponses());
        putIfNotBlank(payload, "previous_response_id", previousResponseId);
        putIfNotBlank(payload, "instructions", properties.getSystemPrompt());

        final String reasoningEffort = trimToNull(properties.getReasoningEffort());
        if (reasoningEffort != null) {
            final ObjectNode reasoning = payload.putObject("reasoning");
            reasoning.put("effort", reasoningEffort);
        }

        final ArrayNode input = payload.putArray("input");
        final ObjectNode user = input.addObject();
        user.put("role", "user");
        final ArrayNode content = user.putArray("content");
        final ObjectNode text = content.addObject();
        text.put("type", "input_text");
        text.put("text", buildChatMessage(message));

        final JsonNode json = post(payload);
        return new ResponseText(
                trimToNull(json.path("id").asText(null)),
                extractOutputText(json)
        );
    }

    public String describeProductPrompt(final ImageGenRequestDTO request)
            throws IOException, InterruptedException {
        final String inputImageUrl = trimToNull(request.inputImageUrl());
        if (inputImageUrl == null) {
            return "";
        }

        final ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", blankToDefault(properties.getVisionModel(), properties.getChatModel()));
        payload.put("store", false);
        putIfNotBlank(payload, "instructions", properties.getProductVisionPrompt());
        if (hasResearchSignals(request)) {
            final ArrayNode tools = payload.putArray("tools");
            tools.addObject().put("type", "web_search");
        }

        final ArrayNode input = payload.putArray("input");
        final ObjectNode user = input.addObject();
        user.put("role", "user");
        final ArrayNode content = user.putArray("content");

        final String visionInput = buildVisionInput(request);
        if (!visionInput.isBlank()) {
            final ObjectNode text = content.addObject();
            text.put("type", "input_text");
            text.put("text", visionInput);
        }

        final ObjectNode image = content.addObject();
        image.put("type", "input_image");
        image.put("image_url", inputImageUrl);
        image.put("detail", "high");

        final JsonNode json = post(payload);
        return sanitizePrompt(extractOutputText(json));
    }

    private JsonNode post(final ObjectNode payload)
            throws IOException, InterruptedException {
        if (!properties.isConfigured()) {
            throw new IOException("OpenAI nao configurado. Defina OPENAI_API_KEY.");
        }

        final String endpoint = properties.getBaseUrl().replaceAll("/+$", "") + "/responses";
        final byte[] body = objectMapper.writeValueAsBytes(payload);
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofMillis(Math.max(1000, properties.getTimeoutMs())))
                .header("Authorization", "Bearer " + properties.getApiKey().trim())
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        final HttpResponse<byte[]> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofByteArray()
        );
        if (response.statusCode() / 100 != 2) {
            throw new IOException(
                    "OpenAI Responses API HTTP "
                            + response.statusCode()
                            + ": "
                            + new String(response.body(), StandardCharsets.UTF_8)
            );
        }
        return objectMapper.readTree(response.body());
    }

    private String buildChatMessage(final String message) {
        final StringBuilder builder = new StringBuilder();
        builder.append("Mensagem do cliente:\n");
        builder.append(blankToDefault(message, ""));
        builder.append("\n\nResponda em portugues do Brasil, com objetividade.");
        return builder.toString().trim();
    }

    private String buildVisionInput(final ImageGenRequestDTO request) {
        final StringBuilder builder = new StringBuilder();
        final String userPrompt = trimToNull(request.prompt());
        if (userPrompt != null) {
            builder.append("Instrucao adicional do usuario: ").append(userPrompt);
        }
        appendVisionField(builder, "Produto informado", request.varText("nome", ""));
        appendVisionField(builder, "Descricao informada", request.varText("descricao", ""));
        appendVisionField(builder, "Categoria informada", request.varText("categoria", ""));
        appendVisionField(builder, "Fabricante informado", request.varText("fabricante", ""));
        appendVisionField(builder, "Codigo ou EAN informado", request.varText("codigo", ""));
        appendVisionField(builder, "Codigo de barras informado", request.varText("codigoBarras", ""));
        appendVisionField(builder, "SKU informado", request.varText("sku", ""));
        appendVisionField(builder, "Tipo fisico esperado", request.varText("tipoFisico", ""));
        builder.append('\n')
                .append("Pesquise aprofundadamente o produto usando os identificadores disponiveis, confirme marca, variante, concentracao, volume, sabor, quantidade, formato da embalagem e elementos visuais oficiais antes de escrever o prompt final. ")
                .append("Use a imagem enviada como referencia principal para validar a identidade do item e corrigir inconsistencias da busca.");
        return builder.toString().trim();
    }

    private boolean hasResearchSignals(final ImageGenRequestDTO request) {
        return hasText(request.varText("nome", ""))
                || hasText(request.varText("descricao", ""))
                || hasText(request.varText("fabricante", ""))
                || hasText(request.varText("codigo", ""))
                || hasText(request.varText("codigoBarras", ""))
                || hasText(request.varText("sku", ""));
    }

    private static void appendVisionField(
            final StringBuilder builder,
            final String label,
            final String value
    ) {
        final String safeValue = trimToNull(value);
        if (safeValue == null) {
            return;
        }
        if (builder.length() > 0) {
            builder.append('\n');
        }
        builder.append(label).append(": ").append(safeValue);
    }

    private static boolean hasText(final String value) {
        return trimToNull(value) != null;
    }

    private static String extractOutputText(final JsonNode json) throws IOException {
        final String outputText = trimToNull(json.path("output_text").asText(null));
        if (outputText != null) {
            return outputText;
        }

        final JsonNode output = json.path("output");
        if (output.isArray()) {
            final StringBuilder builder = new StringBuilder();
            final Iterator<JsonNode> iterator = output.elements();
            while (iterator.hasNext()) {
                final JsonNode item = iterator.next();
                final JsonNode content = item.path("content");
                if (!content.isArray()) {
                    continue;
                }
                for (JsonNode part : content) {
                    final String text = trimToNull(part.path("text").asText(null));
                    if (text != null) {
                        if (builder.length() > 0) {
                            builder.append('\n');
                        }
                        builder.append(text);
                    }
                }
            }
            final String extracted = trimToNull(builder.toString());
            if (extracted != null) {
                return extracted;
            }
        }
        throw new IOException("OpenAI nao retornou texto na resposta.");
    }

    private static void putIfNotBlank(
            final ObjectNode node,
            final String fieldName,
            final String value
    ) {
        final String safeValue = trimToNull(value);
        if (safeValue != null) {
            node.put(fieldName, safeValue);
        }
    }

    private static String blankToDefault(final String value, final String fallback) {
        final String trimmed = trimToNull(value);
        return trimmed == null ? fallback : trimmed;
    }

    private static String sanitizePrompt(final String value) {
        final String trimmed = trimToNull(value);
        if (trimmed == null) {
            return "";
        }
        return trimmed
                .replace('“', '"')
                .replace('”', '"')
                .replace("```", "")
                .replaceAll("^\"+|\"+$", "")
                .trim();
    }

    private static String trimToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record ResponseText(String responseId, String text) {
    }
}
