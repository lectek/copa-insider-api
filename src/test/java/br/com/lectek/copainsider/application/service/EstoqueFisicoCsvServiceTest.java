package br.com.lectek.copainsider.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EstoqueFisicoCsvServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void searchShouldPreserveBlankColumnsAndParseDecimalStockAsUnits() throws Exception {
        Path csv = tempDir.resolve("estoque.csv");
        Files.writeString(
                csv,
                String.join("\n",
                        "COD BARRAS;CODIGO;PRODUTO;FABRICANTE;UN;LOCALIZA;ESTOQUE;PR.TABELA;PR.VENDA",
                        "7891234567890;123;Dipirona 500mg;;UN;A1;30,000;12,34;10,99"
                ),
                StandardCharsets.ISO_8859_1
        );

        EstoqueFisicoCsvService service = new EstoqueFisicoCsvService(csv.toString());

        List<EstoqueFisicoCsvService.EstoqueItem> items = service.search("");

        assertThat(items).hasSize(1);
        EstoqueFisicoCsvService.EstoqueItem item = items.getFirst();
        assertThat(item.codigoBarras()).isEqualTo("7891234567890");
        assertThat(item.legacyId()).isEqualTo(123L);
        assertThat(item.nome()).isEqualTo("Dipirona 500mg");
        assertThat(item.fabricante()).isEmpty();
        assertThat(item.estoque()).isEqualTo(30);
    }

    @Test
    void searchShouldResolveCsvFileInsideConfiguredDirectory() throws Exception {
        Path csvDir = tempDir.resolve("Estoque Fisico.csv");
        Files.createDirectories(csvDir);
        Path csv = csvDir.resolve("dados.csv");
        Files.writeString(
                csv,
                String.join("\n",
                        "COD BARRAS;CODIGO;PRODUTO;FABRICANTE;UN;LOCALIZA;ESTOQUE;PR.TABELA;PR.VENDA",
                        "7890000000001;456;Vitamina C;Marca X;UN;B2;8;15,50;13,90"
                ),
                StandardCharsets.ISO_8859_1
        );

        EstoqueFisicoCsvService service = new EstoqueFisicoCsvService(csvDir.toString());

        List<EstoqueFisicoCsvService.EstoqueItem> items = service.search("vitamina");

        assertThat(items).hasSize(1);
        assertThat(items.getFirst().estoque()).isEqualTo(8);
        assertThat(items.getFirst().nome()).isEqualTo("Vitamina C");
    }

    @Test
    void parseUploadedCsvShouldReadStreamWithoutFilesystemDependency() throws Exception {
        byte[] csvBytes = String.join("\n",
                        "COD BARRAS;CODIGO;PRODUTO;FABRICANTE;UN;LOCALIZA;ESTOQUE;PR.TABELA;PR.VENDA",
                        "7890000000002;457;Fralda Infantil;Marca Y;UN;C3;12;45,90;39,90"
                )
                .getBytes(StandardCharsets.ISO_8859_1);

        EstoqueFisicoCsvService service = new EstoqueFisicoCsvService("Estoque Fisico.csv");

        List<EstoqueFisicoCsvService.EstoqueItem> items = service.parseUploadedCsv(new ByteArrayInputStream(csvBytes));

        assertThat(items).hasSize(1);
        assertThat(items.getFirst().codigoBarras()).isEqualTo("7890000000002");
        assertThat(items.getFirst().nome()).isEqualTo("Fralda Infantil");
        assertThat(items.getFirst().estoque()).isEqualTo(12);
    }
}
