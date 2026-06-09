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
 *  org.hibernate.annotations.CreationTimestamp
 *  org.hibernate.annotations.UpdateTimestamp
 */
package br.com.redemaisfarma.adapters.outbound.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name="email_delivery", indexes={@Index(name="idx_email_delivery_status_created", columnList="status,created_at"), @Index(name="idx_email_delivery_destination", columnList="destination")})
public class EmailDelivery {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(name="purpose", length=64, nullable=false)
    private String purpose;
    @Column(name="destination", length=254, nullable=false)
    private String destination;
    @Column(name="payload_json", columnDefinition="json", nullable=false)
    private String payloadJson;
    @Column(name="provider", length=32, nullable=false)
    private String provider;
    @Column(name="status", length=16, nullable=false)
    private String status;
    @Column(name="attempts", nullable=false)
    private Integer attempts = 0;
    @Column(name="last_error", columnDefinition="text")
    private String lastError;
    @Column(name="message_id", length=191)
    private String messageId;
    @CreationTimestamp
    @Column(name="created_at", updatable=false, nullable=false)
    private Instant createdAt;
    @UpdateTimestamp
    @Column(name="updated_at")
    private Instant updatedAt;

    public Long getId() {
        return this.id;
    }

    public String getPurpose() {
        return this.purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getDestination() {
        return this.destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getPayloadJson() {
        return this.payloadJson;
    }

    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }

    public String getProvider() {
        return this.provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getAttempts() {
        return this.attempts;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }

    public String getLastError() {
        return this.lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public String getMessageId() {
        return this.messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

