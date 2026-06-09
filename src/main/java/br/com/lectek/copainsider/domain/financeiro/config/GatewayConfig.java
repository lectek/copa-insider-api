/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.persistence.Column
 *  jakarta.persistence.Entity
 *  jakarta.persistence.GeneratedValue
 *  jakarta.persistence.GenerationType
 *  jakarta.persistence.Id
 *  jakarta.persistence.Index
 *  jakarta.persistence.Lob
 *  jakarta.persistence.Table
 *  jakarta.persistence.UniqueConstraint
 *  org.hibernate.annotations.CreationTimestamp
 *  org.hibernate.annotations.UpdateTimestamp
 */
package br.com.lectek.copainsider.domain.financeiro.config;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name="gateway_config", uniqueConstraints={@UniqueConstraint(name="uk_gateway_provedor_nome", columnNames={"provedor", "nome"})}, indexes={@Index(name="idx_gateway_provedor", columnList="provedor"), @Index(name="idx_gateway_ativo", columnList="ativo")})
public class GatewayConfig
implements Serializable {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, length=120)
    private String nome;
    @Column(nullable=false, length=60)
    private String provedor;
    @Column(name="api_key", nullable=false, length=255)
    private String apiKey;
    @Column(name="api_secret", length=255)
    private String apiSecret;
    @Column(name="webhook_url", length=512)
    private String webhookUrl;
    @Column(nullable=false)
    private boolean ativo = true;
    @Column(name="timeout_ms", nullable=false)
    private Integer timeoutMs = 10000;
    @Column(name="max_retries", nullable=false)
    private Integer maxRetries = 3;
    @Lob
    @Column(columnDefinition="TEXT")
    private String metadata;
    @CreationTimestamp
    @Column(name="criado_em", nullable=false, updatable=false)
    private OffsetDateTime criadoEm;
    @UpdateTimestamp
    @Column(name="atualizado_em", nullable=false)
    private OffsetDateTime atualizadoEm;

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

    public boolean isAtivo() {
        return this.ativo;
    }

    public void setAtivo(boolean ativo) {
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

    public OffsetDateTime getCriadoEm() {
        return this.criadoEm;
    }

    public OffsetDateTime getAtualizadoEm() {
        return this.atualizadoEm;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GatewayConfig)) {
            return false;
        }
        GatewayConfig that = (GatewayConfig)o;
        return Objects.equals(this.id, that.id);
    }

    public int hashCode() {
        return Objects.hash(this.id);
    }

    public String toString() {
        return "GatewayConfig{id=" + this.id + ", nome='" + this.nome + "', provedor='" + this.provedor + "', ativo=" + this.ativo + "}";
    }
}

