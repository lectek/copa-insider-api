/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 *  org.springframework.data.jpa.repository.JpaRepository
 */
package br.com.redemaisfarma.adapters.outbound.persistence.jpa;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProductImageJobEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageJobJpaRepository
extends JpaRepository<ProductImageJobEntity, Long> {
    public Page<ProductImageJobEntity> findByStatusOrderByCreatedAtAsc(String var1, Pageable var2);
    public ProductImageJobEntity findTopByProductIdOrderByIdDesc(Long var1);

    public boolean existsByProductIdAndFingerprint(Long var1, String var2);
}
