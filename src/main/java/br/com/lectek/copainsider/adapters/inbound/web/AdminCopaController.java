package br.com.lectek.copainsider.adapters.inbound.web;

import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.CopaAcessoJPARepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.CopaCompraJPARepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.CopaProdutoJPARepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/copa")
public class AdminCopaController {

    private static final int PAGE_SIZE = 30;

    private final CopaProdutoJPARepository produtoRepo;
    private final CopaCompraJPARepository  compraRepo;
    private final CopaAcessoJPARepository  acessoRepo;

    public AdminCopaController(
            CopaProdutoJPARepository produtoRepo,
            CopaCompraJPARepository  compraRepo,
            CopaAcessoJPARepository  acessoRepo) {
        this.produtoRepo = produtoRepo;
        this.compraRepo  = compraRepo;
        this.acessoRepo  = acessoRepo;
    }

    // ── Produtos ─────────────────────────────────────────────────────────────

    @GetMapping("/produtos")
    public String produtos(Model model) {
        model.addAttribute("produtos", produtoRepo.findAll(Sort.by("ordem")));
        return "pages/admin/copa/produtos";
    }

    @PostMapping("/produtos/{id}/toggle-ativo")
    public String toggleAtivo(
            @PathVariable Long id,
            RedirectAttributes ra) {
        produtoRepo.findById(id).ifPresentOrElse(p -> {
            p.setAtivo(!p.isAtivo());
            produtoRepo.save(p);
            ra.addFlashAttribute("success",
                    "Produto \"" + nome(p.getNomePtPt(), p.getSlug()) + "\" "
                    + (p.isAtivo() ? "ativado" : "desativado") + ".");
        }, () -> ra.addFlashAttribute("error", "Produto não encontrado."));
        return "redirect:/admin/copa/produtos";
    }

    @PostMapping("/produtos/{id}/hotmart-url")
    public String atualizarHotmartUrl(
            @PathVariable Long id,
            @RequestParam String hotmartUrl,
            RedirectAttributes ra) {
        produtoRepo.findById(id).ifPresentOrElse(p -> {
            p.setHotmartUrl(hotmartUrl.isBlank() ? null : hotmartUrl.trim());
            produtoRepo.save(p);
            ra.addFlashAttribute("success", "URL Hotmart actualizada.");
        }, () -> ra.addFlashAttribute("error", "Produto não encontrado."));
        return "redirect:/admin/copa/produtos";
    }

    // ── Compras ───────────────────────────────────────────────────────────────

    @GetMapping("/compras")
    public String compras(
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        var pg = compraRepo.findAllByOrderByCriadoEmDesc(PageRequest.of(page, PAGE_SIZE));
        model.addAttribute("compras",    pg.getContent());
        model.addAttribute("pagina",     pg);
        model.addAttribute("paginaAtual", page);
        return "pages/admin/copa/compras";
    }

    // ── Acessos ───────────────────────────────────────────────────────────────

    @GetMapping("/acessos")
    public String acessos(
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        var pageable = PageRequest.of(page, PAGE_SIZE);
        var pg = (email != null && !email.isBlank())
                ? acessoRepo.findByEmailContainingIgnoreCaseOrderByConcedidoEmDesc(email.trim(), pageable)
                : acessoRepo.findAllByOrderByConcedidoEmDesc(pageable);
        model.addAttribute("acessos",    pg.getContent());
        model.addAttribute("pagina",     pg);
        model.addAttribute("paginaAtual", page);
        model.addAttribute("emailFiltro", email != null ? email : "");
        return "pages/admin/copa/acessos";
    }

    @PostMapping("/acessos/{id}/revogar")
    public String revogarAcesso(
            @PathVariable Long id,
            @RequestParam(required = false) String email,
            RedirectAttributes ra) {
        acessoRepo.findById(id).ifPresentOrElse(a -> {
            acessoRepo.deleteById(id);
            ra.addFlashAttribute("success",
                    "Acesso de " + a.getEmail() + " ao produto \"" + a.getProdutoSlug() + "\" revogado.");
        }, () -> ra.addFlashAttribute("error", "Acesso não encontrado."));
        var redirect = "redirect:/admin/copa/acessos";
        if (email != null && !email.isBlank()) {
            redirect += "?email=" + email;
        }
        return redirect;
    }

    private static String nome(String nomePtPt, String slug) {
        return (nomePtPt != null && !nomePtPt.isBlank()) ? nomePtPt : slug;
    }
}
