package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.application.copa.Copa2026DataService;
import br.com.lectek.copainsider.application.copa.PartidaVM;
import br.com.lectek.copainsider.application.copa.SelecaoVM;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class CalendarioController {

    private final Copa2026DataService copaData;

    public CalendarioController(Copa2026DataService copaData) {
        this.copaData = copaData;
    }

    @GetMapping("/calendario")
    public String calendario(
            @RequestParam(required = false) String grupo,
            Model model) {

        Map<String, List<PartidaVM>> partidasPorGrupo = copaData.partidasPorGrupo();
        Map<String, List<SelecaoVM>> selecoesPorGrupo = copaData.selecoesPorGrupo();
        List<PartidaVM> aoVivo = copaData.partidasAoVivo();
        List<PartidaVM> proximas = copaData.proximasPartidas(6);

        List<PartidaVM> partidasFiltradas = (grupo != null && !grupo.isBlank())
                ? partidasPorGrupo.getOrDefault(grupo.toUpperCase(), List.of())
                : copaData.listPartidas();

        model.addAttribute("partidasPorGrupo", partidasPorGrupo);
        model.addAttribute("selecoesPorGrupo", selecoesPorGrupo);
        model.addAttribute("aoVivo", aoVivo);
        model.addAttribute("proximas", proximas);
        model.addAttribute("partidasFiltradas", partidasFiltradas);
        model.addAttribute("grupoSelecionado", grupo != null ? grupo.toUpperCase() : null);
        model.addAttribute("grupos", partidasPorGrupo.keySet());

        return "pages/site/calendario";
    }
}
