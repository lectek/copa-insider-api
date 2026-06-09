package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.application.core.exception.ImportInProgressException;
import br.com.lectek.copainsider.application.core.tenant.TenantResolverService;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.MetodoLeituraCodigoBarras;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.lectek.copainsider.application.service.validation.ProductSourcePolicy;
import br.com.lectek.copainsider.domain.support.BarcodeNormalizer;
import br.com.lectek.copainsider.domain.support.ProdutoHashUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class EstoqueFisicoImportService {

    private static final int BATCH_SIZE = 400;
    private static final String IMPORT_LOCK_NAME = "copainsider:estoque-fisico-import";
    private static final int IMPORT_LOCK_TIMEOUT_SECONDS = 0;

    private final EstoqueFisicoCsvService estoqueFisicoCsvService;
    private final ProdutoRepository produtoRepository;
    private final ProductCategoryBindingService categoryBindingService;
    @Autowired(required = false)
    private TenantResolverService tenantResolverService;

    @PersistenceContext(unitName = "mysqlPU")
    private EntityManager em;

    @Transactional(transactionManager = "mysqlTransactionManager")
    public ImportacaoResumo importarTodosComoNaoDisponiveis() {
        this.acquireImportLock();
        try {
            List<EstoqueFisicoCsvService.EstoqueItem> itens = this.estoqueFisicoCsvService.search("");
            return this.importarItens(itens);
        } finally {
            this.releaseImportLock();
        }
    }

    @Transactional(transactionManager = "mysqlTransactionManager")
    public ImportacaoResumo importarTodosComoNaoDisponiveis(InputStream csvStream) throws IOException {
        this.acquireImportLock();
        try {
            List<EstoqueFisicoCsvService.EstoqueItem> itens = this.estoqueFisicoCsvService.parseUploadedCsv(csvStream);
            return this.importarItens(itens);
        } finally {
            this.releaseImportLock();
        }
    }

    private ImportacaoResumo importarItens(List<EstoqueFisicoCsvService.EstoqueItem> itens) {
        if (itens.isEmpty()) {
            return new ImportacaoResumo(0, 0, 0, 0, 0);
        }

        int lidos = 0;
        int inseridos = 0;
        int atualizados = 0;
        int ignorados = 0;
        int erros = 0;

        List<ProdutoEntity> lote = new ArrayList<>(BATCH_SIZE);
        Map<Long, ProdutoEntity> cacheLegacy = new LinkedHashMap<>();
        Map<String, ProdutoEntity> cacheBarcode = new LinkedHashMap<>();

        for (EstoqueFisicoCsvService.EstoqueItem item : itens) {
            lidos++;
            try {
                TargetResolution resolution = this.resolveProdutoExistente(item, cacheLegacy, cacheBarcode);
                if (resolution.skip()) {
                    ignorados++;
                    continue;
                }
                ProdutoEntity entity = resolution.entity();
                boolean novo = entity.getId() == null;
                Snapshot before = novo ? null : Snapshot.from(entity);

                String resolvedBarcode = this.resolveBarcodeForEntity(entity, item, cacheBarcode);
                this.applyCsvData(entity, item, resolvedBarcode);
                Snapshot after = Snapshot.from(entity);

                if (!novo && Objects.equals(before, after)) {
                    ignorados++;
                    continue;
                }

                lote.add(entity);
                if (novo) {
                    inseridos++;
                } else {
                    atualizados++;
                }

                if (lote.size() >= BATCH_SIZE) {
                    this.flushBatch(lote, cacheLegacy, cacheBarcode);
                }
            } catch (Exception ex) {
                erros++;
                log.warn("[estoque-import] falha no item legacyId={} codigo={} nome='{}': {}",
                        item.legacyId(), item.codigoBarras(), item.nome(), ex.getMessage());
            }
        }

        this.flushBatch(lote, cacheLegacy, cacheBarcode);
        return new ImportacaoResumo(lidos, inseridos, atualizados, ignorados, erros);
    }

    private TargetResolution resolveProdutoExistente(
            EstoqueFisicoCsvService.EstoqueItem item,
            Map<Long, ProdutoEntity> cacheLegacy,
            Map<String, ProdutoEntity> cacheBarcode
    ) {
        Long legacyId = item.legacyId();
        String barcode = BarcodeNormalizer.normalize(item.codigoBarras());
        Long tenantId = this.resolveTenantId();

        if (legacyId != null) {
            TargetResolution byLegacy = this.resolveByLegacyId(tenantId, legacyId, cacheLegacy, cacheBarcode);
            if (byLegacy != null) {
                return byLegacy;
            }
            ProdutoEntity entity = new ProdutoEntity();
            entity.setTenantId(tenantId);
            return TargetResolution.use(entity);
        }

        TargetResolution byBarcode = this.resolveByBarcode(tenantId, barcode, legacyId, cacheLegacy, cacheBarcode);
        if (byBarcode != null) {
            return byBarcode;
        }

        ProdutoEntity entity = new ProdutoEntity();
        entity.setTenantId(tenantId);
        return TargetResolution.use(entity);
    }

    private String resolveBarcodeForEntity(
            ProdutoEntity target,
            EstoqueFisicoCsvService.EstoqueItem item,
            Map<String, ProdutoEntity> cacheBarcode
    ) {
        Long tenantId = this.resolveTenantId();
        String barcode = BarcodeNormalizer.normalize(item.codigoBarras());
        if (barcode.isBlank()) {
            return "";
        }

        ProdutoEntity owner = cacheBarcode.get(barcode);
        if (owner == null) {
            owner = tenantId == null
                    ? this.produtoRepository.findByAnyCodigo(barcode).orElse(null)
                    : this.produtoRepository.findByAnyCodigo(tenantId, barcode).orElse(null);
            if (owner != null) {
                cacheBarcode.put(barcode, owner);
            }
        }

        if (owner == null) {
            return barcode;
        }
        if (target.getId() != null && owner.getId() != null && target.getId().equals(owner.getId())) {
            return barcode;
        }

        // Evita violar a constraint unique de codigo_barras quando o mesmo codigo ja pertence a outro produto.
        return "";
    }

    private TargetResolution resolveByLegacyId(
            Long tenantId,
            Long legacyId,
            Map<Long, ProdutoEntity> cacheLegacy,
            Map<String, ProdutoEntity> cacheBarcode
    ) {
        if (legacyId == null) {
            return null;
        }

        ProdutoEntity cached = cacheLegacy.get(legacyId);
        if (cached != null) {
            return TargetResolution.use(cached);
        }

        List<ProdutoEntity> foundList = tenantId == null
                ? this.produtoRepository.findAllByLegacyIdOrderByIdAsc(legacyId)
                : this.produtoRepository.findAllByTenantIdAndLegacyIdOrderByIdAsc(tenantId, legacyId);
        if (foundList.isEmpty()) {
            return null;
        }

        ProdutoEntity found = foundList.stream()
                .filter(ProductSourcePolicy::isManagedByStockImport)
                .findFirst()
                .orElse(null);
        if (found == null) {
            ProdutoEntity protectedOwner = foundList.stream()
                    .filter(ProductSourcePolicy::isProtectedFromStockImport)
                    .findFirst()
                    .orElse(null);
            if (protectedOwner != null) {
                log.warn(
                        "[estoque-import] item ignorado: legacyId={} pertence a produto protegido id={} origem={}",
                        legacyId,
                        protectedOwner.getId(),
                        protectedOwner.getMetodoLeituraCodigoBarras()
                );
                return TargetResolution.skipItem();
            }
            return null;
        }

        cacheLegacy.put(legacyId, found);
        String foundBarcode = BarcodeNormalizer.normalize(found.getCodigoBarras());
        if (!foundBarcode.isBlank()) {
            cacheBarcode.putIfAbsent(foundBarcode, found);
        }
        return TargetResolution.use(found);
    }

    private TargetResolution resolveByBarcode(
            Long tenantId,
            String barcode,
            Long legacyId,
            Map<Long, ProdutoEntity> cacheLegacy,
            Map<String, ProdutoEntity> cacheBarcode
    ) {
        if (barcode.isBlank()) {
            return null;
        }

        ProdutoEntity cached = cacheBarcode.get(barcode);
        if (cached != null) {
            return TargetResolution.use(cached);
        }

        ProdutoEntity found = tenantId == null
                ? this.produtoRepository.findByAnyCodigo(barcode).orElse(null)
                : this.produtoRepository.findByAnyCodigo(tenantId, barcode).orElse(null);
        if (found == null) {
            return null;
        }
        if (ProductSourcePolicy.isProtectedFromStockImport(found)) {
            log.warn(
                    "[estoque-import] item ignorado: codigoBarras={} pertence a produto protegido id={} origem={}",
                    barcode,
                    found.getId(),
                    found.getMetodoLeituraCodigoBarras()
            );
            return TargetResolution.skipItem();
        }

        cacheBarcode.put(barcode, found);
        if (legacyId != null) {
            cacheLegacy.putIfAbsent(legacyId, found);
        }
        return TargetResolution.use(found);
    }

    private void applyCsvData(ProdutoEntity entity, EstoqueFisicoCsvService.EstoqueItem item, String resolvedBarcode) {
        Long legacyId = item.legacyId();
        String nome = truncate(defaultText(item.nome(), "Produto do estoque fisico"), 255);
        String descricao = truncate(defaultText(item.nome(), "Produto do estoque fisico"), 1000);
        String fabricante = truncate(blankToNull(item.fabricante()), 128);

        BigDecimal precoVenda = this.resolvePrecoVenda(item);
        BigDecimal precoCusto = this.resolvePrecoCusto(item, precoVenda);
        Integer estoque = item.estoque() == null ? 0 : Math.max(0, item.estoque());

        entity.setLegacyId(legacyId);
        if (entity.getTenantId() == null) {
            entity.setTenantId(this.resolveTenantId());
        }
        entity.setCodigoBarras(resolvedBarcode.isBlank() ? null : truncate(resolvedBarcode, 64));
        if (resolvedBarcode.isBlank()) {
            entity.preserveCodigoOriginalBarcode(item.codigoBarras());
        }
        entity.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.CSV_ESTOQUE);
        entity.setNome(nome);
        entity.setDescricao(descricao);
        entity.setCategoria("Estoque fisico");
        entity.setFabricante(fabricante);
        entity.setEstoque(estoque);
        entity.setPrecoVenda(precoVenda);
        entity.setPrecoCusto(precoCusto);
        entity.setDisponivel(Boolean.FALSE);
        entity.setStatus(ProdutoStatus.IMPORTADO);
        entity.setPublicadoEm(null);
        entity.setDataImportacao(LocalDateTime.now());
        entity.setStatusSync("SINCRONIZADO");
        if (entity.getDataCadastro() == null) {
            entity.setDataCadastro(LocalDate.now());
        }

        String hash = ProdutoHashUtil.buildHash(
                entity.getCodigoBarras() == null ? "" : entity.getCodigoBarras(),
                entity.getNome() == null ? "" : entity.getNome(),
                entity.getDescricao() == null ? "" : entity.getDescricao(),
                entity.getPrecoVenda(),
                entity.getLegacyId()
        );
        entity.setHashLegado(hash);
    }

    private BigDecimal resolvePrecoVenda(EstoqueFisicoCsvService.EstoqueItem item) {
        BigDecimal venda = item.precoVenda();
        if (venda != null && venda.compareTo(BigDecimal.ZERO) > 0) {
            return venda.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal tabela = item.precoTabela();
        if (tabela != null && tabela.compareTo(BigDecimal.ZERO) > 0) {
            return tabela.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal resolvePrecoCusto(EstoqueFisicoCsvService.EstoqueItem item, BigDecimal fallback) {
        BigDecimal tabela = item.precoTabela();
        if (tabela != null && tabela.compareTo(BigDecimal.ZERO) >= 0) {
            return tabela.setScale(2, RoundingMode.HALF_UP);
        }
        if (fallback == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return fallback.setScale(2, RoundingMode.HALF_UP);
    }

    private void flushBatch(
            List<ProdutoEntity> lote,
            Map<Long, ProdutoEntity> cacheLegacy,
            Map<String, ProdutoEntity> cacheBarcode
    ) {
        if (lote.isEmpty()) {
            return;
        }
        this.categoryBindingService.bindAll(lote);
        List<ProdutoEntity> saved = this.produtoRepository.saveAll(lote);
        for (ProdutoEntity entity : saved) {
            if (entity.getLegacyId() != null) {
                cacheLegacy.put(entity.getLegacyId(), entity);
            }
            String barcode = BarcodeNormalizer.normalize(entity.getCodigoBarras());
            if (!barcode.isBlank()) {
                cacheBarcode.put(barcode, entity);
            }
        }
        lote.clear();
        this.em.flush();
        this.em.clear();
    }

    private void acquireImportLock() {
        Number result = (Number) this.em.createNativeQuery(
                        "SELECT GET_LOCK(:lockName, :timeoutSeconds)"
                )
                .setParameter("lockName", IMPORT_LOCK_NAME)
                .setParameter("timeoutSeconds", IMPORT_LOCK_TIMEOUT_SECONDS)
                .getSingleResult();
        if (result == null || result.intValue() != 1) {
            throw new ImportInProgressException("Ja existe uma importacao de estoque fisico em execucao.");
        }
    }

    private void releaseImportLock() {
        try {
            this.em.createNativeQuery("SELECT RELEASE_LOCK(:lockName)")
                    .setParameter("lockName", IMPORT_LOCK_NAME)
                    .getSingleResult();
        } catch (Exception ex) {
            log.warn("[estoque-import] falha ao liberar lock de importacao", ex);
        }
    }

    private static String defaultText(String value, String fallback) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isBlank() ? fallback : trimmed;
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private static String truncate(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLen ? value : value.substring(0, maxLen);
    }

    private record Snapshot(
            Long legacyId,
            String codigoBarras,
            String nome,
            String descricao,
            String categoria,
            String fabricante,
            Integer estoque,
            BigDecimal precoVenda,
            BigDecimal precoCusto,
            Boolean disponivel,
            ProdutoStatus status
    ) {
        static Snapshot from(ProdutoEntity p) {
            return new Snapshot(
                    p.getLegacyId(),
                    BarcodeNormalizer.normalizeOrNull(p.getCodigoBarras()),
                    p.getNome(),
                    p.getDescricao(),
                    p.getCategoria(),
                    p.getFabricante(),
                    p.getEstoque(),
                    p.getPrecoVenda(),
                    p.getPrecoCusto(),
                    p.getDisponivel(),
                    p.getStatus()
            );
        }
    }

    private record TargetResolution(ProdutoEntity entity, boolean skip) {

        static TargetResolution use(ProdutoEntity entity) {
            return new TargetResolution(entity, false);
        }

        static TargetResolution skipItem() {
            return new TargetResolution(null, true);
        }
    }

    private Long resolveTenantId() {
        return this.tenantResolverService == null ? null : this.tenantResolverService.resolveDefaultTenantId();
    }

    public record ImportacaoResumo(
            int lidos,
            int inseridos,
            int atualizados,
            int ignorados,
            int erros
    ) {
    }
}
