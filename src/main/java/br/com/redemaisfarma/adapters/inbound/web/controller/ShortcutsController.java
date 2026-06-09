package br.com.redemaisfarma.adapters.inbound.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ShortcutsController {

    /**
     * Redirects generic customer shortcut to account page.
     *
     * @return customer account redirect
     */
    @GetMapping(value = {"/cliente"})
    public String cliente() {
        return "redirect:/cliente/conta";
    }
}
