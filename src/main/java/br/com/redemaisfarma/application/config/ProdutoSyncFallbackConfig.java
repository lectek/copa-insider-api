package br.com.redemaisfarma.application.config;

import br.com.redemaisfarma.application.service.sync.ProdutoSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ProdutoSyncFallbackConfig {

    @Bean
    @ConditionalOnMissingBean(ProdutoSyncService.class)
    public ProdutoSyncService produtoSyncServiceNoOp() {
        log.warn("⚠️ Carregando ProdutoSyncService NO-OP (fallback). Nenhuma sincronização real será executada.");
        return new ProdutoSyncService(null) {
            @Override
            public int sincronizar() {
                log.info("ProdutoSyncService NO-OP: sincronizar() chamado — nenhuma ação realizada.");
                return 0;
            }
        };
    }
}
