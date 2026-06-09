package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoCategoriaRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProductCategoryBindingService {

    private final ProdutoCategoriaRepository categoriaRepository;

    public ProductCategoryBindingService(
            final ProdutoCategoriaRepository categoriaRepository
    ) {
        this.categoriaRepository = categoriaRepository;
    }

    public void bind(final ProdutoEntity entity) {
        if (entity == null) {
            return;
        }
        final String categoria = entity.getCategoria() == null
                ? null
                : entity.getCategoria().trim();
        if (categoria == null || categoria.isBlank()) {
            entity.setCategoria(null);
            entity.setCategoriaRef(null);
            return;
        }
        entity.setCategoria(categoria);
        categoriaRepository.findByNomeIgnoreCase(categoria)
                .ifPresentOrElse(found -> {
                    entity.setCategoriaRef(found);
                    entity.setCategoria(found.getNome());
                }, () -> entity.setCategoriaRef(null));
    }

    public void bindAll(final List<ProdutoEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return;
        }
        for (ProdutoEntity entity : entities) {
            bind(entity);
        }
    }
}
