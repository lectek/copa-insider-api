/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.data.domain.PageRequest
 *  org.springframework.data.domain.Pageable
 *  org.springframework.stereotype.Service
 */
package br.com.redemaisfarma.domain.catalogo;

import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.application.view.HomePageVM;
import br.com.redemaisfarma.application.view.ProductCardVM;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class HomepageCatalogFacade {
    private final ProdutoQueryService produtoQuery;
    private final AppSettingService settings;

    public HomepageCatalogFacade(ProdutoQueryService produtoQuery, AppSettingService settings) {
        this.produtoQuery = produtoQuery;
        this.settings = settings;
    }

    public HomePageVM buildHomepage() {
        Map<String, String> cfg = this.settings.getAllByKeys(Set.of(
                "LAYOUT.layout.itens_por_pagina",
                "LAYOUT.layout.exibir_indisponiveis",
                "HOME.main_product_id"
        ));
        int limit = HomepageCatalogFacade.parseIntOrDefault(cfg.getOrDefault("LAYOUT.layout.itens_por_pagina", "24"), 24);
        boolean exibirIndisp = HomepageCatalogFacade.parseBooleanOrDefault(cfg.get("LAYOUT.layout.exibir_indisponiveis"), false);
        PageRequest page = PageRequest.of((int)0, (int)Math.min(limit, 8));
        List<ProductCardVM> maisVendidos = this.produtoQuery.topSellers((Pageable)page, exibirIndisp);
        List<ProductCardVM> novidades = this.produtoQuery.newArrivals((Pageable)page, exibirIndisp);
        List<ProductCardVM> destaques = this.produtoQuery.featured((Pageable)page, exibirIndisp);
        List<ProductCardVM> paraVoce = this.produtoQuery.recommended((Pageable)page, exibirIndisp);
        if (paraVoce.isEmpty()) {
            paraVoce = maisVendidos;
        }
        List<ProductCardVM> fallbackCandidates = Stream.of(
                destaques,
                paraVoce,
                novidades,
                maisVendidos
        ).filter(list -> list != null && !list.isEmpty())
                .flatMap(List::stream)
                .collect(Collectors.toList());

        Optional<ProductCardVM> principal = resolveProdutoPrincipal(cfg, exibirIndisp, fallbackCandidates);
        return new HomePageVM(principal.orElse(null), paraVoce, maisVendidos, novidades, destaques);
    }

    private Optional<ProductCardVM> resolveProdutoPrincipal(Map<String, String> cfg, boolean incluirIndisponiveis, List<ProductCardVM> fallbackCandidates) {
        long id = HomepageCatalogFacade.parseLongOrDefault(cfg.get("HOME.main_product_id"), 0L);
        if (id <= 0L) return Optional.empty();
        Optional<ProductCardVM> configured = this.produtoQuery.findById(id, incluirIndisponiveis);
        if (configured.isPresent()) {
            return configured;
        }
        for (ProductCardVM candidate : fallbackCandidates) {
            if (candidate != null) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

    private static int parseIntOrDefault(String v, int def) {
        if (v == null || v.isBlank()) {
            return def;
        }
        try {
            return Integer.parseInt(v.trim());
        }
        catch (NumberFormatException e) {
            return def;
        }
    }

    private static long parseLongOrDefault(String v, long def) {
        if (v == null || v.isBlank()) {
            return def;
        }
        try {
            return Long.parseLong(v.trim());
        }
        catch (NumberFormatException e) {
            return def;
        }
    }

    private static boolean parseBooleanOrDefault(String v, boolean def) {
        if (v == null || v.isBlank()) {
            return def;
        }
        String s = v.trim().toLowerCase();
        return "true".equals(s) || "1".equals(s) || "yes".equals(s) || "on".equals(s);
    }
}
