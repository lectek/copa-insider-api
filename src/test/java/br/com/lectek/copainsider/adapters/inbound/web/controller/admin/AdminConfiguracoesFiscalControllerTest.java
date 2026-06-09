package br.com.lectek.copainsider.adapters.inbound.web.controller.admin;

import br.com.lectek.copainsider.application.service.fiscal.FiscalDocumentService;
import br.com.lectek.copainsider.application.service.fiscal.FiscalEmitterConfigService;
import br.com.lectek.copainsider.domain.fiscal.FiscalDocumentModel;
import br.com.lectek.copainsider.domain.fiscal.FiscalDocumentStatus;
import br.com.lectek.copainsider.domain.fiscal.FiscalEnvironment;
import br.com.lectek.copainsider.domain.fiscal.FiscalProvider;
import br.com.lectek.copainsider.domain.fiscal.FiscalTaxRegime;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminConfiguracoesFiscalControllerTest {

    @Mock
    private FiscalEmitterConfigService fiscalEmitterConfigService;

    @Mock
    private FiscalDocumentService fiscalDocumentService;

    private AdminConfiguracoesFiscalController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminConfiguracoesFiscalController(
                fiscalEmitterConfigService,
                fiscalDocumentService
        );
    }

    @Test
    void formLoadsConfigAndRecentDocuments() {
        when(fiscalEmitterConfigService.load(FiscalProvider.FOCUS_NFE))
                .thenReturn(new FiscalEmitterConfigService.FiscalEmitterConfig(
                        FiscalProvider.FOCUS_NFE,
                        true,
                        FiscalEnvironment.PRODUCAO,
                        "Rede Mais Farma LTDA",
                        "Rede Mais Farma",
                        "12345678000199",
                        "123456789",
                        "",
                        FiscalTaxRegime.SIMPLES_NACIONAL,
                        "https://api.focusnfe.com.br",
                        "https://copainsider.com/webhooks/fiscal",
                        1,
                        2,
                        true,
                        "000001",
                        true,
                        false
                ));
        when(fiscalDocumentService.listRecent(12)).thenReturn(List.of(
                new FiscalDocumentService.FiscalDocumentSummary(
                        1L,
                        10L,
                        "FISC-001",
                        FiscalDocumentModel.NFCE_65,
                        FiscalDocumentStatus.SUBMITTED,
                        BigDecimal.TEN,
                        LocalDateTime.now().minusMinutes(10),
                        LocalDateTime.now()
                )
        ));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.form(model);

        Assertions.assertThat(view)
                .isEqualTo("pages/admin/configuracoes/fiscal");
        Assertions.assertThat(model.get("recentDocuments"))
                .isInstanceOf(List.class);
        Assertions.assertThat((List<?>) model.get("recentDocuments"))
                .hasSize(1);
        AdminConfiguracoesFiscalController.FiscalConfigForm form =
                (AdminConfiguracoesFiscalController.FiscalConfigForm) model.get("cfg");
        Assertions.assertThat(form).isNotNull();
        Assertions.assertThat(form.getProvider()).isEqualTo(FiscalProvider.FOCUS_NFE);
        Assertions.assertThat(form.isEnabled()).isTrue();
        Assertions.assertThat(form.isApiTokenConfigured()).isTrue();
        Assertions.assertThat(form.getNfceSeries()).isEqualTo(2);
    }

    @Test
    void salvarPersistsConfigurationAndRedirectsWithSuccess() {
        AdminConfiguracoesFiscalController.FiscalConfigForm form =
                new AdminConfiguracoesFiscalController.FiscalConfigForm();
        form.setProvider(FiscalProvider.FOCUS_NFE);
        form.setEnabled(true);
        form.setEnvironment(FiscalEnvironment.HOMOLOGACAO);
        form.setCompanyLegalName("Rede Mais Farma LTDA");
        form.setCompanyTradeName("Rede Mais Farma");
        form.setCnpj("12.345.678/0001-99");
        form.setStateRegistration("123456789");
        form.setTaxRegime(FiscalTaxRegime.SIMPLES_NACIONAL);
        form.setApiBaseUrl("https://homologacao.focusnfe.com.br");
        form.setWebhookUrl("https://copainsider.com/webhooks/fiscal");
        form.setNfeSeries(1);
        form.setNfceSeries(1);
        form.setApiToken("token");
        form.setCscId("000001");
        form.setCsc("csc");
        RedirectAttributesModelMap redirectAttributes =
                new RedirectAttributesModelMap();

        when(fiscalEmitterConfigService.save(
                eq(FiscalProvider.FOCUS_NFE),
                any(FiscalEmitterConfigService.FiscalEmitterConfigInput.class)
        )).thenReturn(new FiscalEmitterConfigService.FiscalEmitterConfig(
                FiscalProvider.FOCUS_NFE,
                true,
                FiscalEnvironment.HOMOLOGACAO,
                "Rede Mais Farma LTDA",
                "Rede Mais Farma",
                "12345678000199",
                "123456789",
                "",
                FiscalTaxRegime.SIMPLES_NACIONAL,
                "https://homologacao.focusnfe.com.br",
                "https://copainsider.com/webhooks/fiscal",
                1,
                1,
                true,
                "000001",
                true,
                false
        ));

        String redirect = controller.salvar(form, redirectAttributes);

        Assertions.assertThat(redirect)
                .isEqualTo("redirect:/admin/configuracoes/fiscal");
        Assertions.assertThat(redirectAttributes.getFlashAttributes().get("success"))
                .isEqualTo("Configuracao fiscal atualizada.");
        verify(fiscalEmitterConfigService).save(
                eq(FiscalProvider.FOCUS_NFE),
                ArgumentMatchers.argThat(input ->
                        Boolean.TRUE.equals(input.enabled())
                                && input.environment() == FiscalEnvironment.HOMOLOGACAO
                                && "000001".equals(input.cscId())
                )
        );
    }

    @Test
    void salvarAddsErrorFlashWhenServiceRejectsPayload() {
        AdminConfiguracoesFiscalController.FiscalConfigForm form =
                new AdminConfiguracoesFiscalController.FiscalConfigForm();
        RedirectAttributesModelMap redirectAttributes =
                new RedirectAttributesModelMap();
        when(fiscalEmitterConfigService.save(
                eq(FiscalProvider.FOCUS_NFE),
                any(FiscalEmitterConfigService.FiscalEmitterConfigInput.class)
        )).thenThrow(new IllegalArgumentException("Token da API fiscal obrigatorio."));

        String redirect = controller.salvar(form, redirectAttributes);

        Assertions.assertThat(redirect)
                .isEqualTo("redirect:/admin/configuracoes/fiscal");
        Assertions.assertThat(redirectAttributes.getFlashAttributes().get("error"))
                .isEqualTo("Token da API fiscal obrigatorio.");
    }
}
