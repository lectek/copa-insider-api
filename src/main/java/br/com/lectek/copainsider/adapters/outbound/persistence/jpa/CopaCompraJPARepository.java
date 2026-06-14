package br.com.lectek.copainsider.adapters.outbound.persistence.jpa;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.CopaCompraEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CopaCompraJPARepository extends JpaRepository<CopaCompraEntity, Long> {
    Optional<CopaCompraEntity> findByTransacao(String transacao);
    Optional<CopaCompraEntity> findByTransacaoIgnoreCase(String transacao);
    boolean existsByTransacao(String transacao);
    Page<CopaCompraEntity> findAllByOrderByCriadoEmDesc(Pageable pageable);
    List<CopaCompraEntity> findTop5ByOrderByCriadoEmDesc();
    Page<CopaCompraEntity> findByCompradorEmailContainingIgnoreCaseOrderByCriadoEmDesc(String email, Pageable pageable);

    @Query("SELECT COALESCE(SUM(c.valor), 0) FROM CopaCompraEntity c WHERE c.status IN ('COMPLETED', 'APPROVED')")
    BigDecimal sumValorAprovado();
}
