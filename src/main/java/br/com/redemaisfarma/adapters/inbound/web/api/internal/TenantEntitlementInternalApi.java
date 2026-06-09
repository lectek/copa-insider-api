package br.com.redemaisfarma.adapters.inbound.web.api.internal;

import br.com.redemaisfarma.application.core.tenant.TenantEntitlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/tenant")
@Tag(name = "Tenant Entitlements (Interno)")
public class TenantEntitlementInternalApi {

    private final TenantEntitlementService tenantEntitlementService;

    public TenantEntitlementInternalApi(final TenantEntitlementService tenantEntitlementServiceValue) {
        this.tenantEntitlementService = tenantEntitlementServiceValue;
    }

    @GetMapping("/{tenantId}/entitlements")
    @Operation(summary = "Consultar features ativas por tenant")
    public ResponseEntity<?> findEntitlements(@PathVariable("tenantId") final String tenantId) {
        return this.tenantEntitlementService.resolveByTenantIdentifier(tenantId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

