package br.com.lectek.copainsider.adapters.outbound.persistence.repository;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantRepository extends JpaRepository<TenantEntity, Long> {

    Optional<TenantEntity> findByCodigo(String codigo);
}

