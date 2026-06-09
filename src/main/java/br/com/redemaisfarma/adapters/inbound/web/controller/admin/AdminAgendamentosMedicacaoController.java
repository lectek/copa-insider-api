package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.application.core.tenant.TenantFeature;
import br.com.redemaisfarma.application.core.tenant.TenantFeatureGateService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/agendamentos")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAgendamentosMedicacaoController {

    private static final String REDIRECT_ADMIN_HOME = "redirect:/admin";
    private final TenantFeatureGateService tenantFeatureGateService;

    public AdminAgendamentosMedicacaoController(
            final TenantFeatureGateService tenantFeatureGateServiceValue
    ) {
        this.tenantFeatureGateService = tenantFeatureGateServiceValue;
    }

    @GetMapping("/medicacao")
    public String medicacao(final RedirectAttributes redirectAttributes) {
        if (!tenantFeatureGateService.isEnabledForCurrentTenant(
                TenantFeature.MOD_RECEITA_CONTROLADA,
                false
        )) {
            redirectAttributes.addFlashAttribute(
                    "warning",
                    "Modulo de medicacao/receita controlada desativado para esta empresa."
            );
            return REDIRECT_ADMIN_HOME;
        }
        return "pages/admin/agendamentos/medicacao";
    }
}
