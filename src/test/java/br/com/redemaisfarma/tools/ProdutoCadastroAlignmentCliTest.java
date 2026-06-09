package br.com.redemaisfarma.tools;

import br.com.redemaisfarma.application.service.ProdutoCadastroAlignmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.core.env.Environment;

import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoCadastroAlignmentCliTest {

    @Mock
    private ProdutoCadastroAlignmentService alignmentService;

    @Mock
    private Environment environment;

    @Test
    void runOnlyAuditsWhenRepairFlagIsDisabled() throws Exception {
        when(this.environment.getProperty("produto-cadastro.alignment.reclassify-unknown-stock")).thenReturn(null);
        when(this.environment.getProperty("PRODUTO_CADASTRO_ALIGNMENT_RECLASSIFY_UNKNOWN_STOCK")).thenReturn(null);
        when(this.environment.getProperty("produto-cadastro.alignment.dry-run")).thenReturn(null);
        when(this.environment.getProperty("PRODUTO_CADASTRO_ALIGNMENT_DRY_RUN")).thenReturn(null);
        when(this.environment.getProperty("produto-cadastro.alignment.exit")).thenReturn("false");
        when(this.alignmentService.audit()).thenReturn(new ProdutoCadastroAlignmentService.AuditReport(
                3,
                2,
                1,
                List.of(),
                0,
                List.of()
        ));

        ProdutoCadastroAlignmentCli cli = new ProdutoCadastroAlignmentCli(this.alignmentService, this.environment);
        cli.run(new DefaultApplicationArguments(new String[0]));

        verify(this.alignmentService).audit();
        verify(this.alignmentService, never()).reclassifyUnknownStock(false);
        verify(this.alignmentService, never()).reclassifyUnknownStock(true);
    }

    @Test
    void runTriggersDryRunRepairWhenUppercaseFlagsAreEnabled() throws Exception {
        when(this.environment.getProperty("produto-cadastro.alignment.reclassify-unknown-stock")).thenReturn(null);
        when(this.environment.getProperty("PRODUTO_CADASTRO_ALIGNMENT_RECLASSIFY_UNKNOWN_STOCK")).thenReturn("true");
        when(this.environment.getProperty("produto-cadastro.alignment.dry-run")).thenReturn(null);
        when(this.environment.getProperty("PRODUTO_CADASTRO_ALIGNMENT_DRY_RUN")).thenReturn("true");
        when(this.environment.getProperty("produto-cadastro.alignment.exit")).thenReturn("false");
        when(this.alignmentService.audit()).thenReturn(new ProdutoCadastroAlignmentService.AuditReport(
                3,
                2,
                1,
                List.of(),
                0,
                List.of()
        ));
        when(this.alignmentService.reclassifyUnknownStock(true)).thenReturn(
                new ProdutoCadastroAlignmentService.RepairSummary(true, 2, 0, 1, 0)
        );

        ProdutoCadastroAlignmentCli cli = new ProdutoCadastroAlignmentCli(this.alignmentService, this.environment);
        cli.run(new DefaultApplicationArguments(new String[0]));

        verify(this.alignmentService).audit();
        verify(this.alignmentService).reclassifyUnknownStock(true);
    }
}
