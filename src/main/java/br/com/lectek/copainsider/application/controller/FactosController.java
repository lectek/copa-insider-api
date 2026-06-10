package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.application.copa.CopaCuriosidadesService;
import br.com.lectek.copainsider.application.copa.FatoCurioso;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class FactosController {

    private final CopaCuriosidadesService curiosidadesService;

    public FactosController(CopaCuriosidadesService curiosidadesService) {
        this.curiosidadesService = curiosidadesService;
    }

    @GetMapping("/factos")
    public String factos(@RequestParam(defaultValue = "20") int n, Model model) {
        int quantidade = Math.min(Math.max(n, 1), 40);
        List<FatoCurioso> fatos = curiosidadesService.getFatos(quantidade);
        model.addAttribute("fatos", fatos);
        model.addAttribute("categorias", curiosidadesService.getCategorias());
        model.addAttribute("quantidade", quantidade);
        return "pages/site/factos";
    }
}
