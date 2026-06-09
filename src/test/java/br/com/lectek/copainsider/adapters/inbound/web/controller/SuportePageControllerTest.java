package br.com.lectek.copainsider.adapters.inbound.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

class SuportePageControllerTest {

    @Test
    void paginaSuporteExposeAlyssonConfig() {
        final SuportePageController controller = new SuportePageController();
        final Model model = new ExtendedModelMap();

        final String view = controller.paginaSuporte(model);

        assertThat(view).isEqualTo("pages/cliente/alysson");
        assertThat(model.getAttribute("assistantName")).isEqualTo("Alysson");
        assertThat(model.getAttribute("assistantTitle"))
                .isEqualTo("Atendente virtual da SaudeMaisFarma");
        assertThat(model.getAttribute("assistantGreeting"))
                .isEqualTo("Posso ajudar com produtos, entrega, pagamento, pedidos e orientacoes gerais da loja.");
        assertThat(model.getAttribute("assistantExamples"))
                .isEqualTo(List.of(
                        "Vocês entregam no meu bairro?",
                        "Como acompanhar meu pedido?",
                        "Quais formas de pagamento estão disponíveis?",
                        "Me explique como comprar um medicamento com receita."
                ));
    }
}
