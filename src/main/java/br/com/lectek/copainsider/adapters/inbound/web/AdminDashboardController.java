package br.com.lectek.copainsider.adapters.inbound.web;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.CopaAcessoJPARepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.CopaCompraJPARepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.CopaProdutoJPARepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.UsuarioJpaRepository;
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
import org.springframework.data.domain.Sort;
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
    private final UsuarioJpaRepository usuarioRepo;
    private final CopaProdutoJPARepository copaProdutoRepo;
    private final CopaCompraJPARepository copaCompraRepo;
    private final CopaAcessoJPARepository copaAcessoRepo;

    public AdminDashboardController(
            final AdminMetricsService metricsService,
            final ObjectProvider<PedidoRepository> pedidoRepository,
            final MetaVendaDashboardService metaVendaDashboardService,
            final UsuarioJpaRepository usuarioRepo,
            final CopaProdutoJPARepository copaProdutoRepo,
            final CopaCompraJPARepository copaCompraRepo,
            final CopaAcessoJPARepository copaAcessoRepo
    ) {
        this.metricsService = metricsService;
        this.pedidoRepository = pedidoRepository;
        this.metaVendaDashboardService = metaVendaDashboardService;
        this.usuarioRepo = usuarioRepo;
        this.copaProdutoRepo = copaProdutoRepo;
        this.copaCompraRepo = copaCompraRepo;
        this.copaAcessoRepo = copaAcessoRepo;
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
            @RequestParam(value = "returnTo", required = false) final String returnTo,
            final RedirectAttributes ra
    ) {
        try {
            final BigDecimal metaAtualizada =
                    metaVendaDashboardService.atualizarMetaDiaria(valor);
            ra.addFlashAttribute(
                    "success",
                    "Meta diária atualizada para "
                            + NumberFormat.getCurrencyInstance(LOCALE_PT_BR)
                                    .format(metaAtualizada)
                            + "."
            );
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:" + resolveRedirectTarget(returnTo);
    }

    private void populateDashboardModel(final Model model, final String period) {
        // Métricas Copa
        final long totalUsuarios = usuarioRepo.count();
        final long totalProdutos = copaProdutoRepo.findByAtivoTrueOrderByOrdemAsc().size();
        final long totalCompras  = copaCompraRepo.count();
        final long totalAcessos  = copaAcessoRepo.count();
        final BigDecimal receitaTotal = copaCompraRepo.sumValorAprovado();

        final List<UsuarioResumoView> usuariosRecentes = usuarioRepo
                .findAll(PageRequest.of(0, 5, Sort.by("id").descending()))
                .stream()
                .map(UsuarioResumoView::from)
                .toList();

        final var comprasRecentes = copaCompraRepo.findTop5ByOrderByCriadoEmDesc();
        final var acessosRecentes = copaAcessoRepo
                .findAllByOrderByConcedidoEmDesc(PageRequest.of(0, 5)).getContent();

        model.addAttribute("totalUsuarios",   totalUsuarios);
        model.addAttribute("totalProdutos",   totalProdutos);
        model.addAttribute("totalCompras",    totalCompras);
        model.addAttribute("totalAcessos",    totalAcessos);
        model.addAttribute("receitaTotal",    receitaTotal != null ? receitaTotal : BigDecimal.ZERO);
        model.addAttribute("comprasRecentes", comprasRecentes);
        model.addAttribute("acessosRecentes", acessosRecentes);
        model.addAttribute("usuariosRecentes", usuariosRecentes);

        // Métricas legadas (mantidas para /admin/index se necessário)
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
                    .filter(a -> a.getTipo() != null && a.getTipo().equalsIgnoreCase(filtro))
                    .toList();
        }
        final List<String> tiposDisponiveis = alertas.stream()
                .map(a -> a.getTipo() == null ? "" : a.getTipo().trim())
                .filter(v -> !v.isBlank())
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
                .map(p -> PedidoResumoAdminView.from(p, itensPorPedido.getOrDefault(p.getId(), 0L)))
                .toList();
    }

    private static LocalDateTime resolvePeriodo(final String period) {
        if (period == null || period.isBlank()) {
            return null;
        }
        return switch (period) {
            case "today" -> LocalDate.now().atStartOfDay();
            case "week"  -> LocalDateTime.now().minusDays(7);
            case "month" -> LocalDateTime.now().minusDays(30);
            default      -> null;
        };
    }

    private static Map<Long, Long> carregarItensPorPedido(
            final PedidoRepository repo, final List<Long> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        final Map<Long, Long> map = new HashMap<>();
        for (final var row : repo.contarItensPorPedidos(ids)) {
            if (row.getId() != null) {
                map.put(row.getId(), row.getTotalItens() != null ? row.getTotalItens() : 0L);
            }
        }
        return map;
    }

    // ── View models ───────────────────────────────────────────────────────────

    public record UsuarioResumoView(
            Long id,
            String nome,
            String email,
            LocalDateTime criadoEm
    ) {
        static UsuarioResumoView from(final UsuarioEntity u) {
            return new UsuarioResumoView(u.getId(), u.getNome(), u.getEmail(), u.getCreatedAt());
        }
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
        static PedidoResumoAdminView from(final PedidoEntity pedido, final Long totalItens) {
            final String cliente = pedido.getCliente() != null
                    && pedido.getCliente().getNome() != null
                    ? pedido.getCliente().getNome()
                    : "Cliente";
            final StatusPedido statusValue = pedido.getStatus();
            final String status = statusValue != null ? statusValue.name() : "DESCONHECIDO";
            return new PedidoResumoAdminView(
                    pedido.getId(),
                    cliente,
                    pedido.getData(),
                    pedido.getTotal(),
                    status,
                    PedidoStatusSupport.adminLabel(statusValue, null),
                    resolveStatusClass(statusValue),
                    totalItens != null ? totalItens : 0L
            );
        }

        private static String resolveStatusClass(final StatusPedido status) {
            if (status == null) return "badge--neutral";
            return switch (status) {
                case ABERTO, AGUARDANDO_PAGAMENTO, ENVIADO, PRONTO_PARA_RETIRADA,
                     PRONTO_PARA_ENTREGA, SAIU_PARA_ENTREGA -> "badge--warning";
                case CANCELADO -> "badge--danger";
                case PAGO, ENTREGUE -> "badge--success";
            };
        }
    }
}
