package br.com.redemaisfarma.application.mapper;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.application.dto.response.ProdutoResponseDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ProdutoRestMapperTest {

    @Test
    void toResponseNaoQuebraQuandoEstoqueNulo() {
        ProdutoEntity entity = new ProdutoEntity();
        entity.setId(1L);
        entity.setNome("Produto teste");
        entity.setPrecoVenda(BigDecimal.valueOf(15.0));
        entity.setDisponivel(true);
        entity.setEstoque(null);

        ProdutoResponseDTO dto = ProdutoRestMapper.toResponse(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.getEntityId()).isEqualTo(1L);
        assertThat(dto.getSituacao()).isEqualTo(ProdutoResponseDTO.SituacaoProduto.ESGOTADO);
    }

    @Test
    void toResponseReplicaImagemNaAliasImagemUrl() {
        ProdutoEntity entity = new ProdutoEntity();
        entity.setId(2L);
        entity.setNome("Produto com imagem");
        entity.setPrecoVenda(BigDecimal.valueOf(20.0));
        entity.setDisponivel(true);
        entity.setEstoque(5);
        entity.setImagem("produto-2.png");

        ProdutoResponseDTO dto = ProdutoRestMapper.toResponse(entity);

        assertThat(dto.getImagem()).isEqualTo("/media/products/produto-2.png");
        assertThat(dto.getImagemUrl()).isEqualTo("/media/products/produto-2.png");
    }
}
