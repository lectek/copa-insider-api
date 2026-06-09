/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 *  com.fasterxml.jackson.annotation.JsonProperty
 *  io.swagger.v3.oas.annotations.media.Schema
 */
package br.com.redemaisfarma.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@Schema(name="ProdutoDTO", description="Informa\u00e7\u00f5es resumidas do produto")
public class ProdutoDTO
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="ID do produto", example="1")
    @JsonProperty(value="produtoId")
    private Long id;
    @Schema(description="Nome do produto", example="Dipirona 500mg")
    @JsonProperty(value="nome")
    private String nome;
    @Schema(description="Pre\u00e7o do produto", example="12.99")
    @JsonProperty(value="preco")
    private BigDecimal preco;

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

    public BigDecimal getPreco() {
        return this.preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProdutoDTO)) {
            return false;
        }
        ProdutoDTO that = (ProdutoDTO)o;
        return Objects.equals(this.id, that.id) && Objects.equals(this.nome, that.nome) && Objects.equals(this.preco, that.preco);
    }

    public int hashCode() {
        return Objects.hash(this.id, this.nome, this.preco);
    }

    public String toString() {
        return "ProdutoDTO{id=" + this.id + ", nome='" + this.nome + "', preco=" + String.valueOf(this.preco) + "}";
    }
}

