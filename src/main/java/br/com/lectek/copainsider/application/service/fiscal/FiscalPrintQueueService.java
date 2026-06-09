package br.com.lectek.copainsider.application.service.fiscal;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.FiscalDocumentEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.FiscalPrintJobEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.FiscalPrintJobEventEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.FiscalPrintStationEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.PedidoFiscalSnapshotEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.FiscalPrintJobEventRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.FiscalPrintJobRepository;
import br.com.lectek.copainsider.domain.fiscal.FiscalPrintChannel;
import br.com.lectek.copainsider.domain.fiscal.FiscalPrintJobEventType;
import br.com.lectek.copainsider.domain.fiscal.FiscalPrintJobStatus;
import br.com.lectek.copainsider.domain.fiscal.FiscalPrintJobType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FiscalPrintQueueService {

    private static final String SYSTEM_ACTOR = "SYSTEM";
    private static final int DEFAULT_LIMIT = 60;
    private static final int MAX_LIMIT = 200;
    private static final int CLAIM_SCAN_LIMIT = 120;
    private static final int IMMEDIATE_PRIORITY = 100;
    private static final int DELIVERY_PRIORITY = 50;
    private static final int REPRINT_PRIORITY = 120;

    private final FiscalPrintJobRepository jobRepository;
    private final FiscalPrintJobEventRepository eventRepository;
    private final PedidoFiscalSnapshotService pedidoFiscalSnapshotService;
    private final FiscalPrintStationService fiscalPrintStationService;

    public FiscalPrintQueueService(
            final FiscalPrintJobRepository fiscalPrintJobRepository,
            final FiscalPrintJobEventRepository fiscalPrintJobEventRepository,
            final PedidoFiscalSnapshotService pedidoFiscalSnapshotServiceValue,
            final FiscalPrintStationService fiscalPrintStationServiceValue
    ) {
        this.jobRepository = fiscalPrintJobRepository;
        this.eventRepository = fiscalPrintJobEventRepository;
        this.pedidoFiscalSnapshotService = pedidoFiscalSnapshotServiceValue;
        this.fiscalPrintStationService = fiscalPrintStationServiceValue;
    }

    @Transactional(readOnly = true)
    public List<PrintJobSummary> listJobSummaries(
            final FiscalPrintJobStatus status,
            final Long pedidoId,
            final int limit
    ) {
        final int normalizedLimit = normalizeLimit(limit);
        List<FiscalPrintJobEntity> jobs = pedidoId == null
                ? jobRepository.findAllByOrderByUpdatedAtDesc(
                        PageRequest.of(0, normalizedLimit)
                )
                : jobRepository.findByPedidoIdOrderByUpdatedAtDesc(
                        pedidoId,
                        PageRequest.of(0, normalizedLimit)
                );
        if (status != null) {
            jobs = jobs.stream()
                    .filter(job -> job.getStatus() == status)
                    .toList();
        }
        return jobs.stream().map(this::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public List<PrintJobEventSummary> listEventSummaries(
            final Long pedidoId,
            final int limit
    ) {
        final int normalizedLimit = normalizeLimit(limit);
        final List<FiscalPrintJobEventEntity> events = pedidoId == null
                ? eventRepository.findAllByOrderByCreatedAtDesc(
                        PageRequest.of(0, normalizedLimit)
                )
                : eventRepository.findByPrintJobPedidoIdOrderByCreatedAtDesc(
                        pedidoId,
                        PageRequest.of(0, normalizedLimit)
                );
        return events.stream().map(this::toEventSummary).toList();
    }

    @Transactional
    public Optional<FiscalPrintJobEntity> syncWithDocument(
            final FiscalDocumentEntity document
    ) {
        if (document == null
                || document.getId() == null
                || document.getPedido() == null
                || document.getPedido().getId() == null) {
            return Optional.empty();
        }

        final Optional<PedidoFiscalSnapshotEntity> snapshotOpt =
                pedidoFiscalSnapshotService.findByPedidoId(
                        document.getPedido().getId()
                );
        if (snapshotOpt.isEmpty()) {
            return Optional.empty();
        }

        final PedidoFiscalSnapshotEntity snapshot = snapshotOpt.get();
        if (snapshot.getPrintChannel() == null
                || snapshot.getPrintChannel() == FiscalPrintChannel.NONE) {
            return Optional.empty();
        }

        final FiscalPrintJobType jobType = resolveJobType(
                snapshot.getPrintChannel()
        );
        final Optional<FiscalPrintJobEntity> latest =
                jobRepository.findTopByFiscalDocumentIdAndJobTypeOrderByCreatedAtDesc(
                        document.getId(),
                        jobType
                );
        if (latest.isEmpty()) {
            return Optional.of(createPrimaryJob(document, snapshot, jobType));
        }
        return Optional.of(updatePrimaryJob(latest.get(), document));
    }

    @Transactional
    public FiscalPrintJobEntity hold(
            final Long jobId,
            final String actor,
            final String message
    ) {
        final FiscalPrintJobEntity job = requireJob(jobId);
        final FiscalPrintJobStatus before = job.getStatus();
        if (before == FiscalPrintJobStatus.PRINTED
                || before == FiscalPrintJobStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Nao e possivel segurar um job ja finalizado."
            );
        }
        job.setStatus(FiscalPrintJobStatus.HELD);
        job.setLastActor(normalizeActor(actor));
        final FiscalPrintJobEntity saved = jobRepository.save(job);
        appendEvent(
                saved,
                FiscalPrintJobEventType.HELD,
                before,
                saved.getStatus(),
                blankToNull(message),
                normalizeActor(actor)
        );
        return saved;
    }

    @Transactional
    public FiscalPrintJobEntity release(
            final Long jobId,
            final String actor
    ) {
        final FiscalPrintJobEntity job = requireJob(jobId);
        if (job.getStatus() != FiscalPrintJobStatus.HELD) {
            throw new IllegalStateException(
                    "Somente jobs em espera podem ser liberados."
            );
        }
        final FiscalPrintJobStatus before = job.getStatus();
        final FiscalPrintJobStatus target =
                resolveStatusFromDocument(job.getFiscalDocument());
        job.setStatus(target);
        job.setLastActor(normalizeActor(actor));
        final FiscalPrintJobEntity saved = jobRepository.save(job);
        appendEvent(
                saved,
                FiscalPrintJobEventType.RELEASED,
                before,
                saved.getStatus(),
                "Job liberado para a fila.",
                normalizeActor(actor)
        );
        return saved;
    }

    @Transactional
    public FiscalPrintJobEntity cancel(
            final Long jobId,
            final String actor,
            final String reason
    ) {
        final FiscalPrintJobEntity job = requireJob(jobId);
        if (job.getStatus() == FiscalPrintJobStatus.PRINTED) {
            throw new IllegalStateException(
                    "Nao e possivel cancelar um job ja impresso."
            );
        }
        final FiscalPrintJobStatus before = job.getStatus();
        job.setStatus(FiscalPrintJobStatus.CANCELLED);
        job.setCancelledAt(LocalDateTime.now());
        job.setCancelReason(blankToNull(reason));
        job.setLastActor(normalizeActor(actor));
        final FiscalPrintJobEntity saved = jobRepository.save(job);
        appendEvent(
                saved,
                FiscalPrintJobEventType.CANCELLED,
                before,
                saved.getStatus(),
                blankToNull(reason),
                normalizeActor(actor)
        );
        return saved;
    }

    @Transactional
    public FiscalPrintJobEntity assignStation(
            final Long jobId,
            final Long stationId,
            final String actor
    ) {
        final FiscalPrintJobEntity job = requireJob(jobId);
        final FiscalPrintStationEntity station = fiscalPrintStationService
                .findEntity(stationId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Estacao de impressao nao encontrada."
                ));
        final FiscalPrintJobStatus before = job.getStatus();
        job.setStation(station);
        job.setLastActor(normalizeActor(actor));
        final FiscalPrintJobEntity saved = jobRepository.save(job);
        appendEvent(
                saved,
                FiscalPrintJobEventType.STATION_ASSIGNED,
                before,
                saved.getStatus(),
                "Estacao atribuida: " + station.getDisplayName(),
                normalizeActor(actor)
        );
        return saved;
    }

    @Transactional
    public FiscalPrintJobEntity startPrinting(
            final Long jobId,
            final String actor
    ) {
        final FiscalPrintJobEntity job = requireJob(jobId);
        if (job.getStatus() != FiscalPrintJobStatus.READY) {
            throw new IllegalStateException(
                    "Somente jobs prontos podem iniciar impressao."
            );
        }
        final FiscalPrintJobStatus before = job.getStatus();
        job.setStatus(FiscalPrintJobStatus.PRINTING);
        job.setStartedAt(LocalDateTime.now());
        job.setLastActor(normalizeActor(actor));
        final FiscalPrintJobEntity saved = jobRepository.save(job);
        appendEvent(
                saved,
                FiscalPrintJobEventType.PRINTING_STARTED,
                before,
                saved.getStatus(),
                "Impressao iniciada.",
                normalizeActor(actor)
        );
        return saved;
    }

    @Transactional
    public FiscalPrintJobEntity markPrinted(
            final Long jobId,
            final String actor
    ) {
        final FiscalPrintJobEntity job = requireJob(jobId);
        if (job.getStatus() != FiscalPrintJobStatus.PRINTING
                && job.getStatus() != FiscalPrintJobStatus.READY) {
            throw new IllegalStateException(
                    "Somente jobs prontos ou em impressao podem ser concluidos."
            );
        }
        final FiscalPrintJobStatus before = job.getStatus();
        job.setStatus(FiscalPrintJobStatus.PRINTED);
        job.setCompletedAt(LocalDateTime.now());
        if (job.getStartedAt() == null) {
            job.setStartedAt(LocalDateTime.now());
        }
        job.setLastActor(normalizeActor(actor));
        final FiscalPrintJobEntity saved = jobRepository.save(job);
        appendEvent(
                saved,
                FiscalPrintJobEventType.PRINTED,
                before,
                saved.getStatus(),
                "Impressao concluida.",
                normalizeActor(actor)
        );
        return saved;
    }

    @Transactional
    public FiscalPrintJobEntity markPrintedForStation(
            final Long jobId,
            final String stationCode,
            final String actor
    ) {
        final FiscalPrintJobEntity job = requireJob(jobId);
        requireStationOwnership(job, stationCode);
        return markPrinted(jobId, actor);
    }

    @Transactional
    public FiscalPrintJobEntity markFailed(
            final Long jobId,
            final String actor,
            final String message
    ) {
        final FiscalPrintJobEntity job = requireJob(jobId);
        if (job.getStatus() == FiscalPrintJobStatus.PRINTED
                || job.getStatus() == FiscalPrintJobStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Nao e possivel falhar um job finalizado."
            );
        }
        final FiscalPrintJobStatus before = job.getStatus();
        job.setStatus(FiscalPrintJobStatus.FAILED);
        job.setErrorMessage(blankToNull(message));
        job.setLastActor(normalizeActor(actor));
        final FiscalPrintJobEntity saved = jobRepository.save(job);
        appendEvent(
                saved,
                FiscalPrintJobEventType.FAILED,
                before,
                saved.getStatus(),
                blankToNull(message),
                normalizeActor(actor)
        );
        return saved;
    }

    @Transactional
    public FiscalPrintJobEntity markFailedForStation(
            final Long jobId,
            final String stationCode,
            final String actor,
            final String message
    ) {
        final FiscalPrintJobEntity job = requireJob(jobId);
        requireStationOwnership(job, stationCode);
        return markFailed(jobId, actor, message);
    }

    @Transactional
    public FiscalPrintJobEntity requeue(
            final Long jobId,
            final String actor
    ) {
        final FiscalPrintJobEntity original = requireJob(jobId);
        final FiscalPrintJobEntity entity = new FiscalPrintJobEntity();
        entity.setFiscalDocument(original.getFiscalDocument());
        entity.setPedido(original.getPedido());
        entity.setStation(original.getStation());
        entity.setJobType(FiscalPrintJobType.REPRINT);
        entity.setStatus(resolveStatusFromDocument(original.getFiscalDocument()));
        entity.setPrintChannel(original.getPrintChannel());
        entity.setPriority(REPRINT_PRIORITY);
        entity.setCopies(original.getCopies());
        entity.setSource(original.getSource());
        entity.setScheduledFor(LocalDateTime.now());
        entity.setReprintOfJobId(original.getId());
        entity.setCreatedBy(normalizeActor(actor));
        entity.setLastActor(normalizeActor(actor));
        final FiscalPrintJobEntity saved = jobRepository.save(entity);
        appendEvent(
                saved,
                FiscalPrintJobEventType.REQUEUED,
                null,
                saved.getStatus(),
                "Reimpressao criada.",
                normalizeActor(actor)
        );
        return saved;
    }

    @Transactional
    public Optional<FiscalPrintJobEntity> claimNextReadyJob(
            final String stationCode,
            final String actor
    ) {
        final var station = fiscalPrintStationService.findActiveEntityByCode(
                stationCode
        ).orElseThrow(() -> new NoSuchElementException(
                "Estacao de impressao ativa nao encontrada."
        ));

        final Optional<FiscalPrintJobEntity> currentPrinting =
                jobRepository.findTopByStationIdAndStatusOrderByStartedAtDesc(
                        station.getId(),
                        FiscalPrintJobStatus.PRINTING
                );
        if (currentPrinting.isPresent()) {
            return currentPrinting;
        }

        final List<FiscalPrintJobEntity> candidates =
                jobRepository.findByStatusOrderByPriorityDescScheduledForAscCreatedAtAsc(
                        FiscalPrintJobStatus.READY,
                        PageRequest.of(0, CLAIM_SCAN_LIMIT)
                );
        for (FiscalPrintJobEntity candidate : candidates) {
            if (!isCandidateForStation(candidate, station)) {
                continue;
            }
            if (candidate.getStation() == null) {
                candidate.setStation(station);
            }
            candidate.setLastActor(normalizeActor(actor));
            final FiscalPrintJobEntity reserved = jobRepository.save(candidate);
            return Optional.of(startPrinting(reserved.getId(), actor));
        }
        return Optional.empty();
    }

    private FiscalPrintJobEntity createPrimaryJob(
            final FiscalDocumentEntity document,
            final PedidoFiscalSnapshotEntity snapshot,
            final FiscalPrintJobType jobType
    ) {
        final FiscalPrintJobEntity entity = new FiscalPrintJobEntity();
        entity.setFiscalDocument(document);
        entity.setPedido(document.getPedido());
        entity.setStation(resolveAutoStation(snapshot.getPrintChannel()));
        entity.setJobType(jobType);
        entity.setStatus(resolveStatusFromDocument(document));
        entity.setPrintChannel(snapshot.getPrintChannel());
        entity.setPriority(resolvePriority(snapshot));
        entity.setCopies(1);
        entity.setSource(snapshot.getSource());
        entity.setScheduledFor(resolveSchedule(document));
        entity.setCreatedBy(SYSTEM_ACTOR);
        entity.setLastActor(SYSTEM_ACTOR);
        entity.setErrorMessage(blankToNull(document.getErrorMessage()));
        final FiscalPrintJobEntity saved = jobRepository.save(entity);
        appendEvent(
                saved,
                FiscalPrintJobEventType.CREATED,
                null,
                saved.getStatus(),
                "Job fiscal criado automaticamente.",
                SYSTEM_ACTOR
        );
        return saved;
    }

    private FiscalPrintJobEntity updatePrimaryJob(
            final FiscalPrintJobEntity job,
            final FiscalDocumentEntity document
    ) {
        final FiscalPrintJobStatus before = job.getStatus();
        final FiscalPrintJobStatus target = resolveStatusFromDocument(document);
        boolean changed = false;

        if (job.getStatus() != FiscalPrintJobStatus.PRINTED
                && job.getStatus() != FiscalPrintJobStatus.CANCELLED) {
            final FiscalPrintJobStatus nextStatus = resolveNextStatus(
                    job.getStatus(),
                    target
            );
            if (nextStatus != job.getStatus()) {
                job.setStatus(nextStatus);
                changed = true;
            }
        }
        if (job.getStation() == null) {
            final FiscalPrintStationEntity station = resolveAutoStation(
                    job.getPrintChannel()
            );
            if (station != null) {
                job.setStation(station);
                changed = true;
            }
        }
        if (job.getScheduledFor() == null) {
            job.setScheduledFor(resolveSchedule(document));
            changed = true;
        }
        final String normalizedError = blankToNull(document.getErrorMessage());
        if (!sameText(job.getErrorMessage(), normalizedError)) {
            job.setErrorMessage(normalizedError);
            changed = true;
        }
        if (job.getStatus() == FiscalPrintJobStatus.CANCELLED
                && job.getCancelledAt() == null) {
            job.setCancelledAt(LocalDateTime.now());
            changed = true;
        }
        if (!changed) {
            return job;
        }
        job.setLastActor(SYSTEM_ACTOR);
        final FiscalPrintJobEntity saved = jobRepository.save(job);
        appendEvent(
                saved,
                FiscalPrintJobEventType.DOCUMENT_SYNC,
                before,
                saved.getStatus(),
                "Fila sincronizada com o status fiscal.",
                SYSTEM_ACTOR
        );
        return saved;
    }

    private FiscalPrintJobStatus resolveNextStatus(
            final FiscalPrintJobStatus current,
            final FiscalPrintJobStatus target
    ) {
        if (target == FiscalPrintJobStatus.CANCELLED
                || target == FiscalPrintJobStatus.FAILED) {
            return target;
        }
        if (current == FiscalPrintJobStatus.HELD
                && (target == FiscalPrintJobStatus.READY
                || target == FiscalPrintJobStatus.WAITING_DOCUMENT)) {
            return FiscalPrintJobStatus.HELD;
        }
        if (current == FiscalPrintJobStatus.PRINTING
                && (target == FiscalPrintJobStatus.READY
                || target == FiscalPrintJobStatus.WAITING_DOCUMENT)) {
            return FiscalPrintJobStatus.PRINTING;
        }
        return target;
    }

    private boolean isCandidateForStation(
            final FiscalPrintJobEntity job,
            final FiscalPrintStationEntity station
    ) {
        if (job.getFiscalDocument() == null
                || job.getFiscalDocument().getDanfeStoragePath() == null
                || job.getFiscalDocument().getDanfeStoragePath().isBlank()) {
            return false;
        }
        if (job.getStation() != null) {
            return station.getId() != null
                    && station.getId().equals(job.getStation().getId());
        }
        return supports(station.getRole(), job.getPrintChannel());
    }

    private boolean supports(
            final br.com.lectek.copainsider.domain.fiscal.FiscalPrintStationRole role,
            final FiscalPrintChannel channel
    ) {
        if (role == null
                || role == br.com.lectek.copainsider.domain.fiscal.FiscalPrintStationRole.FLEX) {
            return true;
        }
        if (channel == FiscalPrintChannel.IMMEDIATE) {
            return role
                    == br.com.lectek.copainsider.domain.fiscal.FiscalPrintStationRole.IMMEDIATE_ONLY;
        }
        return role
                == br.com.lectek.copainsider.domain.fiscal.FiscalPrintStationRole.DELIVERY_ONLY;
    }

    private FiscalPrintStationEntity resolveAutoStation(
            final FiscalPrintChannel printChannel
    ) {
        return fiscalPrintStationService.findBestStationEntity(printChannel)
                .orElse(null);
    }

    private FiscalPrintJobStatus resolveStatusFromDocument(
            final FiscalDocumentEntity document
    ) {
        if (document == null || document.getStatus() == null) {
            return FiscalPrintJobStatus.WAITING_DOCUMENT;
        }
        return switch (document.getStatus()) {
            case AUTHORIZED -> FiscalPrintJobStatus.READY;
            case CANCELLED -> FiscalPrintJobStatus.CANCELLED;
            case REJECTED, ERROR -> FiscalPrintJobStatus.FAILED;
            case DRAFT, PENDING_SUBMISSION, SUBMITTED ->
                    FiscalPrintJobStatus.WAITING_DOCUMENT;
        };
    }

    private FiscalPrintJobType resolveJobType(
            final FiscalPrintChannel printChannel
    ) {
        return printChannel == FiscalPrintChannel.IMMEDIATE
                ? FiscalPrintJobType.DANFE_IMMEDIATE
                : FiscalPrintJobType.DANFE_WITH_DELIVERY;
    }

    private int resolvePriority(final PedidoFiscalSnapshotEntity snapshot) {
        if (snapshot.getPrintChannel() == FiscalPrintChannel.IMMEDIATE) {
            return IMMEDIATE_PRIORITY;
        }
        final String source = snapshot.getSource() == null
                ? ""
                : snapshot.getSource().toUpperCase();
        if (source.contains("CAIXA") || source.contains("VENDA_RAPIDA")) {
            return IMMEDIATE_PRIORITY;
        }
        return DELIVERY_PRIORITY;
    }

    private LocalDateTime resolveSchedule(final FiscalDocumentEntity document) {
        if (document.getAuthorizedAt() != null) {
            return document.getAuthorizedAt();
        }
        if (document.getLastStatusAt() != null) {
            return document.getLastStatusAt();
        }
        return LocalDateTime.now();
    }

    private FiscalPrintJobEntity requireJob(final Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Job de impressao nao encontrado."
                ));
    }

    private void requireStationOwnership(
            final FiscalPrintJobEntity job,
            final String stationCode
    ) {
        final String normalizedStationCode = blankToNull(stationCode);
        if (normalizedStationCode == null) {
            throw new IllegalArgumentException(
                    "Codigo da estacao de impressao obrigatorio."
            );
        }
        if (job.getStation() == null
                || job.getStation().getCode() == null
                || !job.getStation().getCode().equalsIgnoreCase(
                        normalizedStationCode
                )) {
            throw new IllegalStateException(
                    "Este job nao pertence a estacao informada."
            );
        }
    }

    private void appendEvent(
            final FiscalPrintJobEntity job,
            final FiscalPrintJobEventType eventType,
            final FiscalPrintJobStatus statusBefore,
            final FiscalPrintJobStatus statusAfter,
            final String message,
            final String actor
    ) {
        final FiscalPrintJobEventEntity event = new FiscalPrintJobEventEntity();
        event.setPrintJob(job);
        event.setEventType(eventType);
        event.setStatusBefore(statusBefore);
        event.setStatusAfter(statusAfter);
        event.setMessage(blankToNull(message));
        event.setActor(blankToNull(actor));
        eventRepository.save(event);
    }

    private PrintJobSummary toSummary(final FiscalPrintJobEntity entity) {
        return new PrintJobSummary(
                entity.getId(),
                entity.getPedido() == null ? null : entity.getPedido().getId(),
                entity.getFiscalDocument() == null
                        ? null
                        : entity.getFiscalDocument().getId(),
                entity.getJobType(),
                entity.getStatus(),
                entity.getPrintChannel(),
                entity.getPriority(),
                entity.getCopies(),
                entity.getSource(),
                entity.getStation() == null
                        ? null
                        : entity.getStation().getDisplayName(),
                entity.getFiscalDocument() == null
                        ? null
                        : entity.getFiscalDocument().getStatus(),
                entity.getFiscalDocument() == null
                        ? null
                        : entity.getFiscalDocument().getAccessKey(),
                entity.getFiscalDocument() == null
                        ? null
                        : entity.getFiscalDocument().getDanfeStoragePath(),
                entity.getScheduledFor(),
                entity.getUpdatedAt(),
                entity.getErrorMessage(),
                entity.getCancelReason(),
                entity.getReprintOfJobId()
        );
    }

    private PrintJobEventSummary toEventSummary(
            final FiscalPrintJobEventEntity entity
    ) {
        return new PrintJobEventSummary(
                entity.getId(),
                entity.getPrintJob() == null ? null : entity.getPrintJob().getId(),
                entity.getPrintJob() == null || entity.getPrintJob().getPedido() == null
                        ? null
                        : entity.getPrintJob().getPedido().getId(),
                entity.getEventType(),
                entity.getStatusBefore(),
                entity.getStatusAfter(),
                entity.getActor(),
                entity.getMessage(),
                entity.getCreatedAt()
        );
    }

    private int normalizeLimit(final int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private String blankToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private String normalizeActor(final String actor) {
        final String normalized = blankToNull(actor);
        return normalized == null ? SYSTEM_ACTOR : normalized;
    }

    private boolean sameText(final String first, final String second) {
        final String firstNormalized = blankToNull(first);
        final String secondNormalized = blankToNull(second);
        if (firstNormalized == null) {
            return secondNormalized == null;
        }
        return firstNormalized.equals(secondNormalized);
    }

    public record PrintJobSummary(
            Long id,
            Long pedidoId,
            Long fiscalDocumentId,
            FiscalPrintJobType jobType,
            FiscalPrintJobStatus status,
            FiscalPrintChannel printChannel,
            Integer priority,
            Integer copies,
            String source,
            String stationName,
            br.com.lectek.copainsider.domain.fiscal.FiscalDocumentStatus documentStatus,
            String accessKey,
            String danfeUrl,
            LocalDateTime scheduledFor,
            LocalDateTime updatedAt,
            String errorMessage,
            String cancelReason,
            Long reprintOfJobId
    ) {
    }

    public record PrintJobEventSummary(
            Long id,
            Long printJobId,
            Long pedidoId,
            FiscalPrintJobEventType eventType,
            FiscalPrintJobStatus statusBefore,
            FiscalPrintJobStatus statusAfter,
            String actor,
            String message,
            LocalDateTime createdAt
    ) {
    }
}
