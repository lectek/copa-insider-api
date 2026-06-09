package br.com.redemaisfarma.tools;

import br.com.redemaisfarma.application.core.exception.ImportInProgressException;
import br.com.redemaisfarma.application.service.EstoqueFisicoImportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.core.env.Environment;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstoqueFisicoImportCliTest {

    @Mock
    private EstoqueFisicoImportService estoqueFisicoImportService;

    @Mock
    private Environment environment;

    @Test
    void runNaoExecutaImportacaoQuandoFlagNaoEstaAtiva() throws Exception {
        when(this.environment.getProperty("estoque-fisico.import.run")).thenReturn(null);
        when(this.environment.getProperty("ESTOQUE_FISICO_IMPORT_RUN")).thenReturn(null);

        EstoqueFisicoImportCli cli = new EstoqueFisicoImportCli(this.estoqueFisicoImportService, this.environment);
        cli.run(new DefaultApplicationArguments(new String[0]));

        verify(this.estoqueFisicoImportService, never()).importarTodosComoNaoDisponiveis();
    }

    @Test
    void runExecutaImportacaoQuandoFlagUppercaseEstaAtiva() throws Exception {
        when(this.environment.getProperty("estoque-fisico.import.run")).thenReturn(null);
        when(this.environment.getProperty("ESTOQUE_FISICO_IMPORT_RUN")).thenReturn("true");
        when(this.environment.getProperty("estoque-fisico.import.exit")).thenReturn(null);
        when(this.environment.getProperty("ESTOQUE_FISICO_IMPORT_EXIT")).thenReturn("false");
        when(this.estoqueFisicoImportService.importarTodosComoNaoDisponiveis())
                .thenReturn(new EstoqueFisicoImportService.ImportacaoResumo(10, 1, 2, 7, 0));

        EstoqueFisicoImportCli cli = new EstoqueFisicoImportCli(this.estoqueFisicoImportService, this.environment);
        cli.run(new DefaultApplicationArguments(new String[0]));

        verify(this.estoqueFisicoImportService).importarTodosComoNaoDisponiveis();
    }

    @Test
    void runIgnoraFalhaQuandoImportacaoJaEstaEmExecucao() throws Exception {
        when(this.environment.getProperty("estoque-fisico.import.run")).thenReturn(null);
        when(this.environment.getProperty("ESTOQUE_FISICO_IMPORT_RUN")).thenReturn("true");
        when(this.environment.getProperty("estoque-fisico.import.exit")).thenReturn("false");
        when(this.estoqueFisicoImportService.importarTodosComoNaoDisponiveis())
                .thenThrow(new ImportInProgressException("Ja existe uma importacao de estoque fisico em execucao."));

        EstoqueFisicoImportCli cli = new EstoqueFisicoImportCli(this.estoqueFisicoImportService, this.environment);
        cli.run(new DefaultApplicationArguments(new String[0]));

        verify(this.estoqueFisicoImportService).importarTodosComoNaoDisponiveis();
    }
}
