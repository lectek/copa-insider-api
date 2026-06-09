/*
 * Decompiled with CFR 0.152.
 */
package br.com.lectek.copainsider.adapters.inbound.web.dto;

import java.time.Instant;

public record EmailDeliveryResponse(Long id, String purpose, String destination, String provider, String status, Integer attempts, String messageId, String lastError, Instant createdAt, Instant updatedAt) {
}

