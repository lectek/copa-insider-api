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
 *  org.springframework.core.annotation.Order
 *  org.springframework.stereotype.Component
 */
package br.com.redemaisfarma.adapters.inbound.web.filter;

import br.com.redemaisfarma.application.service.otp.OtpServicePort;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(value=-2147483628)
public class OtpTokenFilter
implements Filter {
    private final OtpServicePort otp;

    public OtpTokenFilter(ObjectProvider<OtpServicePort> otpProvider) {
        this.otp = otpProvider.getIfAvailable();
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)request;
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(request, response);
            return;
        }
        if (otp == null) {
            chain.doFilter(request, response);
            return;
        }
        if ("/clientes".equals(req.getRequestURI()) && "POST".equalsIgnoreCase(req.getMethod())) {
            String token = req.getParameter("otpToken");
            if (token == null || token.isBlank()) {
                token = req.getHeader("X-OTP-TOKEN");
            }
            if (token == null || token.isBlank() || !this.otp.consumeToken(token)) {
                HttpServletResponse resp = (HttpServletResponse)response;
                resp.setStatus(400);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"message\":\"Verifica\u00e7\u00e3o necess\u00e1ria: c\u00f3digo inv\u00e1lido ou ausente.\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
