/*
 * Decompiled with CFR 0.152.
 */
package br.com.redemaisfarma.application.view;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AssinaturaView(Long id, String clienteNome, String plano, BigDecimal valor, LocalDate renovaEm, String status) {
}

