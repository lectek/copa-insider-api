package br.com.lectek.copainsider.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Schema(name = "VendaRequestDTO", description = "Dados para registro de venda")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendaRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID do cliente", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{venda.clienteId.notNull}")
    @JsonProperty("clienteId")
    private Long clienteId;

    @Schema(description = "Itens da venda", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{venda.itens.notEmpty}")
    @Valid
    @JsonProperty("itens")
    private List<@Valid ItemVendaDTO> itens;

    @Schema(description = "Valor total bruto (soma dos itens)", example = "200.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{venda.totalBruto.notNull}")
    @DecimalMin(value = "0.00", inclusive = false, message = "{venda.totalBruto.min}")
    @JsonProperty("totalBruto")
    private BigDecimal totalBruto;

    @Schema(description = "Total de descontos aplicados", example = "20.00")
    @DecimalMin(value = "0.00", inclusive = true, message = "{venda.totalDescontos.min}")
    @JsonProperty("totalDescontos")
    private BigDecimal totalDescontos;

    @Schema(description = "Total líquido (bruto - descontos)", example = "180.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{venda.totalLiquido.notNull}")
    @DecimalMin(value = "0.00", inclusive = false, message = "{venda.totalLiquido.min}")
    @JsonProperty("totalLiquido")
    private BigDecimal totalLiquido;

    @Schema(description = "Formas de pagamento da venda", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "{venda.formasPagamento.notEmpty}")
    @Valid
    @JsonProperty("formasPagamento")
    private List<@Valid FormaPagamentoRequestDTO> formasPagamento;

    @Schema(description = "Tipo da venda (PRESENCIAL ou ONLINE)", example = "PRESENCIAL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "{venda.tipoVenda.notNull}")
    @JsonProperty("tipoVenda")
    private TipoVenda tipoVenda;

    @Schema(description = "ID do ponto de venda (obrigatório para PRESENCIAL)", example = "10")
    @JsonProperty("pontoVendaId")
    private Long pontoVendaId;

    @Schema(description = "Data/hora da venda", type = "string", format = "date-time", example = "2025-07-04T10:15:30")
    @NotNull(message = "{venda.dataVenda.notNull}")
    @PastOrPresent(message = "{venda.dataVenda.pastOrPresent}")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dataVenda")
    private LocalDateTime dataVenda;

    @Schema(description = "Observações adicionais", example = "Entregar em domicílio na parte da tarde")
    @Size(max = 500, message = "{venda.observacao.size}")
    @JsonProperty("observacao")
    private String observacao;

    @Schema(description = "ID do tenant (multi-inquilino)", example = "copainsider-001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "{venda.tenantId.notBlank}")
    @JsonProperty("tenantId")
    private String tenantId;

    @Schema(description = "Token de rastreamento (UUID) para auditoria", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty("traceId")
    private UUID traceId;

    /* ==================== Regras de coerência ==================== */

    @AssertTrue(message = "{venda.totalLiquido.coerencia}")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean isTotaisCoerentes() {
        if (totalBruto == null || totalLiquido == null) return false;
        if (totalBruto.signum() < 0 || totalLiquido.signum() <= 0) return false;

        BigDecimal descontos = totalDescontos != null ? totalDescontos : BigDecimal.ZERO;
        if (descontos.signum() < 0 || descontos.compareTo(totalBruto) > 0) return false;

        return totalBruto.subtract(descontos).compareTo(totalLiquido) == 0;
    }

    @AssertTrue(message = "{venda.formasPagamento.somaIgualTotalLiquido}")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean isSomaFormasPagamentoValida() {
        if (formasPagamento == null || formasPagamento.isEmpty() || totalLiquido == null) return false;
        BigDecimal soma = formasPagamento.stream()
                .map(FormaPagamentoRequestDTO::getValor)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return soma.compareTo(totalLiquido) == 0;
    }

    @AssertTrue(message = "{venda.pontoVenda.obrigatorio.presencial}")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean isPontoVendaValido() {
        if (tipoVenda == null) return false;
        if (tipoVenda == TipoVenda.PRESENCIAL) return pontoVendaId != null;
        return true;
    }

    @AssertTrue(message = "{venda.itens.somaIgualTotalBruto}")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public boolean isSomaItensIgualTotalBruto() {
        if (itens == null || itens.isEmpty() || totalBruto == null) return false;
        BigDecimal soma = itens.stream()
                .map(ItemVendaDTO::getSubtotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return soma.compareTo(totalBruto) == 0;
    }

    /* ==================== Tipos internos ==================== */

    public enum TipoVenda {
        PRESENCIAL,
        ONLINE
    }

    @Schema(name = "ItemVendaDTO", description = "Item de venda")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemVendaDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "ID do produto", example = "987", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{venda.item.produtoId.notNull}")
        private Long produtoId;

        @Schema(description = "Quantidade do item", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{venda.item.quantidade.notNull}")
        @Min(value = 1, message = "{venda.item.quantidade.min}")
        private Integer quantidade;

        @Schema(description = "Preço unitário", example = "100.00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{venda.item.precoUnitario.notNull}")
        @DecimalMin(value = "0.00", inclusive = false, message = "{venda.item.precoUnitario.min}")
        private BigDecimal precoUnitario;

        @Schema(description = "Subtotal do item (quantidade * preço unitário)", example = "200.00", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "{venda.item.subtotal.notNull}")
        @DecimalMin(value = "0.00", inclusive = false, message = "{venda.item.subtotal.min}")
        private BigDecimal subtotal;

        @AssertTrue(message = "{venda.item.subtotal.coerencia}")
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        public boolean isSubtotalCoerente() {
            if (quantidade == null || precoUnitario == null || subtotal == null) return false;
            BigDecimal esperado = precoUnitario.multiply(BigDecimal.valueOf(quantidade));
            return esperado.compareTo(subtotal) == 0;
        }
    }
}

