package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.application.copa.Copa2026DataService;
import br.com.lectek.copainsider.application.service.SalaJogoService;
import br.com.lectek.copainsider.application.service.NotasJogadorService;
import br.com.lectek.copainsider.application.service.SalaJogoSseRegistry;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.List;
import java.util.Map;

@Controller
public class SalaJogoController {

    private final Copa2026DataService copaData;
    private final SalaJogoService chatService;
    private final NotasJogadorService notasService;
    private final SalaJogoSseRegistry sse;

    public SalaJogoController(Copa2026DataService copaData,
                              SalaJogoService chatService,
                              NotasJogadorService notasService,
                              SalaJogoSseRegistry sse) {
        this.copaData     = copaData;
        this.chatService  = chatService;
        this.notasService = notasService;
        this.sse          = sse;
    }

    @GetMapping("/jogo/{id}/sala")
    public String sala(@PathVariable Long id, HttpSession session, Model model) {
        return copaData.findPartida(id).map(partida -> {
            model.addAttribute("partida",  partida);
            model.addAttribute("historico", chatService.historico(id));
            model.addAttribute("notasCasa",
                    notasService.estado(id, partida.slugCasa(), session.getId()));
            model.addAttribute("notasVisitante",
                    notasService.estado(id, partida.slugVisitante(), session.getId()));
            model.addAttribute("selecaoCasa",
                    copaData.findSelecao(partida.slugCasa()).orElse(null));
            model.addAttribute("selecaoVisitante",
                    copaData.findSelecao(partida.slugVisitante()).orElse(null));
            model.addAttribute("sessionId", session.getId());
            return "pages/site/sala-do-jogo";
        }).orElse("redirect:/calendario");
    }

    // ── Notas page ──

    @GetMapping("/jogo/{id}/notas")
    public String notas(@PathVariable Long id,
                        @RequestParam(defaultValue = "") String selecao,
                        HttpSession session, Model model) {
        return copaData.findPartida(id).map(partida -> {
            String slug = selecao.isBlank() ? partida.slugCasa() : selecao;
            model.addAttribute("partida",  partida);
            model.addAttribute("selecaoAtiva", slug);
            model.addAttribute("jogadores", notasService.estado(id, slug, session.getId()));
            model.addAttribute("selecaoCasa",
                    copaData.findSelecao(partida.slugCasa()).orElse(null));
            model.addAttribute("selecaoVisitante",
                    copaData.findSelecao(partida.slugVisitante()).orElse(null));
            model.addAttribute("sessionId", session.getId());
            return "pages/site/notas-ao-vivo";
        }).orElse("redirect:/calendario");
    }

    // ── Chat REST ──

    @PostMapping("/api/jogo/{id}/chat")
    @ResponseBody
    public Map<String, Object> enviarMensagem(@PathVariable Long id,
                                              @RequestParam String nome,
                                              @RequestParam String mensagem,
                                              HttpSession session) {
        try {
            var vm = chatService.enviar(id, session.getId(), nome, mensagem);
            return Map.of("ok", true, "id", vm.id());
        } catch (IllegalArgumentException e) {
            return Map.of("ok", false, "erro", e.getMessage());
        }
    }

    @PostMapping("/api/jogo/{id}/chat/reacao")
    @ResponseBody
    public Map<String, Boolean> reagir(@PathVariable Long id, @RequestParam String emoji) {
        List<String> permitidos = List.of("⚽", "🔥", "👏", "😱", "💛", "❤️", "💪", "🎉");
        if (!permitidos.contains(emoji)) return Map.of("ok", false);
        chatService.reagir(id, emoji);
        return Map.of("ok", true);
    }

    @GetMapping("/api/jogo/{id}/chat/stream")
    public SseEmitter chatStream(@PathVariable Long id) {
        return sse.subscribeChatEmitter(id);
    }

    // ── Notas REST ──

    @PostMapping("/api/jogo/{id}/notas/{selecaoSlug}/{jogadorSlug}")
    @ResponseBody
    public Map<String, Object> votar(@PathVariable Long id,
                                     @PathVariable String selecaoSlug,
                                     @PathVariable String jogadorSlug,
                                     @RequestParam int nota,
                                     HttpSession session) {
        try {
            var vm = notasService.votar(id, selecaoSlug, jogadorSlug, session.getId(), nota);
            return Map.of("ok", true, "media", vm.media(), "totalVotos", vm.totalVotos(),
                          "votoMinimo", vm.votoMinimo());
        } catch (Exception e) {
            return Map.of("ok", false, "erro", e.getMessage());
        }
    }

    @GetMapping("/api/jogo/{id}/notas/stream")
    public SseEmitter notasStream(@PathVariable Long id) {
        return sse.subscribeNotasEmitter(id);
    }
}
