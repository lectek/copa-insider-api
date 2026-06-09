package br.com.lectek.copainsider.adapters.inbound.web.controller.publico;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/produtos-publico")
public class ProdutoPublicPageController {

    /**
     * Renders public product catalog page.
     *
     * @param categoria selected category filter
     * @param tag selected tag filter
     * @param ordenacao selected sort mode
     * @param page current page number
     * @param size page size
     * @param model view model
     * @return public products page
     */
    @GetMapping
    public String produtos(
            @RequestParam(required = false) final String categoria,
            @RequestParam(required = false) final String tag,
            @RequestParam(required = false, name = "sort")
            final String ordenacao,
            @RequestParam(required = false, defaultValue = "0")
            final Integer page,
            @RequestParam(required = false, defaultValue = "24")
            final Integer size,
            final Model model
    ) {

        model.addAttribute("categoriaSelecionada", categoria);
        model.addAttribute("tagSelecionada", tag);
        model.addAttribute("ordenacao", ordenacao);
        model.addAttribute("page", page);
        model.addAttribute("size", size);

        return "produtos";
    }

    /**
     * Renders alternate list page.
     *
     * @param categoria selected category filter
     * @param tag selected tag filter
     * @param model view model
     * @return alternate list page
     */
    @GetMapping("/lista")
    public String lista(
            @RequestParam(required = false) final String categoria,
            @RequestParam(required = false) final String tag,
            final Model model
    ) {

        model.addAttribute("categoriaSelecionada", categoria);
        model.addAttribute("tagSelecionada", tag);

        return "lista";
    }

    /**
     * Renders public product details page.
     *
     * @param id product id
     * @param model view model
     * @return details page
     */
    @GetMapping("/{id}")
    public String detalhes(
            @PathVariable("id") final Long id,
            final Model model
    ) {
        model.addAttribute("produtoId", id);
        return "detalhes";
    }
}
