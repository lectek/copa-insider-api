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
 *  jakarta.persistence.Table
 *  jakarta.persistence.Transient
 */
package br.com.redemaisfarma.adapters.outbound.persistence.jpa.otp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name="otp_code", indexes={@Index(name="idx_otp_destination", columnList="destination"), @Index(name="idx_otp_expires", columnList="expires_at"), @Index(name="idx_otp_verification_token", columnList="verification_token")})
public class OtpCodeEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(name="delivery_id", length=128, nullable=false, unique=true)
    private String deliveryId;
    @Column(name="destination", length=255, nullable=false)
    private String destination;
    @Column(name="code_hash", length=255, nullable=false)
    private String codeHash;
    @Column(name="salt", length=64, nullable=false)
    private String salt;
    @Column(name="ttl_seconds", nullable=false)
    private int ttlSeconds;
    @Column(name="attempts", nullable=false)
    private int attempts;
    @Column(name="max_attempts", nullable=false)
    private int maxAttempts;
    @Column(name="created_at", nullable=false)
    private Instant createdAt;
    @Column(name="expires_at", nullable=false)
    private Instant expiresAt;
    @Column(name="status", length=20, nullable=false)
    private String status;
    @Column(name="verification_token", length=255)
    private String verificationToken;
    @Column(name="verified_at")
    private Instant verifiedAt;
    @Column(name="consumed_at")
    private Instant consumedAt;

    public Long getId() {
        return this.id;
    }

    public String getDeliveryId() {
        return this.deliveryId;
    }

    public void setDeliveryId(String deliveryId) {
        this.deliveryId = deliveryId;
    }

    public String getDestination() {
        return this.destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getCodeHash() {
        return this.codeHash;
    }

    public void setCodeHash(String codeHash) {
        this.codeHash = codeHash;
    }

    public String getSalt() {
        return this.salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public int getTtlSeconds() {
        return this.ttlSeconds;
    }

    public void setTtlSeconds(int ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    public int getAttempts() {
        return this.attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public int getMaxAttempts() {
        return this.maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return this.expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVerificationToken() {
        return this.verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    public Instant getVerifiedAt() {
        return this.verifiedAt;
    }

    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public Instant getConsumedAt() {
        return this.consumedAt;
    }

    public void setConsumedAt(Instant consumedAt) {
        this.consumedAt = consumedAt;
    }

    @Transient
    public boolean isExpired() {
        return this.expiresAt != null && Instant.now().isAfter(this.expiresAt);
    }

    @Transient
    public boolean isBlocked() {
        return "BLOCKED".equalsIgnoreCase(this.status);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OtpCodeEntity)) {
            return false;
        }
        OtpCodeEntity that = (OtpCodeEntity)o;
        return Objects.equals(this.deliveryId, that.deliveryId);
    }

    public int hashCode() {
        return Objects.hash(this.deliveryId);
    }

    public String toString() {
        return "OtpCodeEntity{id=" + String.valueOf(this.id) + ", deliveryId='" + this.deliveryId + "', destination='" + this.destination + "', attempts=" + this.attempts + ", status='" + this.status + "', expiresAt=" + String.valueOf(this.expiresAt) + "}";
    }
}

