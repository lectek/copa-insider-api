/*
 * Decompiled with CFR 0.152.
 */
package br.com.lectek.copainsider.adapters.outbound.email.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class EmailMessage
implements Serializable {
    private List<String> to;
    private List<String> cc;
    private List<String> bcc;
    private String subject;
    private String htmlBody;
    private String textBody;

    public List<String> getTo() {
        return this.to;
    }

    public void setTo(List<String> to) {
        this.to = to;
    }

    public List<String> getCc() {
        return this.cc;
    }

    public void setCc(List<String> cc) {
        this.cc = cc;
    }

    public List<String> getBcc() {
        return this.bcc;
    }

    public void setBcc(List<String> bcc) {
        this.bcc = bcc;
    }

    public String getSubject() {
        return this.subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getHtmlBody() {
        return this.htmlBody;
    }

    public void setHtmlBody(String htmlBody) {
        this.htmlBody = htmlBody;
    }

    public String getTextBody() {
        return this.textBody;
    }

    public void setTextBody(String textBody) {
        this.textBody = textBody;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EmailMessage)) {
            return false;
        }
        EmailMessage that = (EmailMessage)o;
        return Objects.equals(this.to, that.to) && Objects.equals(this.subject, that.subject);
    }

    public int hashCode() {
        return Objects.hash(this.to, this.subject);
    }
}

