/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.context.event.EventListener
 *  org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent
 *  org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent
 *  org.springframework.stereotype.Component
 */
package br.com.lectek.copainsider.adapters.inbound.web.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class SecurityEvents {
    private static final Logger log = LoggerFactory.getLogger(SecurityEvents.class);

    @EventListener
    public void handleAuthSuccess(InteractiveAuthenticationSuccessEvent e) {
        log.info("\u2705 Autenticado: {}", (Object)e.getAuthentication().getName());
    }

    @EventListener
    public void handleAuthFailure(AbstractAuthenticationFailureEvent e) {
        log.warn("\u274c Falha de auth: {} - {}", (Object)e.getAuthentication().getName(), (Object)e.getException().getMessage());
    }
}

