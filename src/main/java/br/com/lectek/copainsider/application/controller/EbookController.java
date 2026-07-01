package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.EbookPedidoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.EbookPedidoJPARepository;
import br.com.lectek.copainsider.application.service.EbookGeracaoService;
import br.com.lectek.copainsider.domain.enums.EbookStatus;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/ebook")
public class EbookController {

    private static final Logger log = LoggerFactory.getLogger(EbookController.class);

    private static final String COOKIE_SELECAO = "ebook_selecao";

    private final EbookPedidoJPARepository repository;
    private final EbookGeracaoService geracaoService;

    @Value("${app.ebook.hotmart-url:}")
    private String hotmartUrl;

    public EbookController(EbookPedidoJPARepository repository, EbookGeracaoService geracaoService) {
        this.repository     = repository;
        this.geracaoService = geracaoService;
    }

    // ── Rota intermédia: guarda a seleção em cookie e redireciona para Hotmart ─

    @GetMapping("/iniciar/{selecaoCode}")
    public String iniciarCheckout(@PathVariable String selecaoCode,
                                   HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_SELECAO, selecaoCode.toUpperCase());
        cookie.setPath("/");
        cookie.setMaxAge(7200);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);

        if (hotmartUrl != null && !hotmartUrl.isBlank()) {
            return "redirect:" + hotmartUrl;
        }
        return "redirect:/loja";
    }

    // ── Minhas Compras ─────────────────────────────────────────────────────────

    @GetMapping("/meus-ebooks")
    public String meusEbooks(Authentication auth, Model model) {
        String email = extrairEmail(auth);
        if (email == null) return "redirect:/auth/login";

        List<EbookPedidoEntity> pedidos = repository
                .findByCompradorEmailOrderByCriadoEmDesc(email);

        model.addAttribute("pedidos", pedidos);
        return "pages/cliente/meus-ebooks";
    }

    // ── Seleção pós-compra ─────────────────────────────────────────────────────

    @GetMapping("/selecionar/{transacao}")
    public String paginaSelecao(@PathVariable String transacao,
                                 Authentication auth,
                                 HttpServletRequest request,
                                 HttpServletResponse response,
                                 Model model) {
        String email = extrairEmail(auth);
        if (email == null) return "redirect:/auth/login";

        EbookPedidoEntity pedido = repository.findByTransacao(transacao).orElse(null);
        if (pedido == null || !pedido.getCompradorEmail().equalsIgnoreCase(email)) {
            return "redirect:/ebook/meus-ebooks";
        }

        String preSelecao = lerCookieSelecao(request);
        if (preSelecao != null) {
            model.addAttribute("preSelecaoCode", preSelecao);
            model.addAttribute("preSelecaoNome", nomeSelecao(preSelecao));
            Cookie clear = new Cookie(COOKIE_SELECAO, "");
            clear.setPath("/");
            clear.setMaxAge(0);
            clear.setHttpOnly(true);
            clear.setSecure(true);
            response.addCookie(clear);
        }

        model.addAttribute("pedido", pedido);
        return "pages/cliente/ebook-selecionar";
    }

    @PostMapping("/selecionar/{transacao}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> confirmarSelecao(
            @PathVariable String transacao,
            @RequestParam String selecaoCode,
            @RequestParam String selecaoNome,
            @RequestParam String idioma,
            Authentication auth) {

        String email = extrairEmail(auth);
        if (email == null) {
            return ResponseEntity.status(401).body(Map.of("erro", "Não autenticado"));
        }

        EbookPedidoEntity pedido = repository.findByTransacao(transacao).orElse(null);
        if (pedido == null || !pedido.getCompradorEmail().equalsIgnoreCase(email)) {
            return ResponseEntity.status(403).body(Map.of("erro", "Acesso negado"));
        }
        if (pedido.getStatus() == EbookStatus.GERANDO || pedido.getStatus() == EbookStatus.PRONTO) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Ebook já está a ser gerado ou pronto"));
        }

        geracaoService.selecionarEGerar(transacao, selecaoCode, selecaoNome, idioma);
        log.info("[ebook] seleção confirmada — transacao={} selecao={} idioma={}", transacao, selecaoCode, idioma);

        return ResponseEntity.ok(Map.of("mensagem", "Geração iniciada! Receberá um email quando estiver pronto."));
    }

    // ── Download seguro via token ──────────────────────────────────────────────

    @GetMapping("/download/{token}")
    public void download(@PathVariable String token, HttpServletResponse response) throws IOException {
        EbookPedidoEntity pedido = repository.findByDownloadToken(token).orElse(null);

        if (pedido == null || pedido.getStatus() != EbookStatus.PRONTO || pedido.getPdfCaminho() == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Ebook não encontrado ou ainda não pronto");
            return;
        }

        File pdf = new File(pedido.getPdfCaminho());
        if (!pdf.exists()) {
            log.error("[ebook] ficheiro não encontrado no disco — token={} caminho={}", token, pedido.getPdfCaminho());
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Ficheiro não encontrado");
            return;
        }

        String nomeArquivo = "guia-" + pedido.getSelecaoCode().toLowerCase() + ".pdf";
        response.setContentType(MediaType.APPLICATION_PDF_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"");
        response.setContentLengthLong(pdf.length());

        Files.copy(pdf.toPath(), response.getOutputStream());
        log.info("[ebook] download servido — token={} email={}", token, pedido.getCompradorEmail());
    }

    // ── Status (polling da UI) ─────────────────────────────────────────────────

    @GetMapping("/status/{transacao}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> status(
            @PathVariable String transacao,
            Authentication auth) {

        String email = extrairEmail(auth);
        if (email == null) return ResponseEntity.status(401).build();

        EbookPedidoEntity pedido = repository.findByTransacao(transacao).orElse(null);
        if (pedido == null || !pedido.getCompradorEmail().equalsIgnoreCase(email)) {
            return ResponseEntity.status(403).build();
        }

        String downloadUrl = pedido.getStatus() == EbookStatus.PRONTO && pedido.getDownloadToken() != null
                ? "/ebook/download/" + pedido.getDownloadToken()
                : null;

        Map<String, String> resp = new java.util.HashMap<>();
        resp.put("status", pedido.getStatus().name());
        if (downloadUrl != null) resp.put("downloadUrl", downloadUrl);
        return ResponseEntity.ok(resp);
    }

    // ── Utilitários ───────────────────────────────────────────────────────────

    private String extrairEmail(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;

        // Form login: principal é UserDetails, getName() devolve o username/email
        if (auth.getName() != null && auth.getName().contains("@")) {
            return auth.getName().toLowerCase().strip();
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails ud) {
            String u = ud.getUsername();
            return u != null && u.contains("@") ? u.toLowerCase().strip() : null;
        }
        // OAuth2 (Google): email está nos atributos
        if (principal instanceof OAuth2User ou) {
            Object emailAttr = ou.getAttributes().get("email");
            if (emailAttr != null && !emailAttr.toString().isBlank()) {
                return emailAttr.toString().toLowerCase().strip();
            }
        }
        return null;
    }

    private String lerCookieSelecao(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> COOKIE_SELECAO.equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank())
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private static final Map<String, String> NOMES_SELECAO = Map.ofEntries(
        Map.entry("BRA","Brasil"),         Map.entry("ARG","Argentina"),
        Map.entry("FRA","França"),         Map.entry("ALE","Alemanha"),
        Map.entry("ITA","Itália"),         Map.entry("URU","Uruguai"),
        Map.entry("ENG","Inglaterra"),     Map.entry("ESP","Espanha"),
        Map.entry("POR","Portugal"),       Map.entry("NED","Países Baixos"),
        Map.entry("BEL","Bélgica"),        Map.entry("CRO","Croácia"),
        Map.entry("SUI","Suíça"),          Map.entry("DEN","Dinamarca"),
        Map.entry("SWE","Suécia"),         Map.entry("NOR","Noruega"),
        Map.entry("AUT","Áustria"),        Map.entry("SRB","Sérvia"),
        Map.entry("POL","Polónia"),        Map.entry("SCO","Escócia"),
        Map.entry("WAL","País de Gales"),  Map.entry("TUR","Turquia"),
        Map.entry("GRE","Grécia"),         Map.entry("HUN","Hungria"),
        Map.entry("ROM","Roménia"),        Map.entry("CZE","República Checa"),
        Map.entry("SVK","Eslováquia"),     Map.entry("BUL","Bulgária"),
        Map.entry("RUS","Rússia"),         Map.entry("UKR","Ucrânia"),
        Map.entry("IRL","Irlanda"),        Map.entry("SVN","Eslovénia"),
        Map.entry("ISL","Islândia"),       Map.entry("BIH","Bósnia-Herzegovina"),
        Map.entry("USA","Estados Unidos"), Map.entry("MEX","México"),
        Map.entry("CAN","Canadá"),         Map.entry("COL","Colômbia"),
        Map.entry("ECU","Equador"),        Map.entry("CHI","Chile"),
        Map.entry("PAR","Paraguai"),       Map.entry("BOL","Bolívia"),
        Map.entry("PER","Peru"),           Map.entry("CRC","Costa Rica"),
        Map.entry("PAN","Panamá"),         Map.entry("HON","Honduras"),
        Map.entry("SLV","El Salvador"),    Map.entry("HAI","Haiti"),
        Map.entry("JAM","Jamaica"),        Map.entry("TRI","Trinidad e Tobago"),
        Map.entry("CUB","Cuba"),           Map.entry("MAR","Marrocos"),
        Map.entry("SEN","Senegal"),        Map.entry("NGA","Nigéria"),
        Map.entry("CMR","Camarões"),       Map.entry("GHA","Gana"),
        Map.entry("CIV","Costa do Marfim"),Map.entry("EGY","Egito"),
        Map.entry("ALG","Argélia"),        Map.entry("TUN","Tunísia"),
        Map.entry("RSA","África do Sul"),  Map.entry("ANG","Angola"),
        Map.entry("TOG","Togo"),           Map.entry("COD","RD Congo"),
        Map.entry("JPN","Japão"),          Map.entry("KOR","Coreia do Sul"),
        Map.entry("IRN","Irão"),           Map.entry("SAU","Arábia Saudita"),
        Map.entry("AUS","Austrália"),      Map.entry("IRQ","Iraque"),
        Map.entry("KUW","Kuwait"),         Map.entry("CHN","China"),
        Map.entry("UAE","Emirados Árabes"),Map.entry("QAT","Qatar"),
        Map.entry("PRK","Coreia do Norte"),Map.entry("IDN","Indonésia"),
        Map.entry("NZL","Nova Zelândia"),  Map.entry("JOR","Jordânia"),
        Map.entry("ISR","Israel")
    );

    private static String nomeSelecao(String code) {
        return NOMES_SELECAO.getOrDefault(code, code);
    }
}
