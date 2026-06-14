package br.com.lectek.copainsider.adapters.outbound.persistence.jpa;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.CopaProdutoEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CopaProdutoJPARepository extends JpaRepository<CopaProdutoEntity, Long> {

    List<CopaProdutoEntity> findByAtivoTrueOrderByOrdemAsc();

    Optional<CopaProdutoEntity> findBySlugAndAtivoTrue(String slug);

    Optional<CopaProdutoEntity> findByHotmartProductId(Long hotmartProductId);

    @Query("SELECT p FROM CopaProdutoEntity p WHERE p.slug = :slug")
    Optional<CopaProdutoEntity> findBySlugIgnoringAtivo(@Param("slug") String slug);
}
