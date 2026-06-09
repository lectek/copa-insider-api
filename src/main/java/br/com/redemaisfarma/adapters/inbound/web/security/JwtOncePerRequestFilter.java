/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.servlet.FilterChain
 *  jakarta.servlet.ServletException
 *  jakarta.servlet.ServletRequest
 *  jakarta.servlet.ServletResponse
 *  jakarta.servlet.http.HttpServletRequest
 *  jakarta.servlet.http.HttpServletResponse
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.lang.NonNull
 *  org.springframework.security.authentication.UsernamePasswordAuthenticationToken
 *  org.springframework.security.core.Authentication
 *  org.springframework.security.core.context.SecurityContextHolder
 *  org.springframework.security.web.authentication.WebAuthenticationDetailsSource
 *  org.springframework.stereotype.Component
 *  org.springframework.web.filter.OncePerRequestFilter
 */
package br.com.redemaisfarma.adapters.inbound.web.security;

import br.com.redemaisfarma.adapters.outbound.auth.jwt.provider.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@ConditionalOnProperty(prefix="jwt", name={"enabled"}, havingValue="true", matchIfMissing=false)
public class JwtOncePerRequestFilter
extends OncePerRequestFilter {
    private final JwtService jwtService;

    public JwtOncePerRequestFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        return uri.startsWith("/api/ping") || uri.startsWith("/api/status") || uri.startsWith("/api/auth/") || uri.startsWith("/api/suporte/") || uri.startsWith("/v3/api-docs") || uri.startsWith("/swagger-ui") || uri.startsWith("/actuator");
    }

    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter((ServletRequest)request, (ServletResponse)response);
            return;
        }
        String token = header.substring(7);
        try {
            Map<String, Object> claims = this.jwtService.parse(token);
            request.setAttribute("jwt.claims", claims);
            String username = (String)claims.getOrDefault("email", claims.get("sub"));
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken((Object)username, null, Collections.emptyList());
                auth.setDetails((Object)new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication((Authentication)auth);
            }
        }
        catch (Exception ex) {
            request.setAttribute("jwt.error", (Object)ex.getClass().getSimpleName());
        }
        filterChain.doFilter((ServletRequest)request, (ServletResponse)response);
    }
}

