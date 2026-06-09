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
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.slf4j.MDC
 *  org.springframework.lang.NonNull
 *  org.springframework.stereotype.Component
 *  org.springframework.web.filter.OncePerRequestFilter
 *  org.springframework.web.util.ContentCachingRequestWrapper
 *  org.springframework.web.util.ContentCachingResponseWrapper
 */
package br.com.redemaisfarma.adapters.inbound.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
public class RequestLoggingFilter
extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) {
            uri = uri.substring(ctx.length());
        }
        return "POST".equalsIgnoreCase(request.getMethod()) && "/api/auth/login".equals(uri);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        byte[] buf;
        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper res = new ContentCachingResponseWrapper(response);
        long start = System.nanoTime();
        String correlationId = (String)request.getAttribute("cid");
        if (correlationId == null) {
            correlationId = MDC.get((String)"cid");
        }
        try {
            filterChain.doFilter((ServletRequest)req, (ServletResponse)res);
        }
        catch (Throwable throwable) {
            byte[] buf2;
            long duration = (System.nanoTime() - start) / 1000000L;
            String method = req.getMethod();
            String uri = req.getRequestURI();
            String body = "";
            if (!"GET".equalsIgnoreCase(method) && !"DELETE".equalsIgnoreCase(method) && (buf2 = req.getContentAsByteArray()).length > 0) {
                body = new String(buf2, StandardCharsets.UTF_8);
                body = this.truncate(body, 1024);
            }
            int status = res.getStatus();
            log.info("[{}] {} {} -> status={} duration={}ms body={}", new Object[]{correlationId, method, uri, status, duration, body});
            res.copyBodyToResponse();
            throw throwable;
        }
        long duration = (System.nanoTime() - start) / 1000000L;
        String method = req.getMethod();
        String uri = req.getRequestURI();
        String body = "";
        if (!"GET".equalsIgnoreCase(method) && !"DELETE".equalsIgnoreCase(method) && (buf = req.getContentAsByteArray()).length > 0) {
            body = new String(buf, StandardCharsets.UTF_8);
            body = this.truncate(body, 1024);
        }
        int status = res.getStatus();
        log.info("[{}] {} {} -> status={} duration={}ms body={}", new Object[]{correlationId, method, uri, status, duration, body});
        res.copyBodyToResponse();
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max) + "\u2026";
    }
}

