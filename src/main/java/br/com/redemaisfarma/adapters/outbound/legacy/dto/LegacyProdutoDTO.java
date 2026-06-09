/*
 * Decompiled with CFR 0.152.
 */
package br.com.redemaisfarma.adapters.outbound.legacy.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LegacyProdutoDTO(Long legacyId, String nome, String apresentacao, String ean, BigDecimal precoVenda, BigDecimal precoPromocional, Integer estoque, LocalDateTime updatedAt) {
}

