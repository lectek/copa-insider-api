/*
 * Decompiled with CFR 0.152.
 */
package br.com.redemaisfarma.adapters.outbound.http.exception;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ExternalClientException
extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final String service;
    private final String method;
    private final String url;
    private final Integer status;
    private final String responseBody;
    private final Map<String, List<String>> headers;
    private final String traceId;

    public ExternalClientException(String message, String service, String method, String url, Integer status, String responseBody, Map<String, List<String>> headers, String traceId, Throwable cause) {
        super(message, cause);
        this.service = service;
        this.method = method;
        this.url = url;
        this.status = status;
        this.responseBody = responseBody;
        this.headers = headers;
        this.traceId = traceId;
    }

    public String getService() {
        return this.service;
    }

    public String getMethod() {
        return this.method;
    }

    public String getUrl() {
        return this.url;
    }

    public Integer getStatus() {
        return this.status;
    }

    public String getResponseBody() {
        return this.responseBody;
    }

    public Map<String, List<String>> getHeaders() {
        return this.headers;
    }

    public String getTraceId() {
        return this.traceId;
    }

    @Override
    public String toString() {
        return "ExternalClientException{service='" + this.service + "', method='" + this.method + "', url='" + this.url + "', status=" + String.valueOf(this.status) + ", traceId='" + this.traceId + "'}";
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ExternalClientException)) {
            return false;
        }
        ExternalClientException that = (ExternalClientException)o;
        return Objects.equals(this.service, that.service) && Objects.equals(this.method, that.method) && Objects.equals(this.url, that.url) && Objects.equals(this.status, that.status) && Objects.equals(this.traceId, that.traceId);
    }

    public int hashCode() {
        return Objects.hash(this.service, this.method, this.url, this.status, this.traceId);
    }
}

