package br.com.redemaisfarma.adapters.outbound.ai.openai;

import br.com.redemaisfarma.application.config.AppAiOpenAiProperties;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OpenAiAiAssistantService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiAiAssistantService.class);

    private final AppAiOpenAiProperties properties;
    private final OpenAiResponsesClient responsesClient;
    private final Map<String, String> previousResponseIds = new ConcurrentHashMap<>();

    public OpenAiAiAssistantService(
            final AppAiOpenAiProperties properties,
            final OpenAiResponsesClient responsesClient
    ) {
        this.properties = properties;
        this.responsesClient = responsesClient;
    }

    public String answer(final String sessionId, final String message) {
        if (!properties.isConfigured()) {
            throw new IllegalStateException("OpenAI nao configurado.");
        }
        try {
            final OpenAiResponsesClient.ResponseText response = responsesClient.chat(
                    message,
                    previousResponseIds.get(sessionId)
            );
            if (response.responseId() != null && sessionId != null && !sessionId.isBlank()) {
                previousResponseIds.put(sessionId, response.responseId());
            }
            return response.text();
        } catch (IOException ex) {
            throw new IllegalStateException("Falha ao consultar OpenAI: " + ex.getMessage(), ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Consulta OpenAI interrompida.", ex);
        } catch (RuntimeException ex) {
            log.warn("Falha no atendimento OpenAI: {}", ex.getMessage());
            throw ex;
        }
    }
}
