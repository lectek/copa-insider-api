/*
 * Decompiled with CFR 0.152.
 */
package br.com.lectek.copainsider.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

public class Produto {
    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal precoVenda;
    private String imagem;
    private String categoria;
    private String codigoBarras;
    private BigDecimal precoCusto;
    private Integer estoque;
    private Boolean disponivel;
    private String fabricante;
    private String codigoOriginal;
    private String unidade;
    private LocalDateTime dataCadastro;

    public Produto() {
    }

    public Produto(Long id, String nome, String descricao, BigDecimal precoVenda, String imagem, String categoria, String codigoBarras, BigDecimal precoCusto, Integer estoque, Boolean disponivel, String fabricante, String codigoOriginal, String unidade, LocalDateTime dataCadastro) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.precoVenda = precoVenda;
        this.imagem = imagem;
        this.categoria = categoria;
        this.codigoBarras = codigoBarras;
        this.precoCusto = precoCusto;
        this.estoque = estoque;
        this.disponivel = disponivel;
        this.fabricante = fabricante;
        this.codigoOriginal = codigoOriginal;
        this.unidade = unidade;
        this.dataCadastro = dataCadastro;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return this.nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return this.descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPrecoVenda() {
        return this.precoVenda;
    }

    public void setPrecoVenda(BigDecimal precoVenda) {
        this.precoVenda = precoVenda;
    }

    public String getImagem() {
        return this.imagem;
    }

    public void setImagem(String imagem) {
        this.imagem = imagem;
    }

    public String getCategoria() {
        return this.categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getCodigoBarras() {
        return this.codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public BigDecimal getPrecoCusto() {
        return this.precoCusto;
    }

    public void setPrecoCusto(BigDecimal precoCusto) {
        this.precoCusto = precoCusto;
    }

    public Integer getEstoque() {
        return this.estoque;
    }

    public void setEstoque(Integer estoque) {
        this.estoque = estoque;
    }

    public Boolean getDisponivel() {
        return this.disponivel;
    }

    public void setDisponivel(Boolean disponivel) {
        this.disponivel = disponivel;
    }

    public String getFabricante() {
        return this.fabricante;
    }

    public void setFabricante(String fabricante) {
        this.fabricante = fabricante;
    }

    public String getCodigoOriginal() {
        return this.codigoOriginal;
    }

    public void setCodigoOriginal(String codigoOriginal) {
        this.codigoOriginal = codigoOriginal;
    }

    public String getUnidade() {
        return this.unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public LocalDateTime getDataCadastro() {
        return this.dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public void aplicarDesconto(BigDecimal percentual) {
        if (percentual == null || percentual.compareTo(BigDecimal.ZERO) <= 0 || percentual.compareTo(BigDecimal.ONE) >= 0) {
            throw new IllegalArgumentException("Percentual de desconto inv\u00e1lido: " + String.valueOf(percentual));
        }
        if (this.precoVenda == null) {
            throw new IllegalStateException("Pre\u00e7o de venda n\u00e3o definido para aplicar desconto");
        }
        this.precoVenda = this.precoVenda.multiply(BigDecimal.ONE.subtract(percentual)).setScale(2, RoundingMode.HALF_UP);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Produto)) {
            return false;
        }
        Produto that = (Produto)o;
        return Objects.equals(this.id, that.id);
    }

    public int hashCode() {
        return Objects.hash(this.id);
    }

    public String toString() {
        return "Produto{id=" + this.id + ", nome='" + this.nome + "', descricao='" + this.descricao + "', precoVenda=" + String.valueOf(this.precoVenda) + ", imagem='" + this.imagem + "', categoria='" + this.categoria + "', codigoBarras='" + this.codigoBarras + "', precoCusto=" + String.valueOf(this.precoCusto) + ", estoque=" + this.estoque + ", disponivel=" + this.disponivel + ", fabricante='" + this.fabricante + "', codigoOriginal='" + this.codigoOriginal + "', unidade='" + this.unidade + "', dataCadastro=" + String.valueOf(this.dataCadastro) + "}";
    }
}

