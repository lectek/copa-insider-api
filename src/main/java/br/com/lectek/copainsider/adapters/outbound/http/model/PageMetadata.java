package br.com.lectek.copainsider.adapters.outbound.http.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Metadados de paginação.
 * Compatível com o construtor antigo (page, size, totalElements, totalPages),
 * com cálculos seguros e flags convenientes (first/last).
 */
public class PageMetadata implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Índice da página (0-based). */
    private int page;

    /** Tamanho da página (itens por página). */
    private int size;

    /** Total de elementos. */
    private long totalElements;

    /** Total de páginas. */
    private int totalPages;

    /** É a primeira página? */
    private boolean first;

    /** É a última página? */
    private boolean last;

    public PageMetadata() { }

    /** Construtor antigo (mantido). */
    public PageMetadata(int page, int size, long totalElements, int totalPages) {
        this.page = Math.max(0, page);
        this.size = Math.max(1, size);
        this.totalElements = Math.max(0, totalElements);
        this.totalPages = Math.max(0, totalPages);
        recalcFirstLast();
    }

    /** Construtor completo. */
    public PageMetadata(int page, int size, long totalElements, int totalPages, boolean first, boolean last) {
        this.page = Math.max(0, page);
        this.size = Math.max(1, size);
        this.totalElements = Math.max(0, totalElements);
        this.totalPages = Math.max(0, totalPages);
        this.first = first;
        this.last = last;
    }

    /** Fábrica com cálculo automático de totalPages. */
    public static PageMetadata of(int page, int size, long totalElements) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, size);
        long safeTotal = Math.max(0, totalElements);

        int pages = safeTotal == 0 ? 0 : (int) Math.max(1, (safeTotal + safeSize - 1) / safeSize);
        PageMetadata meta = new PageMetadata(safePage, safeSize, safeTotal, pages);
        meta.recalcFirstLast();
        return meta;
    }

    private void recalcFirstLast() {
        this.first = (this.page <= 0) || (this.totalPages == 0);
        this.last  = (this.totalPages == 0) || (this.page >= Math.max(0, this.totalPages - 1));
    }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = Math.max(0, page); recalcFirstLast(); }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = Math.max(1, size); recalcFirstLast(); }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) {
        this.totalElements = Math.max(0, totalElements);
        recalcFirstLast();
    }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = Math.max(0, totalPages); recalcFirstLast(); }

    public boolean isFirst() { return first; }
    public void setFirst(boolean first) { this.first = first; }

    public boolean isLast() { return last; }
    public void setLast(boolean last) { this.last = last; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PageMetadata)) return false;
        PageMetadata that = (PageMetadata) o;
        return page == that.page &&
               size == that.size &&
               totalElements == that.totalElements &&
               totalPages == that.totalPages &&
               first == that.first &&
               last == that.last;
    }

    @Override
    public int hashCode() {
        return Objects.hash(page, size, totalElements, totalPages, first, last);
    }

    @Override
    public String toString() {
        return "PageMetadata{" +
                "page=" + page +
                ", size=" + size +
                ", totalElements=" + totalElements +
                ", totalPages=" + totalPages +
                ", first=" + first +
                ", last=" + last +
                '}';
    }
}
