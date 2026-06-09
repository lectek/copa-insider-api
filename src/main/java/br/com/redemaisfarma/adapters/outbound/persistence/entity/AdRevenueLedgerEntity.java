package br.com.redemaisfarma.adapters.outbound.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ad_revenue_ledger")
public class AdRevenueLedgerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "reference_type", nullable = false, length = 32)
    private String referenceType;

    @Column(name = "reference_id", nullable = false, length = 64)
    private String referenceId;

    @Column(name = "impressions")
    private Long impressions;

    @Column(name = "clicks")
    private Long clicks;

    @Column(name = "revenue_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal revenueAmount;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    public Long getId() {
        return id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public Long getImpressions() {
        return impressions;
    }

    public Long getClicks() {
        return clicks;
    }

    public BigDecimal getRevenueAmount() {
        return revenueAmount;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}

