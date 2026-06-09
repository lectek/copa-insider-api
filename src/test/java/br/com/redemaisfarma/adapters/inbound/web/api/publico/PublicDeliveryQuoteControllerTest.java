package br.com.redemaisfarma.adapters.inbound.web.api.publico;

import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.application.service.delivery.DeliveryPricingService;
import br.com.redemaisfarma.application.view.DeliveryQuoteVM;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicDeliveryQuoteController.class)
@AutoConfigureMockMvc(addFilters = false)
class PublicDeliveryQuoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeliveryPricingService deliveryPricingService;

    @MockitoBean
    private AppSettingService appSettingService;

    @Test
    void quoteRetornaDetalhesDaPoliticaDeFrete() throws Exception {
        when(appSettingService.get(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(Optional.empty());
        when(deliveryPricingService.quoteForAddress("Rua Exemplo, 123"))
                .thenReturn(DeliveryQuoteVM.available(
                        "Rua Exemplo, 123",
                        new BigDecimal("7.25"),
                        new BigDecimal("5.00"),
                        new BigDecimal("2.25"),
                        new BigDecimal("2.00"),
                        new BigDecimal("4.50"),
                        new BigDecimal("20.00"),
                        new BigDecimal("24.50"),
                        "Frete padrao em R$ 4,50",
                        "Distancia estimada de 7.25 km. Ate 5.00 km a entrega e gratis; depois cobramos R$ 2,00 por km excedente."
                ));

        mockMvc.perform(get("/api/public/entrega/cotacao")
                        .param("endereco", "Rua Exemplo, 123"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.distanceKm").value(7.25))
                .andExpect(jsonPath("$.billableDistanceKm").value(2.25))
                .andExpect(jsonPath("$.standardShippingAmount").value(4.50))
                .andExpect(jsonPath("$.priorityShippingAmount").value(24.50));
    }
}
