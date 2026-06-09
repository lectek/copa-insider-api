package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoCategoriaEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoCategoriaRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import lombok.Generated;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Profile("!test")
@Controller
@RequestMapping("/admin/produtos/categorias")
public class ProdutoCategoriaAdminController {

    private final ProdutoCategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categoria", new ProdutoCategoriaEntity());
        model.addAttribute("categorias", this.categoriaRepository.findAll(Sort.by("nome").ascending()));
        return "pages/admin/produtos/categorias";
    }

    @PostMapping
    public String criar(@ModelAttribute("categoria") ProdutoCategoriaEntity categoria, RedirectAttributes ra) {
        String nome = normalize(categoria.getNome());
        if (nome.isBlank()) {
            ra.addFlashAttribute("toast", "Informe o nome da categoria.");
            return "redirect:/admin/produtos/categorias";
        }
        if (this.categoriaRepository.existsByNomeIgnoreCase(nome)) {
            ra.addFlashAttribute("toast", "Categoria ja existe.");
            return "redirect:/admin/produtos/categorias";
        }
        categoria.setNome(nome);
        this.categoriaRepository.save(categoria);
        ra.addFlashAttribute("toast", "Categoria adicionada.");
        return "redirect:/admin/produtos/categorias";
    }

    @PostMapping("/{id}/atualizar")
    public String atualizar(@PathVariable("id") Long id, @RequestParam("nome") String nome, RedirectAttributes ra) {
        String nomeNormalizado = normalize(nome);
        if (nomeNormalizado.isBlank()) {
            ra.addFlashAttribute("toast", "Informe o nome da categoria.");
            return "redirect:/admin/produtos/categorias";
        }
        Optional<ProdutoCategoriaEntity> existente = this.categoriaRepository.findById(id);
        if (existente.isEmpty()) {
            ra.addFlashAttribute("toast", "Categoria nao encontrada.");
            return "redirect:/admin/produtos/categorias";
        }
        Optional<ProdutoCategoriaEntity> conflito = this.categoriaRepository.findByNomeIgnoreCase(nomeNormalizado);
        if (conflito.isPresent() && !conflito.get().getId().equals(id)) {
            ra.addFlashAttribute("toast", "Ja existe outra categoria com esse nome.");
            return "redirect:/admin/produtos/categorias";
        }
        ProdutoCategoriaEntity entity = existente.get();
        entity.setNome(nomeNormalizado);
        this.categoriaRepository.save(entity);
        ra.addFlashAttribute("toast", "Categoria atualizada.");
        return "redirect:/admin/produtos/categorias";
    }

    @PostMapping("/{id}/remover")
    public String remover(@PathVariable("id") Long id, RedirectAttributes ra) {
        Optional<ProdutoCategoriaEntity> existente = this.categoriaRepository.findById(id);
        if (existente.isEmpty()) {
            ra.addFlashAttribute("toast", "Categoria nao encontrada.");
            return "redirect:/admin/produtos/categorias";
        }
        ProdutoCategoriaEntity categoria = existente.get();
        long usados = this.produtoRepository.countByCategoriaIgnoreCase(categoria.getNome());
        if (usados > 0) {
            ra.addFlashAttribute("toast", "Categoria em uso por " + usados + " produtos.");
            return "redirect:/admin/produtos/categorias";
        }
        this.categoriaRepository.delete(categoria);
        ra.addFlashAttribute("toast", "Categoria removida.");
        return "redirect:/admin/produtos/categorias";
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    @Generated
    public ProdutoCategoriaAdminController(ProdutoCategoriaRepository categoriaRepository,
                                           ProdutoRepository produtoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.produtoRepository = produtoRepository;
    }
}
