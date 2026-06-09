package br.com.redemaisfarma.adapters.outbound.email.config;

import jakarta.validation.constraints.AssertTrue;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "app.mail")
public class AppMailProperties {

    private boolean enabled = false;
    private String from;
    private List<String> bcc = new ArrayList<>();
    private String cssUrl;
    private int sendTimeoutMs = 10_000;

    @AssertTrue(message = "app.mail.from deve ser informado quando app.mail.enabled=true")
    public boolean isFromValidWhenEnabled() {
        return !enabled || (from != null && !from.isBlank());
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public List<String> getBcc() { return bcc; }
    public void setBcc(List<String> bcc) { this.bcc = (bcc == null ? new ArrayList<>() : bcc); }

    public String getCssUrl() { return cssUrl; }
    public void setCssUrl(String cssUrl) { this.cssUrl = cssUrl; }

    public int getSendTimeoutMs() { return sendTimeoutMs; }
    public void setSendTimeoutMs(int sendTimeoutMs) { this.sendTimeoutMs = sendTimeoutMs; }

    public boolean hasGlobalBcc() { return bcc != null && !bcc.isEmpty(); }
}
