package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.application.copa.Copa2026DataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PartidaController {

    private final Copa2026DataService copaData;

    public PartidaController(Copa2026DataService copaData) {
        this.copaData = copaData;
    }

    @GetMapping("/partida/{id}")
    public String partida(@PathVariable Long id, Model model) {
        return copaData.findPartida(id)
                .map(partida -> {
                    model.addAttribute("partida", partida);
                    model.addAttribute("selecaoCasa",
                            copaData.findSelecao(partida.slugCasa()).orElse(null));
                    model.addAttribute("selecaoVisitante",
                            copaData.findSelecao(partida.slugVisitante()).orElse(null));
                    return "pages/site/partida";
                })
                .orElse("redirect:/calendario");
    }
}
