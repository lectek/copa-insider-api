package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.legacy.entity.ProdutoLegacyEntity;
import br.com.redemaisfarma.adapters.outbound.legacy.repository.ProdutoLegacyRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.MetodoLeituraCodigoBarras;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.application.core.tenant.TenantResolverService;
import br.com.redemaisfarma.application.service.validation.ProductSourcePolicy;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@ConditionalOnProperty(name = "legacy.sync.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
public class SincronizacaoCatalogoService {

    private static final int CHUNK_SIZE = 1000;

    private final ProdutoLegacyRepository legacyRepo;
    private final ProdutoRepository produtoRepo;
    private final ObjectProvider<TenantResolverService> tenantResolverServiceProvider;
    @Lazy
    private final SincronizacaoCatalogoService self;

    @PersistenceContext(unitName = "mysqlPU")
    private EntityManager em;

    /** Lê tudo do legado em páginas e faz upsert no MySQL. */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public ResumoSync sincronizarTudo() {
        long t0 = System.currentTimeMillis();
        AtomicLong lidos = new AtomicLong();
        AtomicLong inseridos = new AtomicLong();
        AtomicLong atualizados = new AtomicLong();
        AtomicLong ignorados = new AtomicLong();
        AtomicLong erros = new AtomicLong();

        int page = 0;
        boolean hasNext = true;
        while (hasNext) {
            Page<ProdutoLegacyEntity> pagina = legacyRepo.findAll(PageRequest.of(page, CHUNK_SIZE));
            List<ProdutoLegacyEntity> lote = pagina.getContent();
            if (lote.isEmpty()) {
                hasNext = false;
                continue;
            }

            try {
                self.processarLote(lote, lidos, inseridos, atualizados, ignorados, erros);
            } catch (Exception e) {
                log.error("Erro processando lote (page={}, size={}): {}", page, lote.size(), e.getMessage(), e);
                erros.addAndGet(lote.size());
            }

            hasNext = pagina.hasNext();
            page++;
        }

        long ms = System.currentTimeMillis() - t0;
        log.info("Sync catálogo finalizada em {} ms | lidos={}, inseridos={}, atualizados={}, ignorados={}, erros={}",
                ms, lidos.get(), inseridos.get(), atualizados.get(), ignorados.get(), erros.get());

        return new ResumoSync(
                cap(lidos.get()), cap(inseridos.get()), cap(atualizados.get()), cap(ignorados.get()), cap(erros.get())
        );
    }

    /** Processa um lote dentro de uma transação nova no MySQL. */
    @Transactional(propagation = Propagation.REQUIRES_NEW, transactionManager = "mysqlTransactionManager")
    void processarLote(
            List<ProdutoLegacyEntity> lote,
            AtomicLong lidos,
            AtomicLong inseridos,
            AtomicLong atualizados,
            AtomicLong ignorados,
            AtomicLong erros
    ) {
        final Long tenantId = this.resolveTenantId();
        int i = 0;
        for (ProdutoLegacyEntity legacy : lote) {
            lidos.incrementAndGet();
            try {
                SyncResult result = processarItem(tenantId, legacy);
                switch (result) {
                    case INSERIDO -> inseridos.incrementAndGet();
                    case ATUALIZADO -> atualizados.incrementAndGet();
                    case IGNORADO -> ignorados.incrementAndGet();
                }
            } catch (Exception e) {
                erros.incrementAndGet();
                log.warn("Falha ao sincronizar item do lote (idx={}): {}", i, e.getMessage(), e);
            }

            if (++i % 250 == 0) {
                em.flush();
                em.clear();
            }
        }
        em.flush();
        em.clear();
    }

    private SyncResult processarItem(Long tenantId, ProdutoLegacyEntity legacy) {
        String legacyId = legacyIdAsString(legacy);
        String codigoBarras = normalizaCodigo(legacy.getCodigoBarras());

        BuscaDestino resultadoBusca = buscarDestino(tenantId, legacyId, codigoBarras);
        if (resultadoBusca.ignorar()) {
            return SyncResult.IGNORADO;
        }

        ProdutoEntity destino = resultadoBusca.destino();
        String novoHash = hashDoLegacy(legacy);

        if (destino == null) {
            inserirProduto(tenantId, legacy, legacyId, codigoBarras, novoHash);
            return SyncResult.INSERIDO;
        }

        if (Objects.equals(safe(destino.getHashLegado()), novoHash)) {
            return SyncResult.IGNORADO;
        }

        atualizarProduto(destino, tenantId, legacy, legacyId, codigoBarras, novoHash);
        return SyncResult.ATUALIZADO;
    }

    private BuscaDestino buscarDestino(Long tenantId, String legacyId, String codigoBarras) {
        ProdutoEntity destino = buscarPorLegacyId(tenantId, legacyId);
        if (ProductSourcePolicy.isProtectedFromLegacySync(destino)) {
            return new BuscaDestino(null, true);
        }

        if (destino == null && !codigoBarras.isBlank()) {
            destino = buscarPorCodigoBarras(tenantId, codigoBarras);
        }

        return new BuscaDestino(destino, false);
    }

    private ProdutoEntity buscarPorLegacyId(Long tenantId, String legacyId) {
        if (legacyId.isBlank()) {
            return null;
        }
        Long legacyIdLong = Long.valueOf(legacyId);
        return tenantId == null
                ? produtoRepo.findByLegacyId(legacyIdLong).orElse(null)
                : produtoRepo.findByTenantIdAndLegacyId(tenantId, legacyIdLong).orElse(null);
    }

    private ProdutoEntity buscarPorCodigoBarras(Long tenantId, String codigoBarras) {
        ProdutoEntity byBarcode = tenantId == null
                ? produtoRepo.findByAnyCodigo(codigoBarras).orElse(null)
                : produtoRepo.findByAnyCodigo(tenantId, codigoBarras).orElse(null);
        return ProductSourcePolicy.isProtectedFromLegacySync(byBarcode) ? null : byBarcode;
    }

    private void inserirProduto(
            Long tenantId,
            ProdutoLegacyEntity legacy,
            String legacyId,
            String codigoBarras,
            String novoHash
    ) {
        ProdutoEntity novo = new ProdutoEntity();
        novo.setTenantId(tenantId);
        preencherCamposComuns(novo, tenantId, legacy, legacyId, codigoBarras, novoHash);
        produtoRepo.save(novo);
    }

    private void atualizarProduto(
            ProdutoEntity destino,
            Long tenantId,
            ProdutoLegacyEntity legacy,
            String legacyId,
            String codigoBarras,
            String novoHash
    ) {
        if (destino.getTenantId() == null) {
            destino.setTenantId(tenantId);
        }
        preencherCamposComuns(destino, tenantId, legacy, legacyId, codigoBarras, novoHash);
        produtoRepo.save(destino);
    }

    private void preencherCamposComuns(
            ProdutoEntity target,
            Long tenantId,
            ProdutoLegacyEntity legacy,
            String legacyId,
            String codigoBarras,
            String novoHash
    ) {
        String resolvedBarcode = resolveBarcode(tenantId, target, codigoBarras);
        target.setNome(chooseFirstNonBlank(legacy.getNome(), legacy.getApresentacao()));
        target.setDescricao(legacy.getApresentacao());
        target.setCodigoBarras(resolvedBarcode);
        if (resolvedBarcode == null || resolvedBarcode.isBlank()) {
            target.preserveCodigoOriginalBarcode(codigoBarras);
        }
        target.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.LEGADO);
        target.setPrecoVenda(precoEfetivo(legacy));
        target.setPrecoPromocional(legacy.getPrecoPromocao());
        target.setDisponivel(Boolean.FALSE);
        target.setStatus(ProdutoStatus.IMPORTADO);
        target.setPublicadoEm(null);
        target.setDespublicadoEm(null);

        if (legacy.getSaldo() != null) {
            target.setEstoque(legacy.getSaldo().intValue());
        }
        if (target.getId() == null && legacy.getEstoqueMinimo() != null) {
            target.setEstoque(Math.max(target.getEstoque() == null ? 0 : target.getEstoque(), 0));
        }
        if (!legacyId.isBlank()) {
            target.setLegacyId(Long.valueOf(legacyId));
        }
        target.setHashLegado(novoHash);
        target.setDataImportacao(LocalDateTime.now());
    }

    private static String legacyIdAsString(ProdutoLegacyEntity legacy) {
        return legacy.getId() == null ? "" : String.valueOf(legacy.getId());
    }

    /* ========================= Helpers ========================= */

    private static boolean isPromocaoAtiva(ProdutoLegacyEntity l) {
        if (l.getPrecoPromocao() == null) return false;
        LocalDateTime now = LocalDateTime.now();
        boolean iniciou = l.getInicioPromocao() == null || !now.isBefore(l.getInicioPromocao());
        boolean naoTerminou = l.getTerminoPromocao() == null || !now.isAfter(l.getTerminoPromocao());
        return iniciou && naoTerminou;
    }

    private static BigDecimal precoEfetivo(ProdutoLegacyEntity l) {
        if (isPromocaoAtiva(l) && l.getPrecoPromocao() != null) return l.getPrecoPromocao();
        if (l.getPrecoVenda() != null) return l.getPrecoVenda();
        return l.getPrecoAnterior() != null ? l.getPrecoAnterior() : BigDecimal.ZERO;
    }

    private static String chooseFirstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a.trim();
        if (b != null && !b.isBlank()) return b.trim();
        return "Sem nome";
    }

    private String resolveBarcode(Long tenantId, ProdutoEntity target, String barcode) {
        String normalized = normalizaCodigo(barcode);
        if (normalized.isBlank()) {
            return normalizeExistingBarcode(target);
        }

        ProdutoEntity owner = tenantId == null
                ? produtoRepo.findByAnyCodigo(normalized).orElse(null)
                : produtoRepo.findByAnyCodigo(tenantId, normalized).orElse(null);
        if (owner == null) {
            return normalized;
        }
        if (target.getId() != null && owner.getId() != null && target.getId().equals(owner.getId())) {
            return normalized;
        }
        return normalizeExistingBarcode(target);
    }

    private static String normalizeExistingBarcode(ProdutoEntity target) {
        String current = target == null ? null : target.getCodigoBarras();
        String normalized = normalizaCodigo(current);
        return normalized.isBlank() ? null : normalized;
    }

    private static String normalizaCodigo(String s) {
        if (s == null) return "";
        String digits = s.replaceAll("\\D+", "");
        return digits.replaceFirst("^0+(?!$)", "");
    }

    private static String hashDoLegacy(ProdutoLegacyEntity l) {
        String base = String.join("|",
                sv(l.getId()),
                sv(l.getNome()),
                normalizaCodigo(l.getCodigoBarras()),
                bd(l.getSaldo()),
                bd(l.getPrecoVenda()),
                bd(l.getPrecoPromocao()),
                bd(l.getEstoqueMinimo()),
                bd(l.getMargemLucro()),
                dt(l.getInicioPromocao()),
                dt(l.getTerminoPromocao()),
                bd(l.getBonus()),
                sv(l.getApresentacao()),
                bd(l.getPrecoAnterior()),
                sv(l.getFornecedorId()),
                sv(l.getCategoriaId()),
                sv(l.getComissaoId())
        );
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(base.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            return base;
        }
    }

    private static String sv(Object o) { return o == null ? "" : String.valueOf(o).trim(); }
    private static String bd(BigDecimal b) { return b == null ? "" : b.stripTrailingZeros().toPlainString(); }
    private static String dt(LocalDateTime t) { return t == null ? "" : t.toString(); }
    private static String safe(String s) { return s == null ? "" : s; }

    private static int cap(long v) {
        return (int) Math.clamp(v, 0L, (long) Integer.MAX_VALUE);
    }

    private Long resolveTenantId() {
        TenantResolverService tenantResolverService = this.tenantResolverServiceProvider.getIfAvailable();
        if (tenantResolverService == null) {
            return null;
        }
        return tenantResolverService.resolveDefaultTenantId();
    }

    /** Resumo agregado da sincronização. */
    public record ResumoSync(int lidos, int inseridos, int atualizados, int ignorados, int erros) {}

    private enum SyncResult {
        INSERIDO,
        ATUALIZADO,
        IGNORADO
    }

    private record BuscaDestino(ProdutoEntity destino, boolean ignorar) {}
}
