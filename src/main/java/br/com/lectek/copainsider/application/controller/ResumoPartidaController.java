package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.application.copa.Copa2026DataService;
import br.com.lectek.copainsider.application.copa.PartidaVM;
import br.com.lectek.copainsider.application.service.ApiFootballService;
import br.com.lectek.copainsider.application.service.NotasJogadorService;
import br.com.lectek.copainsider.application.copa.vm.NotaJogadorVM;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Comparator;
import java.util.List;

@Controller
public class ResumoPartidaController {

    private static final int TOP_JOGADORES = 5;

    private final Copa2026DataService copaData;
    private final NotasJogadorService notasService;
    private final ApiFootballService  apiFootball;

    public ResumoPartidaController(Copa2026DataService copaData,
                                   NotasJogadorService notasService,
                                   ApiFootballService apiFootball) {
        this.copaData     = copaData;
        this.notasService = notasService;
        this.apiFootball  = apiFootball;
    }

    @GetMapping("/jogo/{id}/resumo")
    public String resumo(@PathVariable Long id, HttpSession session, Model model) {
        return copaData.findPartida(id).map(partida -> {

            model.addAttribute("partida", partida);
            model.addAttribute("selecaoCasa",
                    copaData.findSelecao(partida.slugCasa()).orElse(null));
            model.addAttribute("selecaoVisitante",
                    copaData.findSelecao(partida.slugVisitante()).orElse(null));

            // Dados oficiais da API-Football (golos, cartões, estatísticas)
            apiFootball.findMatchData(partida)
                    .ifPresent(md -> model.addAttribute("matchData", md));

            // Top jogadores avaliados pelos adeptos
            List<NotaJogadorVM> topCasa = topJogadores(id, partida.slugCasa(), session.getId());
            List<NotaJogadorVM> topVisitante = topJogadores(id, partida.slugVisitante(), session.getId());
            model.addAttribute("topCasa",      topCasa);
            model.addAttribute("topVisitante", topVisitante);

            // Contexto histórico H2H
            copaData.comparar(partida.slugCasa(), partida.slugVisitante())
                    .ifPresent(h2h -> model.addAttribute("h2h", h2h));

            // Outras partidas da fase
            List<PartidaVM> maisPartidas = copaData.listPartidas().stream()
                    .filter(p -> !p.id().equals(id) && p.encerrada())
                    .sorted(Comparator.comparing(PartidaVM::dataHora).reversed())
                    .limit(4)
                    .toList();
            model.addAttribute("maisPartidas", maisPartidas);

            return "pages/site/resumo-partida";
        }).orElse("redirect:/calendario");
    }

    private List<NotaJogadorVM> topJogadores(Long jogoId, String selecaoSlug, String sessionId) {
        return notasService.estado(jogoId, selecaoSlug, sessionId).stream()
                .filter(NotaJogadorVM::votoMinimo)
                .sorted(Comparator.comparingDouble(NotaJogadorVM::media).reversed())
                .limit(TOP_JOGADORES)
                .toList();
    }
}
