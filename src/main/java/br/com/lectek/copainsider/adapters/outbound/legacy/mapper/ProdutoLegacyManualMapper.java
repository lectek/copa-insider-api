/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.stereotype.Component
 */
package br.com.lectek.copainsider.adapters.outbound.legacy.mapper;

import br.com.lectek.copainsider.adapters.outbound.legacy.entity.ProdutoLegacyEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.MetodoLeituraCodigoBarras;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class ProdutoLegacyManualMapper {
    public ProdutoEntity toEntity(ProdutoLegacyEntity legacy) {
        String nome;
        if (legacy == null) {
            return null;
        }
        ProdutoEntity e = new ProdutoEntity();
        e.setId(null);
        if (legacy.getId() != null) {
            e.setLegacyId(legacy.getId().longValue());
        }
        e.setNome((nome = ProdutoLegacyManualMapper.clampLen(ProdutoLegacyManualMapper.trimToNull(legacy.getNome()), 150)) != null ? nome : "Produto sem nome");
        String desc = ProdutoLegacyManualMapper.clampLen(ProdutoLegacyManualMapper.firstNonBlank(legacy.getApresentacao(), legacy.getNome()), 255);
        e.setDescricao(desc);
        String cb = ProdutoLegacyManualMapper.normalizeEan(legacy.getCodigoBarras());
        e.setCodigoBarras(ProdutoLegacyManualMapper.clampLen(cb, 50));
        e.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.LEGADO);
        BigDecimal pv = ProdutoLegacyManualMapper.coalesce(legacy.getPrecoVenda(), legacy.getPrecoPromocao());
        pv = ProdutoLegacyManualMapper.ensureMin(ProdutoLegacyManualMapper.scale2(pv), new BigDecimal("0.01"));
        e.setPrecoVenda(pv);
        e.setPrecoCusto(ProdutoLegacyManualMapper.scale2(legacy.getPrecoPromocao()));
        e.setCategoria("Sem Categoria");
        int estoque = 0;
        if (legacy.getSaldo() != null) {
            try {
                estoque = legacy.getSaldo().intValue();
            }
            catch (ArithmeticException ignore) {
                estoque = legacy.getSaldo().intValueExact();
            }
        }
        e.setEstoque(Math.max(0, estoque));
        e.setDisponivel(Boolean.TRUE);
        String fab = legacy.getMargemLucro() != null ? "Lucro: " + String.valueOf(legacy.getMargemLucro()) + "%" : "Desconhecido";
        e.setFabricante(ProdutoLegacyManualMapper.clampLen(fab, 100));
        e.setUnidade(null);
        e.setDataCadastro(legacy.getInicioPromocao() != null ? legacy.getInicioPromocao().toLocalDate() : LocalDate.now());
        e.setImagem(null);
        e.setStatusSync("PENDENTE");
        return e;
    }

    private static String firstNonBlank(String ... vals) {
        if (vals == null) {
            return null;
        }
        String[] stringArray = vals;
        int n = vals.length;
        int n2 = 0;
        while (n2 < n) {
            String v = stringArray[n2];
            String t = ProdutoLegacyManualMapper.trimToNull(v);
            if (t != null) {
                return t;
            }
            ++n2;
        }
        return null;
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String clampLen(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }

    private static BigDecimal coalesce(BigDecimal a, BigDecimal b) {
        return a != null ? a : b;
    }

    private static BigDecimal scale2(BigDecimal v) {
        return v == null ? null : v.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal ensureMin(BigDecimal v, BigDecimal min) {
        if (v == null) {
            return min;
        }
        return v.compareTo(min) < 0 ? min : v;
    }

    private static String normalizeEan(String raw) {
        if (raw == null) {
            return null;
        }
        String digits = raw.replaceAll("\\D+", "");
        return digits.isEmpty() ? null : digits;
    }
}
