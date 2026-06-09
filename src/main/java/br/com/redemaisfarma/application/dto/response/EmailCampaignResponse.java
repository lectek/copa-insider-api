package br.com.redemaisfarma.application.dto.response;

import java.time.Instant;

public class EmailCampaignResponse {
    private Long id;
    private String nome;
    private String assunto;
    private String templateKey;
    private String status;
    private Instant scheduledAt;
    private Instant createdAt;
    private Instant updatedAt;
    private String segmentLabel;
    private String segmentoDetalhado;
    private String scheduledZone;
    private String validationStatus;

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

    public String getSegmentLabel() {
        return segmentLabel;
    }

    public void setSegmentLabel(String segmentLabel) {
        this.segmentLabel = segmentLabel;
    }

    public String getSegmentoDetalhado() {
        return segmentoDetalhado;
    }

    public void setSegmentoDetalhado(String segmentoDetalhado) {
        this.segmentoDetalhado = segmentoDetalhado;
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
}
