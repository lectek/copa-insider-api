package br.com.lectek.copainsider.adapters.inbound.web;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProdutoCardDTOTest {

    @Test
    void fromUsaPlaceholderGenericoQuandoProdutoNaoTemImagem() {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(22L);
        produto.setNome("Produto sem foto");
        produto.setPrecoVenda(BigDecimal.valueOf(18.50));
        produto.setDisponivel(true);
        produto.setEstoque(7);
        produto.setCategoria("GERAL");

        ProdutoCardDTO dto = ProdutoCardDTO.from(produto);

        assertThat(dto.imagem()).isEqualTo("/img/produtos/placeholder-generico.png");
    }
}
