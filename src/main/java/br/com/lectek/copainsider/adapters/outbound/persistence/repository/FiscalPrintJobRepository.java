package br.com.lectek.copainsider.adapters.outbound.persistence.repository;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.FiscalPrintJobEntity;
import br.com.lectek.copainsider.domain.fiscal.FiscalPrintJobStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FiscalPrintJobRepository
        extends JpaRepository<FiscalPrintJobEntity, Long> {

    Optional<FiscalPrintJobEntity> findTopByFiscalDocumentIdAndJobTypeOrderByCreatedAtDesc(
            Long fiscalDocumentId,
            br.com.lectek.copainsider.domain.fiscal.FiscalPrintJobType jobType
    );

    List<FiscalPrintJobEntity> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    List<FiscalPrintJobEntity> findByPedidoIdOrderByUpdatedAtDesc(
            Long pedidoId,
            Pageable pageable
    );

    List<FiscalPrintJobEntity> findByStatusOrderByPriorityDescScheduledForAscCreatedAtAsc(
            FiscalPrintJobStatus status,
            Pageable pageable
    );

    Optional<FiscalPrintJobEntity> findTopByStationIdAndStatusOrderByStartedAtDesc(
            Long stationId,
            FiscalPrintJobStatus status
    );
}
