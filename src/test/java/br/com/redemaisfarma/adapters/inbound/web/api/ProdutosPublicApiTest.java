package br.com.redemaisfarma.adapters.inbound.web.api;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ProdutoJpaRepository;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.application.service.ProductCategorySectionService;
import br.com.redemaisfarma.application.view.ProductCardVM;
import br.com.redemaisfarma.application.view.ProductCategorySectionVM;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProdutosPublicApi.class)
@AutoConfigureMockMvc(addFilters = false)
class ProdutosPublicApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProdutoJpaRepository repo;

    @MockitoBean
    private AppSettingService appSettingService;

    @MockitoBean
    private ProductCategorySectionService productCategorySectionService;

    @Test
    void obterRetornaProdutoQuandoPublico() throws Exception {
        ProdutoEntity entity = new ProdutoEntity();
        entity.setId(10L);
        entity.setNome("Dipirona 500mg");
        entity.setDescricao("Analgesico");
        entity.setPrecoVenda(BigDecimal.valueOf(12.50));
        entity.setImagem("dipirona-500.png");
        entity.setCategoria("ANALGESICO");
        entity.setCodigoBarras("7891234567895");
        entity.setEstoque(9);
        entity.setDisponivel(true);
        entity.setDataCadastro(LocalDate.now());
        entity.setUpdatedAt(LocalDateTime.now());

        when(repo.findPublicById(10L)).thenReturn(Optional.of(entity));

        mockMvc.perform(get("/api/public/produtos/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entityId").value(10))
                .andExpect(jsonPath("$.nome").value("Dipirona 500mg"))
                .andExpect(jsonPath("$.imagem").value("/media/products/dipirona-500.png"))
                .andExpect(jsonPath("$.imagemUrl").value("/media/products/dipirona-500.png"))
                .andExpect(jsonPath("$.situacao").value("ATIVO"));
    }

    @Test
    void obterRetornaNotFoundQuandoProdutoNaoEstaPublico() throws Exception {
        when(repo.findPublicById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/public/produtos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void secoesPorCategoriaRetornaAgrupamentoConfigurado() throws Exception {
        when(productCategorySectionService.loadPublicSections(8)).thenReturn(List.of(
                new ProductCategorySectionVM(
                        "medicamentos",
                        "Medicamentos",
                        List.of("ANALGESICOS", "ANTI-INFLAMATORIOS"),
                        8,
                        List.of(new ProductCardVM(
                                10L,
                                "Dipirona 500mg",
                                "/img/dipirona.png",
                                BigDecimal.valueOf(12.50),
                                null,
                                null,
                                true,
                                9,
                                "ANALGESICOS"
                        ))
                )
        ));

        mockMvc.perform(get("/api/public/produtos/secoes-por-categoria")
                        .param("limit", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].chave").value("medicamentos"))
                .andExpect(jsonPath("$[0].titulo").value("Medicamentos"))
                .andExpect(jsonPath("$[0].categorias[1]").value("ANTI-INFLAMATORIOS"))
                .andExpect(jsonPath("$[0].produtos[0].nome").value("Dipirona 500mg"))
                .andExpect(jsonPath("$[0].limite").value(8));
    }

    @Test
    void destaquesRetornamImagemETambemImagemUrl() throws Exception {
        ProdutoEntity entity = new ProdutoEntity();
        entity.setId(22L);
        entity.setNome("Vitamina C");
        entity.setDescricao("Suplemento");
        entity.setPrecoVenda(BigDecimal.valueOf(19.90));
        entity.setImagem("vitamina-c.png");
        entity.setCategoria("SUPLEMENTOS");
        entity.setCodigoBarras("7891234567896");
        entity.setEstoque(12);
        entity.setDisponivel(true);
        entity.setDataCadastro(LocalDate.now());
        entity.setUpdatedAt(LocalDateTime.now());

        when(repo.findCarrossel(org.springframework.data.domain.PageRequest.of(0, 10)))
                .thenReturn(List.of(entity));

        mockMvc.perform(get("/api/public/produtos/destaques"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].imagem").value("/media/products/vitamina-c.png"))
                .andExpect(jsonPath("$[0].imagemUrl").value("/media/products/vitamina-c.png"));
    }
}
