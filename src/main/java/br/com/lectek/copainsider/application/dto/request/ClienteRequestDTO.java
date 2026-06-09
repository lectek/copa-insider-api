/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonFormat
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 *  com.fasterxml.jackson.annotation.JsonProperty
 *  com.fasterxml.jackson.annotation.JsonProperty$Access
 *  io.swagger.v3.oas.annotations.media.Schema
 *  jakarta.validation.constraints.Email
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.NotNull
 *  jakarta.validation.constraints.PastOrPresent
 *  jakarta.validation.constraints.Pattern
 *  jakarta.validation.constraints.Size
 */
package br.com.lectek.copainsider.application.dto.request;

import br.com.lectek.copainsider.domain.enums.GrupoCliente;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
@Schema(name="ClienteRequestDTO", description="Dados para cria\u00e7\u00e3o/atualiza\u00e7\u00e3o de cliente")
public class ClienteRequestDTO
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description="ID do cliente (envie no update; n\u00e3o envie no create)", example="42")
    @JsonProperty(value="id", access=JsonProperty.Access.READ_ONLY)
    private Long id;
    @Schema(description="Nome completo do cliente", example="Jo\u00e3o da Silva", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{cliente.nome.notBlank}")
    @Size(max=100, message="{cliente.nome.size}")
    @JsonProperty(value="nome")
    private @NotBlank(message="{cliente.nome.notBlank}") @Size(max=100, message="{cliente.nome.size}") String nome;
    @Schema(description="E-mail do cliente", example="joao@email.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{cliente.email.notBlank}")
    @Email(message="{cliente.email.email}")
    @Size(max=150, message="{cliente.email.size}")
    @JsonProperty(value="email")
    private @NotBlank(message="{cliente.email.notBlank}") @Email(message="{cliente.email.email}") @Size(max=150, message="{cliente.email.size}") String email;
    @Schema(description="CPF do cliente (com ou sem m\u00e1scara)", example="123.456.789-09", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{cliente.cpf.notBlank}")
    @Pattern(regexp="^(\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2})$", message="{cliente.cpf.pattern}")
    @Size(max=14, message="{cliente.cpf.size}")
    @JsonProperty(value="cpf")
    private @NotBlank(message="{cliente.cpf.notBlank}") @Pattern(regexp="^(\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2})$", message="{cliente.cpf.pattern}") @Size(max=14, message="{cliente.cpf.size}") String cpf;
    @Schema(description="Telefone de contato", example="+55 (83) 99999-9999", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{cliente.telefone.notBlank}")
    @Pattern(regexp="\\+?\\d{1,3}? ?\\(?\\d{2}\\)? ?\\d{4,5}-?\\d{4}", message="{cliente.telefone.pattern}")
    @Size(max=25, message="{cliente.telefone.size}")
    @JsonProperty(value="telefone")
    private @NotBlank(message="{cliente.telefone.notBlank}") @Pattern(regexp="\\+?\\d{1,3}? ?\\(?\\d{2}\\)? ?\\d{4,5}-?\\d{4}", message="{cliente.telefone.pattern}") @Size(max=25, message="{cliente.telefone.size}") String telefone;
    @Schema(description="Endere\u00e7o completo", example="Rua das Flores, 123, Jo\u00e3o Pessoa - PB", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message="{cliente.endereco.notBlank}")
    @Size(max=200, message="{cliente.endereco.size}")
    @JsonProperty(value="endereco")
    private @NotBlank(message="{cliente.endereco.notBlank}") @Size(max=200, message="{cliente.endereco.size}") String endereco;
    @Schema(description="Grupo de cliente (ex: RETAIL, WHOLESALE)", example="RETAIL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message="{cliente.grupo.notNull}")
    @JsonProperty(value="grupoCliente")
    private @NotNull(message="{cliente.grupo.notNull}") GrupoCliente grupoCliente;
    @Schema(description="Observa\u00e7\u00f5es adicionais", example="Prefere atendimento online")
    @Size(max=500, message="{cliente.observacao.size}")
    @JsonProperty(value="observacao")
    private @Size(max=500, message="{cliente.observacao.size}") String observacao;
    @Schema(description="Data e hora do cadastro (servidor)", type="string", format="date-time", example="2025-07-04T18:30:00")
    @PastOrPresent(message="{cliente.dataCadastro.pastOrPresent}")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty(value="dataCadastro", access=JsonProperty.Access.READ_ONLY)
    private @PastOrPresent(message="{cliente.dataCadastro.pastOrPresent}") LocalDateTime dataCadastro;
    @Schema(description="Senha do cliente (somente no create; nunca retorna no response)", example="MinhaSenhaForte123!")
    @JsonProperty(value="senha", access=JsonProperty.Access.WRITE_ONLY)
    @Size(min=8, max=100, message="{cliente.senha.size}")
    private @Size(min=8, max=100, message="{cliente.senha.size}") String senha;

    public ClienteRequestDTO() {
    }

    public ClienteRequestDTO(Long id, String nome, String email, String cpf, String telefone, String endereco, GrupoCliente grupoCliente, String observacao, LocalDateTime dataCadastro) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.telefone = telefone;
        this.endereco = endereco;
        this.grupoCliente = grupoCliente;
        this.observacao = observacao;
        this.dataCadastro = dataCadastro;
    }

    public ClienteRequestDTO(String nome, String email, String cpf, String telefone, String endereco, GrupoCliente grupoCliente, String observacao, String senha) {
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.telefone = telefone;
        this.endereco = endereco;
        this.grupoCliente = grupoCliente;
        this.observacao = observacao;
        this.senha = senha;
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

    public GrupoCliente getGrupoCliente() {
        return this.grupoCliente;
    }

    public void setGrupoCliente(GrupoCliente grupoCliente) {
        this.grupoCliente = grupoCliente;
    }

    public String getObservacao() {
        return this.observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public LocalDateTime getDataCadastro() {
        return this.dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public String getSenha() {
        return this.senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClienteRequestDTO)) {
            return false;
        }
        ClienteRequestDTO that = (ClienteRequestDTO)o;
        return Objects.equals(this.id, that.id) && Objects.equals(this.nome, that.nome) && Objects.equals(this.email, that.email) && Objects.equals(this.cpf, that.cpf) && Objects.equals(this.telefone, that.telefone) && Objects.equals(this.endereco, that.endereco) && this.grupoCliente == that.grupoCliente && Objects.equals(this.observacao, that.observacao) && Objects.equals(this.dataCadastro, that.dataCadastro);
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.id, this.nome, this.email, this.cpf, this.telefone, this.endereco, this.grupoCliente, this.observacao, this.dataCadastro});
    }

    public String toString() {
        return "ClienteRequestDTO{id=" + this.id + ", nome='" + this.nome + "', email='" + this.email + "', cpf='" + this.cpf + "', telefone='" + this.telefone + "', endereco='" + this.endereco + "', grupoCliente=" + String.valueOf((Object)this.grupoCliente) + ", observacao='" + this.observacao + "', dataCadastro=" + String.valueOf(this.dataCadastro) + "}";
    }
}


