package br.com.lectek.copainsider.adapters.inbound.web.controller.admin;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.ProdutoJpaRepository;
import br.com.lectek.copainsider.application.core.media.ImageStorageService;
import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import br.com.lectek.copainsider.application.service.ProductCategoryBindingService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProdutoAdminRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProdutoAdminRestFiscalFieldsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProdutoJpaRepository repo;

    @MockitoBean
    private ImageStorageService imageStorageService;

    @MockitoBean
    private AppSettingService appSettingService;

    @MockitoBean
    private ProductCategoryBindingService categoryBindingService;

    private ProdutoEntity entity;

    @BeforeEach
    void setUp() {
        entity = new ProdutoEntity();
        entity.setId(1L);
        entity.setNome("Dipirona");
        entity.setDescricao("Dipirona 500mg");
        entity.setCategoria("Medicacoes");
        entity.setPrecoVenda(BigDecimal.valueOf(12.50));
        entity.setCodigoBarras("7891234567895");
        entity.setEstoque(5);
        entity.setImagem("https://cdn.example.com/produtos/1.png");
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setHashLegado("hash-1");
        when(repo.findById(1L)).thenReturn(Optional.of(entity));
        when(repo.findByNomeIgnoreCase(anyString())).thenReturn(Optional.of(entity));
        when(repo.save(any(ProdutoEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void updatePersistsFiscalFields() throws Exception {
        String payload = """
                {
                  "nome": "Dipirona",
                  "descricao": "Dipirona 500mg",
                  "preco": 12.50,
                  "imagem": "https://cdn.example.com/produtos/1.png",
                  "categoria": "Medicacoes",
                  "codigoBarras": "7891234567895",
                  "estoque": 5,
                  "ativo": false,
                  "tarjaMedicacao": "SEM_TARJA",
                  "exigeReceita": false,
                  "fiscalNcm": "30049069",
                  "fiscalCest": "1300100",
                  "fiscalCfop": "5102",
                  "fiscalOrigem": 0,
                  "fiscalCsosn": "102",
                  "fiscalPisCst": "01",
                  "fiscalCofinsCst": "01"
                }
                """;

        mockMvc.perform(put("/api/admin/produtos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        ArgumentCaptor<ProdutoEntity> captor =
                ArgumentCaptor.forClass(ProdutoEntity.class);
        verify(repo).save(captor.capture());
        ProdutoEntity persisted = captor.getValue();

        assertThat(persisted.getFiscalNcm()).isEqualTo("30049069");
        assertThat(persisted.getFiscalCest()).isEqualTo("1300100");
        assertThat(persisted.getFiscalCfop()).isEqualTo("5102");
        assertThat(persisted.getFiscalOrigem()).isEqualTo(0);
        assertThat(persisted.getFiscalCsosn()).isEqualTo("102");
        assertThat(persisted.getFiscalPisCst()).isEqualTo("01");
        assertThat(persisted.getFiscalCofinsCst()).isEqualTo("01");
    }
}
