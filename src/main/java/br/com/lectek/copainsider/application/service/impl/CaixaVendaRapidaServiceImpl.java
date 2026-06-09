package br.com.lectek.copainsider.application.service.impl;

import br.com.lectek.copainsider.adapters.inbound.web.dto.ProdutoBuscaDTO;
import br.com.lectek.copainsider.adapters.inbound.web.dto.VendaRapidaFinalizarRequestDTO;
import br.com.lectek.copainsider.adapters.inbound.web.dto.VendaRapidaFinalizarResponseDTO;
import br.com.lectek.copainsider.adapters.inbound.web.dto.VendaRapidaItemRequestDTO;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ItemPedidoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.lectek.copainsider.application.core.tenant.TenantResolverService;
import br.com.lectek.copainsider.application.service.CaixaVendaRapidaService;
import br.com.lectek.copainsider.application.service.MailService;
import br.com.lectek.copainsider.application.service.PaymentTerminalService;
import br.com.lectek.copainsider.application.service.PaymentTerminalService.TerminalPaymentRequest;
import br.com.lectek.copainsider.application.service.PaymentTerminalService.TerminalPaymentResult;
import br.com.lectek.copainsider.application.service.fiscal.FiscalOrderEmissionService;
import br.com.lectek.copainsider.application.service.fiscal.PedidoFiscalSnapshotService;
import br.com.lectek.copainsider.domain.fiscal.FiscalPrintChannel;
import br.com.lectek.copainsider.domain.enums.ModoEntrega;
import br.com.lectek.copainsider.domain.enums.StatusPedido;
import br.com.lectek.copainsider.domain.enums.TipoPagamento;
import br.com.lectek.copainsider.domain.service.EstoqueService;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Profile("!test")
@Service
public class CaixaVendaRapidaServiceImpl implements CaixaVendaRapidaService {

    private static final String DEFAULT_CLIENT_NAME = "Consumidor Final";
    private static final String DEFAULT_CLIENT_CPF = "00000000000";
    private static final String DEFAULT_CLIENT_EMAIL = "consumidor.final@local.invalid";

    private final ClienteRepository clienteRepo;
    private final ProdutoRepository produtoRepo;
    private final PedidoRepository pedidoRepo;
    private final EstoqueService estoque;
    private final MailService mailService;
    private final PaymentTerminalService paymentTerminalService;
    private final PedidoFiscalSnapshotService pedidoFiscalSnapshotService;
    private final FiscalOrderEmissionService fiscalOrderEmissionService;
    @Autowired(required = false)
    private TenantResolverService tenantResolverService;

    public CaixaVendaRapidaServiceImpl(
            ClienteRepository clienteRepo,
            ProdutoRepository produtoRepo,
            PedidoRepository pedidoRepo,
            EstoqueService estoque,
            MailService mailService,
            PaymentTerminalService paymentTerminalService,
            PedidoFiscalSnapshotService pedidoFiscalSnapshotService,
            FiscalOrderEmissionService fiscalOrderEmissionService
    ) {
        this.clienteRepo = clienteRepo;
        this.produtoRepo = produtoRepo;
        this.pedidoRepo = pedidoRepo;
        this.estoque = estoque;
        this.mailService = mailService;
        this.paymentTerminalService = paymentTerminalService;
        this.pedidoFiscalSnapshotService = pedidoFiscalSnapshotService;
        this.fiscalOrderEmissionService = fiscalOrderEmissionService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProdutoBuscaDTO> buscarProdutos(String termo, int limit) {
        String q = termo == null ? "" : termo.trim();
        if (q.isEmpty()) {
            return List.of();
        }
        Long tenantId = resolveTenantId();
        Optional<ProdutoEntity> byBarcode = tenantId == null
                ? produtoRepo.findByAnyCodigo(q)
                : produtoRepo.findByAnyCodigo(tenantId, q);
        if (byBarcode.isPresent()) {
            return List.of(toBusca(byBarcode.get()));
        }
        Pageable pageable = PageRequest.of(0, Math.max(1, Math.min(limit, 20)));
        return produtoRepo.searchPage(tenantId, q, pageable)
                .getContent()
                .stream()
                .map(CaixaVendaRapidaServiceImpl::toBusca)
                .toList();
    }

    @Override
    @Transactional
    public VendaRapidaFinalizarResponseDTO finalizar(VendaRapidaFinalizarRequestDTO request) {
        if (request == null || request.itens() == null || request.itens().isEmpty()) {
            return new VendaRapidaFinalizarResponseDTO(false, "Nenhum item informado.", null, null, false);
        }
        NotaOpcao nota = NotaOpcao.from(request.notaOpcao());
        if (nota == NotaOpcao.EMAIL) {
            String email = normalizeEmail(request.clienteEmail());
            if (email == null || email.isBlank()) {
                return new VendaRapidaFinalizarResponseDTO(false, "E-mail obrigatÃ³rio para envio.", null, null, false);
            }
        }
        List<ItemPedidoEntity> itens = montarItens(request.itens());

        if (nota == NotaOpcao.PULAR) {
            baixarEstoque(itens, "Venda rÃ¡pida sem nota");
            return new VendaRapidaFinalizarResponseDTO(true, "Estoque atualizado.", null, null, false);
        }

        ClienteEntity cliente = resolverCliente(request);
        PedidoEntity pedido = new PedidoEntity();
        pedido.setCliente(cliente);
        pedido.setData(LocalDateTime.now());
        pedido.setStatus(StatusPedido.PAGO);
        TipoPagamento tipoPagamento = mapTipoPagamento(request.pagamentoTipo());
        pedido.setModoEntrega(ModoEntrega.RETIRADA);
        pedido.setTotal(BigDecimal.ZERO);
        itens.forEach(pedido::addItem);
        BigDecimal total = itens.stream()
                .map(ItemPedidoEntity::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        TerminalPaymentResult terminalResult = authorizeTerminalPayment(
                request,
                tipoPagamento,
                total
        );
        if (!terminalResult.approved()) {
            return new VendaRapidaFinalizarResponseDTO(
                    false,
                    terminalResult.message(),
                    null,
                    null,
                    false
            );
        }
        pedido.setTipoPagamento(tipoPagamento);
        pedido.setMetodoPagamento(buildMetodoPagamento(
                request.pagamentoTipo(),
                request.trocoPara(),
                terminalResult.transactionId()
        ));
        pedido.setTotal(total);

        pedido = pedidoRepo.save(pedido);
        baixarEstoque(itens, "Venda rÃ¡pida pedido #" + pedido.getId());

        pedidoFiscalSnapshotService.capture(
                pedido,
                new PedidoFiscalSnapshotService.SnapshotRequest(
                        "CAIXA_VENDA_RAPIDA",
                        cliente.getNome(),
                        cliente.getCpf(),
                        cliente.getEmail(),
                        cliente.getTelefone(),
                        null,
                        BigDecimal.ZERO
                )
        );
        if (nota == NotaOpcao.EMAIL) {
            String destinoNota = normalizeEmail(request.clienteEmail());
            if (destinoNota == null || destinoNota.isBlank()) {
                destinoNota = normalizeEmail(cliente.getEmail());
            }
            pedidoFiscalSnapshotService.updateDeliveryPreferences(
                    pedido.getId(),
                    new PedidoFiscalSnapshotService.UpdateDeliveryPreferencesRequest(
                            FiscalPrintChannel.IMMEDIATE,
                            true,
                            destinoNota
                    )
            );
        }
        fiscalOrderEmissionService.processPaidOrder(
                pedido.getId(),
                "CAIXA_VENDA_RAPIDA"
        );

        boolean emailEnviado = false;
        if (nota == NotaOpcao.EMAIL) {
            String destino = normalizeEmail(request.clienteEmail());
            if (destino == null || destino.isBlank()) {
                destino = normalizeEmail(cliente.getEmail());
            }
            if (destino != null && !destino.isBlank()) {
                enviarEmailRecibo(destino, pedido);
                emailEnviado = true;
            }
        }

        String reciboUrl = "/admin/vendas/rapida/recibo/" + pedido.getId();
        return new VendaRapidaFinalizarResponseDTO(true, "Venda registrada.", pedido.getId(), reciboUrl, emailEnviado);
    }

    private List<ItemPedidoEntity> montarItens(List<VendaRapidaItemRequestDTO> itens) {
        List<Long> ids = itens.stream().map(VendaRapidaItemRequestDTO::produtoId).toList();
        Long tenantId = resolveTenantId();
        List<ProdutoEntity> produtos = tenantId == null
                ? produtoRepo.findAllByIdIn(ids)
                : produtoRepo.findAllByTenantIdIn(tenantId, ids);
        if (produtos.isEmpty()) {
            throw new EntityNotFoundException("Produtos nÃ£o encontrados.");
        }
        return itens.stream().map(item -> {
            ProdutoEntity produto = produtos.stream()
                    .filter(p -> p.getId().equals(item.produtoId()))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("Produto nÃ£o encontrado."));
            int qtd = item.quantidade() == null ? 0 : item.quantidade();
            if (qtd <= 0) {
                throw new IllegalArgumentException("Quantidade invÃ¡lida.");
            }
            if (produto.getEstoque() < qtd || !estoque.temDisponivel(produto.getId(), qtd)) {
                throw new IllegalArgumentException("Estoque insuficiente para " + produto.getNome());
            }
            BigDecimal preco = produto.getPrecoVenda() != null ? produto.getPrecoVenda() : BigDecimal.ZERO;
            ItemPedidoEntity entity = new ItemPedidoEntity();
            entity.setProduto(produto);
            entity.setQuantidade(qtd);
            entity.setPrecoUnitario(preco);
            entity.setSubtotal(preco.multiply(BigDecimal.valueOf(qtd)).setScale(2, RoundingMode.HALF_UP));
            return entity;
        }).collect(Collectors.toList());
    }

    private void baixarEstoque(List<ItemPedidoEntity> itens, String motivo) {
        for (ItemPedidoEntity item : itens) {
            estoque.baixar(item.getProduto().getId(), item.getQuantidade(), motivo);
        }
    }

    private ClienteEntity resolverCliente(VendaRapidaFinalizarRequestDTO request) {
        String cpf = sanitizeCpf(request.clienteCpf());
        String email = normalizeEmail(request.clienteEmail());
        String nome = safe(request.clienteNome(), DEFAULT_CLIENT_NAME);
        String telefone = safe(request.clienteTelefone(), "");
        boolean criarCliente = Boolean.TRUE.equals(request.criarCliente());

        if (cpf != null) {
            Optional<ClienteEntity> found = clienteRepo.findByCpf(cpf);
            if (found.isPresent()) {
                return found.get();
            }
            if (criarCliente) {
                return criarCliente(nome, email, telefone, cpf);
            }
            return clienteRepo.findByCpf(DEFAULT_CLIENT_CPF)
                    .orElseGet(this::criarClienteDefault);
        }

        if (email != null) {
            Optional<ClienteEntity> found = clienteRepo.findByEmailIgnoreCase(email);
            if (found.isPresent()) {
                return found.get();
            }
        }

        return clienteRepo.findByCpf(DEFAULT_CLIENT_CPF)
                .orElseGet(this::criarClienteDefault);
    }

    private ClienteEntity criarCliente(String nome, String email, String telefone, String cpf) {
        ClienteEntity e = new ClienteEntity();
        e.setNome(nome);
        e.setEmail(email != null && !email.isBlank() ? email : buildFallbackEmail(cpf));
        e.setTelefone(telefone != null && !telefone.isBlank() ? telefone : null);
        e.setCpf(cpf);
        e.setSenha("caixa-" + UUID.randomUUID());
        e.setAtivo(true);
        return clienteRepo.save(e);
    }

    private ClienteEntity criarClienteDefault() {
        ClienteEntity e = new ClienteEntity();
        e.setNome(DEFAULT_CLIENT_NAME);
        e.setEmail(DEFAULT_CLIENT_EMAIL);
        e.setTelefone(null);
        e.setCpf(DEFAULT_CLIENT_CPF);
        e.setSenha("caixa-" + UUID.randomUUID());
        e.setAtivo(true);
        return clienteRepo.save(e);
    }

    private static String buildFallbackEmail(String cpf) {
        String clean = cpf == null ? "00000000000" : cpf;
        return clean + "@local.invalid";
    }

    private static String normalizeEmail(String email) {
        if (email == null) return null;
        String v = email.trim().toLowerCase(Locale.ROOT);
        return v.isBlank() ? null : v;
    }

    private static String sanitizeCpf(String cpf) {
        if (cpf == null) return null;
        String clean = cpf.replaceAll("\\D", "");
        return clean.isBlank() ? null : clean;
    }

    private static String safe(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private static TipoPagamento mapTipoPagamento(String raw) {
        if (raw == null) return TipoPagamento.DINHEIRO;
        return switch (raw.trim().toUpperCase(Locale.ROOT)) {
            case "PIX" -> TipoPagamento.PIX;
            case "CARTAO_CREDITO" -> TipoPagamento.CARTAO_CREDITO;
            case "CARTAO_DEBITO" -> TipoPagamento.CARTAO_DEBITO;
            case "DINHEIRO" -> TipoPagamento.DINHEIRO;
            default -> TipoPagamento.DINHEIRO;
        };
    }

    private TerminalPaymentResult authorizeTerminalPayment(
            final VendaRapidaFinalizarRequestDTO request,
            final TipoPagamento tipoPagamento,
            final BigDecimal total
    ) {
        if (tipoPagamento != TipoPagamento.CARTAO_CREDITO
                && tipoPagamento != TipoPagamento.CARTAO_DEBITO) {
            return new TerminalPaymentResult(true, "not_required", null, "");
        }
        final String paymentType = tipoPagamento == TipoPagamento.CARTAO_DEBITO
                ? "CARTAO_DEBITO"
                : "CARTAO_CREDITO";
        return paymentTerminalService.authorize(new TerminalPaymentRequest(
                total,
                paymentType,
                buildTerminalReference(request),
                "PDV_VENDA_RAPIDA",
                Map.of("notaOpcao", safe(request.notaOpcao(), "IMPRESSAO"))
        ));
    }

    private static String buildTerminalReference(
            final VendaRapidaFinalizarRequestDTO request
    ) {
        final String cpf = sanitizeCpf(request.clienteCpf());
        if (cpf != null && !cpf.isBlank()) {
            return "PDV-" + cpf;
        }
        return "PDV-" + UUID.randomUUID().toString()
                .substring(0, 8)
                .toUpperCase(Locale.ROOT);
    }

    private static String buildMetodoPagamento(
            String raw,
            BigDecimal trocoPara,
            String transactionId
    ) {
        String base = raw == null ? "DINHEIRO" : raw.trim().toUpperCase(Locale.ROOT);
        String value = base;
        if ("DINHEIRO".equals(base) && trocoPara != null) {
            value = "DINHEIRO (troco para " + trocoPara + ")";
        }
        if (transactionId != null && !transactionId.isBlank()) {
            value = value + " [tx:" + transactionId.trim() + "]";
        }
        return value.length() > 80 ? value.substring(0, 80) : value;
    }

    private void enviarEmailRecibo(String destino, PedidoEntity pedido) {
        String subject = "Recibo de venda #" + pedido.getId();
        String body = buildEmailBody(pedido);
        mailService.sendText(destino, subject, body, null);
    }

    private static String buildEmailBody(PedidoEntity pedido) {
        StringBuilder sb = new StringBuilder();
        sb.append("Recibo de venda #").append(pedido.getId()).append('\n');
        sb.append("Data: ").append(pedido.getData()).append('\n');
        sb.append("Pagamento: ").append(pedido.getTipoPagamento()).append('\n');
        sb.append('\n').append("Itens:").append('\n');
        for (ItemPedidoEntity item : pedido.getItens()) {
            ProdutoEntity p = item.getProduto();
            String nome = p != null ? p.getNome() : "Produto";
            sb.append("- ").append(nome)
                    .append(" x").append(item.getQuantidade())
                    .append(" = ").append(item.getSubtotal()).append('\n');
        }
        sb.append('\n').append("Total: ").append(pedido.getTotal());
        return sb.toString();
    }

    private static ProdutoBuscaDTO toBusca(ProdutoEntity p) {
        return new ProdutoBuscaDTO(
                p.getId(),
                p.getNome(),
                p.getCodigoBarras(),
                p.getPrecoVenda(),
                p.getEstoque(),
                p.getImagem()
        );
    }

    private Long resolveTenantId() {
        if (this.tenantResolverService == null) {
            return null;
        }
        return this.tenantResolverService.resolveDefaultTenantId();
    }

    private enum NotaOpcao {
        EMAIL,
        IMPRESSAO,
        PULAR;

        static NotaOpcao from(String raw) {
            if (raw == null) return IMPRESSAO;
            return switch (raw.trim().toUpperCase(Locale.ROOT)) {
                case "EMAIL" -> EMAIL;
                case "PULAR" -> PULAR;
                default -> IMPRESSAO;
            };
        }
    }
}
