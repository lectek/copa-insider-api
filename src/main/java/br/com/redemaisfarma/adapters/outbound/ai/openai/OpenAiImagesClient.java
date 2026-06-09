package br.com.redemaisfarma.adapters.outbound.ai.openai;

import br.com.redemaisfarma.application.config.AppAiOpenAiProperties;
import br.com.redemaisfarma.application.port.inbound.ImageStudioUseCase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class OpenAiImagesClient {

    private static final String PROVIDER = "openai";

    private final AppAiOpenAiProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OpenAiImagesClient(
            final AppAiOpenAiProperties properties,
            final ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(Math.max(1000, properties.getTimeoutMs())))
                .build();
    }

    public ImageStudioUseCase.GeneratedImageResult generate(
            final String prompt
    ) throws IOException, InterruptedException {
        if (!properties.isConfigured()) {
            throw new IOException("OpenAI nao configurado. Defina OPENAI_API_KEY.");
        }

        final ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", blankToDefault(properties.getImageModel(), "gpt-image-1.5"));
        payload.put("prompt", blankToDefault(prompt, "Packshot de produto em fundo branco."));
        payload.put("size", blankToDefault(properties.getImageSize(), "1024x1024"));
        payload.put("quality", blankToDefault(properties.getImageQuality(), "medium"));
        payload.put("background", blankToDefault(properties.getImageBackground(), "opaque"));
        payload.put("output_format", blankToDefault(properties.getImageOutputFormat(), "png"));

        final String endpoint = properties.getBaseUrl().replaceAll("/+$", "") + "/images/generations";
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
                    "OpenAI Images API HTTP "
                            + response.statusCode()
                            + ": "
                            + new String(response.body(), StandardCharsets.UTF_8)
            );
        }

        final JsonNode json = objectMapper.readTree(response.body());
        final JsonNode first = json.path("data").isArray() && json.path("data").size() > 0
                ? json.path("data").get(0)
                : null;
        if (first == null) {
            throw new IOException("OpenAI nao retornou imagem.");
        }

        final String b64 = trimToNull(first.path("b64_json").asText(null));
        final String revisedPrompt = trimToNull(first.path("revised_prompt").asText(null));
        final String url = trimToNull(first.path("url").asText(null));
        final String outputFormat = blankToDefault(properties.getImageOutputFormat(), "png");
        final String mimeType = "image/" + outputFormat;

        final String reference;
        if (b64 != null) {
            reference = "data:" + mimeType + ";base64," + b64;
        } else if (url != null) {
            reference = url;
        } else {
            throw new IOException("OpenAI nao retornou b64_json nem url para a imagem.");
        }

        return new ImageStudioUseCase.GeneratedImageResult(
                reference,
                mimeType,
                PROVIDER,
                prompt,
                revisedPrompt
        );
    }

    private static String blankToDefault(final String value, final String fallback) {
        final String trimmed = trimToNull(value);
        return trimmed == null ? fallback : trimmed;
    }

    private static String trimToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
