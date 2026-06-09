package br.com.redemaisfarma.adapters.outbound.ai.ollama;

import br.com.redemaisfarma.application.config.AppAiOllamaProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OllamaChatClient {

    private final AppAiOllamaProperties props;
    private final HttpClient http;
    private final ObjectMapper mapper = new ObjectMapper();

    public OllamaChatClient(final AppAiOllamaProperties props) {
        this.props = props;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(props.getTimeoutMs()))
                .build();
    }

    public String chatOnce(final String sessionId, final String userMessage) throws Exception {
        return chat(sessionId, List.of(new ChatMessage("user", userMessage == null ? "" : userMessage)));
    }

    public String chat(final String sessionId, final List<ChatMessage> conversation) throws Exception {
        final String url = props.getBaseUrl().replaceAll("/+$", "") + "/api/chat";

        final ObjectNode root = mapper.createObjectNode();
        root.put("model", props.getModel());
        root.put("stream", false);
        root.put("temperature", props.getTemperature());
        root.put("top_p", props.getTopP());

        final ArrayNode messages = root.putArray("messages");
        final String systemPrompt = trimToNull(props.getSystemPrompt());
        if (systemPrompt != null) {
            final ObjectNode systemMessage = mapper.createObjectNode();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messages.add(systemMessage);
        }

        for (ChatMessage item : conversation == null ? List.<ChatMessage>of() : conversation) {
            if (item == null) {
                continue;
            }
            final String role = normalizeRole(item.role());
            final String content = trimToNull(item.content());
            if (content == null) {
                continue;
            }
            final ObjectNode node = mapper.createObjectNode();
            node.put("role", role);
            node.put("content", content);
            messages.add(node);
        }

        final byte[] body = mapper.writeValueAsBytes(root);
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(props.getTimeoutMs()))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
        final HttpResponse<byte[]> response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() / 100 != 2) {
            throw new IllegalStateException(
                    "Ollama HTTP " + response.statusCode() + ": "
                            + new String(response.body(), StandardCharsets.UTF_8)
            );
        }

        final JsonNode json = mapper.readTree(response.body());
        final JsonNode message = json.path("message").path("content");
        final String answer = trimToNull(message.asText(null));
        if (answer == null) {
            return "Sem resposta no momento. Tente novamente.";
        }
        return answer;
    }

    private static String normalizeRole(final String role) {
        if ("assistant".equalsIgnoreCase(role)) {
            return "assistant";
        }
        if ("system".equalsIgnoreCase(role)) {
            return "system";
        }
        return "user";
    }

    private static String trimToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record ChatMessage(String role, String content) {
    }
}
