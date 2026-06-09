/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.Size
 */
package br.com.lectek.copainsider.adapters.inbound.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetSenhaForm {
    @NotBlank
    private String token;
    @NotBlank
    @Size(min=8, max=72)
    private @NotBlank @Size(min=8, max=72) String novaSenha;
    @NotBlank
    @Size(min=8, max=72)
    private @NotBlank @Size(min=8, max=72) String confirmarSenha;

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNovaSenha() {
        return this.novaSenha;
    }

    public void setNovaSenha(String novaSenha) {
        this.novaSenha = novaSenha;
    }

    public String getConfirmarSenha() {
        return this.confirmarSenha;
    }

    public void setConfirmarSenha(String confirmarSenha) {
        this.confirmarSenha = confirmarSenha;
    }
}

