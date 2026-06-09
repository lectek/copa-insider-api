/*
 * Decompiled with CFR 0.152.
 */
package br.com.lectek.copainsider.application.port.outbound;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ProductImageJobRepository {
    public Job createQueued(Long var1, String var2);

    public void markRunning(Long var1);

    public void markDone(Long var1, String var2);

    public void markError(Long var1, String var2);

    public void markSkipped(Long var1, String var2);

    public Optional<Job> findLastByProduct(Long var1);

    public List<Job> findByStatus(Status var1, int var2, int var3);

    public boolean existsByProductIdAndFingerprint(Long var1, String var2);

    public record Job(Long id, Long productId, Status status, String resultUrl, String errorMsg, String fingerprint, Instant createdAt, Instant updatedAt) {
    }

    public static enum Status {
        QUEUED,
        RUNNING,
        DONE,
        ERROR,
        SKIPPED;

    }
}

