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
import java.util.Objects;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@Schema(name="ClienteDTO", description="Dados b\u00e1sicos do cliente")
public class ClienteDTO
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="ID do cliente", example="42")
    @JsonProperty(value="id")
    private Long id;
    @Schema(description="Nome do cliente", example="Jo\u00e3o Silva")
    @JsonProperty(value="nome")
    private String nome;
    @Schema(description="E-mail do cliente", example="joao@exemplo.com")
    @JsonProperty(value="email")
    private String email;
    @Schema(description="CPF do cliente", example="12345678901")
    @JsonProperty(value="cpf")
    private String cpf;

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

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpf() {
        return this.cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClienteDTO)) {
            return false;
        }
        ClienteDTO that = (ClienteDTO)o;
        return Objects.equals(this.id, that.id);
    }

    public int hashCode() {
        return Objects.hash(this.id);
    }
}

