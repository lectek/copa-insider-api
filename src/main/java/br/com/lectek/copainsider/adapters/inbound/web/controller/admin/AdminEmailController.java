package br.com.lectek.copainsider.adapters.inbound.web.controller.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class AdminEmailController {

    /**
     * Canonical admin email center route.
     */
    private static final String CANONICAL_PATH = "/admin/marketing/emails/central";

    /**
     * View name used by the admin email center.
     */
    private static final String VIEW = "pages/admin/email/central";

    /**
     * Redirects legacy email center URLs to the canonical route.
     *
     * @return redirect to canonical admin email center
     */
    @GetMapping({
            "/admin/email",
            "/admin/email/",
            "/admin/email/central",
            "/admin/email/central/"
    })
    public String redirectLegacyPaths() {
        return "redirect:" + CANONICAL_PATH;
    }

    /**
     * Renders the canonical admin email center page.
     *
     * @param model thymeleaf model
     * @return admin email center view
     */
    @GetMapping({
            CANONICAL_PATH,
            CANONICAL_PATH + "/"
    })
    public String index(final Model model) {
        model.addAttribute(
                "title",
                "Central de E-mails - Admin - CopaInsider"
        );
        model.addAttribute("pageTitle", "Central de E-mails");
        model.addAttribute("active", "marketing");
        return VIEW;
    }
}
