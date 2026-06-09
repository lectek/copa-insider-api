/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.domain.PageRequest
 *  org.springframework.data.domain.Pageable
 */
package br.com.redemaisfarma.domain.catalogo;

import br.com.redemaisfarma.application.view.ProductCardVM;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public interface ProdutoQueryService {
    public List<ProductCardVM> topSellers(Pageable var1, boolean var2);

    public List<ProductCardVM> newArrivals(Pageable var1, boolean var2);

    public List<ProductCardVM> featured(Pageable var1, boolean var2);

    public List<ProductCardVM> recommended(Pageable var1, boolean var2);

    public java.util.Optional<ProductCardVM> findById(Long id, boolean incluirIndisponiveis);

    default public List<ProductCardVM> featured(int limit) {
        int size = Math.max(1, Math.min(limit, 50));
        PageRequest page = PageRequest.of((int)0, (int)size);
        return this.featured((Pageable)page, false);
    }
}
