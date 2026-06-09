package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.application.core.tenant.TenantResolverService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin/estoque")
public class AdminEstoqueNiveisController {

    /**
     * Settings key for the default low-stock threshold.
     */
    private static final String KEY_ALERTA_ESTOQUE_LIMITE =
            "app.estoque.alerta.limite";

    /**
     * Fallback threshold when no setting exists.
     */
    private static final int DEFAULT_ALERTA_ESTOQUE_LIMITE = 2;

    /**
     * Minimum allowed threshold value.
     */
    private static final int MIN_ALERTA_ESTOQUE_LIMITE = 2;

    /**
     * Maximum allowed threshold value.
     */
    private static final int MAX_ALERTA_ESTOQUE_LIMITE = 100_000;
    private static final int DEFAULT_PAGE_SIZE = 100;
    private static final int MIN_PAGE_SIZE = 25;
    private static final int MAX_PAGE_SIZE = 200;
    private static final List<Integer> PAGE_SIZE_OPTIONS = List.of(50, 100, 200);

    /**
     * View used by the stock levels page.
     */
    private static final String VIEW = "pages/admin/estoque/niveis";

    /**
     * Redirect used after stock threshold updates.
     */
    private static final String REDIRECT = "redirect:/admin/estoque/niveis";

    /**
     * Repository used to read and update products.
     */
    private final ProdutoRepository produtoRepository;

    /**
     * Settings service used to resolve the global default threshold.
     */
    private final AppSettingService appSettingService;
    @Autowired(required = false)
    private TenantResolverService tenantResolverService;

    /**
     * Creates controller with required dependencies.
     *
     * @param produtoRepositoryValue product repository
     * @param appSettingServiceValue app settings service
     */
    public AdminEstoqueNiveisController(
            final ProdutoRepository produtoRepositoryValue,
            final AppSettingService appSettingServiceValue
    ) {
        this.produtoRepository = produtoRepositoryValue;
        this.appSettingService = appSettingServiceValue;
    }

    /**
     * Renders the stock thresholds page.
     *
     * @param model thymeleaf model
     * @return stock thresholds view
     */
    @GetMapping("/niveis")
    public String listar(
            @RequestParam(name = "page", defaultValue = "0") final Integer page,
            @RequestParam(name = "size", defaultValue = "100") final Integer size,
            final Model model
    ) {
        final int limitePadrao = resolveLimitePadrao();
        final Long tenantId = resolveTenantId();
        final List<ProdutoEntity> produtosEmAlerta =
                produtoRepository.findComEstoqueBaixoScoped(tenantId, limitePadrao);
        final int safePage = Math.max(page == null ? 0 : page.intValue(), 0);
        final int safeSize = resolvePageSize(size);
        final Page<EstoqueNivelItemView> itensPage = produtoRepository.findPaginaNiveisEstoqueScoped(
                        tenantId,
                        limitePadrao,
                        PageRequest.of(safePage, safeSize, Sort.unsorted())
                )
                .map(produto -> EstoqueNivelItemView.from(produto, limitePadrao));
        final long totalItens = itensPage.getTotalElements();
        final long itensInicio = totalItens == 0L ? 0L
                : (long) itensPage.getNumber() * itensPage.getSize() + 1L;
        final long itensFim = totalItens == 0L ? 0L
                : Math.min(itensInicio + itensPage.getNumberOfElements() - 1L, totalItens);

        model.addAttribute("pageTitle", "Niveis de estoque");
        model.addAttribute("active", "estoque");
        model.addAttribute("limitePadrao", limitePadrao);
        model.addAttribute(
                "alertaEstoqueBaixoTotal",
                produtosEmAlerta == null ? 0 : produtosEmAlerta.size()
        );
        model.addAttribute("itensPage", itensPage);
        model.addAttribute("itens", itensPage.getContent());
        model.addAttribute("paginaAtual", itensPage.getNumber());
        model.addAttribute("tamanhoPagina", itensPage.getSize());
        model.addAttribute("totalPaginas", itensPage.getTotalPages());
        model.addAttribute("totalItens", totalItens);
        model.addAttribute("itensInicio", itensInicio);
        model.addAttribute("itensFim", itensFim);
        model.addAttribute("hasPrev", itensPage.hasPrevious());
        model.addAttribute("hasNext", itensPage.hasNext());
        model.addAttribute("tamanhosPagina", PAGE_SIZE_OPTIONS);
        return VIEW;
    }

    /**
     * Updates one product-specific low-stock threshold.
     *
     * @param id product id
     * @param minimo threshold value or null to revert to the global default
     * @param ra redirect attributes
     * @return redirect to stock thresholds page
     */
    @PostMapping("/{id}/minimo")
    public String atualizarMinimo(
            @PathVariable("id") final Long id,
            @RequestParam(name = "minimo", required = false) final Integer minimo,
            @RequestParam(name = "page", required = false) final Integer page,
            @RequestParam(name = "size", required = false) final Integer size,
            final RedirectAttributes ra
    ) {
        final Long tenantId = resolveTenantId();
        return (tenantId == null
                ? produtoRepository.findById(id)
                : produtoRepository.findByScopedId(tenantId, id))
                .map(produto -> {
                    final String nome = resolveNome(produto);
                    if (minimo == null) {
                        produto.setAlertaEstoqueLimite(null);
                        produtoRepository.save(produto);
                        ra.addFlashAttribute(
                                "success",
                                "Limite personalizado removido para " + nome + "."
                        );
                        return buildRedirect(page, size);
                    }

                    final int safeMinimo = Math.clamp(
                            minimo,
                            MIN_ALERTA_ESTOQUE_LIMITE,
                            MAX_ALERTA_ESTOQUE_LIMITE
                    );
                    produto.setAlertaEstoqueLimite(safeMinimo);
                    produtoRepository.save(produto);
                    ra.addFlashAttribute(
                            "success",
                            "Limite minimo atualizado para " + nome + ": "
                                    + safeMinimo + "."
                    );
                    return buildRedirect(page, size);
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("warning", "Produto nao encontrado.");
                    return buildRedirect(page, size);
                });
    }

    private Long resolveTenantId() {
        if (this.tenantResolverService == null) {
            return null;
        }
        return this.tenantResolverService.resolveDefaultTenantId();
    }

    private int resolveLimitePadrao() {
        return Math.max(
                MIN_ALERTA_ESTOQUE_LIMITE,
                appSettingService.getInt(
                        KEY_ALERTA_ESTOQUE_LIMITE,
                        DEFAULT_ALERTA_ESTOQUE_LIMITE
                )
        );
    }

    private static int resolvePageSize(final Integer size) {
        final int requested = size == null ? DEFAULT_PAGE_SIZE : size.intValue();
        return Math.clamp(requested, MIN_PAGE_SIZE, MAX_PAGE_SIZE);
    }

    private static String buildRedirect(final Integer page, final Integer size) {
        if (page == null && size == null) {
            return REDIRECT;
        }
        final int safePage = Math.max(page == null ? 0 : page.intValue(), 0);
        final int safeSize = resolvePageSize(size);
        return REDIRECT + "?page=" + safePage + "&size=" + safeSize;
    }

    private static String resolveNome(final ProdutoEntity produto) {
        if (produto == null || produto.getNome() == null
                || produto.getNome().isBlank()) {
            return "produto";
        }
        return produto.getNome().trim();
    }

    /**
     * View model for one stock threshold row.
     *
     * @param id product id
     * @param nome product name
     * @param estoque current stock quantity
     * @param minimo effective threshold
     * @param critico whether stock is at or below threshold
     * @param personalizado whether the threshold is product-specific
     */
    public record EstoqueNivelItemView(
            Long id,
            String nome,
            int estoque,
            int minimo,
            boolean critico,
            boolean personalizado
    ) {

        /**
         * Maps one product to the page view model.
         *
         * @param produto product entity
         * @param limitePadrao default threshold from settings
         * @return stock threshold view model
         */
        static EstoqueNivelItemView from(
                final ProdutoEntity produto,
                final int limitePadrao
        ) {
            final int estoqueAtual = Math.max(
                    0,
                    produto.getEstoque() == null ? 0 : produto.getEstoque()
            );
            final Integer limiteCustomizado = produto.getAlertaEstoqueLimite();
            final boolean personalizado = limiteCustomizado != null;
            final int minimo;
            if (limiteCustomizado == null) {
                minimo = limitePadrao;
            } else {
                minimo = Math.clamp(
                        limiteCustomizado.intValue(),
                        MIN_ALERTA_ESTOQUE_LIMITE,
                        MAX_ALERTA_ESTOQUE_LIMITE
                );
            }

            return new EstoqueNivelItemView(
                    produto.getId(),
                    resolveNome(produto),
                    estoqueAtual,
                    minimo,
                    estoqueAtual > 0 && estoqueAtual < minimo,
                    personalizado
            );
        }
    }
}
