package br.com.lectek.copainsider.adapters.inbound.web;

import br.com.lectek.copainsider.application.view.ProductCardVM;
import br.com.lectek.copainsider.domain.catalogo.ProdutoQueryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/produtos")
public class PublicProdutoController {

    private static final int DEFAULT_LIMIT = 12;
    private static final int MAX_LIMIT = 24;

    private final ProdutoQueryService service;

    public PublicProdutoController(ProdutoQueryService service) {
        this.service = service;
    }

    @GetMapping("/novidades")
    public List<ProductCardVM> novidades(
            @RequestParam(name = "limit", defaultValue = "12") int limit
    ) {
        return service.newArrivals(limitToPageable(limit), false);
    }

    @GetMapping("/para-voce")
    public List<ProductCardVM> paraVoce(
            @RequestParam(name = "limit", defaultValue = "12") int limit
    ) {
        return service.recommended(limitToPageable(limit), false);
    }

    @GetMapping("/mais-vendidos")
    public List<ProductCardVM> maisVendidos(
            @RequestParam(name = "limit", defaultValue = "12") int limit
    ) {
        return service.topSellers(limitToPageable(limit), false);
    }

    private Pageable limitToPageable(int limit) {
        int sanitized = limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
        return PageRequest.of(0, sanitized);
    }
}
