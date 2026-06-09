package br.com.lectek.copainsider.adapters.inbound.web;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.lectek.copainsider.application.dto.response.AlertItemDTO;
import br.com.lectek.copainsider.application.dto.response.PainelAdminResponseDTO;
import br.com.lectek.copainsider.application.service.AdminMetricsService;
import br.com.lectek.copainsider.application.service.MetaVendaDashboardService;
import br.com.lectek.copainsider.application.support.PedidoStatusSupport;
import br.com.lectek.copainsider.domain.enums.StatusPedido;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminDashboardController {

    private static final Locale LOCALE_PT_BR = Locale.forLanguageTag("pt-BR");

    private final AdminMetricsService metricsService;
    private final ObjectProvider<PedidoRepository> pedidoRepository;
    private final MetaVendaDashboardService metaVendaDashboardService;

    public AdminDashboardController(
            final AdminMetricsService metricsService,
            final ObjectProvider<PedidoRepository> pedidoRepository,
            final MetaVendaDashboardService metaVendaDashboardService
    ) {
        this.metricsService = metricsService;
        this.pedidoRepository = pedidoRepository;
        this.metaVendaDashboardService = metaVendaDashboardService;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(
            final Model model,
            @RequestParam(value = "period", required = false) final String period
    ) {
        populateDashboardModel(model, period);
        return "pages/admin/dashboard";
    }

    @GetMapping("/admin/index")
    public String index(
            final Model model,
            @RequestParam(value = "period", required = false) final String period
    ) {
        populateDashboardModel(model, period);
        return "pages/admin/index";
    }

    @PostMapping("/admin/dashboard/meta-diaria")
    public String atualizarMetaDiaria(
            @RequestParam("valor") final String valor,
            @RequestParam(value = "returnTo", required = false)
            final String returnTo,
            final RedirectAttributes ra
    ) {
        try {
            final BigDecimal metaAtualizada =
                    metaVendaDashboardService.atualizarMetaDiaria(valor);
            ra.addFlashAttribute(
                    "success",
                    "Meta diaria atualizada para "
                            + NumberFormat.getCurrencyInstance(LOCALE_PT_BR)
                                    .format(metaAtualizada)
                            + "."
            );
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:" + resolveRedirectTarget(returnTo);
    }

    private void populateDashboardModel(
            final Model model,
            final String period
    ) {
        final PainelAdminResponseDTO painel = metricsService.montarPainel();
        final MetaVendaDashboardService.MetaVendaPainel metaVendaPainel =
                metaVendaDashboardService.carregarPainel(LocalDate.now());
        final List<AlertItemDTO> alertas = painel.getAlertas() == null
                ? new ArrayList<>()
                : new ArrayList<>(painel.getAlertas());
        alertas.add(new AlertItemDTO("META", metaVendaPainel.alertaResumo()));
        painel.setAlertas(alertas);

        model.addAttribute("painelAdmin", painel);
        model.addAttribute("metaVendaPainel", metaVendaPainel);

        final Map<StatusPedido, Long> porStatus = painel.getPedidosPorStatus();
        final var chartLabels = new ArrayList<String>();
        final var chartData = new ArrayList<Long>();

        if (porStatus != null && !porStatus.isEmpty()) {
            for (final StatusPedido status : StatusPedido.values()) {
                if (porStatus.containsKey(status)) {
                    chartLabels.add(status.name());
                    chartData.add(porStatus.get(status));
                }
            }
        }

        model.addAttribute("chartStatusLabels", chartLabels);
        model.addAttribute("chartStatusData", chartData);
        model.addAttribute("period", period);
        model.addAttribute("ultimosPedidos", buscarUltimosPedidos(period));
    }

    private static String resolveRedirectTarget(final String returnTo) {
        if ("index".equalsIgnoreCase(returnTo)) {
            return "/admin/index";
        }
        return "/admin/dashboard";
    }

    @GetMapping("/admin/alertas")
    public String alertas(
            final Model model,
            @RequestParam(value = "tipo", required = false) final String tipo
    ) {
        final PainelAdminResponseDTO painel = metricsService.montarPainel();
        final List<AlertItemDTO> alertas = painel.getAlertas() == null
                ? List.of()
                : painel.getAlertas();
        List<AlertItemDTO> filtrados = alertas;
        if (tipo != null && !tipo.isBlank()) {
            final String filtro = tipo.trim();
            filtrados = alertas.stream()
                    .filter(alerta -> alerta.getTipo() != null
                            && alerta.getTipo().equalsIgnoreCase(filtro))
                    .toList();
        }

        final List<String> tiposDisponiveis = alertas.stream()
                .map(alerta -> alerta.getTipo() == null ? "" : alerta.getTipo().trim())
                .filter(value -> !value.isBlank())
                .distinct()
                .sorted()
                .toList();

        model.addAttribute("painelAdmin", painel);
        model.addAttribute("alertasFiltrados", filtrados);
        model.addAttribute("tipoSelecionado", tipo == null ? "" : tipo.trim());
        model.addAttribute("tiposDisponiveis", tiposDisponiveis);
        return "pages/admin/alertas";
    }

    private List<PedidoResumoAdminView> buscarUltimosPedidos(final String period) {
        final PedidoRepository repo = pedidoRepository.getIfAvailable();
        if (repo == null) {
            return List.of();
        }
        final LocalDateTime de = resolvePeriodo(period);
        final List<PedidoEntity> pedidos = repo.listarRecentes(de, PageRequest.of(0, 5));
        if (pedidos.isEmpty()) {
            return List.of();
        }
        final List<Long> ids = pedidos.stream()
                .map(PedidoEntity::getId)
                .filter(id -> id != null)
                .toList();
        final Map<Long, Long> itensPorPedido = carregarItensPorPedido(repo, ids);
        return pedidos.stream()
                .map(pedido -> PedidoResumoAdminView.from(
                        pedido,
                        itensPorPedido.getOrDefault(pedido.getId(), 0L)
                ))
                .toList();
    }

    private static LocalDateTime resolvePeriodo(final String period) {
        if (period == null || period.isBlank()) {
            return null;
        }
        return switch (period) {
            case "today" -> LocalDate.now().atStartOfDay();
            case "week" -> LocalDateTime.now().minusDays(7);
            case "month" -> LocalDateTime.now().minusDays(30);
            default -> null;
        };
    }

    private static Map<Long, Long> carregarItensPorPedido(
            final PedidoRepository repo,
            final List<Long> ids
    ) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        final Map<Long, Long> map = new HashMap<>();
        for (final var row : repo.contarItensPorPedidos(ids)) {
            if (row.getId() != null) {
                map.put(
                        row.getId(),
                        row.getTotalItens() != null ? row.getTotalItens() : 0L
                );
            }
        }
        return map;
    }

    private static String resolveStatusLabel(final StatusPedido status) {
        return PedidoStatusSupport.adminLabel(status, null);
    }

    private static String resolveStatusClass(final StatusPedido status) {
        if (status == null) {
            return "badge--neutral";
        }
        return switch (status) {
            case ABERTO, AGUARDANDO_PAGAMENTO, ENVIADO, PRONTO_PARA_RETIRADA,
                    PRONTO_PARA_ENTREGA, SAIU_PARA_ENTREGA ->
                    "badge--warning";
            case CANCELADO -> "badge--danger";
            case PAGO, ENTREGUE -> "badge--success";
        };
    }

    public record PedidoResumoAdminView(
            Long id,
            String clienteNome,
            LocalDateTime data,
            BigDecimal total,
            String status,
            String statusLabel,
            String statusClass,
            Long totalItens
    ) {
        static PedidoResumoAdminView from(
                final PedidoEntity pedido,
                final Long totalItens
        ) {
            final String cliente = pedido.getCliente() != null
                    && pedido.getCliente().getNome() != null
                    ? pedido.getCliente().getNome()
                    : "Cliente";
            final StatusPedido statusValue = pedido.getStatus();
            final String status = statusValue != null
                    ? statusValue.name()
                    : "DESCONHECIDO";
            return new PedidoResumoAdminView(
                    pedido.getId(),
                    cliente,
                    pedido.getData(),
                    pedido.getTotal(),
                    status,
                    resolveStatusLabel(statusValue),
                    resolveStatusClass(statusValue),
                    totalItens != null ? totalItens : 0L
            );
        }
    }
}
