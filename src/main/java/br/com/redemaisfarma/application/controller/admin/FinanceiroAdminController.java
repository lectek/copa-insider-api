package br.com.redemaisfarma.application.controller.admin;

import br.com.redemaisfarma.application.view.AssinaturaView;
import br.com.redemaisfarma.domain.financeiro.FinanceiroAdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/admin/financeiro")
public class FinanceiroAdminController {

    private static final Logger log = LoggerFactory.getLogger(FinanceiroAdminController.class);
    private static final String VIEW_PREFIX = "pages/admin/financeiro/";
    private final FinanceiroAdminService service;

    public FinanceiroAdminController(FinanceiroAdminService service) {
        this.service = service;
    }

    @GetMapping("/assinaturas")
    public String listar(Model model) {
        model.addAttribute("pageTitle", "Financeiro • Assinaturas");
        model.addAttribute("active", "financeiro");
        model.addAttribute("assinaturas", service.listarAssinaturas());
        return VIEW_PREFIX + "assinaturas";
    }

    @GetMapping("/assinaturas/{id}")
    public String detalhe(@PathVariable("id") Long id, Model model) {
        Optional<AssinaturaView> assinatura = service.buscarAssinatura(id);
        if (assinatura.isEmpty()) {
            log.warn("Assinatura id={} não encontrada. Redirecionando para lista.", id);
            return "redirect:/admin/financeiro/assinaturas";
        }
        AssinaturaView a = assinatura.get();
        model.addAttribute("pageTitle", "Assinatura • " + a.clienteNome());
        model.addAttribute("active", "financeiro");
        model.addAttribute("assinatura", a);
        return VIEW_PREFIX + "assinaturas-detalhe";
    }
}
