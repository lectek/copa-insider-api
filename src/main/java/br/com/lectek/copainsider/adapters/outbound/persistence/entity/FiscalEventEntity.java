package br.com.lectek.copainsider.adapters.outbound.persistence.entity;

import br.com.lectek.copainsider.domain.fiscal.FiscalDocumentStatus;
import br.com.lectek.copainsider.domain.fiscal.FiscalEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "fiscal_event")
public class FiscalEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fiscal_document_id", nullable = false)
    private FiscalDocumentEntity fiscalDocument;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private FiscalEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_before", length = 40)
    private FiscalDocumentStatus statusBefore;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_after", length = 40)
    private FiscalDocumentStatus statusAfter;

    @Column(name = "provider_event_id", length = 120)
    private String providerEventId;

    @Column(name = "message", length = 500)
    private String message;

    @Lob
    @Column(name = "payload")
    private String payload;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        providerEventId = trim(providerEventId);
        message = trim(message);
        payload = trim(payload);
    }

    private static String trim(final String value) {
        return value == null ? null : value.trim();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long idValue) {
        id = idValue;
    }

    public FiscalDocumentEntity getFiscalDocument() {
        return fiscalDocument;
    }

    public void setFiscalDocument(final FiscalDocumentEntity fiscalDocumentValue) {
        fiscalDocument = fiscalDocumentValue;
    }

    public FiscalEventType getEventType() {
        return eventType;
    }

    public void setEventType(final FiscalEventType eventTypeValue) {
        eventType = eventTypeValue;
    }

    public FiscalDocumentStatus getStatusBefore() {
        return statusBefore;
    }

    public void setStatusBefore(final FiscalDocumentStatus statusBeforeValue) {
        statusBefore = statusBeforeValue;
    }

    public FiscalDocumentStatus getStatusAfter() {
        return statusAfter;
    }

    public void setStatusAfter(final FiscalDocumentStatus statusAfterValue) {
        statusAfter = statusAfterValue;
    }

    public String getProviderEventId() {
        return providerEventId;
    }

    public void setProviderEventId(final String providerEventIdValue) {
        providerEventId = providerEventIdValue;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String messageValue) {
        message = messageValue;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(final String payloadValue) {
        payload = payloadValue;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(final LocalDateTime processedAtValue) {
        processedAt = processedAtValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
