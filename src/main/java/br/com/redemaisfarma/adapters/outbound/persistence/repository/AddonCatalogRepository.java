package br.com.redemaisfarma.adapters.outbound.persistence.repository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.AddonCatalogEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddonCatalogRepository extends JpaRepository<AddonCatalogEntity, Long> {

    Optional<AddonCatalogEntity> findByIdAndActiveTrue(Long id);

    List<AddonCatalogEntity> findAllByIdInAndActiveTrue(List<Long> ids);
}

