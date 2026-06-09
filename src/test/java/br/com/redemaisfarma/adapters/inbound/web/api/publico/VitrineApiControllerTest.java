package br.com.redemaisfarma.adapters.inbound.web.api.publico;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.application.core.produto.ProdutoVitrineService;
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

@WebMvcTest(controllers = VitrineApiController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("deprecation")
class VitrineApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProdutoVitrineService vitrineService;

    @MockitoBean
    private AppSettingService appSettingService;

    @Test
    void destaquesRetornaHeadersDeDeprecacao() throws Exception {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(2L);
        produto.setNome("Paracetamol");
        produto.setImagem("/img/paracetamol.png");
        produto.setPrecoVenda(BigDecimal.valueOf(9.9));
        produto.setEstoque(8);
        produto.setDisponivel(true);
        produto.setCategoria("MEDICACOES");

        when(vitrineService.listarDestaques(12)).thenReturn(List.of(produto));

        mockMvc.perform(get("/api/public/vitrine/destaques").param("limit", "12"))
                .andExpect(status().isOk())
                .andExpect(header().string("Deprecation", "true"))
                .andExpect(header().string("Sunset", "Tue, 30 Sep 2026 23:59:59 GMT"))
                .andExpect(header().string("Link", "</api/public/produtos/destaques>; rel=\"successor-version\""))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].nome").value("Paracetamol"));
    }
}
