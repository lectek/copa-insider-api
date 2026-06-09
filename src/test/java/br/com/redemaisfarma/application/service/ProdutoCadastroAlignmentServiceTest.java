package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.MetodoLeituraCodigoBarras;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoCadastroAlignmentServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    private ProdutoCadastroAlignmentService service;

    @BeforeEach
    void setUp() {
        this.service = new ProdutoCadastroAlignmentService(this.produtoRepository);
    }

    @Test
    void auditCountsUnknownStockDuplicateLegacyAndPdfOverlap() {
        ProdutoEntity unknownA = produto(10L, 100L, MetodoLeituraCodigoBarras.DESCONHECIDO, "Estoque fisico", "7891", "Dipirona");
        ProdutoEntity unknownB = produto(11L, 100L, MetodoLeituraCodigoBarras.DESCONHECIDO, "Estoque fisico", "7892", "Dipirona Z");
        ProdutoEntity unknownC = produto(12L, 200L, MetodoLeituraCodigoBarras.DESCONHECIDO, "Catalogo nacional", "7893", "Vitamina");
        ProdutoEntity pdf = produto(30L, 200L, MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA, "Catalogo local", "7893", "Vitamina local");

        when(this.produtoRepository.findAllByMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.DESCONHECIDO))
                .thenReturn(List.of(unknownA, unknownB, unknownC));
        when(this.produtoRepository.findAllByMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA))
                .thenReturn(List.of(pdf));

        ProdutoCadastroAlignmentService.AuditReport report = this.service.audit();

        assertThat(report.totalUnknown()).isEqualTo(3);
        assertThat(report.unknownStock()).isEqualTo(2);
        assertThat(report.unknownNonStock()).isEqualTo(1);
        assertThat(report.duplicateUnknownRows()).isEqualTo(2);
        assertThat(report.duplicateUnknownLegacyIds()).hasSize(1);
        assertThat(report.duplicateUnknownLegacyIds().getFirst().legacyId()).isEqualTo(100L);
        assertThat(report.pdfLegacyOverlaps()).hasSize(1);
        assertThat(report.pdfLegacyOverlaps().getFirst().legacyId()).isEqualTo(200L);
        assertThat(report.pdfLegacyOverlaps().getFirst().produtos())
                .extracting(ProdutoCadastroAlignmentService.ProdutoSummary::origem)
                .containsExactly("DESCONHECIDO", "PDF_CATALOGO_VENDA");
    }

    @Test
    void reclassifyUnknownStockDryRunDoesNotPersist() {
        ProdutoEntity unknownA = produto(10L, 100L, MetodoLeituraCodigoBarras.DESCONHECIDO, "Estoque fisico", "7891", "Dipirona");
        ProdutoEntity unknownB = produto(11L, 101L, MetodoLeituraCodigoBarras.DESCONHECIDO, "Catalogo nacional", "7892", "Vitamina");

        when(this.produtoRepository.findAllByMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.DESCONHECIDO))
                .thenReturn(List.of(unknownA, unknownB));
        when(this.produtoRepository.findAllByMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA))
                .thenReturn(List.of());

        ProdutoCadastroAlignmentService.RepairSummary summary = this.service.reclassifyUnknownStock(true);

        assertThat(summary.dryRun()).isTrue();
        assertThat(summary.candidateUnknownStockRows()).isEqualTo(1);
        assertThat(summary.appliedUnknownStockRows()).isZero();
        verify(this.produtoRepository, never()).saveAll(any());
        assertThat(unknownA.getMetodoLeituraCodigoBarras()).isEqualTo(MetodoLeituraCodigoBarras.DESCONHECIDO);
    }

    @Test
    void reclassifyUnknownStockApplyPersistsOnlyStockRows() {
        ProdutoEntity unknownA = produto(10L, 100L, MetodoLeituraCodigoBarras.DESCONHECIDO, "Estoque fisico", "7891", "Dipirona");
        ProdutoEntity unknownB = produto(11L, 101L, MetodoLeituraCodigoBarras.DESCONHECIDO, "Catalogo nacional", "7892", "Vitamina");

        when(this.produtoRepository.findAllByMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.DESCONHECIDO))
                .thenReturn(List.of(unknownA, unknownB));
        when(this.produtoRepository.findAllByMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA))
                .thenReturn(List.of());
        when(this.produtoRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ProdutoCadastroAlignmentService.RepairSummary summary = this.service.reclassifyUnknownStock(false);

        assertThat(summary.dryRun()).isFalse();
        assertThat(summary.candidateUnknownStockRows()).isEqualTo(1);
        assertThat(summary.appliedUnknownStockRows()).isEqualTo(1);
        assertThat(unknownA.getMetodoLeituraCodigoBarras()).isEqualTo(MetodoLeituraCodigoBarras.CSV_ESTOQUE);
        assertThat(unknownB.getMetodoLeituraCodigoBarras()).isEqualTo(MetodoLeituraCodigoBarras.DESCONHECIDO);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ProdutoEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(this.produtoRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).containsExactly(unknownA);
    }

    private ProdutoEntity produto(
            Long id,
            Long legacyId,
            MetodoLeituraCodigoBarras origem,
            String categoria,
            String codigoBarras,
            String nome
    ) {
        ProdutoEntity entity = new ProdutoEntity();
        entity.setId(id);
        entity.setLegacyId(legacyId);
        entity.setMetodoLeituraCodigoBarras(origem);
        entity.setCategoria(categoria);
        entity.setCodigoBarras(codigoBarras);
        entity.setNome(nome);
        entity.setEstoque(0);
        return entity;
    }
}
