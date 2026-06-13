package br.com.lectek.copainsider.adapters.inbound.web;

import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.CopaAcessoJPARepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.CopaProdutoJPARepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.CopaAcessoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.CopaProdutoEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

@Controller
public class ContaAcessosController {

    private final CopaAcessoJPARepository acessoRepo;
    private final CopaProdutoJPARepository produtoRepo;

    public ContaAcessosController(CopaAcessoJPARepository acessoRepo,
                                   CopaProdutoJPARepository produtoRepo) {
        this.acessoRepo  = acessoRepo;
        this.produtoRepo = produtoRepo;
    }

    @GetMapping("/conta/acessos")
    public String acessos(@AuthenticationPrincipal UserDetails user, Model model) {
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
                .anyMatch(a -> "copa-pass".equals(a.getProdutoSlug())));
        return "pages/site/conta-acessos";
    }

    private String nomePtPt(CopaProdutoEntity p) {
        return (p.getNomePtPt() != null && !p.getNomePtPt().isBlank())
                ? p.getNomePtPt() : p.getNomePtBr();
    }

    private String urlPara(String slug) {
        return switch (slug) {
            case "copa-pass", "acesso-calendario-comparador", "historico-confronto" -> "/comparar";
            case "copa-em-20-factos" -> "/factos";
            default -> slug.startsWith("guia-selecao-")
                       ? "/selecoes/" + slug.replace("guia-selecao-", "")
                       : "/loja";
        };
    }

    private String iconePara(String slug) {
        return switch (slug) {
            case "copa-pass"                       -> "⭐";
            case "acesso-calendario-comparador"    -> "📅";
            case "historico-confronto"             -> "⚔️";
            case "copa-em-20-factos"               -> "📊";
            case "guia-selecao-portugal"           -> "🇵🇹";
            case "guia-selecao-brasil"             -> "🇧🇷";
            default -> "📄";
        };
    }
}
