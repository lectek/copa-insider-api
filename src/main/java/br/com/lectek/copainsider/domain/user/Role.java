/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.persistence.Column
 *  jakarta.persistence.Entity
 *  jakarta.persistence.GeneratedValue
 *  jakarta.persistence.GenerationType
 *  jakarta.persistence.Id
 *  jakarta.persistence.PrePersist
 *  jakarta.persistence.PreUpdate
 *  jakarta.persistence.Table
 *  jakarta.persistence.Transient
 *  jakarta.persistence.UniqueConstraint
 */
package br.com.lectek.copainsider.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name="roles", uniqueConstraints={@UniqueConstraint(name="uk_roles_nome", columnNames={"nome"})})
public class Role {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(name="nome", nullable=false, length=50)
    private String nome;
    @Column(name="descricao", length=255)
    private String descricao;
    @Column(name="created_at", updatable=false, insertable=false)
    private Instant createdAt;
    @Column(name="updated_at", insertable=false)
    private Instant updatedAt;

    protected Role() {
    }

    public Role(String nome) {
        this.setNome(nome);
    }

    public Role(String nome, String descricao) {
        this.setNome(nome);
        this.descricao = descricao;
    }

    public static Role of(String nome) {
        return new Role(nome);
    }

    public Long getId() {
        return this.id;
    }

    public String getNome() {
        return this.nome;
    }

    public void setNome(String nome) {
        String n;
        this.nome = nome == null ? null : ((n = nome.trim()).isEmpty() ? null : n.toUpperCase());
    }

    public String getDescricao() {
        return this.descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    @Transient
    public String getName() {
        return this.nome;
    }

    public void setName(String name) {
        this.setNome(name);
    }

    @Transient
    public boolean equalsIgnoreCase(String other) {
        return this.nome != null && other != null && this.nome.equalsIgnoreCase(other);
    }

    @Transient
    public boolean equalsIgnoreCaseCase(String other) {
        return this.equalsIgnoreCase(other);
    }

    @PrePersist
    @PreUpdate
    void normalize() {
        this.setNome(this.nome);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Role)) {
            return false;
        }
        Role other = (Role)o;
        return this.id != null && this.id.equals(other.id) || this.id == null && other.id == null && Objects.equals(this.nome, other.nome);
    }

    public int hashCode() {
        return this.id != null ? this.id.hashCode() : Objects.hash(this.nome);
    }

    public String toString() {
        return "Role{id=" + this.id + ", nome='" + this.nome + "'}";
    }
}

