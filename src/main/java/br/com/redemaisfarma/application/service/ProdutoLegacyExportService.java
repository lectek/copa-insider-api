package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.legacy.entity.ProdutoLegacyEntity;
import br.com.redemaisfarma.adapters.outbound.legacy.repository.ProdutoLegacyRepository;
import lombok.Generated;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

@Service
@ConditionalOnProperty(prefix = "app.sync.legacy", name = {"enabled"}, havingValue = "true")
public class ProdutoLegacyExportService {

    private final ProdutoLegacyRepository repository;

    /**
     * Exporta todos os produtos legados em CSV (delimitador ';'), com BOM UTF-8.
     */
    @Transactional(readOnly = true)
    public void writeCsv(OutputStream out) throws IOException {
        // BOM UTF-8
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

        try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            // Cabeçalho
            w.write(String.join(";",
                    "ID", "NOME", "CODIGOBARRAS", "SALDO", "PRECOVENDA", "PRECOPROMOCAO",
                    "ESTOQUEMINIMO", "MARGEMLUCRO", "INICIOPROMOCAO", "TERMINOPROMOCAO",
                    "BONUS", "APRESENTACAO", "PRECOANTERIOR", "FORNECEDOR_ID",
                    "CATEGORIA_ID", "COMISSAO_ID"));
            w.write('\n');

            // Corpo
            try (Stream<ProdutoLegacyEntity> stream = repository.streamAll()) {
                stream.forEach(p -> {
                    try {
                        w.write(String.join(";",
                                csv(p.getId()),
                                csv(p.getNome()),
                                csv(p.getCodigoBarras()),
                                csv(p.getSaldo()),
                                csv(p.getPrecoVenda()),
                                csv(p.getPrecoPromocao()),
                                csv(p.getEstoqueMinimo()),
                                csv(p.getMargemLucro()),
                                csv(p.getInicioPromocao()),
                                csv(p.getTerminoPromocao()),
                                csv(p.getBonus()),
                                csv(p.getApresentacao()),
                                csv(p.getPrecoAnterior()),
                                csv(p.getFornecedorId()),
                                csv(p.getCategoriaId()),
                                csv(p.getComissaoId())
                        ));
                        w.write('\n');
                    } catch (IOException e) {
                        // Converter para unchecked para não quebrar o forEach (tratado fora)
                        throw new UncheckedIOException(e);
                    }
                });
            } catch (UncheckedIOException e) {
                // Rejoga como IOException para a assinatura do método
                throw e.getCause();
            }

            w.flush();
        }
    }

    /** Escapa para CSV: envolve em aspas, duplica aspas internas e remove quebras de linha. */
    private static String csv(Object v) {
        if (v == null) return "\"\"";
        String s = String.valueOf(v);
        s = s.replace("\r", " ").replace("\n", " ");
        s = s.replace("\"", "\"\"");
        return "\"" + s + "\"";
    }

    @Generated
    public ProdutoLegacyExportService(ProdutoLegacyRepository repository) {
        this.repository = repository;
    }
}
