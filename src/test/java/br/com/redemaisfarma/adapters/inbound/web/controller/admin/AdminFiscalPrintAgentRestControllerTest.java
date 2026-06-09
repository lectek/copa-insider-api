package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.FiscalDocumentEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.FiscalPrintJobEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.FiscalPrintStationEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.application.service.fiscal.FiscalPrintQueueService;
import br.com.redemaisfarma.application.service.fiscal.FiscalPrintStationService;
import br.com.redemaisfarma.domain.fiscal.FiscalDocumentStatus;
import br.com.redemaisfarma.domain.fiscal.FiscalPrintChannel;
import br.com.redemaisfarma.domain.fiscal.FiscalPrintJobStatus;
import br.com.redemaisfarma.domain.fiscal.FiscalPrintJobType;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminFiscalPrintAgentRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminFiscalPrintAgentRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FiscalPrintQueueService fiscalPrintQueueService;

    @MockitoBean
    private FiscalPrintStationService fiscalPrintStationService;

    @MockitoBean
    private AppSettingService appSettingService;

    @Test
    void nextJobReturnsClaimedJobPayload() throws Exception {
        final FiscalPrintJobEntity job = new FiscalPrintJobEntity();
        job.setId(77L);
        job.setJobType(FiscalPrintJobType.DANFE_IMMEDIATE);
        job.setPrintChannel(FiscalPrintChannel.IMMEDIATE);
        job.setStatus(FiscalPrintJobStatus.PRINTING);
        job.setCopies(1);

        final PedidoEntity pedido = new PedidoEntity();
        pedido.setId(14L);
        job.setPedido(pedido);

        final FiscalDocumentEntity document = new FiscalDocumentEntity();
        document.setId(99L);
        document.setStatus(FiscalDocumentStatus.AUTHORIZED);
        document.setAccessKey("123");
        document.setDanfeStoragePath("https://danfe");
        job.setFiscalDocument(document);

        final FiscalPrintStationEntity station = new FiscalPrintStationEntity();
        station.setCode("CAIXA-1");
        station.setDisplayName("Caixa principal");
        job.setStation(station);

        when(fiscalPrintQueueService.claimNextReadyJob(
                eq("CAIXA-1"),
                eq("user")
        )).thenReturn(Optional.of(job));

        mockMvc.perform(get("/api/admin/fiscal/impressao/agente/estacoes/CAIXA-1/proximo-job")
                        .principal(new UsernamePasswordAuthenticationToken("user", "N/A")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.job.jobId").value(77))
                .andExpect(jsonPath("$.job.stationCode").value("CAIXA-1"))
                .andExpect(jsonPath("$.job.danfeUrl").value("https://danfe"));
    }

    @Test
    void heartbeatReturnsUpdatedStation() throws Exception {
        final FiscalPrintStationEntity station = new FiscalPrintStationEntity();
        station.setId(1L);
        station.setCode("CAIXA-1");
        station.setDisplayName("Caixa principal");
        station.setActive(true);
        station.setLastHeartbeatAt(LocalDateTime.of(2026, 3, 11, 15, 0));

        when(fiscalPrintStationService.recordHeartbeat("CAIXA-1"))
                .thenReturn(station);

        mockMvc.perform(post("/api/admin/fiscal/impressao/agente/estacoes/CAIXA-1/heartbeat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CAIXA-1"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void markFailedReturnsUpdatedJob() throws Exception {
        final FiscalPrintJobEntity job = new FiscalPrintJobEntity();
        job.setId(88L);
        job.setStatus(FiscalPrintJobStatus.FAILED);
        final PedidoEntity pedido = new PedidoEntity();
        pedido.setId(25L);
        job.setPedido(pedido);

        when(fiscalPrintQueueService.markFailed(
                eq(88L),
                eq("agent"),
                eq("sem papel")
        )).thenReturn(job);

        mockMvc.perform(post("/api/admin/fiscal/impressao/agente/jobs/88/falhou")
                        .principal(new UsernamePasswordAuthenticationToken("agent", "N/A"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "sem papel"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(88))
                .andExpect(jsonPath("$.pedidoId").value(25));
    }

    @Test
    void markPrintedForPrintAgentUsesStationBoundFlow() throws Exception {
        final FiscalPrintJobEntity job = new FiscalPrintJobEntity();
        job.setId(66L);
        job.setStatus(FiscalPrintJobStatus.PRINTED);

        when(fiscalPrintQueueService.markPrintedForStation(
                eq(66L),
                eq("CAIXA-1"),
                eq("PRINT_AGENT:CAIXA-1")
        )).thenReturn(job);

        mockMvc.perform(post("/api/admin/fiscal/impressao/agente/jobs/66/concluido")
                        .principal(new UsernamePasswordAuthenticationToken(
                                "PRINT_AGENT:CAIXA-1",
                                "N/A",
                                java.util.List.of(new SimpleGrantedAuthority("ROLE_PRINT_AGENT"))
                        ))
                        .header("X-Print-Station-Code", "CAIXA-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(66));

        verify(fiscalPrintQueueService).markPrintedForStation(
                66L,
                "CAIXA-1",
                "PRINT_AGENT:CAIXA-1"
        );
    }
}
