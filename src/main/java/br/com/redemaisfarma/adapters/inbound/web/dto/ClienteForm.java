/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.constraints.Email
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.Size
 */
package br.com.redemaisfarma.adapters.inbound.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class ClienteForm {
    @NotBlank(message="Informe seu nome.")
    @Size(max=120, message="Nome muito longo.")
    private @NotBlank(message="Informe seu nome.") @Size(max=120, message="Nome muito longo.") String nome;
    @NotBlank(message="Informe um e-mail.")
    @Email(message="E-mail inv\u00e1lido.")
    @Size(max=180, message="E-mail muito longo.")
    private @NotBlank(message="Informe um e-mail.") @Email(message="E-mail inv\u00e1lido.") @Size(max=180, message="E-mail muito longo.") String email;
    @NotBlank(message="Informe o CPF.")
    @Size(min=11, max=14, message="CPF inv\u00e1lido.")
    private @NotBlank(message="Informe o CPF.") @Size(min=11, max=14, message="CPF inv\u00e1lido.") String cpf;
    @Size(max=20, message="Telefone muito longo.")
    private @Size(max=20, message="Telefone muito longo.") String telefone;
    private LocalDate dataDeNascimento;
    @NotBlank(message="Informe uma senha.")
    @Size(min=8, max=128, message="Senha deve ter entre 8 e 128 caracteres.")
    private @NotBlank(message="Informe uma senha.") @Size(min=8, max=128, message="Senha deve ter entre 8 e 128 caracteres.") String senha;
    @NotBlank(message="Confirme a senha.")
    private @NotBlank(message="Confirme a senha.") String confirmarSenha;

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

    public LocalDate getDataDeNascimento() {
        return this.dataDeNascimento;
    }

    public void setDataDeNascimento(LocalDate dataDeNascimento) {
        this.dataDeNascimento = dataDeNascimento;
    }

    public String getSenha() {
        return this.senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getConfirmarSenha() {
        return this.confirmarSenha;
    }

    public void setConfirmarSenha(String confirmarSenha) {
        this.confirmarSenha = confirmarSenha;
    }
}

