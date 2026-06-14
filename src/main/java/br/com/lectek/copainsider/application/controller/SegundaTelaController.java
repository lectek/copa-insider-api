package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.lectek.copainsider.application.copa.Copa2026DataService;
import br.com.lectek.copainsider.application.service.SalaJogoService;
import br.com.lectek.copainsider.application.service.NotasJogadorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class SegundaTelaController {

    private final Copa2026DataService copaData;
    private final SalaJogoService     chatService;
    private final NotasJogadorService notasService;
    private final UsuarioRepository   usuarioRepo;

    public SegundaTelaController(Copa2026DataService copaData,
                                 SalaJogoService chatService,
                                 NotasJogadorService notasService,
                                 UsuarioRepository usuarioRepo) {
        this.copaData     = copaData;
        this.chatService  = chatService;
        this.notasService = notasService;
        this.usuarioRepo  = usuarioRepo;
    }

    @GetMapping("/segunda-tela/{id}")
    public String segundaTela(@PathVariable Long id,
                              @RequestParam(defaultValue = "chat") String aba,
                              Authentication auth,
                              HttpSession session, Model model) {
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getName())) {
            return "redirect:/auth/login?redirect=/segunda-tela/" + id;
        }
        return copaData.findPartida(id).map(partida -> {
            model.addAttribute("partida",         partida);
            model.addAttribute("abaAtiva",        aba);
            model.addAttribute("historico",       chatService.historico(id));
            model.addAttribute("notasCasa",
                    notasService.estado(id, partida.slugCasa(), session.getId()));
            model.addAttribute("notasVisitante",
                    notasService.estado(id, partida.slugVisitante(), session.getId()));
            model.addAttribute("sessionId",       session.getId());
            model.addAttribute("nomeUtilizador",  nomeParaChat(auth.getName()));
            return "pages/site/segunda-tela";
        }).orElse("redirect:/calendario");
    }

    private String nomeParaChat(String email) {
        return usuarioRepo.findByEmailIgnoreCase(email)
                .map(u -> u.getNome() != null && !u.getNome().isBlank()
                        ? u.getNome().split(" ")[0]
                        : email.split("@")[0])
                .orElseGet(() -> email.split("@")[0]);
    }
}
