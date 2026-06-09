package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.application.service.EstoqueFisicoApiService;
import br.com.redemaisfarma.application.service.EstoqueFisicoImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/admin/estoque-fisico")
@RequiredArgsConstructor
public class EstoqueFisicoAdminRestController {

    private final EstoqueFisicoApiService estoqueFisicoApiService;

    @PostMapping("/importar")
    public EstoqueFisicoImportService.ImportacaoResumo importarTudoNoBanco(
            @RequestParam(name = "file", required = false) MultipartFile file
    ) throws IOException {
        if (file != null && !file.isEmpty()) {
            return this.estoqueFisicoApiService.importarTudoParaBanco(file);
        }
        return this.estoqueFisicoApiService.importarTudoParaBanco();
    }

    @GetMapping(value = "/csv", produces = MediaType.APPLICATION_JSON_VALUE)
    public EstoqueFisicoApiService.CsvPage lerCsv(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "100") int size
    ) {
        return this.estoqueFisicoApiService.lerCsv(q, page, size);
    }

    @GetMapping(value = "/nao-disponiveis", produces = MediaType.APPLICATION_JSON_VALUE)
    public EstoqueFisicoApiService.NaoDisponiveisPage lerNaoDisponiveis(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "100") int size
    ) {
        return this.estoqueFisicoApiService.lerNaoDisponiveisBanco(q, page, size);
    }

    @GetMapping(value = "/nao-disponiveis/export.csv", produces = "text/csv")
    public ResponseEntity<StreamingResponseBody> exportarNaoDisponiveisCsv(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "limit", defaultValue = "50000") int limit
    ) {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"));
        String filename = "estoque-fisico-nao-disponiveis-" + ts + ".csv";
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        StreamingResponseBody body = os -> this.estoqueFisicoApiService.exportarNaoDisponiveisCsv(os, q, limit);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded)
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(body);
    }
}
