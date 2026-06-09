package br.com.lectek.copainsider.adapters.inbound.web.controller.admin;

import br.com.lectek.copainsider.adapters.outbound.legacy.entity.ProdutoLegacyEntity;
import br.com.lectek.copainsider.adapters.outbound.legacy.repository.ProdutoLegacyRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.MetodoLeituraCodigoBarras;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.lectek.copainsider.application.core.exception.ImportInProgressException;
import br.com.lectek.copainsider.application.core.media.ImageStorageService;
import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import br.com.lectek.copainsider.application.service.CatalogoVendaDisponivelService;
import br.com.lectek.copainsider.application.service.EstoqueFisicoCsvService;
import br.com.lectek.copainsider.application.service.EstoqueFisicoImportService;
import br.com.lectek.copainsider.application.service.ProductCategoryBindingService;
import br.com.lectek.copainsider.application.service.ProductImageJobService;
import br.com.lectek.copainsider.application.service.ProdutoAdminService;
import br.com.lectek.copainsider.application.service.otp.OtpServicePort;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoCategoriaRepository;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = ProdutoAdminPageController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProdutoAdminPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProdutoAdminPageController produtoAdminPageController;

    @MockitoBean
    private ProdutoAdminService produtoAdminService;

    @MockitoBean
    private ProdutoRepository produtoRepository;

    @MockitoBean
    private AppSettingService appSettingService;

    @MockitoBean
    private ProdutoCategoriaRepository produtoCategoriaRepository;

    @MockitoBean
    private OtpServicePort otpServicePort;

    @MockitoBean
    private ImageStorageService imageStorageService;

    @MockitoBean
    private ProductImageJobService productImageJobService;

    @MockitoBean
    private ProductCategoryBindingService productCategoryBindingService;

    @MockitoBean
    private CatalogoVendaDisponivelService catalogoVendaDisponivelService;

    @MockitoBean
    private EstoqueFisicoCsvService estoqueFisicoCsvService;

    @MockitoBean
    private EstoqueFisicoImportService estoqueFisicoImportService;

    @MockitoBean
    private ProdutoLegacyRepository produtoLegacyRepository;

    @MockitoBean
    private ThymeleafViewResolver thymeleafViewResolver;

    @BeforeEach
    void setupAuthentication() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("produto-admin", "N/A",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        when(thymeleafViewResolver.resolveViewName(any(), any()))
                .thenReturn(new MappingJackson2JsonView());
        when(produtoRepository.searchPageByCategoria(any(), any(), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(produtoRepository.searchNaoDisponiveisByCategoria(any(), any(), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(produtoRepository.searchNaoDisponiveis(any(), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(produtoRepository.findAll(any(Pageable.class)))
                .thenReturn(Page.empty());
        when(produtoRepository.findComEstoqueBaixo(anyInt()))
                .thenReturn(List.of());
        when(produtoCategoriaRepository.findAllNomes())
                .thenReturn(List.of("Sem Categoria", "Medicacoes"));
        when(appSettingService.getBoolean(any(), anyBoolean()))
                .thenReturn(true);
        when(appSettingService.getInt(any(), anyInt()))
                .thenReturn(10);
        when(estoqueFisicoCsvService.search(any())).thenReturn(List.of());
        when(produtoLegacyRepository.findByCodigoBarras(any())).thenReturn(Optional.empty());
        when(produtoLegacyRepository.findByNomeContainingIgnoreCase(any())).thenReturn(List.of());
        when(productImageJobService.ensureImageLinkedFromLastSuccessfulJob(any()))
                .thenReturn(Optional.empty());
    }

    @AfterEach
    void cleanupAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void formRedirectsToNovo() throws Exception {
        mockMvc.perform(get("/admin/produtos/form"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/produtos/novo"));
    }

    @Test
    void listaProdutosPageReturnsCatalogoDoAdmin() throws Exception {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(15L);
        produto.setNome("Shampoo infantil");
        produto.setCategoria("Higiene");

        Page<ProdutoEntity> page = new PageImpl<>(List.of(produto), PageRequest.of(0, 20), 1);
        when(produtoAdminService.buscarPagina(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);
        when(produtoRepository.countPubliclySellable(eq(ProdutoRepository.PUBLIC_ALLOWED_SOURCES)))
                .thenReturn(7L);

        mockMvc.perform(get("/admin/produtos").param("q", "shampoo"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/produtos/lista"))
                .andExpect(model().attribute("q", equalTo("shampoo")))
                .andExpect(model().attribute("categoria", equalTo("")))
                .andExpect(jsonPath("$.produtos[0].id").value(15))
                .andExpect(jsonPath("$.produtos[0].nome").value("Shampoo infantil"))
                .andExpect(jsonPath("$.resumoProdutos.disponiveis").value(7));
    }

    @Test
    void listaProdutosLegadaRedirecionaParaRotaPrincipal() throws Exception {
        mockMvc.perform(get("/admin/produtos/lista")
                        .param("q", "dipirona")
                        .param("categoria", "Medicacoes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/admin/produtos**"))
                .andExpect(header().string("Location", containsString("q=dipirona")))
                .andExpect(header().string("Location", containsString("categoria=Medicacoes")));
    }

    @Test
    void novoProdutoPageAceitaPrefillParaItemNaoCadastrado() throws Exception {
        mockMvc.perform(get("/admin/produtos/novo")
                        .param("legacyId", "801")
                        .param("nome", "Vitamina C")
                        .param("descricao", "Vitamina C 1g")
                        .param("categoria", "Estoque fisico")
                        .param("codigoBarras", "7891112223334")
                        .param("estoque", "14")
                        .param("fabricante", "Lab X")
                        .param("unidade", "30 comprimidos")
                        .param("origem", "ESTOQUE_FISICO"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/produtos/form"))
                .andExpect(model().attribute("produtoPrefill", equalTo(true)))
                .andExpect(jsonPath("$.produto.legacyId").value(801))
                .andExpect(jsonPath("$.produto.nome").value("Vitamina C"))
                .andExpect(jsonPath("$.produto.descricao").value("Vitamina C 1g"))
                .andExpect(jsonPath("$.produto.categoria").value("Estoque fisico"))
                .andExpect(jsonPath("$.produto.codigoBarras").value("7891112223334"))
                .andExpect(jsonPath("$.produto.estoque").value(14))
                .andExpect(jsonPath("$.produto.fabricante").value("Lab X"))
                .andExpect(jsonPath("$.produto.unidade").value("30 comprimidos"))
                .andExpect(jsonPath("$.produto.disponivel").value(true))
                .andExpect(jsonPath("$.produto.metodoLeituraCodigoBarras")
                        .value(MetodoLeituraCodigoBarras.MANUAL.name()));
    }

    @Test
    void naoProntosTodosPageReturnsOk() throws Exception {
        mockMvc.perform(get("/admin/produtos/nao-prontos/todos"))
                .andExpect(status().isOk());
    }

    @Test
    void naoProntosTodosPageRetornaPendentesDoBanco() throws Exception {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(77L);
        produto.setLegacyId(700L);
        produto.setNome("Amoxicilina");
        produto.setDescricao("Amoxicilina 500mg");
        produto.setCategoria("Estoque fisico");
        produto.setCodigoBarras("7890007770001");
        produto.setEstoque(5);

        Page<ProdutoEntity> page = new PageImpl<>(List.of(produto), PageRequest.of(0, 1000), 1);
        when(produtoRepository.searchPageByCategoria(any(), any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/admin/produtos/nao-prontos/todos").param("q", "amoxi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingTotal").value(1))
                .andExpect(jsonPath("$.pendingItems[0].id").value(77))
                .andExpect(jsonPath("$.pendingItems[0].origem").value("CATALOGO"))
                .andExpect(jsonPath("$.pendingItems[0].nome").value("Amoxicilina"));
    }

    @Test
    void naoProntosTodosQuandoFiltroNaoEncontraNadaVoltaParaListaCompleta() throws Exception {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(91L);
        produto.setNome("Pantoprazol");
        produto.setCategoria("Estoque fisico");
        produto.setEstoque(6);

        when(produtoRepository.searchPageByCategoria(any(), any(), any(Pageable.class)))
                .thenAnswer(invocation -> {
                    String q = invocation.getArgument(0, String.class);
                    Pageable pageable = invocation.getArgument(2, Pageable.class);
                    if ("zzz".equals(q)) {
                        return Page.empty(pageable);
                    }
                    if (q == null) {
                        return new PageImpl<>(List.of(produto), pageable, 1);
                    }
                    return Page.empty(pageable);
                });

        mockMvc.perform(get("/admin/produtos/nao-prontos/todos").param("q", "zzz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingTotal").value(1))
                .andExpect(jsonPath("$.pendingItems[0].id").value(91))
                .andExpect(jsonPath("$.pendingItems[0].nome").value("Pantoprazol"));
    }

    @Test
    void naoProntosEndpointRetornaItensDoBanco() throws Exception {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(10L);
        produto.setLegacyId(123L);
        produto.setNome("Dipirona");
        produto.setDescricao("Dipirona 500mg");
        produto.setCategoria("Estoque fisico");
        produto.setCodigoBarras("7890001112223");
        produto.setEstoque(9);

        Page<ProdutoEntity> page = new PageImpl<>(List.of(produto), PageRequest.of(0, 12), 1);
        when(produtoRepository.searchNaoDisponiveisByCategoria(any(), any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/admin/produtos/nao-prontos")
                        .param("q", "dip")
                        .param("page", "0")
                        .param("size", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].id").value(10))
                .andExpect(jsonPath("$.items[0].origem").value("CATALOGO_PENDENTE"))
                .andExpect(jsonPath("$.items[0].nome").value("Dipirona"));
    }

    @Test
    void naoProntosEndpointRetornaPaginacaoDoBanco() throws Exception {
        ProdutoEntity produto1 = new ProdutoEntity();
        produto1.setId(11L);
        produto1.setNome("Nimesulida");
        produto1.setCategoria("Estoque fisico");
        produto1.setEstoque(3);

        ProdutoEntity produto2 = new ProdutoEntity();
        produto2.setId(12L);
        produto2.setNome("Nimesulida Gotas");
        produto2.setCategoria("Estoque fisico");
        produto2.setEstoque(2);

        Page<ProdutoEntity> page1 = new PageImpl<>(List.of(produto1), PageRequest.of(0, 1), 2);
        Page<ProdutoEntity> page2 = new PageImpl<>(List.of(produto2), PageRequest.of(1, 1), 2);
        when(produtoRepository.searchNaoDisponiveisByCategoria(any(), any(), any(Pageable.class)))
                .thenReturn(page1)
                .thenReturn(page2);

        mockMvc.perform(get("/admin/produtos/nao-prontos")
                        .param("q", "nim")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.hasNext").value(true));
    }

    @Test
    void naoProntosEndpointUsaFallbackQuandoCategoriaNaoRetornaItens() throws Exception {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(33L);
        produto.setNome("Ibuprofeno");
        produto.setCategoria("Estoque Físico");
        produto.setEstoque(2);

        when(produtoRepository.searchNaoDisponiveisByCategoria(any(), any(), any(Pageable.class)))
                .thenReturn(Page.empty());
        when(produtoRepository.searchNaoDisponiveis(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(produto), PageRequest.of(0, 12), 1));

        mockMvc.perform(get("/admin/produtos/nao-prontos")
                        .param("q", "ibu")
                        .param("page", "0")
                        .param("size", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].id").value(33))
                .andExpect(jsonPath("$.items[0].nome").value("Ibuprofeno"));
    }

    @Test
    void naoProntosEndpointCombinaBancoComCsv() throws Exception {
        ProdutoEntity doBanco = new ProdutoEntity();
        doBanco.setId(44L);
        doBanco.setLegacyId(440L);
        doBanco.setNome("Omeprazol");
        doBanco.setCategoria("Estoque fisico");
        doBanco.setCodigoBarras("7891231231231");
        doBanco.setEstoque(4);

        Page<ProdutoEntity> page = new PageImpl<>(List.of(doBanco), PageRequest.of(0, 12), 1);
        when(produtoRepository.searchNaoDisponiveisByCategoria(any(), any(), any(Pageable.class)))
                .thenReturn(page);

        EstoqueFisicoCsvService.EstoqueItem novoDoCsv = new EstoqueFisicoCsvService.EstoqueItem(
                445L,
                "7898887776665",
                "Ranitidina",
                "Lab Q",
                9,
                BigDecimal.valueOf(20.00),
                BigDecimal.valueOf(18.50),
                "ranitidina 445 7898887776665 lab q"
        );
        when(estoqueFisicoCsvService.search(eq("ome"))).thenReturn(List.of(novoDoCsv));

        mockMvc.perform(get("/admin/produtos/nao-prontos")
                        .param("q", "ome")
                        .param("page", "0")
                        .param("size", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.items[0].id").value(44))
                .andExpect(jsonPath("$.items[1].origem").value("ESTOQUE_FISICO"))
                .andExpect(jsonPath("$.items[1].legacyId").value(445))
                .andExpect(jsonPath("$.items[1].nome").value("Ranitidina"));
    }

    @Test
    void naoProntosTodosPageRetornaVazioQuandoCatalogoNaoTemItens() throws Exception {
        mockMvc.perform(get("/admin/produtos/nao-prontos/todos").param("q", "lorat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingTotal").value(0))
                .andExpect(jsonPath("$.listedCount").value(0));
    }

    @Test
    void naoProntosEndpointUsaFallbackDoCsvQuandoBancoVazio() throws Exception {
        EstoqueFisicoCsvService.EstoqueItem csvItem = new EstoqueFisicoCsvService.EstoqueItem(
                801L,
                "7891112223334",
                "Vitamina C",
                "Lab X",
                14,
                BigDecimal.valueOf(25.00),
                BigDecimal.valueOf(21.50),
                "vitamina c 801 7891112223334 lab x"
        );
        when(estoqueFisicoCsvService.search(eq("vit"))).thenReturn(List.of(csvItem));

        mockMvc.perform(get("/admin/produtos/nao-prontos")
                        .param("q", "vit")
                        .param("page", "0")
                        .param("size", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].origem").value("ESTOQUE_FISICO"))
                .andExpect(jsonPath("$.items[0].legacyId").value(801))
                .andExpect(jsonPath("$.items[0].nome").value("Vitamina C"));
    }

    @Test
    void naoProntosTodosPageUsaApenasCatalogoComPaginacao() throws Exception {
        ProdutoEntity doBanco = new ProdutoEntity();
        doBanco.setId(77L);
        doBanco.setLegacyId(700L);
        doBanco.setNome("Amoxicilina");
        doBanco.setDescricao("Amoxicilina 500mg");
        doBanco.setCategoria("Estoque fisico");
        doBanco.setCodigoBarras("7890007770001");
        doBanco.setEstoque(5);

        Page<ProdutoEntity> page = new PageImpl<>(List.of(doBanco), PageRequest.of(1, 10), 21);
        when(produtoRepository.searchPageByCategoria(any(), any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/admin/produtos/nao-prontos/todos")
                        .param("q", "amoxi")
                        .param("page", "1")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingTotal").value(21))
                .andExpect(jsonPath("$.listedCount").value(1))
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.hasPrev").value(true))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.pendingItems[0].id").value(77))
                .andExpect(jsonPath("$.pendingItems[0].origem").value("CATALOGO"));
    }

    @Test
    void naoProntosEndpointNormalizaFiltroEmBrancoParaNull() throws Exception {
        mockMvc.perform(get("/admin/produtos/nao-prontos")
                        .param("q", "   ")
                        .param("page", "0")
                        .param("size", "12"))
                .andExpect(status().isOk());

        verify(produtoRepository).searchNaoDisponiveisByCategoria(
                isNull(),
                eq("Estoque fisico"),
                any(Pageable.class)
        );
    }

    @Test
    void importarEstoqueFisicoRedirectsToNovo() throws Exception {
        when(estoqueFisicoImportService.importarTodosComoNaoDisponiveis())
                .thenReturn(new EstoqueFisicoImportService.ImportacaoResumo(1, 1, 0, 0, 0));

        mockMvc.perform(post("/admin/produtos/importar-estoque-fisico"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/produtos/novo"));
    }

    @Test
    void importarEstoqueFisicoComUploadRedirectsToNovo() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "arquivoCsv",
                "estoque.csv",
                "text/csv",
                "COD BARRAS;CODIGO;PRODUTO\n789;1;Produto".getBytes()
        );
        when(estoqueFisicoImportService.importarTodosComoNaoDisponiveis(any(java.io.InputStream.class)))
                .thenReturn(new EstoqueFisicoImportService.ImportacaoResumo(2, 1, 1, 0, 0));

        mockMvc.perform(multipart("/admin/produtos/importar-estoque-fisico").file(file))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/produtos/novo"))
                .andExpect(flash().attribute("success", containsString("via upload")));
    }

    @Test
    void parseCatalogoPdfLinesIgnoraCabecalhoRodapeETotaisDoEstoqueFisico() throws Exception {
        Method method = ProdutoAdminPageController.class
                .getDeclaredMethod("parseCatalogoPdfLines", List.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<Object> itens = (List<Object>) method.invoke(this.produtoAdminPageController, List.of(
                "ESTOQUE FISICO",
                "SAUDE MAIS FARMA",
                "COD. BARRAS      CODIGO    PRODUTO                          LABORATORIO                   LOCALIZACAO         TRIB.     NCM       SALDO   PR. TABELA   PR. VENDA                M.A.      ULT. COMPRA",
                "                           BUBBALOO HORTELA MENTA           POLYBALAS DISTIRIBUIDORA",
                "7895800440797     60003                                                                                           ST   17041000    29                     R$ 0,50      R$ 0,00 70,77 %         R$ 0,29      R$ 8,49",
                "                           #DES.MONANGE AER.ANTI            PROCTER & GAMBLE",
                "7896235353904     58204                                                                                           ST   33072010     2                    R$ 11,79      R$ 0,00 62,17 %         R$ 7,27     R$ 14,54",
                "                           DET.150M",
                "SUB-TOTAL DA PAGINA:                                                                                                                                   R$ 508,04                                         R$ 332,58",
                "Gerado Por ANDRE TAVARES",
                "24/04/2026      18:07:31                                                                                                                                                                                    1 de 63"
        ));

        assertThat(itens).hasSize(2);
        Object primeiro = itens.getFirst();
        assertThat(invokeRecordMethod(primeiro, "codigoBarras")).isEqualTo("7895800440797");
        assertThat(invokeRecordMethod(primeiro, "legacyId")).isEqualTo(60003L);
        assertThat(invokeRecordMethod(primeiro, "nome")).isEqualTo("BUBBALOO HORTELA MENTA");
        assertThat(invokeRecordMethod(primeiro, "quantidade")).isEqualTo(29);
        assertThat(invokeRecordMethod(primeiro, "precoVenda")).isEqualTo(new BigDecimal("0.50"));
    }

    @Test
    void parseCatalogoPdfLinesAceitaBlocosExtraidosPeloOpenPdf() throws Exception {
        Method method = ProdutoAdminPageController.class
                .getDeclaredMethod("parseCatalogoPdfLines", List.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<Object> itens = (List<Object>) method.invoke(this.produtoAdminPageController, List.of(
                "SAUDE MAIS FARMA",
                " CÓD. BARRASCÓDIGOPRODUTOLABORATÓRIOTRIB.NCMSALDOPR. TABELAPR. VENDA",
                " PR.",
                " PROMOÇÃO",
                " M.A. ULT. COMPRA",
                " TOTAL ULT.",
                " COMPRA",
                " LOCALIZAÇÃO",
                " 7895800440797 60003",
                " BUBBALOO HORTELA MENTA",
                " POLYBALAS DISTIRIBUIDORA",
                " ST 17041000 29 R$ 0,50R$ 0,0070,77 %R$ 0,29R$ 8,49",
                " 7896235353904 58204",
                " #DES.MONANGE AER.ANTI",
                " DET.150M",
                " PROCTER & GAMBLE",
                " ST 33072010 2 R$ 11,79R$ 0,0062,17 %R$ 7,27R$ 14,54",
                " SUB-TOTAL DA PÁGINA:R$ 508,04R$ 332,58"
        ));

        assertThat(itens).hasSize(2);
        Object primeiro = itens.getFirst();
        assertThat(invokeRecordMethod(primeiro, "codigoBarras")).isEqualTo("7895800440797");
        assertThat(invokeRecordMethod(primeiro, "legacyId")).isEqualTo(60003L);
        assertThat(invokeRecordMethod(primeiro, "nome")).isEqualTo("BUBBALOO HORTELA MENTA POLYBALAS DISTIRIBUIDORA");
        assertThat(invokeRecordMethod(primeiro, "quantidade")).isEqualTo(29);
        assertThat(invokeRecordMethod(primeiro, "precoVenda")).isEqualTo(new BigDecimal("0.50"));
    }

    @Test
    void confirmarCatalogoPdfPublicaProdutosParaVenda() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("admin.catalogo.pdf.preview", List.of(
                catalogoPdfPreviewItem(58018L, "7898133131837", "XAROPE DE GUACO 150ML", 1, new BigDecimal("23.99")),
                catalogoPdfPreviewItem(59090L, "7898665751695", "ZINCO MAIS 60CAPS", 2, new BigDecimal("34.99"))
        ));

        when(produtoRepository.findByAnyCodigo("7898133131837")).thenReturn(Optional.empty());
        when(produtoRepository.findByAnyCodigo("7898665751695")).thenReturn(Optional.empty());
        when(produtoRepository.findByLegacyId(58018L)).thenReturn(Optional.empty());
        when(produtoRepository.findByLegacyId(59090L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/admin/produtos/importar-catalogo-pdf/confirmar").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/produtos/novo"))
                .andExpect(flash().attribute("success", containsString("Importacao confirmada")));

        verify(produtoRepository).saveAll(argThat((Iterable<ProdutoEntity> produtos) -> {
            List<ProdutoEntity> lista = new ArrayList<>();
            produtos.forEach(lista::add);
            assertThat(lista).hasSize(2);
            assertThat(lista).allSatisfy(produto -> {
                assertThat(produto.getDisponivel()).isTrue();
                assertThat(produto.getStatus()).isEqualTo(ProdutoStatus.PUBLICADO);
                assertThat(produto.getPublicadoEm()).isNotNull();
                assertThat(produto.getDespublicadoEm()).isNull();
                assertThat(produto.getMetodoLeituraCodigoBarras())
                        .isEqualTo(MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA);
            });
            assertThat(lista.get(0).getEstoque()).isEqualTo(1);
            assertThat(lista.get(0).getPrecoVenda()).isEqualTo(new BigDecimal("23.99"));
            assertThat(lista.get(1).getEstoque()).isEqualTo(2);
            assertThat(lista.get(1).getPrecoVenda()).isEqualTo(new BigDecimal("34.99"));
            return true;
        }));
    }

    @Test
    void confirmarCatalogoPdfPromoveProdutoExistenteDoEstoqueParaVenda() throws Exception {
        ProdutoEntity existente = new ProdutoEntity();
        existente.setId(77L);
        existente.setLegacyId(58018L);
        existente.setNome("Xarope importado do estoque");
        existente.setCategoria("Estoque fisico");
        existente.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.CSV_ESTOQUE);
        existente.setStatus(ProdutoStatus.IMPORTADO);
        existente.setDisponivel(false);
        existente.setEstoque(0);
        existente.setPrecoVenda(BigDecimal.ZERO);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("admin.catalogo.pdf.preview", List.of(
                catalogoPdfPreviewItem(58018L, "7898133131837", "XAROPE DE GUACO 150ML", 3, new BigDecimal("23.99"))
        ));

        when(produtoRepository.findByAnyCodigo("7898133131837")).thenReturn(Optional.empty());
        when(produtoRepository.findByLegacyId(58018L)).thenReturn(Optional.of(existente));

        mockMvc.perform(post("/admin/produtos/importar-catalogo-pdf/confirmar").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/produtos/novo"))
                .andExpect(flash().attribute("success", containsString("Importacao confirmada")));

        verify(produtoRepository).saveAll(argThat((Iterable<ProdutoEntity> produtos) -> {
            List<ProdutoEntity> lista = new ArrayList<>();
            produtos.forEach(lista::add);
            assertThat(lista).hasSize(1);
            ProdutoEntity publicado = lista.getFirst();
            assertThat(publicado.getId()).isEqualTo(77L);
            assertThat(publicado.getCodigoBarras()).isEqualTo("7898133131837");
            assertThat(publicado.getMetodoLeituraCodigoBarras()).isEqualTo(MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA);
            assertThat(publicado.getDisponivel()).isTrue();
            assertThat(publicado.getStatus()).isEqualTo(ProdutoStatus.PUBLICADO);
            assertThat(publicado.getEstoque()).isEqualTo(3);
            assertThat(publicado.getPrecoVenda()).isEqualTo(new BigDecimal("23.99"));
            assertThat(publicado.getPublicadoEm()).isNotNull();
            assertThat(publicado.getDespublicadoEm()).isNull();
            return true;
        }));
    }

    @Test
    void confirmarCatalogoPdfNaoPublicaProdutoSemCodigoValido() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("admin.catalogo.pdf.preview", List.of(
                catalogoPdfPreviewItem(52832L, "46153", "UNILEVER", 699, new BigDecimal("19.99"))
        ));

        when(produtoRepository.findByAnyCodigo("46153")).thenReturn(Optional.empty());
        when(produtoRepository.findByLegacyId(52832L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/admin/produtos/importar-catalogo-pdf/confirmar").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/produtos/novo"))
                .andExpect(flash().attribute("success", containsString("Importacao confirmada")));

        verify(produtoRepository).saveAll(argThat((Iterable<ProdutoEntity> produtos) -> {
            List<ProdutoEntity> lista = new ArrayList<>();
            produtos.forEach(lista::add);
            assertThat(lista).hasSize(1);
            ProdutoEntity produto = lista.getFirst();
            assertThat(produto.getNome()).isEqualTo("UNILEVER");
            assertThat(produto.getEstoque()).isEqualTo(699);
            assertThat(produto.getDisponivel()).isFalse();
            assertThat(produto.getStatus()).isEqualTo(ProdutoStatus.IMPORTADO);
            assertThat(produto.getPublicadoEm()).isNull();
            assertThat(produto.getDespublicadoEm()).isNotNull();
            return true;
        }));
    }

    @Test
    void importarEstoqueFisicoRetornaWarningQuandoOutraImportacaoJaEstaEmExecucao() throws Exception {
        when(estoqueFisicoImportService.importarTodosComoNaoDisponiveis())
                .thenThrow(new ImportInProgressException("Ja existe uma importacao de estoque fisico em execucao."));

        mockMvc.perform(post("/admin/produtos/importar-estoque-fisico"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/produtos/novo"))
                .andExpect(flash().attribute("warning", "Ja existe uma importacao de estoque fisico em execucao."));
    }

    @Test
    void sincronizarCatalogoVendaLocalRedirecionaParaPaginaSolicitada() throws Exception {
        when(catalogoVendaDisponivelService.sincronizarCatalogoDisponivel())
                .thenReturn(new CatalogoVendaDisponivelService.ImportacaoResumo(2, 1, 1, 0, 0));

        mockMvc.perform(post("/admin/produtos/sincronizar-catalogo-venda-local")
                        .param("redirect", "/admin/produtos/novo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/produtos/novo"));

        verify(catalogoVendaDisponivelService).sincronizarCatalogoDisponivel();
    }

    @Test
    void buscaRapidaUsaCatalogoNacionalAntesDasSugestoesLocais() throws Exception {
        ProdutoLegacyEntity legacy = new ProdutoLegacyEntity();
        legacy.setId(902);
        legacy.setNome("Ibuprofeno Suspensao");
        legacy.setCodigoBarras("7891234567890");
        legacy.setApresentacao("100MG/ML 20ML");

        ProdutoEntity sugestaoLocal = new ProdutoEntity();
        sugestaoLocal.setId(88L);
        sugestaoLocal.setNome("Sugestao local");
        sugestaoLocal.setCategoria("Catalogo");

        when(produtoLegacyRepository.findByNomeContainingIgnoreCase(eq("ibup")))
                .thenReturn(List.of(legacy));
        when(produtoRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sugestaoLocal), PageRequest.of(0, 8), 1));

        mockMvc.perform(get("/admin/produtos/busca-rapida")
                        .param("q", "ibup")
                        .param("limit", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].origem").value("CATALOGO_NACIONAL"))
                .andExpect(jsonPath("$[0].legacyId").value(902))
                .andExpect(jsonPath("$[0].nome").value("Ibuprofeno Suspensao"));
    }

    @Test
    void deleteProdutoRetornaNoContentQuandoExiste() throws Exception {
        when(produtoRepository.existsById(10L)).thenReturn(true);

        mockMvc.perform(delete("/admin/produtos/10"))
                .andExpect(status().isNoContent());

        verify(produtoRepository).deleteById(10L);
    }

    @Test
    void deleteProdutoRetornaNotFoundQuandoNaoExiste() throws Exception {
        when(produtoRepository.existsById(11L)).thenReturn(false);

        mockMvc.perform(delete("/admin/produtos/11"))
                .andExpect(status().isNotFound());

        verify(produtoRepository, never()).deleteById(11L);
    }

    @Test
    void deleteProdutoViaPaginaRedirecionaComSucesso() throws Exception {
        when(produtoRepository.existsById(10L)).thenReturn(true);

        mockMvc.perform(post("/admin/produtos/10/excluir"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/produtos"))
                .andExpect(flash().attribute("success", "Produto excluido com sucesso."));

        verify(produtoRepository).deleteById(10L);
    }

    @Test
    void deleteProdutoViaPaginaRetornaWarningQuandoNaoExiste() throws Exception {
        when(produtoRepository.existsById(11L)).thenReturn(false);

        mockMvc.perform(post("/admin/produtos/11/excluir"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/produtos"))
                .andExpect(flash().attribute("warning", "Produto nao encontrado para exclusao."));

        verify(produtoRepository, never()).deleteById(11L);
    }

    @Test
    void editarProdutoPageCarregaProdutoNoModelo() throws Exception {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(52746L);
        produto.setNome("Produto teste");
        produto.setCategoria("Higiene");
        produto.setCodigoBarras("7891234567895");

        when(produtoRepository.findById(52746L)).thenReturn(Optional.of(produto));

        mockMvc.perform(get("/admin/produtos/52746/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/produtos/editar"))
                .andExpect(model().attribute("produtoId", equalTo(52746L)))
                .andExpect(jsonPath("$.produto.id").value(52746))
                .andExpect(jsonPath("$.produto.nome").value("Produto teste"))
                .andExpect(jsonPath("$.produto.codigoBarras").value("7891234567895"));
    }

    @Test
    void criarProdutoRemoveArquivosPersistidosQuandoSalvarGaleriaFalha() throws Exception {
        ProdutoEntity salvo = new ProdutoEntity();
        salvo.setId(10L);
        salvo.setNome("Produto teste");
        salvo.setCategoria("Higiene");

        when(produtoRepository.save(any(ProdutoEntity.class)))
                .thenReturn(salvo)
                .thenThrow(new IllegalStateException("db"));
        when(imageStorageService.saveProductImage(eq(10L), any()))
                .thenReturn("/media/products/produto-10-a.png");

        MockMultipartFile file = new MockMultipartFile(
                "imagemFile",
                "foto.png",
                "image/png",
                "ok".getBytes()
        );

        mockMvc.perform(multipart("/admin/produtos")
                        .file(file)
                        .param("nome", "Produto teste")
                        .param("categoria", "Higiene"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/produtos/novo?erro=imagem_upload"));

        verify(imageStorageService).deleteProductImageByUrl("/media/products/produto-10-a.png");
        verify(produtoRepository).deleteById(10L);
    }

    @Test
    void publicarAptosEmLotePublicaSomenteProdutosComDadosMinimos() throws Exception {
        ProdutoEntity apto = new ProdutoEntity();
        apto.setId(1L);
        apto.setNome("Produto apto");
        apto.setCodigoBarras("7891234567890");
        apto.setPrecoVenda(BigDecimal.valueOf(11.90));
        apto.setEstoque(7);
        apto.setImagem("https://cdn.exemplo.com/p1.png");
        apto.setDisponivel(false);
        apto.setStatus(ProdutoStatus.IMPORTADO);
        apto.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.MANUAL);

        ProdutoEntity bloqueado = new ProdutoEntity();
        bloqueado.setId(2L);
        bloqueado.setNome("Produto sem imagem");
        bloqueado.setCodigoBarras("7891234567891");
        bloqueado.setPrecoVenda(BigDecimal.valueOf(9.90));
        bloqueado.setEstoque(10);
        bloqueado.setImagem(null);
        bloqueado.setDisponivel(false);
        bloqueado.setStatus(ProdutoStatus.IMPORTADO);
        bloqueado.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.MANUAL);

        Page<ProdutoEntity> page = new PageImpl<>(List.of(apto, bloqueado), PageRequest.of(0, 1000), 2);
        when(produtoRepository.searchNaoDisponiveisByCategoria(any(), any(), any(Pageable.class)))
                .thenReturn(page);
        when(produtoRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(apto, bloqueado));

        mockMvc.perform(post("/admin/produtos/nao-prontos/publicar-aptos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/produtos/nao-prontos/todos"));

        verify(produtoRepository).saveAll(argThat((Iterable<ProdutoEntity> publicados) -> {
            List<ProdutoEntity> lista = new ArrayList<>();
            publicados.forEach(lista::add);
            assertThat(lista).hasSize(1);
            ProdutoEntity publicado = lista.get(0);
            assertThat(publicado.getId()).isEqualTo(1L);
            assertThat(publicado.getStatus()).isEqualTo(ProdutoStatus.PUBLICADO);
            assertThat(publicado.getDisponivel()).isTrue();
            assertThat(publicado.getPublicadoEm()).isNotNull();
            return true;
        }));
    }

    @Test
    void publicarAptosEmLoteNaoSalvaQuandoNaoHaPendentes() throws Exception {
        when(produtoRepository.searchNaoDisponiveisByCategoria(any(), any(), any(Pageable.class)))
                .thenReturn(Page.empty());

        mockMvc.perform(post("/admin/produtos/nao-prontos/publicar-aptos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/produtos/nao-prontos/todos"));

        verify(produtoRepository, never()).saveAll(any());
    }

    @Test
    void publicarAptosEmLoteVinculaImagemDoUltimoJobAntesDePublicar() throws Exception {
        ProdutoEntity semImagem = new ProdutoEntity();
        semImagem.setId(3L);
        semImagem.setNome("Produto IA");
        semImagem.setCodigoBarras("7891234567892");
        semImagem.setPrecoVenda(BigDecimal.valueOf(19.90));
        semImagem.setEstoque(8);
        semImagem.setImagem(null);
        semImagem.setDisponivel(false);
        semImagem.setStatus(ProdutoStatus.IMPORTADO);
        semImagem.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.MANUAL);

        Page<ProdutoEntity> page = new PageImpl<>(List.of(semImagem), PageRequest.of(0, 1000), 1);
        when(produtoRepository.searchNaoDisponiveisByCategoria(any(), any(), any(Pageable.class)))
                .thenReturn(page);
        when(produtoRepository.findAllById(List.of(3L))).thenReturn(List.of(semImagem));
        when(productImageJobService.ensureImageLinkedFromLastSuccessfulJob(eq(3L)))
                .thenReturn(Optional.of("https://cdn.exemplo.com/produto-ia.png"));

        mockMvc.perform(post("/admin/produtos/nao-prontos/publicar-aptos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/produtos/nao-prontos/todos"));

        verify(produtoRepository).saveAll(argThat((Iterable<ProdutoEntity> publicados) -> {
            List<ProdutoEntity> lista = new ArrayList<>();
            publicados.forEach(lista::add);
            assertThat(lista).hasSize(1);
            ProdutoEntity publicado = lista.get(0);
            assertThat(publicado.getId()).isEqualTo(3L);
            assertThat(publicado.getImagem()).isEqualTo("https://cdn.exemplo.com/produto-ia.png");
            assertThat(publicado.getStatus()).isEqualTo(ProdutoStatus.PUBLICADO);
            assertThat(publicado.getDisponivel()).isTrue();
            return true;
        }));
    }

    @Test
    void publicarAptosEmLoteIgnoraProdutosComOrigemNaoPublicavel() throws Exception {
        ProdutoEntity legado = new ProdutoEntity();
        legado.setId(4L);
        legado.setNome("Produto legado");
        legado.setCodigoBarras("7891234567890");
        legado.setPrecoVenda(BigDecimal.valueOf(11.90));
        legado.setEstoque(6);
        legado.setImagem("https://cdn.exemplo.com/p4.png");
        legado.setDisponivel(false);
        legado.setStatus(ProdutoStatus.IMPORTADO);
        legado.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.LEGADO);

        Page<ProdutoEntity> page = new PageImpl<>(List.of(legado), PageRequest.of(0, 1000), 1);
        when(produtoRepository.searchNaoDisponiveisByCategoria(any(), any(), any(Pageable.class)))
                .thenReturn(page);
        when(produtoRepository.findAllById(List.of(4L))).thenReturn(List.of(legado));

        mockMvc.perform(post("/admin/produtos/nao-prontos/publicar-aptos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/produtos/nao-prontos/todos"))
                .andExpect(flash().attribute("warning",
                        "Nenhum produto apto para publicar. Corrija origem, codigo de barras, imagem, preco e estoque dos pendentes."));

        verify(produtoRepository, never()).saveAll(any());
    }

    @Test
    void publicarProdutoFluxoRetornaConflictQuandoOrigemNaoEhPublicavel() throws Exception {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(15L);
        produto.setStatus(ProdutoStatus.VALIDADO);
        produto.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.LEGADO);
        produto.setCodigoBarras("7891234567890");
        produto.setPrecoVenda(BigDecimal.valueOf(25.90));
        produto.setEstoque(3);
        produto.setImagem("https://cdn.exemplo.com/p15.png");

        when(produtoRepository.findById(15L)).thenReturn(Optional.of(produto));

        mockMvc.perform(post("/admin/produtos/15/publicar").param("validador", "teste"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Produto precisa ser salvo como cadastro local antes de publicar."));

        verify(produtoRepository, never()).save(any());
    }

    private static Object invokeRecordMethod(Object target, String methodName) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        return method.invoke(target);
    }

    private static Object catalogoPdfPreviewItem(
            Long legacyId,
            String codigoBarras,
            String nome,
            int quantidade,
            BigDecimal precoVenda
    ) throws Exception {
        Class<?> itemClass = Class.forName(ProdutoAdminPageController.class.getName() + "$CatalogoPdfPreviewItem");
        Constructor<?> constructor = itemClass.getDeclaredConstructor(Long.class, String.class, String.class, int.class, BigDecimal.class);
        constructor.setAccessible(true);
        return constructor.newInstance(legacyId, codigoBarras, nome, quantidade, precoVenda);
    }
}
