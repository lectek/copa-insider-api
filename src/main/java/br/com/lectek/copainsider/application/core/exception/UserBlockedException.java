/*
 * Decompiled with CFR 0.152.
 */
package br.com.lectek.copainsider.application.core.exception;

public class UserBlockedException
extends RuntimeException {
    public UserBlockedException() {
        super("Usu\u00e1rio temporariamente bloqueado por tentativas de login inv\u00e1lidas.");
    }

    public UserBlockedException(String message) {
        super(message);
    }
}

