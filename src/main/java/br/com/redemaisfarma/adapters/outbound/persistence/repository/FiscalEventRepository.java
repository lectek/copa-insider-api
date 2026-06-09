package br.com.redemaisfarma.adapters.outbound.persistence.repository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.FiscalEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FiscalEventRepository extends JpaRepository<FiscalEventEntity, Long> {
}
