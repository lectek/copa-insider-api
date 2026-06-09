package br.com.redemaisfarma.adapters.inbound.web;

import br.com.redemaisfarma.application.view.ProductCardVM;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.domain.catalogo.ProdutoQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicCarrosselController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("deprecation")
class PublicCarrosselControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProdutoQueryService service;

    @MockitoBean
    private AppSettingService appSettingService;

    @Test
    void destaquesRetornaHeadersDeDeprecacao() throws Exception {
        when(service.featured(10)).thenReturn(List.of(
                new ProductCardVM(1L, "Dipirona", "/img/dipirona.png", BigDecimal.TEN, null, null, true, 5, "MEDICACOES")
        ));

        mockMvc.perform(get("/api/public/carrossel/destaques").param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(header().string("Deprecation", "true"))
                .andExpect(header().string("Sunset", "Tue, 30 Sep 2026 23:59:59 GMT"))
                .andExpect(header().string("Link", "</api/public/produtos/destaques>; rel=\"successor-version\""))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nome").value("Dipirona"));
    }
}
