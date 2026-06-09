package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.application.copa.Copa2026DataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ComparadorController {

    private final Copa2026DataService copaData;

    public ComparadorController(Copa2026DataService copaData) {
        this.copaData = copaData;
    }

    @GetMapping("/comparar")
    public String comparar(
            @RequestParam(required = false) String time1,
            @RequestParam(required = false) String time2,
            Model model) {

        model.addAttribute("selecoes", copaData.listSelecoes());
        model.addAttribute("time1", time1 != null ? time1 : "");
        model.addAttribute("time2", time2 != null ? time2 : "");

        if (time1 != null && time2 != null && !time1.isBlank() && !time2.isBlank() && !time1.equals(time2)) {
            copaData.comparar(time1, time2)
                    .ifPresent(r -> model.addAttribute("resultado", r));
        }

        return "pages/site/comparar";
    }
}
