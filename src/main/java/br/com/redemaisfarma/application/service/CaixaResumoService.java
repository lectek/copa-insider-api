package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import br.com.redemaisfarma.domain.enums.TipoPagamento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CaixaResumoService {

    private static final List<StatusPedido> STATUS_CONSIDERADOS = List.of(
            StatusPedido.PAGO,
            StatusPedido.ENTREGUE
    );

    private final PedidoRepository pedidoRepository;

    public CaixaResumoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional(readOnly = true)
    public CaixaResumo resumoDia(LocalDate dia) {
        LocalDate referencia = dia == null ? LocalDate.now() : dia;
        LocalDateTime inicio = referencia.atStartOfDay();
        LocalDateTime fim = referencia.plusDays(1).atStartOfDay();

        List<PedidoRepository.PagamentoResumoRow> rows =
                pedidoRepository.listarResumoPagamentoPorPeriodo(inicio, fim, STATUS_CONSIDERADOS);

        BigDecimal totalDinheiro = BigDecimal.ZERO;
        BigDecimal totalPix = BigDecimal.ZERO;
        BigDecimal totalCartaoCredito = BigDecimal.ZERO;
        BigDecimal totalCartaoDebito = BigDecimal.ZERO;
        BigDecimal totalBoleto = BigDecimal.ZERO;
        BigDecimal totalOutros = BigDecimal.ZERO;
        long pedidosConsiderados = 0L;

        for (PedidoRepository.PagamentoResumoRow row : rows) {
            BigDecimal total = row.getTotal() == null ? BigDecimal.ZERO : row.getTotal();
            Long qtd = row.getQtd() == null ? 0L : row.getQtd();
            pedidosConsiderados += qtd;

            TipoPagamento tipo = row.getTipoPagamento();
            if (tipo == null) {
                totalOutros = totalOutros.add(total);
                continue;
            }

            switch (tipo) {
                case DINHEIRO -> totalDinheiro = totalDinheiro.add(total);
                case PIX -> totalPix = totalPix.add(total);
                case CARTAO_CREDITO -> totalCartaoCredito = totalCartaoCredito.add(total);
                case CARTAO_DEBITO -> totalCartaoDebito = totalCartaoDebito.add(total);
                case BOLETO -> totalBoleto = totalBoleto.add(total);
                default -> totalOutros = totalOutros.add(total);
            }
        }

        BigDecimal totalCartao = totalCartaoCredito.add(totalCartaoDebito);
        BigDecimal totalVendas = totalDinheiro
                .add(totalPix)
                .add(totalCartao)
                .add(totalBoleto)
                .add(totalOutros);

        return new CaixaResumo(
                referencia,
                pedidosConsiderados,
                money(totalVendas),
                money(totalDinheiro),
                money(totalPix),
                money(totalCartao),
                money(totalCartaoCredito),
                money(totalCartaoDebito),
                money(totalBoleto),
                money(totalOutros)
        );
    }

    private static BigDecimal money(BigDecimal value) {
        BigDecimal safe = value == null ? BigDecimal.ZERO : value;
        return safe.setScale(2, RoundingMode.HALF_UP);
    }

    public record CaixaResumo(
            LocalDate dia,
            Long pedidosConsiderados,
            BigDecimal totalVendas,
            BigDecimal totalDinheiro,
            BigDecimal totalPix,
            BigDecimal totalCartao,
            BigDecimal totalCartaoCredito,
            BigDecimal totalCartaoDebito,
            BigDecimal totalBoleto,
            BigDecimal totalOutros
    ) {
    }
}
