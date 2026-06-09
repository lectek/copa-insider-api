package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.application.service.CartService;
import br.com.redemaisfarma.application.service.validation.CartValidationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@Validated
public class CartController {

    private static final String SESSION_TENANT_CONTEXT_ID = "tenantContextId";

    /**
     * Service responsible for cart operations.
     */
    private final CartService cartService;

    /**
     * Creates controller with cart service dependency.
     *
     * @param service cart service
     */
    public CartController(final CartService service) {
        this.cartService = service;
    }

    /**
     * Adds an item to the session cart.
     *
     * @param produtoId product id
     * @param quantidade requested quantity
     * @param ra redirect attributes
     * @param session current session
     * @return redirect to cart page
     */
    @PostMapping("/carrinho/adicionar")
    public String adicionar(
            @RequestParam("produtoId") @NotNull final Long produtoId,
            @RequestParam(value = "quantidade", defaultValue = "1")
            @Min(1) final int quantidade,
            @RequestParam(value = "tenantId", required = false) final String tenantId,
            final RedirectAttributes ra,
            final HttpSession session
    ) {
        final CartValidationService.CartValidationResult validation =
                cartService.validateAdd(session, produtoId, quantidade);
        if (!validation.valid()) {
            ra.addFlashAttribute("error", validation.message());
            return redirectToCart(ra, session, tenantId);
        }
        cartService.addItem(session, produtoId, quantidade);
        ra.addFlashAttribute("success", "Produto adicionado ao carrinho.");
        return redirectToCart(ra, session, tenantId);
    }

    /**
     * Updates an existing item quantity in the cart.
     *
     * @param produtoId product id
     * @param quantidade requested quantity
     * @param ra redirect attributes
     * @param session current session
     * @return redirect to cart page
     */
    @PostMapping("/carrinho/atualizar")
    public String atualizar(
            @RequestParam("produtoId") @NotNull final Long produtoId,
            @RequestParam("quantidade") final int quantidade,
            @RequestParam(value = "tenantId", required = false) final String tenantId,
            final RedirectAttributes ra,
            final HttpSession session
    ) {
        cartService.updateItem(session, produtoId, quantidade);
        ra.addFlashAttribute("success", "Carrinho atualizado.");
        return redirectToCart(ra, session, tenantId);
    }

    /**
     * Removes an item from the cart.
     *
     * @param produtoId product id
     * @param ra redirect attributes
     * @param session current session
     * @return redirect to cart page
     */
    @PostMapping("/carrinho/remover")
    public String remover(
            @RequestParam("produtoId") @NotNull final Long produtoId,
            @RequestParam(value = "tenantId", required = false) final String tenantId,
            final RedirectAttributes ra,
            final HttpSession session
    ) {
        cartService.removeItem(session, produtoId);
        ra.addFlashAttribute("success", "Item removido.");
        return redirectToCart(ra, session, tenantId);
    }

    private String redirectToCart(
            final RedirectAttributes ra,
            final HttpSession session,
            final String tenantId
    ) {
        final String normalizedTenantId = normalizeTenantId(tenantId);
        if (!normalizedTenantId.isBlank()) {
            ra.addAttribute("tenantId", normalizedTenantId);
            if (session != null) {
                session.setAttribute(SESSION_TENANT_CONTEXT_ID, normalizedTenantId);
            }
        }
        return "redirect:/carrinho";
    }

    private String normalizeTenantId(final String tenantId) {
        return tenantId == null ? "" : tenantId.trim();
    }
}
