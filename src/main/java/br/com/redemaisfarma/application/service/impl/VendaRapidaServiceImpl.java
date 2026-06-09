/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  br.com.redemaisfarma.domain.enums.StatusPedido
 *  br.com.redemaisfarma.domain.enums.TipoPagamento
 *  br.com.redemaisfarma.domain.service.EstoqueService
 *  jakarta.persistence.EntityNotFoundException
 *  lombok.Generated
 *  org.springframework.context.annotation.Profile
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 */
package br.com.redemaisfarma.application.service.impl;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ItemPedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.application.core.tenant.TenantResolverService;
import br.com.redemaisfarma.application.service.VendaRapidaService;
import br.com.redemaisfarma.application.service.fiscal.PedidoFiscalSnapshotService;
import br.com.redemaisfarma.domain.enums.ModoEntrega;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import br.com.redemaisfarma.domain.enums.TipoPagamento;
import br.com.redemaisfarma.domain.service.EstoqueService;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Profile(value={"!test"})
@Service
public class VendaRapidaServiceImpl
implements VendaRapidaService {
    private final ClienteRepository clienteRepo;
    private final ProdutoRepository produtoRepo;
    private final PedidoRepository pedidoRepo;
    private final EstoqueService estoque;
    private final PedidoFiscalSnapshotService pedidoFiscalSnapshotService;
    @Autowired(required = false)
    private TenantResolverService tenantResolverService;

    @Override
    @Transactional
    public Long criar(String refCliente, String refProduto, int qtd) {
        if (qtd <= 0) {
            throw new IllegalArgumentException("Quantidade inv\u00e1lida.");
        }
        ClienteEntity cliente = this.buscarCliente(refCliente);
        ProdutoEntity produto = this.buscarProduto(refProduto);
        int est = produto.getEstoque();
        if (est < qtd || !this.estoque.temDisponivel(produto.getId(), qtd)) {
            throw new IllegalArgumentException("Estoque insuficiente para " + produto.getNome());
        }
        PedidoEntity pedido = new PedidoEntity();
        pedido.setCliente(cliente);
        pedido.setData(LocalDateTime.now());
        pedido.setStatus(StatusPedido.ABERTO);
        pedido.setTipoPagamento(TipoPagamento.PIX);
        pedido.setModoEntrega(ModoEntrega.RETIRADA);
        pedido.setTotal(BigDecimal.ZERO);
        ItemPedidoEntity item = new ItemPedidoEntity();
        item.setPedido(pedido);
        item.setProduto(produto);
        item.setQuantidade(qtd);
        BigDecimal preco = produto.getPrecoVenda() != null ? produto.getPrecoVenda() : BigDecimal.ZERO;
        item.setPrecoUnitario(preco);
        item.setSubtotal(preco.multiply(BigDecimal.valueOf(qtd)));
        pedido.addItem(item);
        BigDecimal total = pedido.getItens().stream().map(ItemPedidoEntity::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        pedido.setTotal(total);
        pedido = this.pedidoRepo.save(pedido);
        this.pedidoFiscalSnapshotService.capture(
                pedido,
                new PedidoFiscalSnapshotService.SnapshotRequest(
                        "VENDA_RAPIDA",
                        cliente.getNome(),
                        cliente.getCpf(),
                        cliente.getEmail(),
                        cliente.getTelefone(),
                        null,
                        BigDecimal.ZERO
                )
        );
        this.estoque.baixar(produto.getId(), qtd, "Venda r\u00e1pida pedido #" + String.valueOf(pedido.getId()));
        return pedido.getId();
    }

    private ClienteEntity buscarCliente(String ref) {
        if (ref == null || ref.isBlank()) {
            throw new IllegalArgumentException("Informe o cliente.");
        }
        return this.clienteRepo.findByEmailIgnoreCase(ref).or(() -> this.clienteRepo.findByCpf(ref)).or(() -> this.clienteRepo.findFirstByNomeContainingIgnoreCase(ref)).or(() -> this.clienteRepo.findByEmail(ref)).orElseThrow(() -> new EntityNotFoundException("Cliente n\u00e3o encontrado."));
    }

    private ProdutoEntity buscarProduto(String ref) {
        if (ref == null || ref.isBlank()) {
            throw new IllegalArgumentException("Informe o produto.");
        }
        Long tenantId = this.resolveTenantId();
        return (tenantId == null
                ? this.produtoRepo.findByAnyCodigo(ref)
                : this.produtoRepo.findByAnyCodigo(tenantId, ref))
                .or(() -> tenantId == null
                        ? this.produtoRepo.findFirstByNomeContainingIgnoreCase(ref)
                        : this.produtoRepo.findFirstByTenantIdAndNomeContainingIgnoreCase(tenantId, ref))
                .orElseThrow(() -> new EntityNotFoundException("Produto n\u00e3o encontrado."));
    }

    @Generated
    public VendaRapidaServiceImpl(ClienteRepository clienteRepo, ProdutoRepository produtoRepo, PedidoRepository pedidoRepo, EstoqueService estoque, PedidoFiscalSnapshotService pedidoFiscalSnapshotService) {
        this.clienteRepo = clienteRepo;
        this.produtoRepo = produtoRepo;
        this.pedidoRepo = pedidoRepo;
        this.estoque = estoque;
        this.pedidoFiscalSnapshotService = pedidoFiscalSnapshotService;
    }

    private Long resolveTenantId() {
        if (this.tenantResolverService == null) {
            return null;
        }
        return this.tenantResolverService.resolveDefaultTenantId();
    }
}
