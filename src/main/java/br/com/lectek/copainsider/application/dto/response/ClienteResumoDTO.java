/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonProperty
 *  io.swagger.v3.oas.annotations.media.Schema
 *  io.swagger.v3.oas.annotations.media.Schema$RequiredMode
 */
package br.com.lectek.copainsider.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Schema(name="ClienteResumoDTO", description="Informa\u00e7\u00f5es resumidas do cliente")
public class ClienteResumoDTO
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="ID do cliente (UUID)", example="3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode=Schema.RequiredMode.REQUIRED)
    @JsonProperty(value="clienteId")
    private UUID clienteId;
    @Schema(description="Nome completo do cliente", example="Jo\u00e3o Silva", requiredMode=Schema.RequiredMode.REQUIRED)
    @JsonProperty(value="nome")
    private String nome;

    public ClienteResumoDTO() {
    }

    public ClienteResumoDTO(UUID clienteId, String nome) {
        this.clienteId = clienteId;
        this.nome = nome;
    }

    public UUID getClienteId() {
        return this.clienteId;
    }

    public void setClienteId(UUID clienteId) {
        this.clienteId = clienteId;
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
        if (!(o instanceof ClienteResumoDTO)) {
            return false;
        }
        ClienteResumoDTO that = (ClienteResumoDTO)o;
        return Objects.equals(this.clienteId, that.clienteId) && Objects.equals(this.nome, that.nome);
    }

    public int hashCode() {
        return Objects.hash(this.clienteId, this.nome);
    }

    public String toString() {
        return "ClienteResumoDTO{clienteId=" + String.valueOf(this.clienteId) + ", nome='" + this.nome + "'}";
    }
}

