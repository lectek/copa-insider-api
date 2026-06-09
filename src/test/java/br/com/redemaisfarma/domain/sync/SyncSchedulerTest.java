package br.com.redemaisfarma.domain.sync;

import br.com.redemaisfarma.adapters.outbound.legacy.dto.LegacyProdutoDTO;
import br.com.redemaisfarma.adapters.outbound.legacy.port.LegacyProdutoPort;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.MetodoLeituraCodigoBarras;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncSchedulerTest {

    @Mock
    private LegacyProdutoPort legacyProdutoPort;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private CheckpointService checkpointService;

    @Test
    void runCriaRegistroLegadoSemSobrescreverProdutoLocalPorCodigoDeBarras() {
        LocalDateTime since = LocalDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);
        LocalDateTime updatedAt = LocalDateTime.now().minusMinutes(2);
        LegacyProdutoDTO dto = new LegacyProdutoDTO(
                55L,
                "Dipirona legado",
                "SOLUCAO 20ML",
                "7890001112223",
                BigDecimal.valueOf(12.90),
                BigDecimal.ZERO,
                18,
                updatedAt
        );

        ProdutoEntity local = new ProdutoEntity();
        local.setId(9L);
        local.setLegacyId(900L);
        local.setCodigoBarras("7890001112223");
        local.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA);
        local.setPrecoVenda(BigDecimal.valueOf(21.50));
        local.setEstoque(3);
        local.setDisponivel(Boolean.TRUE);
        local.setStatus(ProdutoStatus.PUBLICADO);

        when(this.checkpointService.readSince(eq("firebird.produtos"), any(LocalDateTime.class))).thenReturn(since);
        when(this.legacyProdutoPort.fetchChangedSince(since, 0, 500)).thenReturn(List.of(dto));
        when(this.produtoRepository.findByLegacyId(55L)).thenReturn(Optional.empty());
        when(this.produtoRepository.findByAnyCodigo("7890001112223")).thenReturn(Optional.of(local));
        when(this.produtoRepository.save(any(ProdutoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SyncScheduler scheduler = new SyncScheduler(this.legacyProdutoPort, this.produtoRepository, this.checkpointService);
        scheduler.run();

        ArgumentCaptor<ProdutoEntity> captor = ArgumentCaptor.forClass(ProdutoEntity.class);
        verify(this.produtoRepository).save(captor.capture());
        ProdutoEntity saved = captor.getValue();

        assertThat(saved.getId()).isNull();
        assertThat(saved.getLegacyId()).isEqualTo(55L);
        assertThat(saved.getCodigoOriginal()).isEqualTo(7890001112223L);
        assertThat(saved.getCodigoBarras()).isEqualTo("7890001112223");
        assertThat(saved.getMetodoLeituraCodigoBarras()).isEqualTo(MetodoLeituraCodigoBarras.LEGADO);
        assertThat(saved.getDisponivel()).isFalse();
        assertThat(saved.getStatus()).isEqualTo(ProdutoStatus.IMPORTADO);
        assertThat(saved.getPrecoVenda()).isEqualByComparingTo("12.90");
        assertThat(saved.getEstoque()).isEqualTo(18);

        assertThat(local.getPrecoVenda()).isEqualByComparingTo("21.50");
        assertThat(local.getEstoque()).isEqualTo(3);
        assertThat(local.getMetodoLeituraCodigoBarras()).isEqualTo(MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA);

        verify(this.checkpointService).writeSince("firebird.produtos", updatedAt);
    }

    @Test
    void runIgnoraAtualizacaoQuandoLegacyIdJaPertenceAProdutoLocalVendavel() {
        LocalDateTime since = LocalDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);
        LocalDateTime updatedAt = LocalDateTime.now().minusMinutes(1);
        LegacyProdutoDTO dto = new LegacyProdutoDTO(
                77L,
                "Produto legado",
                "APRESENTACAO",
                "7899990001112",
                BigDecimal.valueOf(8.50),
                null,
                5,
                updatedAt
        );

        ProdutoEntity local = new ProdutoEntity();
        local.setId(10L);
        local.setLegacyId(77L);
        local.setCodigoBarras("7899990001112");
        local.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.MANUAL);
        local.setPrecoVenda(BigDecimal.valueOf(15.00));
        local.setEstoque(2);
        local.setDisponivel(Boolean.TRUE);
        local.setStatus(ProdutoStatus.PUBLICADO);

        when(this.checkpointService.readSince(eq("firebird.produtos"), any(LocalDateTime.class))).thenReturn(since);
        when(this.legacyProdutoPort.fetchChangedSince(since, 0, 500)).thenReturn(List.of(dto));
        when(this.produtoRepository.findByLegacyId(77L)).thenReturn(Optional.of(local));

        SyncScheduler scheduler = new SyncScheduler(this.legacyProdutoPort, this.produtoRepository, this.checkpointService);
        scheduler.run();

        verify(this.produtoRepository, never()).save(any(ProdutoEntity.class));
        assertThat(local.getPrecoVenda()).isEqualByComparingTo("15.00");
        assertThat(local.getEstoque()).isEqualTo(2);
        assertThat(local.getMetodoLeituraCodigoBarras()).isEqualTo(MetodoLeituraCodigoBarras.MANUAL);
        verify(this.checkpointService).writeSince("firebird.produtos", updatedAt);
    }
}
