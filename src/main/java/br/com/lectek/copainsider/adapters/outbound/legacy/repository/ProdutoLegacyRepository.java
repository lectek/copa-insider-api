/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.persistence.QueryHint
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.data.jpa.repository.Query
 *  org.springframework.data.jpa.repository.QueryHints
 *  org.springframework.stereotype.Repository
 *  org.springframework.transaction.annotation.Transactional
 */
package br.com.lectek.copainsider.adapters.outbound.legacy.repository;

import br.com.lectek.copainsider.adapters.outbound.legacy.entity.ProdutoLegacyEntity;
import jakarta.persistence.QueryHint;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ProdutoLegacyRepository
extends JpaRepository<ProdutoLegacyEntity, Integer> {
    public Optional<ProdutoLegacyEntity> findByCodigoBarras(String var1);

    public List<ProdutoLegacyEntity> findByNomeContainingIgnoreCase(String var1);

    public List<ProdutoLegacyEntity> findBySaldoGreaterThan(BigDecimal var1);

    @Query(value="select p from ProdutoLegacyEntity p order by p.id")
    @QueryHints(value={@QueryHint(name="org.hibernate.fetchSize", value="1000"), @QueryHint(name="org.hibernate.readOnly", value="true")})
    @Transactional(readOnly=true, transactionManager="firebirdTransactionManager")
    public Stream<ProdutoLegacyEntity> streamAll();
}

