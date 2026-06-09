package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MetaVendaDashboardService {

    static final String META_DIARIA_KEY = "dashboard.meta_venda_diaria.valor";
    static final BigDecimal META_DIARIA_PADRAO = new BigDecimal("2000.00");

    private static final String META_DIARIA_DESCRICAO =
            "Meta diaria de vendas exibida no dashboard administrativo";
    private static final int DIAS_COMPARATIVO = 7;
    private static final Locale LOCALE_PT_BR = Locale.forLanguageTag("pt-BR");

    private final AppSettingService appSettingService;
    private final CaixaResumoService caixaResumoService;

    public MetaVendaDashboardService(
            final AppSettingService appSettingServiceValue,
            final CaixaResumoService caixaResumoServiceValue
    ) {
        this.appSettingService = appSettingServiceValue;
        this.caixaResumoService = caixaResumoServiceValue;
    }

    @Transactional(readOnly = true)
    public MetaVendaPainel carregarPainel(final LocalDate referencia) {
        final LocalDate diaReferencia = referencia == null
                ? LocalDate.now()
                : referencia;
        final BigDecimal metaDiaria = metaDiariaAtual();
        final MetaVendaDiaView hoje = construirDia(diaReferencia, metaDiaria);
        final List<MetaVendaDiaView> comparativo = new ArrayList<>();

        for (int index = 1; index <= DIAS_COMPARATIVO; index++) {
            comparativo.add(construirDia(diaReferencia.minusDays(index), metaDiaria));
        }

        final String titulo;
        final String mensagem;
        final String badgeClass;
        final String badgeLabel;

        if (hoje.metaBatida()) {
            if (hoje.diferencaAbsoluta().compareTo(BigDecimal.ZERO) == 0) {
                titulo = "Boa, meta cravada hoje.";
                mensagem = "Voce bateu exatamente a meta diaria de "
                        + formatCurrency(metaDiaria) + ".";
            } else {
                titulo = "Boa, voce bateu a meta.";
                mensagem = "Passamos " + formatCurrency(hoje.diferencaAbsoluta())
                        + " acima da meta diaria.";
            }
            badgeClass = "badge--success";
            badgeLabel = "Meta batida";
        } else {
            titulo = "Quase la.";
            mensagem = "Faltou " + formatCurrency(hoje.diferencaAbsoluta())
                    + " para bater a meta diaria.";
            badgeClass = "badge--warning";
            badgeLabel = "Meta em andamento";
        }

        final String alertaResumo;
        if (hoje.metaBatida()) {
            alertaResumo = hoje.diferencaAbsoluta().compareTo(BigDecimal.ZERO) == 0
                    ? "Boa, voce bateu exatamente a meta do dia."
                    : "Boa, voce bateu a meta do dia e passou "
                            + formatCurrency(hoje.diferencaAbsoluta()) + ".";
        } else {
            alertaResumo = "Quase la, faltou "
                    + formatCurrency(hoje.diferencaAbsoluta())
                    + " para bater a meta do dia.";
        }

        return new MetaVendaPainel(
                metaDiaria,
                hoje.vendas(),
                hoje.diferencaAbsoluta(),
                hoje.metaBatida(),
                titulo,
                mensagem,
                badgeClass,
                badgeLabel,
                alertaResumo,
                comparativo
        );
    }

    @Transactional
    public BigDecimal atualizarMetaDiaria(final String rawValor) {
        final BigDecimal valor = parseValor(rawValor);
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Informe uma meta diaria maior que zero."
            );
        }

        final BigDecimal normalized = money(valor);
        appSettingService.upsert(
                META_DIARIA_KEY,
                normalized.toPlainString(),
                META_DIARIA_DESCRICAO
        );
        return normalized;
    }

    @Transactional(readOnly = true)
    public BigDecimal metaDiariaAtual() {
        return money(
                appSettingService.getDecimal(
                        META_DIARIA_KEY,
                        META_DIARIA_PADRAO
                )
        );
    }

    private MetaVendaDiaView construirDia(
            final LocalDate dia,
            final BigDecimal metaDiaria
    ) {
        final CaixaResumoService.CaixaResumo resumo = caixaResumoService.resumoDia(dia);
        final BigDecimal vendas = money(resumo.totalVendas());
        final BigDecimal saldoMeta = money(vendas.subtract(metaDiaria));
        final BigDecimal diferencaAbsoluta = money(saldoMeta.abs());
        final boolean metaBatida = saldoMeta.compareTo(BigDecimal.ZERO) >= 0;
        final String mensagem = metaBatida
                ? diferencaAbsoluta.compareTo(BigDecimal.ZERO) == 0
                    ? "Boa, meta cravada."
                    : "Boa, passou " + formatCurrency(diferencaAbsoluta) + "."
                : "Quase la, faltou " + formatCurrency(diferencaAbsoluta) + ".";
        final String badgeClass = metaBatida
                ? "badge--success"
                : "badge--warning";
        final String badgeLabel = metaBatida
                ? "Meta batida"
                : "Nao bateu";
        return new MetaVendaDiaView(
                dia,
                metaDiaria,
                vendas,
                saldoMeta,
                diferencaAbsoluta,
                metaBatida,
                mensagem,
                badgeClass,
                badgeLabel
        );
    }

    private static BigDecimal parseValor(final String rawValor) {
        if (rawValor == null || rawValor.isBlank()) {
            throw new IllegalArgumentException(
                    "Informe o valor da meta diaria."
            );
        }
        try {
            return new BigDecimal(rawValor.trim().replace(",", "."));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Informe um valor valido para a meta diaria."
            );
        }
    }

    private static BigDecimal money(final BigDecimal value) {
        final BigDecimal safe = value == null ? BigDecimal.ZERO : value;
        return safe.setScale(2, RoundingMode.HALF_UP);
    }

    private static String formatCurrency(final BigDecimal value) {
        return NumberFormat.getCurrencyInstance(LOCALE_PT_BR).format(
                value == null ? BigDecimal.ZERO : value
        );
    }

    public record MetaVendaPainel(
            BigDecimal metaDiaria,
            BigDecimal vendasHoje,
            BigDecimal diferencaHoje,
            boolean metaBatidaHoje,
            String tituloMensagem,
            String mensagem,
            String badgeClass,
            String badgeLabel,
            String alertaResumo,
            List<MetaVendaDiaView> comparativo
    ) {
    }

    public record MetaVendaDiaView(
            LocalDate dia,
            BigDecimal metaDiaria,
            BigDecimal vendas,
            BigDecimal saldoMeta,
            BigDecimal diferencaAbsoluta,
            boolean metaBatida,
            String mensagem,
            String badgeClass,
            String badgeLabel
    ) {
    }
}
