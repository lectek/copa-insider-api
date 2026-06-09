/*
 * Decompiled with CFR 0.152.
 */
package br.com.lectek.copainsider.application.core.exception;

public class InvalidCredentialsException
extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "Credenciais inv\u00e1lidas. Verifique e tente novamente.";

    public InvalidCredentialsException() {
        super(DEFAULT_MESSAGE);
    }

    public InvalidCredentialsException(String message) {
        super(message != null ? message : DEFAULT_MESSAGE);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message != null ? message : DEFAULT_MESSAGE, cause);
    }

    public InvalidCredentialsException(Throwable cause) {
        super(DEFAULT_MESSAGE, cause);
    }
}

