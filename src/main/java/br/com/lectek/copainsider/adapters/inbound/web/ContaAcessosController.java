package br.com.lectek.copainsider.adapters.inbound.web;

import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.CopaAcessoJPARepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.CopaProdutoJPARepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.CopaAcessoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.CopaProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.lectek.copainsider.application.service.MailService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

@Controller
public class ContaAcessosController {

    private static final Logger log = LoggerFactory.getLogger(ContaAcessosController.class);
    private static final String SESSION_WELCOME  = "ci_welcome_sent";
    private static final String SLUG_COPA_PASS   = "copa-pass";

    private final CopaAcessoJPARepository acessoRepo;
    private final CopaProdutoJPARepository produtoRepo;
    private final MailService mailService;
    private final UsuarioRepository usuarioRepo;

    @Value("${app.web.base-url:https://allaboutworldcup2026.com}")
    private String baseUrl;

    public ContaAcessosController(CopaAcessoJPARepository acessoRepo,
                                   CopaProdutoJPARepository produtoRepo,
                                   MailService mailService,
                                   UsuarioRepository usuarioRepo) {
        this.acessoRepo  = acessoRepo;
        this.produtoRepo = produtoRepo;
        this.mailService = mailService;
        this.usuarioRepo = usuarioRepo;
    }

    @GetMapping("/conta/acessos")
    public String acessos(@AuthenticationPrincipal UserDetails user,
                          Model model, HttpServletRequest request) {
        if (user == null) {
            return "redirect:/auth/login?redirect=/conta/acessos";
        }

        String email = user.getUsername().toLowerCase();
        List<CopaAcessoEntity> acessos = acessoRepo.findByEmailIgnoreCase(email);

        List<Map<String, Object>> items = acessos.stream()
                .sorted(Comparator.comparing(CopaAcessoEntity::getConcedidoEm).reversed())
                .map(a -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("slug",        a.getProdutoSlug());
                    item.put("concedidoEm", a.getConcedidoEm());
                    produtoRepo.findBySlugAndAtivoTrue(a.getProdutoSlug()).ifPresent(p -> {
                        item.put("nome",      nomePtPt(p));
                        item.put("descricao", p.getDescPtPt() != null ? p.getDescPtPt() : p.getDescPtBr());
                        item.put("precoEur",  p.getPrecoEur());
                    });
                    item.put("url",  urlPara(a.getProdutoSlug()));
                    item.put("icon", iconePara(a.getProdutoSlug()));
                    return item;
                })
                .filter(item -> item.containsKey("nome"))  // ignora slugs de produtos inactivos/deletados
                .toList();

        model.addAttribute("acessos", items);
        model.addAttribute("email",   email);
        model.addAttribute("temCopaPass", acessos.stream()
                .anyMatch(a -> SLUG_COPA_PASS.equals(a.getProdutoSlug())));

        if (!items.isEmpty() && request.getSession().getAttribute(SESSION_WELCOME) == null) {
            request.getSession().setAttribute(SESSION_WELCOME, true);
            enviarBoasVindas(email, items);
        }

        return "pages/site/conta-acessos";
    }

    private void enviarBoasVindas(String email, List<Map<String, Object>> items) {
        try {
            String nome = usuarioRepo.findByEmailIgnoreCase(email)
                    .map(u -> u.getNome() != null ? u.getNome().split(" ")[0] : email)
                    .orElse(email);
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("nome",         nome);
            ctx.put("acessos",      items);
            ctx.put("plataformaUrl", baseUrl);
            mailService.sendTemplate(email,
                    "Os teus conteúdos Copa Insider estão prontos ⚽",
                    "mail/boas-vindas", ctx, null);
            log.info("[boas-vindas] email enviado para {}", email);
        } catch (Exception ex) {
            log.warn("[boas-vindas] falha ao enviar para {}: {}", email, ex.getMessage());
        }
    }

    private String nomePtPt(CopaProdutoEntity p) {
        return (p.getNomePtPt() != null && !p.getNomePtPt().isBlank())
                ? p.getNomePtPt() : p.getNomePtBr();
    }

    private String urlPara(String slug) {
        return switch (slug) {
            case SLUG_COPA_PASS, "acesso-calendario-comparador", "historico-confronto" -> "/comparar";
            case "copa-em-20-factos" -> "/factos";
            default -> slug.startsWith("guia-selecao-")
                       ? "/selecoes/" + slug.replace("guia-selecao-", "")
                       : "/loja";
        };
    }

    private String iconePara(String slug) {
        return switch (slug) {
            case SLUG_COPA_PASS                       -> "⭐";
            case "acesso-calendario-comparador"    -> "📅";
            case "historico-confronto"             -> "⚔️";
            case "copa-em-20-factos"               -> "📊";
            case "guia-selecao-portugal"           -> "🇵🇹";
            case "guia-selecao-brasil"             -> "🇧🇷";
            case "guia-selecao-franca"             -> "🇫🇷";
            case "guia-selecao-argentina"          -> "🇦🇷";
            case "guia-selecao-inglaterra"         -> "🏴󠁧󠁢󠁥󠁮󠁧󠁿";
            case "guia-selecao-espanha"            -> "🇪🇸";
            default -> "📄";
        };
    }
}
