package br.com.lectek.copainsider.application.controller.admin;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.lectek.copainsider.application.service.PaymentMethodService;
import br.com.lectek.copainsider.application.support.PedidoStatusSupport;
import br.com.lectek.copainsider.domain.enums.StatusPedido;
import br.com.lectek.copainsider.domain.enums.TipoPagamento;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/financeiro")
public class FinanceiroPagamentosAdminController {

    /**
     * Maximum number of recent orders loaded for the payments page.
     */
    private static final int RECENT_PAGE_SIZE = 200;

    /**
     * First page index used for recent orders listing.
     */
    private static final int FIRST_PAGE_INDEX = 0;

    /**
     * Number of digits used in order number formatting.
     */
    private static final int ORDER_NUMBER_DIGITS = 4;

    /**
     * Default text for missing values.
     */
    private static final String DEFAULT_UNKNOWN = "-";

    /**
     * Locale used to format BRL currency values.
     */
    private static final Locale LOCALE_PT_BR = Locale.forLanguageTag("pt-BR");

    /**
     * Repository for order reads.
     */
    private final PedidoRepository pedidoRepository;

    /**
     * Service used to resolve payment method labels.
     */
    private final PaymentMethodService paymentMethodService;

    /**
     * Creates controller with dependencies.
     *
     * @param pedidoRepositoryValue order repository
     * @param paymentMethodServiceValue payment method service
     */
    public FinanceiroPagamentosAdminController(
            final PedidoRepository pedidoRepositoryValue,
            final PaymentMethodService paymentMethodServiceValue
    ) {
        this.pedidoRepository = pedidoRepositoryValue;
        this.paymentMethodService = paymentMethodServiceValue;
    }

    /**
     * Renders the admin payments page with optional filters.
     *
     * @param q optional text filter
     * @param status optional status filter
     * @param metodo optional payment type filter
     * @param model thymeleaf model
     * @return admin payments view
     */
    @GetMapping("/pagamentos")
    public String listar(
            @RequestParam(name = "q", required = false) final String q,
            @RequestParam(name = "status", required = false)
            final String status,
            @RequestParam(name = "metodo", required = false)
            final String metodo,
            final Model model
    ) {
        final List<PagamentoItemView> transacoes = pedidoRepository.listarRecentes(
                        PageRequest.of(FIRST_PAGE_INDEX, RECENT_PAGE_SIZE)
                )
                .stream()
                .filter(pedido -> matchesStatus(pedido, status))
                .filter(pedido -> matchesMetodo(pedido, metodo))
                .filter(pedido -> matchesQuery(pedido, q))
                .map(pedido -> PagamentoItemView.from(
                        pedido,
                        resolveMetodoLabel(pedido),
                        resolveRequestedMethodLabel(pedido)
                ))
                .toList();

        model.addAttribute("pageTitle", "Financeiro - Pagamentos");
        model.addAttribute("active", "financeiro");
        model.addAttribute("transacoes", transacoes);
        model.addAttribute("resumo", PagamentoResumoView.from(transacoes));
        model.addAttribute("q", nvl(q));
        model.addAttribute("status", nvl(status));
        model.addAttribute("metodo", nvl(metodo));
        model.addAttribute("statusOptions", StatusPedido.values());
        model.addAttribute("paymentTypeOptions", TipoPagamento.values());
        return "pages/admin/financeiro/pagamentos";
    }

    private static boolean matchesStatus(
            final PedidoEntity pedido,
            final String raw
    ) {
        if (raw == null || raw.isBlank()) {
            return true;
        }
        final StatusPedido status = pedido.getStatus();
        return status != null && status.name().equalsIgnoreCase(raw.trim());
    }

    private static boolean matchesMetodo(
            final PedidoEntity pedido,
            final String raw
    ) {
        if (raw == null || raw.isBlank()) {
            return true;
        }
        final TipoPagamento tipoPagamento = pedido.getTipoPagamento();
        return tipoPagamento != null
                && tipoPagamento.name().equalsIgnoreCase(raw.trim());
    }

    private static boolean matchesQuery(
            final PedidoEntity pedido,
            final String raw
    ) {
        if (raw == null || raw.isBlank()) {
            return true;
        }
        final String query = raw.trim().toLowerCase(LOCALE_PT_BR);
        if (pedido.getId() != null
                && pedido.getId().toString().contains(query)) {
            return true;
        }

        final ClienteEntity cliente = pedido.getCliente();
        if (cliente == null) {
            return false;
        }
        final String nome = cliente.getNome();
        if (nome != null && nome.toLowerCase(LOCALE_PT_BR).contains(query)) {
            return true;
        }
        final String email = cliente.getEmail();
        return email != null
                && email.toLowerCase(LOCALE_PT_BR).contains(query);
    }

    private static String resolveStatusLabel(final StatusPedido status) {
        return PedidoStatusSupport.adminLabel(status, null);
    }

    private String resolveMetodoLabel(final PedidoEntity pedido) {
        if (pedido.getFormaPagamentoRecebida() != null
                && !pedido.getFormaPagamentoRecebida().isBlank()) {
            return formatReceivedPaymentLabel(
                    pedido.getFormaPagamentoRecebida()
            );
        }
        return resolveRequestedMethodLabel(pedido);
    }

    private String resolveRequestedMethodLabel(final PedidoEntity pedido) {
        if (pedido.getMetodoPagamento() != null
                && !pedido.getMetodoPagamento().isBlank()) {
            return paymentMethodService.resolveLabel(
                    pedido.getMetodoPagamento()
            );
        }

        final TipoPagamento tipoPagamento = pedido.getTipoPagamento();
        if (tipoPagamento == null) {
            return DEFAULT_UNKNOWN;
        }

        return switch (tipoPagamento) {
            case PIX -> "PIX";
            case BOLETO -> "Boleto Bancario";
            case CARTAO_CREDITO -> "Cartao de Credito";
            case CARTAO_DEBITO -> "Cartao de Debito";
            case DINHEIRO -> "Dinheiro";
            case CUSTOM -> "Metodo customizado";
        };
    }

    private static String formatReceivedPaymentLabel(final String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return DEFAULT_UNKNOWN;
        }
        return switch (rawValue.trim().toUpperCase(LOCALE_PT_BR)) {
            case "PIX" -> "Pix";
            case "DINHEIRO" -> "Dinheiro";
            case "CARTAO" -> "Cartao";
            default -> "Outro";
        };
    }

    private static String formatCurrency(final BigDecimal value) {
        final BigDecimal safe = value == null ? BigDecimal.ZERO : value;
        return NumberFormat.getCurrencyInstance(LOCALE_PT_BR).format(safe);
    }

    private static String nvl(final String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * View model for one payments page row.
     *
     * @param id row identifier
     * @param pedidoId order id
     * @param pedidoNumero formatted order number
     * @param clienteNome customer name
     * @param metodo payment method label
     * @param valor order total
     * @param status status name
     * @param statusLabel user-friendly status
     * @param badgeClass CSS badge modifier
     * @param data payment date
     */
    public record PagamentoItemView(
            Long id,
            Long pedidoId,
            String pedidoNumero,
            String clienteNome,
            String metodo,
            String metodoSolicitado,
            boolean pagamentoDivergente,
            Integer avaliacaoCliente,
            BigDecimal valor,
            String status,
            String statusLabel,
            String badgeClass,
            LocalDateTime data
    ) {

        /**
         * Maps one order to the payments page view.
         *
         * @param pedido order entity
         * @param metodoLabel resolved payment method label
         * @return payments page row
         */
        static PagamentoItemView from(
                final PedidoEntity pedido,
                final String metodoLabel,
                final String metodoSolicitadoLabel
        ) {
            final String numero = pedido.getId() != null
                    ? String.format(
                            "%0" + ORDER_NUMBER_DIGITS + "d",
                            pedido.getId()
                    )
                    : DEFAULT_UNKNOWN;
            final String clienteNome = pedido.getCliente() != null
                    && pedido.getCliente().getNome() != null
                    ? pedido.getCliente().getNome()
                    : "Cliente";
            final StatusPedido status = pedido.getStatus();
            return new PagamentoItemView(
                    pedido.getId(),
                    pedido.getId(),
                    numero,
                    clienteNome,
                    metodoLabel,
                    metodoSolicitadoLabel,
                    pedido.isPagamentoDivergente(),
                    pedido.getAvaliacaoCliente(),
                    pedido.getTotal(),
                    status == null ? DEFAULT_UNKNOWN : status.name(),
                    resolveStatusLabel(status),
                    resolveBadgeClass(status),
                    pedido.getData()
            );
        }

        private static String resolveBadgeClass(final StatusPedido status) {
            if (status == null) {
                return "";
            }
            return switch (status) {
                case PAGO, ENTREGUE -> "badge--success";
                case CANCELADO -> "badge--danger";
                case AGUARDANDO_PAGAMENTO, ABERTO, PRONTO_PARA_RETIRADA,
                        PRONTO_PARA_ENTREGA, SAIU_PARA_ENTREGA, ENVIADO -> "badge--warning";
            };
        }
    }

    /**
     * Summary view model for the payments page.
     *
     * @param recebido value received in filtered results
     * @param pendentes count of pending payment orders
     * @param cancelados count of cancelled orders
     */
    public record PagamentoResumoView(
            String recebido,
            long pendentes,
            long cancelados
    ) {

        /**
         * Builds the summary from filtered payment rows.
         *
         * @param itens filtered rows
         * @return summary view model
         */
        static PagamentoResumoView from(final List<PagamentoItemView> itens) {
            BigDecimal recebido = BigDecimal.ZERO;
            long pendentes = 0L;
            long cancelados = 0L;

            for (PagamentoItemView item : itens) {
                if ("PAGO".equals(item.status())
                        || "ENTREGUE".equals(item.status())) {
                    recebido = recebido.add(
                            item.valor() == null ? BigDecimal.ZERO : item.valor()
                    );
                }
                if ("AGUARDANDO_PAGAMENTO".equals(item.status())) {
                    pendentes++;
                }
                if ("CANCELADO".equals(item.status())) {
                    cancelados++;
                }
            }

            return new PagamentoResumoView(
                    formatCurrency(recebido),
                    pendentes,
                    cancelados
            );
        }
    }
}
