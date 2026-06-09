/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.data.jpa.repository.Modifying
 *  org.springframework.data.jpa.repository.Query
 *  org.springframework.data.repository.query.Param
 *  org.springframework.stereotype.Repository
 */
package br.com.redemaisfarma.adapters.outbound.persistence.jpa;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.RefreshTokenEntity;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenJpaRepository
extends JpaRepository<RefreshTokenEntity, Long> {
    public Optional<RefreshTokenEntity> findByToken(String var1);

    public long deleteByExpiresAtBefore(Instant var1);

    @Modifying(clearAutomatically=true, flushAutomatically=true)
    @Query(value="update RefreshTokenEntity r\n   set r.revokedAt = :when\n where r.userId = :userId\n   and r.revokedAt is null\n   and (:tenantId is null or r.tenantId = :tenantId)\n")
    public int revokeAllForUser(@Param(value="userId") Long var1, @Param(value="tenantId") String var2, @Param(value="when") Instant var3);
}

