package br.com.lectek.copainsider.adapters.outbound.persistence.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProdutoEntityTest {

    @Test
    void getExigeReceitaForcaReceitaQuandoTarjaExigir() {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setTarjaMedicacao(TarjaMedicacao.TARJA_VERMELHA);
        produto.setExigeReceita(Boolean.FALSE);

        assertThat(produto.getExigeReceita()).isTrue();
    }

    @Test
    void onUpdateNormalizaExigeReceitaQuandoTarjaForSemTarja() {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setTarjaMedicacao(TarjaMedicacao.SEM_TARJA);
        produto.setExigeReceita(Boolean.TRUE);

        produto.onUpdate();

        assertThat(produto.getExigeReceita()).isFalse();
    }

    @Test
    void onCreatePreservaExigenciaManualDaTarjaAmarela() {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setTarjaMedicacao(TarjaMedicacao.TARJA_AMARELA);
        produto.setExigeReceita(Boolean.TRUE);

        produto.onCreate();

        assertThat(produto.getExigeReceita()).isTrue();
    }

    @Test
    void getCodigoBarrasUsaCodigoOriginalQuandoCampoPrincipalVierVazio() {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setCodigoBarras("   ");
        produto.setCodigoOriginal(78912345678901L);

        assertThat(produto.getCodigoBarras()).isEqualTo("78912345678901");
    }

    @Test
    void getCodigoBarrasMantemCodigoPrincipalValidoQuandoExistir() {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setCodigoBarras("7891234567895");
        produto.setCodigoOriginal(12345678L);

        assertThat(produto.getCodigoBarras()).isEqualTo("7891234567895");
    }

    @Test
    void getCodigoBarrasRecuperaZeroEsquerdaQuandoCodigoOriginalPerdeuDigitoAoVirarLong() {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setCodigoBarras("   ");
        produto.setCodigoOriginal(12345678901L);

        assertThat(produto.getCodigoBarras()).isEqualTo("012345678901");
    }

    @Test
    void getImagemResolveNomeSimplesParaMediaProducts() {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setImagem("dipirona-500mg.jpg");

        assertThat(produto.getImagem())
                .isEqualTo("/media/products/dipirona-500mg.jpg");
    }

    @Test
    void getImagemNormalizaSeparadoresWindowsSemDescartarImagem() {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setImagem("media\\products\\dipirona-500mg.jpg");

        assertThat(produto.getImagem())
                .isEqualTo("/media/products/dipirona-500mg.jpg");
    }

    @Test
    void setImagensProdutoMantemPrimeiraComoPrincipalEDeduplicaLista() {
        ProdutoEntity produto = new ProdutoEntity();

        produto.setImagensProduto(java.util.List.of(
                "/media/products/a.jpg",
                "/media/products/b.jpg",
                "/media/products/a.jpg"
        ));

        assertThat(produto.getImagem()).isEqualTo("/media/products/a.jpg");
        assertThat(produto.getImagensProduto()).containsExactly(
                "/media/products/a.jpg",
                "/media/products/b.jpg"
        );
    }

    @Test
    void definirImagemPrincipalReordenaSemPerderGaleria() {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setImagensProduto(java.util.List.of(
                "/media/products/a.jpg",
                "/media/products/b.jpg",
                "/media/products/c.jpg"
        ));

        produto.definirImagemPrincipal("/media/products/c.jpg");

        assertThat(produto.getImagem()).isEqualTo("/media/products/c.jpg");
        assertThat(produto.getImagensProduto()).containsExactly(
                "/media/products/c.jpg",
                "/media/products/a.jpg",
                "/media/products/b.jpg"
        );
    }

    @Test
    void onCreateNormalizaCamposTextuaisEUsaDescricaoQuandoNomeForPlaceholder() {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setNome("  Produto do estoque fisico  ");
        produto.setDescricao("  Dipirona 500mg 20 comprimidos  ");
        produto.setCategoria("  Medicamentos   Gerais  ");
        produto.setFabricante("  null  ");
        produto.setStatusSync("  SINCRONIZADO   ");

        produto.onCreate();

        assertThat(produto.getNome()).isEqualTo("Dipirona 500mg 20 comprimidos");
        assertThat(produto.getDescricao()).isEqualTo("Dipirona 500mg 20 comprimidos");
        assertThat(produto.getCategoria()).isEqualTo("Medicamentos Gerais");
        assertThat(produto.getFabricante()).isNull();
        assertThat(produto.getStatusSync()).isEqualTo("SINCRONIZADO");
    }

    @Test
    void onUpdateNormalizaAliasesDeStatusSyncParaValoresAceitosPelaConstraint() {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setStatusSync("  synced  ");

        produto.onUpdate();

        assertThat(produto.getStatusSync()).isEqualTo("SINCRONIZADO");

        produto.setStatusSync("AGUARDANDO_CODIGO_BARRAS");
        produto.onUpdate();

        assertThat(produto.getStatusSync()).isEqualTo("PENDENTE");
    }

    @Test
    void onUpdatePreencheDescricaoComNomeQuandoDescricaoVierVazia() {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setNome("  Vitamina C   1g ");
        produto.setDescricao("   ");

        produto.onUpdate();

        assertThat(produto.getNome()).isEqualTo("Vitamina C 1g");
        assertThat(produto.getDescricao()).isEqualTo("Vitamina C 1g");
    }
}
