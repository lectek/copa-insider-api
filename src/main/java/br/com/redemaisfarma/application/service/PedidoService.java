/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  br.com.redemaisfarma.domain.Pedido
 */
package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.domain.Pedido;
import java.util.List;
import java.util.Optional;

public interface PedidoService {
    public Pedido findById(Long var1);

    public List<Pedido> list();

    public Pedido create(Pedido var1);

    public Pedido update(Long var1, Pedido var2);

    public void delete(Long var1);

    default public Optional<Pedido> findByIdOptional(Long id) {
        try {
            return Optional.ofNullable(this.findById(id));
        }
        catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    @Deprecated
    default public Pedido buscarPorId(Long id) {
        return this.findById(id);
    }

    @Deprecated
    default public List<Pedido> listarTodos() {
        return this.list();
    }

    @Deprecated
    default public Pedido salvar(Pedido pedido) {
        return this.create(pedido);
    }

    @Deprecated
    default public Pedido atualizar(Long id, Pedido pedido) {
        return this.update(id, pedido);
    }

    @Deprecated
    default public void deletar(Long id) {
        this.delete(id);
    }
}

