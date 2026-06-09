package br.com.redemaisfarma.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * DTO para criação de pedidos (API pública).
 */
@JsonInclude(Include.NON_NULL)
@Schema(name = "CriarPedidoRequest", description = "Dados para criação de um pedido na RedeMaisFarma")
public class CriarPedidoRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID único do pedido (idempotência)", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value = "pedidoId", access = JsonProperty.Access.WRITE_ONLY)
    private UUID pedidoId;

    @Schema(description = "ID do cliente que realizou o pedido", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{pedido.clienteId.notNull}")
    @Min(value = 1, message = "{pedido.clienteId.min}")
    @JsonProperty("clienteId")
    private Long clienteId;

    @Schema(description = "Itens do pedido", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{pedido.itens.notEmpty}")
    @Valid
    @JsonProperty("itens")
    private List<@NotNull @Valid ItemPedidoRequestDTO> itens;

    @Schema(description = "Endereço de entrega completo", example = "Rua das Acácias, 45, Centro, João Pessoa - PB", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{pedido.enderecoEntrega.notBlank}")
    @jakarta.validation.constraints.Size(min = 5, max = 200, message = "{pedido.enderecoEntrega.size}")
    @JsonProperty("enderecoEntrega")
    private String enderecoEntrega;

    @Schema(description = "Valor total do pedido", example = "150.75", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{pedido.valorTotal.notNull}")
    @DecimalMin(value = "0.01", inclusive = true, message = "{pedido.valorTotal.min}")
    @Digits(integer = 12, fraction = 2, message = "{pedido.valorTotal.digits}")
    @JsonProperty("valorTotal")
    private BigDecimal valorTotal;

    @Schema(description = "Forma de pagamento", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{pedido.formaPagamento.notNull}")
    @Valid
    @JsonProperty("formaPagamento")
    private FormaPagamentoRequestDTO formaPagamento;

    @Schema(description = "ID do tenant", example = "redemaisfarma-001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{pedido.tenantId.notBlank}")
    @jakarta.validation.constraints.Size(max = 100, message = "{pedido.tenantId.size}")
    @JsonProperty("tenantId")
    private String tenantId;

    @Schema(description = "Token de rastreamento do pedido", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value = "traceId", access = JsonProperty.Access.READ_ONLY)
    private UUID traceId;

    @Schema(description = "Timestamp de criação do pedido", type = "string", format = "date-time",
            example = "2025-07-04T10:15:30", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{pedido.dataCriacao.notNull}")
    @PastOrPresent(message = "{pedido.dataCriacao.pastOrPresent}")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dataCriacao")
    private LocalDateTime dataCriacao;

    @Schema(description = "Observações adicionais", example = "Entregar com cuidado, atrás do portão branco")
    @jakarta.validation.constraints.Size(max = 500, message = "{pedido.observacao.size}")
    @JsonProperty("observacao")
    private String observacao;

    @Schema(description = "Timestamp da última atualização do pedido", type = "string", format = "date-time",
            example = "2025-07-04T11:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value = "dataAtualizacao", access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime dataAtualizacao;

    public CriarPedidoRequest() {
    }

    public CriarPedidoRequest(
            UUID pedidoId,
            Long clienteId,
            List<ItemPedidoRequestDTO> itens,
            String enderecoEntrega,
            BigDecimal valorTotal,
            FormaPagamentoRequestDTO formaPagamento,
            String tenantId,
            UUID traceId,
            LocalDateTime dataCriacao,
            String observacao,
            LocalDateTime dataAtualizacao
    ) {
        this.pedidoId = pedidoId;
        this.clienteId = clienteId;
        this.itens = itens;
        this.enderecoEntrega = enderecoEntrega;
        this.valorTotal = valorTotal;
        this.formaPagamento = formaPagamento;
        this.tenantId = tenantId;
        this.traceId = traceId;
        this.dataCriacao = dataCriacao;
        this.observacao = observacao;
        this.dataAtualizacao = dataAtualizacao;
    }

    // ======= Validações de consistência (não expostas no JSON) =======

    @AssertTrue(message = "{pedido.totais.coerencia}")
    @JsonIgnore
    public boolean isTotaisCoerentes() {
        if (itens == null || itens.isEmpty() || valorTotal == null) return false;

        BigDecimal soma = itens.stream()
                .map(i -> i.getPrecoUnitario()
                        .multiply(BigDecimal.valueOf(i.getQuantidade().longValue()))
                        .setScale(2, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), BigDecimal::add);

        return soma.compareTo(valorTotal) == 0;
    }

    @AssertTrue(message = "{pedido.formaPagamento.igualTotal}")
    @JsonIgnore
    public boolean isFormaPagamentoIgualTotal() {
        if (formaPagamento == null || valorTotal == null || formaPagamento.getValor() == null) return false;
        return valorTotal.compareTo(formaPagamento.getValor()) == 0;
    }

    @AssertTrue(message = "{pedido.datas.ordemValida}")
    @JsonIgnore
    public boolean isOrdemTemporalValida() {
        if (dataCriacao == null) return false;
        if (dataAtualizacao == null) return true;
        return !dataAtualizacao.isBefore(dataCriacao);
    }

    // ======= Getters/Setters =======

    public UUID getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(UUID pedidoId) {
        this.pedidoId = pedidoId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public List<ItemPedidoRequestDTO> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedidoRequestDTO> itens) {
        this.itens = itens;
    }

    public String getEnderecoEntrega() {
        return enderecoEntrega;
    }

    public void setEnderecoEntrega(String enderecoEntrega) {
        this.enderecoEntrega = enderecoEntrega == null ? null : enderecoEntrega.trim();
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public FormaPagamentoRequestDTO getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamentoRequestDTO formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId == null ? null : tenantId.trim();
    }

    public UUID getTraceId() {
        return traceId;
    }

    public void setTraceId(UUID traceId) {
        this.traceId = traceId;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao == null ? null : observacao.trim();
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    // ======= equals/hashCode/toString =======

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CriarPedidoRequest)) return false;
        CriarPedidoRequest that = (CriarPedidoRequest) o;
        return Objects.equals(pedidoId, that.pedidoId)
                && Objects.equals(clienteId, that.clienteId)
                && Objects.equals(itens, that.itens)
                && Objects.equals(enderecoEntrega, that.enderecoEntrega)
                && Objects.equals(valorTotal, that.valorTotal)
                && Objects.equals(formaPagamento, that.formaPagamento)
                && Objects.equals(tenantId, that.tenantId)
                && Objects.equals(traceId, that.traceId)
                && Objects.equals(dataCriacao, that.dataCriacao)
                && Objects.equals(observacao, that.observacao)
                && Objects.equals(dataAtualizacao, that.dataAtualizacao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                pedidoId, clienteId, itens, enderecoEntrega, valorTotal,
                formaPagamento, tenantId, traceId, dataCriacao, observacao, dataAtualizacao
        );
    }

    @Override
    public String toString() {
        return "CriarPedidoRequest{" +
                "pedidoId=" + pedidoId +
                ", clienteId=" + clienteId +
                ", itens=" + itens +
                ", enderecoEntrega='" + enderecoEntrega + '\'' +
                ", valorTotal=" + valorTotal +
                ", formaPagamento=" + formaPagamento +
                ", tenantId='" + tenantId + '\'' +
                ", traceId=" + traceId +
                ", dataCriacao=" + dataCriacao +
                ", observacao='" + observacao + '\'' +
                ", dataAtualizacao=" + dataAtualizacao +
                '}';
    }
}
