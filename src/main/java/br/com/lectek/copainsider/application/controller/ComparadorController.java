package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.application.copa.Copa2026DataService;
import br.com.lectek.copainsider.application.service.CopaAcessoService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ComparadorController {

    private static final String SLUG_ACESSO = "acesso-calendario-comparador";
    private static final String VIEW        = "pages/site/comparar";

    private final Copa2026DataService copaData;
    private final CopaAcessoService   acessoService;

    public ComparadorController(Copa2026DataService copaData, CopaAcessoService acessoService) {
        this.copaData     = copaData;
        this.acessoService = acessoService;
    }

    @GetMapping("/comparar")
    public String comparar(
            @RequestParam(required = false) String time1,
            @RequestParam(required = false) String time2,
            Authentication authentication,
            Model model) {

        model.addAttribute("selecoes", copaData.listSelecoes());
        model.addAttribute("time1", time1 != null ? time1 : "");
        model.addAttribute("time2", time2 != null ? time2 : "");

        boolean tentouComparar = time1 != null && !time1.isBlank()
                              && time2 != null && !time2.isBlank()
                              && !time1.equals(time2);

        if (!tentouComparar) {
            return VIEW;
        }

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

        copaData.comparar(time1, time2)
                .ifPresent(r -> model.addAttribute("resultado", r));

        return VIEW;
    }
}
