package br.com.lectek.copainsider.adapters.inbound.web.controller;

import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SuportePageController {

    /**
     * Renders the customer support page.
     *
     * @return support view
     */
    @GetMapping(value = {"/suporte", "/alysson"})
    public String paginaSuporte(final Model model) {
        model.addAttribute("assistantName", "Alysson");
        model.addAttribute("assistantTitle", "Atendente virtual da SaudeMaisFarma");
        model.addAttribute("assistantGreeting",
                "Posso ajudar com produtos, entrega, pagamento, pedidos e orientacoes gerais da loja.");
        model.addAttribute("assistantExamples", List.of(
                "Vocês entregam no meu bairro?",
                "Como acompanhar meu pedido?",
                "Quais formas de pagamento estão disponíveis?",
                "Me explique como comprar um medicamento com receita."
        ));
        return "pages/cliente/alysson";
    }
}
