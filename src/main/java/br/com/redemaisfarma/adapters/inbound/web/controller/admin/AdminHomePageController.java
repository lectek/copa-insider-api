package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.AppSettingEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ProdutoJpaRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.AppSettingRepository;
import br.com.redemaisfarma.application.core.tenant.TenantResolverService;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/home")
@PreAuthorize("hasRole('ADMIN')")
public class AdminHomePageController {

    /**
     * Key storing the selected main product id for home page.
     */
    private static final String MAIN_PRODUCT_KEY = "HOME.main_product_id";

    /**
     * Description stored alongside main product setting.
     */
    private static final String MAIN_PRODUCT_DESCRIPTION =
            "Produto principal da home";

    /**
     * Number of products listed in selector.
     */
    private static final int HOME_PRODUCTS_PAGE_SIZE = 200;

    /**
     * Repository used for product lookups.
     */
    private final ProdutoJpaRepository produtoRepository;

    /**
     * Repository used for app settings.
     */
    private final AppSettingRepository settingRepository;
    @Autowired(required = false)
    private TenantResolverService tenantResolverService;

    /**
     * Creates controller with required dependencies.
     *
     * @param produtoRepo product repository
     * @param appSettingRepository app setting repository
     */
    public AdminHomePageController(
            final ProdutoJpaRepository produtoRepo,
            final AppSettingRepository appSettingRepository
    ) {
        this.produtoRepository = produtoRepo;
        this.settingRepository = appSettingRepository;
    }

    /**
     * Shows the main product selection page.
     *
     * @param model thymeleaf model
     * @return view name
     */
    @GetMapping("/produto-principal")
    public String form(final Model model) {
        final Long tenantId = resolveTenantId();
        final var page = PageRequest.of(
                0,
                HOME_PRODUCTS_PAGE_SIZE,
                Sort.by("nome").ascending()
        );
        final List<ProdutoOption> produtos = (tenantId == null
                ? produtoRepository.findAll(page)
                : produtoRepository.findByTenantId(tenantId, page))
                .getContent()
                .stream()
                .map(ProdutoOption::from)
                .toList();

        final String selecionado =
                settingRepository.findBySettingKey(MAIN_PRODUCT_KEY)
                .map(AppSettingEntity::getSettingValue)
                .orElse("");

        model.addAttribute("produtos", produtos);
        model.addAttribute("selectedId", selecionado);
        return "pages/admin/home-produto-principal";
    }

    /**
     * Updates the selected main product.
     *
     * @param produtoId product id as string
     * @param redirectAttributes flash attributes
     * @return redirect path
     */
    @PostMapping("/produto-principal")
    public String salvar(
            @RequestParam(name = "produtoId", required = false)
            final String produtoId,
            final RedirectAttributes redirectAttributes
    ) {
        final String id = produtoId == null ? "" : produtoId.trim();
        if (id.isBlank()) {
            settingRepository.findBySettingKey(MAIN_PRODUCT_KEY)
                    .ifPresent(settingRepository::delete);
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Produto principal removido."
            );
            return "redirect:/admin/home/produto-principal";
        }

        try {
            final long parsed = Long.parseLong(id);
            if (parsed <= 0L) {
                redirectAttributes.addFlashAttribute(
                        "error",
                        "Informe um produto valido."
                );
                return "redirect:/admin/home/produto-principal";
            }
        } catch (NumberFormatException ex) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Informe um produto valido."
            );
            return "redirect:/admin/home/produto-principal";
        }

        final AppSettingEntity entity =
                settingRepository.findBySettingKey(MAIN_PRODUCT_KEY)
                        .orElseGet(() -> new AppSettingEntity(
                                MAIN_PRODUCT_KEY,
                                id,
                                MAIN_PRODUCT_DESCRIPTION
                        ));
        entity.setSettingValue(id);
        entity.setDescription(MAIN_PRODUCT_DESCRIPTION);
        settingRepository.save(entity);

        redirectAttributes.addFlashAttribute(
                "success",
                "Produto principal atualizado."
        );
        return "redirect:/admin/home/produto-principal";
    }

    private Long resolveTenantId() {
        if (this.tenantResolverService == null) {
            return null;
        }
        return this.tenantResolverService.resolveDefaultTenantId();
    }

    /**
     * Product option rendered in home selector.
     *
     * @param id product id
     * @param nome product name
     * @param preco product price
     * @param disponivel product availability
     * @param imagem product image
     */
    public record ProdutoOption(
            @NotNull Long id,
            String nome,
            BigDecimal preco,
            boolean disponivel,
            String imagem
    ) {
        /**
         * Builds option from product entity.
         *
         * @param produto product entity
         * @return option view model
         */
        static ProdutoOption from(final ProdutoEntity produto) {
            return new ProdutoOption(
                    produto.getId(),
                    produto.getNome(),
                    produto.getPrecoVenda(),
                    Boolean.TRUE.equals(produto.getDisponivel()),
                    produto.getImagem()
            );
        }
    }
}
