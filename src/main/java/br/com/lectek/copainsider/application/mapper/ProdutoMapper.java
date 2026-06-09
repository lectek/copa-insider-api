/*
 * Decompiled with CFR 0.152.
 */
package br.com.lectek.copainsider.application.mapper;

import br.com.lectek.copainsider.adapters.outbound.legacy.entity.ProdutoLegacyEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.MetodoLeituraCodigoBarras;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.domain.Produto;
import br.com.lectek.copainsider.domain.support.BarcodeNormalizer;
import br.com.lectek.copainsider.domain.support.ProdutoHashUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;

public class ProdutoMapper {
    public static ProdutoEntity fromLegacy(ProdutoLegacyEntity legacy) {
        if (legacy == null) {
            return null;
        }
        ProdutoEntity e = new ProdutoEntity();
        e.setLegacyId(legacy.getId() != null ? Long.valueOf(legacy.getId().longValue()) : null);
        e.setNome(Optional.ofNullable(ProdutoMapper.trim(legacy.getNome())).orElse("Produto"));
        e.setDescricao(ProdutoMapper.trim(legacy.getApresentacao()));
        e.setCodigoBarras(ProdutoMapper.normalizeBarcode(legacy.getCodigoBarras()));
        BigDecimal precoOriginal = ProdutoMapper.toBig(legacy.getPrecoVenda());
        int estoque = ProdutoMapper.toInt(legacy.getSaldo());
        BigDecimal precoPersistir = precoOriginal != null && precoOriginal.compareTo(BigDecimal.ZERO) > 0 ? precoOriginal.setScale(2, RoundingMode.HALF_UP) : new BigDecimal("0.01");
        e.setPrecoVenda(precoPersistir);
        e.setEstoque(Math.max(0, estoque));
        boolean disponivel = precoOriginal != null && precoOriginal.compareTo(BigDecimal.ZERO) > 0 && estoque > 0;
        e.setDisponivel(disponivel);
        e.setMetodoLeituraCodigoBarras(MetodoLeituraCodigoBarras.LEGADO);
        if (e.getDataCadastro() == null) {
            e.setDataCadastro(LocalDate.now());
        }
        String hash = ProdutoHashUtil.buildHash(e.getCodigoBarras() == null ? "" : e.getCodigoBarras(), e.getNome() == null ? "" : e.getNome(), e.getDescricao() == null ? "" : e.getDescricao(), e.getPrecoVenda(), e.getLegacyId());
        e.setHashLegado(hash);
        return e;
    }

    public static Produto toDomain(ProdutoEntity entity) {
        if (entity == null) {
            return null;
        }
        Produto p = new Produto();
        p.setId(entity.getId());
        p.setNome(entity.getNome());
        p.setDescricao(entity.getDescricao());
        p.setPrecoVenda(entity.getPrecoVenda());
        p.setImagem(entity.getImagem());
        p.setCategoria(entity.getCategoria());
        p.setCodigoBarras(entity.getCodigoBarras());
        p.setPrecoCusto(entity.getPrecoCusto());
        p.setEstoque(entity.getEstoque());
        p.setDisponivel(entity.getDisponivel());
        p.setFabricante(entity.getFabricante());
        p.setUnidade(entity.getUnidade());
        p.setDataCadastro(entity.getDataCadastro() == null ? null : entity.getDataCadastro().atStartOfDay());
        return p;
    }

    public static ProdutoEntity toEntity(Produto domain) {
        if (domain == null) {
            return null;
        }
        ProdutoEntity e = new ProdutoEntity();
        e.setId(domain.getId());
        e.setNome(ProdutoMapper.trim(domain.getNome()));
        e.setDescricao(ProdutoMapper.trim(domain.getDescricao()));
        e.setPrecoVenda(ProdutoMapper.safeMoney(domain.getPrecoVenda()));
        e.setImagem(ProdutoMapper.trim(domain.getImagem()));
        e.setCategoria(ProdutoMapper.trim(domain.getCategoria()));
        e.setCodigoBarras(ProdutoMapper.normalizeBarcode(domain.getCodigoBarras()));
        e.setPrecoCusto(ProdutoMapper.safeMoney(domain.getPrecoCusto()));
        e.setEstoque(domain.getEstoque() == null ? 0 : Math.max(0, domain.getEstoque()));
        e.setDisponivel(Optional.ofNullable(domain.getDisponivel()).orElse(Boolean.TRUE));
        e.setFabricante(ProdutoMapper.trim(domain.getFabricante()));
        e.setUnidade(ProdutoMapper.trim(domain.getUnidade()));
        if (e.getDataCadastro() == null) {
            e.setDataCadastro(LocalDate.now());
        }
        String hash = ProdutoHashUtil.buildHash(e.getCodigoBarras() == null ? "" : e.getCodigoBarras(), e.getNome() == null ? "" : e.getNome(), e.getDescricao() == null ? "" : e.getDescricao(), e.getPrecoVenda(), e.getLegacyId());
        e.setHashLegado(hash);
        return e;
    }

    public static void updateEntity(ProdutoEntity target, Produto src) {
        if (target == null || src == null) {
            return;
        }
        if (src.getNome() != null) {
            target.setNome(ProdutoMapper.trim(src.getNome()));
        }
        if (src.getDescricao() != null) {
            target.setDescricao(ProdutoMapper.trim(src.getDescricao()));
        }
        if (src.getPrecoVenda() != null) {
            target.setPrecoVenda(ProdutoMapper.safeMoney(src.getPrecoVenda()));
        }
        if (src.getImagem() != null) {
            target.setImagem(ProdutoMapper.trim(src.getImagem()));
        }
        if (src.getCategoria() != null) {
            target.setCategoria(ProdutoMapper.trim(src.getCategoria()));
        }
        if (src.getCodigoBarras() != null) {
            target.setCodigoBarras(ProdutoMapper.normalizeBarcode(src.getCodigoBarras()));
        }
        if (src.getPrecoCusto() != null) {
            target.setPrecoCusto(ProdutoMapper.safeMoney(src.getPrecoCusto()));
        }
        if (src.getEstoque() != null) {
            target.setEstoque(Math.max(0, src.getEstoque()));
        }
        if (src.getDisponivel() != null) {
            target.setDisponivel(src.getDisponivel());
        }
        if (src.getFabricante() != null) {
            target.setFabricante(ProdutoMapper.trim(src.getFabricante()));
        }
        if (src.getUnidade() != null) {
            target.setUnidade(ProdutoMapper.trim(src.getUnidade()));
        }
        String hash = ProdutoHashUtil.buildHash(target.getCodigoBarras() == null ? "" : target.getCodigoBarras(), target.getNome() == null ? "" : target.getNome(), target.getDescricao() == null ? "" : target.getDescricao(), target.getPrecoVenda(), target.getLegacyId());
        target.setHashLegado(hash);
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }

    private static String normalizeBarcode(String s) {
        return BarcodeNormalizer.normalizeOrNull(s);
    }

    private static BigDecimal toBig(Number n) {
        if (n == null) {
            return null;
        }
        return new BigDecimal(n.toString()).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal safeMoney(BigDecimal v) {
        if (v == null) {
            return null;
        }
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    private static int toInt(Number n) {
        if (n == null) {
            return 0;
        }
        int x = (int)Math.floor(new BigDecimal(n.toString()).doubleValue());
        return Math.max(0, x);
    }
}
