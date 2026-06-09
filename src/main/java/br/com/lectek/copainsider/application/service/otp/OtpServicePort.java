/*
 * Decompiled with CFR 0.152.
 */
package br.com.lectek.copainsider.application.service.otp;

public interface OtpServicePort {
    public StartResult start(String var1, String var2, String var3);

    public String verify(String var1, String var2);

    public boolean consumeToken(String var1);

    public boolean consumeTokenForDestino(String var1, String var2);

    public static class OtpException
    extends RuntimeException {
        private final String reason;

        public OtpException(String reason, String message) {
            super(message);
            this.reason = reason;
        }

        public String reason() {
            return this.reason;
        }
    }

    public record StartResult(String deliveryId, String maskedDestino, int cooldownSec, int ttlSeconds, String demoCode) {
    }
}

