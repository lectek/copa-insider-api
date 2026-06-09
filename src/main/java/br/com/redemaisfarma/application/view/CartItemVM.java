package br.com.redemaisfarma.application.view;

import java.math.BigDecimal;

public record CartItemVM(
        Long produtoId,
        String nome,
        String imagem,
        BigDecimal precoUnitario,
        Integer quantidade,
        BigDecimal subtotal,
        boolean invalid,
        String issue,
        Integer estoque
) {
}
