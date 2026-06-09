package br.com.redemaisfarma.adapters.outbound.persistence.repository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.TenantAddonSubscriptionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantAddonSubscriptionRepository extends JpaRepository<TenantAddonSubscriptionEntity, Long> {

    List<TenantAddonSubscriptionEntity> findAllByTenantIdAndActiveTrue(Long tenantId);
}

