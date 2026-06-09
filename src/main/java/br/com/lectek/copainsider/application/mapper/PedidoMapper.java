/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.mapstruct.BeanMapping
 *  org.mapstruct.Mapper
 *  org.mapstruct.Mapping
 *  org.mapstruct.MappingTarget
 *  org.mapstruct.Mappings
 *  org.mapstruct.NullValuePropertyMappingStrategy
 *  org.mapstruct.ReportingPolicy
 */
package br.com.lectek.copainsider.application.mapper;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.lectek.copainsider.domain.Pedido;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel="spring", unmappedTargetPolicy=ReportingPolicy.IGNORE, unmappedSourcePolicy=ReportingPolicy.IGNORE)
public interface PedidoMapper {
    @Mappings(value={@Mapping(target="cliente", ignore=true), @Mapping(target="itens", ignore=true), @Mapping(target="createdAt", ignore=true), @Mapping(target="updatedAt", ignore=true), @Mapping(target="version", ignore=true)})
    public PedidoEntity toEntity(Pedido var1);

    public Pedido toDomain(PedidoEntity var1);

    @BeanMapping(nullValuePropertyMappingStrategy=NullValuePropertyMappingStrategy.IGNORE)
    @Mappings(value={@Mapping(target="id", ignore=true), @Mapping(target="cliente", ignore=true), @Mapping(target="itens", ignore=true), @Mapping(target="createdAt", ignore=true), @Mapping(target="updatedAt", ignore=true), @Mapping(target="version", ignore=true)})
    public void updateEntity(@MappingTarget PedidoEntity var1, Pedido var2);
}

