// application/service/sync/ProdutoSyncService.java
package br.com.redemaisfarma.application.service.sync;

import br.com.redemaisfarma.adapters.outbound.legacy.entity.ProdutoLegacyEntity;
import br.com.redemaisfarma.adapters.outbound.legacy.mapper.ProdutoLegacyManualMapper;
import br.com.redemaisfarma.adapters.outbound.legacy.repository.ProdutoLegacyRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
// Se você usa Spring Data, mantenha o que EXISTE no projeto:
// import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ProdutoJpaRepository;
import br.com.redemaisfarma.application.core.tenant.TenantResolverService;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "legacy.sync.enabled", havingValue = "true", matchIfMissing = false)
public class ProdutoSyncServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(ProdutoSyncServiceImpl.class);

    // Se sua page-size vem do properties, você pode manter @Value; se não, deixe default seguro
    private int pageSize = 500;

    private final ProdutoLegacyRepository legacyRepository;
    // Troque aqui para o que você tem de fato:
    // private final ProdutoRepository novoRepository;
    private final ProdutoJpaRepository novoRepository;
    @Autowired(required = false)
    private TenantResolverService tenantResolverService;

    private final ProdutoLegacyManualMapper mapper;

    public int sincronizar() { return this.sincronizarProdutos(); }

    public int sincronizarProdutos() {
        long lidos = 0, alterados = 0, ignorados = 0, erros = 0;

        StopWatch swAll = new StopWatch("sync-produtos");
        swAll.start();

        int page = 0;
        while (true) {
            Page<ProdutoLegacyEntity> lote = legacyRepository.findAll(PageRequest.of(page, Math.max(pageSize, 1)));
            if (lote.isEmpty()) break;

            List<ProdutoLegacyEntity> items = lote.getContent();
            lidos += items.size();

            try {
                LoteResultado r = processarLote(items);
                alterados += r.alterados();
                ignorados += r.ignorados();
            } catch (Exception e) {
                erros += items.size();
                log.error("Erro ao processar lote page={} size={}: {}", page, items.size(), e.getMessage(), e);
            }

            if (!lote.hasNext()) break;
            page++;
        }

        swAll.stop();
        log.info("Sincronização concluída em {} ms. Lidos={}, Alterados={}, Ignorados={}, Erros={}",
                swAll.getTotalTimeMillis(), lidos, alterados, ignorados, erros);

        return Math.toIntExact(alterados);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected LoteResultado processarLote(List<ProdutoLegacyEntity> items) {
        long alterados = 0, ignorados = 0;
        final Long tenantId = resolveTenantId();
        List<ProdutoEntity> toSalvar = new ArrayList<>(items.size());

        for (ProdutoLegacyEntity legacy : items) {
            try {
                ProdutoEntity mapeado = mapper.toEntity(legacy);
                if (mapeado == null) { ignorados++; continue; }

                ProdutoEntity destino = localizarExistente(tenantId, legacy, mapeado);
                boolean houveMudanca = mesclarDados(destino, mapeado);
                if (destino.getTenantId() == null) {
                    destino.setTenantId(tenantId);
                }
                aplicarDefaultsENormalizacoes(destino);
                aplicarHashAuditoriaSeDisponivel(destino, hashDoLegacy(legacy));

                if (houveMudanca || isNovo(destino)) toSalvar.add(destino);
                else ignorados++;
            } catch (Exception e) {
                log.warn("Falha ao sincronizar produto legado id={}: {}", legacy != null ? legacy.getId() : null, e.getMessage(), e);
            }
        }

        if (!toSalvar.isEmpty()) {
            novoRepository.saveAll(toSalvar);
            alterados += toSalvar.size();
        }

        return new LoteResultado(alterados, ignorados);
    }

    private boolean isNovo(ProdutoEntity e) {
        try { Method getId = e.getClass().getMethod("getId"); return getId.invoke(e) == null; }
        catch (Exception ignore) { return false; }
    }

    private ProdutoEntity localizarExistente(Long tenantId, ProdutoLegacyEntity legacy, ProdutoEntity mapeado) {
        Optional<ProdutoEntity> byLegacy;
        Optional<ProdutoEntity> byCb;

        if (legacy != null && legacy.getId() != null &&
            (byLegacy = tenantId == null
                    ? novoRepository.findByLegacyId(legacy.getId().longValue())
                    : novoRepository.findByTenantIdAndLegacyId(tenantId, legacy.getId().longValue())).isPresent()) {
            return byLegacy.get();
        }
        if (mapeado != null && !isVazio(mapeado.getCodigoBarras()) &&
            (byCb = tenantId == null
                    ? novoRepository.findByAnyCodigo(mapeado.getCodigoBarras())
                    : novoRepository.findByAnyCodigo(tenantId, mapeado.getCodigoBarras())).isPresent()) {
            return byCb.get();
        }
        ProdutoEntity novo = new ProdutoEntity();
        novo.setTenantId(tenantId);
        if (legacy != null && legacy.getId() != null) novo.setLegacyId(legacy.getId().longValue());
        return novo;
    }

    private boolean mesclarDados(ProdutoEntity destino, ProdutoEntity src) {
        boolean changed = false;
        changed |= setIfDiff(destino::getNome, destino::setNome, src.getNome());
        changed |= setIfDiff(destino::getDescricao, destino::setDescricao, src.getDescricao());
        changed |= setIfDiff(destino::getImagem, destino::setImagem, src.getImagem());
        changed |= setIfDiff(destino::getCategoria, destino::setCategoria, src.getCategoria());
        changed |= setIfDiff(destino::getCodigoBarras, destino::setCodigoBarras, src.getCodigoBarras());
        changed |= setIfDiff(destino::getFabricante, destino::setFabricante, src.getFabricante());
        changed |= setIfDiff(destino::getUnidade, destino::setUnidade, src.getUnidade());
        changed |= setIfDiffBigDecimal(destino::getPrecoVenda, destino::setPrecoVenda, src.getPrecoVenda());
        changed |= setIfDiffBigDecimal(destino::getPrecoCusto, destino::setPrecoCusto, src.getPrecoCusto());
        changed |= setIfDiffObj(destino::getDisponivel, destino::setDisponivel, src.getDisponivel());
        changed |= setIfDiffObj(destino::getCodigoOriginal, destino::setCodigoOriginal, src.getCodigoOriginal());
        changed |= setIfDiffObj(destino::getDataCadastro, destino::setDataCadastro, src.getDataCadastro());
        changed |= setIfDiffObj(destino::getLegacyId, destino::setLegacyId, src.getLegacyId());
        try {
            Integer estSrc = src.getEstoque();
            if (estSrc != null) {
                int novoValor = Math.max(0, estSrc);
                Integer atual = destino.getEstoque();
                if (!Objects.equals(atual, novoValor)) { destino.setEstoque(novoValor); changed = true; }
            }
        } catch (Exception ignore) {}
        return changed;
    }

    private void aplicarDefaultsENormalizacoes(ProdutoEntity e) {
        if (e.getImagem() != null && e.getImagem().isBlank()) e.setImagem(null);
        if (e.getDisponivel() == null) e.setDisponivel(Boolean.TRUE);
        try { if (e.getEstoque() != null && e.getEstoque() < 0) e.setEstoque(0); } catch (Exception ignore) {}
        if (e.getPrecoVenda() == null || menorOuIgualZero(e.getPrecoVenda())) e.setPrecoVenda(new BigDecimal("0.01"));
    }

    private void aplicarHashAuditoriaSeDisponivel(ProdutoEntity destino, String hash) {
        if (hash == null) return;
        try { destino.getClass().getMethod("setHashOrigem", String.class).invoke(destino, hash); return; }
        catch (NoSuchMethodException ignored) {}
        catch (Exception e) { log.debug("Falha ao setar hash via setter: {}", e.getMessage()); }
        try { Field f = destino.getClass().getDeclaredField("hashOrigem"); f.setAccessible(true); f.set(destino, hash); }
        catch (NoSuchFieldException ignored) {}
        catch (Exception e) { log.debug("Falha ao setar hash via field: {}", e.getMessage()); }
    }

    private String hashDoLegacy(ProdutoLegacyEntity l) {
        try {
            if (l == null) return null;
            String base = String.join("|",
                String.valueOf(l.getId()), safe(l.getNome()), safe(l.getCodigoBarras()),
                safe(l.getPrecoVenda()), safe(l.getPrecoPromocao()), safe(l.getSaldo()),
                safe(l.getMargemLucro()), safe(l.getInicioPromocao()), safe(l.getTerminoPromocao())
            );
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(base.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { log.debug("Não foi possível calcular hash do legado: {}", e.getMessage()); return null; }
    }

    // helpers
    private boolean setIfDiff(SupplierEx<String> g, ConsumerEx<String> s, String nv) {
        String atual = nullSafeTrim(g.get()); String v = nullSafeTrim(nv);
        if (isVazio(v)) return false; if (!Objects.equals(atual, v)) { s.accept(v); return true; } return false;
    }
    private boolean setIfDiffBigDecimal(SupplierEx<BigDecimal> g, ConsumerEx<BigDecimal> s, BigDecimal nv) {
        BigDecimal at = g.get(); if (nv == null) return false; boolean diff = (at == null) || (nv.compareTo(at) != 0);
        if (diff) { s.accept(nv); return true; } return false;
    }
    private <T> boolean setIfDiffObj(SupplierEx<T> g, ConsumerEx<T> s, T nv) {
        if (nv == null) return false; T at = g.get(); if (!Objects.equals(at, nv)) { s.accept(nv); return true; } return false;
    }
    private boolean menorOuIgualZero(BigDecimal v) { return v != null && v.compareTo(BigDecimal.ZERO) <= 0; }
    private boolean isVazio(String s) { return s == null || s.isBlank(); }
    private String nullSafeTrim(String s) { return s == null ? null : s.trim(); }
    private String safe(Object o) { return o == null ? "" : o.toString().trim(); }

    @FunctionalInterface private interface SupplierEx<T> { T get(); }
    @FunctionalInterface private interface ConsumerEx<T> { void accept(T t); }
    private record LoteResultado(long alterados, long ignorados) {}

    private Long resolveTenantId() {
        if (this.tenantResolverService == null) {
            return null;
        }
        return this.tenantResolverService.resolveDefaultTenantId();
    }
}
