package br.com.lectek.copainsider.adapters.outbound.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "plan_feature")
public class PlanFeatureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(name = "feature_key", nullable = false, length = 64)
    private String featureKey;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    public Long getId() {
        return id;
    }

    public Long getPlanId() {
        return planId;
    }

    public String getFeatureKey() {
        return featureKey;
    }

    public Boolean getEnabled() {
        return enabled;
    }
}

