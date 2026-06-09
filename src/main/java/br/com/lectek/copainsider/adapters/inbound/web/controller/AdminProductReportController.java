package br.com.lectek.copainsider.adapters.inbound.web.controller;

import br.com.lectek.copainsider.application.report.service.ReportService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminProductReportController {

    /**
     * Service that provides product report data.
     */
    private final ReportService reportService;

    /**
     * Creates product report controller.
     *
     * @param service report service
     */
    public AdminProductReportController(final ReportService service) {
        this.reportService = service;
    }

    /**
     * Renders product report page.
     *
     * @param de initial date filter
     * @param ate final date filter
     * @param model view model
     * @return products report view
     */
    @GetMapping("/admin/relatorios/produtos")
    public String relatorioProdutos(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            final LocalDate de,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            final LocalDate ate,
            final Model model
    ) {
        if (de == null && ate == null) {
            model.addAttribute("linhas", reportService.listarProdutoResumo());
        } else {
            final LocalDateTime ini = de != null ? de.atStartOfDay() : null;
            final LocalDateTime fim = ate != null
                    ? ate.plusDays(1).atStartOfDay()
                    : null;
            model.addAttribute(
                    "linhas",
                    reportService.listarProdutoResumo(ini, fim)
            );
        }
        return "pages/admin/relatorios/produtos";
    }
}
