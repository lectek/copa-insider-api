package br.com.redemaisfarma.adapters.outbound.persistence.entity;

import br.com.redemaisfarma.domain.fiscal.FiscalDocumentModel;
import br.com.redemaisfarma.domain.fiscal.FiscalPrintChannel;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "pedido_fiscal_snapshot",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_pedido_fiscal_snapshot_pedido",
                        columnNames = "pedido_id"
                )
        }
)
public class PedidoFiscalSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private PedidoEntity pedido;

    @Column(name = "source", nullable = false, length = 40)
    private String source;

    @Enumerated(EnumType.STRING)
    @Column(name = "suggested_document_model", nullable = false, length = 20)
    private FiscalDocumentModel suggestedDocumentModel;

    @Column(name = "recipient_name", length = 160)
    private String recipientName;

    @Column(name = "recipient_document", length = 20)
    private String recipientDocument;

    @Column(name = "recipient_email", length = 150)
    private String recipientEmail;

    @Column(name = "recipient_phone", length = 25)
    private String recipientPhone;

    @Column(name = "recipient_address", length = 255)
    private String recipientAddress;

    @Column(name = "issuer_cnpj", length = 14)
    private String issuerCnpj;

    @Column(name = "payment_method", length = 120)
    private String paymentMethod;

    @Column(name = "shipping_amount", precision = 19, scale = 2)
    private BigDecimal shippingAmount;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "print_channel", nullable = false, length = 30)
    private FiscalPrintChannel printChannel;

    @Column(name = "email_delivery_requested", nullable = false)
    private boolean emailDeliveryRequested;

    @Column(name = "email_delivery_address", length = 180)
    private String emailDeliveryAddress;

    @Lob
    @Column(name = "payload_json")
    private String payloadJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        source = trim(source);
        recipientName = trim(recipientName);
        recipientDocument = digits(recipientDocument);
        recipientEmail = trim(recipientEmail);
        recipientPhone = trim(recipientPhone);
        recipientAddress = trim(recipientAddress);
        issuerCnpj = digits(issuerCnpj);
        paymentMethod = trim(paymentMethod);
        emailDeliveryAddress = trim(emailDeliveryAddress);
        payloadJson = trim(payloadJson);
        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
        }
        if (shippingAmount == null) {
            shippingAmount = BigDecimal.ZERO;
        }
        if (printChannel == null) {
            printChannel = FiscalPrintChannel.NONE;
        }
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

    public String getSource() {
        return source;
    }

    public void setSource(final String sourceValue) {
        source = sourceValue;
    }

    public FiscalDocumentModel getSuggestedDocumentModel() {
        return suggestedDocumentModel;
    }

    public void setSuggestedDocumentModel(
            final FiscalDocumentModel suggestedDocumentModelValue
    ) {
        suggestedDocumentModel = suggestedDocumentModelValue;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(final String recipientNameValue) {
        recipientName = recipientNameValue;
    }

    public String getRecipientDocument() {
        return recipientDocument;
    }

    public void setRecipientDocument(final String recipientDocumentValue) {
        recipientDocument = recipientDocumentValue;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(final String recipientEmailValue) {
        recipientEmail = recipientEmailValue;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(final String recipientPhoneValue) {
        recipientPhone = recipientPhoneValue;
    }

    public String getRecipientAddress() {
        return recipientAddress;
    }

    public void setRecipientAddress(final String recipientAddressValue) {
        recipientAddress = recipientAddressValue;
    }

    public String getIssuerCnpj() {
        return issuerCnpj;
    }

    public void setIssuerCnpj(final String issuerCnpjValue) {
        issuerCnpj = issuerCnpjValue;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(final String paymentMethodValue) {
        paymentMethod = paymentMethodValue;
    }

    public BigDecimal getShippingAmount() {
        return shippingAmount;
    }

    public void setShippingAmount(final BigDecimal shippingAmountValue) {
        shippingAmount = shippingAmountValue;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(final BigDecimal totalAmountValue) {
        totalAmount = totalAmountValue;
    }

    public FiscalPrintChannel getPrintChannel() {
        return printChannel;
    }

    public void setPrintChannel(final FiscalPrintChannel printChannelValue) {
        printChannel = printChannelValue;
    }

    public boolean isEmailDeliveryRequested() {
        return emailDeliveryRequested;
    }

    public void setEmailDeliveryRequested(
            final boolean emailDeliveryRequestedValue
    ) {
        emailDeliveryRequested = emailDeliveryRequestedValue;
    }

    public String getEmailDeliveryAddress() {
        return emailDeliveryAddress;
    }

    public void setEmailDeliveryAddress(
            final String emailDeliveryAddressValue
    ) {
        emailDeliveryAddress = emailDeliveryAddressValue;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public void setPayloadJson(final String payloadJsonValue) {
        payloadJson = payloadJsonValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
