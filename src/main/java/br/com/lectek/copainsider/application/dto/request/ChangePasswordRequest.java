/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.Pattern
 *  jakarta.validation.constraints.Size
 */
package br.com.lectek.copainsider.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequest {
    @NotBlank(message="Informe a senha atual.")
    private @NotBlank(message="Informe a senha atual.") String senhaAtual;
    @NotBlank(message="Informe a nova senha.")
    @Size(min=8, max=128, message="A nova senha deve ter entre 8 e 128 caracteres.")
    @Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,128}$", message="A nova senha deve ter min\u00fascula, MAI\u00daSCULA, n\u00famero e caractere especial.")
    private @NotBlank(message="Informe a nova senha.") @Size(min=8, max=128, message="A nova senha deve ter entre 8 e 128 caracteres.") @Pattern(regexp="^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).{8,128}$", message="A nova senha deve ter min\u00fascula, MAI\u00daSCULA, n\u00famero e caractere especial.") String novaSenha;
    @NotBlank(message="Confirme a nova senha.")
    private @NotBlank(message="Confirme a nova senha.") String confirmarNovaSenha;

    public String getSenhaAtual() {
        return this.senhaAtual;
    }

    public void setSenhaAtual(String senhaAtual) {
        this.senhaAtual = senhaAtual;
    }

    public String getNovaSenha() {
        return this.novaSenha;
    }

    public void setNovaSenha(String novaSenha) {
        this.novaSenha = novaSenha;
    }

    public String getConfirmarNovaSenha() {
        return this.confirmarNovaSenha;
    }

    public void setConfirmarNovaSenha(String confirmarNovaSenha) {
        this.confirmarNovaSenha = confirmarNovaSenha;
    }
}

