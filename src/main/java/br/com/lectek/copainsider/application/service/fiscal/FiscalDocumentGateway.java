package br.com.lectek.copainsider.application.service.fiscal;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.FiscalDocumentEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.FiscalEmitterConfigEntity;
import br.com.lectek.copainsider.domain.fiscal.FiscalDocumentStatus;
import br.com.lectek.copainsider.domain.fiscal.FiscalProvider;
import java.time.LocalDateTime;

public interface FiscalDocumentGateway {

    FiscalProvider provider();

    GatewayStatusSnapshot submitDocument(
            FiscalEmitterConfigEntity config,
            FiscalDocumentEntity document,
            String requestPayload
    );

    GatewayStatusSnapshot queryStatus(
            FiscalEmitterConfigEntity config,
            FiscalDocumentEntity document
    );

    record GatewayStatusSnapshot(
            FiscalDocumentStatus status,
            String externalId,
            String accessKey,
            Integer series,
            Integer documentNumber,
            String protocol,
            String xmlStoragePath,
            String danfeStoragePath,
            String message,
            String errorMessage,
            String providerEventId,
            String responsePayload,
            LocalDateTime issuedAt,
            LocalDateTime authorizedAt,
            LocalDateTime cancelledAt,
            LocalDateTime processedAt
    ) {
    }
}
