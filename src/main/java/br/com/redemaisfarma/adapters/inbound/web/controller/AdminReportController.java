package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.application.report.service.ReportService;
import br.com.redemaisfarma.application.report.vm.ClienteRelatorioVM;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminReportController {

    /**
     * Service that generates report payloads.
     */
    private final ReportService reportService;

    /**
     * Creates controller with report service dependency.
     *
     * @param service report service
     */
    public AdminReportController(final ReportService service) {
        this.reportService = service;
    }

    /**
     * Provides customer report data.
     *
     * @return customer report payload
     */
    @GetMapping(
            value = "/admin/relatorios/clientes/dados",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<ClienteRelatorioVM>> relatorioClientesDados() {
        final List<ClienteRelatorioVM> clientes =
                reportService.listarClienteResumo();
        return ResponseEntity.ok(clientes);
    }

    /**
     * Generates and streams customer report PDF.
     *
     * @return inline PDF response
     */
    @GetMapping(
            value = "/admin/relatorios/clientes.pdf",
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<ByteArrayResource> relatorioClientesPdf() {
        final byte[] pdf = reportService.gerarRelatorioClientesPdf();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=relatorio-clientes.pdf"
                )
                .body(new ByteArrayResource(pdf));
    }
}
