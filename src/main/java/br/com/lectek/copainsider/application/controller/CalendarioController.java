package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.application.copa.Copa2026DataService;
import br.com.lectek.copainsider.application.copa.PartidaVM;
import br.com.lectek.copainsider.application.copa.SelecaoVM;
import br.com.lectek.copainsider.application.service.CopaAcessoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class CalendarioController {

    private static final String SLUG_ACESSO = "acesso-calendario-comparador";
    private static final String VIEW        = "pages/site/calendario";

    private final Copa2026DataService copaData;
    private final CopaAcessoService   acessoService;

    public CalendarioController(Copa2026DataService copaData, CopaAcessoService acessoService) {
        this.copaData     = copaData;
        this.acessoService = acessoService;
    }

    @GetMapping("/calendario")
    public String calendario(
            @RequestParam(required = false) String grupo,
            Authentication authentication,
            Model model) {

        List<PartidaVM> aoVivo   = copaData.partidasAoVivo();
        List<PartidaVM> proximas = copaData.proximasPartidas(5);
        model.addAttribute("aoVivo",   aoVivo);
        model.addAttribute("proximas", proximas);

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            model.addAttribute("precisaLogin", true);
            return VIEW;
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        if (!isAdmin && !acessoService.temAcesso(authentication.getName(), SLUG_ACESSO)) {
            model.addAttribute("precisaComprar", true);
            return VIEW;
        }

        Map<String, List<PartidaVM>> partidasPorGrupo = copaData.partidasPorGrupo();
        Map<String, List<SelecaoVM>> selecoesPorGrupo = copaData.selecoesPorGrupo();

        List<PartidaVM> partidasFiltradas = (grupo != null && !grupo.isBlank())
                ? partidasPorGrupo.getOrDefault(grupo.toUpperCase(), List.of())
                : copaData.listPartidas();

        model.addAttribute("partidasPorGrupo",  partidasPorGrupo);
        model.addAttribute("selecoesPorGrupo",  selecoesPorGrupo);
        model.addAttribute("partidasFiltradas", partidasFiltradas);
        model.addAttribute("grupoSelecionado",  grupo != null ? grupo.toUpperCase() : null);
        model.addAttribute("grupos",            partidasPorGrupo.keySet());

        return VIEW;
    }
}
