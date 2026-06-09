/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.scheduling.annotation.Scheduled
 *  org.springframework.stereotype.Component
 *  org.springframework.transaction.annotation.Propagation
 *  org.springframework.transaction.annotation.Transactional
 */
package br.com.lectek.copainsider.application.service.otp;

import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.otp.OtpCodeRepository;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OtpCleanupJob {
    private static final Logger log = LoggerFactory.getLogger(OtpCleanupJob.class);
    private final OtpCodeRepository repo;

    public OtpCleanupJob(OtpCodeRepository repo) {
        this.repo = repo;
    }

    @Scheduled(fixedDelayString="${otp.cleanup.fixed-delay-ms:300000}")
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    public void run() {
        try {
            long removed = this.repo.deleteByExpiresAtBefore(Instant.now());
            if (removed > 0L) {
                log.info("otp_cleanup: removidos {}", (Object)removed);
            } else {
                log.debug("otp_cleanup: nenhum registro expirado para remover");
            }
        }
        catch (Exception e) {
            log.error("otp_cleanup: falha ao remover registros expirados", (Throwable)e);
        }
    }
}

