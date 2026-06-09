package br.com.redemaisfarma.application.service.automation;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class CartSnapshot implements Serializable {
    private final Long clienteId;
    private final Instant updatedAt;
    private final BigDecimal total;
    private final List<CartSnapshotItem> items;

    public CartSnapshot(Long clienteId, Instant updatedAt, BigDecimal total, List<CartSnapshotItem> items) {
        this.clienteId = clienteId;
        this.updatedAt = updatedAt;
        this.total = total;
        this.items = items;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public List<CartSnapshotItem> getItems() {
        return items;
    }

    public static class CartSnapshotItem implements Serializable {
        private final Long produtoId;
        private final Integer quantidade;

        public CartSnapshotItem(Long produtoId, Integer quantidade) {
            this.produtoId = produtoId;
            this.quantidade = quantidade;
        }

        public Long getProdutoId() {
            return produtoId;
        }

        public Integer getQuantidade() {
            return quantidade;
        }
    }
}
