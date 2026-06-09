package br.com.lectek.copainsider.adapters.inbound.web.security;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.lectek.copainsider.domain.user.Role;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class DbUserDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarios;

    public DbUserDetailsService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (!StringUtils.hasText(username)) {
            throw new UsernameNotFoundException("Usuario vazio.");
        }

        String ident = username.trim();
        Optional<UsuarioEntity> opt;
        if (ident.contains("@")) {
            opt = usuarios.findByEmailIgnoreCase(ident.toLowerCase(Locale.ROOT));
        } else {
            String digits = ident.replaceAll("\\D", "");
            if (digits.length() == 11) {
                opt = usuarios.findByCpf(digits);
            } else {
                opt = usuarios.findByEmailOrCpf(ident);
            }
        }

        UsuarioEntity user = opt.orElseThrow(() ->
                new UsernameNotFoundException("Usuario nao encontrado."));

        Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(Role::getNome)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(DbUserDetailsService::normalizeRole)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        if (authorities.isEmpty()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        boolean locked = user.getTentativasFalhas() != null && user.getTentativasFalhas() >= 5;

        return User.withUsername(user.getEmail())
                .password(user.getSenha())
                .authorities(authorities)
                .accountLocked(locked)
                .build();
    }

    private static String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "ROLE_USER";
        }
        String trimmed = role.trim().toUpperCase(Locale.ROOT);
        return trimmed.startsWith("ROLE_") ? trimmed : "ROLE_" + trimmed;
    }
}
