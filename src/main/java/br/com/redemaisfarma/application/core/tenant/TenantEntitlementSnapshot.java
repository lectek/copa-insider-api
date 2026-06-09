package br.com.redemaisfarma.application.core.tenant;

import java.util.List;

public record TenantEntitlementSnapshot(
        Long tenantId,
        String tenantCode,
        String planCode,
        List<String> featureKeys,
        List<String> addonCodes
) {
}

