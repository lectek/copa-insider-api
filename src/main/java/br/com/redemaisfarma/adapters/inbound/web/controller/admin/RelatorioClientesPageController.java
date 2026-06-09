package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RelatorioClientesPageController {

    /**
     * Renders the customer report page.
     *
     * @return customer report view
     */
    @GetMapping(
            value = "/admin/relatorios/clientes",
            produces = MediaType.TEXT_HTML_VALUE
    )
    public String index() {
        return "pages/admin/relatorios/clientes";
    }
}
