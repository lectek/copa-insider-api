/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  br.com.lectek.copainsider.domain.user.Role
 *  br.com.lectek.copainsider.domain.user.Usuario
 *  org.springframework.stereotype.Component
 */
package br.com.lectek.copainsider.adapters.outbound.persistence.mapper;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.lectek.copainsider.domain.user.Role;
import br.com.lectek.copainsider.domain.user.Usuario;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {
    private static final ZoneId ZONE = ZoneId.systemDefault();

    public Usuario toDomain(UsuarioEntity e) {
        if (e == null) {
            return null;
        }
        Usuario u = new Usuario(e.getNome(), e.getEmail(), e.getCpf(), e.getSenha());
        u.setId(e.getId());
        u.setRoles(this.toRoleNames(e.getRoles()));
        u.setClienteVip(Boolean.TRUE.equals(e.getClienteVip()));
        u.setTentativasFalhas(e.getTentativasFalhas() == null ? 0 : e.getTentativasFalhas());
        u.setUltimoAcesso(e.getUltimoAcesso() != null ? e.getUltimoAcesso().atZone(ZONE).toInstant() : null);
        return u;
    }

    public UsuarioEntity toEntity(Usuario u) {
        if (u == null) {
            return null;
        }
        UsuarioEntity e = new UsuarioEntity();
        e.setId(u.getId());
        e.setNome(u.getNome());
        e.setEmail(u.getEmail());
        e.setCpf(u.getCpf());
        e.setSenha(u.getSenha());
        e.setRoles(this.toRoleEntities(u.getRoles()));
        e.setClienteVip(u.isClienteVip());
        e.setTentativasFalhas(u.getTentativasFalhas());
        e.setUltimoAcesso(u.getUltimoAcesso() != null ? LocalDateTime.ofInstant(u.getUltimoAcesso(), ZONE) : null);
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

