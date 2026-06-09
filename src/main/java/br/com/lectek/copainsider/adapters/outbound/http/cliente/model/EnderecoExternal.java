/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonProperty
 */
package br.com.lectek.copainsider.adapters.outbound.http.cliente.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;

public class EnderecoExternal
implements Serializable {
    @JsonProperty(value="cep")
    private String cep;
    @JsonProperty(value="logradouro")
    private String logradouro;
    @JsonProperty(value="numero")
    private String numero;
    @JsonProperty(value="complemento")
    private String complemento;
    @JsonProperty(value="bairro")
    private String bairro;
    @JsonProperty(value="cidade")
    private String cidade;
    @JsonProperty(value="estado")
    private String estado;
    @JsonProperty(value="ibge")
    private String ibge;
    @JsonProperty(value="tipo")
    private String tipo;
    @JsonProperty(value="principal")
    private Boolean principal;

    public String getCep() {
        return this.cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getLogradouro() {
        return this.logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getNumero() {
        return this.numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return this.complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getBairro() {
        return this.bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return this.cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return this.estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getIbge() {
        return this.ibge;
    }

    public void setIbge(String ibge) {
        this.ibge = ibge;
    }

    public String getTipo() {
        return this.tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Boolean getPrincipal() {
        return this.principal;
    }

    public void setPrincipal(Boolean principal) {
        this.principal = principal;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EnderecoExternal)) {
            return false;
        }
        EnderecoExternal that = (EnderecoExternal)o;
        return Objects.equals(this.cep, that.cep) && Objects.equals(this.logradouro, that.logradouro) && Objects.equals(this.numero, that.numero) && Objects.equals(this.cidade, that.cidade) && Objects.equals(this.estado, that.estado);
    }

    public int hashCode() {
        return Objects.hash(this.cep, this.logradouro, this.numero, this.cidade, this.estado);
    }
}

