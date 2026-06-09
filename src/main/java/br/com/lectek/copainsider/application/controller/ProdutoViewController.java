package br.com.lectek.copainsider.application.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProdutoViewController {

    @GetMapping("/catalogo")
    public String redirecionarCatalogo(
            @RequestParam(value = "q", required = false) String termo,
            @RequestParam(value = "cat", required = false) String categoria,
            @RequestParam(value = "categoria", required = false) String categoriaLegada,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pagina", required = false) Integer paginaLegada,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "tamanho", required = false) Integer tamanhoLegado,
            RedirectAttributes redirectAttributes
    ) {
        String q = normalizeOrEmpty(termo);
        String cat = firstNotBlank(categoria, categoriaLegada);
        int targetPage = sanitizeNonNegative(firstNonNull(page, paginaLegada), 0);
        int targetSize = sanitizeRange(firstNonNull(size, tamanhoLegado), 1, 100, 18);

        redirectAttributes.addAttribute("q", q);
        if (cat != null) {
            redirectAttributes.addAttribute("cat", cat);
        }
        redirectAttributes.addAttribute("page", targetPage);
        redirectAttributes.addAttribute("size", targetSize);

        return "redirect:/produtos";
    }

    private static String firstNotBlank(String preferred, String fallback) {
        String a = normalizeOrNull(preferred);
        return a != null ? a : normalizeOrNull(fallback);
    }

    private static String normalizeOrEmpty(String value) {
        String normalized = normalizeOrNull(value);
        return normalized == null ? "" : normalized;
    }

    private static String normalizeOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static Integer firstNonNull(Integer preferred, Integer fallback) {
        return preferred != null ? preferred : fallback;
    }

    private static int sanitizeNonNegative(Integer value, int fallback) {
        if (value == null) {
            return fallback;
        }
        return Math.max(0, value);
    }

    private static int sanitizeRange(Integer value, int min, int max, int fallback) {
        if (value == null) {
            return fallback;
        }
        return Math.min(max, Math.max(min, value));
    }
}
