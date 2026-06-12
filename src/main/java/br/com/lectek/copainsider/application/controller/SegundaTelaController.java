package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.application.copa.Copa2026DataService;
import br.com.lectek.copainsider.application.service.SalaJogoService;
import br.com.lectek.copainsider.application.service.NotasJogadorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class SegundaTelaController {

    private final Copa2026DataService copaData;
    private final SalaJogoService chatService;
    private final NotasJogadorService notasService;

    public SegundaTelaController(Copa2026DataService copaData,
                                 SalaJogoService chatService,
                                 NotasJogadorService notasService) {
        this.copaData     = copaData;
        this.chatService  = chatService;
        this.notasService = notasService;
    }

    @GetMapping("/segunda-tela/{id}")
    public String segundaTela(@PathVariable Long id,
                              @RequestParam(defaultValue = "chat") String aba,
                              HttpSession session, Model model) {
        return copaData.findPartida(id).map(partida -> {
            model.addAttribute("partida",  partida);
            model.addAttribute("abaAtiva", aba);
            model.addAttribute("historico", chatService.historico(id));
            model.addAttribute("notasCasa",
                    notasService.estado(id, partida.slugCasa(), session.getId()));
            model.addAttribute("notasVisitante",
                    notasService.estado(id, partida.slugVisitante(), session.getId()));
            model.addAttribute("sessionId", session.getId());
            return "pages/site/segunda-tela";
        }).orElse("redirect:/calendario");
    }
}
