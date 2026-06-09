package br.com.lectek.copainsider.adapters.inbound.web.controller.site;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.lectek.copainsider.application.core.tenant.TenantResolverService;
import br.com.lectek.copainsider.application.service.delivery.PublicDeliveryEstimateService;
import br.com.lectek.copainsider.application.view.ProductCardVM;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class ProdutoDetalheController {

    /**
     * Repository used to load product detail.
     */
    private final ProdutoRepository produtoRepository;
    private final PublicDeliveryEstimateService deliveryEstimateService;
    @Autowired(required = false)
    private TenantResolverService tenantResolverService;

    /**
     * Creates controller for product detail.
     *
     * @param repository product repository
     */
    public ProdutoDetalheController(
            final ProdutoRepository repository,
            final PublicDeliveryEstimateService deliveryEstimateService
    ) {
        this.produtoRepository = repository;
        this.deliveryEstimateService = deliveryEstimateService;
    }

    /**
     * Renders customer product detail page.
     *
     * @param id product id
     * @param model view model
     * @return detail page template
     */
    @GetMapping({"/produto/{id}", "/produtos/{id}"})
    public String detalhe(
            @PathVariable("id") final Long id,
            final Authentication authentication,
            final Model model
    ) {
        final Long tenantId = resolveTenantId();
        final ProdutoEntity produto = (tenantId == null
                ? produtoRepository.findPublicById(id)
                : produtoRepository.findPublicById(tenantId, id))
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
                );
        final boolean outOfStock = produto.getEstoque() != null
                && produto.getEstoque() <= 0;
        model.addAttribute("produto", produto);
        model.addAttribute("outOfStock", outOfStock);
        model.addAttribute("exigeReceita", produto.getExigeReceita());
        model.addAttribute("tarjaMedicacao", produto.getTarjaMedicacao());
        model.addAttribute("tarjaDescricao", produto.getTarjaMedicacao() == null
                ? null
                : produto.getTarjaMedicacao().getDescricaoRegulatoria());
        model.addAttribute(
                "deliveryEstimate",
                deliveryEstimateService.estimateFor(authentication)
        );
        model.addAttribute("relatedCards", loadRelatedCards(tenantId, produto));
        return "pages/cliente/produtos/detalhe";
    }

    private List<ProductCardVM> loadRelatedCards(final Long tenantId, final ProdutoEntity produto) {
        final int limit = 6;
        final List<ProdutoEntity> candidates = new ArrayList<>();

        if (produto.getCategoria() != null && !produto.getCategoria().isBlank()) {
            candidates.addAll((tenantId == null
                    ? produtoRepository.searchPublicPageByCategoria(
                        null,
                        produto.getCategoria(),
                        PageRequest.of(0, limit + 1))
                    : produtoRepository.searchPublicPageByCategoria(
                        tenantId,
                        null,
                        produto.getCategoria(),
                        PageRequest.of(0, limit + 1))).getContent());
        }

        return ProductCardVM.fromList(
                candidates.stream()
                        .filter(item -> item.getId() != null
                                && !item.getId().equals(produto.getId()))
                        .limit(limit)
                        .toList()
        );
    }

    private Long resolveTenantId() {
        if (this.tenantResolverService == null) {
            return null;
        }
        return this.tenantResolverService.resolveDefaultTenantId();
    }
}
