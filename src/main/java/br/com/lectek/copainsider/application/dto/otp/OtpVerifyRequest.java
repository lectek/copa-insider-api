/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonAlias
 *  com.fasterxml.jackson.annotation.JsonIgnoreProperties
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.Pattern
 *  jakarta.validation.constraints.Size
 */
package br.com.lectek.copainsider.application.dto.otp;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown=true)
public record OtpVerifyRequest(@NotBlank(message="deliveryId \u00e9 obrigat\u00f3rio") @Size(max=128, message="deliveryId muito longo") @JsonAlias(value={"delivery_id", "deliveryId"}) @NotBlank(message="deliveryId \u00e9 obrigat\u00f3rio") @Size(max=128, message="deliveryId muito longo") String deliveryId, @NotBlank(message="code \u00e9 obrigat\u00f3rio") @Pattern(regexp="\\d{6}", message="code deve ter 6 d\u00edgitos") @NotBlank(message="code \u00e9 obrigat\u00f3rio") @Pattern(regexp="\\d{6}", message="code deve ter 6 d\u00edgitos") String code) {
}

