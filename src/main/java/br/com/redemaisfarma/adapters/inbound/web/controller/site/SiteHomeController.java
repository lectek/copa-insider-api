package br.com.redemaisfarma.adapters.inbound.web.controller.site;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoCategoriaRepository;
import br.com.redemaisfarma.application.core.produto.ProdutoVitrineService;
import br.com.redemaisfarma.application.service.HomeCarouselConfigService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = {"/site"})
public class SiteHomeController {

    /**
     * Maximum number of featured products shown on site home.
     */
    private static final int DESTAQUES_LIMIT = 12;

    /**
     * Service that provides home showcase products.
     */
    private final ProdutoVitrineService vitrine;

    /**
     * Repository used to load available categories.
     */
    private final ProdutoCategoriaRepository categoriaRepository;
    private final HomeCarouselConfigService homeCarouselConfigService;

    /**
     * Creates site home controller.
     *
     * @param service showcase service
     * @param repository category repository
     */
    public SiteHomeController(
            final ProdutoVitrineService service,
            final ProdutoCategoriaRepository repository,
            final HomeCarouselConfigService carouselConfigService
    ) {
        this.vitrine = service;
        this.categoriaRepository = repository;
        this.homeCarouselConfigService = carouselConfigService;
    }

    /**
     * Renders site home page with featured products.
     *
     * @param model view model
     * @return customer home page template
     */
    @GetMapping(value = {"", "/"})
    public String home(final Model model) {
        final List<ProdutoEntity> destaques = vitrine.listarDestaques(
                DESTAQUES_LIMIT
        );
        final int hasDisponiveis = destaques.isEmpty() ? 0 : 1;
        model.addAttribute("destaques", destaques);
        model.addAttribute("hasDisponiveis", hasDisponiveis);
        model.addAttribute(
                "title",
                "RedeMaisFarma - Sua farmacia de confianca"
        );
        model.addAttribute("homeCategorias", this.resolveHomeCategorias());
        model.addAttribute(
                "homeCarouselStyles",
                homeCarouselConfigService.resolveStyles()
        );
        return "pages/cliente/index";
    }

    private List<String> resolveHomeCategorias() {
        final List<String> categorias = categoriaRepository.findAllNomes();
        if (categorias == null || categorias.isEmpty()) {
            return List.of();
        }
        return categorias.stream()
                .map(this::normalizeCategoria)
                .filter(nome -> !nome.isBlank())
                .distinct()
                .limit(8)
                .toList();
    }

    private String normalizeCategoria(final String value) {
        return value == null ? "" : value.trim();
    }
}
