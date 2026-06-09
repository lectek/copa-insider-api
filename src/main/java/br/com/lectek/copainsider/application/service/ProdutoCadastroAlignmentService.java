package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.MetodoLeituraCodigoBarras;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProdutoCadastroAlignmentService {

    private static final String CATEGORIA_ESTOQUE_FISICO = "Estoque fisico";

    private final ProdutoRepository produtoRepository;

    @Transactional(readOnly = true, transactionManager = "mysqlTransactionManager")
    public AuditReport audit() {
        List<ProdutoEntity> unknown = this.loadBySource(MetodoLeituraCodigoBarras.DESCONHECIDO);
        List<ProdutoEntity> catalogoLocal = this.loadBySource(MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA);
        return this.buildReport(unknown, catalogoLocal);
    }

    @Transactional(transactionManager = "mysqlTransactionManager")
    public RepairSummary reclassifyUnknownStock(boolean dryRun) {
        List<ProdutoEntity> unknown = this.loadBySource(MetodoLeituraCodigoBarras.DESCONHECIDO);
        List<ProdutoEntity> catalogoLocal = this.loadBySource(MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA);
        AuditReport audit = this.buildReport(unknown, catalogoLocal);

        List<ProdutoEntity> candidates = unknown.stream()
                .filter(this::isEstoqueFisico)
                .toList();

        int applied = 0;
        if (!dryRun && !candidates.isEmpty()) {
            List<ProdutoEntity> toSave = new ArrayList<>(candidates.size());
            for (ProdutoEntity entity : candidates) {
                entity.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.CSV_ESTOQUE);
                toSave.add(entity);
            }
            this.produtoRepository.saveAll(toSave);
            applied = toSave.size();
        }

        return new RepairSummary(
                dryRun,
                candidates.size(),
                applied,
                audit.duplicateUnknownLegacyIds().size(),
                audit.pdfLegacyOverlaps().size()
        );
    }

    private AuditReport buildReport(List<ProdutoEntity> unknown, List<ProdutoEntity> catalogoLocal) {
        List<LegacyConflictGroup> duplicateUnknownLegacyIds = toConflictGroups(groupByLegacyId(unknown).entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        Map<Long, List<ProdutoEntity>> catalogoLocalByLegacyId = groupByLegacyId(catalogoLocal);
        List<LegacyConflictGroup> pdfLegacyOverlaps = groupByLegacyId(unknown).entrySet().stream()
                .filter(entry -> catalogoLocalByLegacyId.containsKey(entry.getKey()))
                .map(entry -> {
                    List<ProdutoSummary> produtos = new ArrayList<>();
                    produtos.addAll(toSummaries(entry.getValue()));
                    produtos.addAll(toSummaries(catalogoLocalByLegacyId.get(entry.getKey())));
                    produtos.sort(Comparator.comparing(ProdutoSummary::origem, Comparator.nullsLast(String::compareTo))
                            .thenComparing(ProdutoSummary::id, Comparator.nullsLast(Long::compareTo)));
                    return new LegacyConflictGroup(entry.getKey(), List.copyOf(produtos));
                })
                .sorted(conflictComparator())
                .toList();

        long unknownStock = unknown.stream().filter(this::isEstoqueFisico).count();
        long unknownNonStock = unknown.size() - unknownStock;
        int duplicateRows = duplicateUnknownLegacyIds.stream().mapToInt(group -> group.produtos().size()).sum();

        return new AuditReport(
                unknown.size(),
                Math.toIntExact(unknownStock),
                Math.toIntExact(unknownNonStock),
                duplicateUnknownLegacyIds,
                duplicateRows,
                pdfLegacyOverlaps
        );
    }

    private List<ProdutoEntity> loadBySource(MetodoLeituraCodigoBarras source) {
        return this.produtoRepository.findAllByMetodoLeituraCodigoBarras(source);
    }

    private Map<Long, List<ProdutoEntity>> groupByLegacyId(List<ProdutoEntity> produtos) {
        return produtos.stream()
                .filter(produto -> produto.getLegacyId() != null)
                .collect(Collectors.groupingBy(
                        ProdutoEntity::getLegacyId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private List<LegacyConflictGroup> toConflictGroups(Map<Long, List<ProdutoEntity>> groups) {
        return groups.entrySet().stream()
                .map(entry -> new LegacyConflictGroup(entry.getKey(), toSummaries(entry.getValue())))
                .sorted(conflictComparator())
                .toList();
    }

    private Comparator<LegacyConflictGroup> conflictComparator() {
        return Comparator
                .comparingInt((LegacyConflictGroup group) -> group.produtos().size())
                .reversed()
                .thenComparing(LegacyConflictGroup::legacyId, Comparator.nullsLast(Long::compareTo));
    }

    private List<ProdutoSummary> toSummaries(List<ProdutoEntity> produtos) {
        return produtos.stream()
                .map(this::toSummary)
                .sorted(Comparator.comparing(ProdutoSummary::id, Comparator.nullsLast(Long::compareTo)))
                .toList();
    }

    private ProdutoSummary toSummary(ProdutoEntity entity) {
        return new ProdutoSummary(
                entity.getId(),
                entity.getLegacyId(),
                entity.getMetodoLeituraCodigoBarras() == null ? null : entity.getMetodoLeituraCodigoBarras().name(),
                entity.getCategoria(),
                entity.getCodigoBarras(),
                entity.getEstoque(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getNome()
        );
    }

    private boolean isEstoqueFisico(ProdutoEntity entity) {
        if (entity == null || entity.getCategoria() == null) {
            return false;
        }
        return normalize(entity.getCategoria()).equals(normalize(CATEGORIA_ESTOQUE_FISICO));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public record AuditReport(
            int totalUnknown,
            int unknownStock,
            int unknownNonStock,
            List<LegacyConflictGroup> duplicateUnknownLegacyIds,
            int duplicateUnknownRows,
            List<LegacyConflictGroup> pdfLegacyOverlaps
    ) {
    }

    public record LegacyConflictGroup(Long legacyId, List<ProdutoSummary> produtos) {
    }

    public record ProdutoSummary(
            Long id,
            Long legacyId,
            String origem,
            String categoria,
            String codigoBarras,
            Integer estoque,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String nome
    ) {
    }

    public record RepairSummary(
            boolean dryRun,
            int candidateUnknownStockRows,
            int appliedUnknownStockRows,
            int duplicateUnknownLegacyGroups,
            int pdfOverlapGroups
    ) {
    }
}
