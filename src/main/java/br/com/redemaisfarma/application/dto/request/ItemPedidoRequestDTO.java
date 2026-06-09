/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 *  com.fasterxml.jackson.annotation.JsonProperty
 *  com.fasterxml.jackson.annotation.JsonProperty$Access
 *  io.swagger.v3.oas.annotations.media.Schema
 *  io.swagger.v3.oas.annotations.media.Schema$AccessMode
 *  jakarta.validation.constraints.DecimalMin
 *  jakarta.validation.constraints.Digits
 *  jakarta.validation.constraints.Min
 *  jakarta.validation.constraints.NotNull
 */
package br.com.redemaisfarma.application.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Schema(name="ItemPedidoRequestDTO", description="DTO para item do pedido no momento da cria\u00e7\u00e3o")
@JsonInclude(value=JsonInclude.Include.NON_NULL)
public class ItemPedidoRequestDTO
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="ID do produto", example="101", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{itemPedido.produtoId.notNull}")
    @JsonProperty(value="produtoId")
    private @NotNull(message="{itemPedido.produtoId.notNull}") Long produtoId;
    @Schema(description="Quantidade desejada do produto", example="3", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{itemPedido.quantidade.notNull}")
    @Min(value=1L, message="{itemPedido.quantidade.min}")
    @JsonProperty(value="quantidade")
    private @NotNull(message="{itemPedido.quantidade.notNull}") @Min(value=1L, message="{itemPedido.quantidade.min}") Integer quantidade;
    @Schema(description="Pre\u00e7o unit\u00e1rio do produto no momento do pedido", example="9.90", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{itemPedido.precoUnitario.notNull}")
    @DecimalMin(value="0.01", inclusive=true, message="{itemPedido.precoUnitario.min}")
    @Digits(integer=12, fraction=2, message="{itemPedido.precoUnitario.digits}")
    @JsonProperty(value="precoUnitario")
    private @NotNull(message="{itemPedido.precoUnitario.notNull}") @DecimalMin(value="0.01", inclusive=true, message="{itemPedido.precoUnitario.min}") @Digits(integer=12, fraction=2, message="{itemPedido.precoUnitario.digits}") BigDecimal precoUnitario;
    @Schema(description="Subtotal calculado (quantidade x pre\u00e7o unit\u00e1rio) - calculado pelo servidor", example="29.70", accessMode=Schema.AccessMode.READ_ONLY)
    @Digits(integer=12, fraction=2, message="{itemPedido.subtotal.digits}")
    @JsonProperty(value="subtotal", access=JsonProperty.Access.READ_ONLY)
    private @Digits(integer=12, fraction=2, message="{itemPedido.subtotal.digits}") BigDecimal subtotal;

    public ItemPedidoRequestDTO() {
    }

    public ItemPedidoRequestDTO(Long produtoId, Integer quantidade, BigDecimal precoUnitario) {
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    public Long getProdutoId() {
        return this.produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
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
        if (!(o instanceof ItemPedidoRequestDTO)) {
            return false;
        }
        ItemPedidoRequestDTO that = (ItemPedidoRequestDTO)o;
        return Objects.equals(this.produtoId, that.produtoId) && Objects.equals(this.quantidade, that.quantidade) && Objects.equals(this.precoUnitario, that.precoUnitario);
    }

    public int hashCode() {
        return Objects.hash(this.produtoId, this.quantidade, this.precoUnitario);
    }

    public String toString() {
        return "ItemPedidoRequestDTO{produtoId=" + this.produtoId + ", quantidade=" + this.quantidade + ", precoUnitario=" + String.valueOf(this.precoUnitario) + "}";
    }
}


