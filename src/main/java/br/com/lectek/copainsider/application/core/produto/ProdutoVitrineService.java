/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.domain.PageRequest
 *  org.springframework.data.domain.Pageable
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 */
package br.com.lectek.copainsider.application.core.produto;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.lectek.copainsider.application.core.tenant.TenantResolverService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProdutoVitrineService {
    private final ProdutoRepository repo;
    @Autowired(required=false)
    private TenantResolverService tenantResolverService;

    public ProdutoVitrineService(ProdutoRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly=true)
    public List<ProdutoEntity> listarDestaques(int limit) {
        PageRequest page = PageRequest.of((int)0, (int)Math.max(1, Math.min(limit, 20)));
        Long tenantId = this.resolveTenantId();
        return tenantId == null
                ? this.repo.findCarrossel((Pageable)page)
                : this.repo.findCarrossel(tenantId, (Pageable)page);
    }

    private Long resolveTenantId() {
        if (this.tenantResolverService == null) {
            return null;
        }
        return this.tenantResolverService.resolveDefaultTenantId();
    }
}
