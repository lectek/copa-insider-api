/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  br.com.lectek.copainsider.application.dto.legacy.ProdutoLegacyDTO
 *  br.com.lectek.copainsider.application.dto.legacy.ProdutoNovoDTO
 *  br.com.lectek.copainsider.domain.Produto
 *  org.mapstruct.InjectionStrategy
 *  org.mapstruct.Mapper
 *  org.mapstruct.Mapping
 *  org.mapstruct.Mappings
 *  org.mapstruct.Named
 *  org.mapstruct.ReportingPolicy
 */
package br.com.lectek.copainsider.adapters.outbound.legacy.mapper;

import br.com.lectek.copainsider.application.dto.legacy.ProdutoLegacyDTO;
import br.com.lectek.copainsider.application.dto.legacy.ProdutoNovoDTO;
import br.com.lectek.copainsider.domain.Produto;
import java.math.BigDecimal;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel="spring", injectionStrategy=InjectionStrategy.CONSTRUCTOR, unmappedTargetPolicy=ReportingPolicy.IGNORE)
public interface ProdutoLegacyDTOMapper {
    @Mappings(value={@Mapping(source="id", target="id"), @Mapping(source="nome", target="descricaoProd"), @Mapping(source="codigoBarras", target="codigoBarras"), @Mapping(source="saldo", target="estoqueAtual", qualifiedByName={"bigDecimalToInt"}), @Mapping(source="precoVenda", target="precoVenda"), @Mapping(source="precoPromocao", target="precoCusto"), @Mapping(source="margemLucro", target="margemLucro"), @Mapping(source="inicioPromocao", target="inicioPromocao"), @Mapping(source="terminoPromocao", target="terminoPromocao"), @Mapping(source="apresentacao", target="unidade"), @Mapping(source="precoAnterior", target="precoAnterior"), @Mapping(source="bonus", target="bonus"), @Mapping(target="categoria", ignore=true), @Mapping(target="ativo", ignore=true), @Mapping(target="fornecedor", ignore=true), @Mapping(target="fabricante", ignore=true), @Mapping(target="dataCadastro", ignore=true), @Mapping(target="imagem", ignore=true)})
    public ProdutoNovoDTO toProdutoNovoDTO(ProdutoLegacyDTO var1);

    @Mappings(value={@Mapping(source="id", target="id"), @Mapping(source="descricaoProd", target="nome"), @Mapping(source="codigoBarras", target="codigoBarras"), @Mapping(source="estoqueAtual", target="saldo", qualifiedByName={"intToBigDecimal"}), @Mapping(source="precoVenda", target="precoVenda"), @Mapping(source="precoCusto", target="precoPromocao"), @Mapping(source="margemLucro", target="margemLucro"), @Mapping(source="inicioPromocao", target="inicioPromocao"), @Mapping(source="terminoPromocao", target="terminoPromocao"), @Mapping(source="unidade", target="apresentacao"), @Mapping(source="precoAnterior", target="precoAnterior"), @Mapping(source="bonus", target="bonus"), @Mapping(target="estoqueMinimo", ignore=true), @Mapping(target="fornecedorId", ignore=true), @Mapping(target="categoriaId", ignore=true), @Mapping(target="comissaoId", ignore=true)})
    public ProdutoLegacyDTO toProdutoLegacyDTO(ProdutoNovoDTO var1);

    @Mappings(value={@Mapping(source="id", target="id"), @Mapping(source="nome", target="descricao"), @Mapping(source="codigoBarras", target="codigoBarras"), @Mapping(source="precoVenda", target="precoVenda")})
    public Produto toDomain(ProdutoLegacyDTO var1);

    @Named(value="bigDecimalToInt")
    default public int bigDecimalToInt(BigDecimal value) {
        return value == null ? 0 : value.intValue();
    }

    @Named(value="intToBigDecimal")
    default public BigDecimal intToBigDecimal(Integer value) {
        return value == null ? null : BigDecimal.valueOf(value.longValue());
    }
}

