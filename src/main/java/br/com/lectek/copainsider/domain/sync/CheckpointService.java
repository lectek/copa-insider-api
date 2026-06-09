// src/main/java/br/com/lectek/copainsider/domain/sync/CheckpointService.java
package br.com.lectek.copainsider.domain.sync;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.SyncCheckpoint;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.SyncCheckpointRepository;
import org.springframework.stereotype.Service;

import java.time.*;

@Service
public class CheckpointService {
    private static final ZoneId ZONE = ZoneOffset.UTC; // fuso único
    private final SyncCheckpointRepository repo;

    public CheckpointService(SyncCheckpointRepository repo) {
        this.repo = repo;
    }

    // ===== API principal em LocalDateTime =====
    public LocalDateTime readSince(String source, LocalDateTime defaultSince) {
        return this.repo.findBySource(source)
                .map(SyncCheckpoint::getLastSince)
                .orElse(defaultSince);
    }

    public void writeSince(String source, LocalDateTime since) {
        SyncCheckpoint cp = this.repo.findBySource(source).orElseGet(() -> {
            SyncCheckpoint c = new SyncCheckpoint();
            c.setId(source);
            c.setSource(source);
            return c;
        });
        cp.setLastSince(since);
        this.repo.save(cp);
    }

    // ===== Overloads convenientes em Instant =====
    public Instant readSinceInstant(String source, Instant defaultSince) {
        return readSinceInstant(source, defaultSince, ZONE);
    }

    public Instant readSinceInstant(String source, Instant defaultSince, ZoneId zone) {
        LocalDateTime ldt = readSince(source,
                LocalDateTime.ofInstant(defaultSince, zone));
        return ldt.atZone(zone).toInstant();
    }

    public void updateSince(String source, Instant since) {
        updateSince(source, since, ZONE);
    }

    public void updateSince(String source, Instant since, ZoneId zone) {
        writeSince(source, LocalDateTime.ofInstant(since, zone));
    }

    // ===== Helper compatível com código legado =====
    /** Retorna o since em Instant ou EPOCH quando não houver checkpoint. */
    public Instant findSinceOrEpoch(String source) {
        LocalDateTime def = LocalDateTime.ofInstant(Instant.EPOCH, ZONE);
        LocalDateTime ldt = readSince(source, def);
        return ldt.atZone(ZONE).toInstant();
    }
}
