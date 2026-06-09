package br.com.lectek.copainsider.adapters.inbound.scheduler;

import br.com.lectek.copainsider.application.service.sync.ProdutoSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "legacy.sync.enabled", havingValue = "true", matchIfMissing = false)
public class SincronizarProdutosJob {

    private static final Logger log = LoggerFactory.getLogger(SincronizarProdutosJob.class);
    private final ObjectProvider<ProdutoSyncService> produtoSyncService;

    public SincronizarProdutosJob(ObjectProvider<ProdutoSyncService> produtoSyncService) {
        this.produtoSyncService = produtoSyncService;
    }

    @Scheduled(cron = "${legacy.sync.cron:0 0 * * * *}")
    public void executar() {
        ProdutoSyncService svc = produtoSyncService.getIfAvailable();
        if (svc == null) {
            log.warn("Sincronização habilitada, mas ProdutoSyncService indisponível (NO-OP).");
            return;
        }
        try {
            int n = svc.sincronizar();
            log.info("Sincronização de produtos concluída: {} itens processados.", n);
        } catch (Exception e) {
            log.error("Falha na sincronização de produtos: {}", e.getMessage(), e);
        }
    }
}
