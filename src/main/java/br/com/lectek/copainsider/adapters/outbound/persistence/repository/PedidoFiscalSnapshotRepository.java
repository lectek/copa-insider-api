package br.com.lectek.copainsider.adapters.outbound.persistence.repository;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.PedidoFiscalSnapshotEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoFiscalSnapshotRepository
        extends JpaRepository<PedidoFiscalSnapshotEntity, Long> {

    Optional<PedidoFiscalSnapshotEntity> findByPedidoId(Long pedidoId);
}
