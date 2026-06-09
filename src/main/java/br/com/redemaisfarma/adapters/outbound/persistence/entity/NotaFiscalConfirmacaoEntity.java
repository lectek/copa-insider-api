/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.persistence.Basic
 *  jakarta.persistence.Column
 *  jakarta.persistence.Entity
 *  jakarta.persistence.FetchType
 *  jakarta.persistence.GeneratedValue
 *  jakarta.persistence.GenerationType
 *  jakarta.persistence.Id
 *  jakarta.persistence.Lob
 *  jakarta.persistence.PrePersist
 *  jakarta.persistence.Table
 *  org.hibernate.annotations.JdbcTypeCode
 */
package br.com.redemaisfarma.adapters.outbound.persistence.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@Table(name="nota_fiscal_confirmacao")
public class NotaFiscalConfirmacaoEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, length=150)
    private String nome;
    @Column(nullable=false, length=50)
    private String preferencia;
    @Column(nullable=false, length=180)
    private String email;
    @Lob
    @Basic(fetch=FetchType.LAZY)
    @JdbcTypeCode(value=-4)
    @Column(name="pdf", nullable=false, columnDefinition="LONGBLOB")
    private byte[] pdf;
    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt;

    @PrePersist
    void pre() {
        this.createdAt = LocalDateTime.now();
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

    public String getPreferencia() {
        return this.preferencia;
    }

    public void setPreferencia(String preferencia) {
        this.preferencia = preferencia;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public byte[] getPdf() {
        return this.pdf;
    }

    public void setPdf(byte[] pdf) {
        this.pdf = pdf;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

