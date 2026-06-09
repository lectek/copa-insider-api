/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.jpa.repository.JpaRepository
 */
package br.com.redemaisfarma.adapters.outbound.persistence.repository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.PasswordResetTokenEntity;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository
extends JpaRepository<PasswordResetTokenEntity, Long> {
    public Optional<PasswordResetTokenEntity> findByToken(String var1);

    public long deleteByUsadoIsTrueOrExpiraEmBefore(LocalDateTime var1);

    public long deleteByUsuarioId(Long var1);
}

