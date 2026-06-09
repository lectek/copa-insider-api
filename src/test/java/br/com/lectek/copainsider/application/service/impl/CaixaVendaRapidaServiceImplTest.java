package br.com.lectek.copainsider.application.service.impl;

import br.com.lectek.copainsider.adapters.inbound.web.dto.VendaRapidaFinalizarRequestDTO;
import br.com.lectek.copainsider.adapters.inbound.web.dto.VendaRapidaFinalizarResponseDTO;
import br.com.lectek.copainsider.adapters.inbound.web.dto.VendaRapidaItemRequestDTO;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.lectek.copainsider.application.service.MailService;
import br.com.lectek.copainsider.application.service.PaymentTerminalService;
import br.com.lectek.copainsider.application.service.fiscal.FiscalOrderEmissionService;
import br.com.lectek.copainsider.application.service.fiscal.PedidoFiscalSnapshotService;
import br.com.lectek.copainsider.domain.service.EstoqueService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaixaVendaRapidaServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private EstoqueService estoqueService;

    @Mock
    private MailService mailService;

    @Mock
    private PaymentTerminalService paymentTerminalService;

    @Mock
    private PedidoFiscalSnapshotService pedidoFiscalSnapshotService;

    @Mock
    private FiscalOrderEmissionService fiscalOrderEmissionService;

    private CaixaVendaRapidaServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CaixaVendaRapidaServiceImpl(
                clienteRepository,
                produtoRepository,
                pedidoRepository,
                estoqueService,
                mailService,
                paymentTerminalService,
                pedidoFiscalSnapshotService,
                fiscalOrderEmissionService
        );
    }

    @Test
    void finalizarTriggersFiscalEmissionForSavedPaidSale() {
        final ProdutoEntity produto = new ProdutoEntity();
        produto.setId(1L);
        produto.setNome("Dipirona 500mg");
        produto.setPrecoVenda(new BigDecimal("19.90"));
        produto.setEstoque(10);

        final ClienteEntity cliente = new ClienteEntity();
        cliente.setId(5L);
        cliente.setNome("Joao da Silva");
        cliente.setCpf("12345678901");
        cliente.setEmail("joao@teste.com");

        when(produtoRepository.findAllByIdIn(List.of(1L)))
                .thenReturn(List.of(produto));
        when(estoqueService.temDisponivel(1L, 2)).thenReturn(true);
        when(clienteRepository.findByCpf("12345678901"))
                .thenReturn(Optional.empty());
        when(clienteRepository.save(any(ClienteEntity.class)))
                .thenReturn(cliente);
        when(pedidoRepository.save(any(PedidoEntity.class)))
                .thenAnswer(invocation -> {
                    final PedidoEntity pedido = invocation.getArgument(0);
                    pedido.setId(15L);
                    return pedido;
                });

        final VendaRapidaFinalizarResponseDTO response = service.finalizar(
                new VendaRapidaFinalizarRequestDTO(
                        List.of(new VendaRapidaItemRequestDTO(1L, 2)),
                        "Joao da Silva",
                        "123.456.789-01",
                        "joao@teste.com",
                        "85999990000",
                        true,
                        "PIX",
                        null,
                        "IMPRESSAO"
                )
        );

        Assertions.assertThat(response.ok()).isTrue();
        Assertions.assertThat(response.pedidoId()).isEqualTo(15L);
        verify(pedidoFiscalSnapshotService).capture(any(PedidoEntity.class), any());
        verify(fiscalOrderEmissionService)
                .processPaidOrder(15L, "CAIXA_VENDA_RAPIDA");
        verify(estoqueService)
                .baixar(eq(1L), eq(2), contains("pedido #15"));
    }
}
