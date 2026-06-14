package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.application.service.CopaAcessoService;
import br.com.lectek.copainsider.application.service.CopaAcessoService.ResgateResult;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/resgatar-codigo")
public class ResgateCodigoController {

    private final CopaAcessoService acessoService;

    public ResgateCodigoController(CopaAcessoService acessoService) {
        this.acessoService = acessoService;
    }

    @GetMapping
    public String form(Authentication auth) {
        if (!isAutenticado(auth)) {
            return "redirect:/auth/login?redirect=/resgatar-codigo";
        }
        return "pages/auth/resgatar-codigo";
    }

    @PostMapping
    public String resgatar(@RequestParam String codigo,
                           Authentication auth,
                           RedirectAttributes ra) {
        if (!isAutenticado(auth)) {
            return "redirect:/auth/login";
        }

        String email = auth.getName();
        ResgateResult resultado = acessoService.resgatar(email, codigo.trim());

        switch (resultado) {
            case OK ->
                ra.addFlashAttribute("successMessage",
                    "Acesso ativado com sucesso! Os teus conteúdos estão disponíveis.");
            case JA_ATIVO ->
                ra.addFlashAttribute("infoMessage",
                    "Este código já está ativo na tua conta.");
            case CODIGO_INVALIDO ->
                ra.addFlashAttribute("errorMessage",
                    "Código inválido ou não encontrado. Verifica se copiaste corretamente.");
            case SEM_PRODUTOS ->
                ra.addFlashAttribute("errorMessage",
                    "Código válido mas ainda a processar. Aguarda alguns minutos e tenta novamente.");
        }

        return "redirect:/resgatar-codigo";
    }

    private static boolean isAutenticado(Authentication auth) {
        return auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getName());
    }
}
