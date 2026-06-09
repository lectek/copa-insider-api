/*
 * Decompiled with CFR 0.152.
 */
package br.com.redemaisfarma.application.port.outbound;

import java.util.Optional;

public interface ProdutoRepositoryPort {
    public Optional<ProdutoDTO> findById(Long var1);

    public void updateImagem(Long var1, String var2);

    public record ProdutoDTO(
            Long id,
            String nome,
            String descricao,
            String categoria,
            String codigoBarras,
            String fabricante,
            String imagem
    ) {
    }
}
