package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.CopaPalpiteEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.CopaPalpiteJPARepository;
import br.com.lectek.copainsider.application.copa.Copa2026DataService;
import br.com.lectek.copainsider.application.copa.PartidaVM;
import br.com.lectek.copainsider.application.copa.vm.PalpiteRankingVM;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class PalpiteController {

    private final Copa2026DataService    copaData;
    private final CopaPalpiteJPARepository palpiteRepo;

    public PalpiteController(Copa2026DataService copaData, CopaPalpiteJPARepository palpiteRepo) {
        this.copaData    = copaData;
        this.palpiteRepo = palpiteRepo;
    }

    @GetMapping("/palpites")
    public String ranking(Model model) {
        Map<String, List<CopaPalpiteEntity>> porEmail = palpiteRepo.findAll().stream()
                .collect(Collectors.groupingBy(p -> p.getEmail().toLowerCase()));

        List<PalpiteRankingVM> ranking = new ArrayList<>();
        for (Map.Entry<String, List<CopaPalpiteEntity>> entry : porEmail.entrySet()) {
            int pontos = 0, exatos = 0, resultados = 0, total = 0;
            for (CopaPalpiteEntity p : entry.getValue()) {
                total++;
                PartidaVM partida = copaData.findPartida(p.getPartidaId()).orElse(null);
                if (partida == null || !partida.encerrada() || !partida.temResultado()) continue;
                int pts = calcularPontos(p, partida);
                pontos += pts;
                if (pts == 3) exatos++;
                else if (pts == 1) resultados++;
            }
            ranking.add(new PalpiteRankingVM(mascaraEmail(entry.getKey()), total, pontos, exatos, resultados));
        }

        ranking.sort(Comparator.comparingInt((PalpiteRankingVM v) -> -v.pontos())
                .thenComparingInt(v -> -v.acertosExatos()));

        model.addAttribute("ranking", ranking);
        model.addAttribute("totalParticipantes", ranking.size());
        return "pages/site/palpites";
    }

    @PostMapping("/palpites")
    public String submeter(@RequestParam Long partidaId,
                           @RequestParam int golsCasa,
                           @RequestParam int golsVisitante,
                           @AuthenticationPrincipal UserDetails user,
                           RedirectAttributes ra) {
        if (user == null) {
            return "redirect:/auth/login?redirect=/partida/" + partidaId;
        }

        PartidaVM partida = copaData.findPartida(partidaId).orElse(null);
        if (partida == null || !partida.agendada()) {
            ra.addFlashAttribute("palpiteErro", "Prazo encerrado — o jogo já começou.");
            return "redirect:/partida/" + partidaId;
        }

        String email = user.getUsername().toLowerCase();
        CopaPalpiteEntity palpite = palpiteRepo
                .findByEmailIgnoreCaseAndPartidaId(email, partidaId)
                .orElse(new CopaPalpiteEntity(email, partidaId));
        palpite.setGolsCasa(golsCasa);
        palpite.setGolsVisitante(golsVisitante);
        palpiteRepo.save(palpite);

        ra.addFlashAttribute("palpiteSalvo", true);
        return "redirect:/partida/" + partidaId;
    }

    static int calcularPontos(CopaPalpiteEntity p, PartidaVM partida) {
        if (p.getGolsCasa() == partida.golsCasa() && p.getGolsVisitante() == partida.golsVisitante()) {
            return 3;
        }
        int signoReal    = Integer.signum(partida.golsCasa()  - partida.golsVisitante());
        int signoPalpite = Integer.signum(p.getGolsCasa() - p.getGolsVisitante());
        return signoReal == signoPalpite ? 1 : 0;
    }

    private static String mascaraEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 2) return email;
        return email.substring(0, 2) + "***" + email.substring(at);
    }
}
