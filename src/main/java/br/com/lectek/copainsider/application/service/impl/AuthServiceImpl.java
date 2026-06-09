package br.com.lectek.copainsider.application.service.impl;

import br.com.lectek.copainsider.adapters.outbound.auth.jwt.model.TokenPair;
import br.com.lectek.copainsider.adapters.outbound.auth.service.AuthTokenOperations;
import br.com.lectek.copainsider.application.core.exception.InvalidCredentialsException;
import br.com.lectek.copainsider.application.dto.request.LoginRequest;
import br.com.lectek.copainsider.application.dto.response.AuthResponse;
import br.com.lectek.copainsider.application.dto.response.LoginResponseDTO;
import br.com.lectek.copainsider.application.port.outbound.AuthRepositoryPort;
import br.com.lectek.copainsider.application.service.AuthService;
import br.com.lectek.copainsider.domain.user.Usuario;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {
    private static final String DEFAULT_TENANT_ID = "rede-mais-farma";

    private final AuthRepositoryPort authRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenOperations tokenOperations;

    public AuthServiceImpl(
            @Qualifier("authRepositoryAdapter") AuthRepositoryPort authRepository,
            PasswordEncoder passwordEncoder,
            ObjectProvider<AuthTokenOperations> tokenOperationsProvider
    ) {
        this.authRepository = authRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenOperations = tokenOperationsProvider.getIfAvailable();
    }

    @Override
    public boolean isBlocked(String identifier) {
        return authRepository.isBlocked(identifier);
    }

    @Override
    public void registerFailedAttempt(String identifier) {
        authRepository.registerFailedAttempt(identifier);
    }

    @Override
    public AuthResponse authenticate(String identifier, String rawPassword) {
        Usuario usuario = authRepository.authenticate(identifier, rawPassword);
        List<String> roles = toRoleNames(usuario.getRoles());
        TokenBundle tokens = issueTokens(usuario.getId(), usuario.getEmail(), roles, DEFAULT_TENANT_ID);
        return new AuthResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                usuario.getId(),
                usuario.getEmail(),
                roles,
                tokens.expiresAt(),
                DEFAULT_TENANT_ID,
                UUID.randomUUID(),
                tokens.issuedAt()
        );
    }

    @Override
    public boolean isClienteVip(String identifier) {
        return authRepository.isClienteVip(identifier);
    }

    @Override
    public String getIdentificador(Usuario usuario) {
        return usuario.getEmail();
    }

    @Override
    public String getPassword(Usuario usuario) {
        return usuario.getSenha();
    }

    @Override
    public String getEmail(Usuario usuario) {
        return usuario.getEmail();
    }

    @Override
    public LoginResponseDTO login(LoginRequest request) {
        final String identifier = request.getUsuario().trim();

        if (isBlocked(identifier)) {
            throw new InvalidCredentialsException("Usuário temporariamente bloqueado por tentativas inválidas.");
        }

        final Optional<Usuario> optUser = authRepository.findByIdentifier(identifier);
        if (optUser.isEmpty()) {
            registerFailedAttempt(identifier);
            throw new InvalidCredentialsException("Credenciais inválidas.");
        }

        final Usuario user = optUser.get();
        if (user.getSenha() == null || !passwordEncoder.matches(request.getSenha(), user.getSenha())) {
            registerFailedAttempt(identifier);
            throw new InvalidCredentialsException("Credenciais inválidas.");
        }

        authRepository.resetFailedAttempts(identifier);
        authRepository.updateLastAccess(user.getId());

        final List<String> roles = toRoleNames(user.getRoles());
        final String tenantId = request.getTenantId() == null || request.getTenantId().isBlank()
                ? DEFAULT_TENANT_ID
                : request.getTenantId();
        final TokenBundle tokens = issueTokens(user.getId(), user.getEmail(), roles, tenantId);

        final LoginResponseDTO dto = new LoginResponseDTO();
        dto.setAccessToken(tokens.accessToken());
        dto.setRefreshToken(tokens.refreshToken());
        dto.setUserType(resolveUserType(user.getRoles()));
        dto.setUserId(user.getId());
        dto.setFullName(user.getNome());
        dto.setEmail(user.getEmail());
        dto.setPermissions(roles);
        dto.setExpiresAt(tokens.expiresAt());
        dto.setLastLoginAt(tokens.issuedAt());
        dto.setAccountStatus(LoginResponseDTO.AccountStatus.ACTIVE);
        dto.setWelcomeMessage(buildWelcome(user));
        dto.setTenantId(tenantId);
        dto.setTraceId(UUID.randomUUID());
        return dto;
    }

    private TokenBundle issueTokens(
            final Long userId,
            final String username,
            final List<String> roles,
            final String tenantId
    ) {
        if (tokenOperations != null) {
            try {
                TokenPair pair = tokenOperations.issueTokens(
                        userId,
                        username,
                        tenantId,
                        roles,
                        null,
                        null
                );
                return new TokenBundle(
                        pair.getAccessToken(),
                        pair.getRefreshToken(),
                        toLocalDateTime(pair.getIssuedAt()),
                        toLocalDateTime(pair.getExpiresAt())
                );
            } catch (IllegalStateException ignored) {
                // Falls back to opaque tokens when JWT support is disabled in the active profile.
            }
        }

        LocalDateTime now = LocalDateTime.now();
        return new TokenBundle(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                now,
                now.plusHours(1)
        );
    }

    private LocalDateTime toLocalDateTime(final Instant value) {
        return value == null
                ? LocalDateTime.now().plusHours(1)
                : LocalDateTime.ofInstant(value, TimeZone.getDefault().toZoneId());
    }

    private record TokenBundle(
            String accessToken,
            String refreshToken,
            LocalDateTime issuedAt,
            LocalDateTime expiresAt
    ) {
    }

    private LoginResponseDTO.UserType resolveUserType(Set<String> roles) {
        if (roles != null) {
            if (roles.stream().anyMatch(r -> r.equalsIgnoreCase("ROLE_ADMIN") || r.equalsIgnoreCase("ADMIN"))) {
                return LoginResponseDTO.UserType.ADMIN;
            }
            if (roles.stream().anyMatch(r -> r.equalsIgnoreCase("ROLE_ATENDENTE") || r.equalsIgnoreCase("ATENDENTE"))) {
                return LoginResponseDTO.UserType.ATENDENTE;
            }
        }
        return LoginResponseDTO.UserType.CLIENTE;
    }

    private String buildWelcome(Usuario u) {
        return "Bem-vindo à CopaInsider, " + (u.getNome() != null ? u.getNome() : "usuário") + "!";
    }

    private List<String> toRoleNames(Set<String> roles) {
        if (roles == null || roles.isEmpty()) return List.of("ROLE_USER");
        return roles.stream()
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .collect(Collectors.toList());
    }
}
