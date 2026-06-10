package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.adapters.outbound.hotmart.HotmartApiClient;
import br.com.lectek.copainsider.adapters.outbound.hotmart.HotmartApiClient.HotmartProduct;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.CopaProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.CopaProdutoJPARepository;
import br.com.lectek.copainsider.domain.enums.TipoCopaProduto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class HotmartSyncService {

    private static final Logger log = LoggerFactory.getLogger(HotmartSyncService.class);

    private final HotmartApiClient apiClient;
    private final CopaProdutoJPARepository repository;

    public HotmartSyncService(HotmartApiClient apiClient, CopaProdutoJPARepository repository) {
        this.apiClient = apiClient;
        this.repository = repository;
    }

    @Transactional
    public SyncResult sincronizar() {
        List<HotmartProduct> produtos = apiClient.listarProdutos();
        if (produtos.isEmpty()) return new SyncResult(0, 0);

        int criados = 0;
        int atualizados = 0;

        for (HotmartProduct p : produtos) {
            if (p.id() == null || p.name() == null) continue;

            var existente = repository.findByHotmartProductId(p.id());

            if (existente.isPresent()) {
                CopaProdutoEntity entity = existente.get();
                entity.setHotmartUrl(p.checkoutUrl());
                entity.setNomePtBr(p.name());
                repository.save(entity);
                atualizados++;
                log.info("[hotmart-sync] atualizado: {} (id={})", p.name(), p.id());
            } else {
                CopaProdutoEntity entity = criarDeHotmart(p);
                repository.save(entity);
                criados++;
                log.info("[hotmart-sync] importado: {} (id={})", p.name(), p.id());
            }
        }

        return new SyncResult(criados, atualizados);
    }

    private CopaProdutoEntity criarDeHotmart(HotmartProduct p) {
        CopaProdutoEntity e = new CopaProdutoEntity();
        e.setHotmartProductId(p.id());
        e.setSlug("hotmart-" + p.id());
        e.setTipo(TipoCopaProduto.GUIA_SELECAO);
        e.setPreco(BigDecimal.ZERO);
        e.setNomePtBr(p.name());
        e.setDescPtBr(p.name());
        e.setHotmartUrl(p.checkoutUrl());
        e.setAtivo(false);  // inativo até ser revisado manualmente
        e.setOrdem(999);
        return e;
    }

    public record SyncResult(int criados, int atualizados) {}
}
