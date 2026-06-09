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
 *  jakarta.persistence.Table
 *  jakarta.persistence.UniqueConstraint
 *  lombok.Generated
 *  org.hibernate.annotations.CreationTimestamp
 *  org.hibernate.annotations.UpdateTimestamp
 */
package br.com.lectek.copainsider.adapters.outbound.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.Generated;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name="customers", uniqueConstraints={@UniqueConstraint(name="uk_customers_email", columnNames={"email"}), @UniqueConstraint(name="uk_customers_provider_user", columnNames={"provider", "provider_user_id"})}, indexes={@Index(name="idx_customers_provider_user", columnList="provider, provider_user_id")})
public class CustomerEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(name="nome", nullable=false, length=120)
    private String nome;
    @Column(name="email", length=180, unique=true)
    private String email;
    @Column(name="avatar_url", length=512)
    private String avatarUrl;
    @Column(name="provider", length=32)
    private String provider;
    @Column(name="provider_user_id", length=128)
    private String providerUserId;
    @Column(name="email_verificado", nullable=false)
    private boolean emailVerificado;
    @Column(name="ativo", nullable=false)
    private boolean ativo;
    @CreationTimestamp
    @Column(name="created_at", nullable=false, updatable=false)
    private Instant createdAt;
    @UpdateTimestamp
    @Column(name="updated_at")
    private Instant updatedAt;

    @Generated
    private static boolean $default$emailVerificado() {
        return false;
    }

    @Generated
    private static boolean $default$ativo() {
        return true;
    }

    @Generated
    public static CustomerEntityBuilder builder() {
        return new CustomerEntityBuilder();
    }

    @Generated
    public Long getId() {
        return this.id;
    }

    @Generated
    public String getNome() {
        return this.nome;
    }

    @Generated
    public String getEmail() {
        return this.email;
    }

    @Generated
    public String getAvatarUrl() {
        return this.avatarUrl;
    }

    @Generated
    public String getProvider() {
        return this.provider;
    }

    @Generated
    public String getProviderUserId() {
        return this.providerUserId;
    }

    @Generated
    public boolean isEmailVerificado() {
        return this.emailVerificado;
    }

    @Generated
    public boolean isAtivo() {
        return this.ativo;
    }

    @Generated
    public Instant getCreatedAt() {
        return this.createdAt;
    }

    @Generated
    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    @Generated
    public void setId(Long id) {
        this.id = id;
    }

    @Generated
    public void setNome(String nome) {
        this.nome = nome;
    }

    @Generated
    public void setEmail(String email) {
        this.email = email;
    }

    @Generated
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    @Generated
    public void setProvider(String provider) {
        this.provider = provider;
    }

    @Generated
    public void setProviderUserId(String providerUserId) {
        this.providerUserId = providerUserId;
    }

    @Generated
    public void setEmailVerificado(boolean emailVerificado) {
        this.emailVerificado = emailVerificado;
    }

    @Generated
    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    @Generated
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Generated
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Generated
    public CustomerEntity() {
        this.emailVerificado = CustomerEntity.$default$emailVerificado();
        this.ativo = CustomerEntity.$default$ativo();
    }

    @Generated
    public CustomerEntity(Long id, String nome, String email, String avatarUrl, String provider, String providerUserId, boolean emailVerificado, boolean ativo, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.emailVerificado = emailVerificado;
        this.ativo = ativo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Generated
    public static class CustomerEntityBuilder {
        @Generated
        private Long id;
        @Generated
        private String nome;
        @Generated
        private String email;
        @Generated
        private String avatarUrl;
        @Generated
        private String provider;
        @Generated
        private String providerUserId;
        @Generated
        private boolean emailVerificado$set;
        @Generated
        private boolean emailVerificado$value;
        @Generated
        private boolean ativo$set;
        @Generated
        private boolean ativo$value;
        @Generated
        private Instant createdAt;
        @Generated
        private Instant updatedAt;

        @Generated
        CustomerEntityBuilder() {
        }

        @Generated
        public CustomerEntityBuilder id(Long id) {
            this.id = id;
            return this;
        }

        @Generated
        public CustomerEntityBuilder nome(String nome) {
            this.nome = nome;
            return this;
        }

        @Generated
        public CustomerEntityBuilder email(String email) {
            this.email = email;
            return this;
        }

        @Generated
        public CustomerEntityBuilder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        @Generated
        public CustomerEntityBuilder provider(String provider) {
            this.provider = provider;
            return this;
        }

        @Generated
        public CustomerEntityBuilder providerUserId(String providerUserId) {
            this.providerUserId = providerUserId;
            return this;
        }

        @Generated
        public CustomerEntityBuilder emailVerificado(boolean emailVerificado) {
            this.emailVerificado$value = emailVerificado;
            this.emailVerificado$set = true;
            return this;
        }

        @Generated
        public CustomerEntityBuilder ativo(boolean ativo) {
            this.ativo$value = ativo;
            this.ativo$set = true;
            return this;
        }

        @Generated
        public CustomerEntityBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        @Generated
        public CustomerEntityBuilder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        @Generated
        public CustomerEntity build() {
            boolean emailVerificado$value = this.emailVerificado$value;
            if (!this.emailVerificado$set) {
                emailVerificado$value = CustomerEntity.$default$emailVerificado();
            }
            boolean ativo$value = this.ativo$value;
            if (!this.ativo$set) {
                ativo$value = CustomerEntity.$default$ativo();
            }
            return new CustomerEntity(this.id, this.nome, this.email, this.avatarUrl, this.provider, this.providerUserId, emailVerificado$value, ativo$value, this.createdAt, this.updatedAt);
        }

        @Generated
        public String toString() {
            return "CustomerEntity.CustomerEntityBuilder(id=" + String.valueOf(this.id) + ", nome=" + this.nome + ", email=" + this.email + ", avatarUrl=" + this.avatarUrl + ", provider=" + this.provider + ", providerUserId=" + this.providerUserId + ", emailVerificado$value=" + this.emailVerificado$value + ", ativo$value=" + this.ativo$value + ", createdAt=" + String.valueOf(this.createdAt) + ", updatedAt=" + String.valueOf(this.updatedAt) + ")";
        }
    }
}

