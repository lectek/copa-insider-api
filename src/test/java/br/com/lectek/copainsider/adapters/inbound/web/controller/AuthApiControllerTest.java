package br.com.lectek.copainsider.adapters.inbound.web.controller;

import br.com.lectek.copainsider.application.core.account.UserAccountService;
import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import br.com.lectek.copainsider.application.dto.response.AuthResponse;
import br.com.lectek.copainsider.application.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthApiController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthApiControllerTest {

    private static final String LOGIN_URL = "/api/auth/login";
    private static final String USUARIO = "admin@example.com";
    private static final String SENHA = "Aa1!aaaa";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserAccountService userAccountService;

    @MockitoBean
    private AppSettingService appSettingService;

    @Test
    void loginReturnsUnauthorizedWhenCredentialsAreInvalid() throws Exception {
        when(authService.authenticate(USUARIO, SENHA))
                .thenThrow(new BadCredentialsException("bad credentials"));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginPayload(USUARIO, SENHA))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Usuario ou senha invalidos."));
    }

    @Test
    void loginReturnsInternalServerErrorWhenUnexpectedErrorHappens() throws Exception {
        when(authService.authenticate(USUARIO, SENHA))
                .thenThrow(new IllegalStateException("unexpected"));

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginPayload(USUARIO, SENHA))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Erro interno ao autenticar."));
    }

    @Test
    void loginReturnsDtoWithFallbackTenantAndTraceId() throws Exception {
        AuthResponse auth = new AuthResponse();
        auth.setAccessToken("access-token");
        auth.setRefreshToken("refresh-token");
        auth.setUserId(7L);
        auth.setUsername("Admin");
        auth.setEmail(USUARIO);
        auth.setRoles(List.of("ROLE_ADMIN"));
        auth.setExpiresAt(LocalDateTime.now().plusHours(2));
        auth.setTenantId(null);
        auth.setTraceId(null);
        when(authService.authenticate(USUARIO, SENHA)).thenReturn(auth);

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginPayload(USUARIO, SENHA))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.userType").value("ADMIN"))
                .andExpect(jsonPath("$.tenantId").value("rede-mais-farma"))
                .andExpect(jsonPath("$.traceId").value(not(blankOrNullString())));

        verify(authService).authenticate(USUARIO, SENHA);
    }

    private record LoginPayload(String usuario, String senha) {
    }
}
