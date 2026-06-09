package br.com.lectek.copainsider.adapters.inbound.web.controller.admin;

import br.com.lectek.copainsider.application.core.user.RoleAdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
public class AdminUserRoleController {

    /**
     * Service responsible for role elevation/removal.
     */
    private final RoleAdminService service;

    /**
     * Creates controller with required role service.
     *
     * @param roleAdminService role admin service
     */
    public AdminUserRoleController(final RoleAdminService roleAdminService) {
        this.service = roleAdminService;
    }

    /**
     * Grants admin role to a user.
     *
     * @param userId user id
     * @param request current request
     * @return empty 200 response
     */
    @PostMapping("/{userId}/grant-admin")
    @PreAuthorize("hasRole('DEVELOPER')")
    public ResponseEntity<Void> grantAdmin(
            @PathVariable("userId") final Long userId,
            final HttpServletRequest request
    ) {
        this.service.grantAdmin(userId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * Revokes admin role from a user.
     *
     * @param userId user id
     * @param request current request
     * @return empty 200 response
     */
    @DeleteMapping("/{userId}/revoke-admin")
    @PreAuthorize("hasRole('DEVELOPER')")
    public ResponseEntity<Void> revokeAdmin(
            @PathVariable("userId") final Long userId,
            final HttpServletRequest request
    ) {
        this.service.revokeAdmin(userId, request);
        return ResponseEntity.ok().build();
    }
}
