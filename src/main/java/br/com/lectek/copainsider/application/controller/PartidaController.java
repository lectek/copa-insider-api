package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.CopaPalpiteJPARepository;
import br.com.lectek.copainsider.application.copa.Copa2026DataService;
import br.com.lectek.copainsider.application.copa.PartidaVM;
import br.com.lectek.copainsider.application.copa.vm.NotaJogadorVM;
import br.com.lectek.copainsider.application.service.NotasJogadorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Comparator;
import java.util.List;

@Controller
public class PartidaController {

    private final Copa2026DataService      copaData;
    private final NotasJogadorService      notasService;
    private final CopaPalpiteJPARepository palpiteRepo;

    public PartidaController(Copa2026DataService copaData,
                              NotasJogadorService notasService,
                              CopaPalpiteJPARepository palpiteRepo) {
        this.copaData     = copaData;
        this.notasService = notasService;
        this.palpiteRepo  = palpiteRepo;
    }

    @GetMapping("/partida/{id}")
    public String partida(@PathVariable Long id,
                          @AuthenticationPrincipal UserDetails user,
                          HttpSession session, Model model) {
        return copaData.findPartida(id)
                .map(partida -> {
                    model.addAttribute("partida",          partida);
                    model.addAttribute("selecaoCasa",      copaData.findSelecao(partida.slugCasa()).orElse(null));
                    model.addAttribute("selecaoVisitante", copaData.findSelecao(partida.slugVisitante()).orElse(null));

                    if (partida.aoVivo()) {
                        // Stats ao vivo da ESPN (golos com minuto, cartões, posse, remates, faltas)
                        copaData.findEspnId(partida.slugCasa(), partida.slugVisitante())
                                .ifPresent(espnId -> {
                                    var ls = copaData.liveStats(espnId, partida.slugCasa());
                                    if (ls != null) model.addAttribute("liveStats", ls);
                                });
                    } else {
                        // Golos via OpenFootball (apenas para jogos encerrados)
                        model.addAttribute("golos", copaData.golosPartida(id));
                    }

                    // H2H histórico
                    copaData.comparar(partida.slugCasa(), partida.slugVisitante())
                            .ifPresent(h2h -> model.addAttribute("h2h", h2h));

                    // Notas dos adeptos
                    model.addAttribute("topCasa",      topJogadores(id, partida.slugCasa(),        session.getId()));
                    model.addAttribute("topVisitante", topJogadores(id, partida.slugVisitante(),   session.getId()));

                    // Outras partidas recentes encerradas
                    List<PartidaVM> maisPartidas = copaData.listPartidas().stream()
                            .filter(p -> !p.id().equals(id) && p.encerrada())
                            .sorted(Comparator.comparing(PartidaVM::dataHora).reversed())
                            .limit(4)
                            .toList();
                    model.addAttribute("maisPartidas", maisPartidas);

                    // Palpite do utilizador autenticado
                    if (user != null) {
                        String email = user.getUsername().toLowerCase();
                        palpiteRepo.findByEmailIgnoreCaseAndPartidaId(email, id)
                                .ifPresent(p -> {
                                    model.addAttribute("meuPalpite", p);
                                    if (partida.encerrada() && partida.temResultado()) {
                                        model.addAttribute("pontosGanhos",
                                                PalpiteController.calcularPontos(p, partida));
                                    }
                                });
                    }

                    return "pages/site/partida";
                })
                .orElse("redirect:/calendario");
    }

    private List<NotaJogadorVM> topJogadores(Long jogoId, String selecaoSlug, String sessionId) {
        return notasService.estado(jogoId, selecaoSlug, sessionId).stream()
                .filter(NotaJogadorVM::votoMinimo)
                .sorted(Comparator.comparingDouble(NotaJogadorVM::media).reversed())
                .limit(5)
                .toList();
    }
}
