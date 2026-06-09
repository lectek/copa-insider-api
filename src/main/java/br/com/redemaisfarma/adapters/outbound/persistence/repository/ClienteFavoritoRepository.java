package br.com.redemaisfarma.adapters.outbound.persistence.repository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteFavoritoEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClienteFavoritoRepository extends JpaRepository<ClienteFavoritoEntity, Long> {

    boolean existsByUsuarioIdAndProdutoId(Long usuarioId, Long produtoId);

    void deleteByUsuarioIdAndProdutoId(Long usuarioId, Long produtoId);

    @Query("""
            select f
              from ClienteFavoritoEntity f
              join fetch f.produto p
             where f.usuario.id = :usuarioId
             order by f.createdAt desc
            """)
    List<ClienteFavoritoEntity> findByUsuarioId(@Param("usuarioId") Long usuarioId);
}
