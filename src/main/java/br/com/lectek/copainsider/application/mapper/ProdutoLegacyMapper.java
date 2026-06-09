/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.mapstruct.Mapper
 *  org.mapstruct.ReportingPolicy
 */
package br.com.lectek.copainsider.application.mapper;

import br.com.lectek.copainsider.adapters.outbound.legacy.entity.ProdutoLegacyEntity;
import br.com.lectek.copainsider.application.dto.legacy.ProdutoLegacyDTO;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel="spring", unmappedTargetPolicy=ReportingPolicy.IGNORE)
public interface ProdutoLegacyMapper {
    public ProdutoLegacyDTO toDto(ProdutoLegacyEntity var1);

    public ProdutoLegacyEntity toEntity(ProdutoLegacyDTO var1);

    public List<ProdutoLegacyDTO> toDtoList(List<ProdutoLegacyEntity> var1);

    public List<ProdutoLegacyEntity> toEntityList(List<ProdutoLegacyDTO> var1);
}

