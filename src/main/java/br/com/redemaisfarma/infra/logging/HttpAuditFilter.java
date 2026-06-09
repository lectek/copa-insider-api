/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.servlet.Filter
 *  jakarta.servlet.FilterChain
 *  jakarta.servlet.ServletException
 *  jakarta.servlet.ServletRequest
 *  jakarta.servlet.ServletResponse
 *  jakarta.servlet.http.HttpServletRequest
 *  jakarta.servlet.http.HttpServletResponse
 *  lombok.Generated
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.slf4j.MDC
 *  org.springframework.security.core.context.SecurityContextHolder
 *  org.springframework.stereotype.Component
 *  org.springframework.web.util.ContentCachingRequestWrapper
 *  org.springframework.web.util.ContentCachingResponseWrapper
 */
package br.com.redemaisfarma.infra.logging;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
public class HttpAuditFilter
implements Filter {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(HttpAuditFilter.class);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest http = (HttpServletRequest)req;
        HttpServletResponse httpRes = (HttpServletResponse)res;
        String path = http.getRequestURI();
        if (path.startsWith("/actuator")) {
            chain.doFilter(req, res);
            return;
        }
        String cid = http.getHeader("X-Correlation-Id");
        if (cid == null || cid.isBlank()) {
            cid = UUID.randomUUID().toString();
        }
        MDC.put((String)"cid", (String)cid);
        long t0 = System.currentTimeMillis();
        ContentCachingRequestWrapper reqW = new ContentCachingRequestWrapper(http);
        ContentCachingResponseWrapper resW = new ContentCachingResponseWrapper(httpRes);
        try {
            chain.doFilter((ServletRequest)reqW, (ServletResponse)resW);
        }
        catch (Throwable throwable) {
            long took = System.currentTimeMillis() - t0;
            String user = SecurityContextHolder.getContext().getAuthentication() != null ? SecurityContextHolder.getContext().getAuthentication().getName() : "ANONYMOUS";
            String method = http.getMethod();
            String ip = http.getRemoteAddr();
            int status = resW.getStatus();
            int reqSize = reqW.getContentAsByteArray() != null ? reqW.getContentAsByteArray().length : 0;
            int resSize = resW.getContentSize();
            log.info("HTTP {} {} status={} user={} ip={} tookMs={} cid={} reqSize={} resSize={}", new Object[]{method, path, status, user, ip, took, cid, reqSize, resSize});
            resW.copyBodyToResponse();
            MDC.clear();
            throw throwable;
        }
        long took = System.currentTimeMillis() - t0;
        String user = SecurityContextHolder.getContext().getAuthentication() != null ? SecurityContextHolder.getContext().getAuthentication().getName() : "ANONYMOUS";
        String method = http.getMethod();
        String ip = http.getRemoteAddr();
        int status = resW.getStatus();
        int reqSize = reqW.getContentAsByteArray() != null ? reqW.getContentAsByteArray().length : 0;
        int resSize = resW.getContentSize();
        log.info("HTTP {} {} status={} user={} ip={} tookMs={} cid={} reqSize={} resSize={}", new Object[]{method, path, status, user, ip, took, cid, reqSize, resSize});
        resW.copyBodyToResponse();
        MDC.clear();
    }
}

