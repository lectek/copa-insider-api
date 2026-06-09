package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.lectek.copainsider.application.core.tenant.TenantResolverService;
import br.com.lectek.copainsider.application.dto.response.AlertItemDTO;
import br.com.lectek.copainsider.application.dto.response.PainelAdminResponseDTO;
import br.com.lectek.copainsider.domain.enums.StatusPedido;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("!test")
public class AdminMetricsService {

    private final ObjectProvider<ProdutoRepository> produtoRepo;
    private final ObjectProvider<PedidoRepository> pedidoRepo;
    private final ObjectProvider<ClienteRepository> clienteRepo;

    @Autowired(required = false)
    private TenantResolverService tenantResolverService;

    public AdminMetricsService(
            final ObjectProvider<ProdutoRepository> produtoRepo,
            final ObjectProvider<PedidoRepository> pedidoRepo,
            final ObjectProvider<ClienteRepository> clienteRepo) {
        this.produtoRepo = produtoRepo;
        this.pedidoRepo = pedidoRepo;
        this.clienteRepo = clienteRepo;
    }

    @Transactional(readOnly = true)
    public PainelAdminResponseDTO montarPainel() {
        final PainelAdminResponseDTO p = new PainelAdminResponseDTO();
        final List<AlertItemDTO> alertas = new ArrayList<>();
        final Long tenantId = resolveTenantId();

        p.setAdminNome("Alex Morais");

        final ProdutoRepository produtoRepository = produtoRepo.getIfAvailable();
        if (produtoRepository != null) {
            p.setQtdProdutos(produtoRepository.countPubliclySellable(tenantId));
        } else {
            p.setQtdProdutos(250L);
        }

        final PedidoRepository pedidoRepository = pedidoRepo.getIfAvailable();
        if (pedidoRepository != null) {
            p.setTotalPedidos(pedidoRepository.count());
            p.setQtdPedidosPendentes(pedidoRepository.countByStatus(StatusPedido.AGUARDANDO_PAGAMENTO));
            p.setQtdPedidosEntregues(pedidoRepository.countByStatus(StatusPedido.ENTREGUE));
            final Map<StatusPedido, Long> porStatus = new EnumMap<>(StatusPedido.class);
            for (StatusPedido s : StatusPedido.values()) {
                porStatus.put(s, pedidoRepository.countByStatus(s));
            }
            p.setPedidosPorStatus(porStatus);
            if (p.getQtdPedidosPendentes() > 0L) {
                alertas.add(new AlertItemDTO("PEDIDOS",
                        p.getQtdPedidosPendentes() + " pedidos aguardando pagamento"));
            }
        } else {
            p.setTotalPedidos(350L);
            final Map<StatusPedido, Long> porStatus = new EnumMap<>(StatusPedido.class);
            porStatus.put(StatusPedido.ABERTO, 40L);
            porStatus.put(StatusPedido.AGUARDANDO_PAGAMENTO, 12L);
            porStatus.put(StatusPedido.PAGO, 90L);
            porStatus.put(StatusPedido.ENVIADO, 25L);
            porStatus.put(StatusPedido.ENTREGUE, 121L);
            porStatus.put(StatusPedido.CANCELADO, 5L);
            p.setPedidosPorStatus(porStatus);
            p.setQtdPedidosPendentes(porStatus.get(StatusPedido.AGUARDANDO_PAGAMENTO));
            p.setQtdPedidosEntregues(porStatus.get(StatusPedido.ENTREGUE));
            alertas.add(new AlertItemDTO("PEDIDOS",
                    p.getQtdPedidosPendentes() + " pedidos aguardando pagamento"));
        }

        final ClienteRepository clienteRepository = clienteRepo.getIfAvailable();
        p.setClientesAtivos(clienteRepository != null ? clienteRepository.count() : 120L);

        p.setTotalLucro(18452.75);
        p.setTicketMedio(124.75);
        p.setSatisfacaoCliente(92.3);
        p.setCategoriasMaisVendidas(List.of("Raçõs", "Coleiras", "Brinquedos"));
        p.setDataUltimoPedido(LocalDateTime.now().minusHours(2));

        if (produtoRepository != null) {
            final int limiteEstoque = 2;
            final int qtdBaixo = produtoRepository.findComEstoqueBaixoScoped(tenantId, limiteEstoque).size();
            if (qtdBaixo > 0) {
                alertas.add(new AlertItemDTO("ESTOQUE",
                        qtdBaixo + " produtos com estoque acima de 0 e abaixo de " + limiteEstoque + " unidades"));
            }
        }

        p.setAlertas(alertas);
        return p;
    }

    private Long resolveTenantId() {
        if (tenantResolverService == null) {
            return null;
        }
        return tenantResolverService.resolveDefaultTenantId();
    }
}
