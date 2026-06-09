package br.com.lectek.copainsider.application.controller.admin;

import br.com.lectek.copainsider.application.view.admin.GatewayConfigForm;
import br.com.lectek.copainsider.domain.financeiro.config.GatewayConfig;
import br.com.lectek.copainsider.domain.financeiro.config.GatewayConfigService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin/financeiro")
public class FinanceiroGatewayAdminController {

    private static final String VIEW_PREFIX = "pages/admin/financeiro/";
    private final GatewayConfigService service;

    public FinanceiroGatewayAdminController(GatewayConfigService service) {
        this.service = service;
    }

    @GetMapping("/gateways")
    public String editar(@RequestParam(name = "fornecedor") Optional<String> fornecedor, Model model) {
        String provedor = fornecedor.orElse("pagarme");
        GatewayConfigForm form = service.buscarAtivaPorProvedor(provedor)
                .map(GatewayConfigForm::fromEntity)
                .orElseGet(() -> {
                    GatewayConfigForm f = new GatewayConfigForm();
                    f.setProvedor(provedor);
                    f.setAtivo(Boolean.TRUE);
                    return f;
                });

        model.addAttribute("pageTitle", "Financeiro • Gateways");
        model.addAttribute("active", "financeiro");
        model.addAttribute("gw", form);
        return VIEW_PREFIX + "gateways";
    }

    @PostMapping("/gateways")
    public String salvar(@Valid @ModelAttribute("gw") GatewayConfigForm form,
                         BindingResult br,
                         Model model,
                         RedirectAttributes ra) {
        if (br.hasErrors()) {
            model.addAttribute("pageTitle", "Financeiro • Gateways");
            model.addAttribute("active", "financeiro");
            return VIEW_PREFIX + "gateways";
        }
        GatewayConfig toSave = form.toEntity();
        GatewayConfig saved = (toSave.getId() == null)
                ? service.criar(toSave)
                : service.atualizar(toSave.getId(), toSave);

        ra.addFlashAttribute("success", "Credenciais do gateway salvas com sucesso.");
        return "redirect:/admin/financeiro/gateways?fornecedor=" + saved.getProvedor();
    }
}
