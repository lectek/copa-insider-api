package br.com.lectek.copainsider.application.service.fiscal;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ItemPedidoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.PedidoFiscalSnapshotEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.PedidoFiscalSnapshotRepository;
import br.com.lectek.copainsider.domain.enums.ModoEntrega;
import br.com.lectek.copainsider.domain.fiscal.FiscalDocumentModel;
import br.com.lectek.copainsider.domain.fiscal.FiscalPrintChannel;
import br.com.lectek.copainsider.domain.fiscal.FiscalProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoFiscalSnapshotService {

    private final PedidoFiscalSnapshotRepository repository;
    private final FiscalEmitterConfigService fiscalEmitterConfigService;
    private final ObjectMapper objectMapper;

    public PedidoFiscalSnapshotService(
            final PedidoFiscalSnapshotRepository pedidoFiscalSnapshotRepository,
            final FiscalEmitterConfigService fiscalEmitterConfigServiceValue,
            final ObjectMapper objectMapperValue
    ) {
        this.repository = pedidoFiscalSnapshotRepository;
        this.fiscalEmitterConfigService = fiscalEmitterConfigServiceValue;
        this.objectMapper = objectMapperValue;
    }

    @Transactional
    public PedidoFiscalSnapshotEntity capture(
            final PedidoEntity pedido,
            final SnapshotRequest request
    ) {
        if (pedido == null || pedido.getId() == null) {
            throw new IllegalArgumentException(
                    "Pedido salvo e obrigatorio para gerar snapshot fiscal."
            );
        }
        return repository.findByPedidoId(pedido.getId()).orElseGet(() -> {
            final PedidoFiscalSnapshotEntity entity =
                    new PedidoFiscalSnapshotEntity();
            entity.setPedido(pedido);
            entity.setSource(blankToDefault(request.source(), "DESCONHECIDO"));
            entity.setSuggestedDocumentModel(resolveSuggestedModel(
                    request.recipientDocument()
            ));
            entity.setRecipientName(blankToNull(request.recipientName()));
            entity.setRecipientDocument(blankToNull(request.recipientDocument()));
            entity.setRecipientEmail(blankToNull(request.recipientEmail()));
            entity.setRecipientPhone(blankToNull(request.recipientPhone()));
            entity.setRecipientAddress(blankToNull(request.recipientAddress()));
            entity.setIssuerCnpj(resolveIssuerCnpj());
            entity.setPaymentMethod(resolvePaymentMethod(pedido));
            entity.setShippingAmount(normalizeMoney(request.shippingAmount()));
            entity.setTotalAmount(normalizeMoney(pedido.getTotal()));
            entity.setPrintChannel(resolveDefaultPrintChannel(pedido));
            entity.setEmailDeliveryRequested(false);
            entity.setEmailDeliveryAddress(blankToNull(request.recipientEmail()));
            entity.setPayloadJson(writePayload(pedido, request));
            return repository.save(entity);
        });
    }

    @Transactional(readOnly = true)
    public Optional<PedidoFiscalSnapshotEntity> findByPedidoId(final Long pedidoId) {
        if (pedidoId == null) {
            return Optional.empty();
        }
        return repository.findByPedidoId(pedidoId);
    }

    @Transactional
    public PedidoFiscalSnapshotEntity captureIfMissing(
            final PedidoEntity pedido,
            final String source
    ) {
        if (pedido == null || pedido.getId() == null) {
            throw new IllegalArgumentException(
                    "Pedido salvo e obrigatorio para gerar snapshot fiscal."
            );
        }
        return repository.findByPedidoId(pedido.getId()).orElseGet(() -> capture(
                pedido,
                fallbackRequest(pedido, source)
        ));
    }

    @Transactional
    public PedidoFiscalSnapshotEntity updateDeliveryPreferences(
            final Long pedidoId,
            final UpdateDeliveryPreferencesRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Preferencias fiscais obrigatorias."
            );
        }
        final PedidoFiscalSnapshotEntity entity = repository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Snapshot fiscal nao encontrado para o pedido."
                ));
        entity.setEmailDeliveryRequested(request.emailRequested());
        if (request.emailRequested()) {
            final String email = blankToNull(firstNonBlank(
                    request.emailAddress(),
                    entity.getEmailDeliveryAddress(),
                    entity.getRecipientEmail()
            ));
            if (email == null) {
                throw new IllegalArgumentException(
                        "Informe um e-mail para receber a nota fiscal."
                );
            }
            entity.setEmailDeliveryAddress(email);
        } else if (request.emailAddress() != null) {
            entity.setEmailDeliveryAddress(blankToNull(request.emailAddress()));
        }
        if (request.printChannel() != null) {
            entity.setPrintChannel(request.printChannel());
        }
        return repository.save(entity);
    }

    @Transactional
    public PedidoFiscalSnapshotEntity updatePaymentMethod(
            final Long pedidoId,
            final String paymentMethod
    ) {
        final PedidoFiscalSnapshotEntity entity = repository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Snapshot fiscal nao encontrado para o pedido."
                ));
        entity.setPaymentMethod(blankToNull(paymentMethod));
        return repository.save(entity);
    }

    private String resolveIssuerCnpj() {
        return fiscalEmitterConfigService.load(FiscalProvider.FOCUS_NFE).cnpj();
    }

    private String resolvePaymentMethod(final PedidoEntity pedido) {
        if (pedido.getFormaPagamentoRecebida() != null
                && !pedido.getFormaPagamentoRecebida().isBlank()) {
            return pedido.getFormaPagamentoRecebida().trim();
        }
        if (pedido.getMetodoPagamento() != null
                && !pedido.getMetodoPagamento().isBlank()) {
            return pedido.getMetodoPagamento().trim();
        }
        return pedido.getTipoPagamento() == null
                ? null
                : pedido.getTipoPagamento().name();
    }

    private FiscalDocumentModel resolveSuggestedModel(
            final String recipientDocument
    ) {
        final String digits = digits(recipientDocument);
        if (digits != null && digits.length() == 14) {
            return FiscalDocumentModel.NFE_55;
        }
        return FiscalDocumentModel.NFCE_65;
    }

    private String writePayload(
            final PedidoEntity pedido,
            final SnapshotRequest request
    ) {
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("pedidoId", pedido.getId());
        payload.put("source", blankToDefault(request.source(), "DESCONHECIDO"));
        payload.put("statusPedido",
                pedido.getStatus() == null ? null : pedido.getStatus().name());
        payload.put("tipoPagamento",
                pedido.getTipoPagamento() == null
                        ? null
                        : pedido.getTipoPagamento().name());
        payload.put("modoEntrega",
                pedido.getModoEntrega() == null
                        ? null
                        : pedido.getModoEntrega().name());
        final Map<String, Object> recipient = new LinkedHashMap<>();
        recipient.put("name", blankToNull(request.recipientName()));
        recipient.put("document", digits(request.recipientDocument()));
        recipient.put("email", blankToNull(request.recipientEmail()));
        recipient.put("phone", blankToNull(request.recipientPhone()));
        recipient.put("address", blankToNull(request.recipientAddress()));
        payload.put("recipient", recipient);
        final Map<String, Object> totals = new LinkedHashMap<>();
        totals.put("shipping", normalizeMoney(request.shippingAmount()));
        totals.put("order", normalizeMoney(pedido.getTotal()));
        payload.put("totals", totals);
        payload.put("items", buildItemsPayload(pedido.getItens()));

        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(
                    "Falha ao serializar snapshot fiscal do pedido.",
                    ex
            );
        }
    }

    private List<Map<String, Object>> buildItemsPayload(
            final List<ItemPedidoEntity> items
    ) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        return items.stream().map(this::toItemPayload).toList();
    }

    private Map<String, Object> toItemPayload(final ItemPedidoEntity item) {
        final ProdutoEntity produto = item.getProduto();
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("produtoId", produto == null ? null : produto.getId());
        payload.put("nome", produto == null ? null : produto.getNome());
        payload.put("codigoBarras", produto == null ? null : produto.getCodigoBarras());
        payload.put("unidade", produto == null ? null : blankToNull(produto.getUnidade()));
        payload.put("quantidade", item.getQuantidade());
        payload.put("subtotal", normalizeMoney(item.getSubtotal()));
        payload.put("valorUnitario", resolveUnitPrice(item));
        final Map<String, Object> fiscal = new LinkedHashMap<>();
        if (produto != null) {
            fiscal.put("ncm", produto.getFiscalNcm());
            fiscal.put("cest", produto.getFiscalCest());
            fiscal.put("cfop", produto.getFiscalCfop());
            fiscal.put("origem", produto.getFiscalOrigem());
            fiscal.put("icmsCst", produto.getFiscalIcmsCst());
            fiscal.put("csosn", produto.getFiscalCsosn());
            fiscal.put("pisCst", produto.getFiscalPisCst());
            fiscal.put("cofinsCst", produto.getFiscalCofinsCst());
        }
        payload.put("fiscal", fiscal);
        return payload;
    }

    private SnapshotRequest fallbackRequest(
            final PedidoEntity pedido,
            final String source
    ) {
        final ClienteEntity cliente = pedido.getCliente();
        return new SnapshotRequest(
                blankToDefault(source, "PEDIDO"),
                cliente == null ? null : cliente.getNome(),
                cliente == null ? null : cliente.getCpf(),
                cliente == null ? null : cliente.getEmail(),
                cliente == null ? null : cliente.getTelefone(),
                pedido.getEnderecoEntrega(),
                BigDecimal.ZERO
        );
    }

    private FiscalPrintChannel resolveDefaultPrintChannel(
            final PedidoEntity pedido
    ) {
        if (pedido == null || pedido.getModoEntrega() == null) {
            return FiscalPrintChannel.NONE;
        }
        if (pedido.getModoEntrega() == ModoEntrega.RETIRADA) {
            return FiscalPrintChannel.IMMEDIATE;
        }
        return FiscalPrintChannel.WITH_DELIVERY;
    }

    private BigDecimal resolveUnitPrice(final ItemPedidoEntity item) {
        if (item != null && item.getPrecoUnitario() != null) {
            return normalizeMoney(item.getPrecoUnitario());
        }

        if (item == null || item.getSubtotal() == null
                || item.getQuantidade() == null
                || item.getQuantidade() <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return item.getSubtotal()
                .divide(BigDecimal.valueOf(item.getQuantidade()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeMoney(final BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String blankToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private String blankToDefault(final String value, final String defaultValue) {
        final String normalized = blankToNull(value);
        return normalized == null ? defaultValue : normalized;
    }

    private String firstNonBlank(final String... values) {
        for (String value : values) {
            final String normalized = blankToNull(value);
            if (normalized != null) {
                return normalized;
            }
        }
        return null;
    }

    private String digits(final String value) {
        if (value == null) {
            return null;
        }
        final String normalized = value.replaceAll("\\D", "");
        return normalized.isBlank() ? null : normalized;
    }

    public record SnapshotRequest(
            String source,
            String recipientName,
            String recipientDocument,
            String recipientEmail,
            String recipientPhone,
            String recipientAddress,
            BigDecimal shippingAmount
    ) {
    }

    public record UpdateDeliveryPreferencesRequest(
            FiscalPrintChannel printChannel,
            boolean emailRequested,
            String emailAddress
    ) {
    }
}
