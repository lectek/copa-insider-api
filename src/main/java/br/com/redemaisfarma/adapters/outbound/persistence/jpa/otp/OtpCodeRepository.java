/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.jpa.repository.JpaRepository
 */
package br.com.redemaisfarma.adapters.outbound.persistence.jpa.otp;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpCodeRepository
extends JpaRepository<OtpCodeEntity, Long> {
    public Optional<OtpCodeEntity> findByDeliveryId(String var1);

    public Optional<OtpCodeEntity> findFirstByDestinationOrderByCreatedAtDesc(String var1);

    public long deleteByExpiresAtBefore(Instant var1);

    public Optional<OtpCodeEntity> findByVerificationToken(String var1);
}

