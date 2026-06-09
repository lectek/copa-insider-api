package br.com.lectek.copainsider.adapters.inbound.web.controller.publico;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import br.com.lectek.copainsider.application.service.ai.AiAssistantService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Profile("!test")
@RestController
@RequestMapping("/api/ia")
public class IaPublicController {

    /**
     * Maximum accepted customer message length.
     */
    private static final int MAX_MESSAGE_LENGTH = 2000;

    /**
     * Service that answers customer questions.
     */
    private final AiAssistantService ai;

    /**
     * Creates IA public controller.
     *
     * @param service IA assistant service
     */
    public IaPublicController(final AiAssistantService service) {
        this.ai = service;
    }

    /**
     * Receives customer prompt and returns IA response.
     *
     * @param request ask payload
     * @param http servlet request
     * @return answer with resolved session id
     */
    @PostMapping(
            value = "/ask",
            consumes = "application/json",
            produces = "application/json"
    )
    public AskResponse ask(
            @RequestBody final JsonNode request,
            final HttpServletRequest http
    ) {
        final String sid = resolveSessionId(request, http);
        String msg = readText(request, "message");
        if (msg.isBlank()) {
            throw new IllegalArgumentException("message is required");
        }

        if (msg.length() > MAX_MESSAGE_LENGTH) {
            msg = msg.substring(0, MAX_MESSAGE_LENGTH);
        }

        final String answer;
        try {
            answer = ai.answer(sid, msg);
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    ex.getMessage(),
                    ex
            );
        }
        return new AskResponse(sid, answer);
    }

    private String resolveSessionId(final JsonNode request, final HttpServletRequest http) {
        final String sessionId = readText(request, "sessionId");
        return sessionId.isBlank() ? safeClientSession(http) : sessionId;
    }

    private String readText(final JsonNode request, final String fieldName) {
        if (request == null) {
            return "";
        }
        return request.path(fieldName).asText("").trim();
    }

    private String safeClientSession(final HttpServletRequest http) {
        final String ua = String.valueOf(http.getHeader("User-Agent"));
        final String ip = String.valueOf(http.getRemoteAddr());
        return UUID.nameUUIDFromBytes((ua + "|" + ip).getBytes()).toString();
    }

    public record AskResponse(@JsonProperty("sessionId") String sessionId, String answer) { }
}
