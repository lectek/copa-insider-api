/*
 * Decompiled with CFR 0.152.
 */
package br.com.lectek.copainsider.application.session;

import java.io.Serializable;
import java.time.LocalDateTime;

public class SessaoCliente
implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String nome;
    private String email;
    private boolean clienteVip;
    private LocalDateTime dataUltimoAcesso;

    public SessaoCliente() {
    }

    public SessaoCliente(Long id, String nome, String email, boolean clienteVip, LocalDateTime dataUltimoAcesso) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.clienteVip = clienteVip;
        this.dataUltimoAcesso = dataUltimoAcesso;
    }

    public SessaoCliente(Long id, String nome, String email, LocalDateTime dataUltimoAcesso) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.dataUltimoAcesso = dataUltimoAcesso;
        this.clienteVip = false;
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

    public boolean isClienteVip() {
        return this.clienteVip;
    }

    public void setClienteVip(boolean clienteVip) {
        this.clienteVip = clienteVip;
    }

    public LocalDateTime getDataUltimoAcesso() {
        return this.dataUltimoAcesso;
    }

    public void setDataUltimoAcesso(LocalDateTime dataUltimoAcesso) {
        this.dataUltimoAcesso = dataUltimoAcesso;
    }

    public boolean isClienteLogado() {
        return this.id != null;
    }
}

