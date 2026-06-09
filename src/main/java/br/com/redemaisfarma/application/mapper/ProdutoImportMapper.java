/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.mapstruct.Mapper
 *  org.mapstruct.Mapping
 *  org.mapstruct.Mappings
 *  org.mapstruct.ReportingPolicy
 */
package br.com.redemaisfarma.application.mapper;

import br.com.redemaisfarma.adapters.outbound.legacy.entity.ProdutoLegacyEntity;
import br.com.redemaisfarma.application.dto.legacy.ProdutoLegacyDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel="spring", unmappedTargetPolicy=ReportingPolicy.IGNORE)
public interface ProdutoImportMapper {
    @Mappings(value={@Mapping(source="id", target="id"), @Mapping(source="nome", target="nome"), @Mapping(source="codigoBarras", target="codigoBarras"), @Mapping(source="saldo", target="saldo"), @Mapping(source="precoVenda", target="precoVenda"), @Mapping(source="precoPromocao", target="precoPromocao"), @Mapping(source="estoqueMinimo", target="estoqueMinimo"), @Mapping(source="margemLucro", target="margemLucro"), @Mapping(source="inicioPromocao", target="inicioPromocao"), @Mapping(source="terminoPromocao", target="terminoPromocao"), @Mapping(source="bonus", target="bonus"), @Mapping(source="apresentacao", target="apresentacao"), @Mapping(source="precoAnterior", target="precoAnterior"), @Mapping(source="fornecedorId", target="fornecedorId"), @Mapping(source="categoriaId", target="categoriaId"), @Mapping(source="comissaoId", target="comissaoId")})
    public ProdutoLegacyEntity toEntity(ProdutoLegacyDTO var1);
}

