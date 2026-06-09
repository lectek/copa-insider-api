package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.inbound.web.security.SecurityAdminApiConfig;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.application.service.CustomPaymentMethodService;
import br.com.redemaisfarma.application.service.CustomPaymentMethodService.CustomPaymentMethod;
import br.com.redemaisfarma.application.service.CustomPaymentMethodService.CustomPaymentMethodInput;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminPaymentMethodsRestController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(SecurityAdminApiConfig.class)
class AdminPaymentMethodsRestControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomPaymentMethodService customPaymentMethodService;

    @MockitoBean
    private AppSettingService appSettingService;

    @Test
    void shouldReturnForbiddenWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/pagamentos/metodos"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(customPaymentMethodService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenWhenRoleIsNotAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/pagamentos/metodos"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(customPaymentMethodService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminToListMethods() throws Exception {
        final CustomPaymentMethod method = new CustomPaymentMethod();
        method.setId("m-1");
        method.setNome("Convenio Empresa");
        method.setTipo("offline");
        method.setTaxa(BigDecimal.valueOf(1.99));
        method.setAtivo(true);
        when(customPaymentMethodService.list()).thenReturn(List.of(method));

        mockMvc.perform(get("/api/admin/pagamentos/metodos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("m-1"))
                .andExpect(jsonPath("$[0].nome").value("Convenio Empresa"))
                .andExpect(jsonPath("$[0].tipo").value("offline"))
                .andExpect(jsonPath("$[0].value").value("custom:m-1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminToCreateMethod() throws Exception {
        final CustomPaymentMethod created = new CustomPaymentMethod();
        created.setId("m-2");
        created.setNome("Link de pagamento");
        created.setTipo("online");
        created.setTaxa(BigDecimal.ZERO);
        created.setAtivo(true);
        when(customPaymentMethodService.create(any(CustomPaymentMethodInput.class)))
                .thenReturn(created);

        mockMvc.perform(post("/api/admin/pagamentos/metodos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Link de pagamento",
                                  "tipo": "online",
                                  "taxa": 0,
                                  "ativo": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("m-2"))
                .andExpect(jsonPath("$.tipo").value("online"))
                .andExpect(jsonPath("$.value").value("custom:m-2"));
    }
}
