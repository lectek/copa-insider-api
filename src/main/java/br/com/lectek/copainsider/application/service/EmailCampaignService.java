package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.EmailCampaign;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.EmailCampaignLog;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.EmailCampaignQueue;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.EmailCampaignLogRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.EmailCampaignQueueRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.EmailCampaignRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.UsuarioJpaRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.lectek.copainsider.application.dto.request.EmailCampaignRequest;
import br.com.lectek.copainsider.application.dto.request.EmailCampaignValidationRequest;
import br.com.lectek.copainsider.application.dto.response.AutomationPreviewResponse;
import br.com.lectek.copainsider.application.dto.response.CampaignQueuePreviewResponse;
import br.com.lectek.copainsider.application.dto.response.EmailCampaignResponse;
import br.com.lectek.copainsider.application.dto.response.QueueStatusResponse;
import br.com.lectek.copainsider.application.service.segment.SegmentType;
import br.com.lectek.copainsider.domain.enums.StatusPedido;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailCampaignService {
    private static final String STATUS_SCHEDULED = "SCHEDULED";
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_PAUSED = "PAUSED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String DEFAULT_VALIDATION_STATUS = "PENDING";
    private static final String SEGMENT_DETAIL_KEY = "segmentoDetalhado";
    private static final String DEFAULT_ZONE = ZoneId.systemDefault().getId();
    private static final String STATUS_SENT = "SENT";
    private static final String QUEUE_PENDING = "PENDING";
    private static final String QUEUE_SENDING = "SENDING";
    private static final String QUEUE_CANCELLED = "CANCELLED";
    private static final String STATUS_FAILED = "FAILED";
    private static final List<String> QUEUE_ACTIVE_STATUSES = List.of(QUEUE_PENDING, QUEUE_SENDING);
    private static final Set<String> VALIDATION_STATUSES = Set.of("PENDING", "APPROVED", "REJECTED", "READY");

    private final EmailCampaignRepository campaignRepository;
    private final EmailCampaignQueueRepository queueRepository;
    private final EmailCampaignLogRepository logRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final ObjectMapper objectMapper;
    private final MailService mailService;

    public EmailCampaignService(
            EmailCampaignRepository campaignRepository,
            EmailCampaignQueueRepository queueRepository,
            EmailCampaignLogRepository logRepository,
            ClienteRepository clienteRepository,
            UsuarioJpaRepository usuarioRepository,
            ObjectMapper objectMapper,
            MailService mailService
    ) {
        this.campaignRepository = campaignRepository;
        this.queueRepository = queueRepository;
        this.logRepository = logRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.objectMapper = objectMapper;
        this.mailService = mailService;
    }

    public Page<EmailCampaignResponse> list(Pageable pageable) {
        return campaignRepository.findAll(pageable).map(this::toResponse);
    }

    public Optional<EmailCampaignResponse> findById(Long id) {
        return campaignRepository.findById(id).map(this::toResponse);
    }

    @Transactional
    public EmailCampaignResponse create(EmailCampaignRequest request) {
        EmailCampaign campaign = buildFromRequest(request);
        ZoneId zone = determineZone(request);
        Instant scheduleAt = resolveSchedule(request, zone);
        campaign.setScheduledAt(scheduleAt);
        campaign.setScheduledZone(zone.getId());
        campaign.setStatus(scheduleAt == null ? STATUS_DRAFT : STATUS_SCHEDULED);
        campaignRepository.save(campaign);
        if (scheduleAt != null) {
            enqueueAllRecipients(campaign, scheduleAt, request);
        }
        return toResponse(campaign);
    }

    @Transactional
    public EmailCampaignResponse update(Long id, EmailCampaignRequest request) {
        EmailCampaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campanha nao encontrada"));
        String oldStatus = campaign.getStatus();
        String oldSegmentJson = campaign.getSegmentJson();
        Instant oldScheduledAt = campaign.getScheduledAt();

        campaign.setNome(safe(request.getNome()));
        campaign.setAssunto(safe(request.getAssunto()));
        campaign.setTemplateKey(safe(request.getTemplateKey()));
        campaign.setSegmentJson(buildSegmentJson(request));
        campaign.setSegmentDetail(defaultString(request.getSegmentoDetalhado(), campaign.getSegmentDetail()));
        campaign.setValidationStatus(determineValidationStatus(request.getValidationStatus(), campaign.getValidationStatus()));
        ZoneId zone = determineZone(request);
        Instant scheduleAt = resolveSchedule(request, zone);
        campaign.setScheduledAt(scheduleAt);
        campaign.setScheduledZone(zone.getId());

        boolean previouslyScheduled = STATUS_SCHEDULED.equals(oldStatus);
        boolean willBeScheduled = scheduleAt != null;
        boolean scheduleChanged = !Objects.equals(oldScheduledAt, scheduleAt);
        boolean segmentChanged = !Objects.equals(oldSegmentJson, campaign.getSegmentJson());

        if (willBeScheduled) {
            campaign.setStatus(STATUS_SCHEDULED);
            if (!previouslyScheduled || scheduleChanged || segmentChanged) {
                cancelPendingQueue(campaign);
                enqueueAllRecipients(campaign, scheduleAt, request);
            }
        } else {
            campaign.setStatus(STATUS_DRAFT);
            cancelPendingQueue(campaign);
        }
        return toResponse(campaignRepository.save(campaign));
    }

    @Transactional
    public void cancel(Long id) {
        EmailCampaign campaign = findCampaign(id);
        campaign.setStatus(STATUS_CANCELLED);
        campaignRepository.save(campaign);
        cancelPendingQueue(campaign);
    }

    @Transactional
    public EmailCampaignResponse pause(Long id) {
        EmailCampaign campaign = findCampaign(id);
        if (!STATUS_SCHEDULED.equals(campaign.getStatus())) {
            throw new IllegalStateException("Campanha precisa estar SCHEDULED para ser pausada");
        }
        campaign.setStatus(STATUS_PAUSED);
        EmailCampaign saved = campaignRepository.save(campaign);
        cancelPendingQueue(saved);
        return toResponse(saved);
    }

    @Transactional
    public EmailCampaignResponse resume(Long id) {
        EmailCampaign campaign = findCampaign(id);
        if (!STATUS_PAUSED.equals(campaign.getStatus())) {
            throw new IllegalStateException("Somente campanhas pausadas podem ser retomadas");
        }
        ZoneId zone = zoneOrDefault(campaign.getScheduledZone());
        Instant scheduledAt = campaign.getScheduledAt();
        if (scheduledAt == null || scheduledAt.isBefore(Instant.now())) {
            scheduledAt = Instant.now();
        }
        campaign.setScheduledAt(scheduledAt);
        campaign.setScheduledZone(zone.getId());
        campaign.setStatus(STATUS_SCHEDULED);
        EmailCampaign saved = campaignRepository.save(campaign);
        cancelPendingQueue(saved);
        enqueueAllRecipientsFromCampaign(saved, scheduledAt);
        return toResponse(saved);
    }

    @Transactional
    public EmailCampaignResponse updateValidationStatus(Long id, EmailCampaignValidationRequest request) {
        EmailCampaign campaign = findCampaign(id);
        String status = determineValidationStatus(request.getStatus(), campaign.getValidationStatus());
        campaign.setValidationStatus(status);
        return toResponse(campaignRepository.save(campaign));
    }

    private void cancelPendingQueue(EmailCampaign campaign) {
        List<EmailCampaignQueue> queued = queueRepository.findByCampaignIdAndStatusIn(
                campaign.getId(),
                QUEUE_ACTIVE_STATUSES
        );
        if (queued.isEmpty()) {
            return;
        }
        queued.forEach(item -> item.setStatus(QUEUE_CANCELLED));
        queueRepository.saveAll(queued);
    }

    public List<QueueStatusResponse> queueStatus() {
        return queueRepository.countByStatus().stream()
                .map(status -> new QueueStatusResponse(status.getStatus(), status.getTotal()))
                .collect(Collectors.toList());
    }

    public List<EmailCampaignQueue> latestQueueItems() {
        return queueRepository.findTop20ByOrderByCreatedAtDesc();
    }

    public List<EmailCampaignLog> latestLogs() {
        return logRepository.findTop20ByOrderByCreatedAtDesc();
    }

    public String preview(Long id) {
        EmailCampaign campaign = findCampaign(id);
        SegmentType type = SegmentType.from(extractSegment(campaign));
        Map<String, Object> payload = buildPayloadModel(campaign.getNome(), type.name());
        return mailService.renderTemplate(campaign.getTemplateKey(), payload);
    }

    public CampaignQueuePreviewResponse queuePreviewMetrics(Long campaignId,
                                                            List<String> statuses,
                                                            Instant scheduledFrom,
                                                            Instant scheduledTo,
                                                            String segmentFilter) {
        EmailCampaign campaign = findCampaign(campaignId);
        List<String> resolvedStatuses = resolveQueueStatuses(statuses);
        List<EmailCampaignQueue> items = queueRepository.findByCampaignIdAndStatusIn(campaignId, resolvedStatuses);
        Stream<EmailCampaignQueue> filtered = items.stream()
                .filter(queue -> withinRange(queue.getScheduledAt(), scheduledFrom, scheduledTo));
        if (segmentFilter != null && !segmentFilter.isBlank()) {
            String normalized = segmentFilter.trim().toLowerCase(Locale.ROOT);
            filtered = filtered.filter(queue -> matchesSegment(queue, normalized));
        }
        List<EmailCampaignQueue> filteredList = filtered.collect(Collectors.toList());
        Map<String, Long> counts = filteredList.stream()
                .collect(Collectors.groupingBy(EmailCampaignQueue::getStatus, Collectors.counting()));
        long total = filteredList.size();
        long failed = counts.getOrDefault(STATUS_FAILED, 0L);
        double failureRate = total == 0 ? 0 : (double) failed / total;
        List<CampaignQueuePreviewResponse.QueueItemSummary> sample = filteredList.stream()
                .limit(20)
                .map(this::toQueueItemSummary)
                .collect(Collectors.toList());
        return new CampaignQueuePreviewResponse(
                total,
                failureRate,
                counts,
                sample,
                extractSegmentDetail(campaign),
                defaultString(campaign.getScheduledZone(), DEFAULT_ZONE)
        );
    }

    public List<AutomationPreviewResponse> automationPreview(List<String> statuses,
                                                             Instant scheduledFrom,
                                                             Instant scheduledTo,
                                                             String segmentoFilter) {
        return campaignRepository.findAll().stream()
                .map(campaign -> buildAutomationPreview(campaign, statuses, scheduledFrom, scheduledTo, segmentoFilter))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    private Optional<AutomationPreviewResponse> buildAutomationPreview(EmailCampaign campaign,
                                                                       List<String> statuses,
                                                                       Instant scheduledFrom,
                                                                       Instant scheduledTo,
                                                                       String segmentoFilter) {
        String automationName = classifyAutomation(campaign);
        if (automationName == null) {
            return Optional.empty();
        }
        CampaignQueuePreviewResponse metrics = queuePreviewMetrics(campaign.getId(), statuses, scheduledFrom, scheduledTo, segmentoFilter);
        AutomationPreviewResponse response = new AutomationPreviewResponse(
                campaign.getId(),
                campaign.getNome(),
                campaign.getTemplateKey(),
                automationName,
                metrics
        );
        return Optional.of(response);
    }

    private String classifyAutomation(EmailCampaign campaign) {
        String key = campaign.getTemplateKey();
        if ("mail/recompra".equals(key)) {
            return "Recompra automática";
        }
        if ("mail/back-in-stock".equals(key)) {
            return "Volta ao estoque";
        }
        if ("mail/cart-abandon".equals(key)) {
            return "Carrinho abandonado";
        }
        if ("mail/promo".equals(key) && containsSegment(campaign, "REENGAGEMENT")) {
            return "Reengajamento";
        }
        return null;
    }

    private boolean containsSegment(EmailCampaign campaign, String value) {
        String detail = campaign.getSegmentDetail();
        if (detail != null && detail.toUpperCase(Locale.ROOT).contains(value)) {
            return true;
        }
        String json = campaign.getSegmentJson();
        if (json != null && json.toUpperCase(Locale.ROOT).contains(value)) {
            return true;
        }
        String name = campaign.getNome();
        return name != null && name.toUpperCase(Locale.ROOT).contains(value);
    }

    private EmailCampaign findCampaign(Long id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Campanha nao encontrada"));
    }

    private EmailCampaign buildFromRequest(EmailCampaignRequest request) {
        EmailCampaign campaign = new EmailCampaign();
        campaign.setNome(safe(request.getNome()));
        campaign.setAssunto(safe(request.getAssunto()));
        campaign.setTemplateKey(safe(request.getTemplateKey()));
        campaign.setSegmentJson(buildSegmentJson(request));
        campaign.setSegmentDetail(defaultString(request.getSegmentoDetalhado(), null));
        campaign.setValidationStatus(determineValidationStatus(request.getValidationStatus(), null));
        return campaign;
    }

    private void enqueueAllRecipients(EmailCampaign campaign, Instant scheduledAt, EmailCampaignRequest request) {
        List<Recipient> recipients = resolveRecipients(request);
        for (Recipient recipient : recipients) {
            EmailCampaignQueue queue = new EmailCampaignQueue();
            queue.setCampaignId(campaign.getId());
            queue.setRecipientEmail(recipient.email());
            queue.setRecipientName(recipient.name());
            queue.setStatus(QUEUE_PENDING);
            queue.setScheduledAt(scheduledAt);
            queue.setPayloadJson(buildPayload(campaign.getNome(), extractSegment(campaign)));
            queueRepository.save(queue);
        }
    }

    private void enqueueAllRecipientsFromCampaign(EmailCampaign campaign, Instant scheduledAt) {
        EmailCampaignRequest request = buildRequestFromCampaign(campaign);
        enqueueAllRecipients(campaign, scheduledAt, request);
    }

    private EmailCampaignRequest buildRequestFromCampaign(EmailCampaign campaign) {
        EmailCampaignRequest request = new EmailCampaignRequest();
        Map<String, Object> payload = parseSegmentJson(campaign.getSegmentJson());
        request.setSegmento(toSegmentValue(payload.get("segmento")));
        request.setSegmentoDetalhado(toNullableString(payload.get(SEGMENT_DETAIL_KEY)));
        request.setCategoria(toNullableString(payload.get("categoria")));
        request.setRecenciaDias(toInteger(payload.get("recenciaDias")));
        request.setTicketMinimo(toBigDecimal(payload.get("ticketMinimo")));
        request.setAgendarTimezone(toNullableString(campaign.getScheduledZone()));
        request.setValidationStatus(defaultString(campaign.getValidationStatus(), DEFAULT_VALIDATION_STATUS));
        return request;
    }

    private Map<String, Object> parseSegmentJson(String segmentJson) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("segmento", SegmentType.TODOS.name());
        if (segmentJson == null || segmentJson.isBlank()) {
            return payload;
        }
        try {
            Map<?, ?> parsed = objectMapper.readValue(segmentJson, Map.class);
            if (parsed != null) {
                for (Map.Entry<?, ?> entry : parsed.entrySet()) {
                    if (entry.getKey() != null) {
                        payload.put(entry.getKey().toString(), entry.getValue());
                    }
                }
            }
        } catch (Exception ex) {
            // ignore and return defaults
        }
        return payload;
    }

    private String toSegmentValue(Object value) {
        if (value == null) {
            return SegmentType.TODOS.name();
        }
        String text = value.toString().trim();
        return text.isBlank() ? SegmentType.TODOS.name() : text;
    }

    private String toNullableString(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString().trim();
        return text.isBlank() ? null : text;
    }

    private Integer toInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String str) {
            try {
                return Integer.parseInt(str.trim());
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        if (value instanceof String str) {
            try {
                return new BigDecimal(str.trim());
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private Instant resolveSchedule(EmailCampaignRequest request, ZoneId zone) {
        if (Boolean.TRUE.equals(request.getEnvioImediato())) {
            return Instant.now();
        }
        LocalDateTime dt = request.getAgendarPara();
        if (dt == null) {
            return null;
        }
        return dt.atZone(zone).toInstant();
    }

    private ZoneId determineZone(EmailCampaignRequest request) {
        if (request == null) {
            return zoneOrDefault(null);
        }
        return zoneOrDefault(request.getAgendarTimezone());
    }

    private ZoneId zoneOrDefault(String zoneId) {
        if (zoneId == null || zoneId.isBlank()) {
            return ZoneId.of(DEFAULT_ZONE);
        }
        try {
            return ZoneId.of(zoneId.trim());
        } catch (Exception ex) {
            return ZoneId.of(DEFAULT_ZONE);
        }
    }

    private List<Recipient> resolveRecipients(EmailCampaignRequest request) {
        SegmentType type = SegmentType.from(request.getSegmento());
        return switch (type) {
            case VIP -> resolveVipRecipients();
            case INATIVOS_90D -> resolveInactiveRecipients(request);
            case CATEGORIA -> resolveCategoriaRecipients(request);
            case RECENCIA -> resolveRecencyRecipients(request);
            case TICKET -> resolveTicketRecipients(request);
            default -> toRecipients(clienteRepository.findAll());
        };
    }

    private List<Recipient> resolveVipRecipients() {
        List<Recipient> out = new ArrayList<>();
        usuarioRepository.findByClienteVipTrue().forEach(usuario -> {
            if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
                return;
            }
            clienteRepository.findByEmailIgnoreCase(usuario.getEmail())
                    .filter(ClienteEntity::isAtivo)
                    .ifPresentOrElse(
                            cliente -> out.add(new Recipient(cliente.getEmail(), cliente.getNome())),
                            () -> out.add(new Recipient(usuario.getEmail(), usuario.getNome()))
                    );
        });
        return out;
    }

    private List<Recipient> resolveInactiveRecipients(EmailCampaignRequest request) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(90);
        return toRecipients(clienteRepository.findInativosAntesDe(cutoff));
    }

    private List<Recipient> resolveCategoriaRecipients(EmailCampaignRequest request) {
        if (request.getCategoria() == null || request.getCategoria().isBlank()) {
            return List.of();
        }
        return toRecipients(clienteRepository.findClientesByCategoriaComprada(request.getCategoria().trim(), StatusPedido.CANCELADO));
    }

    private List<Recipient> resolveRecencyRecipients(EmailCampaignRequest request) {
        if (request.getRecenciaDias() == null || request.getRecenciaDias() <= 0) {
            return List.of();
        }
        LocalDateTime from = LocalDateTime.now().minusDays(request.getRecenciaDias());
        return toRecipients(clienteRepository.findClientesByRecencia(from, StatusPedido.CANCELADO));
    }

    private List<Recipient> resolveTicketRecipients(EmailCampaignRequest request) {
        BigDecimal ticket = request.getTicketMinimo();
        if (ticket == null || ticket.compareTo(BigDecimal.ZERO) <= 0) {
            return List.of();
        }
        return toRecipients(clienteRepository.findClientesByTicketMedio(ticket, StatusPedido.CANCELADO));
    }

    private List<Recipient> toRecipients(List<ClienteEntity> clientes) {
        return clientes.stream()
                .filter(cliente -> cliente != null && cliente.isAtivo())
                .filter(cliente -> cliente.getEmail() != null && !cliente.getEmail().isBlank())
                .map(cliente -> new Recipient(cliente.getEmail(), cliente.getNome()))
                .collect(Collectors.toList());
    }

    private String buildPayload(String campaignName, String segmento) {
        try {
            return objectMapper.writeValueAsString(buildPayloadModel(campaignName, segmento));
        } catch (Exception ex) {
            return "{}";
        }
    }

    private Map<String, Object> buildPayloadModel(String campaignName, String segmento) {
        Map<String, Object> model = new HashMap<>();
        model.put("headline", campaignName == null ? "" : campaignName);
        model.put("message", "Confira as ofertas selecionadas para voce.");
        model.put("segmento", segmento == null ? "" : SegmentType.from(segmento).name());
        return model;
    }

    private String extractSegment(EmailCampaign campaign) {
        if (campaign.getSegmentJson() == null || campaign.getSegmentJson().isBlank()) {
            return SegmentType.TODOS.name();
        }
        try {
            Map<?, ?> parsed = objectMapper.readValue(campaign.getSegmentJson(), Map.class);
            Object segmento = parsed.get("segmento");
            return segmento == null ? SegmentType.TODOS.name() : segmento.toString();
        } catch (Exception ex) {
            return SegmentType.TODOS.name();
        }
    }

    private String extractSegmentDetail(EmailCampaign campaign) {
        if (campaign.getSegmentDetail() != null && !campaign.getSegmentDetail().isBlank()) {
            return campaign.getSegmentDetail();
        }
        Map<String, Object> parsed = parseSegmentJson(campaign.getSegmentJson());
        return toNullableString(parsed.get(SEGMENT_DETAIL_KEY));
    }

    private String buildSegmentJson(EmailCampaignRequest request) {
        Map<String, Object> payload = new HashMap<>();
        SegmentType type = SegmentType.from(request.getSegmento());
        payload.put("segmento", type.name());
        if (request.getSegmentoDetalhado() != null && !request.getSegmentoDetalhado().isBlank()) {
            payload.put(SEGMENT_DETAIL_KEY, request.getSegmentoDetalhado().trim());
        }
        if (request.getCategoria() != null && !request.getCategoria().isBlank()) {
            payload.put("categoria", request.getCategoria().trim());
        }
        if (request.getRecenciaDias() != null) {
            payload.put("recenciaDias", request.getRecenciaDias());
        }
        if (request.getTicketMinimo() != null) {
            payload.put("ticketMinimo", request.getTicketMinimo());
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            return "{}";
        }
    }

    private List<String> resolveQueueStatuses(List<String> requested) {
        if (requested == null || requested.isEmpty()) {
            return List.of(QUEUE_PENDING, QUEUE_SENDING, STATUS_SENT, STATUS_FAILED);
        }
        return requested.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .toList();
    }

    private boolean withinRange(Instant value, Instant from, Instant to) {
        if (value == null) {
            return false;
        }
        if (from != null && value.isBefore(from)) {
            return false;
        }
        if (to != null && value.isAfter(to)) {
            return false;
        }
        return true;
    }

    private boolean matchesSegment(EmailCampaignQueue queue, String normalized) {
        String payloadSegment = extractSegmentFromPayload(queue.getPayloadJson());
        return payloadSegment != null && payloadSegment.toLowerCase(Locale.ROOT).contains(normalized);
    }

    private CampaignQueuePreviewResponse.QueueItemSummary toQueueItemSummary(EmailCampaignQueue queue) {
        return new CampaignQueuePreviewResponse.QueueItemSummary(
                queue.getRecipientEmail(),
                queue.getStatus(),
                queue.getScheduledAt(),
                queue.getAttempts(),
                queue.getLastError(),
                extractSegmentFromPayload(queue.getPayloadJson())
        );
    }

    private String extractSegmentFromPayload(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            return null;
        }
        try {
            Map<?, ?> parsed = objectMapper.readValue(payloadJson, Map.class);
            Object detail = parsed.get(SEGMENT_DETAIL_KEY);
            if (detail != null) {
                return detail.toString();
            }
            Object segmento = parsed.get("segmento");
            return segmento == null ? null : segmento.toString();
        } catch (Exception ex) {
            return null;
        }
    }

    private static String defaultString(String value, String fallback) {
        if (value != null) {
            String trimmed = value.trim();
            if (!trimmed.isEmpty()) {
                return trimmed;
            }
        }
        if (fallback == null) {
            return null;
        }
        String trimmedFallback = fallback.trim();
        return trimmedFallback.isEmpty() ? null : trimmedFallback;
    }

    private static String determineValidationStatus(String requested, String current) {
        String candidate = defaultString(requested, current);
        if (candidate == null) {
            return DEFAULT_VALIDATION_STATUS;
        }
        candidate = candidate.toUpperCase(Locale.ROOT);
        return VALIDATION_STATUSES.contains(candidate) ? candidate : DEFAULT_VALIDATION_STATUS;
    }

    private EmailCampaignResponse toResponse(EmailCampaign campaign) {
        EmailCampaignResponse response = new EmailCampaignResponse();
        response.setId(campaign.getId());
        response.setNome(campaign.getNome());
        response.setAssunto(campaign.getAssunto());
        response.setTemplateKey(campaign.getTemplateKey());
        response.setStatus(campaign.getStatus());
        response.setScheduledAt(campaign.getScheduledAt());
        response.setCreatedAt(campaign.getCreatedAt());
        response.setUpdatedAt(campaign.getUpdatedAt());
        response.setSegmentLabel(extractSegment(campaign));
        response.setSegmentoDetalhado(extractSegmentDetail(campaign));
        response.setScheduledZone(defaultString(campaign.getScheduledZone(), DEFAULT_ZONE));
        response.setValidationStatus(defaultString(campaign.getValidationStatus(), DEFAULT_VALIDATION_STATUS));
        return response;
    }

    private record Recipient(String email, String name) {
    }
}
