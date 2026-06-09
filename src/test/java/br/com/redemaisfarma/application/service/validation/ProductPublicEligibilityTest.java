package br.com.redemaisfarma.application.service.validation;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.MetodoLeituraCodigoBarras;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ProductPublicEligibilityTest {

    @Test
    void isPubliclySellableRetornaTrueQuandoProdutoPublicoEstaVendavel() {
        ProdutoEntity produto = produtoBase();
        produto.setEstoque(5);

        assertThat(ProductPublicEligibility.isPubliclySellable(produto)).isTrue();
    }

    @Test
    void isPubliclySellableRetornaFalseQuandoStatusNaoEhPublicado() {
        ProdutoEntity produto = produtoBase();
        produto.setEstoque(5);
        produto.setStatus(ProdutoStatus.IMPORTADO);

        assertThat(ProductPublicEligibility.isPubliclySellable(produto)).isFalse();
    }

    @Test
    void isPubliclySellableRetornaFalseQuandoOrigemEhCatalogoNacional() {
        ProdutoEntity produto = produtoBase();
        produto.setEstoque(5);
        produto.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.LEGADO);

        assertThat(ProductPublicEligibility.isPubliclySellable(produto)).isFalse();
    }

    @Test
    void isStockSubscribableAceitaProdutoPublicadoMesmoSemEstoque() {
        ProdutoEntity produto = produtoBase();
        produto.setEstoque(0);

        assertThat(ProductPublicEligibility.isStockSubscribable(produto)).isTrue();
    }

    @Test
    void isStockSubscribableRetornaFalseQuandoJanelaPublicacaoFechada() {
        ProdutoEntity produto = produtoBase();
        produto.setDespublicadoEm(LocalDateTime.now().minusMinutes(1));

        assertThat(ProductPublicEligibility.isStockSubscribable(produto)).isFalse();
    }

    @Test
    void isStockSubscribableRetornaFalseQuandoOrigemNaoEhLocalVendavel() {
        ProdutoEntity produto = produtoBase();
        produto.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.LEGADO);

        assertThat(ProductPublicEligibility.isStockSubscribable(produto)).isFalse();
    }

    @Test
    void isPubliclySellableAceitaCodigoOriginalComZeroEsquerdaRecuperado() {
        ProdutoEntity produto = produtoBase();
        produto.setCodigoBarras(" ");
        produto.setCodigoOriginal(12345678901L);
        produto.setEstoque(5);

        assertThat(ProductPublicEligibility.isPubliclySellable(produto)).isTrue();
    }

    private static ProdutoEntity produtoBase() {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setDisponivel(true);
        produto.setPrecoVenda(BigDecimal.valueOf(12.5));
        produto.setCodigoBarras("7891234567890");
        produto.setStatus(ProdutoStatus.PUBLICADO);
        produto.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA);
        produto.setPublicadoEm(LocalDateTime.now().minusMinutes(5));
        produto.setDespublicadoEm(LocalDateTime.now().plusMinutes(5));
        return produto;
    }
}
