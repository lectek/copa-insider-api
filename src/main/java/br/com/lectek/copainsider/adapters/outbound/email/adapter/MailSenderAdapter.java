/*
 * Decompiled with CFR 0.152.
 */
package br.com.lectek.copainsider.adapters.outbound.email.adapter;

import java.util.List;

public interface MailSenderAdapter {
    public String send(String var1, String var2, String var3, List<String> var4);

    default public String send(String to, String subject, String htmlBody) {
        return this.send(to, subject, htmlBody, null);
    }
}

