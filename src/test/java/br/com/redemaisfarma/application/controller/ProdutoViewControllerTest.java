package br.com.redemaisfarma.application.controller;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.assertj.core.api.Assertions.assertThat;

class ProdutoViewControllerTest {

    private final ProdutoViewController controller = new ProdutoViewController();

    @Test
    void redirecionarCatalogoNormalizaParametrosLegados() {
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.redirecionarCatalogo(
                "  dipirona ",
                null,
                " Analgesicos ",
                null,
                -3,
                null,
                200,
                redirect
        );

        assertThat(view).isEqualTo("redirect:/produtos");
        assertThat(redirect)
                .containsEntry("q", "dipirona")
                .containsEntry("cat", "Analgesicos")
                .containsEntry("page", "0")
                .containsEntry("size", "100");
    }

    @Test
    void redirecionarCatalogoAplicaPadroesQuandoParametrosAusentes() {
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.redirecionarCatalogo(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                redirect
        );

        assertThat(view).isEqualTo("redirect:/produtos");
        assertThat(redirect)
                .containsEntry("q", "")
                .containsEntry("page", "0")
                .containsEntry("size", "18")
                .doesNotContainKey("cat");
    }
}
