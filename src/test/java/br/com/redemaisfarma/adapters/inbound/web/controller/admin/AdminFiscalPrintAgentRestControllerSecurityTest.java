package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.inbound.web.security.SecurityAdminApiConfig;
import br.com.redemaisfarma.adapters.inbound.web.security.FiscalPrintAgentApiKeyFilter;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.FiscalPrintStationEntity;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.application.service.fiscal.FiscalPrintQueueService;
import br.com.redemaisfarma.application.service.fiscal.FiscalPrintStationService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminFiscalPrintAgentRestController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(SecurityAdminApiConfig.class)
class AdminFiscalPrintAgentRestControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FiscalPrintQueueService fiscalPrintQueueService;

    @MockitoBean
    private FiscalPrintStationService fiscalPrintStationService;

    @MockitoBean
    private AppSettingService appSettingService;

    @Test
    void shouldReturnForbiddenWhenAnonymous() throws Exception {
        mockMvc.perform(get("/api/admin/fiscal/impressao/agente/estacoes/CAIXA-1/proximo-job"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(fiscalPrintQueueService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForUnsupportedRole() throws Exception {
        mockMvc.perform(get("/api/admin/fiscal/impressao/agente/estacoes/CAIXA-1/proximo-job"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(fiscalPrintQueueService);
    }

    @Test
    @WithMockUser(roles = "CAIXA")
    void shouldAllowCaixaToClaimJobs() throws Exception {
        when(fiscalPrintQueueService.claimNextReadyJob("CAIXA-1", "user"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/admin/fiscal/impressao/agente/estacoes/CAIXA-1/proximo-job"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CAIXA")
    void shouldAllowCaixaToHeartbeat() throws Exception {
        when(fiscalPrintStationService.recordHeartbeat("CAIXA-1"))
                .thenThrow(new java.util.NoSuchElementException("missing"));

        mockMvc.perform(post("/api/admin/fiscal/impressao/agente/estacoes/CAIXA-1/heartbeat"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldAllowStationApiKeyToClaimJobs() throws Exception {
        final FiscalPrintStationEntity station = new FiscalPrintStationEntity();
        station.setCode("CAIXA-1");
        station.setDisplayName("Caixa principal");
        station.setActive(true);

        when(fiscalPrintStationService.authenticateAgent(
                eq("CAIXA-1"),
                eq("pst_token")
        )).thenReturn(Optional.of(station));
        when(fiscalPrintQueueService.claimNextReadyJob(
                "CAIXA-1",
                "PRINT_AGENT:CAIXA-1"
        )).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/admin/fiscal/impressao/agente/estacoes/CAIXA-1/proximo-job")
                        .header(FiscalPrintAgentApiKeyFilter.HEADER_STATION_CODE, "CAIXA-1")
                        .header(FiscalPrintAgentApiKeyFilter.HEADER_STATION_KEY, "pst_token"))
                .andExpect(status().isOk());
    }
}
