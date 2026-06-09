package br.com.lectek.copainsider.adapters.outbound.persistence.repository;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.TenantPlanEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantPlanRepository extends JpaRepository<TenantPlanEntity, Long> {

    Optional<TenantPlanEntity> findByTenantId(Long tenantId);
}

