package br.com.redemaisfarma.tools;

import br.com.redemaisfarma.application.core.exception.ImportInProgressException;
import br.com.redemaisfarma.application.service.EstoqueFisicoImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class EstoqueFisicoImportCli implements ApplicationRunner {

    private final EstoqueFisicoImportService estoqueFisicoImportService;
    private final Environment environment;

    @Override
    public void run(ApplicationArguments args) {
        if (!this.readBoolean("estoque-fisico.import.run", "ESTOQUE_FISICO_IMPORT_RUN", false)) {
            return;
        }

        System.out.println("[estoque-csv-import] iniciando importacao completa para o banco...");
        try {
            EstoqueFisicoImportService.ImportacaoResumo resumo = this.estoqueFisicoImportService.importarTodosComoNaoDisponiveis();
            System.out.printf(
                    "[estoque-csv-import] finalizado | lidos=%d inseridos=%d atualizados=%d ignorados=%d erros=%d%n",
                    resumo.lidos(),
                    resumo.inseridos(),
                    resumo.atualizados(),
                    resumo.ignorados(),
                    resumo.erros()
            );
        } catch (ImportInProgressException ex) {
            System.out.printf("[estoque-csv-import] abortado | motivo=%s%n", ex.getMessage());
        }

        if (this.readBoolean("estoque-fisico.import.exit", "ESTOQUE_FISICO_IMPORT_EXIT", true)) {
            System.exit(0);
        }
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
