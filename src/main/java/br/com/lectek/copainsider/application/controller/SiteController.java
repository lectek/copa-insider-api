package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.CopaProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.CopaProdutoJPARepository;
import br.com.lectek.copainsider.application.copa.Copa2026DataService;
import br.com.lectek.copainsider.application.copa.CopaProdutoVM;
import br.com.lectek.copainsider.application.copa.PartidaVM;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import br.com.lectek.copainsider.application.copa.ClassificacaoVM;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
public class SiteController {

    private static final String AO_VIVO_ATTR = "aoVivo";

    private static final List<Map<String, String>> FONTES_LIVE = List.of(
        Map.of("nome", "FIFA+",       "url", "https://www.fifa.com/fifaplus",         "flag", "🌍", "desc", "Internacional"),
        Map.of("nome", "RTP Play",    "url", "https://www.rtp.pt/play/",              "flag", "🇵🇹", "desc", "Portugal"),
        Map.of("nome", "CazéTV",      "url", "https://www.youtube.com/@CazeTV",       "flag", "🇧🇷", "desc", "Brasil — YouTube"),
        Map.of("nome", "Globo Play",  "url", "https://globoplay.globo.com",           "flag", "🇧🇷", "desc", "Brasil"),
        Map.of("nome", "BBC iPlayer", "url", "https://www.bbc.co.uk/iplayer",         "flag", "🇬🇧", "desc", "Reino Unido"),
        Map.of("nome", "ITV X",       "url", "https://www.itv.com",                   "flag", "🇬🇧", "desc", "Reino Unido")
    );

    private final CopaProdutoJPARepository produtoRepo;
    private final Copa2026DataService      copaData;

    public SiteController(CopaProdutoJPARepository produtoRepo, Copa2026DataService copaData) {
        this.produtoRepo = produtoRepo;
        this.copaData    = copaData;
    }

    @GetMapping("/ao-vivo")
    public String aoVivo(Model model) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate hoje = now.toLocalDate();

        List<PartidaVM> todas = copaData.listPartidas();

        List<PartidaVM> aoVivo = todas.stream()
                .filter(PartidaVM::aoVivo)
                .toList();

        PartidaVM proxJogo = todas.stream()
                .filter(p -> p.agendada() && p.dataHora() != null && p.dataHora().isAfter(now))
                .min(Comparator.comparing(PartidaVM::dataHora))
                .orElse(null);

        // Todos os jogos de hoje (exceto os que já estão em aoVivo), ordenados por hora
        List<PartidaVM> jogosHoje = todas.stream()
                .filter(p -> p.dataHora() != null && p.dataHora().toLocalDate().equals(hoje))
                .filter(p -> !p.aoVivo())
                .sorted(Comparator.comparing(PartidaVM::dataHora))
                .toList();

        model.addAttribute(AO_VIVO_ATTR,    aoVivo);
        model.addAttribute("proxJogo",  proxJogo);
        model.addAttribute("jogosHoje", jogosHoje);
        model.addAttribute("fontes",    FONTES_LIVE);
        return "pages/site/ao-vivo";
    }

    @GetMapping("/bracket")
    public String bracket(Model model) {
        model.addAttribute("knockout", copaData.knockoutPorFase());
        model.addAttribute(AO_VIVO_ATTR,   copaData.partidasAoVivo());
        return "pages/site/bracket";
    }

    @GetMapping("/classificacao")
    public String classificacao(Model model) {
        Map<String, List<ClassificacaoVM>> classificacao = copaData.getClassificacao();
        model.addAttribute("classificacao", classificacao);
        model.addAttribute("terceiros", copaData.getTerceirosOrdenados(classificacao));
        model.addAttribute("qualificados", copaData.getQualificados(classificacao));
        model.addAttribute("gruposEncerrados", copaData.isGruposEncerrados());
        model.addAttribute(AO_VIVO_ATTR, copaData.partidasAoVivo());
        return "pages/site/classificacao";
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
