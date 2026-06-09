package br.com.redemaisfarma.adapters.outbound.persistence.repository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.PlanFeatureEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanFeatureRepository extends JpaRepository<PlanFeatureEntity, Long> {

    List<PlanFeatureEntity> findAllByPlanIdAndEnabledTrue(Long planId);
}

