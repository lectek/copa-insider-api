package br.com.lectek.copainsider.application.service.fiscal;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.FiscalDocumentEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.PedidoFiscalSnapshotEntity;
import br.com.lectek.copainsider.domain.enums.ModoEntrega;
import br.com.lectek.copainsider.domain.fiscal.FiscalDocumentModel;
import br.com.lectek.copainsider.domain.fiscal.FiscalDocumentStatus;
import br.com.lectek.copainsider.domain.fiscal.FiscalPrintChannel;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoFiscalPresentationService {

    private final PedidoFiscalSnapshotService pedidoFiscalSnapshotService;
    private final FiscalDocumentService fiscalDocumentService;

    public PedidoFiscalPresentationService(
            final PedidoFiscalSnapshotService pedidoFiscalSnapshotServiceValue,
            final FiscalDocumentService fiscalDocumentServiceValue
    ) {
        this.pedidoFiscalSnapshotService = pedidoFiscalSnapshotServiceValue;
        this.fiscalDocumentService = fiscalDocumentServiceValue;
    }

    @Transactional(readOnly = true)
    public PedidoFiscalView build(final PedidoEntity pedido) {
        if (pedido == null || pedido.getId() == null) {
            throw new IllegalArgumentException(
                    "Pedido salvo e obrigatorio para apresentar dados fiscais."
            );
        }

        final Optional<PedidoFiscalSnapshotEntity> snapshot =
                pedidoFiscalSnapshotService.findByPedidoId(pedido.getId());
        final Optional<FiscalDocumentEntity> document =
                fiscalDocumentService.findLatestByPedidoId(pedido.getId());

        final FiscalPrintChannel printChannel = snapshot
                .map(PedidoFiscalSnapshotEntity::getPrintChannel)
                .orElse(resolvePrintChannel(pedido.getModoEntrega()));
        final String emailAddress = firstNonBlank(
                snapshot.map(PedidoFiscalSnapshotEntity::getEmailDeliveryAddress)
                        .orElse(null),
                snapshot.map(PedidoFiscalSnapshotEntity::getRecipientEmail)
                        .orElse(null),
                pedido.getCliente() == null ? null : pedido.getCliente().getEmail()
        );
        final boolean emailRequested = snapshot
                .map(PedidoFiscalSnapshotEntity::isEmailDeliveryRequested)
                .orElse(false);
        final LocalDateTime emailSentAt = document
                .map(FiscalDocumentEntity::getEmailDeliverySentAt)
                .orElse(null);
        final FiscalDocumentModel model = document.map(FiscalDocumentEntity::getModel)
                .orElse(snapshot.map(PedidoFiscalSnapshotEntity::getSuggestedDocumentModel)
                        .orElse(null));
        final FiscalDocumentStatus status = document.map(FiscalDocumentEntity::getStatus)
                .orElse(null);

        return new PedidoFiscalView(
                pedido.getId(),
                printChannel,
                printChannelLabel(printChannel),
                physicalInstruction(printChannel),
                emailRequested,
                emailAddress,
                emailStatusLabel(emailRequested, emailAddress, emailSentAt),
                model,
                documentModelLabel(model),
                status,
                documentStatusLabel(status),
                document.map(FiscalDocumentEntity::getAccessKey).orElse(null),
                document.map(FiscalDocumentEntity::getDanfeStoragePath)
                        .orElse(null),
                document.map(FiscalDocumentEntity::getXmlStoragePath)
                        .orElse(null),
                emailSentAt
        );
    }

    private FiscalPrintChannel resolvePrintChannel(final ModoEntrega modoEntrega) {
        if (modoEntrega == ModoEntrega.RETIRADA) {
            return FiscalPrintChannel.IMMEDIATE;
        }
        if (modoEntrega == ModoEntrega.ENTREGA) {
            return FiscalPrintChannel.WITH_DELIVERY;
        }
        return FiscalPrintChannel.NONE;
    }

    private String printChannelLabel(final FiscalPrintChannel printChannel) {
        if (printChannel == null) {
            return "Nao definido";
        }
        return switch (printChannel) {
            case IMMEDIATE -> "Imprimir na hora";
            case WITH_DELIVERY -> "Imprimir com a entrega";
            case NONE -> "Nao definido";
        };
    }

    private String physicalInstruction(final FiscalPrintChannel printChannel) {
        if (printChannel == FiscalPrintChannel.IMMEDIATE) {
            return "Via fisica para impressao imediata no caixa ou na retirada.";
        }
        if (printChannel == FiscalPrintChannel.WITH_DELIVERY) {
            return "Via fisica para seguir impressa junto com a entrega.";
        }
        return "Canal de impressao fiscal ainda nao definido.";
    }

    private String emailStatusLabel(
            final boolean emailRequested,
            final String emailAddress,
            final LocalDateTime emailSentAt
    ) {
        final String normalizedEmail = blankToNull(emailAddress);
        if (!emailRequested) {
            return "Cliente nao solicitou envio por e-mail.";
        }
        if (emailSentAt != null) {
            return normalizedEmail == null
                    ? "Nota fiscal enviada por e-mail."
                    : "Nota fiscal enviada por e-mail para " + normalizedEmail + ".";
        }
        if (normalizedEmail == null) {
            return "Envio por e-mail solicitado.";
        }
        return "Envio por e-mail pendente para " + normalizedEmail + ".";
    }

    private String documentModelLabel(final FiscalDocumentModel model) {
        if (model == null) {
            return "Nao definido";
        }
        return switch (model) {
            case NFCE_65 -> "NFC-e 65";
            case NFE_55 -> "NF-e 55";
        };
    }

    private String documentStatusLabel(final FiscalDocumentStatus status) {
        if (status == null) {
            return "Aguardando emissao";
        }
        return switch (status) {
            case DRAFT -> "Rascunho";
            case PENDING_SUBMISSION -> "Enviando";
            case SUBMITTED -> "Em processamento";
            case AUTHORIZED -> "Autorizada";
            case REJECTED -> "Rejeitada";
            case ERROR -> "Erro";
            case CANCELLED -> "Cancelada";
        };
    }

    private String firstNonBlank(final String... values) {
        for (String value : values) {
            final String normalized = blankToNull(value);
            if (normalized != null) {
                return normalized;
            }
        }
        return null;
    }

    private String blankToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    public record PedidoFiscalView(
            Long pedidoId,
            FiscalPrintChannel printChannel,
            String printChannelLabel,
            String physicalInstruction,
            boolean emailRequested,
            String emailAddress,
            String emailStatusLabel,
            FiscalDocumentModel documentModel,
            String documentModelLabel,
            FiscalDocumentStatus documentStatus,
            String documentStatusLabel,
            String accessKey,
            String danfeUrl,
            String xmlUrl,
            LocalDateTime emailSentAt
    ) {
    }
}
