/*
 * Decompiled with CFR 0.152.
 */
package br.com.redemaisfarma.adapters.outbound.auth.jwt.exception;

public class MalformedTokenException
extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public MalformedTokenException(String message) {
        super(message);
    }

    public MalformedTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}

