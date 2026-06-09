package br.com.lectek.copainsider.application.service.fiscal;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.FiscalDocumentEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.FiscalEmitterConfigEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.PedidoFiscalSnapshotEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.lectek.copainsider.domain.enums.StatusPedido;
import br.com.lectek.copainsider.domain.fiscal.FiscalDocumentStatus;
import br.com.lectek.copainsider.domain.fiscal.FiscalProvider;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FiscalOrderEmissionService {

    private final PedidoRepository pedidoRepository;
    private final PedidoFiscalSnapshotService pedidoFiscalSnapshotService;
    private final FiscalEmitterConfigService fiscalEmitterConfigService;
    private final FiscalDocumentService fiscalDocumentService;
    private final FocusFiscalPayloadBuilder focusFiscalPayloadBuilder;

    public FiscalOrderEmissionService(
            final PedidoRepository pedidoRepositoryValue,
            final PedidoFiscalSnapshotService pedidoFiscalSnapshotServiceValue,
            final FiscalEmitterConfigService fiscalEmitterConfigServiceValue,
            final FiscalDocumentService fiscalDocumentServiceValue,
            final FocusFiscalPayloadBuilder focusFiscalPayloadBuilderValue
    ) {
        this.pedidoRepository = pedidoRepositoryValue;
        this.pedidoFiscalSnapshotService = pedidoFiscalSnapshotServiceValue;
        this.fiscalEmitterConfigService = fiscalEmitterConfigServiceValue;
        this.fiscalDocumentService = fiscalDocumentServiceValue;
        this.focusFiscalPayloadBuilder = focusFiscalPayloadBuilderValue;
    }

    @Transactional
    public Optional<FiscalDocumentEntity> processPaidOrder(
            final Long pedidoId,
            final String source
    ) {
        final PedidoEntity pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Pedido nao encontrado para emissao fiscal."
                ));
        if (pedido.getStatus() != StatusPedido.PAGO) {
            return Optional.empty();
        }

        final FiscalEmitterConfigEntity config = loadEnabledConfig();
        if (config == null) {
            return Optional.empty();
        }

        final PedidoFiscalSnapshotEntity snapshot =
                pedidoFiscalSnapshotService.captureIfMissing(pedido, source);
        final FiscalDocumentEntity document = fiscalDocumentService
                .findLatestByPedidoId(pedido.getId())
                .orElseGet(() -> fiscalDocumentService.createDraft(
                        new FiscalDocumentService.CreateDraftInput(
                                pedido.getId(),
                                config.getProvider(),
                                snapshot.getSuggestedDocumentModel(),
                                config.getEnvironment(),
                                null,
                                snapshot.getIssuerCnpj(),
                                snapshot.getRecipientDocument(),
                                snapshot.getTotalAmount(),
                                null,
                                null,
                                null
                        )
                ));

        if (isInFlightOrFinished(document)) {
            if (document.getStatus() == FiscalDocumentStatus.AUTHORIZED
                    && document.getEmailDeliverySentAt() == null) {
                return fiscalDocumentService.dispatchPendingEmail(
                        document.getId()
                );
            }
            return Optional.of(document);
        }

        final String requestPayload;
        try {
            requestPayload = focusFiscalPayloadBuilder.build(snapshot, config);
        } catch (IllegalArgumentException ex) {
            return Optional.of(
                    fiscalDocumentService.recordError(
                            document.getId(),
                            ex.getMessage(),
                            null
                    )
            );
        }

        return Optional.of(
                fiscalDocumentService.submit(document.getId(), requestPayload)
        );
    }

    private FiscalEmitterConfigEntity loadEnabledConfig() {
        try {
            return fiscalEmitterConfigService.requireEnabledEntity(
                    FiscalProvider.FOCUS_NFE
            );
        } catch (IllegalStateException | NoSuchElementException ex) {
            return null;
        }
    }

    private boolean isInFlightOrFinished(final FiscalDocumentEntity document) {
        return document.getStatus() == FiscalDocumentStatus.AUTHORIZED
                || document.getStatus() == FiscalDocumentStatus.SUBMITTED
                || document.getStatus() == FiscalDocumentStatus.PENDING_SUBMISSION;
    }
}
