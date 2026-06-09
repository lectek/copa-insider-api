package br.com.lectek.copainsider.adapters.inbound.web.controller;

import br.com.lectek.copainsider.application.dto.request.StockSubscriptionRequest;
import br.com.lectek.copainsider.application.service.ProductStockSubscriptionService;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProductStockSubscriptionEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductStockSubscriptionController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductStockSubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductStockSubscriptionService subscriptionService;

    @MockitoBean
    private AppSettingService appSettingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void subscribeReturnsCreated() throws Exception {
        StockSubscriptionRequest request = new StockSubscriptionRequest("user@example.com", "Cliente");

        ProductStockSubscriptionEntity entity = new ProductStockSubscriptionEntity();
        entity.setId(3L);
        entity.setRecipientEmail("user@example.com");
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(12L);
        produto.setNome("Produto Teste");
        produto.setCategoria("Medicamento");
        produto.setPrecoVenda(BigDecimal.valueOf(10));
        produto.setDisponivel(true);
        produto.setEstoque(5);
        entity.setProduto(produto);
        entity.setNotifiedAt(null);

        when(subscriptionService.subscribe(eq(12L), eq("user@example.com"), eq("Cliente")))
                .thenReturn(entity);

        mockMvc.perform(post("/api/public/produtos/12/stock/subscribe").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/public/produtos/12/stock/subscriptions/3"));
    }
}
