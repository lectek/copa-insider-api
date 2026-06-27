package br.com.lectek.copainsider.adapters.inbound.web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Intercepts POST /auth/login before Spring Security processes it.
 *
 * Rejects the request (redirects to error page) if:
 *  - The client IP has exceeded the failure threshold (rate limit), OR
 *  - The honeypot field "website" is non-empty (bot detection).
 *
 * On rejection the request never reaches the authentication provider,
 * so the attacker cannot distinguish block from bad credentials.
 */
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final String LOGIN_URL   = "/auth/login";
    private static final String BLOCKED_URL = "/auth/login?blocked";
    private static final String HONEYPOT    = "website";

    private final LoginAttemptService attemptService;

    public LoginRateLimitFilter(LoginAttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !(HttpMethod.POST.matches(request.getMethod())
                && LOGIN_URL.equals(request.getServletPath()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String ip = resolveClientIp(request);

        if (attemptService.isBlocked(ip)) {
            response.sendRedirect(BLOCKED_URL);
            return;
        }

        String honeypot = request.getParameter(HONEYPOT);
        if (honeypot != null && !honeypot.isBlank()) {
            // silently block bots — same redirect as wrong password
            response.sendRedirect("/auth/login?error");
            return;
        }

        chain.doFilter(request, response);
    }

    private static String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // take only the first (client) IP; proxies append their own
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
