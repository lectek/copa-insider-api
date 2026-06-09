/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonProperty
 */
package br.com.redemaisfarma.adapters.outbound.http.cliente.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class PageMetadataExternal
implements Serializable {
    @JsonProperty(value="page")
    private Integer page;
    @JsonProperty(value="size")
    private Integer size;
    @JsonProperty(value="totalElements")
    private Long totalElements;
    @JsonProperty(value="totalPages")
    private Integer totalPages;
    @JsonProperty(value="sorted")
    private Boolean sorted;

    public Integer getPage() {
        return this.page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return this.size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Long getTotalElements() {
        return this.totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public Integer getTotalPages() {
        return this.totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Boolean getSorted() {
        return this.sorted;
    }

    public void setSorted(Boolean sorted) {
        this.sorted = sorted;
    }
}

