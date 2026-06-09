package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteNotificacaoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EntregaParadaEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ItemPedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoFiscalSnapshotEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteNotificacaoRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.EntregaParadaRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.application.service.PaymentMethodService;
import br.com.redemaisfarma.application.service.fiscal.FiscalOrderEmissionService;
import br.com.redemaisfarma.application.service.fiscal.PedidoFiscalPresentationService;
import br.com.redemaisfarma.application.service.fiscal.PedidoFiscalSnapshotService;
import br.com.redemaisfarma.application.support.NavigationLinkSupport;
import br.com.redemaisfarma.application.support.PedidoStatusSupport;
import br.com.redemaisfarma.domain.enums.EntregaRotaStatus;
import br.com.redemaisfarma.domain.enums.MotivoCancelamentoPedido;
import br.com.redemaisfarma.domain.enums.ModoEntrega;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import br.com.redemaisfarma.domain.enums.TipoPagamento;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/pedidos")
public class AdminPedidosController {

    private static final int RECENT_PAGE_INDEX = 0;
    private static final int RECENT_PAGE_SIZE = 200;
    private static final int ORDER_NUMBER_DIGITS = 4;
    private static final int PRECO_SCALE = 2;
    private static final String DEFAULT_LABEL = "Cliente";
    private static final String DEFAULT_UNKNOWN = "-";
    private static final String STATUS_DESCONHECIDO = "DESCONHECIDO";
    private static final String ENDERECO_NAO_INFORMADO = "Nao informado";
    private static final String NAO_INFORMADO = "Nao informado";
    private static final String UI_PEDIDOS_TEXT_PREFIX = "ui.admin.pedidos.text.";
    private static final String UI_PEDIDOS_COLOR_PREFIX = "ui.admin.pedidos.color.";
    private static final String ERROR_CANCELAMENTO_SEM_MOTIVO =
            "Informe o motivo do cancelamento.";
    private static final String ERROR_CANCELAMENTO_PAGAMENTO =
            "Somente pedidos nao pagos podem ser cancelados pelo admin.";
    private static final List<EntregaRotaStatus> ACTIVE_DELIVERY_ROUTE_STATUSES =
            List.of(
                    EntregaRotaStatus.PLANEJADA,
                    EntregaRotaStatus.DESPACHADA,
                    EntregaRotaStatus.EM_EXECUCAO
            );

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteNotificacaoRepository notificacaoRepository;
    private final EntregaParadaRepository entregaParadaRepository;
    private final AppSettingService appSettingService;
    private final PaymentMethodService paymentMethodService;
    private final FiscalOrderEmissionService fiscalOrderEmissionService;
    private final PedidoFiscalSnapshotService pedidoFiscalSnapshotService;
    private final PedidoFiscalPresentationService pedidoFiscalPresentationService;

    public AdminPedidosController(
            final PedidoRepository pedidoRepositoryValue,
            final UsuarioRepository usuarioRepositoryValue,
            final ClienteNotificacaoRepository notificacaoRepositoryValue,
            final EntregaParadaRepository entregaParadaRepositoryValue,
            final AppSettingService appSettingServiceValue,
            final PaymentMethodService paymentMethodServiceValue,
            final FiscalOrderEmissionService fiscalOrderEmissionServiceValue,
            final PedidoFiscalSnapshotService pedidoFiscalSnapshotServiceValue,
            final PedidoFiscalPresentationService pedidoFiscalPresentationServiceValue
    ) {
        this.pedidoRepository = pedidoRepositoryValue;
        this.usuarioRepository = usuarioRepositoryValue;
        this.notificacaoRepository = notificacaoRepositoryValue;
        this.entregaParadaRepository = entregaParadaRepositoryValue;
        this.appSettingService = appSettingServiceValue;
        this.paymentMethodService = paymentMethodServiceValue;
        this.fiscalOrderEmissionService = fiscalOrderEmissionServiceValue;
        this.pedidoFiscalSnapshotService = pedidoFiscalSnapshotServiceValue;
        this.pedidoFiscalPresentationService = pedidoFiscalPresentationServiceValue;
    }

    @GetMapping
    public String listar(
            @RequestParam(name = "q", required = false) final String q,
            @RequestParam(name = "status", required = false) final String status,
            @RequestParam(name = "modoEntrega", required = false)
            final String modoEntrega,
            @RequestParam(name = "periodo", required = false) final String periodo,
            final Model model
    ) {
        final List<PedidoEntity> pedidos = pedidoRepository.listarRecentes(
                PageRequest.of(RECENT_PAGE_INDEX, RECENT_PAGE_SIZE)
        );
        final Map<Long, Long> itensPorPedido = contarItensPorPedido(pedidos);
        final List<PedidoEntity> pedidosBase = pedidos.stream()
                .filter(pedido -> matchesModoEntrega(pedido, modoEntrega))
                .filter(pedido -> matchesQuery(pedido, q))
                .filter(pedido -> matchesPeriodo(pedido, periodo))
                .toList();
        final List<PedidoListaView> lista = pedidosBase.stream()
                .filter(pedido -> matchesStatus(pedido, status))
                .map(pedido -> PedidoListaView.from(
                        pedido,
                        itensPorPedido.getOrDefault(pedido.getId(), 0L),
                        paymentMethodService
                ))
                .toList();
        model.addAttribute("pedidos", lista);
        model.addAttribute("resumoPedidos", resolveOrderListSummary(pedidosBase));
        model.addAttribute("ui", resolvePedidosListUi());
        model.addAttribute("searchQuery", q == null ? "" : q.trim());
        model.addAttribute("selectedStatus", status == null ? "" : status.trim());
        model.addAttribute(
                "selectedModoEntrega",
                modoEntrega == null
                        ? ""
                        : modoEntrega.trim().toUpperCase(Locale.ROOT)
        );
        model.addAttribute("selectedPeriodo", normalizePeriodo(periodo));
        return "pages/admin/pedidos/lista";
    }

    @GetMapping("/{id}")
    public String detalhe(
            @PathVariable("id") final Long id,
            final Model model,
            final RedirectAttributes ra
    ) {
        final Optional<PedidoEntity> pedido = pedidoRepository.buscarDetalheAdmin(id);
        if (pedido.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Pedido nao encontrado.");
            return "redirect:/admin/pedidos";
        }
        final PedidoEntity entity = pedido.get();
        final Optional<PedidoFiscalSnapshotEntity> snapshot =
                pedidoFiscalSnapshotService.findByPedidoId(entity.getId());
        model.addAttribute(
                "pedido",
                PedidoAdminDetalheView.from(
                        entity,
                        snapshot.orElse(null),
                        paymentMethodService
                )
        );
        model.addAttribute(
                "fiscal",
                pedidoFiscalPresentationService.build(entity)
        );
        model.addAttribute(
                "statusOptions",
                statusOptions(entity.getModoEntrega(), entity.getStatus())
        );
        resolveActiveRouteId(entity.getId()).ifPresent(
                routeId -> model.addAttribute("activeRouteId", routeId)
        );
        model.addAttribute("canCancelPedido", canCancelPedido(entity));
        model.addAttribute("cancelReasonOptions", cancelReasonOptions());
        return "pages/admin/pedidos/detalhe";
    }

    @PostMapping("/{id}/status")
    public String atualizarStatus(
            @PathVariable("id") final Long id,
            @RequestParam("status") final String status,
            @RequestParam(name = "cancelReason", required = false)
            final String cancelReason,
            @RequestParam(name = "formaPagamentoRecebida", required = false)
            final String formaPagamentoRecebida,
            @RequestParam(name = "avaliacaoCliente", required = false)
            final Integer avaliacaoCliente,
            @RequestParam(name = "fiscalAction", required = false)
            final String fiscalAction,
            @RequestParam(name = "redirectTo", required = false) final String redirectTo,
            final RedirectAttributes ra
    ) {
        final String redirectTarget = resolveRedirectTarget(redirectTo, id);
        final Optional<StatusPedido> novoStatus = parseStatus(status);
        if (novoStatus.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Status do pedido invalido.");
            return redirectTarget;
        }

        final Optional<PedidoEntity> pedidoOpt = pedidoRepository.buscarDetalheAdmin(id);
        if (pedidoOpt.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Pedido nao encontrado.");
            return redirectTarget;
        }

        final PedidoEntity pedido = pedidoOpt.get();
        final StatusPedido statusAtual = pedido.getStatus();
        final StatusPedido statusNovo = novoStatus.get();
        final Optional<MotivoCancelamentoPedido> motivoCancelamento =
                parseCancelReason(cancelReason);
        final String normalizedReceivedPayment = statusNovo == StatusPedido.PAGO
                ? normalizeReceivedPayment(formaPagamentoRecebida)
                : "";

        if (pedido.getModoEntrega() == ModoEntrega.RETIRADA
                && (statusNovo == StatusPedido.ENVIADO
                || statusNovo == StatusPedido.SAIU_PARA_ENTREGA
                || statusNovo == StatusPedido.PRONTO_PARA_ENTREGA)) {
            ra.addFlashAttribute(
                    "errorMessage",
                    "Pedidos de retirada nao usam status de entrega."
            );
            return redirectTarget;
        }
        if (pedido.getModoEntrega() != ModoEntrega.RETIRADA
                && statusNovo == StatusPedido.PRONTO_PARA_RETIRADA) {
            ra.addFlashAttribute(
                    "errorMessage",
                    "Pronto para retirada so pode ser usado em pedidos de retirada."
            );
            return redirectTarget;
        }
        if (statusNovo == StatusPedido.CANCELADO) {
            if (!canCancelPedido(pedido)) {
                ra.addFlashAttribute(
                        "errorMessage",
                        ERROR_CANCELAMENTO_PAGAMENTO
                );
                return redirectTarget;
            }
            if (motivoCancelamento.isEmpty()) {
                ra.addFlashAttribute(
                        "errorMessage",
                        ERROR_CANCELAMENTO_SEM_MOTIVO
                );
                return redirectTarget;
            }
        }
        if (statusNovo == StatusPedido.PAGO) {
            if (normalizedReceivedPayment.isBlank()) {
                ra.addFlashAttribute(
                        "errorMessage",
                        "Informe se o pagamento foi recebido em dinheiro, cartao ou pix."
                );
                return redirectTarget;
            }
            if (avaliacaoCliente == null) {
                ra.addFlashAttribute(
                        "errorMessage",
                        "Avalie o cliente para fechar o recebimento."
                );
                return redirectTarget;
            }
        }

        pedido.setStatus(statusNovo);
        if (statusNovo == StatusPedido.PAGO) {
            pedido.setFormaPagamentoRecebida(normalizedReceivedPayment);
            pedido.setPagamentoDivergente(
                    !expectedPaymentCategory(pedido).equals(
                            normalizedReceivedPayment
                    )
            );
            pedido.setAvaliacaoCliente(sanitizeRating(avaliacaoCliente));
            if (pedido.getPagamentoRecebidoEm() == null
                    || statusAtual != StatusPedido.PAGO) {
                pedido.setPagamentoRecebidoEm(LocalDateTime.now());
            }
        }
        if (statusNovo == StatusPedido.ENTREGUE) {
            pedido.setCodigoEntregaConfirmadoEm(LocalDateTime.now());
        } else if (statusAtual == StatusPedido.ENTREGUE) {
            pedido.setCodigoEntregaConfirmadoEm(null);
        }
        if (statusNovo == StatusPedido.CANCELADO) {
            pedido.setCancelamentoMotivo(motivoCancelamento.orElse(null));
            pedido.setCanceladoEm(LocalDateTime.now());
        } else if (statusAtual == StatusPedido.CANCELADO) {
            pedido.setCancelamentoMotivo(null);
            pedido.setCanceladoEm(null);
        }

        final Optional<UsuarioEntity> usuarioNotificacao =
                shouldNotifyCustomerStatus(pedido, statusAtual, statusNovo)
                        ? resolveUsuarioNotificacao(pedido)
                        : Optional.empty();

        pedidoRepository.save(pedido);
        final FiscalActionResult fiscalResult = statusNovo == StatusPedido.PAGO
                ? handleFiscalAction(pedido, fiscalAction)
                : FiscalActionResult.none();
        final boolean clienteNotificado = usuarioNotificacao.isPresent()
                && notifyClienteStatusChange(
                        usuarioNotificacao.get(),
                        pedido,
                        statusAtual,
                        statusNovo
                );

        if (fiscalResult.errorMessage() != null) {
            ra.addFlashAttribute("errorMessage", fiscalResult.errorMessage());
        }

        if (fiscalResult.successMessage() != null) {
            ra.addFlashAttribute("successMessage", fiscalResult.successMessage());
        } else if (clienteNotificado) {
            ra.addFlashAttribute("successMessage", notificationSuccessMessage(statusNovo));
        } else if (statusAtual == statusNovo) {
            ra.addFlashAttribute("successMessage", "Pedido ja estava com esse status.");
        } else if (statusNovo == StatusPedido.PAGO) {
            ra.addFlashAttribute("successMessage", "Pagamento registrado com sucesso.");
        } else {
            ra.addFlashAttribute("successMessage", "Status do pedido atualizado.");
        }
        return fiscalResult.redirectTarget() != null
                ? fiscalResult.redirectTarget()
                : redirectTarget;
    }

    private static boolean matchesStatus(
            final PedidoEntity pedido,
            final String raw
    ) {
        if (raw == null || raw.isBlank()) {
            return true;
        }
        final StatusPedido status = pedido.getStatus();
        if (status == null) {
            return false;
        }
        final String normalized = raw.trim().toUpperCase(Locale.ROOT);
        if ("AGUARDANDO".equals(normalized)) {
            return status == StatusPedido.AGUARDANDO_PAGAMENTO;
        }
        return normalized.equals(status.name());
    }

    static String statusLabel(final StatusPedido status) {
        return statusLabel(status, null);
    }

    static String statusLabel(
            final StatusPedido status,
            final ModoEntrega modoEntrega
    ) {
        return PedidoStatusSupport.adminLabel(status, modoEntrega);
    }

    static List<StatusOption> statusOptions(final ModoEntrega modoEntrega) {
        return statusOptions(modoEntrega, null);
    }

    static List<StatusOption> statusOptions(
            final ModoEntrega modoEntrega,
            final StatusPedido statusAtual
    ) {
        if (modoEntrega == ModoEntrega.RETIRADA) {
            final List<StatusOption> options = buildStatusOptions(
                    modoEntrega,
                    StatusPedido.ABERTO,
                    StatusPedido.AGUARDANDO_PAGAMENTO,
                    StatusPedido.PAGO,
                    StatusPedido.PRONTO_PARA_RETIRADA,
                    StatusPedido.ENTREGUE
            );
            appendLegacyCurrentOption(options, statusAtual, modoEntrega);
            return appendCurrentCancelledOption(options, statusAtual);
        }
        final List<StatusOption> options = buildStatusOptions(
                modoEntrega,
                StatusPedido.ABERTO,
                StatusPedido.AGUARDANDO_PAGAMENTO,
                StatusPedido.PAGO,
                StatusPedido.PRONTO_PARA_ENTREGA,
                StatusPedido.SAIU_PARA_ENTREGA,
                StatusPedido.ENTREGUE
        );
        appendLegacyCurrentOption(options, statusAtual, modoEntrega);
        return appendCurrentCancelledOption(options, statusAtual);
    }

    private static List<StatusOption> buildStatusOptions(
            final ModoEntrega modoEntrega,
            final StatusPedido... statuses
    ) {
        return Arrays.stream(statuses)
                .map(status -> new StatusOption(
                        status.name(),
                        statusLabel(status, modoEntrega)
                ))
                .toList();
    }

    private static Optional<StatusPedido> parseStatus(final String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        final String normalized = raw.trim().toUpperCase(Locale.ROOT);
        if ("AGUARDANDO".equals(normalized)) {
            return Optional.of(StatusPedido.AGUARDANDO_PAGAMENTO);
        }
        return Arrays.stream(StatusPedido.values())
                .filter(status -> status.name().equals(normalized))
                .findFirst();
    }

    private static Optional<MotivoCancelamentoPedido> parseCancelReason(
            final String raw
    ) {
        return MotivoCancelamentoPedido.fromValue(raw);
    }

    private static String resolveRedirectTarget(
            final String redirectTo,
            final Long id
    ) {
        if (redirectTo != null
                && !redirectTo.isBlank()
                && redirectTo.startsWith("/admin/")
                && !redirectTo.startsWith("//")) {
            return "redirect:" + redirectTo;
        }
        return "redirect:/admin/pedidos/" + id;
    }

    private Map<Long, Long> contarItensPorPedido(
            final List<PedidoEntity> pedidos
    ) {
        if (pedidos == null || pedidos.isEmpty()) {
            return Map.of();
        }
        final List<Long> ids = pedidos.stream()
                .map(PedidoEntity::getId)
                .filter(id -> id != null)
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return pedidoRepository.contarItensPorPedidos(ids).stream()
                .collect(Collectors.toMap(
                        PedidoRepository.PedidoItensCountRow::getId,
                        PedidoRepository.PedidoItensCountRow::getTotalItens
                ));
    }

    private static boolean shouldNotifyCustomerStatus(
            final PedidoEntity pedido,
            final StatusPedido statusAtual,
            final StatusPedido statusNovo
    ) {
        if (pedido.getModoEntrega() == ModoEntrega.RETIRADA) {
            return statusNovo == StatusPedido.PRONTO_PARA_RETIRADA
                    && statusAtual != StatusPedido.PRONTO_PARA_RETIRADA;
        }
        return (statusNovo == StatusPedido.PRONTO_PARA_ENTREGA
                && statusAtual != StatusPedido.PRONTO_PARA_ENTREGA)
                || (statusNovo == StatusPedido.SAIU_PARA_ENTREGA
                && statusAtual != StatusPedido.SAIU_PARA_ENTREGA);
    }

    private static boolean canCancelPedido(final PedidoEntity pedido) {
        if (pedido == null || pedido.getStatus() == null) {
            return false;
        }
        return pedido.getStatus() == StatusPedido.ABERTO
                || pedido.getStatus() == StatusPedido.AGUARDANDO_PAGAMENTO;
    }

    private static List<StatusOption> appendCurrentCancelledOption(
            final List<StatusOption> options,
            final StatusPedido statusAtual
    ) {
        if (statusAtual != StatusPedido.CANCELADO) {
            return options;
        }
        final List<StatusOption> extended = new java.util.ArrayList<>(options);
        extended.add(new StatusOption(
                StatusPedido.CANCELADO.name(),
                statusLabel(StatusPedido.CANCELADO)
        ));
        return extended;
    }

    private static void appendLegacyCurrentOption(
            final List<StatusOption> options,
            final StatusPedido statusAtual,
            final ModoEntrega modoEntrega
    ) {
        if (statusAtual != StatusPedido.ENVIADO) {
            return;
        }
        final boolean alreadyPresent = options.stream()
                .anyMatch(option -> option.value().equals(statusAtual.name()));
        if (alreadyPresent) {
            return;
        }
        options.add(new StatusOption(
                statusAtual.name(),
                statusLabel(statusAtual, modoEntrega)
        ));
    }

    private static List<CancelReasonOption> cancelReasonOptions() {
        return Arrays.stream(MotivoCancelamentoPedido.values())
                .map(item -> new CancelReasonOption(item.name(), item.getLabel()))
                .toList();
    }

    private boolean notifyClienteStatusChange(
            final UsuarioEntity usuario,
            final PedidoEntity pedido,
            final StatusPedido statusAtual,
            final StatusPedido statusNovo
    ) {
        if (pedido.getModoEntrega() == ModoEntrega.RETIRADA
                && statusNovo == StatusPedido.PRONTO_PARA_RETIRADA
                && statusAtual != StatusPedido.PRONTO_PARA_RETIRADA) {
            return saveNotification(
                    usuario,
                    "Pedido pronto para retirada",
                    buildPickupReadyMessage(pedido)
            );
        }
        if (pedido.getModoEntrega() != ModoEntrega.RETIRADA
                && statusNovo == StatusPedido.PRONTO_PARA_ENTREGA
                && statusAtual != StatusPedido.PRONTO_PARA_ENTREGA) {
            return saveNotification(
                    usuario,
                    "Pedido pronto para entrega",
                    buildDeliveryReadyMessage(pedido)
            );
        }
        if (pedido.getModoEntrega() != ModoEntrega.RETIRADA
                && statusNovo == StatusPedido.SAIU_PARA_ENTREGA
                && statusAtual != StatusPedido.SAIU_PARA_ENTREGA) {
            return saveNotification(
                    usuario,
                    "Pedido saiu para entrega",
                    buildOutForDeliveryMessage(pedido)
            );
        }
        return false;
    }

    private boolean saveNotification(
            final UsuarioEntity usuario,
            final String titulo,
            final String mensagem
    ) {
        final ClienteNotificacaoEntity notificacao = new ClienteNotificacaoEntity();
        notificacao.setUsuario(usuario);
        notificacao.setTipo("PEDIDO");
        notificacao.setTitulo(titulo);
        notificacao.setMensagem(mensagem);
        notificacaoRepository.save(notificacao);
        return true;
    }

    private Optional<UsuarioEntity> resolveUsuarioNotificacao(
            final PedidoEntity pedido
    ) {
        final ClienteEntity cliente = pedido.getCliente();
        if (cliente == null) {
            return Optional.empty();
        }
        if (cliente.getEmail() != null && !cliente.getEmail().isBlank()) {
            final Optional<UsuarioEntity> byEmail = usuarioRepository.findByEmailOrCpf(
                    cliente.getEmail().trim()
            );
            if (byEmail.isPresent()) {
                return byEmail;
            }
        }
        if (cliente.getCpf() != null && !cliente.getCpf().isBlank()) {
            return usuarioRepository.findByEmailOrCpf(cliente.getCpf().trim());
        }
        return Optional.empty();
    }

    private static String buildPickupReadyMessage(final PedidoEntity pedido) {
        final String numero = pedido.getId() != null
                ? String.format("%0" + ORDER_NUMBER_DIGITS + "d", pedido.getId())
                : DEFAULT_UNKNOWN;
        final StringBuilder message = new StringBuilder(
                "Seu pedido #" + numero + " esta pronto para retirada na loja."
        );
        if (pedido.getCodigoEntrega() != null && !pedido.getCodigoEntrega().isBlank()) {
            message.append(" Codigo de retirada: ")
                    .append(pedido.getCodigoEntrega())
                    .append('.');
        }
        return message.toString();
    }

    private static String buildDeliveryReadyMessage(final PedidoEntity pedido) {
        final StringBuilder message = new StringBuilder(
                "Seu pedido #" + resolveOrderNumber(pedido.getId())
                        + " esta pronto para entrega."
        );
        if (pedido.getCodigoEntrega() != null && !pedido.getCodigoEntrega().isBlank()) {
            message.append(" Codigo de confirmacao: ")
                    .append(pedido.getCodigoEntrega())
                    .append('.');
        }
        return message.toString();
    }

    private static String buildOutForDeliveryMessage(final PedidoEntity pedido) {
        final StringBuilder message = new StringBuilder(
                "Seu pedido #" + resolveOrderNumber(pedido.getId())
                        + " saiu para entrega."
        );
        if (pedido.getCodigoEntrega() != null && !pedido.getCodigoEntrega().isBlank()) {
            message.append(" Codigo de confirmacao: ")
                    .append(pedido.getCodigoEntrega())
                    .append('.');
        }
        return message.toString();
    }

    private static String notificationSuccessMessage(final StatusPedido statusNovo) {
        return switch (statusNovo) {
            case PRONTO_PARA_RETIRADA ->
                    "Status atualizado e cliente avisado para retirada.";
            case PRONTO_PARA_ENTREGA ->
                    "Status atualizado e cliente avisado que o pedido esta pronto para entrega.";
            case SAIU_PARA_ENTREGA ->
                    "Status atualizado e cliente avisado que o pedido saiu para entrega.";
            default -> "Status do pedido atualizado.";
        };
    }

    private FiscalActionResult handleFiscalAction(
            final PedidoEntity pedido,
            final String rawFiscalAction
    ) {
        final FiscalAction action = FiscalAction.from(rawFiscalAction);
        if (action == FiscalAction.NONE) {
            return FiscalActionResult.none();
        }

        final PedidoFiscalSnapshotEntity snapshot =
                pedidoFiscalSnapshotService.captureIfMissing(
                        pedido,
                        "ADMIN_PEDIDO_STATUS"
                );
        pedidoFiscalSnapshotService.updatePaymentMethod(
                pedido.getId(),
                pedido.getFormaPagamentoRecebida()
        );

        if (action == FiscalAction.EMAIL) {
            final String email = firstNonBlank(
                    snapshot.getEmailDeliveryAddress(),
                    snapshot.getRecipientEmail(),
                    pedido.getCliente() == null
                            ? null
                            : pedido.getCliente().getEmail()
            );
            if (email == null) {
                return new FiscalActionResult(
                        null,
                        "Pagamento registrado com sucesso.",
                        "Pagamento registrado, mas o pedido nao possui e-mail para a nota fiscal."
                );
            }
            pedidoFiscalSnapshotService.updateDeliveryPreferences(
                    pedido.getId(),
                    new PedidoFiscalSnapshotService.UpdateDeliveryPreferencesRequest(
                            snapshot.getPrintChannel(),
                            true,
                            email
                    )
            );
        }

        final boolean fiscalTriggered = fiscalOrderEmissionService.processPaidOrder(
                pedido.getId(),
                action == FiscalAction.EMAIL
                        ? "ADMIN_PEDIDO_PAGO_EMAIL"
                        : "ADMIN_PEDIDO_PAGO_IMPRESSAO"
        ).isPresent();

        if (!fiscalTriggered) {
            return new FiscalActionResult(
                    null,
                    "Pagamento registrado com sucesso.",
                    "Pagamento registrado, mas a emissao fiscal ainda nao foi iniciada."
            );
        }

        if (action == FiscalAction.PRINT) {
            return new FiscalActionResult(
                    "redirect:/admin/fiscal/impressao?pedidoId=" + pedido.getId(),
                    "Pagamento registrado. A nota fiscal foi encaminhada para impressao.",
                    null
            );
        }
        return new FiscalActionResult(
                null,
                "Pagamento registrado. A nota fiscal sera enviada por e-mail assim que autorizada.",
                null
        );
    }

    private static BigDecimal resolvePreco(final ItemPedidoEntity item) {
        if (item.getPrecoUnitario() != null) {
            return item.getPrecoUnitario();
        }

        final ProdutoEntity produto = item.getProduto();
        if (produto != null && produto.getPrecoVenda() != null) {
            return produto.getPrecoVenda();
        }

        final Integer qtd = item.getQuantidade();
        final BigDecimal subtotal = item.getSubtotal();
        if (qtd != null && qtd > 0 && subtotal != null) {
            return subtotal.divide(
                    BigDecimal.valueOf(qtd),
                    PRECO_SCALE,
                    RoundingMode.HALF_UP
            );
        }
        return BigDecimal.ZERO;
    }

    private static boolean matchesModoEntrega(
            final PedidoEntity pedido,
            final String raw
    ) {
        if (raw == null || raw.isBlank()) {
            return true;
        }
        final ModoEntrega modoEntrega = pedido.getModoEntrega();
        if (modoEntrega == null) {
            return false;
        }
        return modoEntrega.name().equals(raw.trim().toUpperCase(Locale.ROOT));
    }

    private static boolean matchesPeriodo(
            final PedidoEntity pedido,
            final String raw
    ) {
        final String periodo = normalizePeriodo(raw);
        if (periodo.isBlank()) {
            return true;
        }
        if (pedido.getData() == null) {
            return false;
        }
        final LocalDate pedidoData = pedido.getData().toLocalDate();
        final LocalDate hoje = LocalDate.now();
        return switch (periodo) {
            case "HOJE" -> pedidoData.isEqual(hoje);
            case "7_DIAS" -> !pedidoData.isBefore(hoje.minusDays(6));
            case "30_DIAS" -> !pedidoData.isBefore(hoje.minusDays(29));
            default -> true;
        };
    }

    private static String normalizePeriodo(final String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        return raw.trim().toUpperCase(Locale.ROOT);
    }

    private OrderListSummary resolveOrderListSummary(final List<PedidoEntity> pedidos) {
        if (pedidos == null || pedidos.isEmpty()) {
            return new OrderListSummary(0L, 0L, 0L, 0L);
        }
        long pedidosDoDia = pedidos.stream().filter(AdminPedidosController::isPedidoDoDia).count();
        long emEntrega = pedidos.stream().filter(this::isEmEntregaSummary).count();
        long aguardando = pedidos.stream().filter(this::isAguardandoSummary).count();
        long cancelados = pedidos.stream()
                .filter(pedido -> pedido != null && pedido.getStatus() == StatusPedido.CANCELADO)
                .count();
        return new OrderListSummary(pedidosDoDia, emEntrega, aguardando, cancelados);
    }

    private boolean isEmEntregaSummary(final PedidoEntity pedido) {
        if (pedido == null || pedido.getStatus() == null) {
            return false;
        }
        return pedido.getStatus() == StatusPedido.PRONTO_PARA_ENTREGA
                || pedido.getStatus() == StatusPedido.SAIU_PARA_ENTREGA
                || pedido.getStatus() == StatusPedido.ENVIADO;
    }

    private boolean isAguardandoSummary(final PedidoEntity pedido) {
        if (pedido == null) {
            return false;
        }
        final StatusPedido effectiveStatus = resolveEffectiveStatus(
                pedido,
                this.paymentMethodService
        );
        return effectiveStatus == StatusPedido.AGUARDANDO_PAGAMENTO
                || effectiveStatus == StatusPedido.ABERTO;
    }

    private static boolean isPedidoDoDia(final PedidoEntity pedido) {
        return pedido != null
                && pedido.getData() != null
                && pedido.getData().toLocalDate().isEqual(LocalDate.now());
    }

    private PedidosListUiConfig resolvePedidosListUi() {
        LinkedHashMap<String, String> defaultTexts = new LinkedHashMap<>();
        defaultTexts.put("title", "Pedidos");
        defaultTexts.put("subtitle", "Acompanhe pedidos, entregas e prioridade operacional com leitura limpa.");
        defaultTexts.put("summary.today", "Pedidos do dia");
        defaultTexts.put("summary.delivery", "Em entrega");
        defaultTexts.put("summary.waiting", "Aguardando");
        defaultTexts.put("summary.cancelled", "Cancelados");
        defaultTexts.put("summary.todayHint", "Movimento filtrado para hoje");
        defaultTexts.put("summary.deliveryHint", "Pedidos na operacao de entrega");
        defaultTexts.put("summary.waitingHint", "Pedidos aguardando andamento");
        defaultTexts.put("summary.cancelledHint", "Pedidos encerrados sem conclusao");
        defaultTexts.put("searchPlaceholder", "Buscar por pedido, cliente, e-mail, CPF, telefone ou codigo");
        defaultTexts.put("filter.statusLabel", "Status");
        defaultTexts.put("filter.typeLabel", "Tipo");
        defaultTexts.put("filter.dateLabel", "Data");
        defaultTexts.put("filter.submit", "Filtrar");
        defaultTexts.put("filter.clear", "Limpar");
        defaultTexts.put("action.deliveryPanel", "Painel de entregas");
        defaultTexts.put("action.settings", "Configurar textos e cores");
        defaultTexts.put("action.details", "Detalhes");
        defaultTexts.put("action.route", "Ver rota");
        defaultTexts.put("action.panel", "Painel de entrega");
        defaultTexts.put("action.markReady", "Pronto para entrega");
        defaultTexts.put("card.total", "Valor total");
        defaultTexts.put("card.client", "Cliente");
        defaultTexts.put("card.payment", "Pagamento");
        defaultTexts.put("card.deliveryCode", "Codigo de entrega");
        defaultTexts.put("card.date", "Atualizado");
        defaultTexts.put("empty", "Nenhum pedido encontrado para os filtros informados.");

        LinkedHashMap<String, String> defaultColors = new LinkedHashMap<>();
        defaultColors.put("gradient.from", "#b91c1c");
        defaultColors.put("gradient.mid", "#1d4ed8");
        defaultColors.put("gradient.to", "#0f172a");
        defaultColors.put("tone.delivery", "#15803d");
        defaultColors.put("tone.prep", "#ca8a04");
        defaultColors.put("tone.waiting", "#2563eb");
        defaultColors.put("tone.cancelled", "#dc2626");

        Map<String, String> persisted = this.appSettingService.getAllByKeys(
                defaultTexts.keySet().stream().map(key -> UI_PEDIDOS_TEXT_PREFIX + key).collect(Collectors.toSet())
        );
        Map<String, String> persistedColors = this.appSettingService.getAllByKeys(
                defaultColors.keySet().stream().map(key -> UI_PEDIDOS_COLOR_PREFIX + key).collect(Collectors.toSet())
        );

        return new PedidosListUiConfig(
                resolveUiValues(defaultTexts, persisted, UI_PEDIDOS_TEXT_PREFIX),
                resolveUiValues(defaultColors, persistedColors, UI_PEDIDOS_COLOR_PREFIX),
                "/admin/settings?q=ui.admin.pedidos"
        );
    }

    private Map<String, String> resolveUiValues(final Map<String, String> defaults,
                                                final Map<String, String> persisted,
                                                final String prefix) {
        LinkedHashMap<String, String> resolved = new LinkedHashMap<>();
        defaults.forEach((key, defaultValue) -> {
            String value = persisted == null ? null : persisted.get(prefix + key);
            resolved.put(key, value == null || value.isBlank() ? defaultValue : value.trim());
        });
        return resolved;
    }

    private static String resolveMetodoLabel(
            final PedidoEntity pedido,
            final PaymentMethodService paymentMethodServiceValue,
            final TipoPagamento tipoPagamento
    ) {
        if (pedido.getFormaPagamentoRecebida() != null
                && !pedido.getFormaPagamentoRecebida().isBlank()) {
            return formatPaymentCategoryLabel(pedido.getFormaPagamentoRecebida());
        }
        if (pedido.getMetodoPagamento() != null
                && !pedido.getMetodoPagamento().isBlank()) {
            return paymentMethodServiceValue.resolveLabel(
                    pedido.getMetodoPagamento()
            );
        }
        return resolveTipoPagamentoLabel(tipoPagamento);
    }

    private static String resolveDisplayStatusLabel(
            final PedidoEntity pedido,
            final PaymentMethodService paymentMethodServiceValue
    ) {
        final StatusPedido effectiveStatus = resolveEffectiveStatus(
                pedido,
                paymentMethodServiceValue
        );
        return AdminPedidosController.statusLabel(
                effectiveStatus,
                pedido.getModoEntrega()
        );
    }

    private static String resolveStatusTone(final StatusPedido status) {
        if (status == null) {
            return "waiting";
        }
        return switch (status) {
            case CANCELADO -> "cancelled";
            case AGUARDANDO_PAGAMENTO -> "waiting";
            case PRONTO_PARA_ENTREGA, SAIU_PARA_ENTREGA, ENVIADO, ENTREGUE -> "delivery";
            case ABERTO, PAGO, PRONTO_PARA_RETIRADA -> "prep";
            default -> "prep";
        };
    }

    private static String resolveDeliveryActionLabel(final PedidoEntity pedido) {
        if (pedido == null || pedido.getStatus() == null) {
            return "Painel de entrega";
        }
        return switch (pedido.getStatus()) {
            case SAIU_PARA_ENTREGA, ENVIADO, ENTREGUE -> "Ver rota";
            default -> "Painel de entrega";
        };
    }

    private static StatusPedido resolveEffectiveStatus(
            final PedidoEntity pedido,
            final PaymentMethodService paymentMethodServiceValue
    ) {
        final boolean paymentOnDelivery =
                paymentMethodServiceValue.isOfflineValue(
                        pedido.getMetodoPagamento()
                ) || pedido.getTipoPagamento() == TipoPagamento.DINHEIRO;
        return paymentOnDelivery
                && pedido.getStatus() == StatusPedido.AGUARDANDO_PAGAMENTO
                ? StatusPedido.ABERTO
                : pedido.getStatus();
    }

    private static boolean canMarkReadyForDelivery(
            final PedidoEntity pedido,
            final PaymentMethodService paymentMethodServiceValue
    ) {
        if (pedido.getModoEntrega() != ModoEntrega.ENTREGA) {
            return false;
        }
        final StatusPedido effectiveStatus = resolveEffectiveStatus(
                pedido,
                paymentMethodServiceValue
        );
        return effectiveStatus == StatusPedido.ABERTO
                || effectiveStatus == StatusPedido.PAGO;
    }

    private static boolean canOpenDeliveryPanel(final PedidoEntity pedido) {
        if (pedido.getModoEntrega() != ModoEntrega.ENTREGA
                || pedido.getStatus() == null) {
            return false;
        }
        return pedido.getStatus() == StatusPedido.PRONTO_PARA_ENTREGA
                || pedido.getStatus() == StatusPedido.SAIU_PARA_ENTREGA
                || pedido.getStatus() == StatusPedido.ENVIADO;
    }

    private static String resolveEnderecoEntrega(final PedidoEntity pedido) {
        if (pedido.getEnderecoEntrega() == null || pedido.getEnderecoEntrega().isBlank()) {
            if (pedido.getModoEntrega() == ModoEntrega.RETIRADA) {
                return "";
            }
            return ENDERECO_NAO_INFORMADO;
        }
        return pedido.getEnderecoEntrega();
    }

    private static String resolveEnderecoEntrega(
            final PedidoEntity pedido,
            final PedidoFiscalSnapshotEntity snapshot
    ) {
        final String endereco = firstNonBlank(
                snapshot == null ? null : snapshot.getRecipientAddress(),
                pedido.getEnderecoEntrega()
        );
        if (endereco == null) {
            if (pedido.getModoEntrega() == ModoEntrega.RETIRADA) {
                return "";
            }
            return ENDERECO_NAO_INFORMADO;
        }
        return endereco;
    }

    private static String resolveModoEntregaLabel(final ModoEntrega modoEntrega) {
        if (modoEntrega == ModoEntrega.RETIRADA) {
            return "Retirada na loja";
        }
        return "Entrega";
    }

    private static String resolveTipoPagamentoLabel(
            final TipoPagamento tipoPagamento
    ) {
        if (tipoPagamento == null) {
            return NAO_INFORMADO;
        }
        return switch (tipoPagamento) {
            case PIX -> "Pix";
            case BOLETO -> "Boleto";
            case CARTAO_CREDITO -> "Cartao de credito";
            case CARTAO_DEBITO -> "Cartao de debito";
            case DINHEIRO -> "Dinheiro";
            case CUSTOM -> "Personalizado";
        };
    }

    private static String normalizeReceivedPayment(final String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return "";
        }
        final String normalized = rawValue.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "pix" -> "PIX";
            case "dinheiro", "cash" -> "DINHEIRO";
            case "cartao", "cartão", "credito", "crédito",
                    "debito", "débito", "cartao_credito",
                    "cartao_debito", "maquineta", "pos" -> "CARTAO";
            default -> "OUTRO";
        };
    }

    private static String expectedPaymentCategory(final PedidoEntity pedido) {
        if (pedido == null) {
            return "OUTRO";
        }
        final String fromMethod = normalizeReceivedPayment(
                pedido.getMetodoPagamento()
        );
        if (!fromMethod.isBlank()) {
            return fromMethod;
        }
        if (pedido.getTipoPagamento() == null) {
            return "OUTRO";
        }
        return switch (pedido.getTipoPagamento()) {
            case PIX -> "PIX";
            case DINHEIRO -> "DINHEIRO";
            case CARTAO_CREDITO, CARTAO_DEBITO -> "CARTAO";
            default -> "OUTRO";
        };
    }

    private static Integer sanitizeRating(final Integer rawValue) {
        if (rawValue == null) {
            return null;
        }
        if (rawValue < 0) {
            return 0;
        }
        if (rawValue > 5) {
            return 5;
        }
        return rawValue;
    }

    private static String formatPaymentCategoryLabel(final String category) {
        if (category == null || category.isBlank()) {
            return DEFAULT_UNKNOWN;
        }
        return switch (category.trim().toUpperCase(Locale.ROOT)) {
            case "PIX" -> "Pix";
            case "DINHEIRO" -> "Dinheiro";
            case "CARTAO" -> "Cartao";
            default -> "Outro";
        };
    }

    private static String resolveCancelamentoMotivoLabel(
            final MotivoCancelamentoPedido motivo
    ) {
        return motivo == null ? DEFAULT_UNKNOWN : motivo.getLabel();
    }

    private static String resolveOrderNumber(final Long id) {
        return id != null
                ? String.format("%0" + ORDER_NUMBER_DIGITS + "d", id)
                : DEFAULT_UNKNOWN;
    }

    private static String resolveClienteNome(
            final PedidoEntity pedido,
            final PedidoFiscalSnapshotEntity snapshot
    ) {
        return firstNonBlank(
                snapshot == null ? null : snapshot.getRecipientName(),
                pedido.getCliente() == null ? null : pedido.getCliente().getNome(),
                DEFAULT_LABEL
        );
    }

    private static String resolveClienteEmail(
            final PedidoEntity pedido,
            final PedidoFiscalSnapshotEntity snapshot
    ) {
        return firstNonBlank(
                snapshot == null ? null : snapshot.getRecipientEmail(),
                pedido.getCliente() == null ? null : pedido.getCliente().getEmail(),
                DEFAULT_UNKNOWN
        );
    }

    private static String resolveClienteTelefone(
            final PedidoEntity pedido,
            final PedidoFiscalSnapshotEntity snapshot
    ) {
        return firstNonBlank(
                snapshot == null ? null : snapshot.getRecipientPhone(),
                pedido.getCliente() == null ? null : pedido.getCliente().getTelefone(),
                DEFAULT_UNKNOWN
        );
    }

    private static String resolveClienteCpf(
            final PedidoEntity pedido,
            final PedidoFiscalSnapshotEntity snapshot
    ) {
        return firstNonBlank(
                snapshot == null ? null : snapshot.getRecipientDocument(),
                pedido.getCliente() == null ? null : pedido.getCliente().getCpf(),
                DEFAULT_UNKNOWN
        );
    }

    private static String firstNonBlank(final String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private static boolean containsText(
            final String value,
            final String query
    ) {
        return value != null
                && query != null
                && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private static String normalizeDigits(final String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\\D", "");
    }

    public record StatusOption(String value, String label) {
    }

    public record PagamentoView(
            String metodo,
            String tipo,
            String metodoRecebidoValor,
            String metodoRecebido,
            boolean pagamentoDivergente,
            Integer avaliacaoCliente,
            LocalDateTime pagamentoRecebidoEm
    ) {
    }

    public record CancelReasonOption(String value, String label) {
    }

    public record OrderListSummary(
            long pedidosDoDia,
            long emEntrega,
            long aguardando,
            long cancelados
    ) {
    }

    public record PedidosListUiConfig(
            Map<String, String> texts,
            Map<String, String> colors,
            String settingsHref
    ) {
    }

    private enum FiscalAction {
        NONE,
        PRINT,
        EMAIL;

        static FiscalAction from(final String rawValue) {
            if (rawValue == null || rawValue.isBlank()) {
                return NONE;
            }
            return switch (rawValue.trim().toUpperCase(Locale.ROOT)) {
                case "PRINT", "IMPRIMIR" -> PRINT;
                case "EMAIL" -> EMAIL;
                default -> NONE;
            };
        }
    }

    private record FiscalActionResult(
            String redirectTarget,
            String successMessage,
            String errorMessage
    ) {
        static FiscalActionResult none() {
            return new FiscalActionResult(null, null, null);
        }
    }

    public record ItemView(
            String nome,
            Integer qtd,
            BigDecimal preco,
            BigDecimal subtotal
    ) {
        static ItemView from(final ItemPedidoEntity item) {
            final String nome = item.getProduto() != null
                    && item.getProduto().getNome() != null
                    ? item.getProduto().getNome()
                    : "Produto";
            return new ItemView(
                    nome,
                    item.getQuantidade(),
                    resolvePreco(item),
                    item.getSubtotal()
            );
        }
    }

    public record PedidoAdminDetalheView(
            Long id,
            String numero,
            LocalDateTime data,
            String clienteNome,
            String clienteEmail,
            String clienteTelefone,
            String clienteCpf,
            String modoEntrega,
            String enderecoEntrega,
            String googleMapsUrl,
            String wazeUrl,
            String codigoEntrega,
            LocalDateTime codigoEntregaGeradoEm,
            LocalDateTime codigoEntregaConfirmadoEm,
            PagamentoView pagamento,
            BigDecimal total,
            String status,
            String statusLabel,
            String cancelamentoMotivo,
            LocalDateTime canceladoEm,
            long totalItens,
            boolean canMarkReadyForDelivery,
            boolean canOpenDeliveryPanel,
            List<ItemView> itens
    ) {
        static PedidoAdminDetalheView from(
                final PedidoEntity pedido,
                final PedidoFiscalSnapshotEntity snapshot,
                final PaymentMethodService paymentMethodServiceValue
        ) {
            final String clienteNome = resolveClienteNome(pedido, snapshot);
            final String clienteEmail = resolveClienteEmail(pedido, snapshot);
            final String statusRaw = pedido.getStatus() != null
                    ? pedido.getStatus().name()
                    : STATUS_DESCONHECIDO;
            final TipoPagamento tipoPagamento = pedido.getTipoPagamento();
            final String metodo = resolveMetodoLabel(
                    pedido,
                    paymentMethodServiceValue,
                    tipoPagamento
            );
            final PagamentoView pagamento =
                    new PagamentoView(
                            metodo,
                            resolveTipoPagamentoLabel(tipoPagamento),
                            pedido.getFormaPagamentoRecebida(),
                            formatPaymentCategoryLabel(
                                    pedido.getFormaPagamentoRecebida()
                            ),
                            pedido.isPagamentoDivergente(),
                            sanitizeRating(pedido.getAvaliacaoCliente()),
                            pedido.getPagamentoRecebidoEm()
                    );
            final List<ItemView> itens = pedido.getItens() == null
                    ? List.of()
                    : pedido.getItens().stream().map(ItemView::from).toList();
            return new PedidoAdminDetalheView(
                    pedido.getId(),
                    resolveOrderNumber(pedido.getId()),
                    pedido.getData(),
                    clienteNome,
                    clienteEmail,
                    resolveClienteTelefone(pedido, snapshot),
                    resolveClienteCpf(pedido, snapshot),
                    resolveModoEntregaLabel(pedido.getModoEntrega()),
                    resolveEnderecoEntrega(pedido, snapshot),
                    buildGoogleMapsUrl(
                            pedido,
                            resolveEnderecoEntrega(pedido, snapshot)
                    ),
                    buildWazeUrl(
                            pedido,
                            resolveEnderecoEntrega(pedido, snapshot)
                    ),
                    pedido.getCodigoEntrega(),
                    pedido.getCodigoEntregaGeradoEm(),
                    pedido.getCodigoEntregaConfirmadoEm(),
                    pagamento,
                    pedido.getTotal(),
                    statusRaw,
                    resolveDisplayStatusLabel(
                            pedido,
                            paymentMethodServiceValue
                    ),
                    resolveCancelamentoMotivoLabel(pedido.getCancelamentoMotivo()),
                    pedido.getCanceladoEm(),
                    itens.stream()
                            .map(ItemView::qtd)
                            .filter(qtd -> qtd != null)
                            .mapToLong(Integer::longValue)
                            .sum(),
                    AdminPedidosController.canMarkReadyForDelivery(
                            pedido,
                            paymentMethodServiceValue
                    ),
                    AdminPedidosController.canOpenDeliveryPanel(pedido),
                    itens
            );
        }
    }

    public record PedidoListaView(
            Long id,
            String numero,
            String clienteNome,
            String clienteEmail,
            String clienteTelefone,
            LocalDateTime data,
            long totalItens,
            String modoEntrega,
            String codigoEntrega,
            String metodoPagamento,
            BigDecimal total,
            String status,
            String statusLabel,
            String statusTone,
            String deliveryActionLabel,
            boolean canMarkReadyForDelivery,
            boolean canOpenDeliveryPanel
    ) {
        static PedidoListaView from(
                final PedidoEntity pedido,
                final long totalItensValue,
                final PaymentMethodService paymentMethodServiceValue
        ) {
            final String cliente = pedido.getCliente() != null
                    && pedido.getCliente().getNome() != null
                    ? pedido.getCliente().getNome()
                    : DEFAULT_LABEL;
            final String status = pedido.getStatus() != null
                    ? pedido.getStatus().name()
                    : STATUS_DESCONHECIDO;
            final StatusPedido effectiveStatus = resolveEffectiveStatus(
                    pedido,
                    paymentMethodServiceValue
            );
            final String statusLabelText =
                    resolveDisplayStatusLabel(
                            pedido,
                            paymentMethodServiceValue
                    );
            return new PedidoListaView(
                    pedido.getId(),
                    resolveOrderNumber(pedido.getId()),
                    cliente,
                    firstNonBlank(
                            pedido.getCliente() == null
                                    ? null
                                    : pedido.getCliente().getEmail(),
                            DEFAULT_UNKNOWN
                    ),
                    firstNonBlank(
                            pedido.getCliente() == null
                                    ? null
                                    : pedido.getCliente().getTelefone(),
                            DEFAULT_UNKNOWN
                    ),
                    pedido.getData(),
                    totalItensValue,
                    resolveModoEntregaLabel(pedido.getModoEntrega()),
                    firstNonBlank(pedido.getCodigoEntrega(), DEFAULT_UNKNOWN),
                    resolveMetodoLabel(
                            pedido,
                            paymentMethodServiceValue,
                            pedido.getTipoPagamento()
                    ),
                    pedido.getTotal(),
                    status,
                    statusLabelText,
                    resolveStatusTone(effectiveStatus),
                    resolveDeliveryActionLabel(pedido),
                    AdminPedidosController.canMarkReadyForDelivery(
                            pedido,
                            paymentMethodServiceValue
                    ),
                    AdminPedidosController.canOpenDeliveryPanel(pedido)
            );
        }
    }

    private static boolean matchesQuery(
            final PedidoEntity pedido,
            final String raw
    ) {
        if (raw == null || raw.isBlank()) {
            return true;
        }
        final String query = raw.trim().toLowerCase(Locale.ROOT);
        final String queryDigits = normalizeDigits(raw);
        if (pedido.getId() != null) {
            final String orderNumber = resolveOrderNumber(pedido.getId());
            if (pedido.getId().toString().contains(query)
                    || orderNumber.toLowerCase(Locale.ROOT).contains(query)) {
                return true;
            }
        }
        if (containsText(pedido.getCodigoEntrega(), query)
                || containsText(resolveEnderecoEntrega(pedido), query)
                || containsText(resolveModoEntregaLabel(pedido.getModoEntrega()), query)
                || containsText(
                        statusLabel(pedido.getStatus(), pedido.getModoEntrega()),
                        query
                )) {
            return true;
        }
        if (pedido.getCliente() == null) {
            return false;
        }
        final ClienteEntity cliente = pedido.getCliente();
        if (containsText(cliente.getNome(), query)
                || containsText(cliente.getEmail(), query)
                || containsText(cliente.getTelefone(), query)
                || containsText(cliente.getCpf(), query)) {
            return true;
        }
        return !queryDigits.isEmpty() && (
                normalizeDigits(cliente.getTelefone()).contains(queryDigits)
                        || normalizeDigits(cliente.getCpf()).contains(queryDigits)
        );
    }

    private static String buildGoogleMapsUrl(
            final PedidoEntity pedido,
            final String endereco
    ) {
        if (pedido.getModoEntrega() == ModoEntrega.RETIRADA) {
            return "";
        }
        return NavigationLinkSupport.googleMapsDirections(endereco);
    }

    private static String buildWazeUrl(
            final PedidoEntity pedido,
            final String endereco
    ) {
        if (pedido.getModoEntrega() == ModoEntrega.RETIRADA) {
            return "";
        }
        return NavigationLinkSupport.wazeNavigate(endereco);
    }

    private Optional<Long> resolveActiveRouteId(final Long pedidoId) {
        if (pedidoId == null) {
            return Optional.empty();
        }
        return entregaParadaRepository
                .findByPedidoIdInRouteStatuses(
                        pedidoId,
                        ACTIVE_DELIVERY_ROUTE_STATUSES
                )
                .stream()
                .map(EntregaParadaEntity::getRota)
                .filter(java.util.Objects::nonNull)
                .map(route -> route.getId())
                .filter(java.util.Objects::nonNull)
                .findFirst();
    }
}
