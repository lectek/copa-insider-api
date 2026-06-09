package br.com.lectek.copainsider.adapters.outbound.persistence.entity;

import br.com.lectek.copainsider.domain.enums.StatusPedido;
import br.com.lectek.copainsider.domain.enums.TipoPagamento;
import br.com.lectek.copainsider.domain.enums.ModoEntrega;
import br.com.lectek.copainsider.domain.enums.MotivoCancelamentoPedido;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "pedido")
@Access(AccessType.FIELD)
@EntityListeners(AuditingEntityListener.class)
public class PedidoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private ClienteEntity cliente;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime data;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = true, message = "Total não pode ser negativo")
    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal total;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedidoEntity> itens = new ArrayList<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPedido status;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pagamento", nullable = false, length = 30)
    private TipoPagamento tipoPagamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "modo_entrega", length = 20)
    private ModoEntrega modoEntrega;

    @Column(name = "metodo_pagamento", length = 80)
    private String metodoPagamento;

    @Column(name = "forma_pagamento_recebida", length = 30)
    private String formaPagamentoRecebida;

    @Column(name = "pagamento_divergente", nullable = false)
    private boolean pagamentoDivergente;

    @Column(name = "avaliacao_cliente")
    private Integer avaliacaoCliente;

    @Column(name = "pagamento_recebido_em")
    private LocalDateTime pagamentoRecebidoEm;

    @Column(name = "gateway_provider", length = 40)
    private String gatewayProvider;

    @Column(name = "gateway_owner_reference", length = 120)
    private String gatewayOwnerReference;

    @Column(name = "gateway_preference_id", length = 80)
    private String gatewayPreferenceId;

    @Column(name = "gateway_external_reference", length = 120)
    private String gatewayExternalReference;

    @Column(name = "gateway_checkout_url", length = 1024)
    private String gatewayCheckoutUrl;

    @Column(name = "gateway_payment_id", length = 80)
    private String gatewayPaymentId;

    @Column(name = "gateway_payment_status", length = 40)
    private String gatewayPaymentStatus;

    @Column(name = "gateway_payment_status_detail", length = 255)
    private String gatewayPaymentStatusDetail;

    @Column(name = "gateway_payment_updated_at")
    private LocalDateTime gatewayPaymentUpdatedAt;

    @Column(name = "gateway_payment_ticket_url", length = 1024)
    private String gatewayPaymentTicketUrl;

    @Column(name = "gateway_pix_qr_code", length = 4096)
    private String gatewayPixQrCode;

    @Lob
    @Column(name = "gateway_pix_qr_code_base64", columnDefinition = "MEDIUMTEXT")
    private String gatewayPixQrCodeBase64;

    @Column(name = "endereco_entrega", length = 255)
    private String enderecoEntrega;

    @Column(name = "codigo_entrega", length = 6)
    private String codigoEntrega;

    @Column(name = "codigo_entrega_gerado_em")
    private LocalDateTime codigoEntregaGeradoEm;

    @Column(name = "codigo_entrega_confirmado_em")
    private LocalDateTime codigoEntregaConfirmadoEm;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancelamento_motivo", length = 40)
    private MotivoCancelamentoPedido cancelamentoMotivo;

    @Column(name = "cancelado_em")
    private LocalDateTime canceladoEm;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    /* ==================================================
       Métodos utilitários para consistência relacional
       ================================================== */

    public void addItem(ItemPedidoEntity item) {
        if (item == null) return;
        itens.add(item);
        item.setPedido(this);
    }

    public void removeItem(ItemPedidoEntity item) {
        if (item == null) return;
        itens.remove(item);
        item.setPedido(null);
    }

    @PrePersist
    public void prePersist() {
        if (data == null) data = LocalDateTime.now();
        if (total == null) total = BigDecimal.ZERO;
        if (modoEntrega == null) modoEntrega = ModoEntrega.ENTREGA;
    }

    /* ==================================================
       Getters e Setters
       ================================================== */

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ClienteEntity getCliente() { return cliente; }
    public void setCliente(ClienteEntity cliente) { this.cliente = cliente; }

    public LocalDateTime getData() { return data; }
    public void setData(LocalDateTime data) { this.data = data; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public List<ItemPedidoEntity> getItens() { return itens; }
    public void setItens(List<ItemPedidoEntity> itens) {
        this.itens.clear();
        if (itens != null) itens.forEach(this::addItem);
    }

    public StatusPedido getStatus() { return status; }
    public void setStatus(StatusPedido status) { this.status = status; }

    public TipoPagamento getTipoPagamento() { return tipoPagamento; }
    public void setTipoPagamento(TipoPagamento tipoPagamento) { this.tipoPagamento = tipoPagamento; }

    public ModoEntrega getModoEntrega() { return modoEntrega; }
    public void setModoEntrega(ModoEntrega modoEntrega) { this.modoEntrega = modoEntrega; }

    public String getMetodoPagamento() { return metodoPagamento; }
    public void setMetodoPagamento(String metodoPagamento) { this.metodoPagamento = metodoPagamento; }

    public String getFormaPagamentoRecebida() {
        return formaPagamentoRecebida;
    }

    public void setFormaPagamentoRecebida(
            final String formaPagamentoRecebidaValue
    ) {
        formaPagamentoRecebida = formaPagamentoRecebidaValue;
    }

    public boolean isPagamentoDivergente() {
        return pagamentoDivergente;
    }

    public void setPagamentoDivergente(final boolean pagamentoDivergenteValue) {
        pagamentoDivergente = pagamentoDivergenteValue;
    }

    public Integer getAvaliacaoCliente() {
        return avaliacaoCliente;
    }

    public void setAvaliacaoCliente(final Integer avaliacaoClienteValue) {
        avaliacaoCliente = avaliacaoClienteValue;
    }

    public LocalDateTime getPagamentoRecebidoEm() {
        return pagamentoRecebidoEm;
    }

    public void setPagamentoRecebidoEm(
            final LocalDateTime pagamentoRecebidoEmValue
    ) {
        pagamentoRecebidoEm = pagamentoRecebidoEmValue;
    }

    public String getGatewayProvider() {
        return gatewayProvider;
    }

    public void setGatewayProvider(final String gatewayProviderValue) {
        gatewayProvider = gatewayProviderValue;
    }

    public String getGatewayOwnerReference() {
        return gatewayOwnerReference;
    }

    public void setGatewayOwnerReference(
            final String gatewayOwnerReferenceValue
    ) {
        gatewayOwnerReference = gatewayOwnerReferenceValue;
    }

    public String getGatewayPreferenceId() {
        return gatewayPreferenceId;
    }

    public void setGatewayPreferenceId(
            final String gatewayPreferenceIdValue
    ) {
        gatewayPreferenceId = gatewayPreferenceIdValue;
    }

    public String getGatewayExternalReference() {
        return gatewayExternalReference;
    }

    public void setGatewayExternalReference(
            final String gatewayExternalReferenceValue
    ) {
        gatewayExternalReference = gatewayExternalReferenceValue;
    }

    public String getGatewayCheckoutUrl() {
        return gatewayCheckoutUrl;
    }

    public void setGatewayCheckoutUrl(final String gatewayCheckoutUrlValue) {
        gatewayCheckoutUrl = gatewayCheckoutUrlValue;
    }

    public String getGatewayPaymentId() {
        return gatewayPaymentId;
    }

    public void setGatewayPaymentId(final String gatewayPaymentIdValue) {
        gatewayPaymentId = gatewayPaymentIdValue;
    }

    public String getGatewayPaymentStatus() {
        return gatewayPaymentStatus;
    }

    public void setGatewayPaymentStatus(
            final String gatewayPaymentStatusValue
    ) {
        gatewayPaymentStatus = gatewayPaymentStatusValue;
    }

    public String getGatewayPaymentStatusDetail() {
        return gatewayPaymentStatusDetail;
    }

    public void setGatewayPaymentStatusDetail(
            final String gatewayPaymentStatusDetailValue
    ) {
        gatewayPaymentStatusDetail = gatewayPaymentStatusDetailValue;
    }

    public LocalDateTime getGatewayPaymentUpdatedAt() {
        return gatewayPaymentUpdatedAt;
    }

    public void setGatewayPaymentUpdatedAt(
            final LocalDateTime gatewayPaymentUpdatedAtValue
    ) {
        gatewayPaymentUpdatedAt = gatewayPaymentUpdatedAtValue;
    }

    public String getGatewayPaymentTicketUrl() {
        return gatewayPaymentTicketUrl;
    }

    public void setGatewayPaymentTicketUrl(
            final String gatewayPaymentTicketUrlValue
    ) {
        gatewayPaymentTicketUrl = gatewayPaymentTicketUrlValue;
    }

    public String getGatewayPixQrCode() {
        return gatewayPixQrCode;
    }

    public void setGatewayPixQrCode(final String gatewayPixQrCodeValue) {
        gatewayPixQrCode = gatewayPixQrCodeValue;
    }

    public String getGatewayPixQrCodeBase64() {
        return gatewayPixQrCodeBase64;
    }

    public void setGatewayPixQrCodeBase64(
            final String gatewayPixQrCodeBase64Value
    ) {
        gatewayPixQrCodeBase64 = gatewayPixQrCodeBase64Value;
    }

    public String getEnderecoEntrega() { return enderecoEntrega; }
    public void setEnderecoEntrega(String enderecoEntrega) { this.enderecoEntrega = enderecoEntrega; }

    public String getCodigoEntrega() { return codigoEntrega; }
    public void setCodigoEntrega(String codigoEntrega) { this.codigoEntrega = codigoEntrega; }

    public LocalDateTime getCodigoEntregaGeradoEm() {
        return codigoEntregaGeradoEm;
    }
    public void setCodigoEntregaGeradoEm(LocalDateTime codigoEntregaGeradoEm) {
        this.codigoEntregaGeradoEm = codigoEntregaGeradoEm;
    }

    public LocalDateTime getCodigoEntregaConfirmadoEm() {
        return codigoEntregaConfirmadoEm;
    }
    public void setCodigoEntregaConfirmadoEm(
            LocalDateTime codigoEntregaConfirmadoEm
    ) {
        this.codigoEntregaConfirmadoEm = codigoEntregaConfirmadoEm;
    }

    public MotivoCancelamentoPedido getCancelamentoMotivo() {
        return cancelamentoMotivo;
    }
    public void setCancelamentoMotivo(
            MotivoCancelamentoPedido cancelamentoMotivoValue
    ) {
        cancelamentoMotivo = cancelamentoMotivoValue;
    }

    public LocalDateTime getCanceladoEm() {
        return canceladoEm;
    }
    public void setCanceladoEm(final LocalDateTime canceladoEmValue) {
        canceladoEm = canceladoEmValue;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    /* ==================================================
       equals, hashCode e toString
       ================================================== */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PedidoEntity that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PedidoEntity{" +
                "id=" + id +
                ", clienteId=" + (cliente != null ? cliente.getId() : null) +
                ", data=" + data +
                ", total=" + total +
                ", status=" + status +
                ", tipoPagamento=" + tipoPagamento +
                ", modoEntrega=" + modoEntrega +
                ", metodoPagamento=" + metodoPagamento +
                ", formaPagamentoRecebida=" + formaPagamentoRecebida +
                ", pagamentoDivergente=" + pagamentoDivergente +
                ", avaliacaoCliente=" + avaliacaoCliente +
                ", pagamentoRecebidoEm=" + pagamentoRecebidoEm +
                ", gatewayProvider=" + gatewayProvider +
                ", gatewayOwnerReference=" + gatewayOwnerReference +
                ", gatewayPreferenceId=" + gatewayPreferenceId +
                ", gatewayExternalReference=" + gatewayExternalReference +
                ", gatewayCheckoutUrl=" + gatewayCheckoutUrl +
                ", gatewayPaymentId=" + gatewayPaymentId +
                ", gatewayPaymentStatus=" + gatewayPaymentStatus +
                ", gatewayPaymentStatusDetail=" + gatewayPaymentStatusDetail +
                ", gatewayPaymentUpdatedAt=" + gatewayPaymentUpdatedAt +
                ", gatewayPaymentTicketUrl=" + gatewayPaymentTicketUrl +
                ", gatewayPixQrCode=" + gatewayPixQrCode +
                ", enderecoEntrega=" + enderecoEntrega +
                ", codigoEntrega=" + codigoEntrega +
                ", codigoEntregaGeradoEm=" + codigoEntregaGeradoEm +
                ", codigoEntregaConfirmadoEm=" + codigoEntregaConfirmadoEm +
                ", cancelamentoMotivo=" + cancelamentoMotivo +
                ", canceladoEm=" + canceladoEm +
                ", version=" + version +
                '}';
    }
}
