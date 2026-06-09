package br.com.lectek.copainsider.adapters.inbound.web.controller.admin;

import br.com.lectek.copainsider.application.core.exception.ImportInProgressException;
import br.com.lectek.copainsider.application.service.EstoqueFisicoApiService;
import br.com.lectek.copainsider.application.service.EstoqueFisicoImportService;
import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import br.com.lectek.copainsider.application.service.otp.OtpServicePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EstoqueFisicoAdminRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class EstoqueFisicoAdminRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EstoqueFisicoApiService estoqueFisicoApiService;

    @MockitoBean
    private OtpServicePort otpServicePort;

    @MockitoBean
    private AppSettingService appSettingService;

    @Test
    void importarEndpointRetornaResumo() throws Exception {
        when(this.estoqueFisicoApiService.importarTudoParaBanco())
                .thenReturn(new EstoqueFisicoImportService.ImportacaoResumo(10, 8, 1, 1, 0));

        this.mockMvc.perform(post("/api/admin/estoque-fisico/importar"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.lidos").value(10))
                .andExpect(jsonPath("$.inseridos").value(8));
    }

    @Test
    void importarEndpointComUploadRetornaResumo() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "estoque.csv",
                "text/csv",
                "COD BARRAS;CODIGO;PRODUTO\n789;1;Produto".getBytes(StandardCharsets.UTF_8)
        );
        when(this.estoqueFisicoApiService.importarTudoParaBanco(any(org.springframework.web.multipart.MultipartFile.class)))
                .thenReturn(new EstoqueFisicoImportService.ImportacaoResumo(5, 4, 1, 0, 0));

        this.mockMvc.perform(multipart("/api/admin/estoque-fisico/importar").file(file))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.lidos").value(5))
                .andExpect(jsonPath("$.inseridos").value(4));
    }

    @Test
    void importarEndpointRetornaConflictQuandoOutraImportacaoJaEstaEmExecucao() throws Exception {
        when(this.estoqueFisicoApiService.importarTudoParaBanco())
                .thenThrow(new ImportInProgressException("Ja existe uma importacao de estoque fisico em execucao."));

        this.mockMvc.perform(post("/api/admin/estoque-fisico/importar"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("import_in_progress"))
                .andExpect(jsonPath("$.message").value("Ja existe uma importacao de estoque fisico em execucao."));
    }

    @Test
    void csvEndpointRetornaPagina() throws Exception {
        EstoqueFisicoApiService.CsvItem item = new EstoqueFisicoApiService.CsvItem(
                1L,
                "7890000000000",
                "Produto CSV",
                "Fabricante",
                5,
                null,
                null
        );
        when(this.estoqueFisicoApiService.lerCsv(anyString(), anyInt(), anyInt()))
                .thenReturn(new EstoqueFisicoApiService.CsvPage(List.of(item), 0, 1, 1, false));

        this.mockMvc.perform(get("/api/admin/estoque-fisico/csv")
                        .param("q", "csv")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].nome").value("Produto CSV"));
    }

    @Test
    void naoDisponiveisEndpointRetornaPagina() throws Exception {
        EstoqueFisicoApiService.NaoDisponivelItem item = new EstoqueFisicoApiService.NaoDisponivelItem(
                99L,
                12L,
                "Produto banco",
                "Estoque fisico",
                "7891111111111",
                0,
                null,
                false,
                "IMPORTADO",
                "CSV_ESTOQUE"
        );
        when(this.estoqueFisicoApiService.lerNaoDisponiveisBanco(anyString(), anyInt(), anyInt()))
                .thenReturn(new EstoqueFisicoApiService.NaoDisponiveisPage(List.of(item), 0, 1, 1, false));

        this.mockMvc.perform(get("/api/admin/estoque-fisico/nao-disponiveis")
                        .param("q", "banco")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.items[0].id").value(99))
                .andExpect(jsonPath("$.items[0].metodoLeituraCodigoBarras").value("CSV_ESTOQUE"));
    }

    @Test
    void exportCsvEndpointRetornaArquivo() throws Exception {
        doAnswer(invocation -> {
            invocation.<java.io.OutputStream>getArgument(0)
                    .write("id;nome\n1;Produto\n".getBytes(StandardCharsets.UTF_8));
            return null;
        }).when(this.estoqueFisicoApiService).exportarNaoDisponiveisCsv(any(), eq("abc"), eq(10));

        this.mockMvc.perform(get("/api/admin/estoque-fisico/nao-disponiveis/export.csv")
                        .param("q", "abc")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attachment; filename=")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("id;nome")));
    }
}
