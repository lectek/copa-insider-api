package br.com.lectek.copainsider.adapters.inbound.web.controller.admin;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.EmailCampaign;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.EmailCampaignLog;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.EmailCampaignQueue;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.EmailCampaignLogRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.EmailCampaignQueueRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.EmailCampaignRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.UsuarioJpaRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.lectek.copainsider.application.service.MailService;
import br.com.lectek.copainsider.domain.enums.StatusPedido;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Tag(
        name = "Admin - Marketing de E-mail",
        description = "Campanhas e templates de e-mail marketing do admin"
)
@Controller
@RequestMapping("/admin/marketing/emails/campanhas")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMarketingEmailCampaignController {

    /**
     * Campaign status for scheduled campaigns.
     */
    private static final String STATUS_SCHEDULED = "SCHEDULED";

    /**
     * Campaign status for draft campaigns.
     */
    private static final String STATUS_DRAFT = "DRAFT";

    /**
     * Campaign status for cancelled campaigns.
     */
    private static final String STATUS_CANCELLED = "CANCELLED";

    /**
     * Queue status for pending items.
     */
    private static final String QUEUE_PENDING = "PENDING";

    /**
     * Queue status for sending items.
     */
    private static final String QUEUE_SENDING = "SENDING";

    /**
     * Queue status for cancelled items.
     */
    private static final String QUEUE_CANCELLED = "CANCELLED";

    /**
     * Status order used in queue dashboard.
     */
    private static final List<String> QUEUE_STATUS_ORDER = List.of(
            "PENDING",
            "SENDING",
            "SENT",
            "FAILED",
            "CANCELLED"
    );

    /**
     * Fixed day range used for inactive segment.
     */
    private static final int INATIVOS_90D_DIAS = 90;

    /**
     * Default payload message rendered in templates.
     */
    private static final String DEFAULT_TEMPLATE_MESSAGE =
            "Confira as ofertas selecionadas para voce.";

    /**
     * View for campaigns form and listing.
     */
    private static final String VIEW_CAMPANHAS =
            "pages/admin/marketing/emails/camapnhas";

    /**
     * View for queue monitor page.
     */
    private static final String VIEW_FILA = "pages/admin/marketing/emails/fila";

    /**
     * Redirect URL for campaigns page.
     */
    private static final String REDIRECT_CAMPANHAS =
            "redirect:/admin/marketing/emails/campanhas";

    /**
     * Campaign repository.
     */
    private final EmailCampaignRepository campaignRepository;

    /**
     * Queue repository.
     */
    private final EmailCampaignQueueRepository queueRepository;

    /**
     * Campaign log repository.
     */
    private final EmailCampaignLogRepository logRepository;

    /**
     * Customer repository.
     */
    private final ClienteRepository clienteRepository;

    /**
     * User repository.
     */
    private final UsuarioJpaRepository usuarioRepository;

    /**
     * JSON mapper.
     */
    private final ObjectMapper objectMapper;

    /**
     * Mail rendering service.
     */
    private final MailService mailService;

    /**
     * Creates controller with required dependencies.
     *
     * @param campaignRepositoryValue campaign repository
     * @param queueRepositoryValue queue repository
     * @param logRepositoryValue log repository
     * @param clienteRepositoryValue customer repository
     * @param usuarioRepositoryValue user repository
     * @param mailServiceValue mail service
     * @param objectMapperValue object mapper
     */
    public AdminMarketingEmailCampaignController(
            final EmailCampaignRepository campaignRepositoryValue,
            final EmailCampaignQueueRepository queueRepositoryValue,
            final EmailCampaignLogRepository logRepositoryValue,
            final ClienteRepository clienteRepositoryValue,
            final UsuarioJpaRepository usuarioRepositoryValue,
            final MailService mailServiceValue,
            final ObjectMapper objectMapperValue
    ) {
        this.campaignRepository = campaignRepositoryValue;
        this.queueRepository = queueRepositoryValue;
        this.logRepository = logRepositoryValue;
        this.clienteRepository = clienteRepositoryValue;
        this.usuarioRepository = usuarioRepositoryValue;
        this.mailService = mailServiceValue;
        this.objectMapper = objectMapperValue;
    }

    /**
     * Shows queue dashboard with status counters and latest items.
     *
     * @param model view model
     * @return queue dashboard view
     */
    @Operation(summary = "Exibe o painel de monitoramento da fila de campanhas")
    @GetMapping("/fila")
    public String fila(final Model model) {
        final Map<String, Long> totals = queueRepository
                .countByStatus()
                .stream()
                .collect(Collectors.toMap(
                        EmailCampaignQueueRepository.StatusCount::getStatus,
                        EmailCampaignQueueRepository.StatusCount::getTotal
                ));
        final List<QueueStatus> statuses = QUEUE_STATUS_ORDER.stream()
                .map(status -> new QueueStatus(
                        status,
                        totals.getOrDefault(status, 0L)
                ))
                .toList();
        final List<EmailCampaignQueue> queueItems =
                queueRepository.findTop20ByOrderByCreatedAtDesc();
        final Set<Long> campaignIds = queueItems.stream()
                .map(EmailCampaignQueue::getCampaignId)
                .collect(Collectors.toSet());
        final Map<Long, String> campaignNames = campaignRepository
                .findAllById(campaignIds)
                .stream()
                .collect(Collectors.toMap(
                        EmailCampaign::getId,
                        EmailCampaign::getNome
                ));
        final List<EmailCampaignLog> logs =
                logRepository.findTop20ByOrderByCreatedAtDesc();

        model.addAttribute("statuses", statuses);
        model.addAttribute("queueItems", queueItems);
        model.addAttribute("campaignNames", campaignNames);
        model.addAttribute("logs", logs);
        return VIEW_FILA;
    }

    /**
     * Renders campaigns form, templates and campaign listing.
     *
     * @param model view model
     * @return campaigns view
     */
    @Operation(
            summary = "Exibe o formulario de campanhas com templates e "
                    + "campanhas existentes"
    )
    @GetMapping
    public String form(final Model model) {
        model.addAttribute("campanha", new CampanhaForm());
        model.addAttribute("templates", templateOptions());
        model.addAttribute(
                "campanhas",
                campaignRepository.findAll(
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );
        return VIEW_CAMPANHAS;
    }

    /**
     * Renders template preview for one campaign.
     *
     * @param id campaign id
     * @return rendered HTML
     */
    @Operation(summary = "Renderiza o preview do template usado pela campanha")
    @GetMapping(path = "/{id}/preview", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String preview(@PathVariable("id") final Long id) {
        final EmailCampaign campaign = findCampaign(id);
        final String segmento = extractSegment(campaign);
        final Map<String, Object> model = buildPayloadModel(
                campaign.getNome(),
                segmento
        );
        return mailService.renderTemplate(campaign.getTemplateKey(), model);
    }

    /**
     * Creates one campaign and optionally enqueues recipients.
     *
     * @param form campaign form payload
     * @param ra redirect attributes
     * @return redirect URL
     */
    @Operation(summary = "Cria uma nova campanha de e-mail")
    @PostMapping
    public String criar(
            @ModelAttribute("campanha") final CampanhaForm form,
            final RedirectAttributes ra
    ) {
        final EmailCampaign campaign = new EmailCampaign();
        campaign.setNome(safe(form.getNome()));
        campaign.setAssunto(safe(form.getAssunto()));
        campaign.setTemplateKey(safe(form.getTemplateKey()));
        campaign.setSegmentJson(buildSegmentJson(form));

        final Instant scheduleAt = resolveSchedule(form);
        campaign.setScheduledAt(scheduleAt);
        campaign.setScheduledZone(ZoneId.systemDefault().getId());
        campaign.setValidationStatus(QUEUE_PENDING);
        campaign.setStatus(
                scheduleAt == null ? STATUS_DRAFT : STATUS_SCHEDULED
        );
        campaignRepository.save(campaign);

        if (scheduleAt != null) {
            final int queued = enqueueAllClientes(campaign, scheduleAt, form);
            if (queued == 0) {
                ra.addFlashAttribute(
                        "warning",
                        "Campanha criada, mas nenhum cliente ativo encontrado."
                );
            } else {
                ra.addFlashAttribute(
                        "success",
                        "Campanha criada e fila gerada (" + queued + ")."
                );
            }
        } else {
            ra.addFlashAttribute("success", "Campanha criada como rascunho.");
        }

        return REDIRECT_CAMPANHAS;
    }

    /**
     * Cancels one campaign and pending queue items.
     *
     * @param id campaign id
     * @param ra redirect attributes
     * @return redirect URL
     */
    @PostMapping("/{id}/cancel")
    public String cancelar(
            @PathVariable("id") final Long id,
            final RedirectAttributes ra
    ) {
        final EmailCampaign campaign = findCampaign(id);
        campaign.setStatus(STATUS_CANCELLED);
        campaignRepository.save(campaign);

        final List<EmailCampaignQueue> queued = queueRepository
                .findByCampaignIdAndStatusIn(
                        campaign.getId(),
                        List.of(QUEUE_PENDING, QUEUE_SENDING)
                );
        queued.forEach(item -> item.setStatus(QUEUE_CANCELLED));
        queueRepository.saveAll(queued);

        ra.addFlashAttribute(
                "success",
                "Campanha cancelada e fila atualizada (" + queued.size() + ")."
        );
        return REDIRECT_CAMPANHAS;
    }

    /**
     * Resolves schedule instant for campaign creation.
     *
     * @param form campaign form payload
     * @return resolved instant, or null for draft
     */
    private Instant resolveSchedule(final CampanhaForm form) {
        if (Boolean.TRUE.equals(form.getEnvioImediato())) {
            return Instant.now();
        }

        final LocalDateTime dt = form.getAgendarPara();
        if (dt == null) {
            return null;
        }

        return dt.atZone(ZoneId.systemDefault()).toInstant();
    }

    /**
     * Enqueues all recipients for one campaign.
     *
     * @param campaign campaign entity
     * @param scheduledAt schedule instant
     * @param form campaign form payload
     * @return number of queued items
     */
    private int enqueueAllClientes(
            final EmailCampaign campaign,
            final Instant scheduledAt,
            final CampanhaForm form
    ) {
        final List<Recipient> recipients = resolveRecipients(form);
        int total = 0;
        for (Recipient recipient : recipients) {
            final EmailCampaignQueue queue = new EmailCampaignQueue();
            queue.setCampaignId(campaign.getId());
            queue.setRecipientEmail(recipient.email());
            queue.setRecipientName(recipient.name());
            queue.setStatus(QUEUE_PENDING);
            queue.setScheduledAt(scheduledAt);
            queue.setPayloadJson(buildPayload(
                    campaign.getNome(),
                    SegmentType.from(form.getSegmento()).name()
            ));
            queueRepository.save(queue);
            total++;
        }
        return total;
    }

    /**
     * Resolves recipient list based on selected segment.
     *
     * @param form campaign form payload
     * @return recipient list
     */
    private List<Recipient> resolveRecipients(final CampanhaForm form) {
        final SegmentType type = SegmentType.from(form.getSegmento());
        switch (type) {
            case VIP -> {
                return resolveVipRecipients();
            }
            case INATIVOS_90D -> {
                final LocalDateTime cutoff = LocalDateTime.now().minusDays(
                        INATIVOS_90D_DIAS
                );
                final List<ClienteEntity> inativos =
                        clienteRepository.findInativosAntesDe(cutoff);
                return toRecipients(inativos);
            }
            case CATEGORIA -> {
                final List<ClienteEntity> byCategoria =
                        resolveCategoriaRecipients(form.getCategoria());
                return toRecipients(byCategoria);
            }
            case RECENCIA -> {
                final List<ClienteEntity> byRecencia =
                        resolveRecencyRecipients(form.getRecenciaDias());
                return toRecipients(byRecencia);
            }
            case TICKET -> {
                final List<ClienteEntity> byTicket =
                        resolveTicketRecipients(form.getTicketMinimo());
                return toRecipients(byTicket);
            }
            default -> {
                return toRecipients(clienteRepository.findAll());
            }
        }
    }

    /**
     * Resolves VIP recipients from users and active customers.
     *
     * @return recipient list
     */
    private List<Recipient> resolveVipRecipients() {
        final List<Recipient> out = new ArrayList<>();
        usuarioRepository.findByClienteVipTrue().forEach(usuario -> {
            if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
                return;
            }
            clienteRepository.findByEmailIgnoreCase(usuario.getEmail())
                    .filter(ClienteEntity::isAtivo)
                    .ifPresentOrElse(
                            cliente -> out.add(new Recipient(
                                    cliente.getEmail(),
                                    cliente.getNome()
                            )),
                            () -> out.add(new Recipient(
                                    usuario.getEmail(),
                                    usuario.getNome()
                            ))
                    );
        });
        return out;
    }

    /**
     * Resolves category-based recipients.
     *
     * @param categoria category name
     * @return customer list
     */
    private List<ClienteEntity> resolveCategoriaRecipients(
            final String categoria
    ) {
        if (categoria == null || categoria.isBlank()) {
            return List.of();
        }
        return clienteRepository.findClientesByCategoriaComprada(
                categoria.trim(),
                StatusPedido.CANCELADO
        );
    }

    /**
     * Resolves recency-based recipients.
     *
     * @param dias day range
     * @return customer list
     */
    private List<ClienteEntity> resolveRecencyRecipients(final Integer dias) {
        if (dias == null || dias <= 0) {
            return List.of();
        }
        final LocalDateTime from = LocalDateTime.now().minusDays(dias);
        return clienteRepository.findClientesByRecencia(
                from,
                StatusPedido.CANCELADO
        );
    }

    /**
     * Resolves ticket-based recipients.
     *
     * @param ticketMinimo minimum ticket amount
     * @return customer list
     */
    private List<ClienteEntity> resolveTicketRecipients(
            final BigDecimal ticketMinimo
    ) {
        if (ticketMinimo == null
                || ticketMinimo.compareTo(BigDecimal.ZERO) <= 0) {
            return List.of();
        }
        return clienteRepository.findClientesByTicketMedio(
                ticketMinimo,
                StatusPedido.CANCELADO
        );
    }

    /**
     * Converts customer entities to recipient payloads.
     *
     * @param clientes customer list
     * @return recipient list
     */
    private List<Recipient> toRecipients(final List<ClienteEntity> clientes) {
        final List<Recipient> out = new ArrayList<>();
        for (ClienteEntity cliente : clientes) {
            if (cliente == null || !cliente.isAtivo()) {
                continue;
            }
            if (cliente.getEmail() == null || cliente.getEmail().isBlank()) {
                continue;
            }
            out.add(new Recipient(cliente.getEmail(), cliente.getNome()));
        }
        return out;
    }

    /**
     * Builds queue payload JSON.
     *
     * @param campaignName campaign name
     * @param segmento segment name
     * @return payload JSON
     */
    private String buildPayload(
            final String campaignName,
            final String segmento
    ) {
        try {
            return objectMapper.writeValueAsString(
                    buildPayloadModel(campaignName, segmento)
            );
        } catch (Exception ex) {
            return "{}";
        }
    }

    /**
     * Builds template model payload.
     *
     * @param campaignName campaign name
     * @param segmento segment name
     * @return payload map
     */
    private Map<String, Object> buildPayloadModel(
            final String campaignName,
            final String segmento
    ) {
        return Map.of(
                "headline",
                campaignName == null ? "" : campaignName,
                "message",
                DEFAULT_TEMPLATE_MESSAGE,
                "segmento",
                segmento == null ? "" : segmento
        );
    }

    /**
     * Extracts segment value from campaign JSON payload.
     *
     * @param campaign campaign entity
     * @return segment value
     */
    private String extractSegment(final EmailCampaign campaign) {
        if (campaign.getSegmentJson() == null
                || campaign.getSegmentJson().isBlank()) {
            return "";
        }
        try {
            final Map<?, ?> parsed = objectMapper.readValue(
                    campaign.getSegmentJson(),
                    Map.class
            );
            final Object segmento = parsed.get("segmento");
            return segmento == null ? "" : segmento.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * Finds campaign by id or throws 404.
     *
     * @param id campaign id
     * @return campaign entity
     */
    private EmailCampaign findCampaign(final Long id) {
        return campaignRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Campanha nao encontrada"
                )
        );
    }

    /**
     * Builds segment JSON used by campaign and queue payload.
     *
     * @param form campaign form payload
     * @return JSON payload
     */
    private String buildSegmentJson(final CampanhaForm form) {
        final SegmentType segmentType = SegmentType.from(form.getSegmento());
        final Map<String, Object> payload = new HashMap<>();
        payload.put("segmento", segmentType.name());
        if (form.getCategoria() != null && !form.getCategoria().isBlank()) {
            payload.put("categoria", form.getCategoria().trim());
        }
        if (form.getRecenciaDias() != null) {
            payload.put("recenciaDias", form.getRecenciaDias());
        }
        if (form.getTicketMinimo() != null) {
            payload.put("ticketMinimo", form.getTicketMinimo());
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            return "{}";
        }
    }

    /**
     * Returns template options for campaign form.
     *
     * @return template options
     */
    private List<TemplateOption> templateOptions() {
        return List.of(
                new TemplateOption("mail/promo", "Promo - basico"),
                new TemplateOption("mail/order-status", "Pedido - status")
        );
    }

    /**
     * Trims string values and defaults null to empty.
     *
     * @param value source value
     * @return trimmed value
     */
    private String safe(final String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Campaign form payload.
     */
    @Getter
    @Setter
    public static final class CampanhaForm {

        /**
         * Campaign name.
         */
        private String nome;

        /**
         * Campaign subject.
         */
        private String assunto;

        /**
         * Template key.
         */
        private String templateKey;

        /**
         * Segment key.
         */
        private String segmento;

        /**
         * Category filter.
         */
        private String categoria;

        /**
         * Recency days filter.
         */
        private Integer recenciaDias;

        /**
         * Minimum ticket filter.
         */
        private BigDecimal ticketMinimo;

        /**
         * Immediate send flag.
         */
        private Boolean envioImediato;

        /**
         * Scheduled datetime.
         */
        private LocalDateTime agendarPara;
    }

    /**
     * Segment types accepted by campaign builder.
     */
    private enum SegmentType {

        /**
         * All recipients segment.
         */
        TODOS,

        /**
         * VIP recipients segment.
         */
        VIP,

        /**
         * Inactive recipients for 90 days segment.
         */
        INATIVOS_90D,

        /**
         * Category-based recipients segment.
         */
        CATEGORIA,

        /**
         * Recency-based recipients segment.
         */
        RECENCIA,

        /**
         * Ticket-based recipients segment.
         */
        TICKET;

        /**
         * Parses a segment type from user input.
         *
         * @param raw raw input
         * @return parsed segment type
         */
        static SegmentType from(final String raw) {
            if (raw == null) {
                return TODOS;
            }
            try {
                return SegmentType.valueOf(raw.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return TODOS;
            }
        }
    }

    /**
     * Template option item.
     *
     * @param key template key
     * @param label template label
     */
    public record TemplateOption(String key, String label) {
    }

    /**
     * Recipient payload.
     *
     * @param email recipient email
     * @param name recipient name
     */
    public record Recipient(String email, String name) {
    }

    /**
     * Queue status summary item.
     *
     * @param status queue status
     * @param total total items
     */
    public record QueueStatus(String status, long total) {
    }
}
