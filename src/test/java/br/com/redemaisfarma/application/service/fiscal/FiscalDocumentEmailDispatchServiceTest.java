package br.com.redemaisfarma.application.service.fiscal;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.FiscalDocumentEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoFiscalSnapshotEntity;
import br.com.redemaisfarma.application.service.MailService;
import br.com.redemaisfarma.domain.fiscal.FiscalDocumentStatus;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FiscalDocumentEmailDispatchServiceTest {

    @Mock
    private PedidoFiscalSnapshotService pedidoFiscalSnapshotService;

    @Mock
    private MailService mailService;

    private FiscalDocumentEmailDispatchService service;

    @BeforeEach
    void setUp() {
        service = new FiscalDocumentEmailDispatchService(
                pedidoFiscalSnapshotService,
                mailService
        );
    }

    @Test
    void dispatchIfRequestedSendsEmailAndMarksDocument() {
        final PedidoEntity pedido = new PedidoEntity();
        pedido.setId(44L);

        final FiscalDocumentEntity document = new FiscalDocumentEntity();
        document.setPedido(pedido);
        document.setStatus(FiscalDocumentStatus.AUTHORIZED);
        document.setAccessKey("123");
        document.setDanfeStoragePath("https://danfe");
        document.setXmlStoragePath("https://xml");

        final PedidoFiscalSnapshotEntity snapshot = new PedidoFiscalSnapshotEntity();
        snapshot.setRecipientName("Joao");
        snapshot.setEmailDeliveryRequested(true);
        snapshot.setEmailDeliveryAddress("joao@teste.com");

        when(pedidoFiscalSnapshotService.findByPedidoId(44L))
                .thenReturn(Optional.of(snapshot));

        service.dispatchIfRequested(document);

        verify(mailService).sendText(
                eq("joao@teste.com"),
                eq("Nota fiscal do pedido #44"),
                contains("DANFE: https://danfe"),
                eq(null)
        );
        assertThat(document.getEmailDeliverySentAt()).isNotNull();
    }

    @Test
    void dispatchIfRequestedSkipsWhenPreferenceIsDisabled() {
        final PedidoEntity pedido = new PedidoEntity();
        pedido.setId(45L);

        final FiscalDocumentEntity document = new FiscalDocumentEntity();
        document.setPedido(pedido);
        document.setStatus(FiscalDocumentStatus.AUTHORIZED);

        final PedidoFiscalSnapshotEntity snapshot = new PedidoFiscalSnapshotEntity();
        snapshot.setEmailDeliveryRequested(false);

        when(pedidoFiscalSnapshotService.findByPedidoId(45L))
                .thenReturn(Optional.of(snapshot));

        service.dispatchIfRequested(document);

        verify(mailService, never()).sendText(
                anyString(),
                anyString(),
                anyString(),
                any()
        );
        assertThat(document.getEmailDeliverySentAt()).isNull();
    }
}
