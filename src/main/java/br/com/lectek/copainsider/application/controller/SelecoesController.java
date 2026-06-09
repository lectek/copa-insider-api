package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.application.copa.Copa2026DataService;
import br.com.lectek.copainsider.application.copa.SelecaoVM;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@Controller
public class SelecoesController {

    private final Copa2026DataService copaData;

    public SelecoesController(Copa2026DataService copaData) {
        this.copaData = copaData;
    }

    @GetMapping("/selecoes")
    public String selecoes(Model model) {
        Map<String, List<SelecaoVM>> porGrupo = copaData.selecoesPorGrupo();
        model.addAttribute("porGrupo", porGrupo);
        model.addAttribute("total", copaData.listSelecoes().size());
        return "pages/site/selecoes";
    }

    @GetMapping("/selecoes/{slug}")
    public String detalhe(@PathVariable String slug, Model model) {
        return copaData.findSelecao(slug)
                .map(selecao -> {
                    model.addAttribute("selecao", selecao);
                    model.addAttribute("partidas", copaData.partidasDaSelecao(slug));
                    model.addAttribute("grupoSelecoes",
                            copaData.selecoesPorGrupo().get(selecao.grupo()));
                    return "pages/site/selecao-detalhe";
                })
                .orElse("redirect:/selecoes");
    }
}
