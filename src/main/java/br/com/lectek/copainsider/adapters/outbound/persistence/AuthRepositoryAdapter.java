/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  br.com.lectek.copainsider.application.port.outbound.AuthRepositoryPort
 *  br.com.lectek.copainsider.domain.user.Role
 *  br.com.lectek.copainsider.domain.user.Usuario
 *  org.springframework.beans.factory.annotation.Value
 *  org.springframework.security.crypto.password.PasswordEncoder
 *  org.springframework.stereotype.Repository
 *  org.springframework.transaction.annotation.Transactional
 */
package br.com.lectek.copainsider.adapters.outbound.persistence;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.lectek.copainsider.application.port.outbound.AuthRepositoryPort;
import br.com.lectek.copainsider.domain.user.Role;
import br.com.lectek.copainsider.domain.user.Usuario;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class AuthRepositoryAdapter
implements AuthRepositoryPort {
    private final int MAX_TENTATIVAS;
    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public AuthRepositoryAdapter(UsuarioRepository repo, PasswordEncoder encoder, @Value(value="${auth.max-tentativas:5}") int maxTentativas) {
        this.repo = repo;
        this.encoder = encoder;
        this.MAX_TENTATIVAS = maxTentativas;
    }

    @Transactional(readOnly=true)
    public Optional<Usuario> findByIdentifier(String identifier) {
        return this.findEntityByIdentifier(identifier).map(this::toDomain);
    }

    @Transactional(readOnly=true)
    public boolean isBlocked(String identifier) {
        return this.findEntityByIdentifier(identifier).map(u -> u.getTentativasFalhas() != null && u.getTentativasFalhas() >= this.MAX_TENTATIVAS).orElse(false);
    }

    @Transactional(readOnly=true)
    public boolean existsByEmail(String email) {
        return this.repo.existsByEmailIgnoreCase(this.normalizeEmail(email));
    }

    @Transactional(readOnly=true)
    public boolean isClienteVip(String identifier) {
        return this.findEntityByIdentifier(identifier).map(u -> Boolean.TRUE.equals(u.getClienteVip())).orElse(false);
    }

    @Transactional
    public void registerFailedAttempt(String identifier) {
        this.findEntityByIdentifier(identifier).ifPresent(u -> {
            int atual = u.getTentativasFalhas() == null ? 0 : u.getTentativasFalhas();
            u.setTentativasFalhas(atual + 1);
            this.repo.save(u);
        });
    }

    @Transactional
    public void resetFailedAttempts(String identifier) {
        this.findEntityByIdentifier(identifier).ifPresent(u -> {
            u.setTentativasFalhas(0);
            this.repo.save(u);
        });
    }

    @Transactional
    public boolean updateLastAccess(Long userId) {
        return this.repo.findById(userId).map(u -> {
            u.setUltimoAcesso(this.now());
            this.repo.save(u);
            return true;
        }).orElse(false);
    }

    @Transactional
    public void save(Usuario user) {
        UsuarioEntity e = this.toEntity(user);
        e.setEmail(this.normalizeEmail(e.getEmail()));
        e.setCpf(this.normalizeCpf(e.getCpf()));
        if (!this.isEncoded(e.getSenha())) {
            e.setSenha(this.encoder.encode((CharSequence)e.getSenha()));
        }
        if (e.getRoles() == null || e.getRoles().isEmpty()) {
            e.setRoles(this.toRoleEntities(Set.of("ROLE_USER")));
        }
        if (e.getTentativasFalhas() == null) {
            e.setTentativasFalhas(0);
        }
        if (e.getClienteVip() == null) {
            e.setClienteVip(Boolean.FALSE);
        }
        this.repo.save(e);
    }

    @Transactional
    public Usuario authenticate(String identifier, String password) {
        UsuarioEntity e = this.findEntityByIdentifier(identifier).orElseThrow(() -> new NoSuchElementException("Usu\u00e1rio n\u00e3o encontrado"));
        if (e.getTentativasFalhas() != null && e.getTentativasFalhas() >= this.MAX_TENTATIVAS) {
            throw new IllegalStateException("Usu\u00e1rio bloqueado por tentativas inv\u00e1lidas");
        }
        if (!this.encoder.matches((CharSequence)password, e.getSenha())) {
            int novo = (e.getTentativasFalhas() == null ? 0 : e.getTentativasFalhas()) + 1;
            e.setTentativasFalhas(novo);
            this.repo.save(e);
            throw new IllegalArgumentException("Senha inv\u00e1lida");
        }
        e.setTentativasFalhas(0);
        e.setUltimoAcesso(this.now());
        this.repo.save(e);
        return this.toDomain(e);
    }

    @Transactional(readOnly=true)
    protected Optional<UsuarioEntity> findEntityByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }
        String ident = identifier.trim();
        if (ident.contains("@")) {
            return this.repo.findByEmailIgnoreCase(this.normalizeEmail(ident));
        }
        String digits = this.normalizeCpf(ident);
        if (digits.length() == 11) {
            return this.repo.findByCpf(digits);
        }
        return this.repo.findByEmailOrCpf(ident);
    }

    private LocalDateTime now() {
        return LocalDateTime.now();
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String normalizeCpf(String cpf) {
        return cpf == null ? null : cpf.replaceAll("\\D", "");
    }

    private boolean isEncoded(String rawOrEncoded) {
        if (rawOrEncoded == null) {
            return false;
        }
        return rawOrEncoded.startsWith("$2a$") || rawOrEncoded.startsWith("$2b$") || rawOrEncoded.startsWith("$2y$");
    }

    private Usuario toDomain(UsuarioEntity e) {
        Set<String> roleNames = this.toRoleNames(e.getRoles());
        return new Usuario(e.getId(), e.getNome(), e.getEmail(), e.getCpf(), e.getSenha(), roleNames, Boolean.TRUE.equals(e.getClienteVip()));
    }

    private UsuarioEntity toEntity(Usuario d) {
        UsuarioEntity e = new UsuarioEntity();
        e.setId(d.getId());
        e.setNome(d.getNome());
        e.setEmail(d.getEmail());
        e.setCpf(d.getCpf());
        e.setSenha(d.getSenha());
        e.setClienteVip(d.isClienteVip());
        e.setRoles(this.toRoleEntities(d.getRoles()));
        return e;
    }

    private Set<String> toRoleNames(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }
        return roles.stream().filter(Objects::nonNull).map(Role::getNome).filter(Objects::nonNull).map(String::trim).filter(s -> !s.isEmpty()).map(String::toUpperCase).collect(Collectors.toCollection(HashSet::new));
    }

    private Set<Role> toRoleEntities(Set<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return new HashSet<Role>();
        }
        return roleNames.stream().filter(Objects::nonNull).map(String::trim).filter(s -> !s.isEmpty()).map(String::toUpperCase).map(Role::of).collect(Collectors.toCollection(HashSet::new));
    }
}

