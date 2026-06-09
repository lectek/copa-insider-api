/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonIgnoreProperties
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.Pattern
 *  jakarta.validation.constraints.Pattern$Flag
 *  jakarta.validation.constraints.Size
 */
package br.com.redemaisfarma.application.dto.otp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown=true)
public record OtpStartRequest(@NotBlank(message="canal \u00e9 obrigat\u00f3rio") @Pattern(regexp="email|sms", flags={Pattern.Flag.CASE_INSENSITIVE}, message="canal deve ser 'email' ou 'sms'") @NotBlank(message="canal \u00e9 obrigat\u00f3rio") @Pattern(regexp="email|sms", flags={Pattern.Flag.CASE_INSENSITIVE}, message="canal deve ser 'email' ou 'sms'") String canal, @NotBlank(message="destino \u00e9 obrigat\u00f3rio") @Size(max=256, message="destino muito longo") @NotBlank(message="destino \u00e9 obrigat\u00f3rio") @Size(max=256, message="destino muito longo") String destino, @Size(max=128, message="previousDeliveryId muito longo") String previousDeliveryId) {
}

