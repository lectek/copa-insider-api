/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.constraints.Min
 *  jakarta.validation.constraints.Min$List
 *  jakarta.validation.constraints.NotBlank
 *  org.springframework.boot.context.properties.ConfigurationProperties
 *  org.springframework.validation.annotation.Validated
 */
package br.com.lectek.copainsider.adapters.outbound.cache.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix="redis")
public class RedisProperties {
    private boolean enabled = true;
    @NotBlank
    private String host = "localhost";
    @Min(value=1L)
@Min(value=1L)
    private @Min(value=1L)
@Min(value=1L) int port = 6379;
    private String password;
    private int database = 0;
    private boolean ssl = false;
    private long timeoutMs = 2000L;
    @NotBlank
    private String keyPrefix = "rmf:";
    @NotBlank
    private String blacklistNamespace = "auth:blacklist:";

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getDatabase() {
        return this.database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public boolean isSsl() {
        return this.ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public long getTimeoutMs() {
        return this.timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public String getKeyPrefix() {
        return this.keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public String getBlacklistNamespace() {
        return this.blacklistNamespace;
    }

    public void setBlacklistNamespace(String blacklistNamespace) {
        this.blacklistNamespace = blacklistNamespace;
    }
}

