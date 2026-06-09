/*
 * Decompiled with CFR 0.152.
 */
package br.com.lectek.copainsider.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class ItemPedido {
    private Long id;
    private Pedido pedido;
    private Long produtoId;
    private String produtoNome;
    private String codigoBarras;
    private Integer quantidade = 0;
    private BigDecimal precoUnitario = BigDecimal.ZERO;
    private BigDecimal subtotal = BigDecimal.ZERO;

    public ItemPedido() {
    }

    public ItemPedido(Long produtoId, Integer quantidade, BigDecimal precoUnitario) {
        this.produtoId = produtoId;
        this.setQuantidade(quantidade);
        this.setPrecoUnitario(precoUnitario);
        this.recalcularSubtotal();
    }

    public static ItemPedido of(Long produtoId, int qtd, BigDecimal preco) {
        return new ItemPedido(produtoId, qtd, preco);
    }

    public void recalcularSubtotal() {
        this.subtotal = this.quantidade != null && this.precoUnitario != null ? this.precoUnitario.multiply(BigDecimal.valueOf(this.quantidade.intValue())).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pedido getPedido() {
        return this.pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Long getProdutoId() {
        return this.produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }

    public String getProdutoNome() {
        return this.produtoNome;
    }

    public void setProdutoNome(String produtoNome) {
        this.produtoNome = produtoNome;
    }

    public String getCodigoBarras() {
        return this.codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public Integer getQuantidade() {
        return this.quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade inv\u00e1lida: " + quantidade);
        }
        this.quantidade = quantidade;
        this.recalcularSubtotal();
    }

    public BigDecimal getPrecoUnitario() {
        return this.precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        if (precoUnitario == null || precoUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Pre\u00e7o unit\u00e1rio inv\u00e1lido: " + String.valueOf(precoUnitario));
        }
        this.precoUnitario = precoUnitario.setScale(2, RoundingMode.HALF_UP);
        this.recalcularSubtotal();
    }

    public BigDecimal getSubtotal() {
        return this.subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = (subtotal == null ? BigDecimal.ZERO : subtotal).setScale(2, RoundingMode.HALF_UP);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ItemPedido)) {
            return false;
        }
        ItemPedido that = (ItemPedido)o;
        return Objects.equals(this.id, that.id);
    }

    public int hashCode() {
        return Objects.hash(this.id);
    }

    public String toString() {
        return "ItemPedido{id=" + this.id + ", produtoId=" + this.produtoId + ", codigoBarras='" + this.codigoBarras + "', quantidade=" + this.quantidade + ", precoUnitario=" + String.valueOf(this.precoUnitario) + ", subtotal=" + String.valueOf(this.subtotal) + "}";
    }
}

