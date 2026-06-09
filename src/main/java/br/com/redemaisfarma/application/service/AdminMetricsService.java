/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  br.com.redemaisfarma.application.dto.response.PainelAdminResponseDTO
 *  br.com.redemaisfarma.domain.enums.StatusPedido
 *  org.springframework.beans.factory.ObjectProvider
 *  org.springframework.context.annotation.Profile
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 */
package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.application.core.tenant.TenantResolverService;
import br.com.redemaisfarma.application.dto.response.AlertItemDTO;
import br.com.redemaisfarma.application.dto.response.PainelAdminResponseDTO;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile(value={"!test"})
public class AdminMetricsService {
    private final ObjectProvider<ProdutoRepository> produtoRepo;
    private final ObjectProvider<PedidoRepository> pedidoRepo;
    private final ObjectProvider<ClienteRepository> clienteRepo;
    @Autowired(required = false)
    private TenantResolverService tenantResolverService;

    public AdminMetricsService(ObjectProvider<ProdutoRepository> produtoRepo, ObjectProvider<PedidoRepository> pedidoRepo, ObjectProvider<ClienteRepository> clienteRepo) {
        this.produtoRepo = produtoRepo;
        this.pedidoRepo = pedidoRepo;
        this.clienteRepo = clienteRepo;
    }

    @Transactional(readOnly=true)
    public PainelAdminResponseDTO montarPainel() {
        PainelAdminResponseDTO p = new PainelAdminResponseDTO();
        List<AlertItemDTO> alertas = new ArrayList<>();
        Long tenantId = this.resolveTenantId();
        p.setAdminNome("Alex Morais");
        p.setQtdProdutos(this.produtoRepo.getIfAvailable() != null
                ? ((ProdutoRepository)this.produtoRepo.getIfAvailable()).countPubliclySellable(tenantId)
                : 250L);
        p.setTotalPedidos(this.pedidoRepo.getIfAvailable() != null ? ((PedidoRepository)this.pedidoRepo.getIfAvailable()).count() : 350L);
        p.setClientesAtivos(this.clienteRepo.getIfAvailable() != null ? ((ClienteRepository)this.clienteRepo.getIfAvailable()).count() : 120L);
        if (this.pedidoRepo.getIfAvailable() != null) {
            PedidoRepository pr = (PedidoRepository)this.pedidoRepo.getIfAvailable();
            p.setQtdPedidosPendentes(pr.countByStatus(StatusPedido.AGUARDANDO_PAGAMENTO));
            p.setQtdPedidosEntregues(pr.countByStatus(StatusPedido.ENTREGUE));
            EnumMap<StatusPedido, Long> porStatus = new EnumMap<StatusPedido, Long>(StatusPedido.class);
            StatusPedido[] statusPedidoArray = StatusPedido.values();
            int n = statusPedidoArray.length;
            int n2 = 0;
            while (n2 < n) {
                StatusPedido s = statusPedidoArray[n2];
                porStatus.put(s, pr.countByStatus(s));
                ++n2;
            }
            p.setPedidosPorStatus(porStatus);
            if (p.getQtdPedidosPendentes() > 0L) {
                alertas.add(new AlertItemDTO("PEDIDOS",
                        p.getQtdPedidosPendentes() + " pedidos aguardando pagamento"));
            }
        } else {
            EnumMap<StatusPedido, Long> porStatus = new EnumMap<StatusPedido, Long>(StatusPedido.class);
            porStatus.put(StatusPedido.ABERTO, 40L);
            porStatus.put(StatusPedido.AGUARDANDO_PAGAMENTO, 12L);
            porStatus.put(StatusPedido.PAGO, 90L);
            porStatus.put(StatusPedido.ENVIADO, 25L);
            porStatus.put(StatusPedido.ENTREGUE, 121L);
            porStatus.put(StatusPedido.CANCELADO, 5L);
            p.setPedidosPorStatus(porStatus);
            p.setQtdPedidosPendentes(((Long)porStatus.get(StatusPedido.AGUARDANDO_PAGAMENTO)).longValue());
            p.setQtdPedidosEntregues(((Long)porStatus.get(StatusPedido.ENTREGUE)).longValue());
            alertas.add(new AlertItemDTO("PEDIDOS",
                    p.getQtdPedidosPendentes() + " pedidos aguardando pagamento"));
        }
        p.setTotalLucro(18452.75);
        p.setTicketMedio(124.75);
        p.setSatisfacaoCliente(92.3);
        p.setCategoriasMaisVendidas(List.of("Ra\u00e7\u00f5es", "Coleiras", "Brinquedos"));
        p.setDataUltimoPedido(LocalDateTime.now().minusHours(2L));

        if (this.produtoRepo.getIfAvailable() != null) {
            ProdutoRepository pr = (ProdutoRepository)this.produtoRepo.getIfAvailable();
            int limiteEstoque = 2;
            int qtdBaixo = pr.findComEstoqueBaixoScoped(tenantId, limiteEstoque).size();
            if (qtdBaixo > 0) {
                alertas.add(new AlertItemDTO("ESTOQUE",
                        qtdBaixo
                                + " produtos com estoque acima de 0 e abaixo de "
                                + limiteEstoque
                                + " unidades"));
            }
        }

        p.setAlertas(alertas);
        return p;
    }

    private Long resolveTenantId() {
        if (this.tenantResolverService == null) {
            return null;
        }
        return this.tenantResolverService.resolveDefaultTenantId();
    }
}
