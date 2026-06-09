package br.com.lectek.copainsider.adapters.outbound.persistence.entity;

import br.com.lectek.copainsider.domain.fiscal.FiscalPrintJobEventType;
import br.com.lectek.copainsider.domain.fiscal.FiscalPrintJobStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "fiscal_print_job_event")
public class FiscalPrintJobEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "print_job_id", nullable = false)
    private FiscalPrintJobEntity printJob;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private FiscalPrintJobEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_before", length = 30)
    private FiscalPrintJobStatus statusBefore;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_after", length = 30)
    private FiscalPrintJobStatus statusAfter;

    @Column(length = 255)
    private String message;

    @Column(length = 80)
    private String actor;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        message = trim(message);
        actor = trim(actor);
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

    public FiscalPrintJobEntity getPrintJob() {
        return printJob;
    }

    public void setPrintJob(final FiscalPrintJobEntity printJobValue) {
        printJob = printJobValue;
    }

    public FiscalPrintJobEventType getEventType() {
        return eventType;
    }

    public void setEventType(final FiscalPrintJobEventType eventTypeValue) {
        eventType = eventTypeValue;
    }

    public FiscalPrintJobStatus getStatusBefore() {
        return statusBefore;
    }

    public void setStatusBefore(final FiscalPrintJobStatus statusBeforeValue) {
        statusBefore = statusBeforeValue;
    }

    public FiscalPrintJobStatus getStatusAfter() {
        return statusAfter;
    }

    public void setStatusAfter(final FiscalPrintJobStatus statusAfterValue) {
        statusAfter = statusAfterValue;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String messageValue) {
        message = messageValue;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(final String actorValue) {
        actor = actorValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
