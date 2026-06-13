package br.com.lectek.copainsider.adapters.outbound.persistence.jpa;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.CopaCompraEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CopaCompraJPARepository extends JpaRepository<CopaCompraEntity, Long> {
    Optional<CopaCompraEntity> findByTransacao(String transacao);
    boolean existsByTransacao(String transacao);
    Page<CopaCompraEntity> findAllByOrderByCriadoEmDesc(Pageable pageable);
}
