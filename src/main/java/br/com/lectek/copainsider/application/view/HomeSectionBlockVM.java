package br.com.lectek.copainsider.application.view;

import java.util.List;

public record HomeSectionBlockVM(
        String key,
        String type,
        String title,
        String subtitle,
        String layout,
        int order,
        String ctaLabel,
        String ctaHref,
        String carouselStyle,
        List<ProductCardVM> items,
        List<ProductCategorySectionVM> categorySections
) {

    public HomeSectionBlockVM {
        items = items == null ? List.of() : List.copyOf(items);
        categorySections = categorySections == null ? List.of() : List.copyOf(categorySections);
    }
}
