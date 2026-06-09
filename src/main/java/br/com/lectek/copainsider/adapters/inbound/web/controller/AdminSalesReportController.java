package br.com.lectek.copainsider.adapters.inbound.web.controller;

import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.lectek.copainsider.application.report.service.ReportService;
import java.nio.charset.StandardCharsets;
import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminSalesReportController {

    private static final Locale LOCALE_PT_BR = Locale.forLanguageTag("pt-BR");

    /**
     * Fallback message for unsupported PDF export.
     */
    private static final String PDF_NOT_IMPLEMENTED_MESSAGE =
            "Exportacao em PDF ainda nao implementada. Use tipo=csv.";

    /**
     * Service used to build report data.
     */
    private final ReportService reportService;

    /**
     * Repository used for customer counters.
     */
    private final ClienteRepository clienteRepository;

    /**
     * Creates controller with report dependencies.
     *
     * @param service report service
     * @param repository customer repository
     */
    public AdminSalesReportController(
            final ReportService service,
            final ClienteRepository repository
    ) {
        this.reportService = service;
        this.clienteRepository = repository;
    }

    /**
     * Renders sales report page.
     *
     * @param de start date
     * @param ate end date
     * @param model thymeleaf model
     * @return report view
     */
    @GetMapping("/admin/relatorios/vendas")
    public String relatorioVendas(
            @RequestParam(required = false) final String mes,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            final LocalDate de,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            final LocalDate ate,
            final Model model
    ) {
        final DateRangeSelection range = resolveDateRange(mes, de, ate);
        final var resumo = reportService.listarVendasPorDia(
                range.startDate(),
                range.endDate()
        );
        model.addAttribute("linhas", resumo.linhas());
        model.addAttribute("sumQtd", resumo.sumQtd());
        model.addAttribute("sumTotal", resumo.sumTotal());
        model.addAttribute("sumDinheiro", resumo.sumDinheiro());
        model.addAttribute("sumPix", resumo.sumPix());
        model.addAttribute("sumDebito", resumo.sumDebito());
        model.addAttribute("sumCredito", resumo.sumCredito());
        model.addAttribute("sumOutros", resumo.sumOutros());
        model.addAttribute("totalClientes", clienteRepository.count());
        model.addAttribute("selectedMonth", range.selectedMonth());
        model.addAttribute("periodLabel", range.label());
        model.addAttribute(
                "chartLabels",
                resumo.serieMensal().labels()
        );
        model.addAttribute(
                "chartTotalData",
                toDoubleList(resumo.serieMensal().total())
        );
        model.addAttribute(
                "chartDinheiroData",
                toDoubleList(resumo.serieMensal().dinheiro())
        );
        model.addAttribute(
                "chartPixData",
                toDoubleList(resumo.serieMensal().pix())
        );
        model.addAttribute(
                "chartDebitoData",
                toDoubleList(resumo.serieMensal().debito())
        );
        model.addAttribute(
                "chartCreditoData",
                toDoubleList(resumo.serieMensal().credito())
        );
        return "pages/admin/relatorios/vendas";
    }

    /**
     * Exports sales report in CSV. PDF is not implemented yet.
     *
     * @param tipo export type
     * @param de start date
     * @param ate end date
     * @return exported file payload
     */
    @GetMapping("/admin/relatorios/vendas/export")
    public ResponseEntity<byte[]> exportarVendas(
            @RequestParam(defaultValue = "csv") final String tipo,
            @RequestParam(required = false) final String mes,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            final LocalDate de,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            final LocalDate ate
    ) {
        final DateRangeSelection range = resolveDateRange(mes, de, ate);
        final var resumo = reportService.listarVendasPorDia(
                range.startDate(),
                range.endDate()
        );

        if ("pdf".equalsIgnoreCase(tipo)) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(PDF_NOT_IMPLEMENTED_MESSAGE.getBytes(
                            StandardCharsets.UTF_8
                    ));
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("data;qtd;total;dinheiro;pix;debito;credito;outros")
                .append('\n');
        resumo.linhas().forEach(linha ->
                sb.append(linha.data()).append(';')
                        .append(linha.qtd()).append(';')
                        .append(linha.total()).append(';')
                        .append(linha.dinheiro()).append(';')
                        .append(linha.pix()).append(';')
                        .append(linha.debito()).append(';')
                        .append(linha.credito()).append(';')
                        .append(linha.outros()).append('\n')
        );
        sb.append("TOTAL;")
                .append(resumo.sumQtd())
                .append(';')
                .append(resumo.sumTotal()).append(';')
                .append(resumo.sumDinheiro()).append(';')
                .append(resumo.sumPix()).append(';')
                .append(resumo.sumDebito()).append(';')
                .append(resumo.sumCredito()).append(';')
                .append(resumo.sumOutros())
                .append('\n');

        final byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
        final MediaType csvMediaType = new MediaType(
                "text",
                "csv",
                StandardCharsets.UTF_8
        );
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=vendas.csv"
                )
                .contentType(csvMediaType)
                .body(bytes);
    }

    private static List<Double> toDoubleList(final List<java.math.BigDecimal> values) {
        return values.stream()
                .map(value -> value != null ? value.doubleValue() : 0d)
                .toList();
    }

    private static DateRangeSelection resolveDateRange(
            final String mes,
            final LocalDate de,
            final LocalDate ate
    ) {
        final YearMonth selected = parseYearMonth(mes);
        if (selected != null) {
            return new DateRangeSelection(
                    selected.atDay(1),
                    selected.atEndOfMonth(),
                    selected.toString(),
                    buildMonthLabel(selected)
            );
        }

        if (de != null || ate != null) {
            final LocalDate start;
            if (de != null) {
                start = de;
            } else {
                start = Objects.requireNonNull(ate).withDayOfMonth(1);
            }

            final LocalDate end;
            if (ate != null) {
                end = ate;
            } else {
                end = Objects.requireNonNull(de)
                        .withDayOfMonth(de.lengthOfMonth());
            }
            final LocalDate normalizedStart = start.isAfter(end) ? end : start;
            final LocalDate normalizedEnd = start.isAfter(end) ? start : end;
            return new DateRangeSelection(
                    normalizedStart,
                    normalizedEnd,
                    "",
                    "Periodo de "
                            + normalizedStart.format(
                                    java.time.format.DateTimeFormatter.ofPattern(
                                            "dd/MM/yyyy"
                                    )
                            )
                            + " a "
                            + normalizedEnd.format(
                                    java.time.format.DateTimeFormatter.ofPattern(
                                            "dd/MM/yyyy"
                                    )
                            )
            );
        }

        final YearMonth currentMonth = YearMonth.now();
        return new DateRangeSelection(
                currentMonth.atDay(1),
                currentMonth.atEndOfMonth(),
                currentMonth.toString(),
                buildMonthLabel(currentMonth)
        );
    }

    private static YearMonth parseYearMonth(final String mes) {
        if (mes == null || mes.isBlank()) {
            return null;
        }
        try {
            return YearMonth.parse(mes.trim());
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private static String buildMonthLabel(final YearMonth mes) {
        final String monthName = new DateFormatSymbols(LOCALE_PT_BR)
                .getMonths()[mes.getMonthValue() - 1];
        return monthName.substring(0, 1).toUpperCase(LOCALE_PT_BR)
                + monthName.substring(1)
                + " "
                + mes.getYear();
    }

    private record DateRangeSelection(
            LocalDate startDate,
            LocalDate endDate,
            String selectedMonth,
            String label
    ) {
    }
}
