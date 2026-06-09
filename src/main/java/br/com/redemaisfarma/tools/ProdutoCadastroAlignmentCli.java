package br.com.redemaisfarma.tools;

import br.com.redemaisfarma.application.service.ProdutoCadastroAlignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "produto-cadastro.alignment.run", havingValue = "true", matchIfMissing = false)
public class ProdutoCadastroAlignmentCli implements ApplicationRunner {

    private static final int SAMPLE_LIMIT = 10;

    private final ProdutoCadastroAlignmentService alignmentService;
    private final Environment environment;

    @Override
    public void run(ApplicationArguments args) {
        ProdutoCadastroAlignmentService.AuditReport audit = this.alignmentService.audit();
        printAudit(audit);

        boolean reclassifyUnknownStock = this.readBoolean(
                "produto-cadastro.alignment.reclassify-unknown-stock",
                "PRODUTO_CADASTRO_ALIGNMENT_RECLASSIFY_UNKNOWN_STOCK",
                false
        );
        boolean dryRun = this.readBoolean(
                "produto-cadastro.alignment.dry-run",
                "PRODUTO_CADASTRO_ALIGNMENT_DRY_RUN",
                true
        );

        if (reclassifyUnknownStock) {
            ProdutoCadastroAlignmentService.RepairSummary summary =
                    this.alignmentService.reclassifyUnknownStock(dryRun);
            System.out.printf(
                    "[produto-cadastro-alignment] reclassify-unknown-stock dryRun=%s candidates=%d applied=%d duplicateLegacyGroups=%d pdfOverlapGroups=%d%n",
                    summary.dryRun(),
                    summary.candidateUnknownStockRows(),
                    summary.appliedUnknownStockRows(),
                    summary.duplicateUnknownLegacyGroups(),
                    summary.pdfOverlapGroups()
            );
        }

        if (this.readBoolean(
                "produto-cadastro.alignment.exit",
                "PRODUTO_CADASTRO_ALIGNMENT_EXIT",
                true
        )) {
            System.exit(0);
        }
    }

    private void printAudit(ProdutoCadastroAlignmentService.AuditReport audit) {
        System.out.printf(
                "[produto-cadastro-alignment] audit totalUnknown=%d unknownStock=%d unknownNonStock=%d duplicateUnknownLegacyGroups=%d duplicateUnknownRows=%d pdfOverlapGroups=%d%n",
                audit.totalUnknown(),
                audit.unknownStock(),
                audit.unknownNonStock(),
                audit.duplicateUnknownLegacyIds().size(),
                audit.duplicateUnknownRows(),
                audit.pdfLegacyOverlaps().size()
        );
        printGroups("duplicate-legacy", audit.duplicateUnknownLegacyIds());
        printGroups("pdf-overlap", audit.pdfLegacyOverlaps());
    }

    private void printGroups(String label, List<ProdutoCadastroAlignmentService.LegacyConflictGroup> groups) {
        int limit = Math.min(SAMPLE_LIMIT, groups.size());
        for (int i = 0; i < limit; i++) {
            ProdutoCadastroAlignmentService.LegacyConflictGroup group = groups.get(i);
            String details = group.produtos().stream()
                    .map(produto -> String.format(
                            "%d:%s:%s:%s",
                            produto.id(),
                            produto.origem(),
                            produto.codigoBarras(),
                            shorten(produto.nome())
                    ))
                    .reduce((left, right) -> left + " | " + right)
                    .orElse("<sem-produtos>");
            System.out.printf(
                    "[produto-cadastro-alignment] %s legacyId=%d qtd=%d -> %s%n",
                    label,
                    group.legacyId(),
                    group.produtos().size(),
                    details
            );
        }
    }

    private String shorten(String value) {
        if (!StringUtils.hasText(value)) {
            return "SEM_NOME";
        }
        String trimmed = value.trim();
        return trimmed.length() <= 40 ? trimmed : trimmed.substring(0, 40);
    }

    private boolean readBoolean(String propertyKey, String envKey, boolean defaultValue) {
        String raw = this.environment.getProperty(propertyKey);
        if (!StringUtils.hasText(raw)) {
            raw = this.environment.getProperty(envKey);
        }
        if (!StringUtils.hasText(raw)) {
            raw = System.getenv(envKey);
        }
        if (!StringUtils.hasText(raw)) {
            return defaultValue;
        }
        return Boolean.parseBoolean(raw.trim());
    }
}
