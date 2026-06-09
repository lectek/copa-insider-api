/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.Pageable
 *  org.springframework.data.jpa.repository.JpaRepository
 *  org.springframework.data.jpa.repository.Query
 */
package br.com.lectek.copainsider.adapters.outbound.persistence.jpa;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.MetodoLeituraCodigoBarras;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.lectek.copainsider.domain.support.BarcodeNormalizer;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdutoJpaRepository extends JpaRepository<ProdutoEntity, Long> {

    List<MetodoLeituraCodigoBarras> PUBLIC_ALLOWED_SOURCES = List.of(
            MetodoLeituraCodigoBarras.MANUAL,
            MetodoLeituraCodigoBarras.SCANNER,
            MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA,
            MetodoLeituraCodigoBarras.API
    );

    Optional<ProdutoEntity> findByCodigoBarras(String var1);

    Optional<ProdutoEntity> findByTenantIdAndId(Long tenantId, Long id);

    Page<ProdutoEntity> findByTenantId(Long tenantId, Pageable pageable);

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
    List<ProdutoEntity> findAllByTenantAndAnyCodigo(@Param("tenantId") Long tenantId,
                                                    @Param("codigo") String codigo);

    Optional<ProdutoEntity> findByLegacyId(Long var1);

    Optional<ProdutoEntity> findByTenantIdAndLegacyId(Long tenantId, Long legacyId);

    List<ProdutoEntity> findByCodigoBarrasIn(Collection<String> var1);

    List<ProdutoEntity> findByLegacyIdIn(Collection<Long> var1);

    Optional<ProdutoEntity> findByNomeIgnoreCase(String var1);

    Page<ProdutoEntity> findByDescricaoContainingIgnoreCaseOrCodigoBarrasContainingIgnoreCase(String var1, String var2, Pageable var3);

    Page<ProdutoEntity> findByDisponivelTrue(Pageable var1);

    @Query("SELECT p FROM ProdutoEntity p WHERE p.imagem IS NULL OR TRIM(p.imagem) = ''")
    Page<ProdutoEntity> findSemMidia(Pageable var1);

    @Query("""
           SELECT p FROM ProdutoEntity p
            WHERE p.tenantId = :tenantId
              AND (p.imagem IS NULL OR TRIM(p.imagem) = '')
              AND p.estoque > 0
              AND (
                :q IS NULL
                OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.categoria) LIKE LOWER(CONCAT('%', :q, '%'))
                OR p.codigoBarras LIKE CONCAT('%', :q, '%')
                OR str(p.codigoOriginal) LIKE CONCAT('%', :q, '%')
              )
            ORDER BY p.estoque DESC, p.nome ASC, p.id ASC
           """)
    Page<ProdutoEntity> searchSemMidiaComEstoque(@Param("tenantId") Long tenantId,
                                                  @Param("q") String q,
                                                  Pageable pageable);

    @Query("""
           SELECT p FROM ProdutoEntity p
            WHERE (p.imagem IS NULL OR TRIM(p.imagem) = '')
              AND p.estoque > 0
              AND (
                :q IS NULL
                OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.categoria) LIKE LOWER(CONCAT('%', :q, '%'))
                OR p.codigoBarras LIKE CONCAT('%', :q, '%')
                OR str(p.codigoOriginal) LIKE CONCAT('%', :q, '%')
              )
            ORDER BY p.estoque DESC, p.nome ASC, p.id ASC
           """)
    Page<ProdutoEntity> searchSemMidiaComEstoque(@Param("q") String q, Pageable pageable);

    @Query("""
           SELECT COUNT(p) FROM ProdutoEntity p
            WHERE (p.imagem IS NULL OR TRIM(p.imagem) = '')
              AND p.estoque > 0
              AND (
                :q IS NULL
                OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.categoria) LIKE LOWER(CONCAT('%', :q, '%'))
                OR p.codigoBarras LIKE CONCAT('%', :q, '%')
                OR str(p.codigoOriginal) LIKE CONCAT('%', :q, '%')
              )
           """)
    long countSemMidiaComEstoque(@Param("q") String q);

    @Query("""
           SELECT COUNT(p) FROM ProdutoEntity p
            WHERE p.tenantId = :tenantId
              AND (p.imagem IS NULL OR TRIM(p.imagem) = '')
              AND p.estoque > 0
              AND (
                :q IS NULL
                OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.categoria) LIKE LOWER(CONCAT('%', :q, '%'))
                OR p.codigoBarras LIKE CONCAT('%', :q, '%')
                OR str(p.codigoOriginal) LIKE CONCAT('%', :q, '%')
              )
           """)
    long countSemMidiaComEstoque(@Param("tenantId") Long tenantId,
                                 @Param("q") String q);

    @Query("""
           SELECT p FROM ProdutoEntity p
            WHERE p.tenantId = :tenantId
              AND (p.imagem IS NULL OR TRIM(p.imagem) = '')
              AND (p.estoque IS NULL OR p.estoque <= 0)
              AND (
                :q IS NULL
                OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.categoria) LIKE LOWER(CONCAT('%', :q, '%'))
                OR p.codigoBarras LIKE CONCAT('%', :q, '%')
                OR str(p.codigoOriginal) LIKE CONCAT('%', :q, '%')
              )
            ORDER BY p.nome ASC, p.id ASC
           """)
    Page<ProdutoEntity> searchSemMidiaSemEstoque(@Param("tenantId") Long tenantId,
                                                  @Param("q") String q,
                                                  Pageable pageable);

    @Query("""
           SELECT p FROM ProdutoEntity p
            WHERE (p.imagem IS NULL OR TRIM(p.imagem) = '')
              AND (p.estoque IS NULL OR p.estoque <= 0)
              AND (
                :q IS NULL
                OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.categoria) LIKE LOWER(CONCAT('%', :q, '%'))
                OR p.codigoBarras LIKE CONCAT('%', :q, '%')
                OR str(p.codigoOriginal) LIKE CONCAT('%', :q, '%')
              )
            ORDER BY p.nome ASC, p.id ASC
           """)
    Page<ProdutoEntity> searchSemMidiaSemEstoque(@Param("q") String q, Pageable pageable);

    @Query("""
           SELECT COUNT(p) FROM ProdutoEntity p
            WHERE (p.imagem IS NULL OR TRIM(p.imagem) = '')
              AND (p.estoque IS NULL OR p.estoque <= 0)
              AND (
                :q IS NULL
                OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.categoria) LIKE LOWER(CONCAT('%', :q, '%'))
                OR p.codigoBarras LIKE CONCAT('%', :q, '%')
                OR str(p.codigoOriginal) LIKE CONCAT('%', :q, '%')
              )
           """)
    long countSemMidiaSemEstoque(@Param("q") String q);

    @Query("""
           SELECT COUNT(p) FROM ProdutoEntity p
            WHERE p.tenantId = :tenantId
              AND (p.imagem IS NULL OR TRIM(p.imagem) = '')
              AND (p.estoque IS NULL OR p.estoque <= 0)
              AND (
                :q IS NULL
                OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.categoria) LIKE LOWER(CONCAT('%', :q, '%'))
                OR p.codigoBarras LIKE CONCAT('%', :q, '%')
                OR str(p.codigoOriginal) LIKE CONCAT('%', :q, '%')
              )
           """)
    long countSemMidiaSemEstoque(@Param("tenantId") Long tenantId,
                                 @Param("q") String q);

    Page<ProdutoEntity> findByStatus(ProdutoStatus var1, Pageable var2);

    Page<ProdutoEntity> findByStatusOrderByDataImportacaoAsc(ProdutoStatus var1, Pageable var2);

    @Query("""
           SELECT p FROM ProdutoEntity p
            WHERE p.disponivel = true
              AND p.tenantId = :tenantId
              AND p.estoque > 0
              AND p.precoVenda > 0
              AND (
                (p.codigoBarras IS NOT NULL AND TRIM(p.codigoBarras) <> '')
                OR (p.codigoOriginal IS NOT NULL AND LENGTH(str(p.codigoOriginal)) IN (7, 8, 11, 12, 13, 14))
              )
              AND p.metodoLeituraCodigoBarras in :allowedSources
              AND p.status = br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoStatus.PUBLICADO
              AND p.destaqueCarrossel = true
              AND (p.publicadoEm IS NULL OR p.publicadoEm <= CURRENT_TIMESTAMP)
              AND (p.despublicadoEm IS NULL OR p.despublicadoEm > CURRENT_TIMESTAMP)
            ORDER BY COALESCE(p.ordemCarrossel, 9999) ASC, p.dataCadastro DESC, p.id DESC
           """)
    List<ProdutoEntity> findCarrossel(@Param("tenantId") Long tenantId,
                                      @Param("allowedSources") Collection<MetodoLeituraCodigoBarras> allowedSources,
                                      Pageable var1);

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
              AND p.status = br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoStatus.PUBLICADO
              AND p.destaqueCarrossel = true
              AND (p.publicadoEm IS NULL OR p.publicadoEm <= CURRENT_TIMESTAMP)
              AND (p.despublicadoEm IS NULL OR p.despublicadoEm > CURRENT_TIMESTAMP)
            ORDER BY COALESCE(p.ordemCarrossel, 9999) ASC, p.dataCadastro DESC, p.id DESC
           """)
    List<ProdutoEntity> findCarrossel(@Param("allowedSources") Collection<MetodoLeituraCodigoBarras> allowedSources,
                                      Pageable var1);

    @Query("""
           SELECT p FROM ProdutoEntity p
            WHERE p.disponivel = true
              AND p.tenantId = :tenantId
              AND p.estoque > 0
              AND p.precoVenda > 0
              AND (
                (p.codigoBarras IS NOT NULL AND TRIM(p.codigoBarras) <> '')
                OR (p.codigoOriginal IS NOT NULL AND LENGTH(str(p.codigoOriginal)) IN (7, 8, 11, 12, 13, 14))
              )
              AND p.metodoLeituraCodigoBarras in :allowedSources
              AND p.status = br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoStatus.PUBLICADO
              AND (p.publicadoEm IS NULL OR p.publicadoEm <= CURRENT_TIMESTAMP)
              AND (p.despublicadoEm IS NULL OR p.despublicadoEm > CURRENT_TIMESTAMP)
            ORDER BY p.dataCadastro DESC, p.id DESC
           """)
    List<ProdutoEntity> findVitrineFallback(
            @Param("tenantId") Long tenantId,
            @Param("allowedSources") Collection<MetodoLeituraCodigoBarras> allowedSources,
            Pageable var1
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
              AND p.status = br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoStatus.PUBLICADO
              AND (p.publicadoEm IS NULL OR p.publicadoEm <= CURRENT_TIMESTAMP)
              AND (p.despublicadoEm IS NULL OR p.despublicadoEm > CURRENT_TIMESTAMP)
            ORDER BY p.dataCadastro DESC, p.id DESC
           """)
    List<ProdutoEntity> findVitrineFallback(
            @Param("allowedSources") Collection<MetodoLeituraCodigoBarras> allowedSources,
            Pageable var1
    );

    @Query("select p from ProdutoEntity p order by p.dataCadastro desc")
    List<ProdutoEntity> findRecent(Pageable var1);

    @Query("select p from ProdutoEntity p where p.tenantId = :tenantId order by p.dataCadastro desc")
    List<ProdutoEntity> findRecent(@Param("tenantId") Long tenantId, Pageable pageable);

    @Query("select p from ProdutoEntity p where p.disponivel = true order by p.dataCadastro desc")
    List<ProdutoEntity> findRecentDisponiveis(Pageable var1);

    @Query("select p from ProdutoEntity p where p.id in :ids")
    List<ProdutoEntity> findAllByIdIn(@Param("ids") Collection<Long> var1);

    @Query("select p from ProdutoEntity p where p.tenantId = :tenantId and p.id in :ids")
    List<ProdutoEntity> findAllByTenantIdIn(@Param("tenantId") Long tenantId,
                                            @Param("ids") Collection<Long> ids);

    @Query("select p from ProdutoEntity p where p.disponivel = true and p.id in :ids")
    List<ProdutoEntity> findAllDisponiveisByIdIn(@Param("ids") Collection<Long> var1);

    @Query("""
           select p from ProdutoEntity p
            where p.id in :ids
              and p.tenantId = :tenantId
              and p.disponivel = true
              and p.estoque > 0
              and p.precoVenda > 0
              and (
                (p.codigoBarras is not null and trim(p.codigoBarras) <> '')
                or (p.codigoOriginal is not null and length(str(p.codigoOriginal)) in (7, 8, 11, 12, 13, 14))
              )
              and p.metodoLeituraCodigoBarras in :allowedSources
              and p.status = br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoStatus.PUBLICADO
              and (p.publicadoEm is null or p.publicadoEm <= CURRENT_TIMESTAMP)
              and (p.despublicadoEm is null or p.despublicadoEm > CURRENT_TIMESTAMP)
           """)
    List<ProdutoEntity> findAllPublicByIdIn(@Param("tenantId") Long tenantId,
                                            @Param("ids") Collection<Long> var1,
                                            @Param("allowedSources")
                                            Collection<MetodoLeituraCodigoBarras> allowedSources);

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
              and p.status = br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoStatus.PUBLICADO
              and (p.publicadoEm is null or p.publicadoEm <= CURRENT_TIMESTAMP)
              and (p.despublicadoEm is null or p.despublicadoEm > CURRENT_TIMESTAMP)
           """)
    List<ProdutoEntity> findAllPublicByIdIn(@Param("ids") Collection<Long> var1,
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
              and p.tenantId = :tenantId
              and trim(p.categoria) <> ''
              and p.disponivel = true
              and p.estoque > 0
              and p.precoVenda > 0
              and (
                (p.codigoBarras is not null and trim(p.codigoBarras) <> '')
                or (p.codigoOriginal is not null and length(str(p.codigoOriginal)) in (7, 8, 11, 12, 13, 14))
              )
              and p.metodoLeituraCodigoBarras in :allowedSources
              and p.status = br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoStatus.PUBLICADO
              and (p.publicadoEm is null or p.publicadoEm <= CURRENT_TIMESTAMP)
              and (p.despublicadoEm is null or p.despublicadoEm > CURRENT_TIMESTAMP)
            order by trim(p.categoria) asc
           """)
    List<String> findDistinctCategoriasPublicas(
            @Param("tenantId") Long tenantId,
            @Param("allowedSources") Collection<MetodoLeituraCodigoBarras> allowedSources
    );

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
              and p.status = br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoStatus.PUBLICADO
              and (p.publicadoEm is null or p.publicadoEm <= CURRENT_TIMESTAMP)
              and (p.despublicadoEm is null or p.despublicadoEm > CURRENT_TIMESTAMP)
            order by trim(p.categoria) asc
           """)
    List<String> findDistinctCategoriasPublicas(
            @Param("allowedSources") Collection<MetodoLeituraCodigoBarras> allowedSources
    );

    @Query("""
           SELECT p FROM ProdutoEntity p
            WHERE p.disponivel = true
              AND p.tenantId = :tenantId
              AND p.estoque > 0
              AND p.precoVenda > 0
              AND (
                (p.codigoBarras IS NOT NULL AND TRIM(p.codigoBarras) <> '')
                OR (p.codigoOriginal IS NOT NULL AND LENGTH(str(p.codigoOriginal)) IN (7, 8, 11, 12, 13, 14))
              )
              AND p.metodoLeituraCodigoBarras in :allowedSources
              AND p.status = br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoStatus.PUBLICADO
              AND (p.publicadoEm IS NULL OR p.publicadoEm <= CURRENT_TIMESTAMP)
              AND (p.despublicadoEm IS NULL OR p.despublicadoEm > CURRENT_TIMESTAMP)
              AND (
                :q IS NULL
                OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                OR p.codigoBarras LIKE CONCAT('%', :q, '%')
                OR str(p.codigoOriginal) LIKE CONCAT('%', :q, '%')
              )
           """)
    Page<ProdutoEntity> searchPublicPage(@Param("tenantId") Long tenantId,
                                         @Param("q") String q,
                                         @Param("allowedSources")
                                         Collection<MetodoLeituraCodigoBarras> allowedSources,
                                         Pageable pageable);

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
              AND p.status = br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoStatus.PUBLICADO
              AND (p.publicadoEm IS NULL OR p.publicadoEm <= CURRENT_TIMESTAMP)
              AND (p.despublicadoEm IS NULL OR p.despublicadoEm > CURRENT_TIMESTAMP)
              AND (
                :q IS NULL
                OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                OR p.codigoBarras LIKE CONCAT('%', :q, '%')
                OR str(p.codigoOriginal) LIKE CONCAT('%', :q, '%')
              )
           """)
    Page<ProdutoEntity> searchPublicPage(@Param("q") String q,
                                         @Param("allowedSources")
                                         Collection<MetodoLeituraCodigoBarras> allowedSources,
                                         Pageable pageable);

    @Query("""
           SELECT p FROM ProdutoEntity p
            WHERE p.tenantId = :tenantId
              AND (
                :q IS NULL
                OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :q, '%'))
                OR p.codigoBarras LIKE CONCAT('%', :q, '%')
                OR str(p.codigoOriginal) LIKE CONCAT('%', :q, '%')
              )
           """)
    Page<ProdutoEntity> searchAnyPage(@Param("tenantId") Long tenantId,
                                      @Param("q") String q,
                                      Pageable pageable);

    @Query("""
           SELECT p FROM ProdutoEntity p
            WHERE :q IS NULL
               OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :q, '%'))
               OR p.codigoBarras LIKE CONCAT('%', :q, '%')
               OR str(p.codigoOriginal) LIKE CONCAT('%', :q, '%')
           """)
    Page<ProdutoEntity> searchAnyPage(@Param("q") String q, Pageable pageable);

    @Query("""
           SELECT p FROM ProdutoEntity p
            WHERE p.id = :id
              AND p.tenantId = :tenantId
              AND p.disponivel = true
              AND p.estoque > 0
              AND p.precoVenda > 0
              AND (
                (p.codigoBarras IS NOT NULL AND TRIM(p.codigoBarras) <> '')
                OR (p.codigoOriginal IS NOT NULL AND LENGTH(str(p.codigoOriginal)) IN (7, 8, 11, 12, 13, 14))
              )
              AND p.metodoLeituraCodigoBarras in :allowedSources
              AND p.status = br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoStatus.PUBLICADO
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
              AND p.estoque > 0
              AND p.precoVenda > 0
              AND (
                (p.codigoBarras IS NOT NULL AND TRIM(p.codigoBarras) <> '')
                OR (p.codigoOriginal IS NOT NULL AND LENGTH(str(p.codigoOriginal)) IN (7, 8, 11, 12, 13, 14))
              )
              AND p.metodoLeituraCodigoBarras in :allowedSources
              AND p.status = br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoStatus.PUBLICADO
              AND (p.publicadoEm IS NULL OR p.publicadoEm <= CURRENT_TIMESTAMP)
              AND (p.despublicadoEm IS NULL OR p.despublicadoEm > CURRENT_TIMESTAMP)
           """)
    Optional<ProdutoEntity> findPublicById(@Param("id") Long id,
                                           @Param("allowedSources")
                                           Collection<MetodoLeituraCodigoBarras> allowedSources);

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

    default Page<ProdutoEntity> searchPage(String q, Pageable pageable) {
        if (q == null || q.isBlank()) {
            return this.findAll(pageable);
        }
        return this.searchAnyPage(q, pageable);
    }

    default Page<ProdutoEntity> searchPage(Long tenantId, String q, Pageable pageable) {
        if (tenantId == null) {
            return this.searchPage(q, pageable);
        }
        if (q == null || q.isBlank()) {
            return this.findByTenantId(tenantId, pageable);
        }
        return this.searchAnyPage(tenantId, q, pageable);
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

    default Page<ProdutoEntity> searchPublicPage(String q, Pageable pageable) {
        return this.searchPublicPage(null, q, pageable);
    }

    default Page<ProdutoEntity> searchPublicPage(Long tenantId, String q, Pageable pageable) {
        if (tenantId == null) {
            return this.searchPublicPage(q, PUBLIC_ALLOWED_SOURCES, pageable);
        }
        return this.searchPublicPage(tenantId, q, PUBLIC_ALLOWED_SOURCES, pageable);
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

    default Optional<ProdutoEntity> findByScopedId(Long tenantId, Long id) {
        if (tenantId == null) {
            return this.findById(id);
        }
        return this.findByTenantIdAndId(tenantId, id);
    }
}
