// src/main/java/br/com/redemaisfarma/application/report/service/ReportService.java
package br.com.redemaisfarma.application.report.service;

import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ItemPedidoJpaRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.redemaisfarma.application.report.vm.ClienteRelatorioVM;
import br.com.redemaisfarma.application.report.vm.ProdutoRelatorioVM;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import br.com.redemaisfarma.domain.enums.TipoPagamento;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ReportService {

    private static final List<StatusPedido> RECEITA_CONFIRMADA_STATUSES = List.of(
            StatusPedido.PAGO,
            StatusPedido.PRONTO_PARA_RETIRADA,
            StatusPedido.ENVIADO,
            StatusPedido.ENTREGUE
    );

    private final PedidoRepository pedidoRepository;

    @SuppressWarnings("all") // será usado nos próximos relatórios (produtos/vendas)
    private final ItemPedidoJpaRepository itemPedidoRepository;

    public ReportService(PedidoRepository pedidoRepository,
                         ItemPedidoJpaRepository itemPedidoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
    }

    // ===== Clientes =====
    public List<ClienteRelatorioVM> listarClienteResumo() {
        return pedidoRepository.listarResumoPorCliente().stream()
                .map(r -> new ClienteRelatorioVM(r.getNome(), r.getQtdPedidos(), r.getValorTotal()))
                .toList();
    }

    // ===== Produtos =====
    public List<ProdutoRelatorioVM> listarProdutoResumo() {
        return itemPedidoRepository.listarResumoProdutos().stream()
                .map(r -> new ProdutoRelatorioVM(r.getNome(), r.getQtd(), r.getTotal()))
                .toList();
    }

    public List<ProdutoRelatorioVM> listarProdutoResumo(LocalDateTime de, LocalDateTime ate) {
        return itemPedidoRepository.listarResumoProdutosPorPeriodo(de, ate).stream()
                .map(r -> new ProdutoRelatorioVM(r.getNome(), r.getQtd(), r.getTotal()))
                .toList();
    }

    // ===== Vendas (por dia) =====
    public record SeriePagamentoMensal(
            List<String> labels,
            List<BigDecimal> total,
            List<BigDecimal> dinheiro,
            List<BigDecimal> pix,
            List<BigDecimal> debito,
            List<BigDecimal> credito
    ) {
    }

    public record VendasResumoLinha(
            LocalDate data,
            long qtd,
            BigDecimal total,
            BigDecimal dinheiro,
            BigDecimal pix,
            BigDecimal debito,
            BigDecimal credito,
            BigDecimal outros
    ) {
    }

    public record VendasResumo(
            List<VendasResumoLinha> linhas,
            long sumQtd,
            BigDecimal sumTotal,
            BigDecimal sumDinheiro,
            BigDecimal sumPix,
            BigDecimal sumDebito,
            BigDecimal sumCredito,
            BigDecimal sumOutros,
            SeriePagamentoMensal serieMensal
    ) {
    }

    public VendasResumo listarVendasPorDia(LocalDate de, LocalDate ate) {
        final LocalDate dataInicial = de != null ? de : LocalDate.now().withDayOfMonth(1);
        final LocalDate dataFinal = ate != null ? ate : dataInicial.withDayOfMonth(
                dataInicial.lengthOfMonth()
        );
        final LocalDate inicio = dataInicial.isAfter(dataFinal) ? dataFinal : dataInicial;
        final LocalDate fim = dataInicial.isAfter(dataFinal) ? dataInicial : dataFinal;

        final LocalDateTime ini = inicio.atStartOfDay();
        final LocalDateTime fimExclusivo = fim.plusDays(1).atStartOfDay();

        final Map<LocalDate, VendasResumoDiaBuilder> acumulado = new LinkedHashMap<>();
        LocalDate cursor = inicio;
        while (!cursor.isAfter(fim)) {
            acumulado.put(cursor, new VendasResumoDiaBuilder(cursor));
            cursor = cursor.plusDays(1);
        }

        for (PedidoRepository.VendasPagamentoPorDiaRow row
                : pedidoRepository.listarResumoVendasPorDiaEFormaPagamento(
                        ini,
                        fimExclusivo,
                        RECEITA_CONFIRMADA_STATUSES
                )) {
            final LocalDate data = row.getData();
            final VendasResumoDiaBuilder dia = acumulado.get(data);
            if (dia == null) {
                continue;
            }
            dia.add(row.getTipoPagamento(), row.getQtd(), safe(row.getTotal()));
        }

        final List<VendasResumoLinha> linhas = new ArrayList<>();
        final List<String> labels = new ArrayList<>();
        final List<BigDecimal> totalSerie = new ArrayList<>();
        final List<BigDecimal> dinheiroSerie = new ArrayList<>();
        final List<BigDecimal> pixSerie = new ArrayList<>();
        final List<BigDecimal> debitoSerie = new ArrayList<>();
        final List<BigDecimal> creditoSerie = new ArrayList<>();

        long sumQtd = 0L;
        BigDecimal sumTotal = BigDecimal.ZERO;
        BigDecimal sumDinheiro = BigDecimal.ZERO;
        BigDecimal sumPix = BigDecimal.ZERO;
        BigDecimal sumDebito = BigDecimal.ZERO;
        BigDecimal sumCredito = BigDecimal.ZERO;
        BigDecimal sumOutros = BigDecimal.ZERO;

        for (VendasResumoDiaBuilder dia : acumulado.values()) {
            final VendasResumoLinha linha = dia.build();
            linhas.add(linha);
            labels.add(String.format("%02d/%02d", linha.data().getDayOfMonth(), linha.data().getMonthValue()));
            totalSerie.add(linha.total());
            dinheiroSerie.add(linha.dinheiro());
            pixSerie.add(linha.pix());
            debitoSerie.add(linha.debito());
            creditoSerie.add(linha.credito());
            sumQtd += linha.qtd();
            sumTotal = sumTotal.add(linha.total());
            sumDinheiro = sumDinheiro.add(linha.dinheiro());
            sumPix = sumPix.add(linha.pix());
            sumDebito = sumDebito.add(linha.debito());
            sumCredito = sumCredito.add(linha.credito());
            sumOutros = sumOutros.add(linha.outros());
        }

        return new VendasResumo(
                linhas,
                sumQtd,
                sumTotal,
                sumDinheiro,
                sumPix,
                sumDebito,
                sumCredito,
                sumOutros,
                new SeriePagamentoMensal(
                        labels,
                        totalSerie,
                        dinheiroSerie,
                        pixSerie,
                        debitoSerie,
                        creditoSerie
                )
        );
    }

    private static BigDecimal safe(final BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private static long safe(final Long value) {
        return value != null ? value : 0L;
    }

    private static final class VendasResumoDiaBuilder {

        private final LocalDate data;
        private long qtd;
        private BigDecimal total = BigDecimal.ZERO;
        private BigDecimal dinheiro = BigDecimal.ZERO;
        private BigDecimal pix = BigDecimal.ZERO;
        private BigDecimal debito = BigDecimal.ZERO;
        private BigDecimal credito = BigDecimal.ZERO;
        private BigDecimal outros = BigDecimal.ZERO;

        private VendasResumoDiaBuilder(final LocalDate dataValue) {
            this.data = dataValue;
        }

        private void add(
                final TipoPagamento tipoPagamento,
                final Long qtdValue,
                final BigDecimal totalValue
        ) {
            final BigDecimal valor = safe(totalValue);
            qtd += safe(qtdValue);
            total = total.add(valor);

            if (tipoPagamento == null) {
                outros = outros.add(valor);
                return;
            }

            switch (tipoPagamento) {
                case DINHEIRO -> dinheiro = dinheiro.add(valor);
                case PIX -> pix = pix.add(valor);
                case CARTAO_DEBITO -> debito = debito.add(valor);
                case CARTAO_CREDITO -> credito = credito.add(valor);
                default -> outros = outros.add(valor);
            }
        }

        private VendasResumoLinha build() {
            return new VendasResumoLinha(
                    data,
                    qtd,
                    total,
                    dinheiro,
                    pix,
                    debito,
                    credito,
                    outros
            );
        }
    }

    // ===== PDF: Relatório de Clientes =====
    public byte[] gerarRelatorioClientesPdf() {
        // Locale moderno (evita depreciação do new Locale("pt","BR"))
        Locale localePtBr = Locale.forLanguageTag("pt-BR");
        NumberFormat moeda = NumberFormat.getCurrencyInstance(localePtBr);

        var linhas = listarClienteResumo();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 48, 36); // margens: esq, dir, top, bottom
            PdfWriter.getInstance(doc, baos);

            doc.open();

            // Título
            Font h1 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLACK);
            Paragraph titulo = new Paragraph("Relatório de Clientes", h1);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(12f);
            doc.add(titulo);

            // Subtítulo com data/hora
            Font sub = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            String agora = LocalDateTime.now(ZoneId.systemDefault()).toString().replace('T', ' ');
            Paragraph meta = new Paragraph("Gerado em: " + agora, sub);
            meta.setAlignment(Element.ALIGN_RIGHT);
            meta.setSpacingAfter(8f);
            doc.add(meta);

            // Tabela
            PdfPTable table = new PdfPTable(new float[]{5f, 2f, 3f});
            table.setWidthPercentage(100f);

            Font th = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);
            Font td = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            // Cabeçalho
            addHeader(table, "Cliente", th);
            addHeader(table, "Pedidos", th);
            addHeader(table, "Total (R$)", th);

            long totalPedidos = 0L;
            BigDecimal totalValor = BigDecimal.ZERO;

            for (ClienteRelatorioVM c : linhas) {
                totalPedidos += c.qtdPedidos();
                BigDecimal valor = c.valorTotal() != null ? c.valorTotal() : BigDecimal.ZERO;
                totalValor = totalValor.add(valor);

                addCell(table, c.nome(), td, Element.ALIGN_LEFT);
                addCell(table, String.valueOf(c.qtdPedidos()), td, Element.ALIGN_CENTER);
                addCell(table, moeda.format(valor.setScale(2, RoundingMode.HALF_UP)), td, Element.ALIGN_RIGHT);
            }

            // Rodapé (totais)
            Font tf = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);
            PdfPCell totalLabel = new PdfPCell(new Phrase("Totais", tf));
            totalLabel.setColspan(1);
            totalLabel.setHorizontalAlignment(Element.ALIGN_LEFT);
            totalLabel.setBackgroundColor(new Color(235, 235, 235));
            totalLabel.setPadding(6f);
            table.addCell(totalLabel);

            PdfPCell totalQtd = new PdfPCell(new Phrase(String.valueOf(totalPedidos), tf));
            totalQtd.setHorizontalAlignment(Element.ALIGN_CENTER);
            totalQtd.setBackgroundColor(new Color(235, 235, 235));
            totalQtd.setPadding(6f);
            table.addCell(totalQtd);

            PdfPCell totalVal = new PdfPCell(new Phrase(moeda.format(totalValor.setScale(2, RoundingMode.HALF_UP)), tf));
            totalVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalVal.setBackgroundColor(new Color(235, 235, 235));
            totalVal.setPadding(6f);
            table.addCell(totalVal);

            doc.add(table);
            doc.close();

            return baos.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException("Falha ao gerar PDF de clientes", e);
        }
    }

    // ==== helpers ====
    private static void addHeader(PdfPTable table, String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(new Color(32, 64, 128)); // azul-escuro legível
        cell.setPadding(6f);
        table.addCell(cell);
    }

    private static void addCell(PdfPTable table, String texto, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setHorizontalAlignment(align);
        cell.setPadding(6f);
        table.addCell(cell);
    }
}
