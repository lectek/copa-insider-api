package br.com.redemaisfarma.adapters.outbound.persistence.repository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.EntregaRotaEntity;
import br.com.redemaisfarma.domain.enums.EntregaRotaStatus;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntregaRotaRepository
        extends JpaRepository<EntregaRotaEntity, Long> {

    long countByStatusIn(List<EntregaRotaStatus> statuses);

    List<EntregaRotaEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
