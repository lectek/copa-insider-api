/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.constraints.NotBlank
 *  org.springframework.boot.context.properties.ConfigurationProperties
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.validation.annotation.Validated
 */
package br.com.redemaisfarma.application.config;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix="app.config")
@Validated
public class AppConfigProperties {
    @NotBlank
    private String envProfileLabel;
    @NotBlank
    private String networkPortLabel;
    private String systemName;
    private String version;
    private Map<String, String> dynamicLabels;

    public String getEnvProfileLabel() {
        return this.envProfileLabel;
    }

    public void setEnvProfileLabel(String envProfileLabel) {
        this.envProfileLabel = envProfileLabel;
    }

    public String getNetworkPortLabel() {
        return this.networkPortLabel;
    }

    public void setNetworkPortLabel(String networkPortLabel) {
        this.networkPortLabel = networkPortLabel;
    }

    public String getSystemName() {
        return this.systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getDynamicLabels() {
        return this.dynamicLabels;
    }

    public void setDynamicLabels(Map<String, String> dynamicLabels) {
        this.dynamicLabels = dynamicLabels;
    }
}

