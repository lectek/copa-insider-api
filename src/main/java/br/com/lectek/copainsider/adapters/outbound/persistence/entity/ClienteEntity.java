/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.persistence.Column
 *  jakarta.persistence.Entity
 *  jakarta.persistence.EntityListeners
 *  jakarta.persistence.GeneratedValue
 *  jakarta.persistence.GenerationType
 *  jakarta.persistence.Id
 *  jakarta.persistence.Index
 *  jakarta.persistence.PrePersist
 *  jakarta.persistence.PreUpdate
 *  jakarta.persistence.Table
 *  jakarta.persistence.Version
 *  jakarta.validation.constraints.Email
 *  jakarta.validation.constraints.Email$List
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.NotBlank$List
 *  jakarta.validation.constraints.Size
 *  jakarta.validation.constraints.Size$List
 *  org.springframework.data.annotation.CreatedDate
 *  org.springframework.data.annotation.LastModifiedDate
 *  org.springframework.data.jpa.domain.support.AuditingEntityListener
 */
package br.com.lectek.copainsider.adapters.outbound.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name="cliente", indexes={@Index(name="uk_cliente_email", columnList="email", unique=true), @Index(name="uk_cliente_cpf", columnList="cpf", unique=true)})
@EntityListeners(value={AuditingEntityListener.class})
public class ClienteEntity
implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message="O nome \u00e9 obrigat\u00f3rio")
@NotBlank(message="O nome \u00e9 obrigat\u00f3rio")
    @Size(max=100, message="O nome pode ter no m\u00e1ximo 100 caracteres")
@Size(max=100, message="O nome pode ter no m\u00e1ximo 100 caracteres")
    @Column(nullable=false, length=100)
    private @NotBlank(message="O nome \u00e9 obrigat\u00f3rio")
@NotBlank(message="O nome \u00e9 obrigat\u00f3rio") @Size(max=100, message="O nome pode ter no m\u00e1ximo 100 caracteres")
@Size(max=100, message="O nome pode ter no m\u00e1ximo 100 caracteres") String nome;
    @NotBlank(message="O e-mail \u00e9 obrigat\u00f3rio")
@NotBlank(message="O e-mail \u00e9 obrigat\u00f3rio")
    @Email(message="Informe um e-mail v\u00e1lido")
@Email(message="Informe um e-mail v\u00e1lido")
    @Size(max=150, message="O e-mail pode ter no m\u00e1ximo 150 caracteres")
@Size(max=150, message="O e-mail pode ter no m\u00e1ximo 150 caracteres")
    @Column(unique=true, nullable=false, length=150)
    private @NotBlank(message="O e-mail \u00e9 obrigat\u00f3rio")
@NotBlank(message="O e-mail \u00e9 obrigat\u00f3rio") @Email(message="Informe um e-mail v\u00e1lido")
@Email(message="Informe um e-mail v\u00e1lido") @Size(max=150, message="O e-mail pode ter no m\u00e1ximo 150 caracteres")
@Size(max=150, message="O e-mail pode ter no m\u00e1ximo 150 caracteres") String email;
    @Size(max=25, message="O telefone pode ter no m\u00e1ximo 25 caracteres")
@Size(max=25, message="O telefone pode ter no m\u00e1ximo 25 caracteres")
    @Column(length=25)
    private @Size(max=25, message="O telefone pode ter no m\u00e1ximo 25 caracteres")
@Size(max=25, message="O telefone pode ter no m\u00e1ximo 25 caracteres") String telefone;
    @NotBlank(message="O CPF \u00e9 obrigat\u00f3rio")
@NotBlank(message="O CPF \u00e9 obrigat\u00f3rio")
    @Size(min=11, max=14, message="O CPF deve ter entre 11 e 14 caracteres")
@Size(min=11, max=14, message="O CPF deve ter entre 11 e 14 caracteres")
    @Column(unique=true, nullable=false, length=14)
    private @NotBlank(message="O CPF \u00e9 obrigat\u00f3rio")
@NotBlank(message="O CPF \u00e9 obrigat\u00f3rio") @Size(min=11, max=14, message="O CPF deve ter entre 11 e 14 caracteres")
@Size(min=11, max=14, message="O CPF deve ter entre 11 e 14 caracteres") String cpf;
    @NotBlank(message="A senha \u00e9 obrigat\u00f3ria")
@NotBlank(message="A senha \u00e9 obrigat\u00f3ria")
    @Size(min=8, max=255, message="A senha deve ter entre 8 e 255 caracteres")
@Size(min=8, max=255, message="A senha deve ter entre 8 e 255 caracteres")
    @Column(nullable=false, length=255)
    private @NotBlank(message="A senha \u00e9 obrigat\u00f3ria")
@NotBlank(message="A senha \u00e9 obrigat\u00f3ria") @Size(min=8, max=255, message="A senha deve ter entre 8 e 255 caracteres")
@Size(min=8, max=255, message="A senha deve ter entre 8 e 255 caracteres") String senha;
    @Column(name="data_nascimento")
    private LocalDate dataDeNascimento;
    @Column(nullable=false)
    private boolean ativo = true;
    @CreatedDate
    @Column(name="created_at", updatable=false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    @Column(name="updated_at")
    private LocalDateTime updatedAt;
    @Version
    @Column(nullable=false)
    private Long version;

    public ClienteEntity() {
    }

    public ClienteEntity(Long id, String nome, String email, String telefone, String cpf, String senha, LocalDate dataDeNascimento, boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.cpf = cpf;
        this.senha = senha;
        this.dataDeNascimento = dataDeNascimento;
        this.ativo = ativo;
    }

    @PrePersist
    protected void onPrePersist() {
        if (this.email != null) {
            this.email = this.email.trim().toLowerCase();
        }
        if (this.version == null) {
            this.version = 0L;
        }
    }

    @PreUpdate
    protected void onPreUpdate() {
        if (this.email != null) {
            this.email = this.email.trim().toLowerCase();
        }
    }

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

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return this.telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getCpf() {
        return this.cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getSenha() {
        return this.senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public LocalDate getDataDeNascimento() {
        return this.dataDeNascimento;
    }

    public void setDataDeNascimento(LocalDate dataDeNascimento) {
        this.dataDeNascimento = dataDeNascimento;
    }

    public boolean isAtivo() {
        return this.ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return this.version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClienteEntity)) {
            return false;
        }
        ClienteEntity that = (ClienteEntity)o;
        return this.id != null && this.id.equals(that.id);
    }

    public int hashCode() {
        return Objects.hash(this.id);
    }

    public String toString() {
        return "ClienteEntity{id=" + String.valueOf(this.id) + ", nome='" + this.nome + "', email='" + this.email + "', telefone='" + this.telefone + "', cpf='" + this.cpf + "', dataDeNascimento=" + String.valueOf(this.dataDeNascimento) + ", ativo=" + this.ativo + ", version=" + String.valueOf(this.version) + "}";
    }
}

