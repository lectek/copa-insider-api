/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.servlet.http.HttpServletRequest
 *  org.springframework.security.core.Authentication
 *  org.springframework.security.core.context.SecurityContextHolder
 *  org.springframework.stereotype.Service
 *  org.springframework.transaction.annotation.Transactional
 */
package br.com.redemaisfarma.application.core.user;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.redemaisfarma.domain.user.Role;
import br.com.redemaisfarma.user.audit.RoleAudit;
import br.com.redemaisfarma.user.audit.RoleAuditRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleAdminService {
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private final UsuarioRepository usuarios;
    private final RoleAuditRepository auditRepo;

    public RoleAdminService(UsuarioRepository usuarios, RoleAuditRepository auditRepo) {
        this.usuarios = usuarios;
        this.auditRepo = auditRepo;
    }

    @Transactional
    public void grantAdmin(Long userId, HttpServletRequest req) {
        UsuarioEntity u = (UsuarioEntity)this.usuarios.findById(userId).orElseThrow(() -> new IllegalArgumentException("Usu\u00e1rio n\u00e3o encontrado"));
        boolean already = u.getRoles().stream().anyMatch(r -> ROLE_ADMIN.equalsIgnoreCase(r.getName()));
        if (!already) {
            u.addRole(Role.of(ROLE_ADMIN));
            this.usuarios.save(u);
            this.audit("GRANT", userId, ROLE_ADMIN, req);
        }
    }

    @Transactional
    public void revokeAdmin(Long userId, HttpServletRequest req) {
        UsuarioEntity u = (UsuarioEntity)this.usuarios.findById(userId).orElseThrow(() -> new IllegalArgumentException("Usu\u00e1rio n\u00e3o encontrado"));
        String current = this.currentUserEmail();
        if (current != null && current.equalsIgnoreCase(u.getEmail())) {
            throw new IllegalStateException("Voc\u00ea n\u00e3o pode remover o pr\u00f3prio ADMIN.");
        }
        boolean removed = u.getRoles().removeIf(r -> ROLE_ADMIN.equalsIgnoreCase(r.getName()));
        if (removed) {
            this.usuarios.save(u);
            this.audit("REVOKE", userId, ROLE_ADMIN, req);
        }
    }

    private void audit(String action, Long targetUserId, String roleName, HttpServletRequest req) {
        RoleAudit a = new RoleAudit();
        a.setAction(action);
        a.setTargetUserId(targetUserId);
        a.setRoleName(roleName);
        a.setActorEmail(this.currentUserEmail());
        a.setIp(this.remoteIp(req));
        a.setUserAgent(req != null ? req.getHeader("User-Agent") : null);
        a.setCreatedAt(Instant.now());
        this.auditRepo.save(a);
    }

    private String currentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    private String remoteIp(HttpServletRequest req) {
        if (req == null) {
            return null;
        }
        String ip = req.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }
}

