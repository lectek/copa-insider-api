package br.com.redemaisfarma.adapters.outbound.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "sync_checkpoint",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_sync_checkpoint_source",
                        columnNames = { "source" }
                )
        }
)
public class SyncCheckpoint {

    @Id
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Column(name = "source", nullable = false, length = 100, unique = true)
    private String source;

    @Column(name = "last_since", nullable = false)
    private LocalDateTime lastSince;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public SyncCheckpoint() {
        // JPA only
    }

    public SyncCheckpoint(String id, String source, LocalDateTime lastSince) {
        this.id = id;
        this.source = source;
        this.lastSince = lastSince;
    }

    @PrePersist
    public void prePersist() {
        if (this.lastSince == null) {
            this.lastSince = LocalDateTime.of(1900, 1, 1, 0, 0);
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (this.lastSince == null) {
            this.lastSince = LocalDateTime.of(1900, 1, 1, 0, 0);
        }
    }

    // Getters

    public String getId() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public LocalDateTime getLastSince() {
        return lastSince;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters

    public void setId(String id) {
        this.id = id;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setLastSince(LocalDateTime lastSince) {
        this.lastSince = lastSince;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
