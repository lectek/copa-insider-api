/*
 * Decompiled with CFR 0.152.
 */
package br.com.redemaisfarma.adapters.outbound.messaging.kafka.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class PedidoCreatedEvent
implements Serializable {
    private UUID pedidoId;
    private Long usuarioId;
    private BigDecimal valorTotal;
    private Instant criadoEm;

    public PedidoCreatedEvent() {
    }

    public PedidoCreatedEvent(UUID pedidoId, Long usuarioId, BigDecimal valorTotal, Instant criadoEm) {
        this.pedidoId = pedidoId;
        this.usuarioId = usuarioId;
        this.valorTotal = valorTotal;
        this.criadoEm = criadoEm;
    }

    public UUID getPedidoId() {
        return this.pedidoId;
    }

    public void setPedidoId(UUID pedidoId) {
        this.pedidoId = pedidoId;
    }

    public Long getUsuarioId() {
        return this.usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public BigDecimal getValorTotal() {
        return this.valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public Instant getCriadoEm() {
        return this.criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PedidoCreatedEvent)) {
            return false;
        }
        PedidoCreatedEvent that = (PedidoCreatedEvent)o;
        return Objects.equals(this.pedidoId, that.pedidoId);
    }

    public int hashCode() {
        return Objects.hash(this.pedidoId);
    }
}

