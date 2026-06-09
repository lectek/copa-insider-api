/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.jpa.repository.JpaRepository
 */
package br.com.lectek.copainsider.adapters.outbound.persistence.repository;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.SyncCheckpoint;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncCheckpointRepository
extends JpaRepository<SyncCheckpoint, String> {
    public Optional<SyncCheckpoint> findBySource(String var1);
}

