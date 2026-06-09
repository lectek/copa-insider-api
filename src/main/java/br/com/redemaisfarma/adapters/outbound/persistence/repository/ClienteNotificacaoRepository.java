package br.com.redemaisfarma.adapters.outbound.persistence.repository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteNotificacaoEntity;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ClienteNotificacaoRepository extends JpaRepository<ClienteNotificacaoEntity, Long> {

    interface AdminListItemProjection {

        Long getId();

        LocalDateTime getCreatedAt();

        String getTipo();

        String getTitulo();

        String getMensagem();

        Boolean getLida();

        Long getUsuarioId();

        String getUsuarioNome();

        String getUsuarioEmail();

        String getUsuarioCpf();
    }

    @Query("""
            select n
              from ClienteNotificacaoEntity n
             where n.usuario.id = :usuarioId
             order by n.createdAt desc
            """)
    List<ClienteNotificacaoEntity> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    long countByUsuarioIdAndLidaFalse(Long usuarioId);

    @Query(
            value = """
                    select n.id as id,
                           n.createdAt as createdAt,
                           n.tipo as tipo,
                           n.titulo as titulo,
                           n.mensagem as mensagem,
                           n.lida as lida,
                           u.id as usuarioId,
                           u.nome as usuarioNome,
                           u.email as usuarioEmail,
                           u.cpf as usuarioCpf
                      from ClienteNotificacaoEntity n
                      join n.usuario u
                     where (:q is null or :q = '' or
                            lower(n.titulo) like lower(concat('%', :q, '%')) or
                            lower(n.mensagem) like lower(concat('%', :q, '%')) or
                            lower(n.tipo) like lower(concat('%', :q, '%')) or
                            lower(u.nome) like lower(concat('%', :q, '%')) or
                            lower(u.email) like lower(concat('%', :q, '%')) or
                            replace(replace(replace(u.cpf,'.',''),'-',''),' ','')
                                like concat(
                                        '%',
                                        replace(replace(replace(:q,'.',''),'-',''),' ',''),
                                        '%'
                                ))
                       and (:tipo is null or :tipo = '' or
                            lower(n.tipo) like lower(concat('%', :tipo, '%')))
                       and (:lida is null or coalesce(n.lida, false) = :lida)
                       and (:createdFrom is null or n.createdAt >= :createdFrom)
                     order by n.createdAt desc, n.id desc
                    """,
            countQuery = """
                    select count(n)
                      from ClienteNotificacaoEntity n
                      join n.usuario u
                     where (:q is null or :q = '' or
                            lower(n.titulo) like lower(concat('%', :q, '%')) or
                            lower(n.mensagem) like lower(concat('%', :q, '%')) or
                            lower(n.tipo) like lower(concat('%', :q, '%')) or
                            lower(u.nome) like lower(concat('%', :q, '%')) or
                            lower(u.email) like lower(concat('%', :q, '%')) or
                            replace(replace(replace(u.cpf,'.',''),'-',''),' ','')
                                like concat(
                                        '%',
                                        replace(replace(replace(:q,'.',''),'-',''),' ',''),
                                        '%'
                                ))
                       and (:tipo is null or :tipo = '' or
                            lower(n.tipo) like lower(concat('%', :tipo, '%')))
                       and (:lida is null or coalesce(n.lida, false) = :lida)
                       and (:createdFrom is null or n.createdAt >= :createdFrom)
                    """
    )
    Page<AdminListItemProjection> searchAdmin(
            @Param("q") String q,
            @Param("tipo") String tipo,
            @Param("lida") Boolean lida,
            @Param("createdFrom") LocalDateTime createdFrom,
            Pageable pageable
    );

    @Query("""
            select count(n)
              from ClienteNotificacaoEntity n
             where n.createdAt >= :createdFrom
            """)
    long countCreatedSince(@Param("createdFrom") LocalDateTime createdFrom);

    @Query("""
            select count(n)
              from ClienteNotificacaoEntity n
             where coalesce(n.lida, false) = false
            """)
    long countUnreadAdmin();

    @Query("""
            select count(n)
              from ClienteNotificacaoEntity n
             where n.createdAt >= :createdFrom
               and upper(n.tipo) = upper(:tipo)
            """)
    long countCreatedSinceByTipo(
            @Param("createdFrom") LocalDateTime createdFrom,
            @Param("tipo") String tipo
    );

    @Query("""
            select count(distinct upper(n.tipo))
              from ClienteNotificacaoEntity n
             where n.createdAt >= :createdFrom
            """)
    long countDistinctTiposSince(@Param("createdFrom") LocalDateTime createdFrom);

    @Transactional
    @Modifying
    @Query("""
            update ClienteNotificacaoEntity n
               set n.lida = true
             where n.usuario.id = :usuarioId
               and n.id in :ids
            """)
    int markAsRead(@Param("usuarioId") Long usuarioId, @Param("ids") Collection<Long> ids);
}
