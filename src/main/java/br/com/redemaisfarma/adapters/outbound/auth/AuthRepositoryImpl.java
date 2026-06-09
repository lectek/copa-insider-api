/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  br.com.redemaisfarma.application.core.exception.InvalidCredentialsException
 *  br.com.redemaisfarma.application.core.exception.UserBlockedException
 *  br.com.redemaisfarma.application.port.outbound.AuthRepositoryPort
 *  br.com.redemaisfarma.domain.user.Usuario
 *  org.springframework.security.crypto.password.PasswordEncoder
 *  org.springframework.stereotype.Repository
 *  org.springframework.transaction.annotation.Transactional
 */
package br.com.redemaisfarma.adapters.outbound.auth;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.UsuarioJpaRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.mapper.UsuarioMapper;
import br.com.redemaisfarma.application.core.exception.InvalidCredentialsException;
import br.com.redemaisfarma.application.core.exception.UserBlockedException;
import br.com.redemaisfarma.application.port.outbound.AuthRepositoryPort;
import br.com.redemaisfarma.domain.user.Usuario;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository(value="authRepositoryImpl")
public class AuthRepositoryImpl
implements AuthRepositoryPort {
    private final UsuarioJpaRepository usuarioJpaRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthRepositoryImpl(UsuarioJpaRepository usuarioJpaRepository, UsuarioMapper usuarioMapper, PasswordEncoder passwordEncoder) {
        this.usuarioJpaRepository = usuarioJpaRepository;
        this.usuarioMapper = usuarioMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly=true)
    public Optional<Usuario> findByIdentifier(String identifier) {
        String norm = this.normalize(identifier);
        return this.usuarioJpaRepository.findByEmailOrCpf(norm).map(entity -> this.usuarioMapper.toDomain((UsuarioEntity)entity));
    }

    @Transactional(readOnly=true)
    public boolean isBlocked(String identifier) {
        String norm = this.normalize(identifier);
        Boolean blocked = this.usuarioJpaRepository.isBlockedByEmailOrCpf(norm);
        return blocked != null && blocked != false;
    }

    @Transactional
    public void registerFailedAttempt(String identifier) {
        this.usuarioJpaRepository.registrarTentativaFalha(this.normalize(identifier));
    }

    @Transactional
    public void resetFailedAttempts(String identifier) {
        this.usuarioJpaRepository.resetarTentativasFalhas(this.normalize(identifier));
    }

    @Transactional
    public boolean updateLastAccess(Long userId) {
        return this.usuarioJpaRepository.updateUltimoAcesso(userId, LocalDateTime.now()) > 0;
    }

    @Transactional
    public void save(Usuario user) {
        UsuarioEntity entity = this.usuarioMapper.toEntity(user);
        entity.setEmail(this.normalize(entity.getEmail()));
        entity.setCpf(this.normalizeCpf(entity.getCpf()));
        if (entity.getSenha() != null && !this.isEncoded(entity.getSenha())) {
            entity.setSenha(this.passwordEncoder.encode((CharSequence)entity.getSenha()));
        }
        this.usuarioJpaRepository.save(entity);
    }

    @Transactional(readOnly=true)
    public boolean existsByEmail(String email) {
        return this.usuarioJpaRepository.existsByEmail(this.normalize(email));
    }

    @Transactional
    public Usuario authenticate(String identifier, String rawPassword) {
        String norm = this.normalize(identifier);
        UsuarioEntity entity = this.usuarioJpaRepository.findByEmailOrCpf(norm).orElseThrow(InvalidCredentialsException::new);
        if (Boolean.TRUE.equals(this.usuarioJpaRepository.isBlockedByEmailOrCpf(norm))) {
            throw new UserBlockedException();
        }
        if (!this.passwordEncoder.matches((CharSequence)rawPassword, entity.getSenha())) {
            this.usuarioJpaRepository.registrarTentativaFalha(norm);
            throw new InvalidCredentialsException();
        }
        this.usuarioJpaRepository.resetarTentativasFalhas(norm);
        this.usuarioJpaRepository.updateUltimoAcesso(entity.getId(), LocalDateTime.now());
        return this.usuarioMapper.toDomain(entity);
    }

    @Transactional(readOnly=true)
    public boolean isClienteVip(String identifier) {
        return this.usuarioJpaRepository.verificarClienteVip(this.normalize(identifier)).orElse(Boolean.FALSE);
    }

    private String normalize(String identifier) {
        if (identifier == null) {
            return null;
        }
        String id = identifier.trim();
        return id.contains("@") ? id.toLowerCase() : id.replaceAll("\\D", "");
    }

    private String normalizeCpf(String cpf) {
        return cpf == null ? null : cpf.replaceAll("\\D", "");
    }

    private boolean isEncoded(String s) {
        if (s == null) {
            return false;
        }
        return s.startsWith("$2a$") || s.startsWith("$2b$") || s.startsWith("$2y$");
    }
}

