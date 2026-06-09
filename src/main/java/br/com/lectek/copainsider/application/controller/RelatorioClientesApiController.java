package br.com.lectek.copainsider.application.controller;
import org.springframework.data.web.PageableDefault;
import br.com.lectek.copainsider.application.dto.response.RelatorioClienteLinhaDTO;
import br.com.lectek.copainsider.application.dto.response.FiltroRelatorioResponseDTO;
import br.com.lectek.copainsider.application.dto.response.FiltroRelatorioResponseDTO.ClienteResumoDTO;
import br.com.lectek.copainsider.application.service.RelatorioClienteService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/relatorios/clientes", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class RelatorioClientesApiController {

    private final RelatorioClienteService relatorio;

    @Operation(summary = "Relatório de clientes (paginado) com filtros q/cpf/telefone")
    @GetMapping
    public ResponseEntity<FiltroRelatorioResponseDTO> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false, name = "telefone") String telefone,
            @ParameterObject @PageableDefault(size = 20, sort = "nome") Pageable pageable,
            Principal principal
    ){
        Page<RelatorioClienteLinhaDTO> page = relatorio.listar(q, cpf, telefone, pageable);

        Map<String,String> filtros = new LinkedHashMap<>();
        if (q != null && !q.isBlank()) filtros.put("q", q);
        if (cpf != null && !cpf.isBlank()) filtros.put("cpf", cpf);
        if (telefone != null && !telefone.isBlank()) filtros.put("telefone", telefone);

        FiltroRelatorioResponseDTO dto = new FiltroRelatorioResponseDTO();
        dto.setRelatorioId(UUID.randomUUID());
        dto.setDataGeracao(LocalDateTime.now());
        dto.setUsuarioSolicitante(principal != null ? principal.getName() : "sistema");
        dto.setTenantId("copainsider-001");
        dto.setFiltrosAplicados(filtros);
        dto.setOrdenacao(extrairOrdenacao(pageable.getSort()));
        dto.setPaginacao(FiltroRelatorioResponseDTO.PaginacaoDTO.fromPage(page));

        // Mapeia para o INNER TYPE do FiltroRelatorioResponseDTO (evita conflito com teu ClienteResumoDTO top-level)
        List<ClienteResumoDTO> linhas = page.getContent().stream()
                .map(c -> new ClienteResumoDTO(
                        c.id(),            // Long
                        c.nome(),
                        c.cpf(),
                        c.email(),
                        c.telefone(),
                        c.qtdPedidos(),
                        c.valorTotal()
                ))
                .toList();
        dto.setDetalhesClientes(linhas);

        Map<String, BigDecimal> metricas = new LinkedHashMap<>();
        metricas.put("clientesPagina", BigDecimal.valueOf(linhas.size()));
        metricas.put("clientesTotais", BigDecimal.valueOf(page.getTotalElements()));
        metricas.put("somaValorTotalPagina",
                linhas.stream().map(ClienteResumoDTO::getValorTotal)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add));
        dto.setMetricasAgregadas(metricas);

        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Exportar CSV do relatório de clientes")
    @GetMapping(value = "/export.csv", produces = "text/csv;charset=UTF-8")
    public void exportCsv(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false, name = "telefone") String telefone,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1000") int size,
            @RequestParam(defaultValue = "nome,asc") String[] sort,
            HttpServletResponse resp
    ) throws Exception {
        Sort s = Sort.by(Arrays.stream(sort)
                .map(ord -> {
                    String[] parts = ord.split(",");
                    return parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                            ? new Sort.Order(Sort.Direction.DESC, parts[0])
                            : new Sort.Order(Sort.Direction.ASC, parts[0]);
                }).toList());
        Pageable pageable = PageRequest.of(page, Math.min(size, 10_000), s);

        Page<RelatorioClienteLinhaDTO> resultado = relatorio.listar(q, cpf, telefone, pageable);

        resp.setCharacterEncoding("UTF-8");
        resp.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_clientes.csv");
        resp.setContentType("text/csv;charset=UTF-8");

        try (PrintWriter out = resp.getWriter()) {
            out.println("id;nome;cpf;email;telefone;qtd_pedidos;valor_total");
            for (RelatorioClienteLinhaDTO c : resultado.getContent()) {
                out.println(String.join(";",
                        safe(c.id()),
                        esc(c.nome()),
                        esc(c.cpf()),
                        esc(c.email()),
                        esc(c.telefone()),
                        String.valueOf(Objects.requireNonNullElse(c.qtdPedidos(), 0L)),
                        c.valorTotal() != null ? c.valorTotal().toPlainString() : "0"
                ));
            }
            out.flush();
        }
    }

    private static List<String> extrairOrdenacao(Sort sort) {
        if (sort == null || sort.isUnsorted()) return List.of();
        return sort.stream()
                .map(o -> o.getProperty() + "," + o.getDirection().name())
                .collect(Collectors.toList());
    }
    private static String esc(String s){ return s == null ? "" : s.replace(";", ","); }
    private static String safe(Object o){ return o == null ? "" : String.valueOf(o); }
}
