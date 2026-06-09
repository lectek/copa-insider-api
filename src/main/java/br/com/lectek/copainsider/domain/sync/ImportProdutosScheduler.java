// src/main/java/br/com/lectek/copainsider/domain/sync/ImportProdutosScheduler.java
package br.com.lectek.copainsider.domain.sync;

import br.com.lectek.copainsider.application.service.SincronizacaoCatalogoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@ConditionalOnProperty(prefix = "legacy.sync", name = "enabled", havingValue = "true", matchIfMissing = false)
@ConditionalOnMissingBean(SincronizacaoCatalogoService.class)
public class ImportProdutosScheduler {
    private static final Logger log = LoggerFactory.getLogger(ImportProdutosScheduler.class);

    private final ImportProdutosService service;

    @Value("${sync.firebird.batch-size:1000}")
    private int batchSize;

    public ImportProdutosScheduler(ImportProdutosService service) {
        this.service = service;
    }

    @Scheduled(fixedDelayString = "${sync.firebird.period-ms:300000}")
    public void run() {
        try {
            int processed = service.runOnce(batchSize);
            log.info("Import Firebird→MySQL executado. processados={}", processed);
        } catch (Exception e) {
            log.error("Erro no import de produtos", e);
        }
    }
}
