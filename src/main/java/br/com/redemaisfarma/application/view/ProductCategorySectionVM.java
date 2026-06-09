package br.com.redemaisfarma.application.view;

import java.util.List;

public record ProductCategorySectionVM(
        String chave,
        String titulo,
        List<String> categorias,
        int limite,
        List<ProductCardVM> produtos
) {

    public ProductCategorySectionVM {
        categorias = categorias == null ? List.of() : List.copyOf(categorias);
        produtos = produtos == null ? List.of() : List.copyOf(produtos);
    }
}
