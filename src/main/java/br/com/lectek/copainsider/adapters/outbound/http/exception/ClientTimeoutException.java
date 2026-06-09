/*
 * Decompiled with CFR 0.152.
 */
package br.com.lectek.copainsider.adapters.outbound.http.exception;

public class ClientTimeoutException
extends ExternalClientException {
    private static final long serialVersionUID = 1L;

    public ClientTimeoutException(String message, String service, String method, String url, String traceId, Throwable cause) {
        super(message, service, method, url, null, null, null, traceId, cause);
    }
}

