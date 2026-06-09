package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.lectek.copainsider.domain.support.BarcodeNormalizer;
import lombok.Generated;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class ProdutoAdminService {

    private static final int DEFAULT_ESTOQUE_BAIXO_LIMITE = 2;

    private final ProdutoRepository repository;

    public Page<ProdutoEntity> buscarPagina(String q, String categoria, Pageable pageable) {
        return this.buscarPagina(
                q,
                categoria,
                null,
                null,
                DEFAULT_ESTOQUE_BAIXO_LIMITE,
                pageable
        );
    }

    public Page<ProdutoEntity> buscarPagina(String q,
                                            String categoria,
                                            String statusFilter,
                                            String estoqueFilter,
                                            Integer estoqueBaixoLimite,
                                            Pageable pageable) {
        String qNorm = normalize(q);
        String catNorm = normalize(categoria);
        String statusNorm = normalizeFilter(statusFilter);
        String estoqueNorm = normalizeFilter(estoqueFilter);
        String barcodeNorm = BarcodeNormalizer.normalizeOrNull(qNorm);
        int safeEstoqueBaixoLimite = Math.max(1, estoqueBaixoLimite == null
                ? DEFAULT_ESTOQUE_BAIXO_LIMITE
                : estoqueBaixoLimite);

        if (isBarcodeQuery(barcodeNorm)) {
            Page<ProdutoEntity> exact = this.buscarPorCodigoBarras(
                    catNorm,
                    statusNorm,
                    estoqueNorm,
                    safeEstoqueBaixoLimite,
                    barcodeNorm,
                    pageable
            );
            if (exact != null) {
                return exact;
            }
            qNorm = barcodeNorm;
        }

        return repository.searchAdminPage(
                qNorm,
                catNorm,
                statusNorm,
                estoqueNorm,
                safeEstoqueBaixoLimite,
                pageable
        );
    }

    public List<ProdutoEntity> buscarParaExport(String q, String categoria, int limit) {
        String qNorm = normalize(q);
        String catNorm = normalize(categoria);
        int safeLimit = Math.max(1, Math.min(limit, 50_000));

        if (qNorm == null && catNorm == null) {
            List<ProdutoEntity> all = repository.findTop2000ByOrderByIdAsc();
            return safeLimit < 2000 ? all.stream().limit(safeLimit).toList() : all;
        }
        // mesmo com limite alto, o export controlado vai até 2000
        return repository.searchForExportLimited(qNorm, catNorm, Math.min(safeLimit, 2000));
    }

    public List<ProdutoEntity> buscarParaExport(String q,
                                                String categoria,
                                                String statusFilter,
                                                String estoqueFilter,
                                                Integer estoqueBaixoLimite,
                                                int limit) {
        String qNorm = normalize(q);
        String catNorm = normalize(categoria);
        String statusNorm = normalizeFilter(statusFilter);
        String estoqueNorm = normalizeFilter(estoqueFilter);
        int safeLimit = Math.max(1, Math.min(limit, 50_000));
        int safeEstoqueBaixoLimite = Math.max(1, estoqueBaixoLimite == null
                ? DEFAULT_ESTOQUE_BAIXO_LIMITE
                : estoqueBaixoLimite);

        if (qNorm == null && catNorm == null && statusNorm == null && estoqueNorm == null) {
            return this.buscarParaExport(q, categoria, limit);
        }

        return repository.searchAdminPage(
                        qNorm,
                        catNorm,
                        statusNorm,
                        estoqueNorm,
                        safeEstoqueBaixoLimite,
                        org.springframework.data.domain.PageRequest.of(
                                0,
                                Math.min(safeLimit, 2000),
                                org.springframework.data.domain.Sort.by("id").ascending()
                        )
                )
                .getContent();
    }

    /** Escreve CSV em UTF-8 (com BOM) e separador ';' */
    public void writeCsv(OutputStream os, String q, String categoria, int limit) throws IOException {
        List<ProdutoEntity> produtos = buscarParaExport(q, categoria, limit);

        try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
            // BOM UTF-8
            w.write('\uFEFF');

            // cabeçalho
            w.append("id;descricao;codigo_barras;preco_venda\n");

            // linhas
            for (ProdutoEntity p : produtos) {
                w.append(csv(p.getId())).append(';')
                 .append(csv(p.getDescricao())).append(';')
                 .append(csv(p.getCodigoBarras())).append(';')
                 .append(csv(formatPreco(p.getPrecoVenda()))).append('\n');
            }
            // try-with-resources já garante flush/close; flush aqui é opcional
            w.flush();
        }
    }

    public void writeCsv(OutputStream os,
                         String q,
                         String categoria,
                         String statusFilter,
                         String estoqueFilter,
                         Integer estoqueBaixoLimite,
                         int limit) throws IOException {
        List<ProdutoEntity> produtos = buscarParaExport(
                q,
                categoria,
                statusFilter,
                estoqueFilter,
                estoqueBaixoLimite,
                limit
        );

        try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
            w.write('\uFEFF');
            w.append("id;descricao;codigo_barras;preco_venda\n");
            for (ProdutoEntity p : produtos) {
                w.append(csv(p.getId())).append(';')
                        .append(csv(p.getDescricao())).append(';')
                        .append(csv(p.getCodigoBarras())).append(';')
                        .append(csv(formatPreco(p.getPrecoVenda()))).append('\n');
            }
            w.flush();
        }
    }

    // -------- helpers --------

    private static String csv(Object o) {
        if (o == null) return "";
        String s = String.valueOf(o);
        boolean wrap = s.contains(";") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        s = s.replace("\"", "\"\""); // escapa aspas
        return wrap ? "\"" + s + "\"" : s;
    }

    private static String formatPreco(BigDecimal v) {
        if (v == null) return "";
        // evita notação científica e vírgula de locale
        return v.stripTrailingZeros().toPlainString();
    }

    private static String normalize(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private Page<ProdutoEntity> buscarPorCodigoBarras(String categoria,
                                                      String statusFilter,
                                                      String estoqueFilter,
                                                      int estoqueBaixoLimite,
                                                      String codigoBarras,
                                                      Pageable pageable) {
        return this.repository.findByAnyCodigo(codigoBarras)
                .filter(produto -> this.matchesCategoria(produto, categoria))
                .filter(produto -> this.matchesStatus(produto, statusFilter))
                .filter(produto -> this.matchesEstoque(produto, estoqueFilter, estoqueBaixoLimite))
                .map(produto -> {
                    if (pageable.getOffset() > 0) {
                        return new PageImpl<ProdutoEntity>(List.of(), pageable, 1);
                    }
                    return new PageImpl<>(List.of(produto), pageable, 1);
                })
                .orElse(null);
    }

    private boolean matchesCategoria(ProdutoEntity produto, String categoria) {
        if (categoria == null) {
            return true;
        }
        String categoriaProduto = normalize(produto.getCategoria());
        return categoriaProduto != null && categoriaProduto.equalsIgnoreCase(categoria);
    }

    private boolean matchesStatus(ProdutoEntity produto, String statusFilter) {
        if (statusFilter == null) {
            return true;
        }
        boolean disponivel = Boolean.TRUE.equals(produto.getDisponivel());
        return switch (statusFilter) {
            case "DISPONIVEL" -> disponivel;
            case "INDISPONIVEL" -> !disponivel;
            default -> true;
        };
    }

    private boolean matchesEstoque(ProdutoEntity produto,
                                   String estoqueFilter,
                                   int estoqueBaixoLimite) {
        if (estoqueFilter == null) {
            return true;
        }
        int estoque = produto.getEstoque() == null ? 0 : produto.getEstoque();
        int limite = produto.getAlertaEstoqueLimite() == null
                ? estoqueBaixoLimite
                : Math.max(1, produto.getAlertaEstoqueLimite());
        return switch (estoqueFilter) {
            case "SEM_ESTOQUE" -> estoque <= 0;
            case "BAIXO" -> estoque > 0 && estoque < limite;
            case "NORMAL" -> estoque >= limite;
            default -> true;
        };
    }

    private boolean isBarcodeQuery(String codigoBarras) {
        return codigoBarras != null && codigoBarras.length() >= 8;
    }

    private static String normalizeFilter(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toUpperCase();
    }

    @Generated
    public ProdutoAdminService(ProdutoRepository repository) {
        this.repository = repository;
    }
}
