package br.com.redemaisfarma.adapters.outbound.persistence.repository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.FiscalDocumentEntity;
import br.com.redemaisfarma.domain.fiscal.FiscalDocumentStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FiscalDocumentRepository
        extends JpaRepository<FiscalDocumentEntity, Long> {

    Optional<FiscalDocumentEntity> findByExternalReference(String externalReference);

    Optional<FiscalDocumentEntity> findTopByPedidoIdOrderByCreatedAtDesc(Long pedidoId);

    List<FiscalDocumentEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<FiscalDocumentEntity> findByStatusInOrderByUpdatedAtAsc(
            List<FiscalDocumentStatus> statuses,
            Pageable pageable
    );
}
