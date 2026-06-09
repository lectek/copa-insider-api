/*
 * Decompiled with CFR 0.152.
 */
package br.com.lectek.copainsider.domain;
import br.com.lectek.copainsider.domain.enums.StatusPedido;
import br.com.lectek.copainsider.domain.enums.TipoPagamento;
import br.com.lectek.copainsider.domain.enums.ModoEntrega;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Pedido {
    private Long id;
    private Cliente cliente;
    private LocalDateTime data = LocalDateTime.now();
    private BigDecimal total = BigDecimal.ZERO;
    private List<ItemPedido> itens;
    private StatusPedido status = StatusPedido.ABERTO;
    private TipoPagamento tipoPagamento = TipoPagamento.PIX;
    private ModoEntrega modoEntrega = ModoEntrega.ENTREGA;
    private String metodoPagamento;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return this.cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public LocalDateTime getData() {
        return this.data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public BigDecimal getTotal() {
        return this.total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<ItemPedido> getItens() {
        return this.itens;
    }

    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
    }

    public StatusPedido getStatus() {
        return this.status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public TipoPagamento getTipoPagamento() {
        return this.tipoPagamento;
    }

    public void setTipoPagamento(TipoPagamento tipoPagamento) {
        this.tipoPagamento = tipoPagamento;
    }

    public ModoEntrega getModoEntrega() {
        return this.modoEntrega;
    }

    public void setModoEntrega(ModoEntrega modoEntrega) {
        this.modoEntrega = modoEntrega;
    }

    public String getMetodoPagamento() {
        return this.metodoPagamento;
    }

    public void setMetodoPagamento(String metodoPagamento) {
        this.metodoPagamento = metodoPagamento;
    }
}
