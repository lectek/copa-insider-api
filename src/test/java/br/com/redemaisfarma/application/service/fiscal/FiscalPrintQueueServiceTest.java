package br.com.redemaisfarma.application.service.fiscal;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.FiscalDocumentEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.FiscalPrintJobEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.FiscalPrintStationEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoFiscalSnapshotEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.FiscalPrintJobEventRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.FiscalPrintJobRepository;
import br.com.redemaisfarma.domain.fiscal.FiscalDocumentStatus;
import br.com.redemaisfarma.domain.fiscal.FiscalPrintChannel;
import br.com.redemaisfarma.domain.fiscal.FiscalPrintJobStatus;
import br.com.redemaisfarma.domain.fiscal.FiscalPrintJobType;
import br.com.redemaisfarma.domain.fiscal.FiscalPrintStationRole;
import java.util.Optional;
import java.util.List;
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
class FiscalPrintQueueServiceTest {

    @Mock
    private FiscalPrintJobRepository jobRepository;

    @Mock
    private FiscalPrintJobEventRepository eventRepository;

    @Mock
    private PedidoFiscalSnapshotService pedidoFiscalSnapshotService;

    @Mock
    private FiscalPrintStationService fiscalPrintStationService;

    private FiscalPrintQueueService service;

    @BeforeEach
    void setUp() {
        service = new FiscalPrintQueueService(
                jobRepository,
                eventRepository,
                pedidoFiscalSnapshotService,
                fiscalPrintStationService
        );
    }

    @Test
    void syncWithDocumentCreatesWaitingJobForImmediatePrinting() {
        final PedidoEntity pedido = pedido(11L);
        final FiscalDocumentEntity document = document(5L, pedido, FiscalDocumentStatus.SUBMITTED);
        final PedidoFiscalSnapshotEntity snapshot = snapshot(pedido, FiscalPrintChannel.IMMEDIATE, "CAIXA_VENDA_RAPIDA");

        final FiscalPrintStationEntity station = new FiscalPrintStationEntity();
        station.setDisplayName("Caixa principal");

        when(pedidoFiscalSnapshotService.findByPedidoId(11L))
                .thenReturn(Optional.of(snapshot));
        when(jobRepository.findTopByFiscalDocumentIdAndJobTypeOrderByCreatedAtDesc(
                5L,
                FiscalPrintJobType.DANFE_IMMEDIATE
        )).thenReturn(Optional.empty());
        when(fiscalPrintStationService.findBestStationEntity(FiscalPrintChannel.IMMEDIATE))
                .thenReturn(Optional.of(station));
        when(jobRepository.save(any(FiscalPrintJobEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        final FiscalPrintJobEntity job = service.syncWithDocument(document).orElseThrow();

        final ArgumentCaptor<FiscalPrintJobEntity> captor =
                ArgumentCaptor.forClass(FiscalPrintJobEntity.class);
        verify(jobRepository).save(captor.capture());
        final FiscalPrintJobEntity persisted = captor.getValue();

        Assertions.assertThat(job.getStatus())
                .isEqualTo(FiscalPrintJobStatus.WAITING_DOCUMENT);
        Assertions.assertThat(persisted.getPriority()).isEqualTo(100);
        Assertions.assertThat(persisted.getStation()).isEqualTo(station);
        Assertions.assertThat(persisted.getJobType())
                .isEqualTo(FiscalPrintJobType.DANFE_IMMEDIATE);
    }

    @Test
    void syncWithDocumentPromotesWaitingJobToReadyWhenAuthorized() {
        final PedidoEntity pedido = pedido(12L);
        final FiscalDocumentEntity document = document(6L, pedido, FiscalDocumentStatus.AUTHORIZED);
        final PedidoFiscalSnapshotEntity snapshot = snapshot(pedido, FiscalPrintChannel.WITH_DELIVERY, "CHECKOUT_WEB");

        final FiscalPrintJobEntity existing = new FiscalPrintJobEntity();
        existing.setFiscalDocument(document);
        existing.setPedido(pedido);
        existing.setJobType(FiscalPrintJobType.DANFE_WITH_DELIVERY);
        existing.setPrintChannel(FiscalPrintChannel.WITH_DELIVERY);
        existing.setStatus(FiscalPrintJobStatus.WAITING_DOCUMENT);

        when(pedidoFiscalSnapshotService.findByPedidoId(12L))
                .thenReturn(Optional.of(snapshot));
        when(jobRepository.findTopByFiscalDocumentIdAndJobTypeOrderByCreatedAtDesc(
                6L,
                FiscalPrintJobType.DANFE_WITH_DELIVERY
        )).thenReturn(Optional.of(existing));
        when(fiscalPrintStationService.findBestStationEntity(FiscalPrintChannel.WITH_DELIVERY))
                .thenReturn(Optional.empty());
        when(jobRepository.save(any(FiscalPrintJobEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        final FiscalPrintJobEntity updated = service.syncWithDocument(document).orElseThrow();

        Assertions.assertThat(updated.getStatus())
                .isEqualTo(FiscalPrintJobStatus.READY);
    }

    @Test
    void requeueCreatesManualReprintJob() {
        final PedidoEntity pedido = pedido(13L);
        final FiscalDocumentEntity document = document(7L, pedido, FiscalDocumentStatus.AUTHORIZED);

        final FiscalPrintJobEntity original = new FiscalPrintJobEntity();
        original.setId(99L);
        original.setFiscalDocument(document);
        original.setPedido(pedido);
        original.setJobType(FiscalPrintJobType.DANFE_IMMEDIATE);
        original.setPrintChannel(FiscalPrintChannel.IMMEDIATE);
        original.setStatus(FiscalPrintJobStatus.PRINTED);
        original.setCopies(1);
        original.setSource("CAIXA_VENDA_RAPIDA");

        when(jobRepository.findById(99L)).thenReturn(Optional.of(original));
        when(jobRepository.save(any(FiscalPrintJobEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        final FiscalPrintJobEntity reprint = service.requeue(99L, "admin@teste");

        Assertions.assertThat(reprint.getJobType())
                .isEqualTo(FiscalPrintJobType.REPRINT);
        Assertions.assertThat(reprint.getStatus())
                .isEqualTo(FiscalPrintJobStatus.READY);
        Assertions.assertThat(reprint.getReprintOfJobId()).isEqualTo(99L);
        Assertions.assertThat(reprint.getCreatedBy()).isEqualTo("admin@teste");
    }

    @Test
    void claimNextReadyJobAssignsStationAndStartsPrinting() {
        final FiscalPrintStationEntity station = new FiscalPrintStationEntity();
        station.setId(3L);
        station.setCode("CAIXA-1");
        station.setDisplayName("Caixa principal");
        station.setRole(FiscalPrintStationRole.IMMEDIATE_ONLY);
        station.setActive(true);

        final PedidoEntity pedido = pedido(14L);
        final FiscalDocumentEntity document = document(8L, pedido, FiscalDocumentStatus.AUTHORIZED);
        document.setDanfeStoragePath("https://danfe");

        final FiscalPrintJobEntity readyJob = new FiscalPrintJobEntity();
        readyJob.setId(120L);
        readyJob.setFiscalDocument(document);
        readyJob.setPedido(pedido);
        readyJob.setJobType(FiscalPrintJobType.DANFE_IMMEDIATE);
        readyJob.setPrintChannel(FiscalPrintChannel.IMMEDIATE);
        readyJob.setStatus(FiscalPrintJobStatus.READY);
        readyJob.setPriority(100);

        when(fiscalPrintStationService.findActiveEntityByCode("CAIXA-1"))
                .thenReturn(Optional.of(station));
        when(jobRepository.findTopByStationIdAndStatusOrderByStartedAtDesc(
                3L,
                FiscalPrintJobStatus.PRINTING
        )).thenReturn(Optional.empty());
        when(jobRepository.findByStatusOrderByPriorityDescScheduledForAscCreatedAtAsc(
                FiscalPrintJobStatus.READY,
                org.springframework.data.domain.PageRequest.of(0, 120)
        )).thenReturn(List.of(readyJob));
        when(jobRepository.save(any(FiscalPrintJobEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(jobRepository.findById(120L)).thenReturn(Optional.of(readyJob));

        final FiscalPrintJobEntity claimed = service.claimNextReadyJob(
                "CAIXA-1",
                "agente-caixa"
        ).orElseThrow();

        Assertions.assertThat(claimed.getStatus())
                .isEqualTo(FiscalPrintJobStatus.PRINTING);
        Assertions.assertThat(claimed.getStation()).isEqualTo(station);
        Assertions.assertThat(claimed.getLastActor()).isEqualTo("agente-caixa");
    }

    @Test
    void markPrintedForStationRejectsJobFromAnotherStation() {
        final FiscalPrintStationEntity station = new FiscalPrintStationEntity();
        station.setId(3L);
        station.setCode("CAIXA-1");

        final PedidoEntity pedido = pedido(15L);
        final FiscalDocumentEntity document = document(
                9L,
                pedido,
                FiscalDocumentStatus.AUTHORIZED
        );
        final FiscalPrintJobEntity printingJob = new FiscalPrintJobEntity();
        printingJob.setId(121L);
        printingJob.setFiscalDocument(document);
        printingJob.setPedido(pedido);
        printingJob.setStation(station);
        printingJob.setStatus(FiscalPrintJobStatus.PRINTING);

        when(jobRepository.findById(121L)).thenReturn(Optional.of(printingJob));

        Assertions.assertThatThrownBy(() -> service.markPrintedForStation(
                        121L,
                        "CAIXA-2",
                        "PRINT_AGENT:CAIXA-2"
                ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nao pertence");
    }

    @Test
    void markFailedForStationAcceptsJobFromSameStation() {
        final FiscalPrintStationEntity station = new FiscalPrintStationEntity();
        station.setId(3L);
        station.setCode("CAIXA-1");

        final PedidoEntity pedido = pedido(16L);
        final FiscalDocumentEntity document = document(
                10L,
                pedido,
                FiscalDocumentStatus.AUTHORIZED
        );
        final FiscalPrintJobEntity printingJob = new FiscalPrintJobEntity();
        printingJob.setId(122L);
        printingJob.setFiscalDocument(document);
        printingJob.setPedido(pedido);
        printingJob.setStation(station);
        printingJob.setStatus(FiscalPrintJobStatus.PRINTING);

        when(jobRepository.findById(122L)).thenReturn(Optional.of(printingJob));
        when(jobRepository.save(any(FiscalPrintJobEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        final FiscalPrintJobEntity failed = service.markFailedForStation(
                122L,
                "CAIXA-1",
                "PRINT_AGENT:CAIXA-1",
                "sem papel"
        );

        Assertions.assertThat(failed.getStatus())
                .isEqualTo(FiscalPrintJobStatus.FAILED);
        Assertions.assertThat(failed.getErrorMessage()).isEqualTo("sem papel");
    }

    private PedidoEntity pedido(final Long id) {
        final PedidoEntity pedido = new PedidoEntity();
        pedido.setId(id);
        return pedido;
    }

    private FiscalDocumentEntity document(
            final Long id,
            final PedidoEntity pedido,
            final FiscalDocumentStatus status
    ) {
        final FiscalDocumentEntity document = new FiscalDocumentEntity();
        document.setId(id);
        document.setPedido(pedido);
        document.setStatus(status);
        return document;
    }

    private PedidoFiscalSnapshotEntity snapshot(
            final PedidoEntity pedido,
            final FiscalPrintChannel channel,
            final String source
    ) {
        final PedidoFiscalSnapshotEntity snapshot = new PedidoFiscalSnapshotEntity();
        snapshot.setPedido(pedido);
        snapshot.setPrintChannel(channel);
        snapshot.setSource(source);
        return snapshot;
    }
}
