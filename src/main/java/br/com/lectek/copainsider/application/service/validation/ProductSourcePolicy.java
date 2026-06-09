package br.com.lectek.copainsider.application.service.validation;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.MetodoLeituraCodigoBarras;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;

import java.util.EnumSet;
import java.util.Set;

/**
 * Regras centralizadas de ownership/origem dos produtos.
 */
public final class ProductSourcePolicy {

    private static final Set<MetodoLeituraCodigoBarras> LOCAL_SELLABLE_SOURCES = EnumSet.of(
            MetodoLeituraCodigoBarras.MANUAL,
            MetodoLeituraCodigoBarras.SCANNER,
            MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA,
            MetodoLeituraCodigoBarras.API
    );
    private static final Set<MetodoLeituraCodigoBarras> STOCK_IMPORT_OWNED_SOURCES = EnumSet.of(
            MetodoLeituraCodigoBarras.CSV_ESTOQUE,
            MetodoLeituraCodigoBarras.DESCONHECIDO
    );
    private static final Set<MetodoLeituraCodigoBarras> CATALOG_SYNC_OWNED_SOURCES = EnumSet.of(
            MetodoLeituraCodigoBarras.PDF_CATALOGO_VENDA,
            MetodoLeituraCodigoBarras.CSV_ESTOQUE,
            MetodoLeituraCodigoBarras.LEGADO,
            MetodoLeituraCodigoBarras.DESCONHECIDO
    );

    private ProductSourcePolicy() {
    }

    public static boolean isLocalSellableSource(ProdutoEntity produto) {
        return produto != null && isLocalSellableSource(produto.getMetodoLeituraCodigoBarras());
    }

    public static boolean isLocalSellableSource(MetodoLeituraCodigoBarras metodo) {
        return metodo != null && LOCAL_SELLABLE_SOURCES.contains(metodo);
    }

    public static boolean isProtectedFromLegacySync(ProdutoEntity produto) {
        return isLocalSellableSource(produto);
    }

    public static boolean isManagedByStockImport(ProdutoEntity produto) {
        return produto == null || isManagedByStockImport(produto.getMetodoLeituraCodigoBarras());
    }

    public static boolean isManagedByStockImport(MetodoLeituraCodigoBarras metodo) {
        return metodo == null || STOCK_IMPORT_OWNED_SOURCES.contains(metodo);
    }

    public static boolean isProtectedFromStockImport(ProdutoEntity produto) {
        return produto != null && !isManagedByStockImport(produto.getMetodoLeituraCodigoBarras());
    }

    public static boolean isManagedByCatalogSync(ProdutoEntity produto) {
        return produto == null || isManagedByCatalogSync(produto.getMetodoLeituraCodigoBarras());
    }

    public static boolean isManagedByCatalogSync(MetodoLeituraCodigoBarras metodo) {
        return metodo == null || CATALOG_SYNC_OWNED_SOURCES.contains(metodo);
    }

    public static boolean isProtectedFromCatalogSync(ProdutoEntity produto) {
        return produto != null && !isManagedByCatalogSync(produto.getMetodoLeituraCodigoBarras());
    }
}
