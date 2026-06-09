package br.com.redemaisfarma.adapters.outbound.persistence.repository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.PlanCatalogEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanCatalogRepository extends JpaRepository<PlanCatalogEntity, Long> {

    Optional<PlanCatalogEntity> findByCode(String code);
}

