package br.com.lectek.copainsider.adapters.inbound.web.controller.site;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.lectek.copainsider.application.core.tenant.TenantResolverService;
import br.com.lectek.copainsider.application.service.delivery.PublicDeliveryEstimateService;
import br.com.lectek.copainsider.application.view.ProductCardVM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/produtos")
public class ProdutosController {

    /**
     * Default page size for public product listing.
     */
    private static final int DEFAULT_PAGE_SIZE = 18;

    /**
     * Repository used to query public products.
     */
    private final ProdutoRepository repo;
    private final PublicDeliveryEstimateService deliveryEstimateService;
    @Autowired(required = false)
    private TenantResolverService tenantResolverService;

    /**
     * Creates controller with product repository dependency.
     *
     * @param repository product repository
     */
    public ProdutosController(
            final ProdutoRepository repository,
            final PublicDeliveryEstimateService deliveryEstimateService
    ) {
        this.repo = repository;
        this.deliveryEstimateService = deliveryEstimateService;
    }

    /**
     * Lists public products when search query parameter is present.
     *
     * @param q search term
     * @param cat category filter
     * @param pageable paging information
     * @param model view model
     * @return product list page
     */
    @GetMapping(params = "q")
    public String listPublic(
            @RequestParam("q") final String q,
            @RequestParam(value = "cat", required = false) final String cat,
            @PageableDefault(
                    size = DEFAULT_PAGE_SIZE,
                    sort = "dataCadastro",
                    direction = Sort.Direction.DESC
            ) final Pageable pageable,
            final Authentication authentication,
            final Model model
    ) {
        final String termo = normalize(q);
        final String categoria = normalize(cat);
        if (termo == null && categoria == null) {
            return "redirect:/produtos";
        }
        final Long tenantId = resolveTenantId();

        final Page<ProdutoEntity> page = categoria == null
                ? (tenantId == null ? repo.searchPublicPage(termo, pageable) : repo.searchPublicPage(tenantId, termo, pageable))
                : (tenantId == null
                    ? repo.searchPublicPageByCategoria(termo, categoria, pageable)
                    : repo.searchPublicPageByCategoria(tenantId, termo, categoria, pageable));

        model.addAttribute("page", page);
        model.addAttribute("cards", ProductCardVM.fromList(page.getContent()));
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("cat", categoria == null ? "" : categoria);
        model.addAttribute("categorias", tenantId == null
                ? repo.findDistinctCategoriasPublicas()
                : repo.findDistinctCategoriasPublicas(tenantId));
        model.addAttribute(
                "deliveryEstimate",
                deliveryEstimateService.estimateFor(authentication)
        );
        return "pages/cliente/produtos/lista";
    }

    private static String normalize(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private Long resolveTenantId() {
        if (this.tenantResolverService == null) {
            return null;
        }
        return this.tenantResolverService.resolveDefaultTenantId();
    }
}
