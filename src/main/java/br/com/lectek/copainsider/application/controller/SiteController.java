package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.CopaProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.CopaProdutoJPARepository;
import br.com.lectek.copainsider.application.copa.CopaProdutoVM;
import br.com.lectek.copainsider.domain.enums.TipoCopaProduto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class SiteController {

    private final CopaProdutoJPARepository produtoRepo;

    public SiteController(CopaProdutoJPARepository produtoRepo) {
        this.produtoRepo = produtoRepo;
    }

    @GetMapping("/")
    public String landing(Model model, Locale locale) {
        List<CopaProdutoEntity> todos = produtoRepo.findByAtivoTrueOrderByOrdemAsc();

        // Agrupa por tipo → 1 card representativo por tipo (para a landing)
        Map<TipoCopaProduto, List<CopaProdutoEntity>> porTipo = todos.stream()
                .collect(Collectors.groupingBy(
                        CopaProdutoEntity::getTipo,
                        LinkedHashMap::new,
                        Collectors.toList()));

        List<CopaProdutoVM> produtos = porTipo.entrySet().stream()
                .map(entry -> {
                    List<CopaProdutoEntity> grupo = entry.getValue();
                    CopaProdutoEntity rep = grupo.get(0); // representativo: primeiro por ordem
                    boolean multiplos = grupo.size() > 1;
                    return toVM(rep, locale, multiplos, grupo.size());
                })
                .limit(4)
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
