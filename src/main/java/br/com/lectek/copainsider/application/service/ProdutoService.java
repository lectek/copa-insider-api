/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  br.com.lectek.copainsider.application.dto.request.CadastroProdutoRequestDTO
 *  br.com.lectek.copainsider.domain.Produto
 */
package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.lectek.copainsider.application.dto.request.CadastroProdutoRequestDTO;
import br.com.lectek.copainsider.domain.Produto;
import java.util.List;

public interface ProdutoService {
    public Produto findById(Long var1);

    public List<Produto> list();

    public Produto create(Produto var1);

    public Produto update(Long var1, Produto var2);

    public void delete(Long var1);

    public Produto createFromDto(CadastroProdutoRequestDTO var1);

    public Produto validar(Long var1, String var2);

    public Produto publicar(Long var1, String var2);

    public List<Produto> listByStatus(ProdutoStatus var1);

    @Deprecated
    default public Produto buscarPorId(Long id) {
        return this.findById(id);
    }

    @Deprecated
    default public List<Produto> listarTodos() {
        return this.list();
    }

    @Deprecated
    default public Produto salvar(Produto produto) {
        return this.create(produto);
    }

    @Deprecated
    default public Produto atualizar(Long id, Produto produto) {
        return this.update(id, produto);
    }

    @Deprecated
    default public void deletar(Long id) {
        this.delete(id);
    }
}

