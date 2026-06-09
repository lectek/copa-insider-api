package br.com.lectek.copainsider.adapters.inbound.web.controller;

import br.com.lectek.copainsider.adapters.inbound.web.security.SecurityMvcConfig;
import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import br.com.lectek.copainsider.application.dto.user.UsuarioFormDTO;
import br.com.lectek.copainsider.application.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminUserController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(SecurityMvcConfig.class)
class AdminUserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AppSettingService appSettingService;

    @MockitoBean
    private SessionRegistry sessionRegistry;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminToCreateUser() throws Exception {
        mockMvc.perform(post("/admin/usuarios")
                        .with(csrf())
                        .param("nome", "Alysson Proprietario")
                        .param("email", "alysson@example.com")
                        .param("cpf", "07958111444")
                        .param("papel", "ADMIN")
                        .param("ativo", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/usuarios"));

        verify(userService).salvar(any(UsuarioFormDTO.class));
    }
}
