package br.com.lectek.copainsider.adapters.outbound.persistence.repository;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.TenantAddonSubscriptionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantAddonSubscriptionRepository extends JpaRepository<TenantAddonSubscriptionEntity, Long> {

    List<TenantAddonSubscriptionEntity> findAllByTenantIdAndActiveTrue(Long tenantId);
}

