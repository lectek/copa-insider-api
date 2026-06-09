package br.com.redemaisfarma.application.view;

import java.math.BigDecimal;
import java.util.List;

public record CartSummaryVM(
        List<CartItemVM> items,
        BigDecimal subtotal,
        BigDecimal total,
        boolean hasInvalidItems
) {
}
