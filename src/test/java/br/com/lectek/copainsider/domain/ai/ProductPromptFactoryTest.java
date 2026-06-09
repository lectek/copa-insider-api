package br.com.lectek.copainsider.domain.ai;

import br.com.lectek.copainsider.application.port.outbound.ProdutoRepositoryPort;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductPromptFactoryTest {

    private final ProductPromptFactory factory = new ProductPromptFactory();

    @Test
    void shouldIncludeCollectorSpecificPhysicalHint() {
        final ProdutoRepositoryPort.ProdutoDTO produto = new ProdutoRepositoryPort.ProdutoDTO(
                1L,
                "COLETOR CRISTAL TAMPA VERMELHA 80ML",
                "COLETOR CRISTAL TAMPAVERMELHA 80ML 100-3BI",
                "Descartaveis",
                "0609963718467",
                "PBMED DISTRIBUIDORA LTDA",
                null
        );

        final String prompt = factory.promptForProduto(produto);

        assertThat(prompt)
                .contains("Nome oficial: COLETOR CRISTAL TAMPA VERMELHA 80ML.")
                .contains("Fabricante: PBMED DISTRIBUIDORA LTDA.")
                .contains("Coletor universal para exames ou laboratorio")
                .contains("tampa rosqueavel na cor vermelha")
                .contains("80 ml")
                .contains("Nao transformar em copo domestico");

        assertThat(factory.varsFromProduto(produto))
                .containsEntry("nome", "COLETOR CRISTAL TAMPA VERMELHA 80ML")
                .containsEntry("fabricante", "PBMED DISTRIBUIDORA LTDA");
        assertThat(String.valueOf(factory.varsFromProduto(produto).get("tipoFisico")))
                .contains("Coletor universal para exames ou laboratorio")
                .contains("80 ml");
    }
}
