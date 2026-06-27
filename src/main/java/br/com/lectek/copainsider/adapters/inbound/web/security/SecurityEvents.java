package br.com.lectek.copainsider.adapters.inbound.web.security;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class SecurityEvents {

    private static final Logger log = LoggerFactory.getLogger(SecurityEvents.class);

    private final LoginAttemptService attemptService;

    public SecurityEvents(LoginAttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @EventListener
    public void handleAuthSuccess(InteractiveAuthenticationSuccessEvent e) {
        log.info("✅ Autenticado: {}", e.getAuthentication().getName());
        String ip = resolveIp();
        if (ip != null) {
            attemptService.loginSucceeded(ip);
        }
    }

    @EventListener
    public void handleAuthFailure(AbstractAuthenticationFailureEvent e) {
        log.warn("❌ Falha de auth: {} - {}", e.getAuthentication().getName(), e.getException().getMessage());
        String ip = resolveIp();
        if (ip != null) {
            attemptService.loginFailed(ip);
        }
    }

    private static String resolveIp() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return null;
        HttpServletRequest request = attrs.getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
