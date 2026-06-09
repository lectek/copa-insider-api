package br.com.redemaisfarma.application.service.fiscal;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.FiscalDocumentEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoFiscalSnapshotEntity;
import br.com.redemaisfarma.domain.enums.ModoEntrega;
import br.com.redemaisfarma.domain.fiscal.FiscalDocumentModel;
import br.com.redemaisfarma.domain.fiscal.FiscalDocumentStatus;
import br.com.redemaisfarma.domain.fiscal.FiscalPrintChannel;
import java.time.LocalDateTime;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoFiscalPresentationServiceTest {

    @Mock
    private PedidoFiscalSnapshotService pedidoFiscalSnapshotService;

    @Mock
    private FiscalDocumentService fiscalDocumentService;

    private PedidoFiscalPresentationService service;

    @BeforeEach
    void setUp() {
        service = new PedidoFiscalPresentationService(
                pedidoFiscalSnapshotService,
                fiscalDocumentService
        );
    }

    @Test
    void buildUsesSnapshotAndDocumentDetails() {
        final PedidoEntity pedido = new PedidoEntity();
        pedido.setId(55L);
        pedido.setModoEntrega(ModoEntrega.ENTREGA);

        final ClienteEntity cliente = new ClienteEntity();
        cliente.setEmail("cliente@teste.com");
        pedido.setCliente(cliente);

        final PedidoFiscalSnapshotEntity snapshot = new PedidoFiscalSnapshotEntity();
        snapshot.setPedido(pedido);
        snapshot.setPrintChannel(FiscalPrintChannel.WITH_DELIVERY);
        snapshot.setEmailDeliveryRequested(true);
        snapshot.setEmailDeliveryAddress("nota@teste.com");
        snapshot.setSuggestedDocumentModel(FiscalDocumentModel.NFCE_65);

        final FiscalDocumentEntity document = new FiscalDocumentEntity();
        document.setPedido(pedido);
        document.setStatus(FiscalDocumentStatus.AUTHORIZED);
        document.setModel(FiscalDocumentModel.NFCE_65);
        document.setAccessKey("123456");
        document.setDanfeStoragePath("https://danfe");
        document.setXmlStoragePath("https://xml");
        document.setEmailDeliverySentAt(LocalDateTime.of(2026, 3, 11, 9, 30));

        when(pedidoFiscalSnapshotService.findByPedidoId(55L))
                .thenReturn(Optional.of(snapshot));
        when(fiscalDocumentService.findLatestByPedidoId(55L))
                .thenReturn(Optional.of(document));

        final PedidoFiscalPresentationService.PedidoFiscalView view =
                service.build(pedido);

        Assertions.assertThat(view.printChannel())
                .isEqualTo(FiscalPrintChannel.WITH_DELIVERY);
        Assertions.assertThat(view.documentStatus())
                .isEqualTo(FiscalDocumentStatus.AUTHORIZED);
        Assertions.assertThat(view.documentModelLabel())
                .isEqualTo("NFC-e 65");
        Assertions.assertThat(view.emailStatusLabel())
                .contains("nota@teste.com");
        Assertions.assertThat(view.danfeUrl()).isEqualTo("https://danfe");
    }

    @Test
    void buildFallsBackToPickupPrintingWhenSnapshotIsMissing() {
        final PedidoEntity pedido = new PedidoEntity();
        pedido.setId(56L);
        pedido.setModoEntrega(ModoEntrega.RETIRADA);

        when(pedidoFiscalSnapshotService.findByPedidoId(56L))
                .thenReturn(Optional.empty());
        when(fiscalDocumentService.findLatestByPedidoId(56L))
                .thenReturn(Optional.empty());

        final PedidoFiscalPresentationService.PedidoFiscalView view =
                service.build(pedido);

        Assertions.assertThat(view.printChannel())
                .isEqualTo(FiscalPrintChannel.IMMEDIATE);
        Assertions.assertThat(view.documentStatusLabel())
                .isEqualTo("Aguardando emissao");
        Assertions.assertThat(view.emailRequested()).isFalse();
    }
}
