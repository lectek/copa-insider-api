package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.application.service.otp.OtpServicePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthEmailClaimController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthEmailClaimControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OtpServicePort otpService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private AppSettingService appSettingService;

    @Test
    void verifyAcceptsCamelDeliveryId() throws Exception {
        String email = "cliente@example.com";
        when(otpService.verify("delivery-1", "123456")).thenReturn("claim-token");
        when(usuarioRepository.existsByEmailIgnoreCase(email)).thenReturn(true);

        mockMvc.perform(post("/api/auth/email-claim/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deliveryId": "delivery-1",
                                  "code": "123456",
                                  "email": "cliente@example.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("claim-token"))
                .andExpect(jsonPath("$.userExists").value(true));

        verify(otpService).verify("delivery-1", "123456");
    }

    @Test
    void startAcceptsSnakePreviousDeliveryId() throws Exception {
        String email = "cliente@example.com";
        when(otpService.start("email", email, "prev-1"))
                .thenReturn(new OtpServicePort.StartResult("delivery-2", "c***@example.com", 60, 300, null));
        when(usuarioRepository.existsByEmailIgnoreCase(email)).thenReturn(false);

        mockMvc.perform(post("/api/auth/email-claim/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "cliente@example.com",
                                  "previous_delivery_id": "prev-1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deliveryId").value("delivery-2"))
                .andExpect(jsonPath("$.userExists").value(false));

        verify(otpService).start("email", email, "prev-1");
    }
}
