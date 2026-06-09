/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.domain.Pageable
 *  org.springframework.data.jpa.repository.Query
 *  org.springframework.data.repository.Repository
 */
package br.com.lectek.copainsider.domain.catalogo;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface VendasAggregRepository
extends Repository<Object, Long> {
    @Query(value="    select ip.produto.id as id\n    from ItemPedido ip\n    group by ip.produto.id\n    order by sum(ip.quantidade) desc\n")
    public List<Long> topProdutoIds(Pageable var1);
}

