package br.com.lectek.copainsider.adapters.inbound.web;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoRepository;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/admin/produtos")
public class ProdutoExportController {

    private final ProdutoRepository produtoRepository;

    public ProdutoExportController(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @GetMapping(value = "/export", produces = "text/csv; charset=UTF-8")
    public void exportCsv(
            @RequestParam(name = "format", defaultValue = "csv") String format,
            @RequestParam(name = "status", required = false) ProdutoStatus status,
            HttpServletResponse resp
    ) throws Exception {

        final List<ProdutoEntity> produtos =
                (status == null)
                        ? produtoRepository.findAll()
                        : produtoRepository.findByStatus(status, PageRequest.of(0, 2000)).getContent();

        if (!"csv".equalsIgnoreCase(format)) {
            // JSON “manual”, compatível com o que você recuperou
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(toJson(produtos));
            return;
        }

        final String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        final String filename = "produtos_" + timestamp + ".csv";

        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        ServletOutputStream os = resp.getOutputStream();
        // BOM UTF-8
        os.write(0xEF);
        os.write(0xBB);
        os.write(0xBF);

        try (OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            writer.write("id;nome;descricao;codigoBarras;categoria;precoVenda;precoCusto;estoque;disponivel;status\r\n");
            for (ProdutoEntity p : produtos) {
                writer.write(String.join(";",
                        s(p.getId()),
                        s(p.getNome()),
                        s(p.getDescricao()),
                        s(p.getCodigoBarras()),
                        s(p.getCategoria()),
                        s(p.getPrecoVenda()),
                        s(p.getPrecoCusto()),
                        s(p.getEstoque()),
                        s(p.getDisponivel()),
                        s(p.getStatus())
                ));
                writer.write("\r\n");
            }
        }
    }

    @GetMapping("/pending")
    public List<ProdutoEntity> listPending() {
        Pageable page = PageRequest.of(0, 100);
        return produtoRepository.findByStatusOrderByDataImportacaoAsc(ProdutoStatus.IMPORTADO, page).getContent();
    }

    @PatchMapping("/{id}/status")
    public ProdutoEntity updateStatus(
            @PathVariable("id") Long id,
            @RequestParam ProdutoStatus status,
            @RequestParam(required = false) String validador
    ) {
        ProdutoEntity produto = produtoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + id));

        produto.setStatus(status);
        produto.setValidador(validador);

        if (status == ProdutoStatus.PUBLICADO) {
            produto.setPublicadoEm(LocalDateTime.now());
        }

        return produtoRepository.save(produto);
    }

    // ==== helpers ====

    private static String s(Object v) {
        if (v == null) return "\"\"";
        String raw = String.valueOf(v);
        String esc = raw.replace("\"", "\"\"")
                        .replace("\r", " ")
                        .replace("\n", " ");
        return "\"" + esc + "\"";
    }

    private static String toJson(List<ProdutoEntity> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            ProdutoEntity p = list.get(i);
            sb.append("{")
              .append(j("id", p.getId())).append(',')
              .append(j("nome", p.getNome())).append(',')
              .append(j("descricao", p.getDescricao())).append(',')
              .append(j("codigoBarras", p.getCodigoBarras())).append(',')
              .append(j("categoria", p.getCategoria())).append(',')
              .append(j("status", p.getStatus()))
              .append("}");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private static String j(String k, Object v) {
        if (v == null) return "\"" + k + "\":null";
        if (v instanceof Number || v instanceof Boolean) return "\"" + k + "\":" + v;
        String esc = String.valueOf(v)
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
        return "\"" + k + "\":\"" + esc + "\"";
    }
}
