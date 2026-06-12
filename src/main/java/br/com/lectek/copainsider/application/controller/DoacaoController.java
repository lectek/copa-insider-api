package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.application.service.DoacaoService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.math.BigDecimal;

@Controller
public class DoacaoController {

    private final DoacaoService doacaoService;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public DoacaoController(DoacaoService doacaoService,
                            ObjectMapper objectMapper,
                            @Value("${app.web.base-url:http://localhost:8080}") String baseUrl) {
        this.doacaoService = doacaoService;
        this.objectMapper  = objectMapper;
        this.baseUrl       = baseUrl;
    }

    @GetMapping("/doacao")
    public String pagina(Model model) {
        model.addAttribute("stripeEnabled", doacaoService.isStripeEnabled());
        return "pages/site/doacao";
    }

    @GetMapping("/doacao/obrigado")
    public String obrigado(@RequestParam(required = false) String session_id, Model model) {
        model.addAttribute("sessionId", session_id);
        return "pages/site/doacao-obrigado";
    }

    @PostMapping("/api/doacao/checkout")
    public String checkout(@RequestParam String nome,
                           @RequestParam String email,
                           @RequestParam BigDecimal valor,
                           @RequestParam(required = false) String mensagem) {
        if (valor == null || valor.compareTo(BigDecimal.valueOf(1)) < 0) {
            return "redirect:/doacao?erro=valor";
        }
        try {
            String url = doacaoService.criarCheckoutSession(nome, email, valor, mensagem, baseUrl);
            if (url == null) {
                return "redirect:/doacao/obrigado";
            }
            return "redirect:" + url;
        } catch (Exception e) {
            return "redirect:/doacao?erro=stripe";
        }
    }

    @PostMapping("/webhooks/stripe")
    @ResponseBody
    public ResponseEntity<String> stripeWebhook(HttpServletRequest request) throws IOException {
        byte[] body = request.getInputStream().readAllBytes();
        try {
            JsonNode event = objectMapper.readTree(body);
            String type = event.path("type").asText();
            if ("checkout.session.completed".equals(type)) {
                String sessionId = event.path("data").path("object").path("id").asText();
                doacaoService.confirmarPagamento(sessionId);
            }
        } catch (Exception ignored) {}
        return ResponseEntity.ok("ok");
    }
}
