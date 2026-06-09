package br.com.redemaisfarma.application.service.fiscal;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.FiscalDocumentEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.FiscalEmitterConfigEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoFiscalSnapshotEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import br.com.redemaisfarma.domain.fiscal.FiscalDocumentModel;
import br.com.redemaisfarma.domain.fiscal.FiscalDocumentStatus;
import br.com.redemaisfarma.domain.fiscal.FiscalEnvironment;
import br.com.redemaisfarma.domain.fiscal.FiscalProvider;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FiscalOrderEmissionServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private PedidoFiscalSnapshotService pedidoFiscalSnapshotService;

    @Mock
    private FiscalEmitterConfigService fiscalEmitterConfigService;

    @Mock
    private FiscalDocumentService fiscalDocumentService;

    @Mock
    private FocusFiscalPayloadBuilder focusFiscalPayloadBuilder;

    private FiscalOrderEmissionService service;

    @BeforeEach
    void setUp() {
        service = new FiscalOrderEmissionService(
                pedidoRepository,
                pedidoFiscalSnapshotService,
                fiscalEmitterConfigService,
                fiscalDocumentService,
                focusFiscalPayloadBuilder
        );
    }

    @Test
    void processPaidOrderCreatesDraftAndSubmitsDocument() {
        final PedidoEntity pedido = new PedidoEntity();
        pedido.setId(11L);
        pedido.setStatus(StatusPedido.PAGO);

        final PedidoFiscalSnapshotEntity snapshot = new PedidoFiscalSnapshotEntity();
        snapshot.setIssuerCnpj("12345678000199");
        snapshot.setRecipientDocument("12345678901");
        snapshot.setTotalAmount(new BigDecimal("39.90"));
        snapshot.setSuggestedDocumentModel(FiscalDocumentModel.NFCE_65);

        final FiscalEmitterConfigEntity config = new FiscalEmitterConfigEntity();
        config.setProvider(FiscalProvider.FOCUS_NFE);
        config.setEnvironment(FiscalEnvironment.PRODUCAO);

        final FiscalDocumentEntity draft = new FiscalDocumentEntity();
        draft.setId(90L);
        draft.setStatus(FiscalDocumentStatus.DRAFT);

        final FiscalDocumentEntity submitted = new FiscalDocumentEntity();
        submitted.setId(90L);
        submitted.setStatus(FiscalDocumentStatus.SUBMITTED);

        when(pedidoRepository.findById(11L)).thenReturn(Optional.of(pedido));
        when(fiscalEmitterConfigService.requireEnabledEntity(FiscalProvider.FOCUS_NFE))
                .thenReturn(config);
        when(pedidoFiscalSnapshotService.captureIfMissing(pedido, "CAIXA"))
                .thenReturn(snapshot);
        when(fiscalDocumentService.findLatestByPedidoId(11L))
                .thenReturn(Optional.empty());
        when(fiscalDocumentService.createDraft(any()))
                .thenReturn(draft);
        when(focusFiscalPayloadBuilder.build(snapshot, config))
                .thenReturn("{\"payload\":true}");
        when(fiscalDocumentService.submit(90L, "{\"payload\":true}"))
                .thenReturn(submitted);

        final Optional<FiscalDocumentEntity> result = service.processPaidOrder(
                11L,
                "CAIXA"
        );

        Assertions.assertThat(result)
                .containsSame(submitted);
        final ArgumentCaptor<FiscalDocumentService.CreateDraftInput> captor =
                ArgumentCaptor.forClass(FiscalDocumentService.CreateDraftInput.class);
        verify(fiscalDocumentService).createDraft(captor.capture());
        Assertions.assertThat(captor.getValue().pedidoId()).isEqualTo(11L);
        Assertions.assertThat(captor.getValue().model())
                .isEqualTo(FiscalDocumentModel.NFCE_65);
        verify(fiscalDocumentService).submit(90L, "{\"payload\":true}");
    }

    @Test
    void processPaidOrderRecordsErrorWhenPayloadBuilderFails() {
        final PedidoEntity pedido = new PedidoEntity();
        pedido.setId(12L);
        pedido.setStatus(StatusPedido.PAGO);

        final PedidoFiscalSnapshotEntity snapshot = new PedidoFiscalSnapshotEntity();
        snapshot.setSuggestedDocumentModel(FiscalDocumentModel.NFCE_65);

        final FiscalEmitterConfigEntity config = new FiscalEmitterConfigEntity();
        config.setProvider(FiscalProvider.FOCUS_NFE);

        final FiscalDocumentEntity existing = new FiscalDocumentEntity();
        existing.setId(91L);
        existing.setStatus(FiscalDocumentStatus.ERROR);

        final FiscalDocumentEntity errored = new FiscalDocumentEntity();
        errored.setId(91L);
        errored.setStatus(FiscalDocumentStatus.ERROR);

        when(pedidoRepository.findById(12L)).thenReturn(Optional.of(pedido));
        when(fiscalEmitterConfigService.requireEnabledEntity(FiscalProvider.FOCUS_NFE))
                .thenReturn(config);
        when(pedidoFiscalSnapshotService.captureIfMissing(pedido, "CAIXA"))
                .thenReturn(snapshot);
        when(fiscalDocumentService.findLatestByPedidoId(12L))
                .thenReturn(Optional.of(existing));
        when(focusFiscalPayloadBuilder.build(snapshot, config))
                .thenThrow(new IllegalArgumentException("CFOP obrigatorio"));
        when(fiscalDocumentService.recordError(91L, "CFOP obrigatorio", null))
                .thenReturn(errored);

        final Optional<FiscalDocumentEntity> result = service.processPaidOrder(
                12L,
                "CAIXA"
        );

        Assertions.assertThat(result)
                .containsSame(errored);
        verify(fiscalDocumentService, never()).createDraft(any());
        verify(fiscalDocumentService, never()).submit(eq(91L), any());
        verify(fiscalDocumentService).recordError(91L, "CFOP obrigatorio", null);
    }

    @Test
    void processPaidOrderIgnoresPedidoNotPaid() {
        final PedidoEntity pedido = new PedidoEntity();
        pedido.setId(13L);
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);

        when(pedidoRepository.findById(13L)).thenReturn(Optional.of(pedido));

        final Optional<FiscalDocumentEntity> result = service.processPaidOrder(
                13L,
                "CHECKOUT"
        );

        Assertions.assertThat(result).isEmpty();
        verify(pedidoFiscalSnapshotService, never()).captureIfMissing(any(), any());
        verify(fiscalDocumentService, never()).createDraft(any());
    }
}
