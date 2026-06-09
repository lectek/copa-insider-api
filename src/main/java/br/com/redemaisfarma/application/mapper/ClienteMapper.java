/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.mapstruct.BeanMapping
 *  org.mapstruct.Mapper
 *  org.mapstruct.MappingTarget
 *  org.mapstruct.NullValuePropertyMappingStrategy
 *  org.mapstruct.ReportingPolicy
 */
package br.com.redemaisfarma.application.mapper;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.domain.Cliente;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel="spring", unmappedTargetPolicy=ReportingPolicy.IGNORE, unmappedSourcePolicy=ReportingPolicy.IGNORE)
public interface ClienteMapper {
    public Cliente toDomain(ClienteEntity var1);

    public ClienteEntity toEntity(Cliente var1);

    @BeanMapping(nullValuePropertyMappingStrategy=NullValuePropertyMappingStrategy.IGNORE)
    public void updateEntity(@MappingTarget ClienteEntity var1, Cliente var2);
}

