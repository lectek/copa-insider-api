package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.application.copa.Copa2026DataService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RankingController {

    private final Copa2026DataService copaData;

    public RankingController(Copa2026DataService copaData) {
        this.copaData = copaData;
    }

    @GetMapping("/ranking")
    public String ranking(
            @RequestParam(defaultValue = "gols") String por,
            Model model) {

        model.addAttribute("jogadores",
                "assistencias".equals(por)
                        ? copaData.topAssistentes(20)
                        : copaData.topGoleadores(20));
        model.addAttribute("por", por);
        return "pages/site/ranking";
    }
}
