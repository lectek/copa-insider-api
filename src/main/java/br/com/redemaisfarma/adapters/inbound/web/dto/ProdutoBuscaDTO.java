package br.com.redemaisfarma.adapters.inbound.web.dto;

import java.math.BigDecimal;

public record ProdutoBuscaDTO(
        Long id,
        String nome,
        String codigoBarras,
        BigDecimal preco,
        Integer estoque,
        String imagem
) {
}
