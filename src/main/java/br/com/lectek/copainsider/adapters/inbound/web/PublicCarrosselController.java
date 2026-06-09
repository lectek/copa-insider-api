package br.com.lectek.copainsider.adapters.inbound.web;

import br.com.lectek.copainsider.application.view.ProductCardVM;
import br.com.lectek.copainsider.domain.catalogo.ProdutoQueryService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Deprecated(since = "2026-03", forRemoval = false)
@RestController
@RequestMapping(value = "/api/public/carrossel", produces = "application/json")
public class PublicCarrosselController {

    private static final String SUCCESSOR = "/api/public/produtos/destaques";
    private final ProdutoQueryService service;

    @Deprecated(since = "2026-03", forRemoval = false)
    public PublicCarrosselController(ProdutoQueryService service) {
        this.service = service;
    }

    @Deprecated(since = "2026-03", forRemoval = false)
    @GetMapping("/destaques")
    public ResponseEntity<List<ProductCardVM>> destaques(
            @RequestParam(name = "limit", defaultValue = "10") int limit
    ) {
        int safe = Math.min(Math.max(limit, 1), 20);
        return ResponseEntity.ok()
                .header("Deprecation", "true")
                .header("Sunset", "Tue, 30 Sep 2026 23:59:59 GMT")
                .header(HttpHeaders.LINK, "<" + SUCCESSOR + ">; rel=\"successor-version\"")
                .body(this.service.featured(safe));
    }
}
