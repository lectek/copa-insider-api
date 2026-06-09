package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.application.copa.Copa2026DataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RivalidadesController {

    private final Copa2026DataService copaData;

    public RivalidadesController(Copa2026DataService copaData) {
        this.copaData = copaData;
    }

    @GetMapping("/rivalidades")
    public String rivalidades(Model model) {
        model.addAttribute("rivalidades", copaData.listRivalidades());
        return "pages/site/rivalidades";
    }
}
