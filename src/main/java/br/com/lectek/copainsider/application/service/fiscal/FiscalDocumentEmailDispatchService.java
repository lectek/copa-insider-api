package br.com.lectek.copainsider.application.service.fiscal;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.FiscalDocumentEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.PedidoFiscalSnapshotEntity;
import br.com.lectek.copainsider.application.service.MailService;
import br.com.lectek.copainsider.domain.fiscal.FiscalDocumentStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class FiscalDocumentEmailDispatchService {

    private final PedidoFiscalSnapshotService pedidoFiscalSnapshotService;
    private final MailService mailService;

    public FiscalDocumentEmailDispatchService(
            final PedidoFiscalSnapshotService pedidoFiscalSnapshotServiceValue,
            final MailService mailServiceValue
    ) {
        this.pedidoFiscalSnapshotService = pedidoFiscalSnapshotServiceValue;
        this.mailService = mailServiceValue;
    }

    public void dispatchIfRequested(final FiscalDocumentEntity document) {
        if (document == null
                || document.getStatus() != FiscalDocumentStatus.AUTHORIZED
                || document.getEmailDeliverySentAt() != null
                || document.getPedido() == null
                || document.getPedido().getId() == null) {
            return;
        }

        final Optional<PedidoFiscalSnapshotEntity> snapshot =
                pedidoFiscalSnapshotService.findByPedidoId(
                        document.getPedido().getId()
                );
        if (snapshot.isEmpty()) {
            return;
        }

        final PedidoFiscalSnapshotEntity delivery = snapshot.get();
        if (!delivery.isEmailDeliveryRequested()) {
            return;
        }
        final String email = normalize(delivery.getEmailDeliveryAddress());
        if (email == null) {
            return;
        }

        mailService.sendText(
                email,
                "Nota fiscal do pedido #" + document.getPedido().getId(),
                buildEmailBody(document, delivery),
                null
        );
        document.setEmailDeliverySentAt(LocalDateTime.now());
    }

    private String buildEmailBody(
            final FiscalDocumentEntity document,
            final PedidoFiscalSnapshotEntity snapshot
    ) {
        final StringBuilder body = new StringBuilder();
        body.append("Ola");
        if (snapshot.getRecipientName() != null
                && !snapshot.getRecipientName().isBlank()) {
            body.append(' ').append(snapshot.getRecipientName().trim());
        }
        body.append(",\n\n");
        body.append("Sua nota fiscal do pedido #")
                .append(document.getPedido().getId())
                .append(" foi autorizada.\n");
        if (document.getAccessKey() != null && !document.getAccessKey().isBlank()) {
            body.append("Chave de acesso: ")
                    .append(document.getAccessKey().trim())
                    .append('\n');
        }
        if (document.getDanfeStoragePath() != null
                && !document.getDanfeStoragePath().isBlank()) {
            body.append("DANFE: ")
                    .append(document.getDanfeStoragePath().trim())
                    .append('\n');
        }
        if (document.getXmlStoragePath() != null
                && !document.getXmlStoragePath().isBlank()) {
            body.append("XML: ")
                    .append(document.getXmlStoragePath().trim())
                    .append('\n');
        }
        body.append("\nRede Mais Farma");
        return body.toString();
    }

    private String normalize(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
