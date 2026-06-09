/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 *  com.fasterxml.jackson.annotation.JsonProperty
 *  io.swagger.v3.oas.annotations.media.Schema
 */
package br.com.lectek.copainsider.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Schema(name="ItemPedidoResponse", description="Item pertencente a um pedido")
@JsonInclude(value=JsonInclude.Include.NON_NULL)
public class ItemPedidoResponse
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="ID do item do pedido", example="9876")
    @JsonProperty(value="itemId")
    private Long itemId;
    @Schema(description="ID do pedido", example="1234")
    @JsonProperty(value="pedidoId")
    private Long pedidoId;
    @Schema(description="ID do produto", example="101")
    @JsonProperty(value="produtoId")
    private Long produtoId;
    @Schema(description="SKU do produto", example="PRD-001-ABC")
    @JsonProperty(value="sku")
    private String sku;
    @Schema(description="Nome do produto", example="Dipirona 500mg")
    @JsonProperty(value="nomeProduto")
    private String nomeProduto;
    @Schema(description="Categoria do produto", example="ANALGESICO")
    @JsonProperty(value="categoria")
    private String categoria;
    @Schema(description="C\u00f3digo de barras (EAN/UPC)", example="7891234567895")
    @JsonProperty(value="codigoBarras")
    private String codigoBarras;
    @Schema(description="URL da imagem do produto", example="https://cdn.farma/img/produto1.jpg")
    @JsonProperty(value="imagemUrl")
    private String imagemUrl;
    @Schema(description="Unidade de venda", example="UN")
    @JsonProperty(value="unidade")
    private String unidade;
    @Schema(description="Quantidade do item", example="3")
    @JsonProperty(value="quantidade")
    private Integer quantidade;
    @Schema(description="Pre\u00e7o unit\u00e1rio vigente", example="12.50")
    @JsonProperty(value="precoUnitario")
    private BigDecimal precoUnitario;
    @Schema(description="Percentual de desconto aplicado (0-100)", example="5.00")
    @JsonProperty(value="percentualDesconto")
    private BigDecimal percentualDesconto;
    @Schema(description="Valor absoluto de desconto", example="1.25")
    @JsonProperty(value="valorDesconto")
    private BigDecimal valorDesconto;
    @Schema(description="Subtotal do item (qtd x unit\u00e1rio - desconto)", example="36.25")
    @JsonProperty(value="total")
    private BigDecimal total;

    public ItemPedidoResponse() {
    }

    public ItemPedidoResponse(Long itemId, Long pedidoId, Long produtoId, String sku, String nomeProduto, String categoria, String codigoBarras, String imagemUrl, String unidade, Integer quantidade, BigDecimal precoUnitario, BigDecimal percentualDesconto, BigDecimal valorDesconto, BigDecimal total) {
        this.itemId = itemId;
        this.pedidoId = pedidoId;
        this.produtoId = produtoId;
        this.sku = sku;
        this.nomeProduto = nomeProduto;
        this.categoria = categoria;
        this.codigoBarras = codigoBarras;
        this.imagemUrl = imagemUrl;
        this.unidade = unidade;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.percentualDesconto = percentualDesconto;
        this.valorDesconto = valorDesconto;
        this.total = total;
    }

    public Long getItemId() {
        return this.itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getPedidoId() {
        return this.pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
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

    public String getCategoria() {
        return this.categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getCodigoBarras() {
        return this.codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
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
        if (!(o instanceof ItemPedidoResponse)) {
            return false;
        }
        ItemPedidoResponse that = (ItemPedidoResponse)o;
        return Objects.equals(this.itemId, that.itemId) && Objects.equals(this.pedidoId, that.pedidoId) && Objects.equals(this.produtoId, that.produtoId) && Objects.equals(this.sku, that.sku) && Objects.equals(this.nomeProduto, that.nomeProduto) && Objects.equals(this.categoria, that.categoria) && Objects.equals(this.codigoBarras, that.codigoBarras) && Objects.equals(this.imagemUrl, that.imagemUrl) && Objects.equals(this.unidade, that.unidade) && Objects.equals(this.quantidade, that.quantidade) && Objects.equals(this.precoUnitario, that.precoUnitario) && Objects.equals(this.percentualDesconto, that.percentualDesconto) && Objects.equals(this.valorDesconto, that.valorDesconto) && Objects.equals(this.total, that.total);
    }

    public int hashCode() {
        return Objects.hash(this.itemId, this.pedidoId, this.produtoId, this.sku, this.nomeProduto, this.categoria, this.codigoBarras, this.imagemUrl, this.unidade, this.quantidade, this.precoUnitario, this.percentualDesconto, this.valorDesconto, this.total);
    }

    public String toString() {
        return "ItemPedidoResponse{itemId=" + this.itemId + ", pedidoId=" + this.pedidoId + ", produtoId=" + this.produtoId + ", sku='" + this.sku + "', nomeProduto='" + this.nomeProduto + "', categoria='" + this.categoria + "', codigoBarras='" + this.codigoBarras + "', imagemUrl='" + this.imagemUrl + "', unidade='" + this.unidade + "', quantidade=" + this.quantidade + ", precoUnitario=" + String.valueOf(this.precoUnitario) + ", percentualDesconto=" + String.valueOf(this.percentualDesconto) + ", valorDesconto=" + String.valueOf(this.valorDesconto) + ", total=" + String.valueOf(this.total) + "}";
    }
}

