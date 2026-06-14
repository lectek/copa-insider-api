package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.CopaProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.CopaProdutoJPARepository;
import br.com.lectek.copainsider.application.copa.CopaProdutoVM;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

@Controller
public class SiteController {

    private final CopaProdutoJPARepository produtoRepo;

    public SiteController(CopaProdutoJPARepository produtoRepo) {
        this.produtoRepo = produtoRepo;
    }

    @GetMapping("/onde-assistir")
    public String ondeAssistir() {
        return "pages/site/onde-assistir";
    }

    @GetMapping("/")
    public String landing(Model model, Locale locale) {
        List<CopaProdutoVM> produtos = produtoRepo.findByAtivoTrueOrderByOrdemAsc()
                .stream()
                .map(p -> toVM(p, locale, false, 1))
                .toList();
        model.addAttribute("produtos", produtos);
        return "pages/site/landing";
    }

    private CopaProdutoVM toVM(CopaProdutoEntity p, Locale locale,
                               boolean categoriaComMultiplos, long totalNaCategoria) {
        String lang    = locale != null ? locale.getLanguage() : "pt";
        String country = locale != null ? locale.getCountry()  : "BR";

        String nome;
        String desc;
        if ("en".equals(lang)) {
            nome = firstNonEmpty(p.getNomeEn(),   p.getNomePtBr());
            desc = firstNonEmpty(p.getDescEn(),   p.getDescPtBr());
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
                categoriaComMultiplos, totalNaCategoria);
    }

    private String firstNonEmpty(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        return b != null ? b : "";
    }
}
