/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.databind.ObjectMapper
 *  com.fasterxml.jackson.databind.node.ObjectNode
 *  jakarta.servlet.http.HttpServletRequest
 *  jakarta.servlet.http.HttpServletResponse
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.security.core.AuthenticationException
 *  org.springframework.security.web.AuthenticationEntryPoint
 *  org.springframework.stereotype.Component
 */
package br.com.redemaisfarma.adapters.inbound.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix="jwt", name={"enabled"}, havingValue="true", matchIfMissing=false)
public class JwtAuthEntryPoint
implements AuthenticationEntryPoint {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setHeader("WWW-Authenticate", "Bearer realm=\"redemaisfarma\"");
        response.setStatus(401);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        ObjectNode body = MAPPER.createObjectNode().put("timestamp", Instant.now().toString()).put("status", 401).put("error", "Unauthorized").put("message", this.safeMessage(authException)).put("path", request.getRequestURI()).put("method", request.getMethod());
        response.getWriter().write(MAPPER.writeValueAsString((Object)body));
    }

    private String safeMessage(AuthenticationException ex) {
        String msg = ex != null ? ex.getMessage() : null;
        return msg == null || msg.isBlank() ? "Token ausente, inv\u00e1lido ou expirado." : msg;
    }
}

