package br.com.redemaisfarma.adapters.outbound.persistence.repository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.FiscalPrintJobEventEntity;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FiscalPrintJobEventRepository
        extends JpaRepository<FiscalPrintJobEventEntity, Long> {

    List<FiscalPrintJobEventEntity> findAllByOrderByCreatedAtDesc(
            Pageable pageable
    );

    List<FiscalPrintJobEventEntity> findByPrintJobPedidoIdOrderByCreatedAtDesc(
            Long pedidoId,
            Pageable pageable
    );
}
