/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  br.com.redemaisfarma.application.port.outbound.ProdutoRepositoryPort
 *  br.com.redemaisfarma.application.port.outbound.ProdutoRepositoryPort$ProdutoDTO
 *  org.springframework.data.domain.Page
 *  org.springframework.data.domain.PageRequest
 *  org.springframework.data.domain.Pageable
 *  org.springframework.stereotype.Repository
 *  org.springframework.transaction.annotation.Transactional
 */
package br.com.redemaisfarma.adapters.outbound.persistence.adapter;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.application.port.outbound.ProdutoRepositoryPort;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ProdutoRepositoryAdapter
implements ProdutoRepositoryPort {
    private final ProdutoRepository jpa;

    public ProdutoRepositoryAdapter(ProdutoRepository jpa) {
        this.jpa = jpa;
    }

    public Optional<ProdutoRepositoryPort.ProdutoDTO> findById(Long id) {
        return this.jpa.findById(id).map(this::mapToDto);
    }

    @Transactional
    public void updateImagem(Long id, String imageUrl) {
        ProdutoEntity e = (ProdutoEntity)this.jpa.findById(id).orElseThrow();
        e.addImagemProduto(imageUrl);
        e.definirImagemPrincipal(imageUrl);
        this.jpa.save(e);
    }

    private ProdutoRepositoryPort.ProdutoDTO mapToDto(ProdutoEntity e) {
        return new ProdutoRepositoryPort.ProdutoDTO(
                e.getId(),
                e.getNome(),
                e.getDescricao(),
                e.getCategoria(),
                e.getCodigoBarras(),
                e.getFabricante(),
                e.getImagem()
        );
    }

    public List<ProdutoEntity> firstNProdutosSemMidia(int limit) {
        Page<ProdutoEntity> page = this.jpa.findSemMidia((Pageable)PageRequest.of((int)0, (int)Math.max(1, limit)));
        return page.getContent();
    }
}
