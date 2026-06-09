package br.com.lectek.copainsider.adapters.inbound.web.controller.admin;

import br.com.lectek.copainsider.adapters.inbound.web.security.SecurityAdminApiConfig;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import br.com.lectek.copainsider.application.service.delivery.AdminEntregaRouteService;
import java.math.BigDecimal;
import java.util.Collections;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminEntregasRestController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(SecurityAdminApiConfig.class)
class AdminEntregasRestControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoRepository pedidoRepository;

    @MockitoBean
    private AdminEntregaRouteService adminEntregaRouteService;

    @MockitoBean
    private AppSettingService appSettingService;

    @Test
    void shouldReturnForbiddenWhenAnonymous() throws Exception {
        mockMvc.perform(post("/api/admin/entregas/roteirizar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pedidoIds": [1, 2, 3],
                                  "origem": "Base"
                                }
                                """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(adminEntregaRouteService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminSessionToPreviewRoute() throws Exception {
        when(adminEntregaRouteService.previewRoute(eq(List.of(1L, 2L, 3L)), eq("Base")))
                .thenReturn(new AdminEntregaRouteService.PreviewRouteView(
                        "Base",
                        BigDecimal.valueOf(6.3d),
                        List.of(),
                        "https://maps.example/route"
                ));

        mockMvc.perform(post("/api/admin/entregas/roteirizar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pedidoIds": [1, 2, 3],
                                  "origem": "Base"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.origem").value("Base"))
                .andExpect(jsonPath("$.distanciaTotalKm").value(6.3))
                .andExpect(jsonPath("$.mapaUrl").value("https://maps.example/route"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminSessionToPreviewRouteWithFormPayload() throws Exception {
        when(adminEntregaRouteService.previewRoute(eq(List.of(1L, 2L, 3L)), eq("Base")))
                .thenReturn(new AdminEntregaRouteService.PreviewRouteView(
                        "Base",
                        BigDecimal.valueOf(6.3d),
                        List.of(),
                        "https://maps.example/route"
                ));

        mockMvc.perform(post("/api/admin/entregas/roteirizar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("pedidoIds", "1", "2", "3")
                        .param("origem", "Base"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.origem").value("Base"))
                .andExpect(jsonPath("$.distanciaTotalKm").value(6.3))
                .andExpect(jsonPath("$.mapaUrl").value("https://maps.example/route"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminSessionToPreviewRouteWithGpsOrigin() throws Exception {
        when(adminEntregaRouteService.previewRoute(
                eq(List.of(1L, 2L, 3L)),
                eq("Localizacao atual do dispositivo"),
                eq(BigDecimal.valueOf(-7.1695d)),
                eq(BigDecimal.valueOf(-34.8393d))))
                .thenReturn(new AdminEntregaRouteService.PreviewRouteView(
                        "Localizacao atual do dispositivo",
                        BigDecimal.valueOf(5.1d),
                        List.of(new AdminEntregaRouteService.PreviewStopView(
                                new br.com.lectek.copainsider.application.service.delivery.DeliveryRouteService.DeliveryStopPlan(
                                1,
                                8L,
                                "Administrador CopaInsider",
                                "Rua Maria de Lourdes Gomes da Silva, 157",
                                "585612",
                                "SAIU_PARA_ENTREGA",
                                BigDecimal.valueOf(0.91d),
                                BigDecimal.valueOf(0.91d),
                                -7.1700d,
                                -34.8450d,
                                null,
                                null
                                ))),
                        "https://maps.example/route"
                ));

        mockMvc.perform(post("/api/admin/entregas/roteirizar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("pedidoIds", "1", "2", "3")
                        .param("origem", "Localizacao atual do dispositivo")
                        .param("origemLatitude", "-7.1695")
                        .param("origemLongitude", "-34.8393"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.origem").value("Localizacao atual do dispositivo"))
                .andExpect(jsonPath("$.distanciaTotalKm").value(5.1))
                .andExpect(jsonPath("$.paradas[0].ordem").value(1))
                .andExpect(jsonPath("$.paradas[0].pedidoId").value(8))
                .andExpect(jsonPath("$.paradas[0].clienteNome").value("Administrador CopaInsider"))
                .andExpect(jsonPath("$.paradas[0].enderecoEntrega").value("Rua Maria de Lourdes Gomes da Silva, 157"))
                .andExpect(jsonPath("$.paradas[0].distanciaAnteriorKm").value(0.91))
                .andExpect(jsonPath("$.paradas[0].distanciaAcumuladaKm").value(0.91))
                .andExpect(jsonPath("$.mapaUrl").value("https://maps.example/route"));
    }

    @Test
    @WithMockUser(username = "admin@copainsider", roles = "ADMIN")
    void shouldAllowAdminSessionToCreateRouteWithFormPayload() throws Exception {
        when(adminEntregaRouteService.createRoute(
                eq(List.of(1L, 2L, 3L)),
                eq("Base"),
                eq("admin@copainsider")))
                .thenReturn(new AdminEntregaRouteService.RouteDetailView(
                        91L,
                        null,
                        "Base",
                        BigDecimal.valueOf(6.3d),
                        null,
                        null,
                        "PLANEJADA",
                        "Planejada",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        Collections.emptyList()
                ));

        mockMvc.perform(post("/api/admin/entregas/rotas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("pedidoIds", "1", "2", "3")
                        .param("origem", "Base"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(91))
                .andExpect(jsonPath("$.origem").value("Base"))
                .andExpect(jsonPath("$.status").value("PLANEJADA"));
    }
}
