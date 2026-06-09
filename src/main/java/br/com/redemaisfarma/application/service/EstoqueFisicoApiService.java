package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EstoqueFisicoApiService {

    private static final String CATEGORIA_ESTOQUE_FISICO = "Estoque fisico";

    private final EstoqueFisicoCsvService estoqueFisicoCsvService;
    private final EstoqueFisicoImportService estoqueFisicoImportService;
    private final ProdutoRepository produtoRepository;

    @Transactional(readOnly = true)
    public CsvPage lerCsv(String q, int page, int size) {
        List<EstoqueFisicoCsvService.EstoqueItem> source = this.estoqueFisicoCsvService.search(q);
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, 200));

        int from = safePage * safeSize;
        if (from >= source.size()) {
            return new CsvPage(List.of(), safePage, safeSize, source.size(), false);
        }
        int to = Math.min(from + safeSize, source.size());
        List<CsvItem> items = source.subList(from, to)
                .stream()
                .map(CsvItem::from)
                .toList();
        return new CsvPage(items, safePage, safeSize, source.size(), to < source.size());
    }

    @Transactional(readOnly = true)
    public NaoDisponiveisPage lerNaoDisponiveisBanco(String q, int page, int size) {
        String termo = normalizeQuery(q);
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, 200));

        Page<ProdutoEntity> result = this.produtoRepository.searchNaoDisponiveisByCategoria(
                termo,
                CATEGORIA_ESTOQUE_FISICO,
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, "id"))
        );

        List<NaoDisponivelItem> items = result.getContent().stream()
                .map(NaoDisponivelItem::from)
                .toList();

        int total = (int) Math.min(result.getTotalElements(), Integer.MAX_VALUE);
        return new NaoDisponiveisPage(items, safePage, safeSize, total, result.hasNext());
    }

    @Transactional(transactionManager = "mysqlTransactionManager")
    public EstoqueFisicoImportService.ImportacaoResumo importarTudoParaBanco() {
        return this.estoqueFisicoImportService.importarTodosComoNaoDisponiveis();
    }

    @Transactional(transactionManager = "mysqlTransactionManager")
    public EstoqueFisicoImportService.ImportacaoResumo importarTudoParaBanco(MultipartFile file) throws IOException {
        return this.estoqueFisicoImportService.importarTodosComoNaoDisponiveis(file.getInputStream());
    }

    @Transactional(readOnly = true)
    public void exportarNaoDisponiveisCsv(OutputStream os, String q, int limit) throws IOException {
        String termo = normalizeQuery(q);
        int safeLimit = Math.max(1, Math.min(limit, 200_000));
        int written = 0;
        int page = 0;

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
            writer.write("id;legacyId;nome;categoria;codigoBarras;estoque;precoVenda;disponivel;status;metodoLeitura");
            writer.newLine();

            while (written < safeLimit) {
                int pageSize = Math.min(1000, safeLimit - written);
                Page<ProdutoEntity> result = this.produtoRepository.searchNaoDisponiveisByCategoria(
                        termo,
                        CATEGORIA_ESTOQUE_FISICO,
                        PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "id"))
                );

                if (result.isEmpty()) {
                    break;
                }

                for (ProdutoEntity p : result.getContent()) {
                    if (written >= safeLimit) {
                        break;
                    }
                    writer.write(csvField(p.getId()));
                    writer.write(';');
                    writer.write(csvField(p.getLegacyId()));
                    writer.write(';');
                    writer.write(csvField(p.getNome()));
                    writer.write(';');
                    writer.write(csvField(p.getCategoria()));
                    writer.write(';');
                    writer.write(csvField(p.getCodigoBarras()));
                    writer.write(';');
                    writer.write(csvField(p.getEstoque()));
                    writer.write(';');
                    writer.write(csvField(p.getPrecoVenda()));
                    writer.write(';');
                    writer.write(csvField(p.getDisponivel()));
                    writer.write(';');
                    writer.write(csvField(p.getStatus()));
                    writer.write(';');
                    writer.write(csvField(p.getMetodoLeituraCodigoBarras()));
                    writer.newLine();
                    written++;
                }

                if (!result.hasNext()) {
                    break;
                }
                page++;
            }
            writer.flush();
        }
    }

    private static String normalizeQuery(String q) {
        if (q == null) {
            return null;
        }
        String trimmed = q.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private static String csvField(Object value) {
        if (value == null) {
            return "";
        }
        String raw = String.valueOf(value);
        String escaped = raw.replace("\"", "\"\"");
        if (escaped.contains(";") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    public record CsvItem(
            Long legacyId,
            String codigoBarras,
            String nome,
            String fabricante,
            Integer estoque,
            BigDecimal precoTabela,
            BigDecimal precoVenda
    ) {
        static CsvItem from(EstoqueFisicoCsvService.EstoqueItem item) {
            return new CsvItem(
                    item.legacyId(),
                    item.codigoBarras(),
                    item.nome(),
                    item.fabricante(),
                    item.estoque(),
                    item.precoTabela(),
                    item.precoVenda()
            );
        }
    }

    public record CsvPage(
            List<CsvItem> items,
            int page,
            int size,
            int total,
            boolean hasNext
    ) {
    }

    public record NaoDisponivelItem(
            Long id,
            Long legacyId,
            String nome,
            String categoria,
            String codigoBarras,
            Integer estoque,
            BigDecimal precoVenda,
            Boolean disponivel,
            String status,
            String metodoLeituraCodigoBarras
    ) {
        static NaoDisponivelItem from(ProdutoEntity p) {
            return new NaoDisponivelItem(
                    p.getId(),
                    p.getLegacyId(),
                    p.getNome(),
                    p.getCategoria(),
                    p.getCodigoBarras(),
                    p.getEstoque(),
                    p.getPrecoVenda(),
                    p.getDisponivel(),
                    p.getStatus() == null ? null : p.getStatus().name(),
                    p.getMetodoLeituraCodigoBarras() == null ? null : p.getMetodoLeituraCodigoBarras().name()
            );
        }
    }

    public record NaoDisponiveisPage(
            List<NaoDisponivelItem> items,
            int page,
            int size,
            int total,
            boolean hasNext
    ) {
    }
}
