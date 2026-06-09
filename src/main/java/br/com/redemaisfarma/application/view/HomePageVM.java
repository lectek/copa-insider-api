/*
 * Decompiled with CFR 0.152.
 */
package br.com.redemaisfarma.application.view;

import java.util.List;

public record HomePageVM(
        ProductCardVM produtoPrincipal,
        List<ProductCardVM> paraVoce,
        List<ProductCardVM> maisVendidos,
        List<ProductCardVM> novidades,
        List<ProductCardVM> destaque
) { }
