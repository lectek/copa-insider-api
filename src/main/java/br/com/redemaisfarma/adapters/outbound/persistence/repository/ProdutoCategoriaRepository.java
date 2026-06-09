package br.com.redemaisfarma.adapters.outbound.persistence.repository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoCategoriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProdutoCategoriaRepository extends JpaRepository<ProdutoCategoriaEntity, Long> {

    boolean existsByNomeIgnoreCase(String nome);

    Optional<ProdutoCategoriaEntity> findByNomeIgnoreCase(String nome);

    @Query("select c.nome from ProdutoCategoriaEntity c order by lower(c.nome) asc")
    List<String> findAllNomes();
}
