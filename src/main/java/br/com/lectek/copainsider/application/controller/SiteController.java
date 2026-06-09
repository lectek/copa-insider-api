package br.com.lectek.copainsider.application.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SiteController {

    @GetMapping("/")
    public String landing() {
        return "pages/site/landing";
    }
}
