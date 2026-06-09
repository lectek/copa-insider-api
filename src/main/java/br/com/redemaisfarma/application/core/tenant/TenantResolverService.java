package br.com.redemaisfarma.application.core.tenant;

import br.com.redemaisfarma.adapters.outbound.persistence.repository.TenantRepository;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TenantResolverService {

    public static final String DEFAULT_TENANT_CODE = "default";
    private static final String HOST_MAPPING_PREFIX = "tenant.host.";

    private final TenantRepository tenantRepository;
    private final AppSettingService appSettingService;
    private volatile Long cachedDefaultTenantId;

    @Transactional(readOnly = true)
    public Long resolveDefaultTenantId() {
        Long cached = this.cachedDefaultTenantId;
        if (cached != null) {
            return cached;
        }
        Long resolved = this.tenantRepository.findByCodigo(DEFAULT_TENANT_CODE)
                .map(t -> t.getId())
                .orElse(null);
        if (resolved != null) {
            this.cachedDefaultTenantId = resolved;
        }
        return resolved;
    }

    @Transactional(readOnly = true)
    public String resolveTenantCodeByHost(final String host) {
        final String normalizedHost = normalizeHost(host);
        if (normalizedHost.isBlank()) {
            return "";
        }
        final String direct = configuredTenantForHost(normalizedHost);
        if (!direct.isBlank()) {
            return direct;
        }
        if (normalizedHost.startsWith("www.")) {
            return configuredTenantForHost(normalizedHost.substring(4));
        }
        return "";
    }

    @Transactional(readOnly = true)
    public Long resolveTenantIdByHost(final String host) {
        final String tenantCode = resolveTenantCodeByHost(host);
        if (tenantCode.isBlank()) {
            return null;
        }
        return this.tenantRepository.findByCodigo(tenantCode)
                .map(t -> t.getId())
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Long resolveTenantIdForRequest(final HttpServletRequest request) {
        if (request == null) {
            return resolveDefaultTenantId();
        }
        final Long resolvedByHost = resolveTenantIdByHost(request.getServerName());
        return resolvedByHost != null ? resolvedByHost : resolveDefaultTenantId();
    }

    private String configuredTenantForHost(final String normalizedHost) {
        final String key = HOST_MAPPING_PREFIX + normalizedHost;
        final String configured = appSettingService.getOrDefault(key, "").trim();
        if (configured.isBlank()) {
            return "";
        }
        if (configured.chars().allMatch(Character::isDigit)) {
            return this.tenantRepository.findById(Long.valueOf(configured))
                    .map(t -> t.getCodigo())
                    .orElse("");
        }
        return configured;
    }

    private static String normalizeHost(final String host) {
        if (host == null || host.isBlank()) {
            return "";
        }
        final String lower = host.trim().toLowerCase();
        final int idx = lower.indexOf(':');
        if (idx > -1) {
            return lower.substring(0, idx);
        }
        return lower;
    }
}
