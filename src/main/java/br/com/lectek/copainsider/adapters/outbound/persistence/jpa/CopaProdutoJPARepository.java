package br.com.lectek.copainsider.adapters.outbound.persistence.jpa;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.CopaProdutoEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CopaProdutoJPARepository extends JpaRepository<CopaProdutoEntity, Long> {

    List<CopaProdutoEntity> findByAtivoTrueOrderByOrdemAsc();

    Optional<CopaProdutoEntity> findBySlugAndAtivoTrue(String slug);

    Optional<CopaProdutoEntity> findByHotmartProductId(Long hotmartProductId);
}
