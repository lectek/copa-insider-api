package br.com.lectek.copainsider.adapters.inbound.web.controller.site;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.lectek.copainsider.application.core.tenant.TenantResolverService;
import br.com.lectek.copainsider.application.service.ProductCategorySectionService;
import br.com.lectek.copainsider.application.service.delivery.PublicDeliveryEstimateService;
import br.com.lectek.copainsider.application.view.ProductCardVM;
import br.com.lectek.copainsider.application.view.ProductCategorySectionVM;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomePageController {

    private static final int DEFAULT_SECTION_LIMIT = 6;

    /**
     * Default page size for public product listing without search.
     */
    private static final int DEFAULT_PAGE_SIZE = 12;

    /**
     * Repository used to load available categories.
     */
    private final ProdutoRepository produtoRepository;
    private final ProductCategorySectionService productCategorySectionService;
    private final PublicDeliveryEstimateService deliveryEstimateService;
    @Autowired(required = false)
    private TenantResolverService tenantResolverService;

    /**
     * Creates controller with vitrine dependencies.
     *
     * @param repository product repository
     */
    public HomePageController(
            final ProdutoRepository repository,
            final ProductCategorySectionService productCategorySectionService,
            final PublicDeliveryEstimateService deliveryEstimateService
    ) {
        this.produtoRepository = repository;
        this.productCategorySectionService = productCategorySectionService;
        this.deliveryEstimateService = deliveryEstimateService;
    }

    /**
     * Renders products page when no search query is present.
     *
     * @param cat optional category filter
     * @param pageable paging information
     * @param model view model
     * @return products list page
     */
    @GetMapping(value = "/produtos", params = "!q")
    public String listarProdutos(
            @RequestParam(value = "cat", required = false) final String cat,
            @PageableDefault(
                    size = DEFAULT_PAGE_SIZE,
                    sort = "dataCadastro",
                    direction = Sort.Direction.DESC
            ) final Pageable pageable,
            final Authentication authentication,
            final Model model
    ) {
        final String categoria = normalize(cat);
        final Long tenantId = resolveTenantId();
        final boolean filteredByCategory = categoria != null;
        final Page<ProdutoEntity> page = categoria == null
                ? (tenantId == null
                    ? produtoRepository.searchPublicPage(null, pageable)
                    : produtoRepository.searchPublicPage(tenantId, null, pageable))
                : (tenantId == null
                    ? produtoRepository.searchPublicPageByCategoria(null, categoria, pageable)
                    : produtoRepository.searchPublicPageByCategoria(tenantId, null, categoria, pageable));
        final boolean hasDisponiveis = !page.isEmpty();
        final java.util.List<ProductCategorySectionVM> categorySections =
                filteredByCategory
                        ? java.util.List.of()
                        : productCategorySectionService.loadPublicSections(
                                DEFAULT_SECTION_LIMIT
                        );
        final boolean showCategorySections =
                !filteredByCategory && !categorySections.isEmpty();

        model.addAttribute("destaques", page.getContent());
        model.addAttribute("lista", page.getContent());
        model.addAttribute("hasDisponiveis", hasDisponiveis);
        model.addAttribute("temProdutos", hasDisponiveis);
        model.addAttribute("page", page);
        model.addAttribute("cards", ProductCardVM.fromList(page.getContent()));
        model.addAttribute("q", "");
        model.addAttribute("cat", categoria == null ? "" : categoria);
        model.addAttribute("categorySections", categorySections);
        model.addAttribute("showCategorySections", showCategorySections);
        model.addAttribute(
                "categorias",
                tenantId == null
                        ? produtoRepository.findDistinctCategoriasPublicas()
                        : produtoRepository.findDistinctCategoriasPublicas(tenantId)
        );
        model.addAttribute("active", "produtos");
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
