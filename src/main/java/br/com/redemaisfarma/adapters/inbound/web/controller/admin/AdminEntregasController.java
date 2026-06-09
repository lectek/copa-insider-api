package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.application.config.AppProps;
import br.com.redemaisfarma.application.service.delivery.AdminEntregaRouteService;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/entregas")
public class AdminEntregasController {

    private final AdminEntregaRouteService adminEntregaRouteService;
    private final AppProps appProps;

    public AdminEntregasController(
            final AdminEntregaRouteService adminEntregaRouteServiceValue,
            final AppProps appPropsValue
    ) {
        this.adminEntregaRouteService = adminEntregaRouteServiceValue;
        this.appProps = appPropsValue;
    }

    @GetMapping
    public String index(
            @RequestParam(name = "q", required = false) final String q,
            final Model model
    ) {
        model.addAttribute(
                "dashboard",
                adminEntregaRouteService.buildDashboard(q)
        );
        model.addAttribute(
                "eligibleOrders",
                adminEntregaRouteService.listEligibleOrders(q)
        );
        model.addAttribute(
                "deliveryRequests",
                adminEntregaRouteService.listDeliveryRequests(q)
        );
        model.addAttribute(
                "recentRoutes",
                adminEntregaRouteService.listRecentRoutes()
        );
        model.addAttribute("origemPadrao", appProps.getAddressQuery());
        model.addAttribute("filtroQ", q == null ? "" : q.trim());
        return "pages/admin/entregas/index";
    }

    @GetMapping("/rotas/{rotaId}")
    public String detalhe(
            @PathVariable("rotaId") final Long rotaId,
            final Model model
    ) {
        model.addAttribute(
                "rota",
                adminEntregaRouteService.getRouteDetail(rotaId)
        );
        return "pages/admin/entregas/detalhe";
    }

    @GetMapping("/rotas/{rotaId}/motoboy")
    public String painelMotoboy(
            @PathVariable("rotaId") final Long rotaId,
            final Model model
    ) {
        model.addAttribute(
                "rota",
                adminEntregaRouteService.getDriverRouteView(rotaId)
        );
        model.addAttribute("occurrenceOptions", occurrenceOptions());
        return "pages/admin/entregas/motoboy";
    }

    @GetMapping("/rotas/{rotaId}/paradas/{paradaId}/motoboy")
    public String detalheParadaMotoboy(
            @PathVariable("rotaId") final Long rotaId,
            @PathVariable("paradaId") final Long paradaId,
            final Model model
    ) {
        model.addAttribute(
                "detalhe",
                adminEntregaRouteService.getDriverStopDetailView(rotaId, paradaId)
        );
        model.addAttribute("occurrenceOptions", occurrenceOptions());
        model.addAttribute("failureStatusOptions", failureStatusOptions());
        model.addAttribute("failureReasonOptions", failureReasonOptions());
        return "pages/admin/entregas/parada-motoboy";
    }

    @PostMapping("/rotas/{rotaId}/iniciar")
    public String iniciarRota(
            @PathVariable("rotaId") final Long rotaId,
            final Authentication authentication,
            final RedirectAttributes redirectAttributes
    ) {
        try {
            adminEntregaRouteService.startRoute(
                    rotaId,
                    authentication != null ? authentication.getName() : null
            );
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Rota iniciada. A primeira parada ja esta pronta para navegacao."
            );
            return redirectDriverPanel(rotaId);
        } catch (ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getReason());
            return redirectRouteDetail(rotaId);
        }
    }

    @PostMapping("/rotas/{rotaId}/paradas/{paradaId}/cheguei")
    public String registrarChegada(
            @PathVariable("rotaId") final Long rotaId,
            @PathVariable("paradaId") final Long paradaId,
            final RedirectAttributes redirectAttributes
    ) {
        try {
            adminEntregaRouteService.markStopArrived(rotaId, paradaId);
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Chegada registrada. Agora confirme a entrega."
            );
        } catch (ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getReason());
        }
        return redirectDriverStopDetail(rotaId, paradaId);
    }

    @PostMapping("/rotas/{rotaId}/paradas/{paradaId}/confirmar")
    public String confirmarParada(
            @PathVariable("rotaId") final Long rotaId,
            @PathVariable("paradaId") final Long paradaId,
            @RequestParam(name = "formaPagamentoRecebida", required = false)
            final String formaPagamentoRecebida,
            @RequestParam(name = "avaliacaoEntrega", required = false)
            final Integer avaliacaoEntrega,
            @RequestParam(name = "ocorrencias", required = false)
            final List<String> ocorrencias,
            @RequestParam(name = "observacao", required = false)
            final String observacao,
            final RedirectAttributes redirectAttributes
    ) {
        try {
            final AdminEntregaRouteService.DriverRouteView updated =
                    adminEntregaRouteService.confirmStop(
                            rotaId,
                            paradaId,
                            new AdminEntregaRouteService.DeliveryClosureInput(
                                    formaPagamentoRecebida,
                                    avaliacaoEntrega,
                                    ocorrencias,
                                    observacao
                            )
                    );
            redirectAttributes.addFlashAttribute(
                    "success",
                    updated.proximaParada() == null
                            ? "Entrega confirmada. Rota concluida."
                            : "Entrega confirmada. Proxima parada liberada."
            );
            if (updated.proximaParada() != null) {
                return redirectDriverStopDetail(
                        rotaId,
                        updated.proximaParada().id()
                );
            }
        } catch (ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getReason());
            return redirectDriverStopDetail(rotaId, paradaId);
        }
        return redirectDriverPanel(rotaId);
    }

    @PostMapping("/rotas/{rotaId}/paradas/{paradaId}/insucesso")
    public String registrarInsucesso(
            @PathVariable("rotaId") final Long rotaId,
            @PathVariable("paradaId") final Long paradaId,
            @RequestParam(name = "statusFalha", required = false)
            final String statusFalha,
            @RequestParam(name = "motivoFalha", required = false)
            final String motivoFalha,
            @RequestParam(name = "observacao", required = false)
            final String observacao,
            final RedirectAttributes redirectAttributes
    ) {
        try {
            final AdminEntregaRouteService.DriverRouteView updated =
                    adminEntregaRouteService.registerStopFailure(
                            rotaId,
                            paradaId,
                            new AdminEntregaRouteService.DeliveryFailureInput(
                                    statusFalha,
                                    motivoFalha,
                                    observacao
                            )
                    );
            redirectAttributes.addFlashAttribute(
                    "success",
                    updated.proximaParada() == null
                            ? "Insucesso registrado. A rota foi concluida."
                            : "Insucesso registrado. A proxima parada foi liberada."
            );
            if (updated.proximaParada() != null) {
                return redirectDriverStopDetail(rotaId, updated.proximaParada().id());
            }
        } catch (ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getReason());
            return redirectDriverStopDetail(rotaId, paradaId);
        }
        return redirectDriverPanel(rotaId);
    }

    private static String redirectRouteDetail(final Long rotaId) {
        return "redirect:/admin/entregas/rotas/" + rotaId;
    }

    private static String redirectDriverPanel(final Long rotaId) {
        return "redirect:/admin/entregas/rotas/" + rotaId + "/motoboy";
    }

    private static String redirectDriverStopDetail(
            final Long rotaId,
            final Long paradaId
    ) {
        return "redirect:/admin/entregas/rotas/"
                + rotaId
                + "/paradas/"
                + paradaId
                + "/motoboy";
    }

    private static List<OccurrenceOption> occurrenceOptions() {
        return List.of(
                new OccurrenceOption("RACISMO", "Racismo"),
                new OccurrenceOption("HOMOFOBIA", "Homofobia"),
                new OccurrenceOption("TRANSFOBIA", "Transfobia"),
                new OccurrenceOption("AGRESSAO", "Agressao"),
                new OccurrenceOption("AMEACA", "Ameaca"),
                new OccurrenceOption("RECUSA_PAGAMENTO", "Recusa de pagamento"),
                new OccurrenceOption("ENDERECO_RISCO", "Endereco de risco"),
                new OccurrenceOption("AUSENTE", "Cliente ausente"),
                new OccurrenceOption("OUTRO", "Outro")
        );
    }

    public record OccurrenceOption(String value, String label) {
    }

    private static List<FailureOption> failureStatusOptions() {
        return List.of(
                new FailureOption(
                        "TENTATIVA_SEM_SUCESSO",
                        "Tentativa sem sucesso"
                ),
                new FailureOption("REAGENDAR", "Reagendar entrega")
        );
    }

    private static List<FailureOption> failureReasonOptions() {
        return List.of(
                new FailureOption("AUSENTE", "Cliente ausente"),
                new FailureOption("ENDERECO_RISCO", "Endereco de risco"),
                new FailureOption("RECUSA_PAGAMENTO", "Recusa de pagamento"),
                new FailureOption("AGUARDAR_CONTATO", "Aguardar contato"),
                new FailureOption("OUTRO", "Outro")
        );
    }

    public record FailureOption(String value, String label) {
    }
}
