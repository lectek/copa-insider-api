/*
 * Decompiled with CFR 0.152.
 */
package br.com.lectek.copainsider.adapters.outbound.http.exception;
import java.util.List;
import java.util.Map;

public class ServerErrorException
extends ExternalClientException {
    private static final long serialVersionUID = 1L;

    public ServerErrorException(String message, String service, String method, String url, Integer status, String responseBody, Map<String, List<String>> headers, String traceId, Throwable cause) {
        super(message, service, method, url, status, responseBody, headers, traceId, cause);
    }
}

