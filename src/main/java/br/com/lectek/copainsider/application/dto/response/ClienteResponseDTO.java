/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonFormat
 *  com.fasterxml.jackson.annotation.JsonProperty
 *  io.swagger.v3.oas.annotations.media.Schema
 *  io.swagger.v3.oas.annotations.media.Schema$RequiredMode
 */
package br.com.lectek.copainsider.application.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Schema(name="ClienteResponseDTO", description="Representa os dados detalhados do cliente na API.")
public class ClienteResponseDTO
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="ID \u00fanico do cliente (UUID).", example="3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode=Schema.RequiredMode.REQUIRED)
    @JsonProperty(value="clienteId")
    private UUID clienteId;
    @Schema(description="Nome completo do cliente.", example="Jo\u00e3o da Silva", requiredMode=Schema.RequiredMode.REQUIRED)
    @JsonProperty(value="nome")
    private String nome;
    @Schema(description="CPF do cliente (formato v\u00e1lido).", example="123.456.789-09", requiredMode=Schema.RequiredMode.REQUIRED)
    @JsonProperty(value="cpf")
    private String cpf;
    @Schema(description="Telefone principal de contato.", example="+55 (83) 99999-9999")
    @JsonProperty(value="telefone")
    private String telefone;
    @Schema(description="Endere\u00e7o completo.", example="Rua das Flores, 123, Jo\u00e3o Pessoa - PB")
    @JsonProperty(value="endereco")
    private String endereco;
    @Schema(description="Segmento do cliente.", example="RETAIL")
    @JsonProperty(value="grupoCliente")
    private String grupoCliente;
    @Schema(description="Data e hora do cadastro.", type="string", format="date-time", example="2025-07-04T14:30:00")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss", timezone="America/Sao_Paulo")
    @JsonProperty(value="dataCadastro")
    private LocalDateTime dataCadastro;
    @Schema(description="Data e hora da \u00faltima atualiza\u00e7\u00e3o.", type="string", format="date-time", example="2025-07-10T09:15:00")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss", timezone="America/Sao_Paulo")
    @JsonProperty(value="dataAtualizacao")
    private LocalDateTime dataAtualizacao;
    @Schema(description="Identificador do tenant (multi-inquilino).", example="copainsider-001")
    @JsonProperty(value="tenantId")
    private String tenantId;
    @Schema(description="Token de rastreamento para auditoria (UUID).", example="4fa85f64-5717-4562-b3fc-2c963f66afa6")
    @JsonProperty(value="traceId")
    private UUID traceId;

    public ClienteResponseDTO() {
    }

    public ClienteResponseDTO(UUID clienteId, String nome, String cpf, String telefone, String endereco, String grupoCliente, LocalDateTime dataCadastro, LocalDateTime dataAtualizacao, String tenantId, UUID traceId) {
        this.clienteId = clienteId;
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
        this.endereco = endereco;
        this.grupoCliente = grupoCliente;
        this.dataCadastro = dataCadastro;
        this.dataAtualizacao = dataAtualizacao;
        this.tenantId = tenantId;
        this.traceId = traceId;
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

    public String getCpf() {
        return this.cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTelefone() {
        return this.telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEndereco() {
        return this.endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getGrupoCliente() {
        return this.grupoCliente;
    }

    public void setGrupoCliente(String grupoCliente) {
        this.grupoCliente = grupoCliente;
    }

    public LocalDateTime getDataCadastro() {
        return this.dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public LocalDateTime getDataAtualizacao() {
        return this.dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    public String getTenantId() {
        return this.tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getTraceId() {
        return this.traceId;
    }

    public void setTraceId(UUID traceId) {
        this.traceId = traceId;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClienteResponseDTO)) {
            return false;
        }
        ClienteResponseDTO that = (ClienteResponseDTO)o;
        return Objects.equals(this.clienteId, that.clienteId) && Objects.equals(this.nome, that.nome) && Objects.equals(this.cpf, that.cpf) && Objects.equals(this.telefone, that.telefone) && Objects.equals(this.endereco, that.endereco) && Objects.equals(this.grupoCliente, that.grupoCliente) && Objects.equals(this.dataCadastro, that.dataCadastro) && Objects.equals(this.dataAtualizacao, that.dataAtualizacao) && Objects.equals(this.tenantId, that.tenantId) && Objects.equals(this.traceId, that.traceId);
    }

    public int hashCode() {
        return Objects.hash(this.clienteId, this.nome, this.cpf, this.telefone, this.endereco, this.grupoCliente, this.dataCadastro, this.dataAtualizacao, this.tenantId, this.traceId);
    }

    public String toString() {
        return "ClienteResponseDTO{clienteId=" + String.valueOf(this.clienteId) + ", nome='" + this.nome + "', cpf='" + this.cpf + "', telefone='" + this.telefone + "', endereco='" + this.endereco + "', grupoCliente='" + this.grupoCliente + "', dataCadastro=" + String.valueOf(this.dataCadastro) + ", dataAtualizacao=" + String.valueOf(this.dataAtualizacao) + ", tenantId='" + this.tenantId + "', traceId=" + String.valueOf(this.traceId) + "}";
    }
}

