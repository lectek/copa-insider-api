/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 */
package br.com.redemaisfarma.application.dto.otp;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
public record OtpStartResponse(String deliveryId, String maskedDestino, Integer cooldownSec, Integer ttlSeconds, String demoCode) {
}

