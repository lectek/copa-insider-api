package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaignLog;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaignQueue;
import br.com.redemaisfarma.application.dto.request.EmailCampaignRequest;
import br.com.redemaisfarma.application.dto.request.EmailCampaignValidationRequest;
import br.com.redemaisfarma.application.dto.response.CampaignQueuePreviewResponse;
import br.com.redemaisfarma.application.dto.response.EmailCampaignLogResponse;
import br.com.redemaisfarma.application.dto.response.EmailCampaignQueueItemResponse;
import br.com.redemaisfarma.application.dto.response.EmailCampaignResponse;
import br.com.redemaisfarma.application.dto.response.QueueDashboardResponse;
import br.com.redemaisfarma.application.dto.response.QueueStatusResponse;
import br.com.redemaisfarma.application.service.EmailCampaignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Admin - Marketing de E-mail REST",
        description = "APIs REST para campanhas de e-mail marketing"
)
@RestController
@RequestMapping("/api/admin/marketing/emails/campanhas")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMarketingEmailCampaignRestController {

    /**
     * Campaign application service.
     */
    private final EmailCampaignService campaignService;

    /**
     * Creates controller with service dependency.
     *
     * @param service campaign service
     */
    public AdminMarketingEmailCampaignRestController(
            final EmailCampaignService service
    ) {
        this.campaignService = service;
    }

    /**
     * Lists campaigns using pageable parameters.
     *
     * @param pageable page request
     * @return page of campaigns
     */
    @Operation(summary = "Lista campanhas paginadas")
    @GetMapping
    public Page<EmailCampaignResponse> list(final Pageable pageable) {
        return campaignService.list(pageable);
    }

    /**
     * Gets one campaign by id.
     *
     * @param id campaign id
     * @return campaign response or 404
     */
    @Operation(summary = "Consulta campanha pelo id")
    @GetMapping("/{id}")
    public ResponseEntity<EmailCampaignResponse> get(
            @PathVariable("id") final Long id
    ) {
        return campaignService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Creates one campaign.
     *
     * @param request create request
     * @return created campaign response
     */
    @Operation(summary = "Cria nova campanha")
    @PostMapping
    public ResponseEntity<EmailCampaignResponse> create(
            @RequestBody final EmailCampaignRequest request
    ) {
        final EmailCampaignResponse response = campaignService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates one campaign.
     *
     * @param id campaign id
     * @param request update request
     * @return updated campaign response
     */
    @Operation(summary = "Atualiza campanha")
    @PutMapping("/{id}")
    public ResponseEntity<EmailCampaignResponse> update(
            @PathVariable("id") final Long id,
            @RequestBody final EmailCampaignRequest request
    ) {
        final EmailCampaignResponse response = campaignService.update(
                id,
                request
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Cancels one scheduled campaign.
     *
     * @param id campaign id
     * @return empty response
     */
    @Operation(summary = "Cancela uma campanha agendada")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable("id") final Long id) {
        campaignService.cancel(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Pauses one scheduled campaign.
     *
     * @param id campaign id
     * @return updated campaign response
     */
    @Operation(summary = "Pausa uma campanha agendada")
    @PostMapping("/{id}/pause")
    public ResponseEntity<EmailCampaignResponse> pause(
            @PathVariable("id") final Long id
    ) {
        final EmailCampaignResponse response = campaignService.pause(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Resumes one paused campaign.
     *
     * @param id campaign id
     * @return updated campaign response
     */
    @Operation(summary = "Retoma uma campanha pausada")
    @PostMapping("/{id}/resume")
    public ResponseEntity<EmailCampaignResponse> resume(
            @PathVariable("id") final Long id
    ) {
        final EmailCampaignResponse response = campaignService.resume(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates validation status for one campaign.
     *
     * @param id campaign id
     * @param request validation request
     * @return updated campaign response
     */
    @Operation(summary = "Atualiza o status de validacao da campanha")
    @PostMapping("/{id}/validate")
    public ResponseEntity<EmailCampaignResponse> validate(
            @PathVariable("id") final Long id,
            @RequestBody final EmailCampaignValidationRequest request
    ) {
        final EmailCampaignResponse response =
                campaignService.updateValidationStatus(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Returns preview queue metrics filtered by status/time/segment.
     *
     * @param id campaign id
     * @param status queue status filter
     * @param scheduledFrom scheduled from instant
     * @param scheduledTo scheduled to instant
     * @param segmento segment filter
     * @return preview metrics payload
     */
    @Operation(summary = "Preview queue metrics filtered por segmento/periodo")
    @GetMapping("/{id}/preview/metrics")
    public CampaignQueuePreviewResponse previewMetrics(
            @PathVariable("id") final Long id,
            @RequestParam(required = false) final List<String> status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant scheduledFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            final Instant scheduledTo,
            @RequestParam(required = false) final String segmento
    ) {
        return campaignService.queuePreviewMetrics(
                id,
                status,
                scheduledFrom,
                scheduledTo,
                segmento
        );
    }

    /**
     * Renders template preview for one campaign.
     *
     * @param id campaign id
     * @return HTML payload
     */
    @Operation(summary = "Gera o preview do template da campanha")
    @GetMapping(path = "/{id}/preview", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> preview(@PathVariable("id") final Long id) {
        return ResponseEntity.ok(campaignService.preview(id));
    }

    /**
     * Returns queue dashboard payload.
     *
     * @return queue dashboard response
     */
    @Operation(summary = "Retorna o status da fila de campanhas")
    @GetMapping("/fila")
    public QueueDashboardResponse queue() {
        final List<QueueStatusResponse> statuses = campaignService
                .queueStatus();
        final List<EmailCampaignQueueItemResponse> queueItems = campaignService
                .latestQueueItems()
                .stream()
                .map(this::mapQueueItem)
                .collect(Collectors.toList());
        final List<EmailCampaignLogResponse> logs = campaignService
                .latestLogs()
                .stream()
                .map(this::mapLog)
                .collect(Collectors.toList());
        return new QueueDashboardResponse(statuses, queueItems, logs);
    }

    /**
     * Maps queue entity to response DTO.
     *
     * @param queue queue entity
     * @return queue item response
     */
    private EmailCampaignQueueItemResponse mapQueueItem(
            final EmailCampaignQueue queue
    ) {
        final EmailCampaignQueueItemResponse dto =
                new EmailCampaignQueueItemResponse();
        dto.setId(queue.getId());
        dto.setCampaignId(queue.getCampaignId());
        dto.setRecipientEmail(queue.getRecipientEmail());
        dto.setStatus(queue.getStatus());
        dto.setScheduledAt(queue.getScheduledAt());
        dto.setAttempts(queue.getAttempts());
        dto.setLastError(queue.getLastError());
        return dto;
    }

    /**
     * Maps log entity to response DTO.
     *
     * @param log log entity
     * @return log response
     */
    private EmailCampaignLogResponse mapLog(final EmailCampaignLog log) {
        final EmailCampaignLogResponse dto = new EmailCampaignLogResponse();
        dto.setId(log.getId());
        dto.setCampaignId(log.getCampaignId());
        dto.setRecipientEmail(log.getRecipientEmail());
        dto.setStatus(log.getStatus());
        dto.setErrorText(log.getErrorText());
        dto.setCreatedAt(log.getCreatedAt());
        return dto;
    }
}
