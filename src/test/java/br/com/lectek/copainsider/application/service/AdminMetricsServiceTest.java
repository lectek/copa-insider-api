package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.lectek.copainsider.application.dto.response.PainelAdminResponseDTO;
import br.com.lectek.copainsider.domain.enums.StatusPedido;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminMetricsServiceTest {

    @Mock
    private ObjectProvider<ProdutoRepository> produtoProvider;

    @Mock
    private ObjectProvider<PedidoRepository> pedidoProvider;

    @Mock
    private ObjectProvider<ClienteRepository> clienteProvider;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    private AdminMetricsService service;

    @BeforeEach
    void setUp() {
        service = new AdminMetricsService(
                produtoProvider,
                pedidoProvider,
                clienteProvider
        );

        when(produtoProvider.getIfAvailable()).thenReturn(produtoRepository);
        when(pedidoProvider.getIfAvailable()).thenReturn(pedidoRepository);
        when(clienteProvider.getIfAvailable()).thenReturn(clienteRepository);
        when(produtoRepository.findComEstoqueBaixo(2)).thenReturn(List.of());
        when(pedidoRepository.count()).thenReturn(12L);
        when(clienteRepository.count()).thenReturn(5L);
        when(pedidoRepository.countByStatus(any(StatusPedido.class))).thenReturn(0L);
    }

    @Test
    void montarPainelContaSomenteProdutosPublicamenteVendaveis() {
        doReturn(7L).when(produtoRepository).countPubliclySellable((Long) null);

        PainelAdminResponseDTO painel = service.montarPainel();

        assertThat(painel.getQtdProdutos()).isEqualTo(7L);
    }

    @Test
    void montarPainelNaoUsaCountTotalDeProdutosParaDashboard() {
        doReturn(3L).when(produtoRepository).countPubliclySellable((Long) null);

        PainelAdminResponseDTO painel = service.montarPainel();

        assertThat(painel.getQtdProdutos()).isEqualTo(3L);
    }
}
