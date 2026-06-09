/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.NotNull
 *  jakarta.validation.constraints.Size
 *  org.hibernate.validator.constraints.URL
 */
package br.com.redemaisfarma.application.view.admin;

import br.com.redemaisfarma.domain.financeiro.config.GatewayConfig;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Objects;
import org.hibernate.validator.constraints.URL;

public class GatewayConfigForm
implements Serializable {
    private Long id;
    @NotBlank(message="Nome de exibi\u00e7\u00e3o \u00e9 obrigat\u00f3rio")
    @Size(max=120, message="Nome pode ter no m\u00e1ximo 120 caracteres")
    private @NotBlank(message="Nome de exibi\u00e7\u00e3o \u00e9 obrigat\u00f3rio") @Size(max=120, message="Nome pode ter no m\u00e1ximo 120 caracteres") String nome;
    @NotBlank(message="Provedor \u00e9 obrigat\u00f3rio")
    @Size(max=60, message="Provedor pode ter no m\u00e1ximo 60 caracteres")
    private @NotBlank(message="Provedor \u00e9 obrigat\u00f3rio") @Size(max=60, message="Provedor pode ter no m\u00e1ximo 60 caracteres") String provedor;
    @NotBlank(message="API Key \u00e9 obrigat\u00f3ria")
    @Size(max=255, message="API Key pode ter no m\u00e1ximo 255 caracteres")
    private @NotBlank(message="API Key \u00e9 obrigat\u00f3ria") @Size(max=255, message="API Key pode ter no m\u00e1ximo 255 caracteres") String apiKey;
    @Size(max=255, message="API Secret pode ter no m\u00e1ximo 255 caracteres")
    private @Size(max=255, message="API Secret pode ter no m\u00e1ximo 255 caracteres") String apiSecret;
    @URL(message="Webhook URL inv\u00e1lida")
    @Size(max=512, message="Webhook URL pode ter no m\u00e1ximo 512 caracteres")
    private @URL(message="Webhook URL inv\u00e1lida") @Size(max=512, message="Webhook URL pode ter no m\u00e1ximo 512 caracteres") String webhookUrl;
    @NotNull(message="Campo 'ativo' \u00e9 obrigat\u00f3rio")
    private @NotNull(message="Campo 'ativo' \u00e9 obrigat\u00f3rio") Boolean ativo = Boolean.TRUE;
    @NotNull(message="Timeout \u00e9 obrigat\u00f3rio")
    private @NotNull(message="Timeout \u00e9 obrigat\u00f3rio") Integer timeoutMs = 10000;
    @NotNull(message="Quantidade de tentativas \u00e9 obrigat\u00f3ria")
    private @NotNull(message="Quantidade de tentativas \u00e9 obrigat\u00f3ria") Integer maxRetries = 3;
    @Size(max=4000, message="Metadados pode ter no m\u00e1ximo 4000 caracteres")
    private @Size(max=4000, message="Metadados pode ter no m\u00e1ximo 4000 caracteres") String metadata;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return this.nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getProvedor() {
        return this.provedor;
    }

    public void setProvedor(String provedor) {
        this.provedor = provedor;
    }

    public String getApiKey() {
        return this.apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiSecret() {
        return this.apiSecret;
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    public String getWebhookUrl() {
        return this.webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public Boolean getAtivo() {
        return this.ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public Integer getTimeoutMs() {
        return this.timeoutMs;
    }

    public void setTimeoutMs(Integer timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public Integer getMaxRetries() {
        return this.maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public String getMetadata() {
        return this.metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public GatewayConfig toEntity() {
        GatewayConfig e = new GatewayConfig();
        e.setId(this.id);
        e.setNome(this.nome);
        e.setProvedor(this.provedor);
        e.setApiKey(this.apiKey);
        e.setApiSecret(this.apiSecret);
        e.setWebhookUrl(this.webhookUrl);
        e.setAtivo(Boolean.TRUE.equals(this.ativo));
        e.setTimeoutMs(this.timeoutMs);
        e.setMaxRetries(this.maxRetries);
        e.setMetadata(this.metadata);
        return e;
    }

    public static GatewayConfigForm fromEntity(GatewayConfig e) {
        Objects.requireNonNull(e, "Entidade n\u00e3o pode ser nula");
        GatewayConfigForm f = new GatewayConfigForm();
        f.setId(e.getId());
        f.setNome(e.getNome());
        f.setProvedor(e.getProvedor());
        f.setApiKey(e.getApiKey());
        f.setApiSecret(e.getApiSecret());
        f.setWebhookUrl(e.getWebhookUrl());
        f.setAtivo(e.isAtivo());
        f.setTimeoutMs(e.getTimeoutMs());
        f.setMaxRetries(e.getMaxRetries());
        f.setMetadata(e.getMetadata());
        return f;
    }
}

