/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonFormat
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 *  com.fasterxml.jackson.annotation.JsonProperty
 *  com.fasterxml.jackson.annotation.JsonProperty$Access
 *  io.swagger.v3.oas.annotations.media.Schema
 *  jakarta.validation.Valid
 *  jakarta.validation.constraints.AssertTrue
 *  jakarta.validation.constraints.DecimalMin
 *  jakarta.validation.constraints.FutureOrPresent
 *  jakarta.validation.constraints.NotEmpty
 *  jakarta.validation.constraints.NotNull
 *  jakarta.validation.constraints.Size
 */
package br.com.redemaisfarma.application.dto.request;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@Schema(description="Dados para atualizar um pedido j\u00e1 existente na farm\u00e1cia")
public class AtualizarItemPedidoDTO
implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotNull(message="{pedido.id.notNull}")
    @Schema(description="ID do pedido a ser atualizado", example="123")
    @JsonProperty(value="pedidoId")
    private @NotNull(message="{pedido.id.notNull}") Long pedidoId;
    @NotEmpty(message="{pedido.itens.notEmpty}")
    @Schema(description="Lista de itens do pedido")
    @JsonProperty(value="itens")
    private @NotEmpty(message="{pedido.itens.notEmpty}") List<@Valid ItemPedidoRequestDTO> itens;
    @DecimalMin(value="0.00", inclusive=true, message="{pedido.total.min}")
    @Schema(description="Valor total atualizado (se omitido, o servidor recalcula)", example="249.90")
    @JsonProperty(value="total")
    private @DecimalMin(value="0.00", inclusive=true, message="{pedido.total.min}") BigDecimal total;
    @Size(max=255, message="{pedido.observacao.size}")
    @Schema(description="Observa\u00e7\u00f5es ou anota\u00e7\u00f5es do atendente", example="Cliente pediu sem sacola.")
    @JsonProperty(value="observacao")
    private @Size(max=255, message="{pedido.observacao.size}") String observacao;
    @NotNull(message="{pedido.clienteId.notNull}")
    @Schema(description="ID do cliente vinculado", example="45")
    @JsonProperty(value="clienteId")
    private @NotNull(message="{pedido.clienteId.notNull}") Long clienteId;
    @FutureOrPresent(message="{pedido.dataEntrega.futureOrPresent}")
    @Schema(description="Data/hora prevista para entrega", example="2025-07-05T15:00:00")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="dataEntrega")
    private @FutureOrPresent(message="{pedido.dataEntrega.futureOrPresent}") LocalDateTime dataEntrega;
    @NotNull(message="{pedido.status.notNull}")
    @Schema(description="Status atual do pedido", example="EM_PREPARO")
    @JsonProperty(value="status")
    private @NotNull(message="{pedido.status.notNull}") StatusPedido status;
    @Schema(description="ID do tenant (multi-inquilino)", example="redemaisfarma-001")
    @JsonProperty(value="tenantId")
    private String tenantId;
    @Schema(description="Token de rastreamento (UUID)", example="3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value="traceId")
    private UUID traceId;
    @Schema(description="Vers\u00e3o para controle otimista de concorr\u00eancia", example="5")
    @JsonProperty(value="version")
    private Long version;

    @AssertTrue(message="{pedido.total.coerente}")
    @JsonProperty(access=JsonProperty.Access.READ_ONLY)
    public @AssertTrue(message="{pedido.total.coerente}") boolean isTotalCoerente() {
        if (this.itens == null || this.itens.isEmpty()) {
            return false;
        }
        if (this.total == null) {
            return true;
        }
        BigDecimal soma = this.itens.stream().filter(Objects::nonNull).map(i -> {
            BigDecimal unit = i.getPrecoUnitario() != null ? i.getPrecoUnitario() : BigDecimal.ZERO;
            int qtd = i.getQuantidade() != null ? i.getQuantidade() : 0;
            return unit.multiply(BigDecimal.valueOf(qtd));
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
        return soma.setScale(2, RoundingMode.HALF_UP).compareTo(this.total.setScale(2, RoundingMode.HALF_UP)) == 0;
    }

    public Long getPedidoId() {
        return this.pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public List<ItemPedidoRequestDTO> getItens() {
        return this.itens;
    }

    public void setItens(List<ItemPedidoRequestDTO> itens) {
        this.itens = itens;
    }

    public BigDecimal getTotal() {
        return this.total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getObservacao() {
        return this.observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public Long getClienteId() {
        return this.clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public LocalDateTime getDataEntrega() {
        return this.dataEntrega;
    }

    public void setDataEntrega(LocalDateTime dataEntrega) {
        this.dataEntrega = dataEntrega;
    }

    public StatusPedido getStatus() {
        return this.status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public String getTenantId() {
        return this.tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getTraceId() {
        return this.traceId;
    }

    public void setTraceId(UUID traceId) {
        this.traceId = traceId;
    }

    public Long getVersion() {
        return this.version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AtualizarItemPedidoDTO)) {
            return false;
        }
        AtualizarItemPedidoDTO that = (AtualizarItemPedidoDTO)o;
        return Objects.equals(this.pedidoId, that.pedidoId) && Objects.equals(this.itens, that.itens) && Objects.equals(this.total, that.total) && Objects.equals(this.observacao, that.observacao) && Objects.equals(this.clienteId, that.clienteId) && Objects.equals(this.dataEntrega, that.dataEntrega) && this.status == that.status && Objects.equals(this.tenantId, that.tenantId) && Objects.equals(this.traceId, that.traceId) && Objects.equals(this.version, that.version);
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.pedidoId, this.itens, this.total, this.observacao, this.clienteId, this.dataEntrega, this.status, this.tenantId, this.traceId, this.version});
    }

    public String toString() {
        return "AtualizarItemPedidoDTO{pedidoId=" + this.pedidoId + ", itens=" + String.valueOf(this.itens) + ", total=" + String.valueOf(this.total) + ", observacao='" + this.observacao + "', clienteId=" + this.clienteId + ", dataEntrega=" + String.valueOf(this.dataEntrega) + ", status=" + String.valueOf((Object)this.status) + ", tenantId='" + this.tenantId + "', traceId=" + String.valueOf(this.traceId) + ", version=" + this.version + "}";
    }
}

