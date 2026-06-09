package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.application.service.PasswordResetService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PasswordResetApiController.class)
@AutoConfigureMockMvc(addFilters = false)
class PasswordResetApiControllerTest {

    private static final String BASE_URL = "/api/auth";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PasswordResetService resetService;

    @MockitoBean
    private AppSettingService appSettingService;

    @Test
    void esqueciSenhaCallsServiceWhenIdentifierExists() throws Exception {
        mockMvc.perform(post(BASE_URL + "/esqueci-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"emailOuCpf\":\" user@example.com \"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));

        verify(resetService).solicitarResetPorEmailOuCpf("user@example.com");
    }

    @Test
    void esqueciSenhaSkipsServiceWhenIdentifierIsBlank() throws Exception {
        mockMvc.perform(post(BASE_URL + "/esqueci-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"emailOuCpf\":\"   \"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));

        verify(resetService, never()).solicitarResetPorEmailOuCpf(anyString());
    }

    @Test
    void validarTokenReturnsTrueWhenTokenExists() throws Exception {
        when(resetService.validarToken("token-ok"))
                .thenReturn(Optional.of(new UsuarioEntity()));

        mockMvc.perform(get(BASE_URL + "/validar-token").param("token", "token-ok"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true));
    }

    @Test
    void resetarSenhaReturnsBadRequestForInvalidPayload() throws Exception {
        mockMvc.perform(post(BASE_URL + "/resetar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"\",\"novaSenha\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("dados_invalidos"));
    }

    @Test
    void resetarSenhaReturnsBadRequestWhenTokenIsInvalid() throws Exception {
        when(resetService.aplicarNovaSenha("token", "NovaSenha#123")).thenReturn(false);

        mockMvc.perform(post(BASE_URL + "/resetar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"token\",\"novaSenha\":\"NovaSenha#123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("token_invalido_ou_expirado"));
    }

    @Test
    void resetarSenhaReturnsOkWhenPasswordIsUpdated() throws Exception {
        when(resetService.aplicarNovaSenha("token", "NovaSenha#123")).thenReturn(true);

        mockMvc.perform(post(BASE_URL + "/resetar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"token\",\"novaSenha\":\"NovaSenha#123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("alterada"));
    }
}
