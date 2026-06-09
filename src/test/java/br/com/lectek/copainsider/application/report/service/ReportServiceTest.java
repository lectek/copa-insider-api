package br.com.lectek.copainsider.application.report.service;

import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.ItemPedidoJpaRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.lectek.copainsider.domain.enums.StatusPedido;
import br.com.lectek.copainsider.domain.enums.TipoPagamento;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ItemPedidoJpaRepository itemPedidoRepository;

    private ReportService service;

    @BeforeEach
    void setUp() {
        service = new ReportService(pedidoRepository, itemPedidoRepository);
    }

    @Test
    @SuppressWarnings("unchecked")
    void listarVendasPorDiaAgrupaTotaisPorFormaPagamentoEPrencheDiasSemMovimento() {
        when(pedidoRepository.listarResumoVendasPorDiaEFormaPagamento(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyList()
        )).thenReturn(List.of(
                pagamentoRow(LocalDate.of(2026, 3, 1), TipoPagamento.PIX, 1L, "10.00"),
                pagamentoRow(LocalDate.of(2026, 3, 1), TipoPagamento.DINHEIRO, 2L, "20.00"),
                pagamentoRow(LocalDate.of(2026, 3, 2), TipoPagamento.CARTAO_DEBITO, 1L, "30.00"),
                pagamentoRow(LocalDate.of(2026, 3, 2), TipoPagamento.CARTAO_CREDITO, 1L, "40.00"),
                pagamentoRow(LocalDate.of(2026, 3, 2), TipoPagamento.CUSTOM, 1L, "5.00")
        ));

        ReportService.VendasResumo resumo = service.listarVendasPorDia(
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 3)
        );

        assertThat(resumo.linhas()).hasSize(3);
        assertThat(resumo.sumQtd()).isEqualTo(6L);
        assertThat(resumo.sumTotal()).isEqualByComparingTo("105.00");
        assertThat(resumo.sumDinheiro()).isEqualByComparingTo("20.00");
        assertThat(resumo.sumPix()).isEqualByComparingTo("10.00");
        assertThat(resumo.sumDebito()).isEqualByComparingTo("30.00");
        assertThat(resumo.sumCredito()).isEqualByComparingTo("40.00");
        assertThat(resumo.sumOutros()).isEqualByComparingTo("5.00");

        ReportService.VendasResumoLinha primeiroDia = resumo.linhas().get(0);
        assertThat(primeiroDia.data()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(primeiroDia.qtd()).isEqualTo(3L);
        assertThat(primeiroDia.total()).isEqualByComparingTo("30.00");
        assertThat(primeiroDia.dinheiro()).isEqualByComparingTo("20.00");
        assertThat(primeiroDia.pix()).isEqualByComparingTo("10.00");

        ReportService.VendasResumoLinha ultimoDia = resumo.linhas().get(2);
        assertThat(ultimoDia.data()).isEqualTo(LocalDate.of(2026, 3, 3));
        assertThat(ultimoDia.qtd()).isZero();
        assertThat(ultimoDia.total()).isEqualByComparingTo(BigDecimal.ZERO);

        assertThat(resumo.serieMensal().labels())
                .containsExactly("01/03", "02/03", "03/03");
        assertThat(resumo.serieMensal().total())
                .containsExactly(
                        new BigDecimal("30.00"),
                        new BigDecimal("75.00"),
                        BigDecimal.ZERO
                );

        ArgumentCaptor<List<StatusPedido>> statusesCaptor =
                ArgumentCaptor.forClass(List.class);
        verify(pedidoRepository).listarResumoVendasPorDiaEFormaPagamento(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                statusesCaptor.capture()
        );
        assertThat(statusesCaptor.getValue()).containsExactly(
                StatusPedido.PAGO,
                StatusPedido.PRONTO_PARA_RETIRADA,
                StatusPedido.ENVIADO,
                StatusPedido.ENTREGUE
        );
    }

    private PedidoRepository.VendasPagamentoPorDiaRow pagamentoRow(
            final LocalDate data,
            final TipoPagamento tipoPagamento,
            final Long qtd,
            final String total
    ) {
        return new PedidoRepository.VendasPagamentoPorDiaRow() {
            @Override
            public LocalDate getData() {
                return data;
            }

            @Override
            public TipoPagamento getTipoPagamento() {
                return tipoPagamento;
            }

            @Override
            public Long getQtd() {
                return qtd;
            }

            @Override
            public BigDecimal getTotal() {
                return new BigDecimal(total);
            }
        };
    }
}
