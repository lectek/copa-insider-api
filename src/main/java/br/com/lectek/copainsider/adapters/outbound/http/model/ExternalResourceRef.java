/*
 * Decompiled with CFR 0.152.
 */
package br.com.lectek.copainsider.adapters.outbound.http.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class ExternalResourceRef
implements Serializable {
    private static final long serialVersionUID = 1L;
    private UUID id;
    private String name;

    public ExternalResourceRef() {
    }

    public ExternalResourceRef(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return this.id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExternalResourceRef)) {
            return false;
        }
        ExternalResourceRef that = (ExternalResourceRef)o;
        return Objects.equals(this.id, that.id) && Objects.equals(this.name, that.name);
    }

    public int hashCode() {
        return Objects.hash(this.id, this.name);
    }

    public String toString() {
        return "ExternalResourceRef{id=" + String.valueOf(this.id) + ", name='" + this.name + "'}";
    }
}

