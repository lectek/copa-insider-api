/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonFormat
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 *  com.fasterxml.jackson.annotation.JsonProperty
 *  io.swagger.v3.oas.annotations.media.Schema
 */
package br.com.redemaisfarma.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Schema(name="ItemVendaDTO", description="Item individual presente em uma venda")
@JsonInclude(value=JsonInclude.Include.NON_NULL)
public class ItemVendaDTO
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="ID do produto", example="456")
    @JsonProperty(value="produtoId")
    private Long produtoId;
    @Schema(description="SKU do produto", example="PRD-001-ABC")
    @JsonProperty(value="sku")
    private String sku;
    @Schema(description="Nome do produto", example="Dipirona 500mg")
    @JsonProperty(value="nomeProduto")
    private String nomeProduto;
    @Schema(description="Quantidade vendida", example="2")
    @JsonProperty(value="quantidade")
    private Integer quantidade;
    @Schema(description="Pre\u00e7o unit\u00e1rio (sem desconto)", example="12.50")
    @JsonProperty(value="precoUnitario")
    private BigDecimal precoUnitario;
    @Schema(description="Percentual de desconto aplicado ao item (0-100)", example="5.00")
    @JsonProperty(value="percentualDesconto")
    private BigDecimal percentualDesconto;
    @Schema(description="Valor absoluto de desconto aplicado ao item", example="0.62")
    @JsonProperty(value="valorDesconto")
    private BigDecimal valorDesconto;
    @Schema(description="Subtotal do item (quantidade x unit\u00e1rio - desconto)", example="23.38")
    @JsonProperty(value="total")
    private BigDecimal total;
    @Schema(description="C\u00f3digo do lote (quando aplic\u00e1vel)", example="Lote1234")
    @JsonProperty(value="lote")
    private String lote;
    @Schema(description="Validade do produto (quando aplic\u00e1vel)", type="string", format="date", example="2026-12-31")
    @JsonFormat(pattern="yyyy-MM-dd")
    @JsonProperty(value="validade")
    private LocalDate validade;

    public ItemVendaDTO() {
    }

    public ItemVendaDTO(Long produtoId, String sku, String nomeProduto, Integer quantidade, BigDecimal precoUnitario, BigDecimal percentualDesconto, BigDecimal valorDesconto, BigDecimal total, String lote, LocalDate validade) {
        this.produtoId = produtoId;
        this.sku = sku;
        this.nomeProduto = nomeProduto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.percentualDesconto = percentualDesconto;
        this.valorDesconto = valorDesconto;
        this.total = total;
        this.lote = lote;
        this.validade = validade;
    }

    public Long getProdutoId() {
        return this.produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }

    public String getSku() {
        return this.sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getNomeProduto() {
        return this.nomeProduto;
    }

    public void setNomeProduto(String nomeProduto) {
        this.nomeProduto = nomeProduto;
    }

    public Integer getQuantidade() {
        return this.quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return this.precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public BigDecimal getPercentualDesconto() {
        return this.percentualDesconto;
    }

    public void setPercentualDesconto(BigDecimal percentualDesconto) {
        this.percentualDesconto = percentualDesconto;
    }

    public BigDecimal getValorDesconto() {
        return this.valorDesconto;
    }

    public void setValorDesconto(BigDecimal valorDesconto) {
        this.valorDesconto = valorDesconto;
    }

    public BigDecimal getTotal() {
        return this.total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getLote() {
        return this.lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public LocalDate getValidade() {
        return this.validade;
    }

    public void setValidade(LocalDate validade) {
        this.validade = validade;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ItemVendaDTO)) {
            return false;
        }
        ItemVendaDTO that = (ItemVendaDTO)o;
        return Objects.equals(this.produtoId, that.produtoId) && Objects.equals(this.sku, that.sku) && Objects.equals(this.nomeProduto, that.nomeProduto) && Objects.equals(this.quantidade, that.quantidade) && Objects.equals(this.precoUnitario, that.precoUnitario) && Objects.equals(this.percentualDesconto, that.percentualDesconto) && Objects.equals(this.valorDesconto, that.valorDesconto) && Objects.equals(this.total, that.total) && Objects.equals(this.lote, that.lote) && Objects.equals(this.validade, that.validade);
    }

    public int hashCode() {
        return Objects.hash(this.produtoId, this.sku, this.nomeProduto, this.quantidade, this.precoUnitario, this.percentualDesconto, this.valorDesconto, this.total, this.lote, this.validade);
    }

    public String toString() {
        return "ItemVendaDTO{produtoId=" + this.produtoId + ", sku='" + this.sku + "', nomeProduto='" + this.nomeProduto + "', quantidade=" + this.quantidade + ", precoUnitario=" + String.valueOf(this.precoUnitario) + ", percentualDesconto=" + String.valueOf(this.percentualDesconto) + ", valorDesconto=" + String.valueOf(this.valorDesconto) + ", total=" + String.valueOf(this.total) + ", lote='" + this.lote + "', validade=" + String.valueOf(this.validade) + "}";
    }
}

