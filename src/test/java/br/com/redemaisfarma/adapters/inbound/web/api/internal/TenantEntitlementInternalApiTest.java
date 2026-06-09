package br.com.redemaisfarma.adapters.inbound.web.api.internal;

import br.com.redemaisfarma.application.core.tenant.TenantEntitlementService;
import br.com.redemaisfarma.application.core.tenant.TenantEntitlementSnapshot;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TenantEntitlementInternalApi.class)
@AutoConfigureMockMvc(addFilters = false)
class TenantEntitlementInternalApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TenantEntitlementService tenantEntitlementService;

    @Test
    void findEntitlementsRetornaDadosQuandoTenantExiste() throws Exception {
        TenantEntitlementSnapshot snapshot = new TenantEntitlementSnapshot(
                10L,
                "embalando",
                "PLUS",
                List.of("ads_enabled", "delivery_route"),
                List.of("ADDON_AI_OPS")
        );
        when(tenantEntitlementService.resolveByTenantIdentifier("10")).thenReturn(Optional.of(snapshot));

        mockMvc.perform(get("/api/internal/tenant/10/entitlements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value(10))
                .andExpect(jsonPath("$.tenantCode").value("embalando"))
                .andExpect(jsonPath("$.planCode").value("PLUS"))
                .andExpect(jsonPath("$.featureKeys[0]").value("ads_enabled"))
                .andExpect(jsonPath("$.addonCodes[0]").value("ADDON_AI_OPS"));
    }

    @Test
    void findEntitlementsRetornaNotFoundQuandoTenantNaoExiste() throws Exception {
        when(tenantEntitlementService.resolveByTenantIdentifier("404")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/internal/tenant/404/entitlements"))
                .andExpect(status().isNotFound());
    }
}

