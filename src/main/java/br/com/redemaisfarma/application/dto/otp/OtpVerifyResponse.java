/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 *  jakarta.validation.constraints.NotBlank
 */
package br.com.redemaisfarma.application.dto.otp;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
public record OtpVerifyResponse(@NotBlank(message="verificationToken \u00e9 obrigat\u00f3rio") String verificationToken) {
}

