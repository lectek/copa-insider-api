/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.constraints.NotBlank
 */
package br.com.lectek.copainsider.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordRequest {
    @NotBlank(message="Informe seu e-mail ou CPF.")
    private @NotBlank(message="Informe seu e-mail ou CPF.") String emailOuCpf;

    public String getEmailOuCpf() {
        return this.emailOuCpf;
    }

    public void setEmailOuCpf(String emailOuCpf) {
        this.emailOuCpf = emailOuCpf;
    }
}

