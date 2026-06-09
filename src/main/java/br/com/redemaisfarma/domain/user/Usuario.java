package br.com.redemaisfarma.domain.user;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Usuario {
    private Long id;
    private String nome;
    private String email;
    private String cpf;
    private String senha;
    private Set<String> roles = new HashSet<>();
    private boolean clienteVip;
    private int tentativasFalhas;
    private Instant ultimoAcesso;

    public Usuario() { }

    public Usuario(String nome, String email, String cpf, String senha) {
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.senha = senha;
    }

    public Usuario(Long id, String nome, String email, String cpf, String senha,
                   Set<String> roles, boolean clienteVip) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.cpf = cpf;
        this.senha = senha;
        setRoles(roles);
        this.clienteVip = clienteVip;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) {
        this.roles = (roles != null) ? new HashSet<>(roles) : new HashSet<>();
    }

    public boolean isClienteVip() { return clienteVip; }
    public void setClienteVip(boolean clienteVip) { this.clienteVip = clienteVip; }

    public int getTentativasFalhas() { return tentativasFalhas; }
    public void setTentativasFalhas(int tentativasFalhas) { this.tentativasFalhas = tentativasFalhas; }

    public Instant getUltimoAcesso() { return ultimoAcesso; }
    public void setUltimoAcesso(Instant ultimoAcesso) { this.ultimoAcesso = ultimoAcesso; }

    public void addRole(String role) {
        if (role == null || role.isBlank()) return;
        String r = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        roles.add(r);
    }

    public void removeRole(String role) {
        if (role == null) return;
        roles.removeIf(r -> r.equalsIgnoreCase(role) || r.equalsIgnoreCase("ROLE_" + role));
    }

    public boolean hasRole(String role) {
        if (role == null) return false;
        String rWith = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        String rNo = role.startsWith("ROLE_") ? role.substring(5) : role;
        return roles.stream().anyMatch(r -> r.equalsIgnoreCase(rWith) || r.equalsIgnoreCase(rNo));
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override public int hashCode() { return Objects.hash(id); }

    @Override public String toString() { return "Usuario{id=%d, email=%s}".formatted(id, email); }
}
