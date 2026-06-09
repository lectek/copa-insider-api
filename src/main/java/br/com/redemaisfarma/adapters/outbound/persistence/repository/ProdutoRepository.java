package br.com.redemaisfarma.adapters.outbound.persistence.repository;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.MetodoLeituraCodigoBarras;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.redemaisfarma.domain.support.BarcodeNormalizer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<ProdutoEntity, Long> {

    List<MetodoLeituraCodigoBarras> PUBLIC_ALLOWED_SOURCES = List.of(
            MetodoLeituraCodigoBarras.MANUAL,
            MetodoLeituraCodigoBarras.SCANNER,
            MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA,
            MetodoLeituraCodigoBarras.API
    );

    Optional<ProdutoEntity> findByCodigoBarras(String codigoBarras);

    Optional<ProdutoEntity> findByTenantIdAndId(Long tenantId, Long id);

    Page<ProdutoEntity> findByTenantId(Long tenantId, Pageable pageable);

    Optional<ProdutoEntity> findByTenantIdAndCodigoBarras(Long tenantId, String codigoBarras);

    boolean existsByCodigoBarras(String codigoBarras);

    Optional<ProdutoEntity> findByCodigoOriginal(Long codigoOriginal);

    boolean existsByCodigoOriginal(Long codigoOriginal);

    @Query("""
            SELECT p FROM ProdutoEntity p
            WHERE p.codigoBarras = :codigo
               OR str(p.codigoOriginal) = :codigo
            ORDER BY CASE WHEN p.codigoBarras = :codigo THEN 0 ELSE 1 END, p.id ASC
            """)
    List<ProdutoEntity> findAllByAnyCodigo(@Param("codigo") String codigo);

    @Query("""
            SELECT p FROM ProdutoEntity p
            WHERE p.tenantId = :tenantId
              AND (p.codigoBarras = :codigo OR str(p.codigoOriginal) = :codigo)
            ORDER BY CASE WHEN p.codigoBarras = :codigo THEN 0 ELSE 1 END, p.id ASC
            """)
    List<ProdutoEntity> findAllByTenantAndAnyCodigo(@Param("tenantId") Long tenantId, @Param("codigo") String codigo);

    Optional<ProdutoEntity> findByLegacyId(Long legacyId);

    Optional<ProdutoEntity> findByTenantIdAndLegacyId(Long tenantId, Long legacyId);

    List<ProdutoEntity> findAllByLegacyIdOrderByIdAsc(Long legacyId);

    List<ProdutoEntity> findAllByTenantIdAndLegacyIdOrderByIdAsc(Long tenantId, Long legacyId);

    List<ProdutoEntity> findAllByLegacyIdIn(Collection<Long> legacyIds);

    @Query("""
            SELECT p FROM ProdutoEntity p
            WHERE p.codigoBarras IN :codigos
               OR str(p.codigoOriginal) IN :codigos
            ORDER BY p.id ASC
            """)
    List<ProdutoEntity> findAllByAnyCodigoIn(@Param("codigos") Collection<String> codigos);

    List<ProdutoEntity> findAllByMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras metodoLeituraCodigoBarras);

    boolean existsByLegacyId(Long legacyId);

    List<ProdutoEntity> findAllByCategoria(String categoria);

    Page<ProdutoEntity> findByCategoriaIgnoreCase(String categoria, Pageable pageable);

    long countByCategoriaIgnoreCase(String categoria);

    @Query("""
            SELECT p FROM ProdutoEntity p
            WHERE p.estoque > 0
              AND p.estoque < COALESCE(p.alertaEstoqueLimite, :limite)
            """)
    List<ProdutoEntity> findComEstoqueBaixo(@Param("limite") Integer limite);

    @Query("""
            SELECT p FROM ProdutoEntity p
            WHERE p.tenantId = :tenantId
              AND p.estoque > 0
              AND p.estoque < COALESCE(p.alertaEstoqueLimite, :limite)
            """)
    List<ProdutoEntity> findComEstoqueBaixo(@Param("tenantId") Long tenantId,
                                            @Param("limite") Integer limite);

    @Query("""
            SELECT p FROM ProdutoEntity p
            ORDER BY
              CASE
                WHEN p.estoque IS NOT NULL
                 AND p.estoque > 0
                 AND p.estoque < COALESCE(p.alertaEstoqueLimite, :limite)
                THEN 0 ELSE 1
              END,
              CASE
                WHEN p.estoque IS NULL THEN 999999999
                ELSE p.estoque
              END,
              LOWER(COALESCE(p.nome, '')),
              p.id
            """)
    Page<ProdutoEntity> findPaginaNiveisEstoque(@Param("limite") Integer limite, Pageable pageable);

    @Query("""
            SELECT p FROM ProdutoEntity p
            WHERE p.tenantId = :tenantId
            ORDER BY
              CASE
                WHEN p.estoque IS NOT NULL
                 AND p.estoque > 0
                 AND p.estoque < COALESCE(p.alertaEstoqueLimite, :limite)
                THEN 0 ELSE 1
              END,
              CASE
                WHEN p.estoque IS NULL THEN 999999999
                ELSE p.estoque
              END,
              LOWER(COALESCE(p.nome, '')),
              p.id
            """)
    Page<ProdutoEntity> findPaginaNiveisEstoque(@Param("tenantId") Long tenantId,
                                                @Param("limite") Integer limite,
                                                Pageable pageable);

    long countByDisponivelTrue();

    @Query("""
            SELECT COUNT(p) FROM ProdutoEntity p
            WHERE p.estoque IS NULL OR p.estoque <= 0
            """)
    long countSemEstoque();

    @Query("""
            SELECT COUNT(p) FROM ProdutoEntity p
            WHERE p.estoque IS NOT NULL
              AND p.estoque > 0
              AND p.estoque < COALESCE(p.alertaEstoqueLimite, :limite)
            """)
    long countComEstoqueBaixo(@Param("limite") Integer limite);

    List<ProdutoEntity> findByDisponivelTrue();

    @Query("SELECT p FROM ProdutoEntity p WHERE p.imagem IS NULL OR TRIM(p.imagem) = ''")
    Page<ProdutoEntity> findSemMidia(Pageable pageable);

    Page<ProdutoEntity> findByDescricaoContainingIgnoreCaseOrCodigoBarrasContainingIgnoreCase(
            String descricao, String codigoBarras, Pageable pageable);

    Page<ProdutoEntity> findByNomeContainingIgnoreCaseOrCodigoBarrasContainingIgnoreCase(
            String nome, String codigoBarras, Pageable pageable);

    List<ProdutoEntity> findTop2000ByOrderByIdAsc();

    List<ProdutoEntity> findTop2000ByDescricaoContainingIgnoreCaseOrCodigoBarrasContainingIgnoreCaseOrderByIdAsc(
            String descricao, String codigoBarras);

    Optional<ProdutoEntity> findFirstByNomeContainingIgnoreCase(String nome);

    Optional<ProdutoEntity> findFirstByTenantIdAndNomeContainingIgnoreCase(Long tenantId, String nome);

    @Query("select p from ProdutoEntity p order by p.dataCadastro desc")
    List<ProdutoEntity> findRecent(Pageable pageable);

    @Query("select p from ProdutoEntity p where p.disponivel = true order by p.dataCadastro desc")
    List<ProdutoEntity> findRecentDisponiveis(Pageable pageable);

    @Query("select p from ProdutoEntity p where p.id in :ids")
    List<ProdutoEntity> findAllByIdIn(Collection<Long> ids);

    @Query("select p from ProdutoEntity p where p.tenantId = :tenantId and p.id in :ids")
    List<ProdutoEntity> findAllByTenantIdIn(@Param("tenantId") Long tenantId,
                                            @Param("ids") Collection<Long> ids);

    @Query("select p from ProdutoEntity p where p.disponivel = true and p.id in :ids")
    List<ProdutoEntity> findAllDisponiveisByIdIn(Collection<Long> ids);

    @Query("""
            select p from ProdutoEntity p
            where p.id in :ids
              and p.disponivel = true
              and p.estoque > 0
              and p.precoVenda > 0
              and (
                (p.codigoBarras is not null and trim(p.codigoBarras) <> '')
                or (p.codigoOriginal is not null and length(str(p.codigoOriginal)) in (7, 8, 11, 12, 13, 14))
              )
              and p.metodoLeituraCodigoBarras in :allowedSources
              and p.status = br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus.PUBLICADO
              and (p.publicadoEm is null or p.publicadoEm <= CURRENT_TIMESTAMP)
              and (p.despublicadoEm is null or p.despublicadoEm > CURRENT_TIMESTAMP)
            """)
    List<ProdutoEntity> findAllPublicByIdIn(@Param("ids")
                                            Collection<Long> ids,
                                            @Param("allowedSources")
                                            Collection<MetodoLeituraCodigoBarras> allowedSources);

    @Query("""
            select p from ProdutoEntity p
            where p.tenantId = :tenantId
              and p.id in :ids
              and p.disponivel = true
              and p.estoque > 0
              and p.precoVenda > 0
              and (
                (p.codigoBarras is not null and trim(p.codigoBarras) <> '')
                or (p.codigoOriginal is not null and length(str(p.codigoOriginal)) in (7, 8, 11, 12, 13, 14))
              )
              and p.metodoLeituraCodigoBarras in :allowedSources
              and p.status = br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus.PUBLICADO
              and (p.publicadoEm is null or p.publicadoEm <= CURRENT_TIMESTAMP)
              and (p.despublicadoEm is null or p.despublicadoEm > CURRENT_TIMESTAMP)
            """)
    List<ProdutoEntity> findAllPublicByIdIn(@Param("tenantId") Long tenantId,
                                            @Param("ids") Collection<Long> ids,
                                            @Param("allowedSources")
                                            Collection<MetodoLeituraCodigoBarras> allowedSources);

    @Query("""
            select distinct trim(p.categoria)
            from ProdutoEntity p
            where p.categoria is not null and trim(p.categoria) <> ''
            order by trim(p.categoria) asc
            """)
    List<String> findDistinctCategorias();

    @Query("""
            select distinct trim(p.categoria)
            from ProdutoEntity p
            where p.categoria is not null
              and trim(p.categoria) <> ''
              and p.disponivel = true
              and p.estoque > 0
              and p.precoVenda > 0
              and (
                (p.codigoBarras is not null and trim(p.codigoBarras) <> '')
                or (p.codigoOriginal is not null and length(str(p.codigoOriginal)) in (7, 8, 11, 12, 13, 14))
              )
              and p.metodoLeituraCodigoBarras in :allowedSources
              and p.status = br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus.PUBLICADO
              and (p.publicadoEm is null or p.publicadoEm <= CURRENT_TIMESTAMP)
              and (p.despublicadoEm is null or p.despublicadoEm > CURRENT_TIMESTAMP)
            order by trim(p.categoria) asc
            """)
    List<String> findDistinctCategoriasPublicas(
            @Param("allowedSources")
            Collection<MetodoLeituraCodigoBarras> allowedSources
    );

    @Query("""
            select distinct trim(p.categoria)
            from ProdutoEntity p
            where p.tenantId = :tenantId
              and p.categoria is not null
              and trim(p.categoria) <> ''
              and p.disponivel = true
              and p.estoque > 0
              and p.precoVenda > 0
              and (
                (p.codigoBarras is not null and trim(p.codigoBarras) <> '')
                or (p.codigoOriginal is not null and length(str(p.codigoOriginal)) in (7, 8, 11, 12, 13, 14))
              )
              and p.metodoLeituraCodigoBarras in :allowedSources
              and p.status = br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus.PUBLICADO
              and (p.publicadoEm is null or p.publicadoEm <= CURRENT_TIMESTAMP)
              and (p.despublicadoEm is null or p.despublicadoEm > CURRENT_TIMESTAMP)
            order by trim(p.categoria) asc
            """)
    List<String> findDistinctCategoriasPublicas(
            @Param("tenantId") Long tenantId,
            @Param("allowedSources") Collection<MetodoLeituraCodigoBarras> allowedSources
    );

    @Query("""
            SELECT p FROM ProdutoEntity p
            WHERE p.disponivel = true
              AND p.estoque > 0
              AND p.precoVenda > 0
              AND (
                (p.codigoBarras IS NOT NULL AND TRIM(p.codigoBarras) <> '')
                OR (p.codigoOriginal IS NOT NULL AND LENGTH(str(p.codigoOriginal)) IN (7, 8, 11, 12, 13, 14))
              )
              AND p.metodoLeituraCodigoBarras in :allowedSources
              AND p.status = br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus.PUBLICADO
              AND p.destaqueCarrossel = true
              AND (p.publicadoEm IS NULL OR p.publicadoEm <= CURRENT_TIMESTAMP)
              AND (p.despublicadoEm IS NULL OR p.despublicadoEm > CURRENT_TIMESTAMP)
            ORDER BY COALESCE(p.ordemCarrossel, 9999) ASC, p.dataCadastro DESC, p.id DESC
            """)
    List<ProdutoEntity> findCarrossel(
            @Param("allowedSources")
            Collection<MetodoLeituraCodigoBarras> allowedSources,
            Pageable pageable
    );

    @Query("""
            SELECT p FROM ProdutoEntity p
            WHERE p.tenantId = :tenantId
              AND p.disponivel = true
              AND p.estoque > 0
              AND p.precoVenda > 0
              AND (
                (p.codigoBarras IS NOT NULL AND TRIM(p.codigoBarras) <> '')
                OR (p.codigoOriginal IS NOT NULL AND LENGTH(str(p.codigoOriginal)) IN (7, 8, 11, 12, 13, 14))
              )
              AND p.metodoLeituraCodigoBarras in :allowedSources
              AND p.status = br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus.PUBLICADO
              AND p.destaqueCarrossel = true
              AND (p.publicadoEm IS NULL OR p.publicadoEm <= CURRENT_TIMESTAMP)
              AND (p.despublicadoEm IS NULL OR p.despublicadoEm > CURRENT_TIMESTAMP)
            ORDER BY COALESCE(p.ordemCarrossel, 9999) ASC, p.dataCadastro DESC, p.id DESC
            """)
    List<ProdutoEntity> findCarrossel(
            @Param("tenantId") Long tenantId,
            @Param("allowedSources") Collection<MetodoLeituraCodigoBarras> allowedSources,
            Pageable pageable
    );

    @Query("""
            SELECT p FROM ProdutoEntity p
            WHERE p.disponivel = true
              AND p.estoque > 0
              AND p.precoVenda > 0
              AND (
                (p.codigoBarras IS NOT NULL AND TRIM(p.codigoBarras) <> '')
                OR (p.codigoOriginal IS NOT NULL AND LENGTH(str(p.codigoOriginal)) IN (7, 8, 11, 12, 13, 14))
              )
              AND p.metodoLeituraCodigoBarras in :allowedSources
              AND p.status = br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus.PUBLICADO
              AND (p.publicadoEm IS NULL OR p.publicadoEm <= CURRENT_TIMESTAMP)
              AND (p.despublicadoEm IS NULL OR p.despublicadoEm > CURRENT_TIMESTAMP)
            ORDER BY p.dataCadastro DESC, p.id DESC
            """)
    List<ProdutoEntity> findVitrineFallback(
            @Param("allowedSources")
            Collection<MetodoLeituraCodigoBarras> allowedSources,
            Pageable pageable
    );

    @Query("""
            SELECT p FROM ProdutoEntity p
            WHERE p.tenantId = :tenantId
              AND p.disponivel = true
              AND p.estoque > 0
              AND p.precoVenda > 0
              AND (
                (p.codigoBarras IS NOT NULL AND TRIM(p.codigoBarras) <> '')
                OR (p.codigoOriginal IS NOT NULL AND LENGTH(str(p.codigoOriginal)) IN (7, 8, 11, 12, 13, 14))
              )
              AND p.metodoLeituraCodigoBarras in :allowedSources
              AND p.status = br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus.PUBLICADO
              AND (p.publicadoEm IS NULL OR p.publicadoEm <= CURRENT_TIMESTAMP)
              AND (p.despublicadoEm IS NULL OR p.despublicadoEm > CURRENT_TIMESTAMP)
            ORDER BY p.dataCadastro DESC, p.id DESC
            """)
    List<ProdutoEntity> findVitrineFallback(
            @Param("tenantId") Long tenantId,
            @Param("allowedSources") Collection<MetodoLeituraCodigoBarras> allowedSources,
            Pageable pageable
    );

    Page<ProdutoEntity> findByStatus(ProdutoStatus status, Pageable pageable);

    Page<ProdutoEntity> findByStatusOrderByDataImportacaoAsc(ProdutoStatus status, Pageable pageable);

    @Query("""
            SELECT p FROM ProdutoEntity p
            WHERE
              (:cat IS NULL OR LOWER(TRIM(p.categoria)) = LOWER(TRIM(:cat)))
              AND (
                :q IS NULL
                OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.nome)      LIKE LOWER(CONCAT('%', :q, '%'))
                OR p.codigoBarras     LIKE CONCAT('%', :q, '%')
                OR str(p.codigoOriginal) LIKE CONCAT('%', :q, '%')
              )
            """)
    Page<ProdutoEntity> searchPageByCategoria(@Param("q") String q,
                                              @Param("cat") String categoria,
                                              Pageable pageable);

    @Query("""
            SELECT p FROM ProdutoEntity p
            WHERE p.tenantId = :tenantId
              AND (:cat IS NULL OR LOWER(TRIM(p.categoria)) = LOWER(TRIM(:cat)))
              AND (
                :q IS NULL
                OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.nome)      LIKE LOWER(CONCAT('%', :q, '%'))
                OR p.codigoBarras     LIKE CONCAT('%', :q, '%')
                OR str(p.codigoOriginal) LIKE CONCAT('%', :q, '%')
              )
            """)
    Page<ProdutoEntity> searchPageByCategoria(@Param("tenantId") Long tenantId,
                                              @Param("q") String q,
                                              @Param("cat") String categoria,
                                              Pageable pageable);

    @Query("""
            SELECT p FROM ProdutoEntity p
            WHERE
              (:cat IS NULL OR LOWER(TRIM(p.categoria)) = LOWER(TRIM(:cat)))
              AND (
                :q IS NULL
                OR LOWER(COALESCE(p.descricao, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(COALESCE(p.nome, '')) LIKE LOWER(CONCAT('%', :q, '%'))
                OR COALESCE(p.codigoBarras, '') LIKE CONCAT('%', :q, '%')
                OR str(p.codigoOriginal) LIKE CONCAT('%', :q, '%')
              )
              AND (
                :statusFilter IS NULL
                OR (:statusFilter = 'DISPONIVEL' AND p.disponivel = true)
                OR (:statusFilter = 'INDISPONIVEL' AND (p.disponivel IS NULL OR p.disponivel = false))
              )
              AND (
                :estoqueFilter IS NULL
                OR (:estoqueFilter = 'SEM_ESTOQUE' AND (p.estoque IS NULL OR p.estoque <= 0))
                OR (:estoqueFilter = 'BAIXO'
                    AND p.estoque IS NOT NULL
                    AND p.estoque > 0
                    AND p.estoque < COALESCE(p.alertaEstoqueLimite, :limite))
                OR (:estoqueFilter = 'NORMAL'
                    AND p.estoque IS NOT NULL
                    AND p.estoque >= COALESCE(p.alertaEstoqueLimite, :limite))
              )
            """)
    Page<ProdutoEntity> searchAdminPage(@Param("q") String q,
                                        @Param("cat") String categoria,
                                        @Param("statusFilter") String statusFilter,
                                        @Param("estoqueFilter") String estoqueFilter,
                                        @Param("limite") Integer limite,
                                        Pageable pageable);

    @Query("""
            SELECT p FROM ProdutoEntity p
            WHERE p.disponivel = true AND p.estoque > 0 AND p.precoVenda > 0
              AND (
                (p.codigoBarras IS NOT NULL AND TRIM(p.codigoBarras) <> '')
                OR (p.codigoOriginal IS NOT NULL AND LENGTH(str(p.codigoOriginal)) IN (7, 8, 11, 12, 13, 14))
              )
              AND p.metodoLeituraCodigoBarras in :allowedSources
              AND p.status = br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus.PUBLICADO
              AND (p.publicadoEm IS NULL OR p.publicadoEm <= CURRENT_TIMESTAMP)
              AND (p.despublicadoEm IS NULL OR p.despublicadoEm > CURRENT_TIMESTAMP)
              AND (
                :q IS NULL
                OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.nome)      LIKE LOWER(CONCAT('%', :q, '%'))
                OR p.codigoBarras     LIKE CONCAT('%', :q, '%')
                OR str(p.codigoOriginal) LIKE CONCAT('%', :q, '%')
              )
            ORDER BY p.dataCadastro DESC, p.id DESC
            """)
    Page<ProdutoEntity> searchPublicPage(@Param("q") String q,
                                         @Param("allowedSources")
                                         Collection<MetodoLeituraCodigoBarras> allowedSources,
                                         Pageable pageable);

    @Query("""
            SELECT p FROM ProdutoEntity p
            WHERE p.tenantId = :tenantId
              AND p.disponivel = true AND p.estoque > 0 AND p.precoVenda > 0
              AND (
                (p.codigoBarras IS NOT NULL AND TRIM(p.codigoBarras) <> '')
                OR (p.codigoOriginal IS NOT NULL AND LENGTH(str(p.codigoOriginal)) IN (7, 8, 11, 12, 13, 14))
              )
              AND p.metodoLeituraCodigoBarras in :allowedSources
              AND p.status = br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus.PUBLICADO
              AND (p.publicadoEm IS NULL OR p.publicadoEm <= CURRENT_TIMESTAMP)
              AND (p.despublicadoEm IS NULL OR p.despublicadoEm > CURRENT_TIMESTAMP)
              AND (
                :q IS NULL
                OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.nome)      LIKE LOWER(CONCAT('%', :q, '%'))
                OR p.codigoBarras     LIKE CONCAT('%', :q, '%')
                OR str(p.codigoOriginal) LIKE CONCAT('%', :q, '%')
              )
            ORDER BY p.dataCadastro DESC, p.id DESC
            """)
    Page<ProdutoEntity> searchPublicPage(@Param("tenantId") Long tenantId,
                                         @Param("q") String q,
                                         @Param("allowedSources")
                                         Collection<MetodoLeituraCodigoBarras> allowedSources,
                                         Pageable pageable);

    @Query("""
            SELECT COUNT(p) FROM ProdutoEntity p
            WHERE p.disponivel = true
              AND p.estoque > 0
              AND p.precoVenda > 0
              AND (
                (p.codigoBarras IS NOT NULL AND TRIM(p.codigoBarras) <> '')
                OR (p.codigoOriginal IS NOT NULL AND LENGTH(str(p.codigoOriginal)) IN (7, 8, 11, 12, 13, 14))
              )
              AND p.metodoLeituraCodigoBarras in :allowedSources
              AND p.status = br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus.PUBLICADO
              AND (p.publicadoEm IS NULL OR p.publicadoEm <= CURRENT_TIMESTAMP)
              AND (p.despublicadoEm IS NULL OR p.despublicadoEm > CURRENT_TIMESTAMP)
            """)
    long countPubliclySellable(@Param("allowedSources")
                               Collection<MetodoLeituraCodigoBarras> allowedSources);

    @Query("""
            SELECT COUNT(p) FROM ProdutoEntity p
            WHERE p.tenantId = :tenantId
              AND p.disponivel = true
              AND p.estoque > 0
              AND p.precoVenda > 0
              AND (
                (p.codigoBarras IS NOT NULL AND TRIM(p.codigoBarras) <> '')
                OR (p.codigoOriginal IS NOT NULL AND LENGTH(str(p.codigoOriginal)) IN (7, 8, 11, 12, 13, 14))
              )
              AND p.metodoLeituraCodigoBarras in :allowedSources
              AND p.status = br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus.PUBLICADO
              AND (p.publicadoEm IS NULL OR p.publicadoEm <= CURRENT_TIMESTAMP)
              AND (p.despublicadoEm IS NULL OR p.despublicadoEm > CURRENT_TIMESTAMP)
            """)
    long countPubliclySellable(@Param("tenantId") Long tenantId,
                               @Param("allowedSources")
                               Collection<MetodoLeituraCodigoBarras> allowedSources);

    @Query("""
            SELECT p FROM ProdutoEntity p
            WHERE p.id = :id
              AND p.disponivel = true
              AND p.estoque > 0
              AND p.precoVenda > 0
              AND (
                (p.codigoBarras IS NOT NULL AND TRIM(p.codigoBarras) <> '')
                OR (p.codigoOriginal IS NOT NULL AND LENGTH(str(p.codigoOriginal)) IN (7, 8, 11, 12, 13, 14))
              )
              AND p.metodoLeituraCodigoBarras in :allowedSources
              AND p.status = br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus.PUBLICADO
              AND (p.publicadoEm IS NULL OR p.publicadoEm <= CURRENT_TIMESTAMP)
              AND (p.despublicadoEm IS NULL OR p.despublicadoEm > CURRENT_TIMESTAMP)
            """)
    Optional<ProdutoEntity> findPublicById(@Param("id") Long id,
                                           @Param("allowedSources")
                                           Collection<MetodoLeituraCodigoBarras> allowedSources);

    @Query("""
            SELECT p FROM ProdutoEntity p
            WHERE p.tenantId = :tenantId
              AND p.id = :id
              AND p.disponivel = true
              AND p.estoque > 0
              AND p.precoVenda > 0
              AND (
                (p.codigoBarras IS NOT NULL AND TRIM(p.codigoBarras) <> '')
                OR (p.codigoOriginal IS NOT NULL AND LENGTH(str(p.codigoOriginal)) IN (7, 8, 11, 12, 13, 14))
              )
              AND p.metodoLeituraCodigoBarras in :allowedSources
              AND p.status = br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus.PUBLICADO
              AND (p.publicadoEm IS NULL OR p.publicadoEm <= CURRENT_TIMESTAMP)
              AND (p.despublicadoEm IS NULL OR p.despublicadoEm > CURRENT_TIMESTAMP)
            """)
    Optional<ProdutoEntity> findPublicById(@Param("tenantId") Long tenantId,
                                           @Param("id") Long id,
                                           @Param("allowedSources")
                                           Collection<MetodoLeituraCodigoBarras> allowedSources);

    @Query("""
            SELECT p FROM ProdutoEntity p
            WHERE p.id = :id
              AND p.disponivel = true
              AND p.precoVenda > 0
              AND (
                (p.codigoBarras IS NOT NULL AND TRIM(p.codigoBarras) <> '')
                OR (p.codigoOriginal IS NOT NULL AND LENGTH(str(p.codigoOriginal)) IN (7, 8, 11, 12, 13, 14))
              )
              AND p.metodoLeituraCodigoBarras in :allowedSources
              AND p.status = br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus.PUBLICADO
              AND (p.publicadoEm IS NULL OR p.publicadoEm <= CURRENT_TIMESTAMP)
              AND (p.despublicadoEm IS NULL OR p.despublicadoEm > CURRENT_TIMESTAMP)
            """)
    Optional<ProdutoEntity> findStockSubscribableById(
            @Param("id") Long id,
            @Param("allowedSources")
            Collection<MetodoLeituraCodigoBarras> allowedSources
    );

    @Query("""
            SELECT p FROM ProdutoEntity p
            WHERE p.disponivel = true AND p.estoque > 0 AND p.precoVenda > 0
              AND (
                (p.codigoBarras IS NOT NULL AND TRIM(p.codigoBarras) <> '')
                OR (p.codigoOriginal IS NOT NULL AND LENGTH(str(p.codigoOriginal)) IN (7, 8, 11, 12, 13, 14))
              )
              AND p.metodoLeituraCodigoBarras in :allowedSources
              AND p.status = br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus.PUBLICADO
              AND (p.publicadoEm IS NULL OR p.publicadoEm <= CURRENT_TIMESTAMP)
              AND (p.despublicadoEm IS NULL OR p.despublicadoEm > CURRENT_TIMESTAMP)
              AND (:cat IS NULL OR LOWER(TRIM(p.categoria)) = LOWER(TRIM(:cat)))
              AND (
                :q IS NULL
                OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.nome)      LIKE LOWER(CONCAT('%', :q, '%'))
                OR p.codigoBarras     LIKE CONCAT('%', :q, '%')
                OR str(p.codigoOriginal) LIKE CONCAT('%', :q, '%')
              )
            ORDER BY p.dataCadastro DESC, p.id DESC
            """)
    Page<ProdutoEntity> searchPublicPageByCategoria(@Param("q") String q,
                                                    @Param("cat") String categoria,
                                                    @Param("allowedSources")
                                                    Collection<MetodoLeituraCodigoBarras> allowedSources,
                                                    Pageable pageable);

    @Query("""
            SELECT p FROM ProdutoEntity p
            WHERE p.tenantId = :tenantId
              AND p.disponivel = true AND p.estoque > 0 AND p.precoVenda > 0
              AND (
                (p.codigoBarras IS NOT NULL AND TRIM(p.codigoBarras) <> '')
                OR (p.codigoOriginal IS NOT NULL AND LENGTH(str(p.codigoOriginal)) IN (7, 8, 11, 12, 13, 14))
              )
              AND p.metodoLeituraCodigoBarras in :allowedSources
              AND p.status = br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus.PUBLICADO
              AND (p.publicadoEm IS NULL OR p.publicadoEm <= CURRENT_TIMESTAMP)
              AND (p.despublicadoEm IS NULL OR p.despublicadoEm > CURRENT_TIMESTAMP)
              AND (:cat IS NULL OR LOWER(TRIM(p.categoria)) = LOWER(TRIM(:cat)))
              AND (
                :q IS NULL
                OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.nome)      LIKE LOWER(CONCAT('%', :q, '%'))
                OR p.codigoBarras     LIKE CONCAT('%', :q, '%')
                OR str(p.codigoOriginal) LIKE CONCAT('%', :q, '%')
              )
            ORDER BY p.dataCadastro DESC, p.id DESC
            """)
    Page<ProdutoEntity> searchPublicPageByCategoria(@Param("tenantId") Long tenantId,
                                                    @Param("q") String q,
                                                    @Param("cat") String categoria,
                                                    @Param("allowedSources")
                                                    Collection<MetodoLeituraCodigoBarras> allowedSources,
                                                    Pageable pageable);

    @Query("""
            SELECT p FROM ProdutoEntity p
            WHERE LOWER(TRIM(p.categoria)) = LOWER(TRIM(:cat))
              AND (
                p.disponivel IS NULL
                OR p.disponivel = false
                OR p.estoque IS NULL
                OR p.estoque <= 0
                OR p.precoVenda IS NULL
                OR p.precoVenda <= 0
                OR (
                  (p.codigoBarras IS NULL OR TRIM(p.codigoBarras) = '')
                  AND (p.codigoOriginal IS NULL OR LENGTH(str(p.codigoOriginal)) NOT IN (7, 8, 11, 12, 13, 14))
                )
                OR p.imagem IS NULL
                OR TRIM(p.imagem) = ''
                OR p.status IS NULL
                OR p.status <> br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus.PUBLICADO
                OR p.publicadoEm IS NULL
                OR p.publicadoEm > CURRENT_TIMESTAMP
                OR (p.despublicadoEm IS NOT NULL AND p.despublicadoEm <= CURRENT_TIMESTAMP)
              )
              AND (
                :q IS NULL
                OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.nome)      LIKE LOWER(CONCAT('%', :q, '%'))
                OR p.codigoBarras     LIKE CONCAT('%', :q, '%')
                OR str(p.codigoOriginal) LIKE CONCAT('%', :q, '%')
                OR CONCAT('', p.legacyId) LIKE CONCAT('%', :q, '%')
              )
            ORDER BY p.id ASC
            """)
    Page<ProdutoEntity> searchNaoDisponiveisByCategoria(@Param("q") String q,
                                                        @Param("cat") String categoria,
                                                        Pageable pageable);

    @Query("""
            SELECT p FROM ProdutoEntity p
            WHERE (
                p.disponivel IS NULL
                OR p.disponivel = false
                OR p.estoque IS NULL
                OR p.estoque <= 0
                OR p.precoVenda IS NULL
                OR p.precoVenda <= 0
                OR (
                  (p.codigoBarras IS NULL OR TRIM(p.codigoBarras) = '')
                  AND (p.codigoOriginal IS NULL OR LENGTH(str(p.codigoOriginal)) NOT IN (7, 8, 11, 12, 13, 14))
                )
                OR p.imagem IS NULL
                OR TRIM(p.imagem) = ''
                OR p.status IS NULL
                OR p.status <> br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus.PUBLICADO
                OR p.publicadoEm IS NULL
                OR p.publicadoEm > CURRENT_TIMESTAMP
                OR (p.despublicadoEm IS NOT NULL AND p.despublicadoEm <= CURRENT_TIMESTAMP)
              )
              AND (
                :q IS NULL
                OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.nome)      LIKE LOWER(CONCAT('%', :q, '%'))
                OR p.codigoBarras     LIKE CONCAT('%', :q, '%')
                OR str(p.codigoOriginal) LIKE CONCAT('%', :q, '%')
                OR CONCAT('', p.legacyId) LIKE CONCAT('%', :q, '%')
              )
            ORDER BY p.id ASC
            """)
    Page<ProdutoEntity> searchNaoDisponiveis(@Param("q") String q, Pageable pageable);

    /* ===================== DEFAULT METHODS (tipadas) ===================== */

    default Optional<ProdutoEntity> findByAnyCodigo(String codigo) {
        String normalized = BarcodeNormalizer.normalizeOrNull(codigo);
        if (normalized == null) {
            return Optional.empty();
        }
        Optional<ProdutoEntity> exact = this.findAllByAnyCodigo(normalized).stream().findFirst();
        if (exact.isPresent()) {
            return exact;
        }
        String stripped = normalized;
        while (stripped.length() > 1 && stripped.startsWith("0")) {
            stripped = stripped.substring(1);
            Optional<ProdutoEntity> candidate = this.findAllByAnyCodigo(stripped).stream().findFirst();
            if (candidate.isPresent()) {
                return candidate;
            }
        }
        return Optional.empty();
    }

    default Optional<ProdutoEntity> findByAnyCodigo(Long tenantId, String codigo) {
        if (tenantId == null) {
            return Optional.empty();
        }
        String normalized = BarcodeNormalizer.normalizeOrNull(codigo);
        if (normalized == null) {
            return Optional.empty();
        }
        Optional<ProdutoEntity> exact = this.findAllByTenantAndAnyCodigo(tenantId, normalized).stream().findFirst();
        if (exact.isPresent()) {
            return exact;
        }
        String stripped = normalized;
        while (stripped.length() > 1 && stripped.startsWith("0")) {
            stripped = stripped.substring(1);
            Optional<ProdutoEntity> candidate = this.findAllByTenantAndAnyCodigo(tenantId, stripped).stream().findFirst();
            if (candidate.isPresent()) {
                return candidate;
            }
        }
        return Optional.empty();
    }

    default boolean existsByAnyCodigo(String codigo) {
        return this.findByAnyCodigo(codigo).isPresent();
    }

    default boolean existsByAnyCodigo(Long tenantId, String codigo) {
        if (tenantId == null) {
            return this.existsByAnyCodigo(codigo);
        }
        return this.findByAnyCodigo(tenantId, codigo).isPresent();
    }

    default boolean existsByLegacyId(Long tenantId, Long legacyId) {
        if (legacyId == null) {
            return false;
        }
        if (tenantId == null) {
            return this.existsByLegacyId(legacyId);
        }
        return this.findByTenantIdAndLegacyId(tenantId, legacyId).isPresent();
    }

    default Page<ProdutoEntity> searchPage(String q, Pageable pageable) {
        if (q == null || q.isBlank()) {
            return this.findAll(pageable);
        }
        return this.searchPageByCategoria(q, null, pageable);
    }

    default Page<ProdutoEntity> searchPage(Long tenantId, String q, Pageable pageable) {
        if (tenantId == null) {
            return this.searchPage(q, pageable);
        }
        if (q == null || q.isBlank()) {
            return this.findByTenantId(tenantId, pageable);
        }
        return this.searchPageByCategoria(tenantId, q, null, pageable);
    }

    default List<ProdutoEntity> searchForExport(String q, int limit) {
        PageRequest pageReq = PageRequest.of(0, Math.max(limit, 1), Sort.by("id").ascending());
        if (q == null || q.isBlank()) {
            return this.findAll(pageReq).getContent();
        }
        return this.searchPageByCategoria(q, null, pageReq).getContent();
    }

    default List<ProdutoEntity> searchForExportLimited(String q, String categoria, int limit) {
        int pageSize = Math.max(1, Math.min(limit, 2000));
        PageRequest pageReq = PageRequest.of(0, pageSize, Sort.by("id").ascending());
        return this.searchPageByCategoria(q, categoria, pageReq).getContent();
    }

    default List<ProdutoEntity> findAllPublicByIdIn(Collection<Long> ids) {
        return this.findAllPublicByIdIn(null, ids);
    }

    default List<ProdutoEntity> findAllPublicByIdIn(Long tenantId, Collection<Long> ids) {
        if (tenantId == null) {
            return this.findAllPublicByIdIn(ids, PUBLIC_ALLOWED_SOURCES);
        }
        return this.findAllPublicByIdIn(tenantId, ids, PUBLIC_ALLOWED_SOURCES);
    }

    default List<String> findDistinctCategoriasPublicas() {
        return this.findDistinctCategoriasPublicas((Long) null);
    }

    default List<String> findDistinctCategoriasPublicas(Long tenantId) {
        if (tenantId == null) {
            return this.findDistinctCategoriasPublicas(PUBLIC_ALLOWED_SOURCES);
        }
        return this.findDistinctCategoriasPublicas(tenantId, PUBLIC_ALLOWED_SOURCES);
    }

    default List<ProdutoEntity> findCarrossel(Pageable pageable) {
        return this.findCarrossel((Long) null, pageable);
    }

    default List<ProdutoEntity> findCarrossel(Long tenantId, Pageable pageable) {
        if (tenantId == null) {
            return this.findCarrossel(PUBLIC_ALLOWED_SOURCES, pageable);
        }
        return this.findCarrossel(tenantId, PUBLIC_ALLOWED_SOURCES, pageable);
    }

    default List<ProdutoEntity> findVitrineFallback(Pageable pageable) {
        return this.findVitrineFallback((Long) null, pageable);
    }

    default List<ProdutoEntity> findVitrineFallback(Long tenantId, Pageable pageable) {
        if (tenantId == null) {
            return this.findVitrineFallback(PUBLIC_ALLOWED_SOURCES, pageable);
        }
        return this.findVitrineFallback(tenantId, PUBLIC_ALLOWED_SOURCES, pageable);
    }

    default Page<ProdutoEntity> searchPublicPage(String q, Pageable pageable) {
        return this.searchPublicPage(null, q, pageable);
    }

    default Page<ProdutoEntity> searchPublicPage(Long tenantId, String q, Pageable pageable) {
        if (tenantId == null) {
            return this.searchPublicPage(q, PUBLIC_ALLOWED_SOURCES, pageable);
        }
        return this.searchPublicPage(tenantId, q, PUBLIC_ALLOWED_SOURCES, pageable);
    }

    default long countPubliclySellable() {
        return this.countPubliclySellable(PUBLIC_ALLOWED_SOURCES);
    }

    default long countPubliclySellable(Long tenantId) {
        if (tenantId == null) {
            return this.countPubliclySellable();
        }
        return this.countPubliclySellable(tenantId, PUBLIC_ALLOWED_SOURCES);
    }

    default List<ProdutoEntity> findComEstoqueBaixoScoped(Long tenantId, Integer limite) {
        if (tenantId == null) {
            return this.findComEstoqueBaixo(limite);
        }
        return this.findComEstoqueBaixo(tenantId, limite);
    }

    default Page<ProdutoEntity> findPaginaNiveisEstoqueScoped(Long tenantId, Integer limite, Pageable pageable) {
        if (tenantId == null) {
            return this.findPaginaNiveisEstoque(limite, pageable);
        }
        return this.findPaginaNiveisEstoque(tenantId, limite, pageable);
    }

    default Optional<ProdutoEntity> findPublicById(Long id) {
        return this.findPublicById(null, id);
    }

    default Optional<ProdutoEntity> findPublicById(Long tenantId, Long id) {
        if (tenantId == null) {
            return this.findPublicById(id, PUBLIC_ALLOWED_SOURCES);
        }
        return this.findPublicById(tenantId, id, PUBLIC_ALLOWED_SOURCES);
    }

    default Optional<ProdutoEntity> findStockSubscribableById(Long id) {
        return this.findStockSubscribableById(id, PUBLIC_ALLOWED_SOURCES);
    }

    default Page<ProdutoEntity> searchPublicPageByCategoria(String q,
                                                            String categoria,
                                                            Pageable pageable) {
        return this.searchPublicPageByCategoria(null, q, categoria, pageable);
    }

    default Page<ProdutoEntity> searchPublicPageByCategoria(Long tenantId,
                                                            String q,
                                                            String categoria,
                                                            Pageable pageable) {
        if (tenantId == null) {
            return this.searchPublicPageByCategoria(q, categoria, PUBLIC_ALLOWED_SOURCES, pageable);
        }
        return this.searchPublicPageByCategoria(tenantId, q, categoria, PUBLIC_ALLOWED_SOURCES, pageable);
    }

    default Optional<ProdutoEntity> findByScopedId(Long tenantId, Long id) {
        if (tenantId == null) {
            return this.findById(id);
        }
        return this.findByTenantIdAndId(tenantId, id);
    }
}
