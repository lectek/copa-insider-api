package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.application.service.fiscal.FiscalPrintQueueService;
import br.com.redemaisfarma.application.service.fiscal.FiscalPrintStationService;
import br.com.redemaisfarma.application.service.otp.OtpServicePort;
import br.com.redemaisfarma.domain.fiscal.FiscalDocumentStatus;
import br.com.redemaisfarma.domain.fiscal.FiscalPrintChannel;
import br.com.redemaisfarma.domain.fiscal.FiscalPrintJobStatus;
import br.com.redemaisfarma.domain.fiscal.FiscalPrintJobType;
import br.com.redemaisfarma.domain.fiscal.FiscalPrintStationRole;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = AdminFiscalPrintQueueController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminFiscalPrintQueueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FiscalPrintQueueService fiscalPrintQueueService;

    @MockitoBean
    private FiscalPrintStationService fiscalPrintStationService;

    @MockitoBean
    private AppSettingService appSettingService;

    @MockitoBean
    private OtpServicePort otpServicePort;

    @MockitoBean
    private ThymeleafViewResolver thymeleafViewResolver;

    @BeforeEach
    void setupAuthentication() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "admin@teste",
                        "N/A",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );
        when(thymeleafViewResolver.resolveViewName(any(), any()))
                .thenReturn(new MappingJackson2JsonView());
    }

    @AfterEach
    void cleanupAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void painelCarregaJobsEEstacoesNoModel() throws Exception {
        when(fiscalPrintQueueService.listJobSummaries(null, null, 80))
                .thenReturn(List.of(new FiscalPrintQueueService.PrintJobSummary(
                        1L,
                        10L,
                        100L,
                        FiscalPrintJobType.DANFE_IMMEDIATE,
                        FiscalPrintJobStatus.READY,
                        FiscalPrintChannel.IMMEDIATE,
                        100,
                        1,
                        "CAIXA_VENDA_RAPIDA",
                        "Caixa principal",
                        FiscalDocumentStatus.AUTHORIZED,
                        "123",
                        "https://danfe",
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        null,
                        null,
                        null
                )));
        when(fiscalPrintQueueService.listEventSummaries(null, 40))
                .thenReturn(List.of(new FiscalPrintQueueService.PrintJobEventSummary(
                        9L,
                        1L,
                        10L,
                        br.com.redemaisfarma.domain.fiscal.FiscalPrintJobEventType.CREATED,
                        null,
                        FiscalPrintJobStatus.READY,
                        "SYSTEM",
                        "Job fiscal criado automaticamente.",
                        LocalDateTime.now()
                )));
        when(fiscalPrintStationService.list())
                .thenReturn(List.of(new FiscalPrintStationService.StationSummary(
                        1L,
                        "CAIXA-1",
                        "Caixa principal",
                        "EPSON",
                        FiscalPrintStationRole.IMMEDIATE_ONLY,
                        true,
                        null,
                        null,
                        true,
                        LocalDateTime.now()
                )));

        final MvcResult result = mockMvc.perform(get("/admin/fiscal/impressao"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/fiscal/impressao"))
                .andReturn();

        final Object jobs = result.getModelAndView().getModel().get("jobs");
        final Object events = result.getModelAndView().getModel().get("events");
        final Object stations = result.getModelAndView().getModel().get("stations");
        assertNotNull(jobs);
        assertNotNull(events);
        assertNotNull(stations);
        assertEquals(1, ((List<?>) jobs).size());
        assertEquals(1, ((List<?>) events).size());
        assertEquals(1, ((List<?>) stations).size());
    }

    @Test
    void segurarJobRedirecionaParaPainel() throws Exception {
        mockMvc.perform(post("/admin/fiscal/impressao/jobs/7/segurar")
                        .principal(new UsernamePasswordAuthenticationToken(
                                "admin@teste",
                                "N/A"
                        )))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/fiscal/impressao"));

        verify(fiscalPrintQueueService).hold(eq(7L), eq("admin@teste"), eq(null));
    }

    @Test
    void regerarCredencialEstacaoRedirecionaParaPainel() throws Exception {
        when(fiscalPrintStationService.rotateApiKey(5L))
                .thenReturn(new FiscalPrintStationService.GeneratedStationCredential(
                        5L,
                        "CAIXA-1",
                        "Caixa principal",
                        "pst_token",
                        LocalDateTime.now()
                ));

        mockMvc.perform(post("/admin/fiscal/impressao/estacoes/5/credencial/regerar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/fiscal/impressao"));

        verify(fiscalPrintStationService).rotateApiKey(5L);
    }
}
