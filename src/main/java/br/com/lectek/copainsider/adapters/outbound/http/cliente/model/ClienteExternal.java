/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonFormat
 *  com.fasterxml.jackson.annotation.JsonProperty
 */
package br.com.lectek.copainsider.adapters.outbound.http.cliente.model;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ClienteExternal
implements Serializable {
    @JsonProperty(value="id")
    private UUID id;
    @JsonProperty(value="nome")
    private String nome;
    @JsonProperty(value="cpf")
    private String cpf;
    @JsonProperty(value="email")
    private String email;
    @JsonProperty(value="telefone")
    private String telefone;
    @JsonProperty(value="dataNascimento")
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate dataNascimento;
    @JsonProperty(value="ativo")
    private Boolean ativo;
    @JsonProperty(value="enderecos")
    private List<EnderecoExternal> enderecos;

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNome() {
        return this.nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return this.cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return this.telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public LocalDate getDataNascimento() {
        return this.dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public Boolean getAtivo() {
        return this.ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public List<EnderecoExternal> getEnderecos() {
        return this.enderecos;
    }

    public void setEnderecos(List<EnderecoExternal> enderecos) {
        this.enderecos = enderecos;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClienteExternal)) {
            return false;
        }
        ClienteExternal that = (ClienteExternal)o;
        return Objects.equals(this.id, that.id) && Objects.equals(this.cpf, that.cpf);
    }

    public int hashCode() {
        return Objects.hash(this.id, this.cpf);
    }
}

