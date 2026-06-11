package br.com.lectek.copainsider.adapters.outbound.persistence.jpa;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.CopaAcessoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CopaAcessoJPARepository extends JpaRepository<CopaAcessoEntity, Long> {
    boolean existsByEmailIgnoreCaseAndProdutoSlug(String email, String produtoSlug);
}
