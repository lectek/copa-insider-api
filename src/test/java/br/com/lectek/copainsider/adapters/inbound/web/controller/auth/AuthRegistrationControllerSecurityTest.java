package br.com.lectek.copainsider.adapters.inbound.web.controller.auth;

import br.com.lectek.copainsider.adapters.inbound.web.security.SecurityMvcConfig;
import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import br.com.lectek.copainsider.application.port.inbound.RegistrationAppService;
import br.com.lectek.copainsider.application.service.otp.OtpServicePort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = AuthRegistrationController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(SecurityMvcConfig.class)
class AuthRegistrationControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrationAppService registrationAppService;

    @MockitoBean
    private OtpServicePort otpServicePort;

    @MockitoBean
    private AppSettingService appSettingService;

    @MockitoBean
    private SessionRegistry sessionRegistry;

    @Test
    void shouldAllowAnonymousCustomerRegistrationPost() throws Exception {
        mockMvc.perform(post("/auth/cliente/cadastro")
                        .with(csrf())
                        .param("nome", "Alysson Proprietario")
                        .param("email", "alysson@example.com")
                        .param("cpf", "07958111444")
                        .param("telefone", "83999999999")
                        .param("senha", "Aa1!aaaa")
                        .param("confirmarSenha", "Aa1!aaaa")
                        .param("canalOtp", "email")
                        .param("otpToken", "token-invalido"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/auth/cadastro-cliente"));
    }
}
