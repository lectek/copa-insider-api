package br.com.redemaisfarma.adapters.inbound.web.controller.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    /**
     * Feature flag that controls OAuth2 login options in the page.
     */
    @Value("${app.security.oauth2.enabled:true}")
    private boolean oauth2Enabled;

    /**
     * Renders login page.
     *
     * @param redirect optional post-login target
     * @param model web model
     * @return login view
     */
    @GetMapping("/auth/login")
    public String login(
            @RequestParam(value = "redirect", required = false) final String redirect,
            final Model model
    ) {
        // Default redirect is customer area.
        // JS swaps to admin by selected profile.
        model.addAttribute("redirectDefault", resolveRedirectDefault(redirect));
        model.addAttribute("oauth2Enabled", oauth2Enabled);
        return "pages/auth/login";
    }

    private String resolveRedirectDefault(final String redirect) {
        if (StringUtils.hasText(redirect)) {
            final String normalized = redirect.trim();
            if (normalized.startsWith("/") && !normalized.startsWith("//")) {
                return normalized;
            }
        }
        return "/cliente/conta";
    }
}
