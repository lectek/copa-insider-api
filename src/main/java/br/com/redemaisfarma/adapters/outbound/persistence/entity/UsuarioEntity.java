package br.com.redemaisfarma.adapters.outbound.persistence.entity;

import br.com.redemaisfarma.domain.user.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@NamedEntityGraph(
    name = "Usuario.roles",
    attributeNodes = { @NamedAttributeNode("roles") }
)
@Entity
@Table(
    name = "usuario",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_usuario_email", columnNames = {"email"}),
        @UniqueConstraint(name = "uk_usuario_cpf", columnNames = {"cpf"})
    },
    indexes = {
        @Index(name = "idx_usuario_email", columnList = "email"),
        @Index(name = "idx_usuario_cpf", columnList = "cpf")
    }
)
@EntityListeners(AuditingEntityListener.class)
public class UsuarioEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String nome;

    @NotBlank
    @Email
    @Size(max = 150)
    @Column(nullable = false, length = 150, unique = true)
    private String email;

    @NotBlank
    @Size(min = 11, max = 14)
    @Column(nullable = false, length = 14, unique = true)
    private String cpf;

    @NotBlank
    @Size(min = 8, max = 255)
    @Column(nullable = false, length = 255)
    private String senha;

    @Size(max = 25)
    @Column(length = 25)
    private String telefone;

    @Size(max = 200)
    @Column(length = 200)
    private String endereco;

    @Size(max = 255)
    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @ManyToMany(fetch = FetchType.EAGER, targetEntity = Role.class)
    @JoinTable(
        name = "usuario_roles",
        joinColumns = @JoinColumn(name = "usuario_id", foreignKey = @ForeignKey(name = "fk_usuario_roles_usuario")),
        inverseJoinColumns = @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "fk_usuario_roles_role"))
    )
    private Set<Role> roles = new HashSet<>();

    @Column(name = "ultimo_acesso")
    private LocalDateTime ultimoAcesso;

    @NotNull
    @Column(name = "cliente_vip", nullable = false)
    private Boolean clienteVip = Boolean.FALSE;

    @NotNull
    @Column(name = "tentativas_falhas", nullable = false)
    private Integer tentativasFalhas = 0;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public UsuarioEntity() {}

    public UsuarioEntity(
        Long id,
        String nome,
        String email,
        String cpf,
        String senha,
        String telefone,
        String endereco,
        String avatarUrl,
        Set<Role> roles,
        LocalDateTime ultimoAcesso,
        Boolean clienteVip,
        Integer tentativasFalhas,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long version
    ) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.senha = senha;
        this.telefone = telefone;
        this.endereco = endereco;
        this.avatarUrl = avatarUrl;
        this.roles = (roles != null) ? new HashSet<>(roles) : new HashSet<>();
        this.ultimoAcesso = ultimoAcesso;
        this.clienteVip = clienteVip;
        this.tentativasFalhas = tentativasFalhas;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = (nome != null) ? nome.trim() : null; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = (email != null) ? email.trim().toLowerCase() : null; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = normalizeCpf(cpf); }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = (telefone != null) ? telefone.trim() : null; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = (endereco != null) ? endereco.trim() : null; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = (avatarUrl != null) ? avatarUrl.trim() : null; }

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) {
        this.roles = (roles != null) ? new HashSet<>(roles) : new HashSet<>();
    }

    @Transient
    public Set<String> getRoleNames() {
        if (roles == null) return Set.of();
        return roles.stream()
            .filter(Objects::nonNull)
            .map(Role::getNome)
            .filter(Objects::nonNull)
            .map(String::toUpperCase)
            .collect(Collectors.toCollection(HashSet::new));
    }

    public void addRole(Role role) {
        if (role != null) {
            if (roles == null) roles = new HashSet<>();
            roles.add(role);
        }
    }

    public void removeRole(Role role) {
        if (role != null && roles != null) {
            roles.remove(role);
        }
    }

    public LocalDateTime getUltimoAcesso() { return ultimoAcesso; }
    public void setUltimoAcesso(LocalDateTime ultimoAcesso) { this.ultimoAcesso = ultimoAcesso; }

    public Boolean getClienteVip() { return clienteVip; }
    public void setClienteVip(Boolean clienteVip) { this.clienteVip = clienteVip; }

    public Integer getTentativasFalhas() { return tentativasFalhas; }
    public void setTentativasFalhas(Integer tentativasFalhas) { this.tentativasFalhas = tentativasFalhas; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = createdAt;
        if (tentativasFalhas == null) tentativasFalhas = 0;
        if (clienteVip == null) clienteVip = Boolean.FALSE;
        if (roles == null) roles = new HashSet<>();
        if (nome != null) nome = nome.trim();
        if (email != null) email = email.trim().toLowerCase();
        if (telefone != null) telefone = telefone.trim();
        if (endereco != null) endereco = endereco.trim();
        cpf = normalizeCpf(cpf);
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (roles == null) roles = new HashSet<>();
        if (nome != null) nome = nome.trim();
        if (email != null) email = email.trim().toLowerCase();
        if (telefone != null) telefone = telefone.trim();
        if (endereco != null) endereco = endereco.trim();
        cpf = normalizeCpf(cpf);
    }

    private static String normalizeCpf(String value) {
        if (value == null) return null;
        String digits = value.replaceAll("\\D", "");
        return digits.isBlank() ? null : digits;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UsuarioEntity)) return false;
        UsuarioEntity that = (UsuarioEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "UsuarioEntity{" +
            "id=" + id +
            ", nome='" + nome + '\'' +
            ", email='" + email + '\'' +
            ", cpf='" + cpf + '\'' +
            ", roles=" + getRoleNames() +
            ", telefone='" + telefone + '\'' +
            ", endereco='" + endereco + '\'' +
            ", avatarUrl='" + avatarUrl + '\'' +
            ", clienteVip=" + clienteVip +
            ", tentativasFalhas=" + tentativasFalhas +
            ", createdAt=" + createdAt +
            ", updatedAt=" + updatedAt +
            ", version=" + version +
            '}';
    }
}
