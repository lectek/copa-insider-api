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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
        this.repository    = repository;
        this.geracaoService = geracaoService;
    }

    // ── Rota intermédia: guarda a seleção em cookie e redireciona para Hotmart ─

    @GetMapping("/iniciar/{selecaoCode}")
    public String iniciarCheckout(@PathVariable String selecaoCode,
                                   HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_SELECAO, selecaoCode.toUpperCase());
        cookie.setPath("/");
        cookie.setMaxAge(7200); // 2 horas
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);

        if (hotmartUrl != null && !hotmartUrl.isBlank()) {
            return "redirect:" + hotmartUrl;
        }
        return "redirect:/loja";
    }

    // ── Minhas Compras — lista os ebooks do cliente logado ──────────────────

    @GetMapping("/meus-ebooks")
    public String meusEbooks(@AuthenticationPrincipal UserDetails user, Model model) {
        if (user == null) return "redirect:/auth/login";

        List<EbookPedidoEntity> pedidos = repository
                .findByCompradorEmailOrderByCriadoEmDesc(user.getUsername());

        model.addAttribute("pedidos", pedidos);
        return "pages/cliente/meus-ebooks";
    }

    // ── Seleção pós-compra — cliente escolhe seleção e idioma ───────────────

    @GetMapping("/selecionar/{transacao}")
    public String paginaSelecao(@PathVariable String transacao,
                                 @AuthenticationPrincipal UserDetails user,
                                 HttpServletRequest request,
                                 HttpServletResponse response,
                                 Model model) {
        if (user == null) return "redirect:/auth/login";

        EbookPedidoEntity pedido = repository.findByTransacao(transacao).orElse(null);
        if (pedido == null || !pedido.getCompradorEmail().equalsIgnoreCase(user.getUsername())) {
            return "redirect:/ebook/meus-ebooks";
        }

        // Lê a seleção guardada quando o cliente clicou "Comprar" na loja
        String preSelecao = lerCookieSelecao(request);
        if (preSelecao != null) {
            model.addAttribute("preSelecaoCode", preSelecao);
            model.addAttribute("preSelecaoNome", nomeSelecao(preSelecao));
            // Limpa o cookie após consumir
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

    private String lerCookieSelecao(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> COOKIE_SELECAO.equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank())
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private static String nomeSelecao(String code) {
        return switch (code) {
            case "BRA" -> "Brasil";       case "POR" -> "Portugal";
            case "ARG" -> "Argentina";    case "FRA" -> "França";
            case "ESP" -> "Espanha";      case "ENG" -> "Inglaterra";
            case "ALE" -> "Alemanha";     case "ITA" -> "Itália";
            case "URU" -> "Uruguai";      case "MEX" -> "México";
            case "MAR" -> "Marrocos";     case "USA" -> "Estados Unidos";
            case "CAN" -> "Canadá";       case "COL" -> "Colômbia";
            case "ECU" -> "Equador";      case "NED" -> "Países Baixos";
            case "BEL" -> "Bélgica";      case "CRO" -> "Croácia";
            case "SUI" -> "Suíça";        case "DEN" -> "Dinamarca";
            case "AUT" -> "Áustria";      case "SRB" -> "Sérvia";
            case "POL" -> "Polónia";      case "SCO" -> "Escócia";
            case "TUR" -> "Turquia";      case "SEN" -> "Senegal";
            case "NGA" -> "Nigéria";      case "EGY" -> "Egito";
            case "CMR" -> "Camarões";     case "GHA" -> "Gana";
            case "CIV" -> "Costa do Marfim"; case "ALG" -> "Argélia";
            case "JPN" -> "Japão";        case "KOR" -> "Coreia do Sul";
            case "IRN" -> "Irão";         case "AUS" -> "Austrália";
            case "SAU" -> "Arábia Saudita"; case "QAT" -> "Qatar";
            case "JOR" -> "Jordânia";     case "IRQ" -> "Iraque";
            case "CRC" -> "Costa Rica";   case "PAN" -> "Panamá";
            case "VEN" -> "Venezuela";    case "CHI" -> "Chile";
            case "PAR" -> "Paraguai";     case "BOL" -> "Bolívia";
            case "RSA" -> "África do Sul"; case "NZL" -> "Nova Zelândia";
            default -> code;
        };
    }

    @PostMapping("/selecionar/{transacao}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> confirmarSelecao(
            @PathVariable String transacao,
            @RequestParam String selecaoCode,
            @RequestParam String selecaoNome,
            @RequestParam String idioma,
            @AuthenticationPrincipal UserDetails user) {

        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("erro", "Não autenticado"));
        }

        EbookPedidoEntity pedido = repository.findByTransacao(transacao).orElse(null);
        if (pedido == null || !pedido.getCompradorEmail().equalsIgnoreCase(user.getUsername())) {
            return ResponseEntity.status(403).body(Map.of("erro", "Acesso negado"));
        }
        if (pedido.getStatus() == EbookStatus.GERANDO || pedido.getStatus() == EbookStatus.PRONTO) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Ebook já está a ser gerado ou pronto"));
        }

        geracaoService.selecionarEGerar(transacao, selecaoCode, selecaoNome, idioma);
        log.info("[ebook] seleção confirmada — transacao={} selecao={} idioma={}", transacao, selecaoCode, idioma);

        return ResponseEntity.ok(Map.of("mensagem", "Geração iniciada! Receberá um email quando estiver pronto."));
    }

    // ── Download seguro via token ────────────────────────────────────────────

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

    // ── Status (polling da UI enquanto o ebook está a ser gerado) ───────────

    @GetMapping("/status/{transacao}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> status(
            @PathVariable String transacao,
            @AuthenticationPrincipal UserDetails user) {

        if (user == null) return ResponseEntity.status(401).build();

        EbookPedidoEntity pedido = repository.findByTransacao(transacao).orElse(null);
        if (pedido == null || !pedido.getCompradorEmail().equalsIgnoreCase(user.getUsername())) {
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
}
