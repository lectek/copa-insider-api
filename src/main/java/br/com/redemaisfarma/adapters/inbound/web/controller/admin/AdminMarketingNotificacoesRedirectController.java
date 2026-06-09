package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin/marketing/notificacoes")
public class AdminMarketingNotificacoesRedirectController {

    /**
     * Canonical notifications page.
     */
    private static final String TARGET = "/admin/notificacoes";

    /**
     * Redirects GET requests to the canonical notifications page.
     *
     * @return redirect string
     */
    @GetMapping({"", "/"})
    public String redirectGet() {
        return "redirect:" + TARGET;
    }

    /**
     * Redirects stale form submissions to the canonical notifications page.
     *
     * @return redirect view with see-other semantics
     */
    @PostMapping({"", "/"})
    public RedirectView redirectPost() {
        final RedirectView redirectView = new RedirectView(TARGET);
        redirectView.setStatusCode(HttpStatus.SEE_OTHER);
        return redirectView;
    }
}
