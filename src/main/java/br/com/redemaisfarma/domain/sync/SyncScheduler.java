// src/main/java/br/com/redemaisfarma/domain/sync/SyncScheduler.java
package br.com.redemaisfarma.domain.sync;

import br.com.redemaisfarma.adapters.outbound.legacy.dto.LegacyProdutoDTO;
import br.com.redemaisfarma.adapters.outbound.legacy.port.LegacyProdutoPort;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.MetodoLeituraCodigoBarras;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.application.service.validation.ProductSourcePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "legacy.sync", name = "enabled", havingValue = "true")
public class SyncScheduler {
    private static final Logger log = LoggerFactory.getLogger(SyncScheduler.class);
    private static final String SOURCE = "firebird.produtos";
    private static final int PAGE_SIZE = 500;

    private final LegacyProdutoPort legacy;
    private final ProdutoRepository repo;
    private final CheckpointService checkpoint;

    public SyncScheduler(LegacyProdutoPort legacy, ProdutoRepository repo, CheckpointService checkpoint) {
        this.legacy = legacy;
        this.repo = repo;
        this.checkpoint = checkpoint;
    }

    @Scheduled(cron = "${sync.firebird.cron:0 */5 * * * *}")
    public void run() {
        // Lê checkpoint como LocalDateTime com fallback para EPOCH (UTC)
        LocalDateTime since = checkpoint.readSince(
                SOURCE,
                LocalDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC)
        );

        log.info("Iniciando sync de produtos desde {}", since);

        int page = 0;
        LocalDateTime lastSeen = since;
        int totalUpserts = 0;

        while (true) {
            final List<LegacyProdutoDTO> lote;
            try {
                lote = legacy.fetchChangedSince(since, page, PAGE_SIZE);
            } catch (Exception e) {
                log.error("Falha no fetchChangedSince(page={}): {}", page, e.getMessage(), e);
                break;
            }
            if (lote.isEmpty()) break;

            LocalDateTime maxUpdated = lote.stream()
                    .map(LegacyProdutoDTO::updatedAt)
                    .max(Comparator.naturalOrder())
                    .orElse(lastSeen);

            for (LegacyProdutoDTO r : lote) {
                if (r.nome() == null || r.precoVenda() == null || r.precoVenda().compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                ProdutoEntity p = resolveTarget(r);
                if (p == null) {
                    continue;
                }
                String resolvedBarcode = resolveBarcode(p, r.ean());
                p.setNome(r.nome());
                p.setDescricao(r.apresentacao());
                p.setCodigoBarras(resolvedBarcode);
                if (resolvedBarcode == null || resolvedBarcode.isBlank()) {
                    p.preserveCodigoOriginalBarcode(r.ean());
                }
                p.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.LEGADO);
                p.setPrecoVenda(r.precoVenda());
                p.setPrecoPromocional(zeroToNull(r.precoPromocional()));
                p.setEstoque(Math.max(0, r.estoque()));
                p.setDisponivel(Boolean.FALSE);
                p.setLegacyId(r.legacyId());
                p.setStatus(ProdutoStatus.IMPORTADO);
                p.setPublicadoEm(null);
                p.setDespublicadoEm(null);
                p.setStatusSync(SyncStatus.SINCRONIZADO);

                if (p.getPrecoPromocional() != null && p.getPrecoPromocional().compareTo(BigDecimal.ZERO) > 0) {
                    double pct = 100.0 * (1.0 - p.getPrecoPromocional().doubleValue() / p.getPrecoVenda().doubleValue());
                    p.setDescontoPercentual((int) Math.round(pct));
                } else {
                    p.setDescontoPercentual(null);
                }

                repo.save(p);
                totalUpserts++;
            }

            lastSeen = maxUpdated;
            page++;
            if (lote.size() < PAGE_SIZE) break; // última página
        }

        if (lastSeen.isAfter(since)) {
            checkpoint.writeSince(SOURCE, lastSeen);
        }
        log.info("Sync de produtos finalizado. upserts={}, since={} -> {}", totalUpserts, since, lastSeen);
    }

    private BigDecimal zeroToNull(BigDecimal v) {
        return (v == null || v.compareTo(BigDecimal.ZERO) <= 0) ? null : v;
    }

    private ProdutoEntity resolveTarget(LegacyProdutoDTO dto) {
        if (dto.legacyId() != null) {
            ProdutoEntity byLegacyId = repo.findByLegacyId(dto.legacyId()).orElse(null);
            if (ProductSourcePolicy.isProtectedFromLegacySync(byLegacyId)) {
                return null;
            }
            if (byLegacyId != null) {
                return byLegacyId;
            }
        }

        ProdutoEntity byBarcode = repo.findByAnyCodigo(dto.ean()).orElse(null);
        if (ProductSourcePolicy.isProtectedFromLegacySync(byBarcode)) {
            return new ProdutoEntity();
        }
        return byBarcode != null ? byBarcode : new ProdutoEntity();
    }

    private String resolveBarcode(ProdutoEntity target, String barcode) {
        if (barcode == null || barcode.isBlank()) {
            return normalizeCurrentBarcode(target);
        }

        ProdutoEntity owner = repo.findByAnyCodigo(barcode).orElse(null);
        if (owner == null) {
            return barcode;
        }
        if (target.getId() != null && owner.getId() != null && target.getId().equals(owner.getId())) {
            return barcode;
        }
        return normalizeCurrentBarcode(target);
    }

    private String normalizeCurrentBarcode(ProdutoEntity target) {
        String current = target == null ? null : target.getCodigoBarras();
        return (current == null || current.isBlank()) ? null : current;
    }
}
