/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Component
 *  org.springframework.web.context.annotation.RequestScope
 */
package br.com.lectek.copainsider.adapters.inbound.web.session;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class UserSession {
    private Long userId;
    private String email;
    private String role;

    public UserSession() {
    }

    public UserSession(Long userId, String email, String role) {
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return this.role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String toString() {
        return "UserSession{userId=" + this.userId + ", email='" + this.email + "', role='" + this.role + "'}";
    }
}

