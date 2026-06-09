package br.com.lectek.copainsider.application.service.fiscal;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.FiscalDocumentEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.FiscalEventEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.FiscalDocumentRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.FiscalEventRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.lectek.copainsider.domain.fiscal.FiscalDocumentModel;
import br.com.lectek.copainsider.domain.fiscal.FiscalDocumentStatus;
import br.com.lectek.copainsider.domain.fiscal.FiscalEnvironment;
import br.com.lectek.copainsider.domain.fiscal.FiscalEventType;
import br.com.lectek.copainsider.domain.fiscal.FiscalProvider;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FiscalDocumentService {

    private static final int DEFAULT_RECENT_LIMIT = 12;
    private static final int MAX_RECENT_LIMIT = 50;

    private final FiscalDocumentRepository documentRepository;
    private final FiscalEventRepository eventRepository;
    private final PedidoRepository pedidoRepository;
    private final FiscalEmitterConfigService emitterConfigService;
    private final FiscalDocumentEmailDispatchService emailDispatchService;
    private final FiscalPrintQueueService fiscalPrintQueueService;
    private final Map<FiscalProvider, FiscalDocumentGateway> gateways;

    public FiscalDocumentService(
            final FiscalDocumentRepository fiscalDocumentRepository,
            final FiscalEventRepository fiscalEventRepository,
            final PedidoRepository pedidoRepositoryValue,
            final FiscalEmitterConfigService fiscalEmitterConfigService,
            final FiscalDocumentEmailDispatchService fiscalDocumentEmailDispatchService,
            final FiscalPrintQueueService fiscalPrintQueueServiceValue,
            final List<FiscalDocumentGateway> gatewayList
    ) {
        this.documentRepository = fiscalDocumentRepository;
        this.eventRepository = fiscalEventRepository;
        this.pedidoRepository = pedidoRepositoryValue;
        this.emitterConfigService = fiscalEmitterConfigService;
        this.emailDispatchService = fiscalDocumentEmailDispatchService;
        this.fiscalPrintQueueService = fiscalPrintQueueServiceValue;
        this.gateways = gatewayList.stream().collect(Collectors.toMap(
                FiscalDocumentGateway::provider,
                Function.identity()
        ));
    }

    @Transactional(readOnly = true)
    public List<FiscalDocumentSummary> listRecent(final int limit) {
        final int normalizedLimit = normalizeLimit(limit);
        return documentRepository.findAllByOrderByCreatedAtDesc(
                PageRequest.of(0, normalizedLimit)
        ).stream().map(this::toSummary).toList();
    }

    @Transactional(readOnly = true)
    public Optional<FiscalDocumentEntity> findLatestByPedidoId(final Long pedidoId) {
        if (pedidoId == null) {
            return Optional.empty();
        }
        return documentRepository.findTopByPedidoIdOrderByCreatedAtDesc(pedidoId);
    }

    @Transactional
    public FiscalDocumentEntity createDraft(final CreateDraftInput input) {
        if (input == null) {
            throw new IllegalArgumentException(
                    "Documento fiscal obrigatorio."
            );
        }
        if (input.model() == null) {
            throw new IllegalArgumentException("Modelo fiscal obrigatorio.");
        }

        final FiscalDocumentEntity entity = new FiscalDocumentEntity();
        entity.setPedido(resolvePedido(input.pedidoId()));
        entity.setProvider(input.provider() == null
                ? FiscalProvider.FOCUS_NFE
                : input.provider());
        entity.setModel(input.model());
        entity.setEnvironment(input.environment() == null
                ? FiscalEnvironment.HOMOLOGACAO
                : input.environment());
        entity.setStatus(FiscalDocumentStatus.DRAFT);
        entity.setExternalReference(generateReference(input.externalReference()));
        entity.setIssuerCnpj(input.issuerCnpj());
        entity.setRecipientDocument(input.recipientDocument());
        entity.setTotalAmount(input.totalAmount());
        entity.setRequestPayload(blankToNull(input.requestPayload()));
        entity.setResponsePayload(blankToNull(input.responsePayload()));
        entity.setErrorMessage(blankToNull(input.errorMessage()));

        final FiscalDocumentEntity saved = documentRepository.save(entity);
        appendEvent(
                saved,
                FiscalEventType.DRAFT_CREATED,
                null,
                saved.getStatus(),
                "Documento fiscal registrado localmente.",
                null,
                input.responsePayload(),
                null
        );
        fiscalPrintQueueService.syncWithDocument(saved);
        return saved;
    }

    @Transactional
    public FiscalDocumentEntity submit(
            final Long documentId,
            final String requestPayload
    ) {
        final FiscalDocumentEntity entity = documentRepository.findById(documentId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Documento fiscal nao encontrado."
                ));
        if (entity.getStatus() == FiscalDocumentStatus.AUTHORIZED
                || entity.getStatus() == FiscalDocumentStatus.CANCELLED) {
            return entity;
        }

        final FiscalDocumentStatus previousStatus = entity.getStatus();
        entity.setStatus(FiscalDocumentStatus.PENDING_SUBMISSION);
        entity.setRequestPayload(blankToNull(requestPayload));
        entity.setLastStatusAt(LocalDateTime.now());
        final FiscalDocumentEntity pending = documentRepository.save(entity);
        appendEvent(
                pending,
                FiscalEventType.SUBMISSION_REQUESTED,
                previousStatus,
                pending.getStatus(),
                "Envio para o provedor fiscal iniciado.",
                null,
                requestPayload,
                pending.getLastStatusAt()
        );

        final FiscalDocumentGateway gateway = gateways.get(pending.getProvider());
        if (gateway == null) {
            return recordError(
                    pending.getId(),
                    "Nao existe gateway fiscal para o provedor selecionado.",
                    null
            );
        }

        try {
            final FiscalDocumentGateway.GatewayStatusSnapshot snapshot =
                    gateway.submitDocument(
                            emitterConfigService.requireEnabledEntity(
                                    pending.getProvider()
                            ),
                            pending,
                            requestPayload
                    );
            applySnapshot(pending, snapshot);
            final FiscalDocumentEntity saved = documentRepository.save(pending);
            appendEvent(
                    saved,
                    resolveEventType(saved.getStatus()),
                    FiscalDocumentStatus.PENDING_SUBMISSION,
                    saved.getStatus(),
                    snapshot.message(),
                    snapshot.providerEventId(),
                    snapshot.responsePayload(),
                    snapshot.processedAt()
            );
            emailDispatchService.dispatchIfRequested(saved);
            fiscalPrintQueueService.syncWithDocument(saved);
            return saved;
        } catch (RuntimeException ex) {
            return recordError(pending.getId(), ex.getMessage(), null);
        }
    }

    @Transactional
    public FiscalDocumentEntity recordError(
            final Long documentId,
            final String message,
            final String payload
    ) {
        final FiscalDocumentEntity entity = documentRepository.findById(documentId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Documento fiscal nao encontrado."
                ));
        final FiscalDocumentStatus previousStatus = entity.getStatus();
        entity.setStatus(FiscalDocumentStatus.ERROR);
        entity.setErrorMessage(blankToNull(message));
        if (payload != null && !payload.isBlank()) {
            entity.setResponsePayload(payload);
        }
        entity.setLastStatusAt(LocalDateTime.now());
        final FiscalDocumentEntity saved = documentRepository.save(entity);
        appendEvent(
                saved,
                FiscalEventType.ERROR,
                previousStatus,
                saved.getStatus(),
                message,
                null,
                payload,
                saved.getLastStatusAt()
        );
        fiscalPrintQueueService.syncWithDocument(saved);
        return saved;
    }

    @Transactional
    public FiscalDocumentEntity syncStatus(final Long documentId) {
        final FiscalDocumentEntity entity = documentRepository.findById(documentId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Documento fiscal nao encontrado."
                ));
        final FiscalDocumentGateway gateway = gateways.get(entity.getProvider());
        if (gateway == null) {
            throw new IllegalStateException(
                    "Nao existe gateway fiscal para o provedor selecionado."
            );
        }

        final FiscalDocumentStatus previousStatus = entity.getStatus();
        final FiscalDocumentGateway.GatewayStatusSnapshot snapshot =
                gateway.queryStatus(
                        emitterConfigService.requireEnabledEntity(entity.getProvider()),
                        entity
                );

        applySnapshot(entity, snapshot);
        final FiscalDocumentEntity saved = documentRepository.save(entity);
        appendEvent(
                saved,
                FiscalEventType.STATUS_SYNC,
                previousStatus,
                saved.getStatus(),
                snapshot.message(),
                snapshot.providerEventId(),
                snapshot.responsePayload(),
                snapshot.processedAt()
        );
        emailDispatchService.dispatchIfRequested(saved);
        fiscalPrintQueueService.syncWithDocument(saved);
        return saved;
    }

    @Transactional
    public void syncPendingDocuments(final int limit) {
        final int normalizedLimit = normalizeLimit(limit);
        final List<FiscalDocumentEntity> documents =
                documentRepository.findByStatusInOrderByUpdatedAtAsc(
                        List.of(
                                FiscalDocumentStatus.PENDING_SUBMISSION,
                                FiscalDocumentStatus.SUBMITTED
                        ),
                        PageRequest.of(0, normalizedLimit)
                );
        for (FiscalDocumentEntity document : documents) {
            try {
                syncStatus(document.getId());
            } catch (RuntimeException ex) {
                recordError(document.getId(), ex.getMessage(), null);
            }
        }
    }

    @Transactional
    public Optional<FiscalDocumentEntity> dispatchPendingEmail(
            final Long documentId
    ) {
        if (documentId == null) {
            return Optional.empty();
        }
        final FiscalDocumentEntity entity = documentRepository.findById(documentId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Documento fiscal nao encontrado."
                ));
        final LocalDateTime before = entity.getEmailDeliverySentAt();
        emailDispatchService.dispatchIfRequested(entity);
        if (before != entity.getEmailDeliverySentAt()) {
            return Optional.of(documentRepository.save(entity));
        }
        return Optional.of(entity);
    }

    private FiscalDocumentSummary toSummary(final FiscalDocumentEntity entity) {
        return new FiscalDocumentSummary(
                entity.getId(),
                entity.getPedido() == null ? null : entity.getPedido().getId(),
                entity.getExternalReference(),
                entity.getModel(),
                entity.getStatus(),
                entity.getTotalAmount(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private PedidoEntity resolvePedido(final Long pedidoId) {
        if (pedidoId == null) {
            return null;
        }
        return pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Pedido nao encontrado para o documento fiscal."
                ));
    }

    private void applySnapshot(
            final FiscalDocumentEntity entity,
            final FiscalDocumentGateway.GatewayStatusSnapshot snapshot
    ) {
        if (snapshot.status() != null) {
            entity.setStatus(snapshot.status());
        }
        if (snapshot.externalId() != null && !snapshot.externalId().isBlank()) {
            entity.setExternalId(snapshot.externalId());
        }
        if (snapshot.accessKey() != null && !snapshot.accessKey().isBlank()) {
            entity.setAccessKey(snapshot.accessKey());
        }
        if (snapshot.series() != null) {
            entity.setSeries(snapshot.series());
        }
        if (snapshot.documentNumber() != null) {
            entity.setDocumentNumber(snapshot.documentNumber());
        }
        if (snapshot.protocol() != null && !snapshot.protocol().isBlank()) {
            entity.setProtocol(snapshot.protocol());
        }
        if (snapshot.xmlStoragePath() != null
                && !snapshot.xmlStoragePath().isBlank()) {
            entity.setXmlStoragePath(snapshot.xmlStoragePath());
        }
        if (snapshot.danfeStoragePath() != null
                && !snapshot.danfeStoragePath().isBlank()) {
            entity.setDanfeStoragePath(snapshot.danfeStoragePath());
        }
        if (snapshot.responsePayload() != null
                && !snapshot.responsePayload().isBlank()) {
            entity.setResponsePayload(snapshot.responsePayload());
        }
        if (snapshot.errorMessage() != null
                && !snapshot.errorMessage().isBlank()) {
            entity.setErrorMessage(snapshot.errorMessage());
        }
        if (snapshot.issuedAt() != null) {
            entity.setIssuedAt(snapshot.issuedAt());
        }
        if (snapshot.authorizedAt() != null) {
            entity.setAuthorizedAt(snapshot.authorizedAt());
        }
        if (snapshot.cancelledAt() != null) {
            entity.setCancelledAt(snapshot.cancelledAt());
        }
        entity.setLastStatusAt(snapshot.processedAt() == null
                ? LocalDateTime.now()
                : snapshot.processedAt());
    }

    private void appendEvent(
            final FiscalDocumentEntity entity,
            final FiscalEventType eventType,
            final FiscalDocumentStatus statusBefore,
            final FiscalDocumentStatus statusAfter,
            final String message,
            final String providerEventId,
            final String payload,
            final LocalDateTime processedAt
    ) {
        final FiscalEventEntity event = new FiscalEventEntity();
        event.setFiscalDocument(entity);
        event.setEventType(eventType);
        event.setStatusBefore(statusBefore);
        event.setStatusAfter(statusAfter);
        event.setMessage(blankToNull(message));
        event.setProviderEventId(blankToNull(providerEventId));
        event.setPayload(blankToNull(payload));
        event.setProcessedAt(processedAt);
        eventRepository.save(event);
    }

    private FiscalEventType resolveEventType(
            final FiscalDocumentStatus status
    ) {
        if (status == FiscalDocumentStatus.AUTHORIZED) {
            return FiscalEventType.AUTHORIZATION;
        }
        if (status == FiscalDocumentStatus.REJECTED
                || status == FiscalDocumentStatus.ERROR) {
            return FiscalEventType.ERROR;
        }
        return FiscalEventType.STATUS_SYNC;
    }

    private int normalizeLimit(final int limit) {
        if (limit <= 0) {
            return DEFAULT_RECENT_LIMIT;
        }
        return Math.min(limit, MAX_RECENT_LIMIT);
    }

    private String generateReference(final String externalReference) {
        if (externalReference != null && !externalReference.isBlank()) {
            return externalReference.trim();
        }
        return "FISC-" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 16).toUpperCase();
    }

    private String blankToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    public record CreateDraftInput(
            Long pedidoId,
            FiscalProvider provider,
            FiscalDocumentModel model,
            FiscalEnvironment environment,
            String externalReference,
            String issuerCnpj,
            String recipientDocument,
            BigDecimal totalAmount,
            String requestPayload,
            String responsePayload,
            String errorMessage
    ) {
    }

    public record FiscalDocumentSummary(
            Long id,
            Long pedidoId,
            String externalReference,
            FiscalDocumentModel model,
            FiscalDocumentStatus status,
            BigDecimal totalAmount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }
}
