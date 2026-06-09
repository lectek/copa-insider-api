package br.com.lectek.copainsider.adapters.inbound.web.controller;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ItemPedidoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.lectek.copainsider.application.service.PaymentMethodService;
import br.com.lectek.copainsider.application.service.delivery.AdminEntregaRouteService;
import br.com.lectek.copainsider.application.service.fiscal.PedidoFiscalPresentationService;
import br.com.lectek.copainsider.application.support.NavigationLinkSupport;
import br.com.lectek.copainsider.application.support.PedidoStatusSupport;
import br.com.lectek.copainsider.domain.enums.ModoEntrega;
import br.com.lectek.copainsider.domain.enums.StatusPedido;
import br.com.lectek.copainsider.domain.enums.TipoPagamento;
import br.com.lectek.copainsider.domain.financeiro.mercadopago.MercadoPagoCheckoutService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpStatus;

@Controller
@RequestMapping("/cliente/pedidos")
public class ClientePedidosController {

    /**
     * Repository used to fetch customer orders.
     */
    private final PedidoRepository pedidoRepository;

    /**
     * Repository used to resolve authenticated user data.
     */
    private final UsuarioRepository usuarioRepository;

    /**
     * Service used to map payment method ids to labels.
     */
    private final PaymentMethodService paymentMethodService;
    private final PedidoFiscalPresentationService pedidoFiscalPresentationService;
    private final AdminEntregaRouteService adminEntregaRouteService;
    private final MercadoPagoCheckoutService mercadoPagoCheckoutService;

    /**
     * Builds the controller with required dependencies.
     *
     * @param pedidoRepo order repository
     * @param usuarioRepo user repository
     * @param paymentService payment method service
     */
    public ClientePedidosController(
            final PedidoRepository pedidoRepo,
            final UsuarioRepository usuarioRepo,
            final PaymentMethodService paymentService,
            final PedidoFiscalPresentationService pedidoFiscalPresentationServiceValue,
            final AdminEntregaRouteService adminEntregaRouteServiceValue,
            final MercadoPagoCheckoutService mercadoPagoCheckoutServiceValue
    ) {
        this.pedidoRepository = pedidoRepo;
        this.usuarioRepository = usuarioRepo;
        this.paymentMethodService = paymentService;
        this.pedidoFiscalPresentationService = pedidoFiscalPresentationServiceValue;
        this.adminEntregaRouteService = adminEntregaRouteServiceValue;
        this.mercadoPagoCheckoutService = mercadoPagoCheckoutServiceValue;
    }

    /**
     * Lists all orders for the authenticated customer.
     *
     * @param model model used by thymeleaf
     * @param auth authenticated principal
     * @return customer order list page
     */
    @GetMapping
    public String listar(final Model model, final Authentication auth) {
        final ClienteIdentidade identidade = resolveIdentidade(auth);
        if (identidade == null) {
            model.addAttribute("pedidos", List.of());
            return "pages/cliente/pedidos/lista";
        }
        final List<PedidoResumoView> pedidos = pedidoRepository
                .listarPorCliente(identidade.email(), identidade.cpf())
                .stream()
                .map(pedido ->
                        PedidoResumoView.from(pedido, paymentMethodService)
                )
                .toList();
        model.addAttribute("pedidos", pedidos);
        return "pages/cliente/pedidos/lista";
    }

    /**
     * Shows one order detail page for the authenticated customer.
     *
     * @param id order identifier
     * @param model model used by thymeleaf
     * @param auth authenticated principal
     * @param redirectAttributes redirect flash attributes
     * @return detail page or redirect to order list
     */
    @GetMapping("/{id}")
    public String detalhe(
            @PathVariable("id") final Long id,
            @RequestParam(name = "payment_id", required = false)
            final String paymentId,
            final Model model,
            final Authentication auth,
            final RedirectAttributes redirectAttributes
    ) {
        final ClienteIdentidade identidade = resolveIdentidade(auth);
        if (identidade == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Usuario nao encontrado."
            );
            return "redirect:/cliente/pedidos";
        }
        final Optional<PedidoEntity> pedido =
                pedidoRepository.buscarDetalhePorCliente(
                        id,
                        identidade.email(),
                        identidade.cpf()
                );
        if (pedido.isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Pedido nao encontrado."
            );
            return "redirect:/cliente/pedidos";
        }
        final PedidoEntity entity = pedido.get();
        try {
            mercadoPagoCheckoutService.ensureCheckoutForPedido(entity);
        } catch (IllegalArgumentException | IllegalStateException ignored) {
            // Keep rendering the order even if checkout regeneration fails.
        }
        if (paymentId != null && !paymentId.isBlank()) {
            try {
                mercadoPagoCheckoutService.syncPaymentForPedido(
                        entity,
                        paymentId
                );
            } catch (IllegalArgumentException
                     | IllegalStateException
                     | NoSuchElementException ignored) {
                // Keep rendering the order even when Mercado Pago sync fails.
            }
        }
        model.addAttribute(
                "pedido",
                PedidoDetalheView.from(
                        entity,
                        paymentMethodService,
                        mercadoPagoCheckoutService
                )
        );
        model.addAttribute(
                "fiscal",
                pedidoFiscalPresentationService.build(entity)
        );
        model.addAttribute(
                "tracking",
                adminEntregaRouteService.getCustomerTrackingView(entity.getId())
        );
        model.addAttribute(
                "deliveryIncident",
                adminEntregaRouteService.getLatestCustomerDeliveryIncident(entity.getId())
        );
        return "pages/cliente/pedidos/detalhe";
    }

    @GetMapping("/{id}/rastreamento")
    @ResponseBody
    public AdminEntregaRouteService.CustomerTrackingView rastreamento(
            @PathVariable("id") final Long id,
            final Authentication auth
    ) {
        ensurePedidoDoCliente(id, auth);
        return adminEntregaRouteService.getCustomerTrackingView(id);
    }

    private PedidoEntity ensurePedidoDoCliente(
            final Long id,
            final Authentication auth
    ) {
        final ClienteIdentidade identidade = resolveIdentidade(auth);
        if (identidade == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario nao encontrado."
            );
        }
        return pedidoRepository.buscarDetalhePorCliente(
                id,
                identidade.email(),
                identidade.cpf()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Pedido nao encontrado."
        ));
    }

    /**
     * Resolves the authenticated user from e-mail or CPF principal.
     *
     * @param auth authenticated principal
     * @return user when found
     */
    private Optional<UsuarioEntity> localizarUsuario(
            final Authentication auth
    ) {
        if (auth == null || auth.getName() == null) {
            return Optional.empty();
        }
        return usuarioRepository.findByEmailOrCpf(auth.getName());
    }

    /**
     * Resolves e-mail/cpf identity used to filter customer orders.
     *
     * @param auth authenticated principal
     * @return identity object or null when authentication is invalid
     */
    private ClienteIdentidade resolveIdentidade(final Authentication auth) {
        if (auth == null
                || auth.getName() == null
                || auth.getName().isBlank()) {
            return null;
        }
        final Optional<UsuarioEntity> usuario = localizarUsuario(auth);
        if (usuario.isPresent()) {
            final UsuarioEntity usuarioAtual = usuario.get();
            return new ClienteIdentidade(
                    usuarioAtual.getEmail(),
                    usuarioAtual.getCpf()
            );
        }
        final String principal = auth.getName().trim();
        return new ClienteIdentidade(principal, principal);
    }

    /**
     * Lightweight identity tuple used in customer order queries.
     *
     * @param email user e-mail
     * @param cpf user cpf
     */
    private record ClienteIdentidade(String email, String cpf) { }

    /**
     * List view payload for customer order page.
     *
     * @param id order id
     * @param data order date
     * @param total order total
     * @param status order status text
     * @param metodoPagamento payment method label
     */
    public record PedidoResumoView(
            Long id,
            LocalDateTime data,
            BigDecimal total,
            String status,
            String metodoPagamento
    ) {
        /**
         * Builds list view payload from entity.
         *
         * @param pedido order entity
         * @param paymentService payment method service
         * @return summarized order data
         */
        static PedidoResumoView from(
                final PedidoEntity pedido,
                final PaymentMethodService paymentService
        ) {
            final String metodo = resolveMetodoLabel(pedido, paymentService);
            final String statusAtual = resolveStatusLabel(pedido, paymentService);
            return new PedidoResumoView(
                    pedido.getId(),
                    pedido.getData(),
                    pedido.getTotal(),
                    statusAtual,
                    metodo
            );
        }
    }

    /**
     * Item view payload for order detail page.
     *
     * @param produto product name
     * @param quantidade item quantity
     * @param subtotal item subtotal
     */
    public record ItemView(
            String produto,
            Integer quantidade,
            BigDecimal subtotal
    ) {
        /**
         * Builds item view payload from order item entity.
         *
         * @param item order item entity
         * @return item view data
         */
        static ItemView from(final ItemPedidoEntity item) {
            final String nome = item.getProduto() != null
                    ? item.getProduto().getNome()
                    : "Produto";
            return new ItemView(
                    nome,
                    item.getQuantidade(),
                    item.getSubtotal()
            );
        }
    }

    /**
     * Detail view payload for customer order detail page.
     *
     * @param id order id
     * @param data order date
     * @param total order total
     * @param status order status text
     * @param metodoPagamento payment method label
     * @param itens order items
     */
    public record PedidoDetalheView(
            Long id,
            LocalDateTime data,
            BigDecimal total,
            String status,
            String metodoPagamento,
            String modoEntrega,
            String enderecoEntrega,
            String googleMapsUrl,
            String wazeUrl,
            String codigoEntrega,
            LocalDateTime codigoEntregaConfirmadoEm,
            String statusMensagem,
            String gatewayCheckoutUrl,
            String gatewayPaymentStatusLabel,
            String gatewayPaymentTicketUrl,
            String gatewayPixQrCode,
            String gatewayPixQrCodeBase64,
            boolean podePagarOnline,
            boolean exibePixQr,
            List<StatusTimelineStep> timeline,
            List<ItemView> itens
    ) {
        /**
         * Builds detail view payload from order entity.
         *
         * @param pedido order entity
         * @param paymentService payment method service
         * @return detail view data
         */
        static PedidoDetalheView from(
                final PedidoEntity pedido,
                final PaymentMethodService paymentService,
                final MercadoPagoCheckoutService checkoutService
        ) {
            final String metodo = resolveMetodoLabel(pedido, paymentService);
            final List<ItemView> itens = pedido.getItens() == null
                    ? List.of()
                    : pedido.getItens().stream().map(ItemView::from).toList();
            final String statusAtual = resolveStatusLabel(pedido, paymentService);
            return new PedidoDetalheView(
                    pedido.getId(),
                    pedido.getData(),
                    pedido.getTotal(),
                    statusAtual,
                    metodo,
                    resolveModoEntregaLabel(pedido.getModoEntrega()),
                    pedido.getEnderecoEntrega(),
                    buildGoogleMapsUrl(pedido),
                    buildWazeUrl(pedido),
                    pedido.getCodigoEntrega(),
                    pedido.getCodigoEntregaConfirmadoEm(),
                    buildStatusMessage(pedido, paymentService),
                    pedido.getGatewayCheckoutUrl(),
                    checkoutService.resolvePaymentStatusLabel(pedido),
                    pedido.getGatewayPaymentTicketUrl(),
                    pedido.getGatewayPixQrCode(),
                    pedido.getGatewayPixQrCodeBase64(),
                    checkoutService.hasPaymentAction(pedido),
                    checkoutService.hasPixPayload(pedido),
                    buildTimeline(pedido, paymentService),
                    itens
            );
        }
    }

    public record StatusTimelineStep(
            String label,
            String description,
            String state
    ) {
    }

    private static String resolveStatusLabel(
            final PedidoEntity pedido,
            final PaymentMethodService paymentService
    ) {
        final boolean paymentOnDelivery =
                paymentService.isOfflineValue(pedido.getMetodoPagamento())
                        || pedido.getTipoPagamento() == TipoPagamento.DINHEIRO;
        final StatusPedido effectiveStatus = paymentOnDelivery
                && pedido.getStatus() == StatusPedido.AGUARDANDO_PAGAMENTO
                ? StatusPedido.ABERTO
                : pedido.getStatus();
        return PedidoStatusSupport.customerLabel(
                effectiveStatus,
                pedido.getModoEntrega()
        );
    }

    private static List<StatusTimelineStep> buildTimeline(
            final PedidoEntity pedido,
            final PaymentMethodService paymentService
    ) {
        final StatusPedido effectiveStatus = resolveEffectiveStatus(
                pedido,
                paymentService
        );
        if (pedido.getModoEntrega() == ModoEntrega.RETIRADA) {
            return List.of(
                    step(
                            "Pedido recebido",
                            "Seu pedido entrou na fila da loja.",
                            pickupStepState(1, effectiveStatus)
                    ),
                    step(
                            "Separando pedido",
                            "A equipe esta organizando os itens da retirada.",
                            pickupStepState(2, effectiveStatus)
                    ),
                    step(
                            "Pronto para retirada",
                            "Voce ja pode ir ate a loja com o codigo informado.",
                            pickupStepState(3, effectiveStatus)
                    ),
                    step(
                            "Retirado",
                            "Pedido entregue em balcao.",
                            pickupStepState(4, effectiveStatus)
                    )
            );
        }
        return List.of(
                step(
                        "Pedido recebido",
                        "Seu pedido foi recebido pela farmacia.",
                        deliveryStepState(1, effectiveStatus)
                ),
                step(
                        "Pronto para entrega",
                        "Os itens foram separados para seguir rota.",
                        deliveryStepState(2, effectiveStatus)
                ),
                step(
                        "Saiu para entrega",
                        "O motoboy ja esta em rota para o endereco.",
                        deliveryStepState(3, effectiveStatus)
                ),
                step(
                        "Entregue",
                        "Pedido finalizado com sucesso.",
                        deliveryStepState(4, effectiveStatus)
                )
        );
    }

    private static StatusTimelineStep step(
            final String label,
            final String description,
            final String state
    ) {
        return new StatusTimelineStep(label, description, state);
    }

    private static String buildStatusMessage(
            final PedidoEntity pedido,
            final PaymentMethodService paymentService
    ) {
        final StatusPedido effectiveStatus = resolveEffectiveStatus(
                pedido,
                paymentService
        );
        final boolean paymentOnDelivery = isPaymentOnDelivery(
                pedido,
                paymentService
        );
        return switch (effectiveStatus) {
            case ABERTO -> paymentOnDelivery
                    ? "Seu pedido foi recebido e esta sendo preparado. O pagamento sera confirmado no recebimento."
                    : "Seu pedido foi recebido e aguarda o proximo passo da equipe para liberar a separacao.";
            case AGUARDANDO_PAGAMENTO ->
                    "Seu pedido foi recebido e aguarda a confirmacao do pagamento para iniciar a preparacao.";
            case PAGO -> pedido.getModoEntrega() == ModoEntrega.RETIRADA
                    ? "Pagamento confirmado. Agora estamos preparando o pedido para retirada."
                    : "Pagamento confirmado. Agora estamos preparando sua entrega.";
            case PRONTO_PARA_ENTREGA ->
                    "Pedido separado. O motoboy sera acionado na proxima saida de rota.";
            case PRONTO_PARA_RETIRADA ->
                    "Seu pedido ja pode ser retirado na loja com o codigo informado.";
            case SAIU_PARA_ENTREGA, ENVIADO ->
                    "Seu pedido esta em rota. Deixe o codigo de confirmacao em maos.";
            case ENTREGUE -> "Pedido concluido com sucesso.";
            case CANCELADO -> "Esse pedido foi cancelado.";
        };
    }

    private static StatusPedido resolveEffectiveStatus(
            final PedidoEntity pedido,
            final PaymentMethodService paymentService
    ) {
        final boolean paymentOnDelivery = isPaymentOnDelivery(
                pedido,
                paymentService
        );
        return paymentOnDelivery
                && pedido.getStatus() == StatusPedido.AGUARDANDO_PAGAMENTO
                ? StatusPedido.ABERTO
                : pedido.getStatus();
    }

    private static boolean isPaymentOnDelivery(
            final PedidoEntity pedido,
            final PaymentMethodService paymentService
    ) {
        return paymentService.isOfflineValue(pedido.getMetodoPagamento())
                || pedido.getTipoPagamento() == TipoPagamento.DINHEIRO;
    }

    private static String deliveryStepState(
            final int step,
            final StatusPedido status
    ) {
        final int currentStep = switch (status) {
            case ABERTO, AGUARDANDO_PAGAMENTO, PAGO -> 1;
            case PRONTO_PARA_ENTREGA -> 2;
            case SAIU_PARA_ENTREGA, ENVIADO -> 3;
            case ENTREGUE -> 4;
            case CANCELADO -> 1;
            case PRONTO_PARA_RETIRADA -> 1;
        };
        return timelineState(step, currentStep, status == StatusPedido.CANCELADO);
    }

    private static String pickupStepState(
            final int step,
            final StatusPedido status
    ) {
        final int currentStep = switch (status) {
            case ABERTO, AGUARDANDO_PAGAMENTO -> 1;
            case PAGO -> 2;
            case PRONTO_PARA_RETIRADA -> 3;
            case ENTREGUE -> 4;
            case CANCELADO -> 1;
            case PRONTO_PARA_ENTREGA, SAIU_PARA_ENTREGA, ENVIADO -> 2;
        };
        return timelineState(step, currentStep, status == StatusPedido.CANCELADO);
    }

    private static String timelineState(
            final int step,
            final int currentStep,
            final boolean cancelled
    ) {
        if (cancelled) {
            return step == currentStep ? "current" : "pending";
        }
        if (step < currentStep) {
            return "completed";
        }
        if (step == currentStep) {
            return "current";
        }
        return "pending";
    }

    /**
     * Resolves payment method label for order list/detail views.
     *
     * @param pedido order entity
     * @param paymentService payment method service
     * @return resolved label
     */
    private static String resolveMetodoLabel(
            final PedidoEntity pedido,
            final PaymentMethodService paymentService
    ) {
        if (pedido.getMetodoPagamento() != null
                && !pedido.getMetodoPagamento().isBlank()) {
            return paymentService.resolveLabel(pedido.getMetodoPagamento());
        }
        return pedido.getTipoPagamento() != null
                ? pedido.getTipoPagamento().name()
                : "DESCONHECIDO";
    }

    private static String buildGoogleMapsUrl(final PedidoEntity pedido) {
        if (pedido.getModoEntrega() == ModoEntrega.RETIRADA) {
            return "";
        }
        return NavigationLinkSupport.googleMapsDirections(
                pedido.getEnderecoEntrega()
        );
    }

    private static String buildWazeUrl(final PedidoEntity pedido) {
        if (pedido.getModoEntrega() == ModoEntrega.RETIRADA) {
            return "";
        }
        return NavigationLinkSupport.wazeNavigate(pedido.getEnderecoEntrega());
    }

    private static String resolveModoEntregaLabel(final ModoEntrega modoEntrega) {
        if (modoEntrega == null) {
            return "Entrega";
        }
        return modoEntrega == ModoEntrega.RETIRADA
                ? "Retirada na loja"
                : "Entrega";
    }
}
