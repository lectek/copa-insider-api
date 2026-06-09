/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.persistence.Column
 *  jakarta.persistence.Entity
 *  jakarta.persistence.GeneratedValue
 *  jakarta.persistence.GenerationType
 *  jakarta.persistence.Id
 *  jakarta.persistence.Index
 *  jakarta.persistence.PrePersist
 *  jakarta.persistence.Table
 */
package br.com.redemaisfarma.user.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name="role_audit", indexes={@Index(name="idx_role_audit_target", columnList="target_user_id, created_at"), @Index(name="idx_role_audit_actor", columnList="actor_email, created_at")})
public class RoleAudit {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(name="action", length=20, nullable=false)
    private String action;
    @Column(name="target_user_id", nullable=false)
    private Long targetUserId;
    @Column(name="role_name", length=50, nullable=false)
    private String roleName;
    @Column(name="actor_email", length=150)
    private String actorEmail;
    @Column(name="ip", length=64)
    private String ip;
    @Column(name="user_agent", length=255)
    private String userAgent;
    @Column(name="created_at", nullable=false, updatable=false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public Long getId() {
        return this.id;
    }

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Long getTargetUserId() {
        return this.targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getRoleName() {
        return this.roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getActorEmail() {
        return this.actorEmail;
    }

    public void setActorEmail(String actorEmail) {
        this.actorEmail = actorEmail;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        RoleAudit that = (RoleAudit)o;
        return Objects.equals(this.id, that.id);
    }

    public int hashCode() {
        return Objects.hash(this.id);
    }

    public String toString() {
        return "RoleAudit{id=" + this.id + ", action='" + this.action + "', targetUserId=" + this.targetUserId + ", roleName='" + this.roleName + "', actorEmail='" + this.actorEmail + "', ip='" + this.ip + "', createdAt=" + String.valueOf(this.createdAt) + "}";
    }
}

