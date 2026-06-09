/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonProperty
 */
package br.com.redemaisfarma.adapters.outbound.http.cliente.model;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;

public class ClientePageExternal
implements Serializable {
    @JsonProperty(value="content")
    private List<ClienteExternal> content;
    @JsonProperty(value="page")
    private PageMetadataExternal page;

    public List<ClienteExternal> getContent() {
        return this.content;
    }

    public void setContent(List<ClienteExternal> content) {
        this.content = content;
    }

    public PageMetadataExternal getPage() {
        return this.page;
    }

    public void setPage(PageMetadataExternal page) {
        this.page = page;
    }
}

