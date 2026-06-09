package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.application.service.fiscal.FiscalPrintQueueService;
import br.com.redemaisfarma.application.service.fiscal.FiscalPrintStationService;
import br.com.redemaisfarma.domain.fiscal.FiscalPrintJobStatus;
import br.com.redemaisfarma.domain.fiscal.FiscalPrintStationRole;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/fiscal/impressao")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFiscalPrintQueueController {

    private static final int JOB_LIMIT = 80;
    private static final int EVENT_LIMIT = 40;

    private final FiscalPrintQueueService fiscalPrintQueueService;
    private final FiscalPrintStationService fiscalPrintStationService;

    public AdminFiscalPrintQueueController(
            final FiscalPrintQueueService fiscalPrintQueueServiceValue,
            final FiscalPrintStationService fiscalPrintStationServiceValue
    ) {
        this.fiscalPrintQueueService = fiscalPrintQueueServiceValue;
        this.fiscalPrintStationService = fiscalPrintStationServiceValue;
    }

    @GetMapping
    public String painel(
            @RequestParam(name = "status", required = false)
            final FiscalPrintJobStatus status,
            @RequestParam(name = "pedidoId", required = false)
            final Long pedidoId,
            final Model model
    ) {
        model.addAttribute(
                "jobs",
                fiscalPrintQueueService.listJobSummaries(status, pedidoId, JOB_LIMIT)
        );
        model.addAttribute(
                "events",
                fiscalPrintQueueService.listEventSummaries(pedidoId, EVENT_LIMIT)
        );
        model.addAttribute("stations", fiscalPrintStationService.list());
        model.addAttribute("stationForm", new StationForm());
        model.addAttribute("roles", FiscalPrintStationRole.values());
        model.addAttribute("statuses", FiscalPrintJobStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedPedidoId", pedidoId);
        return "pages/admin/fiscal/impressao";
    }

    @PostMapping("/estacoes")
    public String salvarEstacao(
            @ModelAttribute("stationForm") final StationForm form,
            final RedirectAttributes redirectAttributes
    ) {
        try {
            fiscalPrintStationService.save(form.toInput());
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Estacao de impressao salva."
            );
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/fiscal/impressao";
    }

    @PostMapping("/estacoes/{id}/ativo")
    public String atualizarAtivoEstacao(
            @PathVariable("id") final Long stationId,
            @RequestParam("active") final boolean active,
            final RedirectAttributes redirectAttributes
    ) {
        try {
            fiscalPrintStationService.updateActive(stationId, active);
            redirectAttributes.addFlashAttribute(
                    "success",
                    active
                            ? "Estacao ativada."
                            : "Estacao desativada."
            );
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/fiscal/impressao";
    }

    @PostMapping("/estacoes/{id}/credencial/regerar")
    public String regerarCredencialEstacao(
            @PathVariable("id") final Long stationId,
            final RedirectAttributes redirectAttributes
    ) {
        try {
            final FiscalPrintStationService.GeneratedStationCredential credential =
                    fiscalPrintStationService.rotateApiKey(stationId);
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Credencial da estacao regenerada."
            );
            redirectAttributes.addFlashAttribute(
                    "generatedCredential",
                    credential
            );
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/fiscal/impressao";
    }

    @PostMapping("/jobs/{id}/segurar")
    public String segurarJob(
            @PathVariable("id") final Long jobId,
            @RequestParam(name = "message", required = false)
            final String message,
            final Authentication authentication,
            final RedirectAttributes redirectAttributes
    ) {
        return runJobAction(
                redirectAttributes,
                () -> fiscalPrintQueueService.hold(jobId, actor(authentication), message),
                "Job colocado em espera."
        );
    }

    @PostMapping("/jobs/{id}/liberar")
    public String liberarJob(
            @PathVariable("id") final Long jobId,
            final Authentication authentication,
            final RedirectAttributes redirectAttributes
    ) {
        return runJobAction(
                redirectAttributes,
                () -> fiscalPrintQueueService.release(jobId, actor(authentication)),
                "Job liberado para a fila."
        );
    }

    @PostMapping("/jobs/{id}/cancelar")
    public String cancelarJob(
            @PathVariable("id") final Long jobId,
            @RequestParam(name = "reason", required = false)
            final String reason,
            final Authentication authentication,
            final RedirectAttributes redirectAttributes
    ) {
        return runJobAction(
                redirectAttributes,
                () -> fiscalPrintQueueService.cancel(jobId, actor(authentication), reason),
                "Job cancelado."
        );
    }

    @PostMapping("/jobs/{id}/iniciar")
    public String iniciarImpressao(
            @PathVariable("id") final Long jobId,
            final Authentication authentication,
            final RedirectAttributes redirectAttributes
    ) {
        return runJobAction(
                redirectAttributes,
                () -> fiscalPrintQueueService.startPrinting(
                        jobId,
                        actor(authentication)
                ),
                "Impressao iniciada."
        );
    }

    @PostMapping("/jobs/{id}/concluir")
    public String concluirImpressao(
            @PathVariable("id") final Long jobId,
            final Authentication authentication,
            final RedirectAttributes redirectAttributes
    ) {
        return runJobAction(
                redirectAttributes,
                () -> fiscalPrintQueueService.markPrinted(
                        jobId,
                        actor(authentication)
                ),
                "Impressao concluida."
        );
    }

    @PostMapping("/jobs/{id}/falhar")
    public String falharImpressao(
            @PathVariable("id") final Long jobId,
            @RequestParam(name = "message", required = false)
            final String message,
            final Authentication authentication,
            final RedirectAttributes redirectAttributes
    ) {
        return runJobAction(
                redirectAttributes,
                () -> fiscalPrintQueueService.markFailed(
                        jobId,
                        actor(authentication),
                        message
                ),
                "Falha registrada na fila."
        );
    }

    @PostMapping("/jobs/{id}/reimprimir")
    public String reimprimir(
            @PathVariable("id") final Long jobId,
            final Authentication authentication,
            final RedirectAttributes redirectAttributes
    ) {
        return runJobAction(
                redirectAttributes,
                () -> fiscalPrintQueueService.requeue(jobId, actor(authentication)),
                "Reimpressao adicionada a fila."
        );
    }

    @PostMapping("/jobs/{id}/estacao")
    public String atribuirEstacao(
            @PathVariable("id") final Long jobId,
            @RequestParam(name = "stationId", required = false)
            final Long stationId,
            final Authentication authentication,
            final RedirectAttributes redirectAttributes
    ) {
        return runJobAction(
                redirectAttributes,
                () -> fiscalPrintQueueService.assignStation(
                        jobId,
                        stationId,
                        actor(authentication)
                ),
                "Estacao atribuida ao job."
        );
    }

    private String runJobAction(
            final RedirectAttributes redirectAttributes,
            final Runnable action,
            final String successMessage
    ) {
        try {
            action.run();
            redirectAttributes.addFlashAttribute("success", successMessage);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/fiscal/impressao";
    }

    private String actor(final Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return "ADMIN";
        }
        final String value = authentication.getName().trim();
        return value.isBlank() ? "ADMIN" : value;
    }

    @FunctionalInterface
    private interface Runnable {
        void run();
    }

    @Getter
    @Setter
    public static final class StationForm {

        private Long id;
        private String code;
        private String displayName;
        private String printerName;
        private FiscalPrintStationRole role;
        private boolean active = true;
        private String notes;

        FiscalPrintStationService.StationInput toInput() {
            return new FiscalPrintStationService.StationInput(
                    id,
                    code,
                    displayName,
                    printerName,
                    role,
                    active,
                    notes
            );
        }
    }
}
