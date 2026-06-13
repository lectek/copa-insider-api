package br.com.lectek.copainsider.adapters.outbound.persistence.jpa;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.CopaAcessoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CopaAcessoJPARepository extends JpaRepository<CopaAcessoEntity, Long> {
    boolean existsByEmailIgnoreCaseAndProdutoSlug(String email, String produtoSlug);
    List<CopaAcessoEntity> findByEmailContainingIgnoreCase(String email);
    Page<CopaAcessoEntity> findAllByOrderByConcedidoEmDesc(Pageable pageable);
    Page<CopaAcessoEntity> findByEmailContainingIgnoreCaseOrderByConcedidoEmDesc(String email, Pageable pageable);
}
