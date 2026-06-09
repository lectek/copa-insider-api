/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 *  com.fasterxml.jackson.annotation.JsonProperty
 *  io.swagger.v3.oas.annotations.media.Schema
 */
package br.com.redemaisfarma.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Schema(name="ItemAtualizarPedidoDTO", description="Item do pedido ap\u00f3s atualiza\u00e7\u00e3o")
@JsonInclude(value=JsonInclude.Include.NON_NULL)
public class ItemAtualizarPedidoDTO
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="ID do produto", example="101")
    @JsonProperty(value="produtoId")
    private Long produtoId;
    @Schema(description="SKU do produto", example="PRD-001-ABC")
    @JsonProperty(value="sku")
    private String sku;
    @Schema(description="Nome do produto", example="Dipirona 500mg")
    @JsonProperty(value="nomeProduto")
    private String nomeProduto;
    @Schema(description="URL da imagem (se houver)", example="https://cdn.farma/img/produto1.jpg")
    @JsonProperty(value="imagemUrl")
    private String imagemUrl;
    @Schema(description="Unidade de venda", example="UN")
    @JsonProperty(value="unidade")
    private String unidade;
    @Schema(description="Quantidade atual do item", example="3")
    @JsonProperty(value="quantidade")
    private Integer quantidade;
    @Schema(description="Pre\u00e7o unit\u00e1rio vigente", example="12.50")
    @JsonProperty(value="precoUnitario")
    private BigDecimal precoUnitario;
    @Schema(description="Percentual de desconto aplicado no item (0-100)", example="5.00")
    @JsonProperty(value="percentualDesconto")
    private BigDecimal percentualDesconto;
    @Schema(description="Valor absoluto de desconto no item", example="1.25")
    @JsonProperty(value="valorDesconto")
    private BigDecimal valorDesconto;
    @Schema(description="Subtotal do item (qtd x unit\u00e1rio - desconto)", example="36.25")
    @JsonProperty(value="total")
    private BigDecimal total;

    public ItemAtualizarPedidoDTO() {
    }

    public ItemAtualizarPedidoDTO(Long produtoId, String sku, String nomeProduto, String imagemUrl, String unidade, Integer quantidade, BigDecimal precoUnitario, BigDecimal percentualDesconto, BigDecimal valorDesconto, BigDecimal total) {
        this.produtoId = produtoId;
        this.sku = sku;
        this.nomeProduto = nomeProduto;
        this.imagemUrl = imagemUrl;
        this.unidade = unidade;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.percentualDesconto = percentualDesconto;
        this.valorDesconto = valorDesconto;
        this.total = total;
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

    public String getImagemUrl() {
        return this.imagemUrl;
    }

    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }

    public String getUnidade() {
        return this.unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
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

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ItemAtualizarPedidoDTO)) {
            return false;
        }
        ItemAtualizarPedidoDTO that = (ItemAtualizarPedidoDTO)o;
        return Objects.equals(this.produtoId, that.produtoId) && Objects.equals(this.sku, that.sku) && Objects.equals(this.nomeProduto, that.nomeProduto) && Objects.equals(this.imagemUrl, that.imagemUrl) && Objects.equals(this.unidade, that.unidade) && Objects.equals(this.quantidade, that.quantidade) && Objects.equals(this.precoUnitario, that.precoUnitario) && Objects.equals(this.percentualDesconto, that.percentualDesconto) && Objects.equals(this.valorDesconto, that.valorDesconto) && Objects.equals(this.total, that.total);
    }

    public int hashCode() {
        return Objects.hash(this.produtoId, this.sku, this.nomeProduto, this.imagemUrl, this.unidade, this.quantidade, this.precoUnitario, this.percentualDesconto, this.valorDesconto, this.total);
    }

    public String toString() {
        return "ItemAtualizarPedidoDTO{produtoId=" + this.produtoId + ", sku='" + this.sku + "', nomeProduto='" + this.nomeProduto + "', imagemUrl='" + this.imagemUrl + "', unidade='" + this.unidade + "', quantidade=" + this.quantidade + ", precoUnitario=" + String.valueOf(this.precoUnitario) + ", percentualDesconto=" + String.valueOf(this.percentualDesconto) + ", valorDesconto=" + String.valueOf(this.valorDesconto) + ", total=" + String.valueOf(this.total) + "}";
    }
}

