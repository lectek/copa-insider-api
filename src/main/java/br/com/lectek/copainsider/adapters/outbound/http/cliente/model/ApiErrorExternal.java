/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonProperty
 */
package br.com.lectek.copainsider.adapters.outbound.http.cliente.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.OffsetDateTime;

public class ApiErrorExternal
implements Serializable {
    @JsonProperty(value="timestamp")
    private OffsetDateTime timestamp;
    @JsonProperty(value="status")
    private Integer status;
    @JsonProperty(value="error")
    private String error;
    @JsonProperty(value="message")
    private String message;
    @JsonProperty(value="path")
    private String path;

    public OffsetDateTime getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getError() {
        return this.error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}

