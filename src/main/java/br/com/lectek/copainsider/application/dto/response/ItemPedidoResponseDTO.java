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

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@Schema(name="ItemPedidoResponseDTO", description="Item que comp\u00f5e o pedido")
public class ItemPedidoResponseDTO
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="ID do produto", example="1001")
    @JsonProperty(value="produtoId")
    private Long produtoId;
    @Schema(description="Nome do produto", example="Dipirona 500mg")
    @JsonProperty(value="nomeProduto")
    private String nomeProduto;
    @Schema(description="Quantidade solicitada", example="2")
    @JsonProperty(value="quantidade")
    private Integer quantidade;
    @Schema(description="Pre\u00e7o unit\u00e1rio", example="12.50")
    @JsonProperty(value="precoUnitario")
    private BigDecimal precoUnitario;
    @Schema(description="Subtotal do item (quantidade x pre\u00e7o)", example="25.00")
    @JsonProperty(value="subtotal")
    private BigDecimal subtotal;

    public Long getProdutoId() {
        return this.produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
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

    public BigDecimal getSubtotal() {
        return this.subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ItemPedidoResponseDTO)) {
            return false;
        }
        ItemPedidoResponseDTO that = (ItemPedidoResponseDTO)o;
        return Objects.equals(this.produtoId, that.produtoId) && Objects.equals(this.nomeProduto, that.nomeProduto) && Objects.equals(this.quantidade, that.quantidade);
    }

    public int hashCode() {
        return Objects.hash(this.produtoId, this.nomeProduto, this.quantidade);
    }
}

