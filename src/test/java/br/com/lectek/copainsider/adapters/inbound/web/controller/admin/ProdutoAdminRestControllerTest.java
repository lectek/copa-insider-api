package br.com.lectek.copainsider.adapters.inbound.web.controller.admin;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.ProdutoJpaRepository;
import br.com.lectek.copainsider.application.core.media.ImageStorageService;
import br.com.lectek.copainsider.application.dto.request.ProdutoRequestDTO;
import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import br.com.lectek.copainsider.application.service.ProductCategoryBindingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProdutoAdminRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProdutoAdminRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProdutoJpaRepository repo;

    @MockitoBean
    private ImageStorageService imageStorageService;

    @MockitoBean
    private AppSettingService appSettingService;

    @MockitoBean
    private ProductCategoryBindingService productCategoryBindingService;

    private ProdutoEntity sampleEntity;

    @BeforeEach
    void setup() {
        sampleEntity = buildEntity(1L);
        when(repo.findByNomeIgnoreCase(anyString())).thenReturn(Optional.empty());
    }

    @Test
    void listReturnsPagedProducts() throws Exception {
        when(repo.searchPage(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleEntity)));

        mockMvc.perform(get("/api/admin/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Produto 1"));
    }

    @Test
    void getProductNotFound() throws Exception {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/admin/produtos/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProductHandlesNullEstoqueWithoutError() throws Exception {
        sampleEntity.setEstoque(null);
        sampleEntity.setDisponivel(true);
        when(repo.findById(1L)).thenReturn(Optional.of(sampleEntity));

        mockMvc.perform(get("/api/admin/produtos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("ESGOTADO"));
    }

    @Test
    void createProductReturnsCreated() throws Exception {
        when(repo.save(any())).thenReturn(sampleEntity);
        ProdutoRequestDTO request = buildRequest();

        mockMvc.perform(post("/api/admin/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Produto 1"))
                .andExpect(jsonPath("$.situacao").value("ESGOTADO"));

        verify(repo).save(any());
    }

    @Test
    void updateProductReturnsOk() throws Exception {
        when(repo.findById(1L)).thenReturn(Optional.of(sampleEntity));
        when(repo.save(any())).thenReturn(sampleEntity);
        when(repo.findByNomeIgnoreCase(anyString())).thenReturn(Optional.of(sampleEntity));
        ProdutoRequestDTO request = buildRequest();

        mockMvc.perform(put("/api/admin/produtos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Produto 1"));
    }

    @Test
    void updateProductAcceptsFlexibleJsonPayloadAndKeepsGallery() throws Exception {
        sampleEntity.setImagensProduto(List.of(
                "https://cdn.example.com/produtos/1.png",
                "https://cdn.example.com/produtos/1-b.png"
        ));
        when(repo.findById(1L)).thenReturn(Optional.of(sampleEntity));
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(repo.findByNomeIgnoreCase(anyString())).thenReturn(Optional.of(sampleEntity));

        String body = """
                {
                  "nome":"Produto 1",
                  "descricao":"Descricao atualizada",
                  "preco":"49.99",
                  "imagem":"https://cdn.example.com/produtos/1-c.png",
                  "imagens":[
                    "https://cdn.example.com/produtos/1-c.png",
                    "https://cdn.example.com/produtos/1-b.png"
                  ],
                  "categoria":"Catalogo local",
                  "estoque":"7",
                  "codigoBarras":"7891234567895",
                  "ativo":true
                }
                """;

        mockMvc.perform(put("/api/admin/produtos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagem").value("https://cdn.example.com/produtos/1-c.png"))
                .andExpect(jsonPath("$.imagens[0]").value("https://cdn.example.com/produtos/1-c.png"))
                .andExpect(jsonPath("$.imagens[1]").value("https://cdn.example.com/produtos/1-b.png"));

        ArgumentCaptor<ProdutoEntity> captor = ArgumentCaptor.forClass(ProdutoEntity.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getPrecoVenda()).isEqualByComparingTo("49.99");
        assertThat(captor.getValue().getEstoque()).isEqualTo(7);
        assertThat(captor.getValue().getImagensProduto()).containsExactly(
                "https://cdn.example.com/produtos/1-c.png",
                "https://cdn.example.com/produtos/1-b.png"
        );
    }

    @Test
    void createProductRequiresImageWhenActive() throws Exception {
        when(repo.save(any())).thenReturn(sampleEntity);
        ProdutoRequestDTO request = buildRequest();
        request.setAtivo(true);
        request.setImagem(null);

        mockMvc.perform(post("/api/admin/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("Imagem obrigatória para produtos disponibilizados na web.")));
    }

    @Test
    void createProductRejectsDuplicateName() throws Exception {
        when(repo.findByNomeIgnoreCase(anyString())).thenReturn(Optional.of(sampleEntity));
        ProdutoRequestDTO request = buildRequest();
        request.setNome(sampleEntity.getNome());

        mockMvc.perform(post("/api/admin/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value(containsString("Produto com este nome já existe – pesquise antes de criar.")));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        when(repo.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/admin/produtos/1"))
                .andExpect(status().isNoContent());

        verify(repo).deleteById(1L);
    }

    @Test
    void deleteReturnsNotFound() throws Exception {
        when(repo.existsById(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/admin/produtos/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void uploadImageUpdatesProduct() throws Exception {
        when(repo.findById(1L)).thenReturn(Optional.of(sampleEntity));
        when(imageStorageService.saveProductImage(anyLong(), any())).thenReturn("https://cdn.example.com/produtos/1.png");
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = new MockMultipartFile("file", "image.png", MediaType.IMAGE_PNG_VALUE, "ok".getBytes());

        mockMvc.perform(multipart("/api/admin/produtos/1/imagem").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagem").value("https://cdn.example.com/produtos/1.png"))
                .andExpect(jsonPath("$.imagens[0]").value("https://cdn.example.com/produtos/1.png"));
    }

    @Test
    void uploadImageNotFound() throws Exception {
        when(repo.findById(1L)).thenReturn(Optional.empty());

        MockMultipartFile file = new MockMultipartFile("file", "image.png", MediaType.IMAGE_PNG_VALUE, "ok".getBytes());

        mockMvc.perform(multipart("/api/admin/produtos/1/imagem").file(file))
                .andExpect(status().isNotFound());

        verify(imageStorageService, never()).saveProductImage(anyLong(), any());
    }

    @Test
    void uploadImagesAppendsGalleryWithoutReplacingPrimary() throws Exception {
        sampleEntity.setImagensProduto(List.of("https://cdn.example.com/produtos/1.png"));
        when(repo.findById(1L)).thenReturn(Optional.of(sampleEntity));
        when(imageStorageService.saveProductImage(anyLong(), any()))
                .thenReturn("https://cdn.example.com/produtos/1-b.png",
                        "https://cdn.example.com/produtos/1-c.png");
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file1 = new MockMultipartFile("file", "image1.png", MediaType.IMAGE_PNG_VALUE, "ok".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file", "image2.png", MediaType.IMAGE_PNG_VALUE, "ok".getBytes());

        mockMvc.perform(multipart("/api/admin/produtos/1/imagens").file(file1).file(file2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagem").value("https://cdn.example.com/produtos/1.png"))
                .andExpect(jsonPath("$.imagens[0]").value("https://cdn.example.com/produtos/1.png"))
                .andExpect(jsonPath("$.imagens[1]").value("https://cdn.example.com/produtos/1-b.png"))
                .andExpect(jsonPath("$.imagens[2]").value("https://cdn.example.com/produtos/1-c.png"));
    }

    @Test
    void uploadImageDeletesSavedFileWhenDatabaseSaveFails() throws Exception {
        when(repo.findById(1L)).thenReturn(Optional.of(sampleEntity));
        when(imageStorageService.saveProductImage(anyLong(), any()))
                .thenReturn("https://cdn.example.com/produtos/1.png");
        when(repo.save(any())).thenThrow(new IllegalStateException("db"));

        MockMultipartFile file = new MockMultipartFile("file", "image.png", MediaType.IMAGE_PNG_VALUE, "ok".getBytes());

        mockMvc.perform(multipart("/api/admin/produtos/1/imagem").file(file))
                .andExpect(status().is5xxServerError());

        verify(imageStorageService).deleteProductImageByUrl("https://cdn.example.com/produtos/1.png");
    }

    @Test
    void validateProductSetsStatus() throws Exception {
        sampleEntity.setStatus(ProdutoStatus.IMPORTADO);
        when(repo.findById(1L)).thenReturn(Optional.of(sampleEntity));
        when(repo.save(any())).thenReturn(sampleEntity);

        mockMvc.perform(post("/api/admin/produtos/1/validar").param("validador", "qa"))
                .andExpect(status().isOk());

        ArgumentCaptor<ProdutoEntity> captor = ArgumentCaptor.forClass(ProdutoEntity.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ProdutoStatus.VALIDADO);
        assertThat(captor.getValue().getValidador()).isEqualTo("qa");
    }

    @Test
    void publicarProductSetsStatusAndPublished() throws Exception {
        sampleEntity.setStatus(ProdutoStatus.VALIDADO);
        when(repo.findById(1L)).thenReturn(Optional.of(sampleEntity));
        when(repo.save(any())).thenReturn(sampleEntity);

        mockMvc.perform(post("/api/admin/produtos/1/publicar").param("validador", "qa"))
                .andExpect(status().isOk());

        ArgumentCaptor<ProdutoEntity> captor = ArgumentCaptor.forClass(ProdutoEntity.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ProdutoStatus.PUBLICADO);
        assertThat(captor.getValue().getValidador()).isEqualTo("qa");
        assertThat(captor.getValue().getPublicadoEm()).isNotNull();
    }

    @Test
    void validateProductReturnsConflictWhenAlreadyPublished() throws Exception {
        sampleEntity.setStatus(ProdutoStatus.PUBLICADO);
        when(repo.findById(1L)).thenReturn(Optional.of(sampleEntity));

        mockMvc.perform(post("/api/admin/produtos/1/validar").param("validador", "qa"))
                .andExpect(status().isConflict());

        verify(repo, never()).save(any());
    }

    @Test
    void publicarProductReturnsConflictWhenNotValidated() throws Exception {
        sampleEntity.setStatus(ProdutoStatus.IMPORTADO);
        when(repo.findById(1L)).thenReturn(Optional.of(sampleEntity));

        mockMvc.perform(post("/api/admin/produtos/1/publicar").param("validador", "qa"))
                .andExpect(status().isConflict());

        verify(repo, never()).save(any());
    }

    private ProdutoEntity buildEntity(Long id) {
        ProdutoEntity entity = new ProdutoEntity();
        entity.setId(id);
        entity.setNome("Produto " + id);
        entity.setDescricao("Descrição");
        entity.setPrecoVenda(BigDecimal.valueOf(12.50));
        entity.setCategoria("ANALGESICO");
        entity.setCodigoBarras("7891234567895");
        entity.setEstoque(5);
        entity.setDisponivel(false);
        entity.setStatus(ProdutoStatus.PUBLICADO);
        entity.setImagem("https://cdn.example.com/produtos/" + id + ".png");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setHashLegado("hash-" + id);
        return entity;
    }

    private ProdutoRequestDTO buildRequest() {
        ProdutoRequestDTO request = new ProdutoRequestDTO();
        request.setSku("SKU-001");
        request.setNome("Produto 1");
        request.setDescricao("Descrição");
        request.setPreco(BigDecimal.valueOf(12.50));
        request.setImagem("https://cdn.example.com/produtos/1.png");
        request.setCategoria("ANALGESICO");
        request.setCodigoBarras("7891234567895");
        request.setEstoque(5);
        request.setTenantId("tenant");
        request.setAtivo(false);
        return request;
    }
}
