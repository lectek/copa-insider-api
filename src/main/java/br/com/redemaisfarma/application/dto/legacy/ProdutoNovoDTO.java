/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 */
package br.com.redemaisfarma.application.dto.legacy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Generated;

public class ProdutoNovoDTO {
    private Long id;
    private String descricaoProd;
    private String codigoBarras;
    private String unidade;
    private String categoria;
    private String nome;
    private BigDecimal precoCusto;
    private BigDecimal precoVenda;
    private BigDecimal precoPromocao;
    private BigDecimal bonus;
    private int estoqueAtual;
    private BigDecimal margemLucro;
    private Float precoAnterior;
    private Boolean ativo;
    private String fornecedor;
    private String fabricante;
    private LocalDate dataCadastro;
    private String imagem;
    private LocalDateTime inicioPromocao;
    private LocalDateTime terminoPromocao;

    @Generated
    public static ProdutoNovoDTOBuilder builder() {
        return new ProdutoNovoDTOBuilder();
    }

    @Generated
    public Long getId() {
        return this.id;
    }

    @Generated
    public String getDescricaoProd() {
        return this.descricaoProd;
    }

    @Generated
    public String getCodigoBarras() {
        return this.codigoBarras;
    }

    @Generated
    public String getUnidade() {
        return this.unidade;
    }

    @Generated
    public String getCategoria() {
        return this.categoria;
    }

    @Generated
    public String getNome() {
        return this.nome;
    }

    @Generated
    public BigDecimal getPrecoCusto() {
        return this.precoCusto;
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
    public BigDecimal getBonus() {
        return this.bonus;
    }

    @Generated
    public int getEstoqueAtual() {
        return this.estoqueAtual;
    }

    @Generated
    public BigDecimal getMargemLucro() {
        return this.margemLucro;
    }

    @Generated
    public Float getPrecoAnterior() {
        return this.precoAnterior;
    }

    @Generated
    public Boolean getAtivo() {
        return this.ativo;
    }

    @Generated
    public String getFornecedor() {
        return this.fornecedor;
    }

    @Generated
    public String getFabricante() {
        return this.fabricante;
    }

    @Generated
    public LocalDate getDataCadastro() {
        return this.dataCadastro;
    }

    @Generated
    public String getImagem() {
        return this.imagem;
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
    public void setId(Long id) {
        this.id = id;
    }

    @Generated
    public void setDescricaoProd(String descricaoProd) {
        this.descricaoProd = descricaoProd;
    }

    @Generated
    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    @Generated
    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    @Generated
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    @Generated
    public void setNome(String nome) {
        this.nome = nome;
    }

    @Generated
    public void setPrecoCusto(BigDecimal precoCusto) {
        this.precoCusto = precoCusto;
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
    public void setBonus(BigDecimal bonus) {
        this.bonus = bonus;
    }

    @Generated
    public void setEstoqueAtual(int estoqueAtual) {
        this.estoqueAtual = estoqueAtual;
    }

    @Generated
    public void setMargemLucro(BigDecimal margemLucro) {
        this.margemLucro = margemLucro;
    }

    @Generated
    public void setPrecoAnterior(Float precoAnterior) {
        this.precoAnterior = precoAnterior;
    }

    @Generated
    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    @Generated
    public void setFornecedor(String fornecedor) {
        this.fornecedor = fornecedor;
    }

    @Generated
    public void setFabricante(String fabricante) {
        this.fabricante = fabricante;
    }

    @Generated
    public void setDataCadastro(LocalDate dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    @Generated
    public void setImagem(String imagem) {
        this.imagem = imagem;
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
    public ProdutoNovoDTO() {
    }

    @Generated
    public ProdutoNovoDTO(Long id, String descricaoProd, String codigoBarras, String unidade, String categoria, String nome, BigDecimal precoCusto, BigDecimal precoVenda, BigDecimal precoPromocao, BigDecimal bonus, int estoqueAtual, BigDecimal margemLucro, Float precoAnterior, Boolean ativo, String fornecedor, String fabricante, LocalDate dataCadastro, String imagem, LocalDateTime inicioPromocao, LocalDateTime terminoPromocao) {
        this.id = id;
        this.descricaoProd = descricaoProd;
        this.codigoBarras = codigoBarras;
        this.unidade = unidade;
        this.categoria = categoria;
        this.nome = nome;
        this.precoCusto = precoCusto;
        this.precoVenda = precoVenda;
        this.precoPromocao = precoPromocao;
        this.bonus = bonus;
        this.estoqueAtual = estoqueAtual;
        this.margemLucro = margemLucro;
        this.precoAnterior = precoAnterior;
        this.ativo = ativo;
        this.fornecedor = fornecedor;
        this.fabricante = fabricante;
        this.dataCadastro = dataCadastro;
        this.imagem = imagem;
        this.inicioPromocao = inicioPromocao;
        this.terminoPromocao = terminoPromocao;
    }

    @Generated
    public static class ProdutoNovoDTOBuilder {
        @Generated
        private Long id;
        @Generated
        private String descricaoProd;
        @Generated
        private String codigoBarras;
        @Generated
        private String unidade;
        @Generated
        private String categoria;
        @Generated
        private String nome;
        @Generated
        private BigDecimal precoCusto;
        @Generated
        private BigDecimal precoVenda;
        @Generated
        private BigDecimal precoPromocao;
        @Generated
        private BigDecimal bonus;
        @Generated
        private int estoqueAtual;
        @Generated
        private BigDecimal margemLucro;
        @Generated
        private Float precoAnterior;
        @Generated
        private Boolean ativo;
        @Generated
        private String fornecedor;
        @Generated
        private String fabricante;
        @Generated
        private LocalDate dataCadastro;
        @Generated
        private String imagem;
        @Generated
        private LocalDateTime inicioPromocao;
        @Generated
        private LocalDateTime terminoPromocao;

        @Generated
        ProdutoNovoDTOBuilder() {
        }

        @Generated
        public ProdutoNovoDTOBuilder id(Long id) {
            this.id = id;
            return this;
        }

        @Generated
        public ProdutoNovoDTOBuilder descricaoProd(String descricaoProd) {
            this.descricaoProd = descricaoProd;
            return this;
        }

        @Generated
        public ProdutoNovoDTOBuilder codigoBarras(String codigoBarras) {
            this.codigoBarras = codigoBarras;
            return this;
        }

        @Generated
        public ProdutoNovoDTOBuilder unidade(String unidade) {
            this.unidade = unidade;
            return this;
        }

        @Generated
        public ProdutoNovoDTOBuilder categoria(String categoria) {
            this.categoria = categoria;
            return this;
        }

        @Generated
        public ProdutoNovoDTOBuilder nome(String nome) {
            this.nome = nome;
            return this;
        }

        @Generated
        public ProdutoNovoDTOBuilder precoCusto(BigDecimal precoCusto) {
            this.precoCusto = precoCusto;
            return this;
        }

        @Generated
        public ProdutoNovoDTOBuilder precoVenda(BigDecimal precoVenda) {
            this.precoVenda = precoVenda;
            return this;
        }

        @Generated
        public ProdutoNovoDTOBuilder precoPromocao(BigDecimal precoPromocao) {
            this.precoPromocao = precoPromocao;
            return this;
        }

        @Generated
        public ProdutoNovoDTOBuilder bonus(BigDecimal bonus) {
            this.bonus = bonus;
            return this;
        }

        @Generated
        public ProdutoNovoDTOBuilder estoqueAtual(int estoqueAtual) {
            this.estoqueAtual = estoqueAtual;
            return this;
        }

        @Generated
        public ProdutoNovoDTOBuilder margemLucro(BigDecimal margemLucro) {
            this.margemLucro = margemLucro;
            return this;
        }

        @Generated
        public ProdutoNovoDTOBuilder precoAnterior(Float precoAnterior) {
            this.precoAnterior = precoAnterior;
            return this;
        }

        @Generated
        public ProdutoNovoDTOBuilder ativo(Boolean ativo) {
            this.ativo = ativo;
            return this;
        }

        @Generated
        public ProdutoNovoDTOBuilder fornecedor(String fornecedor) {
            this.fornecedor = fornecedor;
            return this;
        }

        @Generated
        public ProdutoNovoDTOBuilder fabricante(String fabricante) {
            this.fabricante = fabricante;
            return this;
        }

        @Generated
        public ProdutoNovoDTOBuilder dataCadastro(LocalDate dataCadastro) {
            this.dataCadastro = dataCadastro;
            return this;
        }

        @Generated
        public ProdutoNovoDTOBuilder imagem(String imagem) {
            this.imagem = imagem;
            return this;
        }

        @Generated
        public ProdutoNovoDTOBuilder inicioPromocao(LocalDateTime inicioPromocao) {
            this.inicioPromocao = inicioPromocao;
            return this;
        }

        @Generated
        public ProdutoNovoDTOBuilder terminoPromocao(LocalDateTime terminoPromocao) {
            this.terminoPromocao = terminoPromocao;
            return this;
        }

        @Generated
        public ProdutoNovoDTO build() {
            return new ProdutoNovoDTO(this.id, this.descricaoProd, this.codigoBarras, this.unidade, this.categoria, this.nome, this.precoCusto, this.precoVenda, this.precoPromocao, this.bonus, this.estoqueAtual, this.margemLucro, this.precoAnterior, this.ativo, this.fornecedor, this.fabricante, this.dataCadastro, this.imagem, this.inicioPromocao, this.terminoPromocao);
        }

        @Generated
        public String toString() {
            return "ProdutoNovoDTO.ProdutoNovoDTOBuilder(id=" + this.id + ", descricaoProd=" + this.descricaoProd + ", codigoBarras=" + this.codigoBarras + ", unidade=" + this.unidade + ", categoria=" + this.categoria + ", nome=" + this.nome + ", precoCusto=" + String.valueOf(this.precoCusto) + ", precoVenda=" + String.valueOf(this.precoVenda) + ", precoPromocao=" + String.valueOf(this.precoPromocao) + ", bonus=" + String.valueOf(this.bonus) + ", estoqueAtual=" + this.estoqueAtual + ", margemLucro=" + String.valueOf(this.margemLucro) + ", precoAnterior=" + this.precoAnterior + ", ativo=" + this.ativo + ", fornecedor=" + this.fornecedor + ", fabricante=" + this.fabricante + ", dataCadastro=" + String.valueOf(this.dataCadastro) + ", imagem=" + this.imagem + ", inicioPromocao=" + String.valueOf(this.inicioPromocao) + ", terminoPromocao=" + String.valueOf(this.terminoPromocao) + ")";
        }
    }
}

