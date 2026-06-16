package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.CopaProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.CopaProdutoJPARepository;
import br.com.lectek.copainsider.application.copa.Copa2026DataService;
import br.com.lectek.copainsider.application.copa.CopaProdutoVM;
import br.com.lectek.copainsider.application.copa.PartidaVM;
import br.com.lectek.copainsider.application.service.CopaAcessoService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CopaLojaController {

    private final CopaProdutoJPARepository repository;
    private final CopaAcessoService        acessoService;
    private final Copa2026DataService      copaData;

    public CopaLojaController(CopaProdutoJPARepository repository,
                               CopaAcessoService acessoService,
                               Copa2026DataService copaData) {
        this.repository   = repository;
        this.acessoService = acessoService;
        this.copaData     = copaData;
    }

    @GetMapping("/loja")
    public String loja(@RequestParam(required = false) String tipo,
                       Model model, Locale locale) {
        List<CopaProdutoEntity> todos = repository.findByAtivoTrueOrderByOrdemAsc();

        List<CopaProdutoVM> produtos = todos.stream()
                .filter(p -> tipo == null || p.getTipo().name().equals(tipo))
                .map(p -> toVM(p, locale))
                .toList();

        model.addAttribute("produtos", produtos);
        model.addAttribute("tipoFiltro", tipo);
        model.addAttribute("tipoNome", tipoNome(tipo));
        return "pages/site/loja";
    }

    @GetMapping("/guia/{slug}")
    public String produto(@PathVariable String slug, Model model, Locale locale,
                          org.springframework.security.core.Authentication auth) {
        return repository.findBySlugAndAtivoTrue(slug)
                .map(p -> {
                    model.addAttribute("produto", toVM(p, locale));
                    String email = (auth != null && auth.isAuthenticated()
                                    && !"anonymousUser".equals(auth.getName()))
                                   ? auth.getName() : null;
                    model.addAttribute("temAcesso", acessoService.temAcesso(email, p.getSlug()));
                    model.addAttribute("urlConteudo", urlConteudo(p.getSlug()));

                    // Próximo jogo da seleção (só para guias de seleção)
                    if (p.getSlugTime1() != null) {
                        String slugTime = p.getSlugTime1();
                        LocalDateTime now = LocalDateTime.now();
                        copaData.listPartidas().stream()
                                .filter(j -> j.agendada() && j.dataHora() != null && j.dataHora().isAfter(now)
                                        && (slugTime.equals(j.slugCasa()) || slugTime.equals(j.slugVisitante())))
                                .min(Comparator.comparing(PartidaVM::dataHora))
                                .ifPresent(j -> model.addAttribute("proximoJogo", j));
                    }

                    return "pages/site/produto";
                })
                .orElse("redirect:/loja");
    }

    private String urlConteudo(String slug) {
        return switch (slug) {
            case "copa-pass", "historico-confronto", "acesso-calendario-comparador" -> "/comparar";
            case "copa-em-20-factos"     -> "/factos";
            case "guia-selecao-portugal" -> "/selecoes/portugal";
            case "guia-selecao-brasil"   -> "/selecoes/brasil";
            default -> slug.startsWith("guia-selecao-")
                       ? "/selecoes/" + slug.replace("guia-selecao-", "")
                       : "/conta/acessos";
        };
    }

    private String tipoNome(String tipo) {
        if (tipo == null) return null;
        return switch (tipo) {
            case "GUIA_SELECAO"   -> "Guias de Seleções";
            case "DUELO_HISTORICO" -> "Histórico de Confrontos";
            case "ANALISE_PREMIUM" -> "Análises & Factos";
            case "COPA_PASS"       -> "Copa Pass";
            default -> tipo;
        };
    }

    private CopaProdutoVM toVM(CopaProdutoEntity p, Locale locale) {
        String lang = locale != null ? locale.getLanguage() : "pt";
        String country = locale != null ? locale.getCountry() : "BR";

        String nome;
        String desc;
        if ("en".equals(lang)) {
            nome = firstNonEmpty(p.getNomeEn(), p.getNomePtBr());
            desc = firstNonEmpty(p.getDescEn(), p.getDescPtBr());
        } else if ("PT".equals(country)) {
            nome = firstNonEmpty(p.getNomePtPt(), p.getNomePtBr());
            desc = firstNonEmpty(p.getDescPtPt(), p.getDescPtBr());
        } else {
            nome = firstNonEmpty(p.getNomePtBr(), p.getNomeEn());
            desc = firstNonEmpty(p.getDescPtBr(), p.getDescEn());
        }

        BigDecimal precoEur = p.getPrecoEur() != null
                ? p.getPrecoEur()
                : p.getPreco().divide(BigDecimal.valueOf(6.20), 2, RoundingMode.HALF_UP);

        return new CopaProdutoVM(
                p.getSlug(), p.getTipo(), p.getPreco(), precoEur,
                nome, desc,
                p.getHotmartUrl(), p.getImagemUrl(),
                p.getSlugTime1(), p.getSlugTime2(),
                false, 1L
        );
    }

    private String firstNonEmpty(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        return b != null ? b : "";
    }
}
