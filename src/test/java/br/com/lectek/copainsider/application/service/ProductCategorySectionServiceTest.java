package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoCategoriaRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import br.com.lectek.copainsider.application.view.ProductCategorySectionVM;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductCategorySectionServiceTest {

    private static final Sort SECTION_SORT =
            Sort.by(Sort.Direction.DESC, "dataCadastro")
                    .and(Sort.by(Sort.Direction.DESC, "id"));

    private AppSettingService settings;
    private ProdutoRepository produtoRepository;
    private ProdutoCategoriaRepository produtoCategoriaRepository;
    private ProductCategorySectionService service;

    @BeforeEach
    void setUp() {
        settings = mock(AppSettingService.class);
        produtoRepository = mock(ProdutoRepository.class);
        produtoCategoriaRepository = mock(ProdutoCategoriaRepository.class);
        service = new ProductCategorySectionService(
                produtoRepository,
                produtoCategoriaRepository,
                settings,
                new ObjectMapper()
        );
    }

    @Test
    void loadPublicSectionsUsaConfiguracaoSalvaEDeduplicaProdutos() {
        when(settings.getOrDefault(ProductCategorySectionService.SETTING_KEY, ""))
                .thenReturn("""
                        [
                          {
                            "chave": "medicamentos",
                            "titulo": "Medicamentos",
                            "categorias": ["ANALGESICOS", "ANTIGRIPAL"],
                            "limite": 3
                          }
                        ]
                        """);
        when(produtoRepository.searchPublicPageByCategoria(
                null,
                "ANALGESICOS",
                PageRequest.of(0, 3, SECTION_SORT)
        )).thenReturn(new PageImpl<>(List.of(
                product(1L, "Dipirona", "ANALGESICOS"),
                product(2L, "Paracetamol", "ANALGESICOS")
        )));
        when(produtoRepository.searchPublicPageByCategoria(
                null,
                "ANTIGRIPAL",
                PageRequest.of(0, 1, SECTION_SORT)
        )).thenReturn(new PageImpl<>(List.of(
                product(2L, "Paracetamol", "ANTIGRIPAL"),
                product(3L, "Naldecon", "ANTIGRIPAL")
        )));

        List<ProductCategorySectionVM> sections = service.loadPublicSections(12);

        Assertions.assertThat(sections).hasSize(1);
        ProductCategorySectionVM section = sections.getFirst();
        Assertions.assertThat(section.chave()).isEqualTo("medicamentos");
        Assertions.assertThat(section.titulo()).isEqualTo("Medicamentos");
        Assertions.assertThat(section.categorias())
                .containsExactly("ANALGESICOS", "ANTIGRIPAL");
        Assertions.assertThat(section.limite()).isEqualTo(3);
        Assertions.assertThat(section.produtos())
                .extracting(product -> product.id())
                .containsExactly(1L, 2L, 3L);
    }

    @Test
    void loadPublicSectionsFazFallbackParaCategoriasPublicasQuandoConfigAusente() {
        when(settings.getOrDefault(ProductCategorySectionService.SETTING_KEY, ""))
                .thenReturn("");
        when(produtoCategoriaRepository.findAllNomes())
                .thenReturn(List.of("HIGIENE", "VITAMINAS"));
        when(produtoRepository.searchPublicPageByCategoria(
                null,
                "HIGIENE",
                PageRequest.of(0, 6, SECTION_SORT)
        )).thenReturn(new PageImpl<>(List.of(
                product(4L, "Sabonete", "HIGIENE")
        )));
        when(produtoRepository.searchPublicPageByCategoria(
                null,
                "VITAMINAS",
                PageRequest.of(0, 6, SECTION_SORT)
        )).thenReturn(new PageImpl<>(List.of()));

        List<ProductCategorySectionVM> sections = service.loadPublicSections(6);

        Assertions.assertThat(sections).hasSize(1);
        ProductCategorySectionVM section = sections.getFirst();
        Assertions.assertThat(section.chave()).isEqualTo("higiene");
        Assertions.assertThat(section.titulo()).isEqualTo("HIGIENE");
        Assertions.assertThat(section.categorias()).containsExactly("HIGIENE");
        Assertions.assertThat(section.limite()).isEqualTo(6);
        Assertions.assertThat(section.produtos())
                .extracting(product -> product.nome())
                .containsExactly("Sabonete");
    }

    @Test
    void loadAvailableCategoriesUsaCadastroAdminMesmoSemProdutoPublicado() {
        when(produtoCategoriaRepository.findAllNomes())
                .thenReturn(List.of("HIGIENE", "VITAMINAS", "  "));

        List<String> categories = service.loadAvailableCategories();

        Assertions.assertThat(categories).containsExactly("HIGIENE", "VITAMINAS");
    }

    @Test
    void saveEditorJsonNormalizaCategoriasDuplicadasETamanhoMaximo() {
        ObjectMapper mapper = new ObjectMapper();
        service.saveEditorJson("""
                [
                  {
                    "titulo": "",
                    "categorias": [" ANALGESICOS ", "analgesicos"],
                    "limite": 99
                  }
                ]
                """);

        verify(settings).upsert(
                eq(ProductCategorySectionService.SETTING_KEY),
                ArgumentMatchers.argThat(value -> hasNormalizedPayload(mapper, value)),
                eq("Secoes publicas de produtos agrupadas por categoria")
        );
    }

    private boolean hasNormalizedPayload(
            final ObjectMapper mapper,
            final String value
    ) {
        try {
            JsonNode root = mapper.readTree(value);
            return root.isArray()
                    && root.size() == 1
                    && "ANALGESICOS".equals(root.get(0).get("titulo").asText())
                    && "ANALGESICOS".equals(root.get(0).get("categorias").get(0).asText())
                    && root.get(0).get("categorias").size() == 1
                    && root.get(0).get("limite").asInt() == 24;
        } catch (Exception ex) {
            return false;
        }
    }

    private ProdutoEntity product(
            final Long id,
            final String nome,
            final String categoria
    ) {
        ProdutoEntity entity = new ProdutoEntity();
        entity.setId(id);
        entity.setNome(nome);
        entity.setCategoria(categoria);
        entity.setImagem("/img/" + id + ".png");
        entity.setPrecoVenda(BigDecimal.TEN);
        entity.setDisponivel(true);
        entity.setEstoque(7);
        return entity;
    }
}
