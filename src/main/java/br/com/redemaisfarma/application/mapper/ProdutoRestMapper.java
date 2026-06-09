/*
 * Decompiled with CFR 0.152.
 */
package br.com.redemaisfarma.application.mapper;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.application.dto.response.ProdutoResponseDTO;
import java.util.List;
import java.util.UUID;

public class ProdutoRestMapper {
    public static ProdutoResponseDTO toResponse(ProdutoEntity e) {
        if (e == null) {
            return null;
        }
        UUID publicId = e.getId() != null ? UUID.nameUUIDFromBytes(("produto:" + e.getId()).getBytes()) : UUID.randomUUID();
        Integer estoqueAtual = e.getEstoque();
        boolean ativo = Boolean.TRUE.equals(e.getDisponivel())
                && e.getPrecoVenda() != null
                && e.getPrecoVenda().signum() > 0
                && estoqueAtual != null
                && estoqueAtual > 0;
        ProdutoResponseDTO.SituacaoProduto situacao = ativo ? ProdutoResponseDTO.SituacaoProduto.ATIVO : ProdutoResponseDTO.SituacaoProduto.ESGOTADO;
        ProdutoResponseDTO dto = new ProdutoResponseDTO(publicId, ProdutoRestMapper.nvl(e.getNome(), "Produto"), ProdutoRestMapper.nvl(e.getDescricao(), ""), e.getPrecoVenda(), e.getImagem(), e.getCategoria(), e.getEstoque(), null, e.getCodigoBarras(), e.getFabricante(), null, null, e.getDataCadastro() != null ? e.getDataCadastro().atStartOfDay() : null, e.getUpdatedAt(), Boolean.TRUE.equals(e.getDestaqueCarrossel()), Boolean.FALSE, Boolean.FALSE, null, List.of(), situacao);
        dto.setEntityId(e.getId());
        dto.setImagens(e.getImagensProduto());
        return dto;
    }

    public static List<ProdutoResponseDTO> toResponseList(List<ProdutoEntity> entities) {
        return entities.stream().map(ProdutoRestMapper::toResponse).toList();
    }

    private static String nvl(String v, String def) {
        return v == null || v.isBlank() ? def : v;
    }
}
