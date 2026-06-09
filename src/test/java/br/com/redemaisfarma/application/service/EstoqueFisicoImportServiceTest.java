package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.MetodoLeituraCodigoBarras;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.application.core.exception.ImportInProgressException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class EstoqueFisicoImportServiceTest {

    @Mock
    private EstoqueFisicoCsvService estoqueFisicoCsvService;

    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private ProductCategoryBindingService categoryBindingService;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query acquireLockQuery;

    @Mock
    private Query releaseLockQuery;

    private EstoqueFisicoImportService service;

    @BeforeEach
    void setUp() {
        this.service = new EstoqueFisicoImportService(
                this.estoqueFisicoCsvService,
                this.produtoRepository,
                this.categoryBindingService
        );
        ReflectionTestUtils.setField(this.service, "em", this.entityManager);
    }

    @Test
    void importarTodosComoNaoDisponiveisShouldPreserveIncomingBarcodeInCodigoOriginalWhenConflictExists() {
        this.stubLockAcquired();

        EstoqueFisicoCsvService.EstoqueItem item = new EstoqueFisicoCsvService.EstoqueItem(
                55L,
                "7890001112223",
                "Dipirona conflito",
                "Fabricante X",
                4,
                new BigDecimal("15.00"),
                new BigDecimal("12.90"),
                "7890001112223 Dipirona conflito"
        );

        ProdutoEntity owner = new ProdutoEntity();
        owner.setId(9L);
        owner.setCodigoBarras("7890001112223");
        owner.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA);
        owner.setStatus(ProdutoStatus.PUBLICADO);

        when(this.estoqueFisicoCsvService.search("")).thenReturn(List.of(item));
        when(this.produtoRepository.findAllByLegacyIdOrderByIdAsc(55L)).thenReturn(List.of());
        when(this.produtoRepository.findByAnyCodigo("7890001112223")).thenReturn(Optional.of(owner));
        List<ProdutoEntity> savedBatch = new ArrayList<>();
        when(this.produtoRepository.saveAll(any())).thenAnswer(invocation -> {
            Iterable<ProdutoEntity> iterable = invocation.getArgument(0);
            iterable.forEach(savedBatch::add);
            return savedBatch;
        });

        EstoqueFisicoImportService.ImportacaoResumo resumo = this.service.importarTodosComoNaoDisponiveis();

        ProdutoEntity saved = savedBatch.getFirst();

        assertThat(resumo.lidos()).isEqualTo(1);
        assertThat(resumo.inseridos()).isEqualTo(1);
        assertThat(saved.getLegacyId()).isEqualTo(55L);
        assertThat(saved.getCodigoOriginal()).isEqualTo(7890001112223L);
        assertThat(saved.getCodigoBarras()).isEqualTo("7890001112223");
        assertThat(saved.getMetodoLeituraCodigoBarras()).isEqualTo(MetodoLeituraCodigoBarras.CSV_ESTOQUE);
        assertThat(saved.getStatus()).isEqualTo(ProdutoStatus.IMPORTADO);
        assertThat(saved.getDisponivel()).isFalse();
    }

    @Test
    void importarTodosComoNaoDisponiveisShouldUseUploadedCsvStreamWhenProvided() throws Exception {
        this.stubLockAcquired();

        EstoqueFisicoCsvService.EstoqueItem item = new EstoqueFisicoCsvService.EstoqueItem(
                77L,
                "7890001113334",
                "Produto upload",
                "Fabricante Upload",
                9,
                new BigDecimal("21.00"),
                new BigDecimal("18.50"),
                "7890001113334 Produto upload"
        );

        when(this.estoqueFisicoCsvService.parseUploadedCsv(any(InputStream.class))).thenReturn(List.of(item));
        when(this.produtoRepository.findAllByLegacyIdOrderByIdAsc(77L)).thenReturn(List.of());
        when(this.produtoRepository.findByAnyCodigo("7890001113334")).thenReturn(Optional.empty());
        List<ProdutoEntity> savedBatch = new ArrayList<>();
        when(this.produtoRepository.saveAll(any())).thenAnswer(invocation -> {
            Iterable<ProdutoEntity> iterable = invocation.getArgument(0);
            iterable.forEach(savedBatch::add);
            return savedBatch;
        });

        EstoqueFisicoImportService.ImportacaoResumo resumo = this.service.importarTodosComoNaoDisponiveis(
                new ByteArrayInputStream("csv".getBytes())
        );

        assertThat(resumo.lidos()).isEqualTo(1);
        assertThat(resumo.inseridos()).isEqualTo(1);
        assertThat(savedBatch.getFirst().getCodigoBarras()).isEqualTo("7890001113334");
        assertThat(savedBatch.getFirst().getLegacyId()).isEqualTo(77L);
    }

    @Test
    void importarTodosComoNaoDisponiveisShouldIgnoreProtectedProductWhenLegacyIdBelongsToCatalogoLocal() {
        this.stubLockAcquired();

        EstoqueFisicoCsvService.EstoqueItem item = new EstoqueFisicoCsvService.EstoqueItem(
                91L,
                "7890001114445",
                "Produto protegido",
                "Fabricante Protegido",
                3,
                new BigDecimal("18.00"),
                new BigDecimal("15.90"),
                "7890001114445 Produto protegido"
        );

        ProdutoEntity protegido = new ProdutoEntity();
        protegido.setId(91L);
        protegido.setLegacyId(91L);
        protegido.setCategoria("Catalogo local");
        protegido.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA);
        protegido.setStatus(ProdutoStatus.PUBLICADO);

        when(this.estoqueFisicoCsvService.search("")).thenReturn(List.of(item));
        when(this.produtoRepository.findAllByLegacyIdOrderByIdAsc(91L)).thenReturn(List.of(protegido));

        EstoqueFisicoImportService.ImportacaoResumo resumo = this.service.importarTodosComoNaoDisponiveis();

        assertThat(resumo.lidos()).isEqualTo(1);
        assertThat(resumo.inseridos()).isZero();
        assertThat(resumo.atualizados()).isZero();
        assertThat(resumo.ignorados()).isEqualTo(1);
        assertThat(protegido.getMetodoLeituraCodigoBarras()).isEqualTo(MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA);
        assertThat(protegido.getCategoria()).isEqualTo("Catalogo local");
        verify(this.produtoRepository, never()).saveAll(any());
    }

    @Test
    void importarTodosComoNaoDisponiveisShouldPreferExistingStockEntryWhenLegacyIdHasMixedSources() {
        this.stubLockAcquired();

        EstoqueFisicoCsvService.EstoqueItem item = new EstoqueFisicoCsvService.EstoqueItem(
                77L,
                "7890001113334",
                "Produto estoque",
                "Fabricante Estoque",
                9,
                new BigDecimal("21.00"),
                new BigDecimal("18.50"),
                "7890001113334 Produto estoque"
        );

        ProdutoEntity protegido = new ProdutoEntity();
        protegido.setId(10L);
        protegido.setLegacyId(77L);
        protegido.setCategoria("Catalogo local");
        protegido.setCodigoBarras("7890001113334");
        protegido.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA);
        protegido.setStatus(ProdutoStatus.PUBLICADO);

        ProdutoEntity estoque = new ProdutoEntity();
        estoque.setId(11L);
        estoque.setLegacyId(77L);
        estoque.setCategoria("Estoque fisico");
        estoque.setCodigoBarras("7890001113334");
        estoque.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.CSV_ESTOQUE);
        estoque.setStatus(ProdutoStatus.IMPORTADO);
        estoque.setEstoque(1);

        when(this.estoqueFisicoCsvService.search("")).thenReturn(List.of(item));
        when(this.produtoRepository.findAllByLegacyIdOrderByIdAsc(77L)).thenReturn(List.of(protegido, estoque));
        List<ProdutoEntity> savedBatch = new ArrayList<>();
        when(this.produtoRepository.saveAll(any())).thenAnswer(invocation -> {
            Iterable<ProdutoEntity> iterable = invocation.getArgument(0);
            iterable.forEach(savedBatch::add);
            return savedBatch;
        });

        EstoqueFisicoImportService.ImportacaoResumo resumo = this.service.importarTodosComoNaoDisponiveis();

        assertThat(resumo.lidos()).isEqualTo(1);
        assertThat(resumo.atualizados()).isEqualTo(1);
        assertThat(savedBatch).hasSize(1);
        assertThat(savedBatch.getFirst().getId()).isEqualTo(11L);
        assertThat(savedBatch.getFirst().getMetodoLeituraCodigoBarras()).isEqualTo(MetodoLeituraCodigoBarras.CSV_ESTOQUE);
        assertThat(savedBatch.getFirst().getEstoque()).isEqualTo(9);
        assertThat(protegido.getMetodoLeituraCodigoBarras()).isEqualTo(MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA);
    }

    @Test
    void importarTodosComoNaoDisponiveisShouldIgnoreBarcodeOnlyItemWhenBarcodeBelongsToProtectedSource() {
        this.stubLockAcquired();

        EstoqueFisicoCsvService.EstoqueItem item = new EstoqueFisicoCsvService.EstoqueItem(
                null,
                "7890001115556",
                "Produto barcode protegido",
                "Fabricante Z",
                2,
                new BigDecimal("10.00"),
                new BigDecimal("8.90"),
                "7890001115556 Produto barcode protegido"
        );

        ProdutoEntity protegido = new ProdutoEntity();
        protegido.setId(15L);
        protegido.setCodigoBarras("7890001115556");
        protegido.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.MANUAL);

        when(this.estoqueFisicoCsvService.search("")).thenReturn(List.of(item));
        when(this.produtoRepository.findByAnyCodigo("7890001115556")).thenReturn(Optional.of(protegido));

        EstoqueFisicoImportService.ImportacaoResumo resumo = this.service.importarTodosComoNaoDisponiveis();

        assertThat(resumo.lidos()).isEqualTo(1);
        assertThat(resumo.inseridos()).isZero();
        assertThat(resumo.atualizados()).isZero();
        assertThat(resumo.ignorados()).isEqualTo(1);
        verify(this.produtoRepository, never()).saveAll(any());
    }

    @Test
    void importarTodosComoNaoDisponiveisShouldFailFastWhenAnotherImportIsRunning() {
        this.stubLockUnavailable();

        assertThatThrownBy(() -> this.service.importarTodosComoNaoDisponiveis())
                .isInstanceOf(ImportInProgressException.class)
                .hasMessageContaining("importacao de estoque fisico");

        verifyNoInteractions(this.estoqueFisicoCsvService, this.produtoRepository);
    }

    private void stubLockAcquired() {
        when(this.entityManager.createNativeQuery(contains("GET_LOCK"))).thenReturn(this.acquireLockQuery);
        when(this.acquireLockQuery.setParameter("lockName", "redemaisfarma:estoque-fisico-import"))
                .thenReturn(this.acquireLockQuery);
        when(this.acquireLockQuery.setParameter("timeoutSeconds", 0))
                .thenReturn(this.acquireLockQuery);
        when(this.acquireLockQuery.getSingleResult()).thenReturn(1);

        when(this.entityManager.createNativeQuery(contains("RELEASE_LOCK"))).thenReturn(this.releaseLockQuery);
        when(this.releaseLockQuery.setParameter("lockName", "redemaisfarma:estoque-fisico-import"))
                .thenReturn(this.releaseLockQuery);
        when(this.releaseLockQuery.getSingleResult()).thenReturn(1);
    }

    private void stubLockUnavailable() {
        when(this.entityManager.createNativeQuery(contains("GET_LOCK"))).thenReturn(this.acquireLockQuery);
        when(this.acquireLockQuery.setParameter("lockName", "redemaisfarma:estoque-fisico-import"))
                .thenReturn(this.acquireLockQuery);
        when(this.acquireLockQuery.setParameter("timeoutSeconds", 0))
                .thenReturn(this.acquireLockQuery);
        when(this.acquireLockQuery.getSingleResult()).thenReturn(0);
    }
}
