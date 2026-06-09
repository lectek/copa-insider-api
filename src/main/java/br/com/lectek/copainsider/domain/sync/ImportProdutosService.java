// src/main/java/br/com/lectek/copainsider/domain/sync/ImportProdutosService.java
package br.com.lectek.copainsider.domain.sync;

import br.com.lectek.copainsider.adapters.outbound.firebird.FirebirdProdutoDao;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.MetodoLeituraCodigoBarras;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.lectek.copainsider.application.service.SincronizacaoCatalogoService;
import br.com.lectek.copainsider.application.service.validation.ProductSourcePolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "legacy.sync", name = "enabled", havingValue = "true", matchIfMissing = false)
@ConditionalOnMissingBean(SincronizacaoCatalogoService.class)
public class ImportProdutosService {

    private static final String CHECKPOINT_SOURCE = "firebird.produtos";
    private static final ZoneId ZONE = ZoneOffset.UTC; // **use sempre o mesmo fuso**

    private final FirebirdProdutoDao dao;         // Firebird (somente leitura)
    private final ProdutoRepository repo;         // MySQL (escrita)
    private final CheckpointService checkpoint;   // MySQL (sync_checkpoint)

    /**
     * Executa uma rodada de importação paginada a partir do checkpoint.
     * @param pageSize tamanho do lote (ex.: 500)
     * @return total de registros processados
     */
    @Transactional
    public int runOnce(int pageSize) {
        // Lê o since (Instant) com fallback para EPOCH (1970-01-01T00:00:00Z)
        Instant since = checkpoint.readSinceInstant(CHECKPOINT_SOURCE, Instant.EPOCH);

        // Cursor interno em LocalDateTime
        LocalDateTime cursor = LocalDateTime.ofInstant(since, ZONE);
        int total = 0;

        while (true) {
            PageResult pr = importarPagina(cursor, pageSize);
            total += pr.processed();

            if (pr.maxTs().isAfter(cursor)) {
                cursor = pr.maxTs();
            }
            // quando processou menos que o limite, terminamos
            if (pr.processed() < pageSize) break;
        }

        // Atualiza checkpoint somente se avançou
        Instant newSince = cursor.atZone(ZONE).toInstant();
        if (newSince.isAfter(since)) {
            checkpoint.updateSince(CHECKPOINT_SOURCE, newSince);
            log.info("Checkpoint avançado para {}", Timestamp.from(newSince));
        }

        log.info("Import Firebird→MySQL concluído: {} produtos processados (desde {}).",
                total, Timestamp.from(since));
        return total;
    }

    /**
     * Importa UMA página de até {@code limit} itens a partir de {@code since}.
     */
    @Transactional
    public PageResult importarPagina(LocalDateTime since, int limit) {
        List<FirebirdProdutoDao.FbProduto> lote = dao.fetchAtualizados(since, limit);
        if (lote.isEmpty()) return new PageResult(0, since);

        Map<String, ProdutoEntity> toUpsert = new LinkedHashMap<>(lote.size());
        LocalDateTime maxTs = since;

        for (FirebirdProdutoDao.FbProduto fb : lote) {
            String ean = normalizeEan(fb.codBarras());
            if (ean == null || ean.isBlank()) {
                // Sem EAN: ignorar (ou definir regra alternativa)
                continue;
            }

            Long legacyId = fb.produtoId() != null ? fb.produtoId().longValue() : null;
            String upsertKey = buildUpsertKey(legacyId, ean);
            ProdutoEntity p = toUpsert.get(upsertKey);
            if (p == null) {
                p = resolveTarget(legacyId, ean);
                if (p == null) {
                    if (fb.lastUpdate() != null && fb.lastUpdate().isAfter(maxTs)) {
                        maxTs = fb.lastUpdate();
                    }
                    continue;
                }
                toUpsert.put(upsertKey, p);
            }

            if (p.getId() == null) {
                String resolvedBarcode = resolveBarcode(p, ean);
                p.setCodigoBarras(resolvedBarcode);
                if (resolvedBarcode == null || resolvedBarcode.isBlank()) {
                    p.preserveCodigoOriginalBarcode(ean);
                }
                p.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.LEGADO);
                p.setDataCadastro(LocalDate.now());
            }

            if (p.getMetodoLeituraCodigoBarras() == null) {
                p.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.LEGADO);
            }

            p.setLegacyId(legacyId);
            p.setNome(nvl(fb.produto(), "Produto"));
            p.setDescricao(nvl(fb.apresentacao(), null));
            String resolvedBarcode = resolveBarcode(p, ean);
            p.setCodigoBarras(resolvedBarcode);
            if (resolvedBarcode == null || resolvedBarcode.isBlank()) {
                p.preserveCodigoOriginalBarcode(ean);
            }
            p.setPrecoVenda(defaultPositive(fb.precoVenda()));
            p.setPrecoPromocional(normalizePromo(fb.precoPromocao()));
            p.setPrecoCusto(defaultPrecoCusto(null, p.getPrecoVenda()));

            int estoque = fb.estoque() == null ? 0 : Math.max(0, fb.estoque());
            p.setEstoque(estoque);

            p.setDisponivel(Boolean.FALSE);

            p.setStatusSync("SINCRONIZADO");
            p.setStatus(ProdutoStatus.IMPORTADO);
            p.setPublicadoEm(null);
            p.setDespublicadoEm(null);

            // Desconto (%) com BigDecimal
            if (p.getPrecoPromocional() != null
                    && p.getPrecoPromocional().compareTo(BigDecimal.ZERO) > 0
                    && p.getPrecoVenda() != null
                    && p.getPrecoVenda().compareTo(BigDecimal.ZERO) > 0) {

                BigDecimal oneMinus = BigDecimal.ONE.subtract(
                        p.getPrecoPromocional()
                         .divide(p.getPrecoVenda(), 8, RoundingMode.HALF_UP));
                int pct = oneMinus.multiply(BigDecimal.valueOf(100))
                                  .setScale(0, RoundingMode.HALF_UP)
                                  .intValue();
                p.setDescontoPercentual(Math.max(0, Math.min(100, pct)));
            } else {
                p.setDescontoPercentual(null);
            }

            // Hash do conteúdo legado para detectar mudanças
            String fingerprint = buildFingerprint(fb, ean);
            p.setHashLegado(sha256Hex(fingerprint));

            if (p.getValidador() == null || p.getValidador().isBlank()) {
                p.setValidador(ean);
            }

            // Avança o maior timestamp visto nesta página
            if (fb.lastUpdate() != null && fb.lastUpdate().isAfter(maxTs)) {
                maxTs = fb.lastUpdate();
            }
        }

        // Escrita em batch
        repo.saveAll(toUpsert.values());
        return new PageResult(toUpsert.size(), maxTs);
    }

    // ========= helpers & value object =========

    private ProdutoEntity resolveTarget(Long legacyId, String ean) {
        if (legacyId != null) {
            ProdutoEntity byLegacyId = repo.findByLegacyId(legacyId).orElse(null);
            if (ProductSourcePolicy.isProtectedFromLegacySync(byLegacyId)) {
                return null;
            }
            if (byLegacyId != null) {
                return byLegacyId;
            }
        }

        ProdutoEntity byBarcode = repo.findByAnyCodigo(ean).orElse(null);
        if (ProductSourcePolicy.isProtectedFromLegacySync(byBarcode)) {
            return new ProdutoEntity();
        }
        return byBarcode != null ? byBarcode : new ProdutoEntity();
    }

    private ProdutoEntity resolveBarcodeOwner(String ean) {
        if (ean == null || ean.isBlank()) {
            return null;
        }
        return repo.findByAnyCodigo(ean).orElse(null);
    }

    private String resolveBarcode(ProdutoEntity target, String ean) {
        String normalized = normalizeEan(ean);
        if (normalized == null || normalized.isBlank()) {
            return normalizeEan(target == null ? null : target.getCodigoBarras());
        }

        ProdutoEntity owner = resolveBarcodeOwner(normalized);
        if (owner == null) {
            return normalized;
        }
        if (target.getId() != null && owner.getId() != null && target.getId().equals(owner.getId())) {
            return normalized;
        }
        return normalizeEan(target == null ? null : target.getCodigoBarras());
    }

    private static String buildUpsertKey(Long legacyId, String ean) {
        if (legacyId != null) {
            return "L:" + legacyId;
        }
        return "B:" + ean;
    }

    private static String buildFingerprint(FirebirdProdutoDao.FbProduto fb, String ean) {
        return String.join("|",
                nz(ean),
                nz(fb.produtoId()),
                nz(fb.produto()),
                nz(fb.apresentacao()),
                nz(fb.precoVenda()),
                nz(fb.precoPromocao()),
                nz(fb.estoque()),
                nz(fb.lastUpdate())
        );
    }

    private static String sha256Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(s.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(dig);
        } catch (Exception e) {
            return "sha256_error_" + Integer.toHexString(s.hashCode());
        }
    }

    private static String nz(Object o) { return o == null ? "" : o.toString().trim(); }

    private static String nvl(String v, String def) {
        return v == null || v.isBlank() ? def : v.trim();
    }

    private static String normalizeEan(String e) {
        if (e == null) return null;
        String d = e.replaceAll("\\D", "");
        return d.isEmpty() ? null : d;
    }

    private static BigDecimal defaultPositive(BigDecimal v) {
        return v == null || v.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : v;
    }

    private static BigDecimal normalizePromo(BigDecimal v) {
        if (v == null) return null;
        return v.compareTo(BigDecimal.ZERO) > 0 ? v : null;
    }

    private static BigDecimal defaultPrecoCusto(BigDecimal custoLido, BigDecimal precoVenda) {
        if (custoLido != null && custoLido.compareTo(BigDecimal.ZERO) >= 0) return custoLido;
        return BigDecimal.ZERO;
    }

    public record PageResult(int processed, LocalDateTime maxTs) {}
}
