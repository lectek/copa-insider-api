package br.com.lectek.copainsider.adapters.inbound.web.api.internal;

import br.com.lectek.copainsider.adapters.inbound.web.security.SecurityApiConfig;
import br.com.lectek.copainsider.application.core.tenant.TenantEntitlementService;
import br.com.lectek.copainsider.application.core.tenant.TenantEntitlementSnapshot;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TenantEntitlementInternalApi.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(SecurityApiConfig.class)
class TenantEntitlementInternalApiSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TenantEntitlementService tenantEntitlementService;

    @Test
    void shouldReturnForbiddenWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/internal/tenant/10/entitlements"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(tenantEntitlementService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenWhenRoleIsNotAllowed() throws Exception {
        mockMvc.perform(get("/api/internal/tenant/10/entitlements"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(tenantEntitlementService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminToAccessInternalTenantEntitlements() throws Exception {
        when(tenantEntitlementService.resolveByTenantIdentifier("10"))
                .thenReturn(Optional.of(snapshot()));

        mockMvc.perform(get("/api/internal/tenant/10/entitlements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value(10))
                .andExpect(jsonPath("$.tenantCode").value("embalando"));
    }

    @Test
    @WithMockUser(roles = "PLATFORM_OWNER")
    void shouldAllowPlatformOwnerToAccessInternalTenantEntitlements() throws Exception {
        when(tenantEntitlementService.resolveByTenantIdentifier("10"))
                .thenReturn(Optional.of(snapshot()));

        mockMvc.perform(get("/api/internal/tenant/10/entitlements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.planCode").value("PLUS"));
    }

    private static TenantEntitlementSnapshot snapshot() {
        return new TenantEntitlementSnapshot(
                10L,
                "embalando",
                "PLUS",
                List.of("ads_enabled"),
                List.of("ADDON_AI_OPS")
        );
    }
}
