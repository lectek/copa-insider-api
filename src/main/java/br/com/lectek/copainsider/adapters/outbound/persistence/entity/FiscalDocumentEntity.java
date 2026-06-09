package br.com.lectek.copainsider.adapters.outbound.persistence.entity;

import br.com.lectek.copainsider.domain.fiscal.FiscalDocumentModel;
import br.com.lectek.copainsider.domain.fiscal.FiscalDocumentStatus;
import br.com.lectek.copainsider.domain.fiscal.FiscalEnvironment;
import br.com.lectek.copainsider.domain.fiscal.FiscalProvider;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "fiscal_document",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_fiscal_document_reference",
                        columnNames = "external_reference"
                )
        }
)
public class FiscalDocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private PedidoEntity pedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private FiscalProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "model", nullable = false, length = 20)
    private FiscalDocumentModel model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FiscalEnvironment environment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private FiscalDocumentStatus status;

    @Column(name = "external_reference", nullable = false, length = 120)
    private String externalReference;

    @Column(name = "external_id", length = 120)
    private String externalId;

    @Column(name = "access_key", length = 64)
    private String accessKey;

    @Column(name = "series")
    private Integer series;

    @Column(name = "document_number")
    private Integer documentNumber;

    @Column(name = "protocol", length = 80)
    private String protocol;

    @Column(name = "issuer_cnpj", length = 14)
    private String issuerCnpj;

    @Column(name = "recipient_document", length = 20)
    private String recipientDocument;

    @Column(name = "total_amount", precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "authorized_at")
    private LocalDateTime authorizedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "last_status_at")
    private LocalDateTime lastStatusAt;

    @Column(name = "xml_storage_path", length = 512)
    private String xmlStoragePath;

    @Column(name = "danfe_storage_path", length = 512)
    private String danfeStoragePath;

    @Column(name = "email_delivery_sent_at")
    private LocalDateTime emailDeliverySentAt;

    @Lob
    @Column(name = "request_payload")
    private String requestPayload;

    @Lob
    @Column(name = "response_payload")
    private String responsePayload;

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
            status = FiscalDocumentStatus.DRAFT;
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
        externalReference = trim(externalReference);
        externalId = trim(externalId);
        accessKey = trim(accessKey);
        protocol = trim(protocol);
        issuerCnpj = digits(issuerCnpj);
        recipientDocument = digits(recipientDocument);
        xmlStoragePath = trim(xmlStoragePath);
        danfeStoragePath = trim(danfeStoragePath);
        requestPayload = trim(requestPayload);
        responsePayload = trim(responsePayload);
        errorMessage = trim(errorMessage);
    }

    private static String trim(final String value) {
        return value == null ? null : value.trim();
    }

    private static String digits(final String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("\\D", "");
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long idValue) {
        id = idValue;
    }

    public PedidoEntity getPedido() {
        return pedido;
    }

    public void setPedido(final PedidoEntity pedidoValue) {
        pedido = pedidoValue;
    }

    public FiscalProvider getProvider() {
        return provider;
    }

    public void setProvider(final FiscalProvider providerValue) {
        provider = providerValue;
    }

    public FiscalDocumentModel getModel() {
        return model;
    }

    public void setModel(final FiscalDocumentModel modelValue) {
        model = modelValue;
    }

    public FiscalEnvironment getEnvironment() {
        return environment;
    }

    public void setEnvironment(final FiscalEnvironment environmentValue) {
        environment = environmentValue;
    }

    public FiscalDocumentStatus getStatus() {
        return status;
    }

    public void setStatus(final FiscalDocumentStatus statusValue) {
        status = statusValue;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(final String externalReferenceValue) {
        externalReference = externalReferenceValue;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(final String externalIdValue) {
        externalId = externalIdValue;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(final String accessKeyValue) {
        accessKey = accessKeyValue;
    }

    public Integer getSeries() {
        return series;
    }

    public void setSeries(final Integer seriesValue) {
        series = seriesValue;
    }

    public Integer getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(final Integer documentNumberValue) {
        documentNumber = documentNumberValue;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(final String protocolValue) {
        protocol = protocolValue;
    }

    public String getIssuerCnpj() {
        return issuerCnpj;
    }

    public void setIssuerCnpj(final String issuerCnpjValue) {
        issuerCnpj = issuerCnpjValue;
    }

    public String getRecipientDocument() {
        return recipientDocument;
    }

    public void setRecipientDocument(final String recipientDocumentValue) {
        recipientDocument = recipientDocumentValue;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(final BigDecimal totalAmountValue) {
        totalAmount = totalAmountValue;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(final LocalDateTime issuedAtValue) {
        issuedAt = issuedAtValue;
    }

    public LocalDateTime getAuthorizedAt() {
        return authorizedAt;
    }

    public void setAuthorizedAt(final LocalDateTime authorizedAtValue) {
        authorizedAt = authorizedAtValue;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(final LocalDateTime cancelledAtValue) {
        cancelledAt = cancelledAtValue;
    }

    public LocalDateTime getLastStatusAt() {
        return lastStatusAt;
    }

    public void setLastStatusAt(final LocalDateTime lastStatusAtValue) {
        lastStatusAt = lastStatusAtValue;
    }

    public String getXmlStoragePath() {
        return xmlStoragePath;
    }

    public void setXmlStoragePath(final String xmlStoragePathValue) {
        xmlStoragePath = xmlStoragePathValue;
    }

    public String getDanfeStoragePath() {
        return danfeStoragePath;
    }

    public void setDanfeStoragePath(final String danfeStoragePathValue) {
        danfeStoragePath = danfeStoragePathValue;
    }

    public LocalDateTime getEmailDeliverySentAt() {
        return emailDeliverySentAt;
    }

    public void setEmailDeliverySentAt(
            final LocalDateTime emailDeliverySentAtValue
    ) {
        emailDeliverySentAt = emailDeliverySentAtValue;
    }

    public String getRequestPayload() {
        return requestPayload;
    }

    public void setRequestPayload(final String requestPayloadValue) {
        requestPayload = requestPayloadValue;
    }

    public String getResponsePayload() {
        return responsePayload;
    }

    public void setResponsePayload(final String responsePayloadValue) {
        responsePayload = responsePayloadValue;
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
