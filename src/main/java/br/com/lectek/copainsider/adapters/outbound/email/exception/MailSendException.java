/*
 * Decompiled with CFR 0.152.
 */
package br.com.lectek.copainsider.adapters.outbound.email.exception;

public class MailSendException
extends RuntimeException {
    public MailSendException(String message) {
        super(message);
    }

    public MailSendException(String message, Throwable cause) {
        super(message, cause);
    }
}

