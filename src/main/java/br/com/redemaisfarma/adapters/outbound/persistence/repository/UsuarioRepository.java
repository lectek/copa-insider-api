// src/main/java/br/com/redemaisfarma/adapters/outbound/persistence/repository/UsuarioRepository.java
package br.com.redemaisfarma.adapters.outbound.persistence.repository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

    // ===== Métodos já esperados por outros componentes =====
    @Transactional(readOnly = true)
    Optional<UsuarioEntity> findByEmailIgnoreCase(String email);

    @Transactional(readOnly = true)
    boolean existsByEmailIgnoreCase(String email);

    @Transactional(readOnly = true)
    Optional<UsuarioEntity> findByCpf(String cpf);

    @Transactional(readOnly = true)
    boolean existsByCpf(String cpf);

    @Transactional(readOnly = true)
    @Query("""
        select u
          from UsuarioEntity u
         where lower(u.email) = lower(:ident)
            or replace(replace(replace(u.cpf,'.',''),'-',''),' ','')
             = replace(replace(replace(:ident,'.',''),'-',''),' ','')
    """)
    Optional<UsuarioEntity> findByEmailOrCpf(@Param("ident") String ident);

    @Transactional(readOnly = true)
    @Query("""
        select case when count(u) > 0 then true else false end
          from UsuarioEntity u
         where lower(u.email) = lower(:ident)
            or replace(replace(replace(u.cpf,'.',''),'-',''),' ','')
             = replace(replace(replace(:ident,'.',''),'-',''),' ','')
    """)
    boolean existsByEmailOrCpf(@Param("ident") String ident);

    // ===== Busca para a listagem do Admin (q + papel/role por nome) =====
    @Transactional(readOnly = true)
    @Query("""
        select distinct u
          from UsuarioEntity u
          left join u.roles r
         where (:q is null or :q = '' or
                lower(u.nome)  like lower(concat('%', :q, '%')) or
                lower(u.email) like lower(concat('%', :q, '%')) or
                replace(replace(replace(u.cpf,'.',''),'-',''),' ','')
                  like replace(replace(replace(:q,'.',''),'-',''),' ',''))
           and (:papel is null or :papel = '' or upper(r.nome) = upper(:papel))
         order by u.nome asc
    """)
    List<UsuarioEntity> search(@Param("q") String q, @Param("papel") String papel);
}
