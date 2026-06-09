package br.com.lectek.copainsider.adapters.inbound.web.controller.admin;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoCategoriaRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import br.com.lectek.copainsider.application.core.tenant.TenantResolverService;
import lombok.Generated;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Profile("!test")
@Controller
@RequestMapping("/admin/produtos")
public class ProdutoAdminEditPageController {
    private static final String KEY_ALERTA_ESTOQUE_LIMITE = "app.estoque.alerta.limite";
    private static final int DEFAULT_ALERTA_ESTOQUE_LIMITE = 2;

    /**
     * Repository used to load and update products.
     */
    private final ProdutoRepository repo;

    /**
     * Repository used to resolve category names.
     */
    private final ProdutoCategoriaRepository categoriaRepository;
    private final AppSettingService appSettingService;
    @Autowired(required = false)
    private TenantResolverService tenantResolverService;

    /**
     * Shows product edit page.
     *
     * @param id product id
     * @param model thymeleaf model
     * @return edit page view
     */
    @GetMapping("/{id}/editar/page")
    public String editarView(
            @PathVariable("id") final Long id,
            final Model model
    ) {
        final Long tenantId = resolveTenantId();
        final ProdutoEntity produto = (tenantId == null
                ? this.repo.findById(id)
                : this.repo.findByScopedId(tenantId, id)).orElse(null);
        model.addAttribute("produto", produto);
        model.addAttribute("produtoId", id);
        model.addAttribute("categorias", this.resolveCategorias());
        model.addAttribute("alertaEstoqueLimite", this.resolveAlertaEstoqueLimite());
        return "pages/admin/produtos/editar";
    }

    /**
     * Generates placeholder image when product has no image.
     *
     * @param id product id
     * @param redirectAttributes flash attributes
     * @return redirect path
     */
    @PostMapping("/{id}/imagem/regenerate")
    public String regenerateImage(
            @PathVariable("id") final Long id,
            final RedirectAttributes redirectAttributes
    ) {
        final Long tenantId = resolveTenantId();
        final var produtoOpt = tenantId == null
                ? this.repo.findById(id)
                : this.repo.findByScopedId(tenantId, id);
        return produtoOpt
                .map(entity -> {
                    if (entity.getImagem() == null
                            || entity.getImagem().isBlank()) {
                        entity.setImagem("/images/placeholder.png");
                        this.repo.save(entity);
                        redirectAttributes.addFlashAttribute(
                                "toast",
                                "Imagem definida como placeholder."
                        );
                    } else {
                        redirectAttributes.addFlashAttribute(
                                "toast",
                                "Produto ja possui imagem."
                        );
                    }
                    return "redirect:/admin/produtos/" + id + "/editar/page";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute(
                            "toast",
                            "Produto nao encontrado."
                    );
                    return "redirect:/admin/produtos";
                });
    }

    /**
     * Creates controller with required repositories.
     *
     * @param productRepository product repository
     * @param productCategoryRepository category repository
     */
    @Generated
    public ProdutoAdminEditPageController(
            final ProdutoRepository productRepository,
            final ProdutoCategoriaRepository productCategoryRepository,
            final AppSettingService appSettingService
    ) {
        this.repo = productRepository;
        this.categoriaRepository = productCategoryRepository;
        this.appSettingService = appSettingService;
    }

    /**
     * Resolves category names with default fallback.
     *
     * @return list of category names
     */
    private List<String> resolveCategorias() {
        final List<String> categorias = this.categoriaRepository.findAllNomes();
        if (categorias == null || categorias.isEmpty()) {
            return List.of("Sem Categoria");
        }
        return categorias;
    }

    private int resolveAlertaEstoqueLimite() {
        return Math.max(
                2,
                this.appSettingService.getInt(
                        KEY_ALERTA_ESTOQUE_LIMITE,
                        DEFAULT_ALERTA_ESTOQUE_LIMITE
                )
        );
    }

    private Long resolveTenantId() {
        if (this.tenantResolverService == null) {
            return null;
        }
        return this.tenantResolverService.resolveDefaultTenantId();
    }
}
