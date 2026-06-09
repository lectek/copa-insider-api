package br.com.redemaisfarma.application.view;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProductCardVMTest {

    @Test
    void ofUsaPlaceholderGenericoQuandoProdutoNaoTemImagem() {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(10L);
        produto.setNome("Produto sem foto");
        produto.setPrecoVenda(BigDecimal.valueOf(15.90));
        produto.setDisponivel(true);
        produto.setEstoque(3);
        produto.setCategoria("GERAL");

        ProductCardVM card = ProductCardVM.of(produto);

        assertThat(card.imagem()).isEqualTo("/img/produtos/placeholder-generico.png");
    }
}
