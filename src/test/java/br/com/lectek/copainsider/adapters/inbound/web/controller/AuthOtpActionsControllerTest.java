package br.com.lectek.copainsider.adapters.inbound.web.controller;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import br.com.lectek.copainsider.application.service.ClienteService;
import br.com.lectek.copainsider.application.service.otp.OtpServicePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthOtpActionsController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthOtpActionsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OtpServicePort otpService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private ClienteService clienteService;

    @MockitoBean
    private AppSettingService appSettingService;

    @Test
    void resetOtpAcceptsSnakeCaseNovaSenha() throws Exception {
        String token = "otp-token";
        String email = "cliente@example.com";

        UsuarioEntity user = new UsuarioEntity();
        user.setEmail(email);

        when(otpService.consumeTokenForDestino(token, email)).thenReturn(true);
        when(usuarioRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("Aa1!aaaa")).thenReturn("encoded-pass");

        mockMvc.perform(post("/api/auth/password/reset-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "otp-token",
                                  "email": "cliente@example.com",
                                  "nova_senha": "Aa1!aaaa"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Senha redefinida."));

        verify(usuarioRepository).save(any(UsuarioEntity.class));
    }

    @Test
    void resetOtpAcceptsCamelCaseNovaSenha() throws Exception {
        String token = "otp-token";
        String email = "cliente@example.com";

        UsuarioEntity user = new UsuarioEntity();
        user.setEmail(email);

        when(otpService.consumeTokenForDestino(token, email)).thenReturn(true);
        when(usuarioRepository.findByEmailIgnoreCase(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("Aa1!aaaa")).thenReturn("encoded-pass");

        mockMvc.perform(post("/api/auth/password/reset-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "otp-token",
                                  "email": "cliente@example.com",
                                  "novaSenha": "Aa1!aaaa"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Senha redefinida."));

        verify(usuarioRepository).save(any(UsuarioEntity.class));
    }
}
