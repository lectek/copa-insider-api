package br.com.redemaisfarma.adapters.outbound.http.cliente.config;

import jakarta.validation.constraints.AssertTrue;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "integrations.cliente")
public class ClienteClientProperties {

    private boolean enabled;
    private String baseUrl;
    private int maxInMemorySize = 0xA00000;
    private int readTimeoutMs = 10000;
    private int writeTimeoutMs = 10000;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getMaxInMemorySize() {
        return maxInMemorySize;
    }

    public void setMaxInMemorySize(int maxInMemorySize) {
        this.maxInMemorySize = maxInMemorySize;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public int getWriteTimeoutMs() {
        return writeTimeoutMs;
    }

    public void setWriteTimeoutMs(int writeTimeoutMs) {
        this.writeTimeoutMs = writeTimeoutMs;
    }

    @AssertTrue(message = "integrations.cliente.baseUrl deve ser informado quando integrations.cliente.enabled=true")
    public boolean isBaseUrlConfiguredWhenEnabled() {
        return !enabled || (baseUrl != null && !baseUrl.isBlank());
    }
}
