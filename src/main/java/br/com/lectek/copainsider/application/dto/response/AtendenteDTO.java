/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 *  com.fasterxml.jackson.annotation.JsonProperty
 *  io.swagger.v3.oas.annotations.media.Schema
 */
package br.com.lectek.copainsider.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Objects;

@Schema(name="AtendenteDTO", description="Informa\u00e7\u00f5es b\u00e1sicas do atendente")
@JsonInclude(value=JsonInclude.Include.NON_NULL)
public class AtendenteDTO
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="ID do atendente", example="10")
    @JsonProperty(value="id")
    private Long id;
    @Schema(description="Nome do atendente", example="Carlos Souza")
    @JsonProperty(value="nome")
    private String nome;

    public AtendenteDTO() {
    }

    public AtendenteDTO(Long id, String nome) {
        this.id = id;
        this.nome = nome;
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

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AtendenteDTO)) {
            return false;
        }
        AtendenteDTO that = (AtendenteDTO)o;
        return Objects.equals(this.id, that.id) && Objects.equals(this.nome, that.nome);
    }

    public int hashCode() {
        return Objects.hash(this.id, this.nome);
    }

    public String toString() {
        return "AtendenteDTO{id=" + this.id + ", nome='" + this.nome + "'}";
    }
}

