package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.inbound.web.security.SecurityAdminApiConfig;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.application.service.PaymentTerminalService;
import br.com.redemaisfarma.application.service.PaymentTerminalService.TerminalConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminPaymentTerminalRestController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(SecurityAdminApiConfig.class)
class AdminPaymentTerminalRestControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentTerminalService paymentTerminalService;

    @MockitoBean
    private AppSettingService appSettingService;

    @Test
    void shouldReturnForbiddenWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/pagamentos/terminal/config"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(paymentTerminalService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenWhenRoleIsNotAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/pagamentos/terminal/config"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(paymentTerminalService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminToReadConfig() throws Exception {
        when(paymentTerminalService.loadConfig()).thenReturn(new TerminalConfig(
                true,
                "mock",
                "stone",
                "",
                "T1",
                "M1",
                10000,
                true
        ));

        mockMvc.perform(get("/api/admin/pagamentos/terminal/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.mode").value("mock"))
                .andExpect(jsonPath("$.provider").value("stone"))
                .andExpect(jsonPath("$.secretConfigured").value(true));
    }
}
