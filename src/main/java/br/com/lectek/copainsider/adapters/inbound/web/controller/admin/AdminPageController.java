package br.com.lectek.copainsider.adapters.inbound.web.controller.admin;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Profile("legacy")
@RequestMapping("/admin")
public class AdminPageController {

    /**
     * Resolves one-level admin section path.
     *
     * @param sec section name
     * @return resolved view path
     */
    @GetMapping("/{sec}")
    public String adminSection(@PathVariable("sec") final String sec) {
        return "pages/admin/" + sec + "/index";
    }

    /**
     * Resolves two-level admin page path.
     *
     * @param sec section name
     * @param page page name
     * @return resolved view path
     */
    @GetMapping("/{sec}/{page}")
    public String adminPage(
            @PathVariable("sec") final String sec,
            @PathVariable("page") final String page
    ) {
        return "pages/admin/" + sec + "/" + page;
    }

    /**
     * Resolves three-level admin page path.
     *
     * @param sec section name
     * @param sub subsection name
     * @param page page name
     * @return resolved view path
     */
    @GetMapping("/{sec}/{sub}/{page}")
    public String adminDeepPage(
            @PathVariable("sec") final String sec,
            @PathVariable("sub") final String sub,
            @PathVariable("page") final String page
    ) {
        return "pages/admin/" + sec + "/" + sub + "/" + page;
    }
}
