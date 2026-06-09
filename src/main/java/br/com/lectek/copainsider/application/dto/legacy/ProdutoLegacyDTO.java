/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package br.com.lectek.copainsider.application.dto.legacy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Generated;

public class ProdutoLegacyDTO {
    private final Integer id;
    private final String nome;
    private final String codigoBarras;
    private final BigDecimal saldo;
    private final BigDecimal precoVenda;
    private final BigDecimal precoPromocao;
    private final BigDecimal estoqueMinimo;
    private final BigDecimal margemLucro;
    private final LocalDateTime inicioPromocao;
    private final LocalDateTime terminoPromocao;
    private final BigDecimal bonus;
    private final String apresentacao;
    private final BigDecimal precoAnterior;
    private final Integer fornecedorId;
    private final Integer categoriaId;
    private final Integer comissaoId;

    @Generated
    public static ProdutoLegacyDTOBuilder builder() {
        return new ProdutoLegacyDTOBuilder();
    }

    @Generated
    public ProdutoLegacyDTOBuilder toBuilder() {
        return new ProdutoLegacyDTOBuilder().id(this.id).nome(this.nome).codigoBarras(this.codigoBarras).saldo(this.saldo).precoVenda(this.precoVenda).precoPromocao(this.precoPromocao).estoqueMinimo(this.estoqueMinimo).margemLucro(this.margemLucro).inicioPromocao(this.inicioPromocao).terminoPromocao(this.terminoPromocao).bonus(this.bonus).apresentacao(this.apresentacao).precoAnterior(this.precoAnterior).fornecedorId(this.fornecedorId).categoriaId(this.categoriaId).comissaoId(this.comissaoId);
    }

    @Generated
    public Integer getId() {
        return this.id;
    }

    @Generated
    public String getNome() {
        return this.nome;
    }

    @Generated
    public String getCodigoBarras() {
        return this.codigoBarras;
    }

    @Generated
    public BigDecimal getSaldo() {
        return this.saldo;
    }

    @Generated
    public BigDecimal getPrecoVenda() {
        return this.precoVenda;
    }

    @Generated
    public BigDecimal getPrecoPromocao() {
        return this.precoPromocao;
    }

    @Generated
    public BigDecimal getEstoqueMinimo() {
        return this.estoqueMinimo;
    }

    @Generated
    public BigDecimal getMargemLucro() {
        return this.margemLucro;
    }

    @Generated
    public LocalDateTime getInicioPromocao() {
        return this.inicioPromocao;
    }

    @Generated
    public LocalDateTime getTerminoPromocao() {
        return this.terminoPromocao;
    }

    @Generated
    public BigDecimal getBonus() {
        return this.bonus;
    }

    @Generated
    public String getApresentacao() {
        return this.apresentacao;
    }

    @Generated
    public BigDecimal getPrecoAnterior() {
        return this.precoAnterior;
    }

    @Generated
    public Integer getFornecedorId() {
        return this.fornecedorId;
    }

    @Generated
    public Integer getCategoriaId() {
        return this.categoriaId;
    }

    @Generated
    public Integer getComissaoId() {
        return this.comissaoId;
    }

    @Generated
    public ProdutoLegacyDTO() {
        this.id = null;
        this.nome = null;
        this.codigoBarras = null;
        this.saldo = null;
        this.precoVenda = null;
        this.precoPromocao = null;
        this.estoqueMinimo = null;
        this.margemLucro = null;
        this.inicioPromocao = null;
        this.terminoPromocao = null;
        this.bonus = null;
        this.apresentacao = null;
        this.precoAnterior = null;
        this.fornecedorId = null;
        this.categoriaId = null;
        this.comissaoId = null;
    }

    @Generated
    public ProdutoLegacyDTO(Integer id, String nome, String codigoBarras, BigDecimal saldo, BigDecimal precoVenda, BigDecimal precoPromocao, BigDecimal estoqueMinimo, BigDecimal margemLucro, LocalDateTime inicioPromocao, LocalDateTime terminoPromocao, BigDecimal bonus, String apresentacao, BigDecimal precoAnterior, Integer fornecedorId, Integer categoriaId, Integer comissaoId) {
        this.id = id;
        this.nome = nome;
        this.codigoBarras = codigoBarras;
        this.saldo = saldo;
        this.precoVenda = precoVenda;
        this.precoPromocao = precoPromocao;
        this.estoqueMinimo = estoqueMinimo;
        this.margemLucro = margemLucro;
        this.inicioPromocao = inicioPromocao;
        this.terminoPromocao = terminoPromocao;
        this.bonus = bonus;
        this.apresentacao = apresentacao;
        this.precoAnterior = precoAnterior;
        this.fornecedorId = fornecedorId;
        this.categoriaId = categoriaId;
        this.comissaoId = comissaoId;
    }

    @Generated
    public static class ProdutoLegacyDTOBuilder {
        @Generated
        private Integer id;
        @Generated
        private String nome;
        @Generated
        private String codigoBarras;
        @Generated
        private BigDecimal saldo;
        @Generated
        private BigDecimal precoVenda;
        @Generated
        private BigDecimal precoPromocao;
        @Generated
        private BigDecimal estoqueMinimo;
        @Generated
        private BigDecimal margemLucro;
        @Generated
        private LocalDateTime inicioPromocao;
        @Generated
        private LocalDateTime terminoPromocao;
        @Generated
        private BigDecimal bonus;
        @Generated
        private String apresentacao;
        @Generated
        private BigDecimal precoAnterior;
        @Generated
        private Integer fornecedorId;
        @Generated
        private Integer categoriaId;
        @Generated
        private Integer comissaoId;

        @Generated
        ProdutoLegacyDTOBuilder() {
        }

        @Generated
        public ProdutoLegacyDTOBuilder id(Integer id) {
            this.id = id;
            return this;
        }

        @Generated
        public ProdutoLegacyDTOBuilder nome(String nome) {
            this.nome = nome;
            return this;
        }

        @Generated
        public ProdutoLegacyDTOBuilder codigoBarras(String codigoBarras) {
            this.codigoBarras = codigoBarras;
            return this;
        }

        @Generated
        public ProdutoLegacyDTOBuilder saldo(BigDecimal saldo) {
            this.saldo = saldo;
            return this;
        }

        @Generated
        public ProdutoLegacyDTOBuilder precoVenda(BigDecimal precoVenda) {
            this.precoVenda = precoVenda;
            return this;
        }

        @Generated
        public ProdutoLegacyDTOBuilder precoPromocao(BigDecimal precoPromocao) {
            this.precoPromocao = precoPromocao;
            return this;
        }

        @Generated
        public ProdutoLegacyDTOBuilder estoqueMinimo(BigDecimal estoqueMinimo) {
            this.estoqueMinimo = estoqueMinimo;
            return this;
        }

        @Generated
        public ProdutoLegacyDTOBuilder margemLucro(BigDecimal margemLucro) {
            this.margemLucro = margemLucro;
            return this;
        }

        @Generated
        public ProdutoLegacyDTOBuilder inicioPromocao(LocalDateTime inicioPromocao) {
            this.inicioPromocao = inicioPromocao;
            return this;
        }

        @Generated
        public ProdutoLegacyDTOBuilder terminoPromocao(LocalDateTime terminoPromocao) {
            this.terminoPromocao = terminoPromocao;
            return this;
        }

        @Generated
        public ProdutoLegacyDTOBuilder bonus(BigDecimal bonus) {
            this.bonus = bonus;
            return this;
        }

        @Generated
        public ProdutoLegacyDTOBuilder apresentacao(String apresentacao) {
            this.apresentacao = apresentacao;
            return this;
        }

        @Generated
        public ProdutoLegacyDTOBuilder precoAnterior(BigDecimal precoAnterior) {
            this.precoAnterior = precoAnterior;
            return this;
        }

        @Generated
        public ProdutoLegacyDTOBuilder fornecedorId(Integer fornecedorId) {
            this.fornecedorId = fornecedorId;
            return this;
        }

        @Generated
        public ProdutoLegacyDTOBuilder categoriaId(Integer categoriaId) {
            this.categoriaId = categoriaId;
            return this;
        }

        @Generated
        public ProdutoLegacyDTOBuilder comissaoId(Integer comissaoId) {
            this.comissaoId = comissaoId;
            return this;
        }

        @Generated
        public ProdutoLegacyDTO build() {
            return new ProdutoLegacyDTO(this.id, this.nome, this.codigoBarras, this.saldo, this.precoVenda, this.precoPromocao, this.estoqueMinimo, this.margemLucro, this.inicioPromocao, this.terminoPromocao, this.bonus, this.apresentacao, this.precoAnterior, this.fornecedorId, this.categoriaId, this.comissaoId);
        }

        @Generated
        public String toString() {
            return "ProdutoLegacyDTO.ProdutoLegacyDTOBuilder(id=" + this.id + ", nome=" + this.nome + ", codigoBarras=" + this.codigoBarras + ", saldo=" + String.valueOf(this.saldo) + ", precoVenda=" + String.valueOf(this.precoVenda) + ", precoPromocao=" + String.valueOf(this.precoPromocao) + ", estoqueMinimo=" + String.valueOf(this.estoqueMinimo) + ", margemLucro=" + String.valueOf(this.margemLucro) + ", inicioPromocao=" + String.valueOf(this.inicioPromocao) + ", terminoPromocao=" + String.valueOf(this.terminoPromocao) + ", bonus=" + String.valueOf(this.bonus) + ", apresentacao=" + this.apresentacao + ", precoAnterior=" + String.valueOf(this.precoAnterior) + ", fornecedorId=" + this.fornecedorId + ", categoriaId=" + this.categoriaId + ", comissaoId=" + this.comissaoId + ")";
        }
    }
}

