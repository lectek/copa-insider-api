package br.com.redemaisfarma.adapters.outbound.http.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Envelope genérico de paginação para respostas HTTP.
 * @param <T> tipo dos itens na página
 */
public class PageResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Itens da página atual (nunca null). */
    private List<T> content;

    /** Metadados de paginação (nunca null). */
    private PageMetadata metadata;

    /** Construtor padrão para (de)serialização. */
    public PageResponse() {
        this.content  = Collections.emptyList();
        this.metadata = new PageMetadata();
    }

    public PageResponse(List<T> content, PageMetadata metadata) {
        this.content  = content  != null ? List.copyOf(content) : Collections.emptyList();
        this.metadata = metadata != null ? metadata : new PageMetadata();
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content != null ? List.copyOf(content) : Collections.emptyList();
    }

    public PageMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(PageMetadata metadata) {
        this.metadata = metadata != null ? metadata : new PageMetadata();
    }

    /** Conveniência: true se não há itens. */
    public boolean isEmpty() {
        return content == null || content.isEmpty();
    }

    /** Conveniência: quantidade de itens na página (0 se content for null). */
    public int size() {
        return content == null ? 0 : content.size();
    }

    /**
     * Mapeia a página atual para outro tipo, preservando metadados.
     * @param mapper função de transformação de item
     * @return nova PageResponse com itens transformados
     */
    public <R> PageResponse<R> map(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        List<? extends R> mapped = (this.content == null)
                ? Collections.emptyList()
                : this.content.stream().map(mapper).toList();
        return new PageResponse<>(List.copyOf(mapped), this.metadata);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PageResponse<?> that)) return false;
        return Objects.equals(content, that.content) &&
               Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, metadata);
    }

    @Override
    public String toString() {
        return "PageResponse{contentSize=" + size() + ", metadata=" + metadata + "}";
    }
}
