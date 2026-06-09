package br.com.lectek.copainsider.adapters.outbound.persistence.entity;

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
@Table(
        name = "email_campaign",
        indexes = {
                @Index(name = "idx_email_campaign_status_scheduled", columnList = "status,scheduled_at")
        }
)
public class EmailCampaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "nome", length = 120, nullable = false)
    private String nome;
    @Column(name = "assunto", length = 180, nullable = false)
    private String assunto;
    @Column(name = "template_key", length = 120, nullable = false)
    private String templateKey;
    @Column(name = "status", length = 20, nullable = false)
    private String status;
    @Column(name = "scheduled_at")
    private Instant scheduledAt;
    @Column(name = "segment_json", columnDefinition = "json")
    private String segmentJson;
    @Column(name = "segment_detail", length = 255)
    private String segmentDetail;
    @Column(name = "scheduled_zone", length = 64)
    private String scheduledZone;
    @Column(name = "validation_status", length = 32)
    private String validationStatus;
    @Column(name = "created_by", length = 120)
    private String createdBy;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public String getTemplateKey() {
        return templateKey;
    }

    public void setTemplateKey(String templateKey) {
        this.templateKey = templateKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Instant scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getSegmentJson() {
        return segmentJson;
    }

    public void setSegmentJson(String segmentJson) {
        this.segmentJson = segmentJson;
    }

    public String getSegmentDetail() {
        return segmentDetail;
    }

    public void setSegmentDetail(String segmentDetail) {
        this.segmentDetail = segmentDetail;
    }

    public String getScheduledZone() {
        return scheduledZone;
    }

    public void setScheduledZone(String scheduledZone) {
        this.scheduledZone = scheduledZone;
    }

    public String getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(String validationStatus) {
        this.validationStatus = validationStatus;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
