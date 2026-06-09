/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.persistence.Column
 *  jakarta.persistence.Entity
 *  jakarta.persistence.Id
 *  jakarta.persistence.Table
 *  lombok.Generated
 */
package br.com.lectek.copainsider.adapters.outbound.legacy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Generated;

@Entity
@Table(name="PRODUTOS")
public class ProdutoLegacyEntity {
    @Id
    @Column(name="PRODUTO_ID", nullable=false)
    private Integer id;
    @Column(name="PRODUTO", length=50)
    private String nome;
    @Column(name="COD_BARRAS", length=14)
    private String codigoBarras;
    @Column(name="PROD_SALDO")
    private BigDecimal saldo;
    @Column(name="PROD_PRVENDA")
    private BigDecimal precoVenda;
    @Column(name="PROD_PRPROMOCAO")
    private BigDecimal precoPromocao;
    @Column(name="PROD_ESTMINIMO")
    private BigDecimal estoqueMinimo;
    @Column(name="MARGEM_LUCRO")
    private BigDecimal margemLucro;
    @Column(name="INICIO_PROMOCAO")
    private LocalDateTime inicioPromocao;
    @Column(name="TERMINO_PROMOCAO")
    private LocalDateTime terminoPromocao;
    @Column(name="BONUS")
    private BigDecimal bonus;
    @Column(name="APRESENTACAO", length=50)
    private String apresentacao;
    @Column(name="PRECO_ANTERIOR")
    private BigDecimal precoAnterior;
    @Column(name="FORNECEDOR_ID")
    private Integer fornecedorId;
    @Column(name="CATEGORIA_ID")
    private Integer categoriaId;
    @Column(name="PADRAO_COMISSAO_ID")
    private Integer comissaoId;

    @Generated
    public static ProdutoLegacyEntityBuilder builder() {
        return new ProdutoLegacyEntityBuilder();
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
    public void setId(Integer id) {
        this.id = id;
    }

    @Generated
    public void setNome(String nome) {
        this.nome = nome;
    }

    @Generated
    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    @Generated
    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }

    @Generated
    public void setPrecoVenda(BigDecimal precoVenda) {
        this.precoVenda = precoVenda;
    }

    @Generated
    public void setPrecoPromocao(BigDecimal precoPromocao) {
        this.precoPromocao = precoPromocao;
    }

    @Generated
    public void setEstoqueMinimo(BigDecimal estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }

    @Generated
    public void setMargemLucro(BigDecimal margemLucro) {
        this.margemLucro = margemLucro;
    }

    @Generated
    public void setInicioPromocao(LocalDateTime inicioPromocao) {
        this.inicioPromocao = inicioPromocao;
    }

    @Generated
    public void setTerminoPromocao(LocalDateTime terminoPromocao) {
        this.terminoPromocao = terminoPromocao;
    }

    @Generated
    public void setBonus(BigDecimal bonus) {
        this.bonus = bonus;
    }

    @Generated
    public void setApresentacao(String apresentacao) {
        this.apresentacao = apresentacao;
    }

    @Generated
    public void setPrecoAnterior(BigDecimal precoAnterior) {
        this.precoAnterior = precoAnterior;
    }

    @Generated
    public void setFornecedorId(Integer fornecedorId) {
        this.fornecedorId = fornecedorId;
    }

    @Generated
    public void setCategoriaId(Integer categoriaId) {
        this.categoriaId = categoriaId;
    }

    @Generated
    public void setComissaoId(Integer comissaoId) {
        this.comissaoId = comissaoId;
    }

    @Generated
    public ProdutoLegacyEntity() {
    }

    @Generated
    public ProdutoLegacyEntity(Integer id, String nome, String codigoBarras, BigDecimal saldo, BigDecimal precoVenda, BigDecimal precoPromocao, BigDecimal estoqueMinimo, BigDecimal margemLucro, LocalDateTime inicioPromocao, LocalDateTime terminoPromocao, BigDecimal bonus, String apresentacao, BigDecimal precoAnterior, Integer fornecedorId, Integer categoriaId, Integer comissaoId) {
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
    public static class ProdutoLegacyEntityBuilder {
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
        ProdutoLegacyEntityBuilder() {
        }

        @Generated
        public ProdutoLegacyEntityBuilder id(Integer id) {
            this.id = id;
            return this;
        }

        @Generated
        public ProdutoLegacyEntityBuilder nome(String nome) {
            this.nome = nome;
            return this;
        }

        @Generated
        public ProdutoLegacyEntityBuilder codigoBarras(String codigoBarras) {
            this.codigoBarras = codigoBarras;
            return this;
        }

        @Generated
        public ProdutoLegacyEntityBuilder saldo(BigDecimal saldo) {
            this.saldo = saldo;
            return this;
        }

        @Generated
        public ProdutoLegacyEntityBuilder precoVenda(BigDecimal precoVenda) {
            this.precoVenda = precoVenda;
            return this;
        }

        @Generated
        public ProdutoLegacyEntityBuilder precoPromocao(BigDecimal precoPromocao) {
            this.precoPromocao = precoPromocao;
            return this;
        }

        @Generated
        public ProdutoLegacyEntityBuilder estoqueMinimo(BigDecimal estoqueMinimo) {
            this.estoqueMinimo = estoqueMinimo;
            return this;
        }

        @Generated
        public ProdutoLegacyEntityBuilder margemLucro(BigDecimal margemLucro) {
            this.margemLucro = margemLucro;
            return this;
        }

        @Generated
        public ProdutoLegacyEntityBuilder inicioPromocao(LocalDateTime inicioPromocao) {
            this.inicioPromocao = inicioPromocao;
            return this;
        }

        @Generated
        public ProdutoLegacyEntityBuilder terminoPromocao(LocalDateTime terminoPromocao) {
            this.terminoPromocao = terminoPromocao;
            return this;
        }

        @Generated
        public ProdutoLegacyEntityBuilder bonus(BigDecimal bonus) {
            this.bonus = bonus;
            return this;
        }

        @Generated
        public ProdutoLegacyEntityBuilder apresentacao(String apresentacao) {
            this.apresentacao = apresentacao;
            return this;
        }

        @Generated
        public ProdutoLegacyEntityBuilder precoAnterior(BigDecimal precoAnterior) {
            this.precoAnterior = precoAnterior;
            return this;
        }

        @Generated
        public ProdutoLegacyEntityBuilder fornecedorId(Integer fornecedorId) {
            this.fornecedorId = fornecedorId;
            return this;
        }

        @Generated
        public ProdutoLegacyEntityBuilder categoriaId(Integer categoriaId) {
            this.categoriaId = categoriaId;
            return this;
        }

        @Generated
        public ProdutoLegacyEntityBuilder comissaoId(Integer comissaoId) {
            this.comissaoId = comissaoId;
            return this;
        }

        @Generated
        public ProdutoLegacyEntity build() {
            return new ProdutoLegacyEntity(this.id, this.nome, this.codigoBarras, this.saldo, this.precoVenda, this.precoPromocao, this.estoqueMinimo, this.margemLucro, this.inicioPromocao, this.terminoPromocao, this.bonus, this.apresentacao, this.precoAnterior, this.fornecedorId, this.categoriaId, this.comissaoId);
        }

        @Generated
        public String toString() {
            return "ProdutoLegacyEntity.ProdutoLegacyEntityBuilder(id=" + String.valueOf(this.id) + ", nome=" + this.nome + ", codigoBarras=" + this.codigoBarras + ", saldo=" + String.valueOf(this.saldo) + ", precoVenda=" + String.valueOf(this.precoVenda) + ", precoPromocao=" + String.valueOf(this.precoPromocao) + ", estoqueMinimo=" + String.valueOf(this.estoqueMinimo) + ", margemLucro=" + String.valueOf(this.margemLucro) + ", inicioPromocao=" + String.valueOf(this.inicioPromocao) + ", terminoPromocao=" + String.valueOf(this.terminoPromocao) + ", bonus=" + String.valueOf(this.bonus) + ", apresentacao=" + this.apresentacao + ", precoAnterior=" + String.valueOf(this.precoAnterior) + ", fornecedorId=" + String.valueOf(this.fornecedorId) + ", categoriaId=" + String.valueOf(this.categoriaId) + ", comissaoId=" + String.valueOf(this.comissaoId) + ")";
        }
    }
}

