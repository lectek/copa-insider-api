package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetaVendaDashboardServiceTest {

    @Mock
    private AppSettingService appSettingService;

    @Mock
    private CaixaResumoService caixaResumoService;

    private MetaVendaDashboardService service;

    @BeforeEach
    void setUp() {
        service = new MetaVendaDashboardService(
                appSettingService,
                caixaResumoService
        );
    }

    @Test
    void carregarPainelUsaMetaConfiguradaEIndicaQuandoBateu() {
        final LocalDate referencia = LocalDate.of(2026, 3, 12);
        when(appSettingService.getDecimal(
                MetaVendaDashboardService.META_DIARIA_KEY,
                MetaVendaDashboardService.META_DIARIA_PADRAO
        )).thenReturn(new BigDecimal("2000.00"));
        when(caixaResumoService.resumoDia(any(LocalDate.class)))
                .thenReturn(resumo(BigDecimal.ZERO));
        when(caixaResumoService.resumoDia(referencia))
                .thenReturn(resumo(new BigDecimal("2350.00")));

        final MetaVendaDashboardService.MetaVendaPainel painel =
                service.carregarPainel(referencia);

        assertThat(painel.metaDiaria()).isEqualByComparingTo("2000.00");
        assertThat(painel.vendasHoje()).isEqualByComparingTo("2350.00");
        assertThat(painel.metaBatidaHoje()).isTrue();
        assertThat(painel.diferencaHoje()).isEqualByComparingTo("350.00");
        assertThat(painel.tituloMensagem()).isEqualTo("Boa, voce bateu a meta.");
        assertThat(painel.comparativo()).hasSize(7);
    }

    @Test
    void atualizarMetaDiariaPersisteValorNormalizado() {
        final BigDecimal atualizado = service.atualizarMetaDiaria("2450,5");

        assertThat(atualizado).isEqualByComparingTo("2450.50");
        verify(appSettingService).upsert(
                eq(MetaVendaDashboardService.META_DIARIA_KEY),
                eq("2450.50"),
                eq("Meta diaria de vendas exibida no dashboard administrativo")
        );
    }

    @Test
    void atualizarMetaDiariaRejeitaValorInvalido() {
        assertThatThrownBy(() -> service.atualizarMetaDiaria("0"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Informe uma meta diaria maior que zero.");
    }

    private CaixaResumoService.CaixaResumo resumo(final BigDecimal totalVendas) {
        return new CaixaResumoService.CaixaResumo(
                LocalDate.of(2026, 3, 12),
                0L,
                totalVendas,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }
}
