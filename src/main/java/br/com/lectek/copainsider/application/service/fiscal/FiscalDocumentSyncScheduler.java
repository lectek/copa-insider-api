package br.com.lectek.copainsider.application.service.fiscal;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FiscalDocumentSyncScheduler {

    private final FiscalDocumentService fiscalDocumentService;

    public FiscalDocumentSyncScheduler(
            final FiscalDocumentService fiscalDocumentServiceValue
    ) {
        this.fiscalDocumentService = fiscalDocumentServiceValue;
    }

    @Scheduled(
            fixedDelayString = "${app.fiscal.sync.fixed-delay-ms:300000}",
            initialDelayString = "${app.fiscal.sync.initial-delay-ms:20000}"
    )
    public void syncPendingDocuments() {
        fiscalDocumentService.syncPendingDocuments(10);
    }
}
