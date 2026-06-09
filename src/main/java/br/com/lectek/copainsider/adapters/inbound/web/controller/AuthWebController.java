package br.com.lectek.copainsider.adapters.inbound.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthWebController {

    /**
     * Redirects legacy login routes to the auth login page.
     *
     * @return login page redirect
     */
    @GetMapping({"/entrar", "/login"})
    public String loginPage() {
        return "redirect:/auth/login";
    }
}
