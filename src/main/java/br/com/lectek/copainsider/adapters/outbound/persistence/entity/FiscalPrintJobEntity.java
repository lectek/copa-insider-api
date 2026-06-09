package br.com.lectek.copainsider.adapters.outbound.persistence.entity;

import br.com.lectek.copainsider.domain.fiscal.FiscalPrintChannel;
import br.com.lectek.copainsider.domain.fiscal.FiscalPrintJobStatus;
import br.com.lectek.copainsider.domain.fiscal.FiscalPrintJobType;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;

@Entity
@Table(name = "fiscal_print_job")
public class FiscalPrintJobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fiscal_document_id", nullable = false)
    private FiscalDocumentEntity fiscalDocument;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private PedidoEntity pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id")
    private FiscalPrintStationEntity station;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 30)
    private FiscalPrintJobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FiscalPrintJobStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "print_channel", nullable = false, length = 30)
    private FiscalPrintChannel printChannel;

    @Column(nullable = false)
    private Integer priority;

    @Column(nullable = false)
    private Integer copies;

    @Column(name = "source", length = 40)
    private String source;

    @Column(name = "scheduled_for")
    private LocalDateTime scheduledFor;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "reprint_of_job_id")
    private Long reprintOfJobId;

    @Column(name = "created_by", length = 80)
    private String createdBy;

    @Column(name = "last_actor", length = 80)
    private String lastActor;

    @Column(name = "cancel_reason", length = 255)
    private String cancelReason;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    @PrePersist
    public void onCreate() {
        final LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        normalize();
        if (status == null) {
            status = FiscalPrintJobStatus.WAITING_DOCUMENT;
        }
        if (priority == null) {
            priority = 50;
        }
        if (copies == null || copies <= 0) {
            copies = 1;
        }
        if (version == null) {
            version = 0L;
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
        normalize();
    }

    private void normalize() {
        source = trim(source);
        createdBy = trim(createdBy);
        lastActor = trim(lastActor);
        cancelReason = trim(cancelReason);
        errorMessage = trim(errorMessage);
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

    public PedidoEntity getPedido() {
        return pedido;
    }

    public void setPedido(final PedidoEntity pedidoValue) {
        pedido = pedidoValue;
    }

    public FiscalPrintStationEntity getStation() {
        return station;
    }

    public void setStation(final FiscalPrintStationEntity stationValue) {
        station = stationValue;
    }

    public FiscalPrintJobType getJobType() {
        return jobType;
    }

    public void setJobType(final FiscalPrintJobType jobTypeValue) {
        jobType = jobTypeValue;
    }

    public FiscalPrintJobStatus getStatus() {
        return status;
    }

    public void setStatus(final FiscalPrintJobStatus statusValue) {
        status = statusValue;
    }

    public FiscalPrintChannel getPrintChannel() {
        return printChannel;
    }

    public void setPrintChannel(final FiscalPrintChannel printChannelValue) {
        printChannel = printChannelValue;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(final Integer priorityValue) {
        priority = priorityValue;
    }

    public Integer getCopies() {
        return copies;
    }

    public void setCopies(final Integer copiesValue) {
        copies = copiesValue;
    }

    public String getSource() {
        return source;
    }

    public void setSource(final String sourceValue) {
        source = sourceValue;
    }

    public LocalDateTime getScheduledFor() {
        return scheduledFor;
    }

    public void setScheduledFor(final LocalDateTime scheduledForValue) {
        scheduledFor = scheduledForValue;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(final LocalDateTime startedAtValue) {
        startedAt = startedAtValue;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(final LocalDateTime completedAtValue) {
        completedAt = completedAtValue;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(final LocalDateTime cancelledAtValue) {
        cancelledAt = cancelledAtValue;
    }

    public Long getReprintOfJobId() {
        return reprintOfJobId;
    }

    public void setReprintOfJobId(final Long reprintOfJobIdValue) {
        reprintOfJobId = reprintOfJobIdValue;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(final String createdByValue) {
        createdBy = createdByValue;
    }

    public String getLastActor() {
        return lastActor;
    }

    public void setLastActor(final String lastActorValue) {
        lastActor = lastActorValue;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(final String cancelReasonValue) {
        cancelReason = cancelReasonValue;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(final String errorMessageValue) {
        errorMessage = errorMessageValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }
}
