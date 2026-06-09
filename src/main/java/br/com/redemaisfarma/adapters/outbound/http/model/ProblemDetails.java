/*
 * Decompiled with CFR 0.152.
 */
package br.com.redemaisfarma.adapters.outbound.http.model;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;

public class ProblemDetails
implements Serializable {
    private static final long serialVersionUID = 1L;
    private String type;
    private String title;
    private int status;
    private String detail;
    private String instance;
    private OffsetDateTime timestamp;
    private Map<String, Object> extras;

    public ProblemDetails() {
    }

    public ProblemDetails(String type, String title, int status, String detail, String instance, OffsetDateTime timestamp, Map<String, Object> extras) {
        this.type = type;
        this.title = title;
        this.status = status;
        this.detail = detail;
        this.instance = instance;
        this.timestamp = timestamp;
        this.extras = extras;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDetail() {
        return this.detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getInstance() {
        return this.instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public OffsetDateTime getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getExtras() {
        return this.extras;
    }

    public void setExtras(Map<String, Object> extras) {
        this.extras = extras;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProblemDetails)) {
            return false;
        }
        ProblemDetails that = (ProblemDetails)o;
        return this.status == that.status && Objects.equals(this.type, that.type) && Objects.equals(this.title, that.title) && Objects.equals(this.detail, that.detail) && Objects.equals(this.instance, that.instance) && Objects.equals(this.timestamp, that.timestamp) && Objects.equals(this.extras, that.extras);
    }

    public int hashCode() {
        return Objects.hash(this.type, this.title, this.status, this.detail, this.instance, this.timestamp, this.extras);
    }

    public String toString() {
        return "ProblemDetails{status=" + this.status + ", title='" + this.title + "', detail='" + this.detail + "'}";
    }
}


