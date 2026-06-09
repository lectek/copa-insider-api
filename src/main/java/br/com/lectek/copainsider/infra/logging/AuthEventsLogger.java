/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.context.event.EventListener
 *  org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent
 *  org.springframework.security.authentication.event.AuthenticationSuccessEvent
 *  org.springframework.security.core.Authentication
 *  org.springframework.stereotype.Component
 */
package br.com.lectek.copainsider.infra.logging;

import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class AuthEventsLogger {
    @Generated
    private static final Logger log = LoggerFactory.getLogger(AuthEventsLogger.class);

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent e) {
        Authentication a = e.getAuthentication();
        String user = a.getName();
        log.info("AUTH_SUCCESS user={} details={}", (Object)user, a.getDetails());
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent e) {
        Authentication a = e.getAuthentication();
        String user = a != null ? a.getName() : "unknown";
        String reason = e.getException() != null ? ((Object)((Object)e.getException())).getClass().getSimpleName() + ": " + e.getException().getMessage() : "unknown";
        log.warn("AUTH_FAIL user={} reason={} details={}", new Object[]{user, reason, a != null ? a.getDetails() : null});
    }
}

