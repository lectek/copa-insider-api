package br.com.redemaisfarma.application.service.validation;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoStatus;
import br.com.redemaisfarma.domain.support.BarcodeNormalizer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Regras centralizadas para elegibilidade publica de produtos.
 */
public final class ProductPublicEligibility {

    private ProductPublicEligibility() {
    }

    public static boolean isPubliclySellable(ProdutoEntity produto) {
        if (produto == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return Boolean.TRUE.equals(produto.getDisponivel())
                && isAllowedPublicSource(produto)
                && hasValidBarcode(produto.getCodigoBarras())
                && hasPositive(produto.getPrecoVenda())
                && hasPositiveStock(produto.getEstoque())
                && produto.getStatus() == ProdutoStatus.PUBLICADO
                && isWithinPublishWindow(produto, now);
    }

    public static boolean isStockSubscribable(ProdutoEntity produto) {
        if (produto == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return Boolean.TRUE.equals(produto.getDisponivel())
                && isAllowedPublicSource(produto)
                && hasValidBarcode(produto.getCodigoBarras())
                && hasPositive(produto.getPrecoVenda())
                && produto.getStatus() == ProdutoStatus.PUBLICADO
                && isWithinPublishWindow(produto, now);
    }

    private static boolean hasPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    private static boolean hasPositiveStock(Integer estoque) {
        return estoque != null && estoque > 0;
    }

    private static boolean hasValidBarcode(final String codigoBarras) {
        final String normalized = BarcodeNormalizer.normalizeOrNull(codigoBarras);
        if (normalized == null) {
            return false;
        }
        final int length = normalized.length();
        return length == 8 || (length >= 12 && length <= 14);
    }

    private static boolean isAllowedPublicSource(ProdutoEntity produto) {
        return ProductSourcePolicy.isLocalSellableSource(produto);
    }

    private static boolean isWithinPublishWindow(ProdutoEntity produto, LocalDateTime now) {
        LocalDateTime publicadoEm = produto.getPublicadoEm();
        if (publicadoEm != null && publicadoEm.isAfter(now)) {
            return false;
        }
        LocalDateTime despublicadoEm = produto.getDespublicadoEm();
        return despublicadoEm == null || despublicadoEm.isAfter(now);
    }
}
