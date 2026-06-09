package br.com.redemaisfarma.adapters.outbound.persistence.entity;

import br.com.redemaisfarma.domain.fiscal.FiscalPrintStationRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "fiscal_print_station",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_fiscal_print_station_code",
                        columnNames = "code"
                )
        }
)
public class FiscalPrintStationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(name = "printer_name", length = 180)
    private String printerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FiscalPrintStationRole role;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "last_heartbeat_at")
    private LocalDateTime lastHeartbeatAt;

    @Column(length = 255)
    private String notes;

    @Column(name = "api_key_hash", length = 255)
    private String apiKeyHash;

    @Column(name = "api_key_last_rotated_at")
    private LocalDateTime apiKeyLastRotatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    @PrePersist
    public void onCreate() {
        final LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        normalize();
        if (role == null) {
            role = FiscalPrintStationRole.FLEX;
        }
        if (version == null) {
            version = 0L;
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
        normalize();
    }

    private void normalize() {
        code = trim(code);
        displayName = trim(displayName);
        printerName = trim(printerName);
        notes = trim(notes);
        apiKeyHash = trim(apiKeyHash);
    }

    private static String trim(final String value) {
        return value == null ? null : value.trim();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long idValue) {
        id = idValue;
    }

    public String getCode() {
        return code;
    }

    public void setCode(final String codeValue) {
        code = codeValue;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final String displayNameValue) {
        displayName = displayNameValue;
    }

    public String getPrinterName() {
        return printerName;
    }

    public void setPrinterName(final String printerNameValue) {
        printerName = printerNameValue;
    }

    public FiscalPrintStationRole getRole() {
        return role;
    }

    public void setRole(final FiscalPrintStationRole roleValue) {
        role = roleValue;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean activeValue) {
        active = activeValue;
    }

    public LocalDateTime getLastHeartbeatAt() {
        return lastHeartbeatAt;
    }

    public void setLastHeartbeatAt(final LocalDateTime lastHeartbeatAtValue) {
        lastHeartbeatAt = lastHeartbeatAtValue;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(final String notesValue) {
        notes = notesValue;
    }

    public String getApiKeyHash() {
        return apiKeyHash;
    }

    public void setApiKeyHash(final String apiKeyHashValue) {
        apiKeyHash = apiKeyHashValue;
    }

    public LocalDateTime getApiKeyLastRotatedAt() {
        return apiKeyLastRotatedAt;
    }

    public void setApiKeyLastRotatedAt(
            final LocalDateTime apiKeyLastRotatedAtValue
    ) {
        apiKeyLastRotatedAt = apiKeyLastRotatedAtValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }
}
