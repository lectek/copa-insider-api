/*
 * Decompiled with CFR 0.152.
 */
package br.com.redemaisfarma.adapters.outbound.auth.jwt.exception;

public class TokenValidationException
extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public TokenValidationException(String message) {
        super(message);
    }

    public TokenValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

