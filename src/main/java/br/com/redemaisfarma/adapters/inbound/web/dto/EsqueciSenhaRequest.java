/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.constraints.NotBlank
 */
package br.com.redemaisfarma.adapters.inbound.web.dto;

import jakarta.validation.constraints.NotBlank;

public record EsqueciSenhaRequest(@NotBlank String identificador) {
}

