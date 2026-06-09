package br.com.lectek.copainsider.application.service.fiscal;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ItemPedidoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.PedidoFiscalSnapshotEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.PedidoFiscalSnapshotRepository;
import br.com.lectek.copainsider.domain.enums.ModoEntrega;
import br.com.lectek.copainsider.domain.enums.StatusPedido;
import br.com.lectek.copainsider.domain.enums.TipoPagamento;
import br.com.lectek.copainsider.domain.fiscal.FiscalDocumentModel;
import br.com.lectek.copainsider.domain.fiscal.FiscalEnvironment;
import br.com.lectek.copainsider.domain.fiscal.FiscalPrintChannel;
import br.com.lectek.copainsider.domain.fiscal.FiscalProvider;
import java.math.BigDecimal;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoFiscalSnapshotServiceTest {

    @Mock
    private PedidoFiscalSnapshotRepository repository;

    @Mock
    private FiscalEmitterConfigService fiscalEmitterConfigService;

    private PedidoFiscalSnapshotService service;

    @BeforeEach
    void setUp() {
        service = new PedidoFiscalSnapshotService(
                repository,
                fiscalEmitterConfigService,
                new com.fasterxml.jackson.databind.ObjectMapper()
        );
    }

    @Test
    void capturePersistsImmutableSnapshotWithProductFiscalData() {
        PedidoEntity pedido = new PedidoEntity();
        pedido.setId(10L);
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        pedido.setTipoPagamento(TipoPagamento.PIX);
        pedido.setModoEntrega(ModoEntrega.ENTREGA);
        pedido.setTotal(new BigDecimal("39.90"));
        ClienteEntity cliente = new ClienteEntity();
        cliente.setNome("Joao da Silva");
        cliente.setCpf("12345678901");
        cliente.setEmail("cliente@teste.com");
        pedido.setCliente(cliente);

        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(77L);
        produto.setNome("Dipirona 500mg");
        produto.setCodigoBarras("7891234567890");
        produto.setFiscalNcm("30049069");
        produto.setFiscalCest("1300100");
        produto.setFiscalCfop("5102");
        produto.setFiscalOrigem(0);
        produto.setFiscalCsosn("102");
        produto.setFiscalPisCst("01");
        produto.setFiscalCofinsCst("01");

        ItemPedidoEntity item = new ItemPedidoEntity();
        item.setProduto(produto);
        item.setQuantidade(2);
        item.setSubtotal(new BigDecimal("39.90"));
        pedido.addItem(item);

        when(repository.findByPedidoId(10L)).thenReturn(Optional.empty());
        when(repository.save(any(PedidoFiscalSnapshotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(fiscalEmitterConfigService.load(FiscalProvider.FOCUS_NFE))
                .thenReturn(new FiscalEmitterConfigService.FiscalEmitterConfig(
                        FiscalProvider.FOCUS_NFE,
                        true,
                        FiscalEnvironment.PRODUCAO,
                        "Rede Mais Farma LTDA",
                        "Rede Mais Farma",
                        "12345678000199",
                        "123456789",
                        "",
                        null,
                        "https://api.focusnfe.com.br",
                        "",
                        1,
                        1,
                        true,
                        "000001",
                        true,
                        false
                ));

        PedidoFiscalSnapshotEntity snapshot = service.capture(
                pedido,
                new PedidoFiscalSnapshotService.SnapshotRequest(
                        "CHECKOUT_WEB",
                        "Joao da Silva",
                        "123.456.789-01",
                        "cliente@teste.com",
                        "(85) 99999-0000",
                        "Rua A, 100",
                        BigDecimal.ZERO
                )
        );

        ArgumentCaptor<PedidoFiscalSnapshotEntity> captor =
                ArgumentCaptor.forClass(PedidoFiscalSnapshotEntity.class);
        verify(repository).save(captor.capture());
        PedidoFiscalSnapshotEntity persisted = captor.getValue();

        Assertions.assertThat(snapshot.getSuggestedDocumentModel())
                .isEqualTo(FiscalDocumentModel.NFCE_65);
        Assertions.assertThat(snapshot.getPrintChannel())
                .isEqualTo(FiscalPrintChannel.WITH_DELIVERY);
        Assertions.assertThat(snapshot.isEmailDeliveryRequested()).isFalse();
        Assertions.assertThat(persisted.getIssuerCnpj())
                .isEqualTo("12345678000199");
        Assertions.assertThat(persisted.getPayloadJson())
                .contains("\"pedidoId\":10")
                .contains("\"ncm\":\"30049069\"")
                .contains("\"cfop\":\"5102\"")
                .contains("\"valorUnitario\":19.95");
    }

    @Test
    void updateDeliveryPreferencesStoresEmailPreference() {
        final PedidoEntity pedido = new PedidoEntity();
        pedido.setId(11L);
        final PedidoFiscalSnapshotEntity snapshot = new PedidoFiscalSnapshotEntity();
        snapshot.setPedido(pedido);
        snapshot.setRecipientEmail("cliente@teste.com");
        snapshot.setPrintChannel(FiscalPrintChannel.IMMEDIATE);

        when(repository.findByPedidoId(11L)).thenReturn(Optional.of(snapshot));
        when(repository.save(any(PedidoFiscalSnapshotEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        final PedidoFiscalSnapshotEntity updated = service.updateDeliveryPreferences(
                11L,
                new PedidoFiscalSnapshotService.UpdateDeliveryPreferencesRequest(
                        FiscalPrintChannel.WITH_DELIVERY,
                        true,
                        "nota@teste.com"
                )
        );

        Assertions.assertThat(updated.isEmailDeliveryRequested()).isTrue();
        Assertions.assertThat(updated.getEmailDeliveryAddress())
                .isEqualTo("nota@teste.com");
        Assertions.assertThat(updated.getPrintChannel())
                .isEqualTo(FiscalPrintChannel.WITH_DELIVERY);
    }
}
