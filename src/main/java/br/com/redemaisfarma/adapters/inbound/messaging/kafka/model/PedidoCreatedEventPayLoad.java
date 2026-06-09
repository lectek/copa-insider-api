/*
 * Decompiled with CFR 0.152.
 */
package br.com.redemaisfarma.adapters.inbound.messaging.kafka.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PedidoCreatedEventPayLoad {
    private Long pedidoId;
    private Long clienteId;
    private BigDecimal total;
    private String status;
    private LocalDateTime dataCriacao;
    private List<ItemPedidoEvent> itens;

    public PedidoCreatedEventPayLoad() {
    }

    public PedidoCreatedEventPayLoad(Long pedidoId, Long clienteId, BigDecimal total, String status, LocalDateTime dataCriacao, List<ItemPedidoEvent> itens) {
        this.pedidoId = pedidoId;
        this.clienteId = clienteId;
        this.total = total;
        this.status = status;
        this.dataCriacao = dataCriacao;
        this.itens = itens;
    }

    public Long getPedidoId() {
        return this.pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public Long getClienteId() {
        return this.clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public BigDecimal getTotal() {
        return this.total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDataCriacao() {
        return this.dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public List<ItemPedidoEvent> getItens() {
        return this.itens;
    }

    public void setItens(List<ItemPedidoEvent> itens) {
        this.itens = itens;
    }

    public static class ItemPedidoEvent {
        private Long produtoId;
        private Integer quantidade;
        private BigDecimal precoUnitario;

        public ItemPedidoEvent() {
        }

        public ItemPedidoEvent(Long produtoId, Integer quantidade, BigDecimal precoUnitario) {
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
    }
}

