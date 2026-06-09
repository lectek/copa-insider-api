package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.adapters.outbound.legacy.entity.ProdutoLegacyEntity;
import br.com.lectek.copainsider.adapters.outbound.legacy.repository.ProdutoLegacyRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.MetodoLeituraCodigoBarras;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.lectek.copainsider.domain.sync.SyncStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogoVendaDisponivelServiceTest {

    @TempDir
    Path tempDir;

    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private ProductCategoryBindingService categoryBindingService;

    @Mock
    private ProdutoLegacyRepository produtoLegacyRepository;

    @Mock
    private ObjectProvider<ProdutoLegacyRepository> legacyRepositoryProvider;

    private CatalogoVendaDisponivelService service;

    @BeforeEach
    void setUp() {
        this.service = new CatalogoVendaDisponivelService(
                this.produtoRepository,
                this.categoryBindingService,
                new DefaultResourceLoader(),
                this.legacyRepositoryProvider
        );
    }

    @Test
    void sincronizarCatalogoDisponivelAtualizaProdutosDoPdfEDesativaItensAntigos() throws Exception {
        Path tsv = this.tempDir.resolve("catalogo-venda-local.tsv");
        Files.writeString(tsv, String.join("\n",
                "legacyId\tcodigoBarras\tnome\tdescricao\tfabricante\tprecoVenda\testoque",
                "100\t7890001112223\tDipirona Infantil\tSOL ORAL 100MG/ML 20ML\tNeo Quimica\t19.90\t7",
                "200\t\tFralda Premium\tPacote G\tMarca X\t8.50\t0"
        ), StandardCharsets.UTF_8);
        ReflectionTestUtils.setField(this.service, "resourceLocation", tsv.toUri().toString());

        ProdutoEntity existente = new ProdutoEntity();
        existente.setId(20L);
        existente.setLegacyId(200L);
        existente.setNome("Fralda antiga");
        existente.setDescricao("Descricao antiga");
        existente.setCategoria("Higiene");
        existente.setEstoque(5);
        existente.setPrecoVenda(BigDecimal.valueOf(10.00));
        existente.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA);
        existente.setStatus(ProdutoStatus.IMPORTADO);

        ProdutoEntity obsoleto = new ProdutoEntity();
        obsoleto.setId(30L);
        obsoleto.setLegacyId(999L);
        obsoleto.setDisponivel(Boolean.TRUE);
        obsoleto.setEstoque(4);
        obsoleto.setStatus(ProdutoStatus.PUBLICADO);
        obsoleto.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA);

        when(this.produtoRepository.findAllByLegacyIdOrderByIdAsc(100L)).thenReturn(List.of());
        when(this.produtoRepository.findAllByLegacyIdOrderByIdAsc(200L)).thenReturn(List.of(existente));
        when(this.produtoRepository.findByAnyCodigo(anyString())).thenReturn(Optional.empty());
        when(this.produtoRepository.findAllByMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA))
                .thenReturn(List.of(obsoleto));

        List<List<ProdutoEntity>> savedBatches = new ArrayList<>();
        when(this.produtoRepository.saveAll(any())).thenAnswer(invocation -> {
            Iterable<ProdutoEntity> iterable = invocation.getArgument(0);
            List<ProdutoEntity> batch = new ArrayList<>();
            iterable.forEach(batch::add);
            savedBatches.add(batch);
            return batch;
        });

        CatalogoVendaDisponivelService.ImportacaoResumo resumo = this.service.sincronizarCatalogoDisponivel();

        assertThat(resumo.lidos()).isEqualTo(2);
        assertThat(resumo.inseridos()).isEqualTo(1);
        assertThat(resumo.atualizados()).isEqualTo(1);
        assertThat(resumo.inalterados()).isZero();
        assertThat(resumo.desativados()).isEqualTo(1);
        assertThat(savedBatches).hasSize(2);

        ProdutoEntity novo = findByLegacyId(savedBatches.get(0), 100L);
        assertThat(novo.getId()).isNull();
        assertThat(novo.getCodigoBarras()).isEqualTo("7890001112223");
        assertThat(novo.getNome()).isEqualTo("Dipirona Infantil");
        assertThat(novo.getFabricante()).isEqualTo("Neo Quimica");
        assertThat(novo.getPrecoVenda()).isEqualByComparingTo("19.90");
        assertThat(novo.getEstoque()).isEqualTo(7);
        assertThat(novo.getUnidade()).hasSize(20).startsWith("SOL ORAL 100MG/ML");
        assertThat(novo.getDisponivel()).isTrue();
        assertThat(novo.getStatus()).isEqualTo(ProdutoStatus.PUBLICADO);
        assertThat(novo.getStatusSync()).isEqualTo(SyncStatus.SINCRONIZADO);
        assertThat(novo.getMetodoLeituraCodigoBarras()).isEqualTo(MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA);

        ProdutoEntity atualizado = findByLegacyId(savedBatches.get(0), 200L);
        assertThat(atualizado.getId()).isEqualTo(20L);
        assertThat(atualizado.getCategoria()).isEqualTo("Higiene");
        assertThat(atualizado.getPrecoVenda()).isEqualByComparingTo("8.50");
        assertThat(atualizado.getEstoque()).isZero();
        assertThat(atualizado.getDisponivel()).isFalse();
        assertThat(atualizado.getStatus()).isEqualTo(ProdutoStatus.IMPORTADO);
        assertThat(atualizado.getStatusSync()).isEqualTo(SyncStatus.PENDENTE);

        ProdutoEntity desativado = savedBatches.get(1).getFirst();
        assertThat(desativado.getLegacyId()).isEqualTo(999L);
        assertThat(desativado.getDisponivel()).isFalse();
        assertThat(desativado.getEstoque()).isZero();
        assertThat(desativado.getStatus()).isEqualTo(ProdutoStatus.IMPORTADO);
        assertThat(desativado.getDespublicadoEm()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void sincronizarCatalogoDisponivelMantemCodigoDeBarrasAtualQuandoOBarcodeDoPdfJaPertenceAOutroProduto() throws Exception {
        Path tsv = this.tempDir.resolve("catalogo-venda-local-conflito.tsv");
        Files.writeString(tsv, String.join("\n",
                "legacyId\tcodigoBarras\tnome\tdescricao\tfabricante\tprecoVenda\testoque",
                "100\t7908780000000\tProduto Atualizado\tUNIDADE TESTE\tMarca Y\t12.30\t4"
        ), StandardCharsets.UTF_8);
        ReflectionTestUtils.setField(this.service, "resourceLocation", tsv.toUri().toString());

        ProdutoEntity destino = new ProdutoEntity();
        destino.setId(10L);
        destino.setLegacyId(100L);
        destino.setCodigoBarras("7890001112223");
        destino.setCategoria("Catalogo local");
        destino.setStatus(ProdutoStatus.IMPORTADO);

        ProdutoEntity donoDoBarcode = new ProdutoEntity();
        donoDoBarcode.setId(11L);
        donoDoBarcode.setCodigoBarras("7908780000000");

        when(this.produtoRepository.findAllByLegacyIdOrderByIdAsc(100L)).thenReturn(List.of(destino));
        when(this.produtoRepository.findByAnyCodigo("7908780000000")).thenReturn(Optional.of(donoDoBarcode));
        when(this.produtoRepository.findAllByMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA))
                .thenReturn(List.of());
        when(this.produtoRepository.saveAll(any())).thenAnswer(invocation -> {
            Iterable<ProdutoEntity> iterable = invocation.getArgument(0);
            List<ProdutoEntity> batch = new ArrayList<>();
            iterable.forEach(batch::add);
            return batch;
        });

        CatalogoVendaDisponivelService.ImportacaoResumo resumo = this.service.sincronizarCatalogoDisponivel();

        assertThat(resumo.atualizados()).isEqualTo(1);
        assertThat(destino.getCodigoBarras()).isEqualTo("7890001112223");
        assertThat(destino.getNome()).isEqualTo("Produto Atualizado");
        assertThat(destino.getDisponivel()).isTrue();
    }

    @Test
    void sincronizarCatalogoDisponivelUsaBarcodeDoCatalogoNacionalQuandoPdfNaoTrazCodigo() throws Exception {
        Path tsv = this.tempDir.resolve("catalogo-venda-local-sem-barcode.tsv");
        Files.writeString(tsv, String.join("\n",
                "legacyId\tcodigoBarras\tnome\tdescricao\tfabricante\tprecoVenda\testoque",
                "321\t\tSabonete Liquido\tREFIL 200ML\tMarca Z\t15.40\t9"
        ), StandardCharsets.UTF_8);
        ReflectionTestUtils.setField(this.service, "resourceLocation", tsv.toUri().toString());

        ProdutoLegacyEntity legacy = new ProdutoLegacyEntity();
        legacy.setId(321);
        legacy.setCodigoBarras("7896541230008");

        when(this.legacyRepositoryProvider.getIfAvailable()).thenReturn(this.produtoLegacyRepository);
        when(this.produtoLegacyRepository.findAllById(List.of(321))).thenReturn(List.of(legacy));
        when(this.produtoRepository.findAllByLegacyIdOrderByIdAsc(321L)).thenReturn(List.of());
        when(this.produtoRepository.findByAnyCodigo("7896541230008")).thenReturn(Optional.empty());
        when(this.produtoRepository.findAllByMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA))
                .thenReturn(List.of());
        List<List<ProdutoEntity>> savedBatches = new ArrayList<>();
        when(this.produtoRepository.saveAll(any())).thenAnswer(invocation -> {
            Iterable<ProdutoEntity> iterable = invocation.getArgument(0);
            List<ProdutoEntity> batch = new ArrayList<>();
            iterable.forEach(batch::add);
            savedBatches.add(batch);
            return batch;
        });

        CatalogoVendaDisponivelService.ImportacaoResumo resumo = this.service.sincronizarCatalogoDisponivel();

        assertThat(resumo.inseridos()).isEqualTo(1);
        assertThat(savedBatches).hasSize(1);
        ProdutoEntity novo = findByLegacyId(savedBatches.getFirst(), 321L);
        assertThat(novo.getCodigoBarras()).isEqualTo("7896541230008");
        assertThat(novo.getDisponivel()).isTrue();
        assertThat(novo.getStatus()).isEqualTo(ProdutoStatus.PUBLICADO);
    }

    @Test
    void sincronizarCatalogoDisponivelIgnoraCadastroManualProtegidoQuandoLegacyIdJaExiste() throws Exception {
        Path tsv = this.tempDir.resolve("catalogo-venda-local-protegido-legacy.tsv");
        Files.writeString(tsv, String.join("\n",
                "legacyId\tcodigoBarras\tnome\tdescricao\tfabricante\tprecoVenda\testoque",
                "701\t7891110002223\tProduto Manual\tCAIXA 10\tMarca M\t22.90\t5"
        ), StandardCharsets.UTF_8);
        ReflectionTestUtils.setField(this.service, "resourceLocation", tsv.toUri().toString());

        ProdutoEntity manual = new ProdutoEntity();
        manual.setId(70L);
        manual.setLegacyId(701L);
        manual.setCodigoBarras("7891110002223");
        manual.setCategoria("Catalogo local");
        manual.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.MANUAL);
        manual.setStatus(ProdutoStatus.PUBLICADO);

        when(this.produtoRepository.findAllByLegacyIdOrderByIdAsc(701L)).thenReturn(List.of(manual));
        when(this.produtoRepository.findAllByMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA))
                .thenReturn(List.of());

        CatalogoVendaDisponivelService.ImportacaoResumo resumo = this.service.sincronizarCatalogoDisponivel();

        assertThat(resumo.lidos()).isEqualTo(1);
        assertThat(resumo.inseridos()).isZero();
        assertThat(resumo.atualizados()).isZero();
        assertThat(resumo.inalterados()).isEqualTo(1);
        assertThat(manual.getMetodoLeituraCodigoBarras()).isEqualTo(MetodoLeituraCodigoBarras.MANUAL);
    }

    @Test
    void sincronizarCatalogoDisponivelIgnoraCadastroManualProtegidoQuandoBarcodeJaExiste() throws Exception {
        Path tsv = this.tempDir.resolve("catalogo-venda-local-protegido-barcode.tsv");
        Files.writeString(tsv, String.join("\n",
                "legacyId\tcodigoBarras\tnome\tdescricao\tfabricante\tprecoVenda\testoque",
                "702\t7891110003334\tProduto Barcode Manual\tUNIDADE\tMarca N\t18.40\t6"
        ), StandardCharsets.UTF_8);
        ReflectionTestUtils.setField(this.service, "resourceLocation", tsv.toUri().toString());

        ProdutoEntity manual = new ProdutoEntity();
        manual.setId(71L);
        manual.setCodigoBarras("7891110003334");
        manual.setCategoria("Catalogo local");
        manual.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.API);
        manual.setStatus(ProdutoStatus.PUBLICADO);

        when(this.produtoRepository.findAllByLegacyIdOrderByIdAsc(702L)).thenReturn(List.of());
        when(this.produtoRepository.findByAnyCodigo("7891110003334")).thenReturn(Optional.of(manual));
        when(this.produtoRepository.findAllByMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA))
                .thenReturn(List.of());

        CatalogoVendaDisponivelService.ImportacaoResumo resumo = this.service.sincronizarCatalogoDisponivel();

        assertThat(resumo.lidos()).isEqualTo(1);
        assertThat(resumo.inseridos()).isZero();
        assertThat(resumo.atualizados()).isZero();
        assertThat(resumo.inalterados()).isEqualTo(1);
        assertThat(manual.getMetodoLeituraCodigoBarras()).isEqualTo(MetodoLeituraCodigoBarras.API);
    }

    @Test
    void sincronizarCatalogoDisponivelPodePromoverCadastroDeEstoqueParaCatalogoLocal() throws Exception {
        Path tsv = this.tempDir.resolve("catalogo-venda-local-promove-estoque.tsv");
        Files.writeString(tsv, String.join("\n",
                "legacyId\tcodigoBarras\tnome\tdescricao\tfabricante\tprecoVenda\testoque",
                "703\t7891110004445\tProduto Estoque\tCAIXA 20\tMarca O\t31.50\t11"
        ), StandardCharsets.UTF_8);
        ReflectionTestUtils.setField(this.service, "resourceLocation", tsv.toUri().toString());

        ProdutoEntity estoque = new ProdutoEntity();
        estoque.setId(72L);
        estoque.setLegacyId(703L);
        estoque.setCodigoBarras("7891110004445");
        estoque.setCategoria("Estoque fisico");
        estoque.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.CSV_ESTOQUE);
        estoque.setStatus(ProdutoStatus.IMPORTADO);

        when(this.produtoRepository.findAllByLegacyIdOrderByIdAsc(703L)).thenReturn(List.of(estoque));
        when(this.produtoRepository.findAllByMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA))
                .thenReturn(List.of());
        List<List<ProdutoEntity>> savedBatches = new ArrayList<>();
        when(this.produtoRepository.saveAll(any())).thenAnswer(invocation -> {
            Iterable<ProdutoEntity> iterable = invocation.getArgument(0);
            List<ProdutoEntity> batch = new ArrayList<>();
            iterable.forEach(batch::add);
            savedBatches.add(batch);
            return batch;
        });

        CatalogoVendaDisponivelService.ImportacaoResumo resumo = this.service.sincronizarCatalogoDisponivel();

        assertThat(resumo.atualizados()).isEqualTo(1);
        assertThat(savedBatches).hasSize(1);
        ProdutoEntity promovido = findByLegacyId(savedBatches.getFirst(), 703L);
        assertThat(promovido.getId()).isEqualTo(72L);
        assertThat(promovido.getMetodoLeituraCodigoBarras()).isEqualTo(MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA);
        assertThat(promovido.getCategoria()).isEqualTo("Catalogo local");
        assertThat(promovido.getDisponivel()).isTrue();
        assertThat(promovido.getStatus()).isEqualTo(ProdutoStatus.PUBLICADO);
    }

    @Test
    void sincronizarCatalogoDisponivelNaoDisponibilizaProdutoComEstoqueZerado() throws Exception {
        Path tsv = this.tempDir.resolve("catalogo-venda-local-estoque-zero.tsv");
        Files.writeString(tsv, String.join("\n",
                "legacyId\tcodigoBarras\tnome\tdescricao\tfabricante\tprecoVenda\testoque",
                "804\t7891110005558\tProduto Sem Estoque\tCAIXA 10\tMarca Z\t12.90\t0"
        ), StandardCharsets.UTF_8);
        ReflectionTestUtils.setField(this.service, "resourceLocation", tsv.toUri().toString());

        when(this.produtoRepository.findAllByLegacyIdOrderByIdAsc(804L)).thenReturn(List.of());
        when(this.produtoRepository.findByAnyCodigo("7891110005558")).thenReturn(Optional.empty());
        when(this.produtoRepository.findAllByMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA))
                .thenReturn(List.of());
        List<List<ProdutoEntity>> savedBatches = new ArrayList<>();
        when(this.produtoRepository.saveAll(any())).thenAnswer(invocation -> {
            Iterable<ProdutoEntity> iterable = invocation.getArgument(0);
            List<ProdutoEntity> batch = new ArrayList<>();
            iterable.forEach(batch::add);
            savedBatches.add(batch);
            return batch;
        });

        CatalogoVendaDisponivelService.ImportacaoResumo resumo = this.service.sincronizarCatalogoDisponivel();

        assertThat(resumo.inseridos()).isEqualTo(1);
        ProdutoEntity produto = findByLegacyId(savedBatches.getFirst(), 804L);
        assertThat(produto.getCodigoBarras()).isEqualTo("7891110005558");
        assertThat(produto.getEstoque()).isZero();
        assertThat(produto.getDisponivel()).isFalse();
        assertThat(produto.getStatus()).isEqualTo(ProdutoStatus.IMPORTADO);
        assertThat(produto.getPublicadoEm()).isNull();
        assertThat(produto.getDespublicadoEm()).isNotNull();
        assertThat(produto.getStatusSync()).isEqualTo(SyncStatus.PENDENTE);
    }

    private static ProdutoEntity findByLegacyId(List<ProdutoEntity> items, Long legacyId) {
        return items.stream()
                .filter(item -> legacyId.equals(item.getLegacyId()))
                .findFirst()
                .orElseThrow();
    }
}
