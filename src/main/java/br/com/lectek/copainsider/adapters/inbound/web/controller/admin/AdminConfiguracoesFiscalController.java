package br.com.lectek.copainsider.adapters.inbound.web.controller.admin;

import br.com.lectek.copainsider.application.service.fiscal.FiscalDocumentService;
import br.com.lectek.copainsider.application.service.fiscal.FiscalEmitterConfigService;
import br.com.lectek.copainsider.domain.fiscal.FiscalEnvironment;
import br.com.lectek.copainsider.domain.fiscal.FiscalProvider;
import br.com.lectek.copainsider.domain.fiscal.FiscalTaxRegime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/configuracoes/fiscal")
@PreAuthorize("hasRole('ADMIN')")
public class AdminConfiguracoesFiscalController {

    private static final int RECENT_DOCUMENT_LIMIT = 12;

    private final FiscalEmitterConfigService fiscalEmitterConfigService;
    private final FiscalDocumentService fiscalDocumentService;

    public AdminConfiguracoesFiscalController(
            final FiscalEmitterConfigService fiscalEmitterConfigServiceValue,
            final FiscalDocumentService fiscalDocumentServiceValue
    ) {
        this.fiscalEmitterConfigService = fiscalEmitterConfigServiceValue;
        this.fiscalDocumentService = fiscalDocumentServiceValue;
    }

    @GetMapping
    public String form(final Model model) {
        final FiscalEmitterConfigService.FiscalEmitterConfig config =
                fiscalEmitterConfigService.load(FiscalProvider.FOCUS_NFE);
        model.addAttribute("cfg", FiscalConfigForm.from(config));
        model.addAttribute("providers", FiscalProvider.values());
        model.addAttribute("environments", FiscalEnvironment.values());
        model.addAttribute("taxRegimes", FiscalTaxRegime.values());
        model.addAttribute(
                "recentDocuments",
                fiscalDocumentService.listRecent(RECENT_DOCUMENT_LIMIT)
        );
        return "pages/admin/configuracoes/fiscal";
    }

    @PostMapping
    public String salvar(
            @ModelAttribute("cfg") final FiscalConfigForm form,
            final RedirectAttributes redirectAttributes
    ) {
        try {
            fiscalEmitterConfigService.save(
                    form.getProvider() == null
                            ? FiscalProvider.FOCUS_NFE
                            : form.getProvider(),
                    form.toInput()
            );
            redirectAttributes.addFlashAttribute(
                    "success",
                    "Configuracao fiscal atualizada."
            );
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/configuracoes/fiscal";
    }

    @Getter
    @Setter
    public static final class FiscalConfigForm {

        private FiscalProvider provider;
        private boolean enabled;
        private FiscalEnvironment environment;
        private String companyLegalName;
        private String companyTradeName;
        private String cnpj;
        private String stateRegistration;
        private String municipalRegistration;
        private FiscalTaxRegime taxRegime;
        private String apiBaseUrl;
        private String webhookUrl;
        private Integer nfeSeries;
        private Integer nfceSeries;
        private String apiToken;
        private boolean clearApiToken;
        private String cscId;
        private String csc;
        private boolean clearCsc;
        private String certificateBase64;
        private String certificatePassword;
        private boolean clearCertificate;
        private boolean apiTokenConfigured;
        private boolean cscConfigured;
        private boolean certificateConfigured;

        static FiscalConfigForm from(
                final FiscalEmitterConfigService.FiscalEmitterConfig config
        ) {
            final FiscalConfigForm form = new FiscalConfigForm();
            form.setProvider(config.provider());
            form.setEnabled(config.enabled());
            form.setEnvironment(config.environment());
            form.setCompanyLegalName(config.companyLegalName());
            form.setCompanyTradeName(config.companyTradeName());
            form.setCnpj(config.cnpj());
            form.setStateRegistration(config.stateRegistration());
            form.setMunicipalRegistration(config.municipalRegistration());
            form.setTaxRegime(config.taxRegime());
            form.setApiBaseUrl(config.apiBaseUrl());
            form.setWebhookUrl(config.webhookUrl());
            form.setNfeSeries(config.nfeSeries());
            form.setNfceSeries(config.nfceSeries());
            form.setApiTokenConfigured(config.apiTokenConfigured());
            form.setCscId(config.cscId());
            form.setCscConfigured(config.cscConfigured());
            form.setCertificateConfigured(config.certificateConfigured());
            return form;
        }

        FiscalEmitterConfigService.FiscalEmitterConfigInput toInput() {
            return new FiscalEmitterConfigService.FiscalEmitterConfigInput(
                    enabled,
                    environment,
                    companyLegalName,
                    companyTradeName,
                    cnpj,
                    stateRegistration,
                    municipalRegistration,
                    taxRegime,
                    apiBaseUrl,
                    webhookUrl,
                    nfeSeries,
                    nfceSeries,
                    apiToken,
                    clearApiToken,
                    cscId,
                    csc,
                    clearCsc,
                    certificateBase64,
                    certificatePassword,
                    clearCertificate
            );
        }
    }
}
