/*
 * Decompiled with CFR 0.152.
 */
package br.com.redemaisfarma.domain;

import java.time.LocalDate;
import java.util.Objects;

public class Cliente {
    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private String cpf;
    private String senha;
    private LocalDate dataDeNascimento;
    private boolean ativo = true;

    public Cliente() {
    }

    public Cliente(Long id, String nome, String email, String telefone, String cpf, String senha, LocalDate dataDeNascimento, boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.cpf = cpf;
        this.senha = senha;
        this.dataDeNascimento = dataDeNascimento;
        this.ativo = ativo;
    }

    public Long getId() {
        return this.id;
    }

    public String getNome() {
        return this.nome;
    }

    public String getEmail() {
        return this.email;
    }

    public String getTelefone() {
        return this.telefone;
    }

    public String getCpf() {
        return this.cpf;
    }

    public String getSenha() {
        return this.senha;
    }

    public LocalDate getDataDeNascimento() {
        return this.dataDeNascimento;
    }

    public boolean isAtivo() {
        return this.ativo;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public void setDataDeNascimento(LocalDate dataDeNascimento) {
        this.dataDeNascimento = dataDeNascimento;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public void atualizarTelefone(String novoTelefone) {
        if (novoTelefone == null || novoTelefone.length() < 8) {
            throw new IllegalArgumentException("Telefone inv\u00e1lido.");
        }
        this.telefone = novoTelefone;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Cliente)) {
            return false;
        }
        Cliente c = (Cliente)o;
        return Objects.equals(this.id, c.id);
    }

    public int hashCode() {
        return Objects.hash(this.id);
    }
}

