package br.com.lectek.copainsider.adapters.inbound.web.support;

import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import br.com.lectek.copainsider.application.core.tenant.TenantResolverService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TenantScopedSettingsService {

    public static final String QUERY_TENANT_ID = "tenantId";
    public static final String SESSION_TENANT_CONTEXT_ID = "tenantContextId";
    private static final String TENANT_KEY_PREFIX = "tenant.";

    private final AppSettingService settings;
    @Autowired(required = false)
    private TenantResolverService tenantResolverService;

    public TenantScopedSettingsService(final AppSettingService appSettingService) {
        this.settings = appSettingService;
    }

    public String resolveTenantContextId(final HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        final String queryTenant = normalizeTenantId(
                request.getParameter(QUERY_TENANT_ID)
        );
        if (!queryTenant.isBlank()) {
            request.getSession().setAttribute(SESSION_TENANT_CONTEXT_ID, queryTenant);
            return queryTenant;
        }
        final String sessionTenant = readTenantContextId(request.getSession(false));
        if (!sessionTenant.isBlank()) {
            return sessionTenant;
        }
        final String hostTenant = resolveTenantContextIdByHost(request);
        if (!hostTenant.isBlank()) {
            request.getSession().setAttribute(SESSION_TENANT_CONTEXT_ID, hostTenant);
            return hostTenant;
        }
        return "";
    }

    public String getOrDefault(
            final String tenantId,
            final String key,
            final String defaultValue
    ) {
        final String normalizedTenant = normalizeTenantId(tenantId);
        if (!normalizedTenant.isBlank()) {
            final String tenantScoped = settings.getOrDefault(
                    scopedKey(normalizedTenant, key),
                    ""
            );
            if (!tenantScoped.isBlank()) {
                return tenantScoped;
            }
        }
        return settings.getOrDefault(key, defaultValue);
    }

    public Map<String, String> getAllByKeys(
            final String tenantId,
            final Collection<String> keys
    ) {
        if (keys == null || keys.isEmpty()) {
            return Map.of();
        }
        final String normalizedTenant = normalizeTenantId(tenantId);
        final Map<String, String> resolved = new LinkedHashMap<>();
        for (String key : keys) {
            if (key == null || key.isBlank()) {
                continue;
            }
            resolved.put(key, getOrDefault(normalizedTenant, key, ""));
        }
        return resolved;
    }

    public void upsert(
            final String tenantId,
            final String key,
            final String value,
            final String description
    ) {
        final String normalizedTenant = normalizeTenantId(tenantId);
        if (normalizedTenant.isBlank()) {
            settings.upsert(key, value, description);
            return;
        }
        settings.upsert(
                scopedKey(normalizedTenant, key),
                value,
                "[tenant=" + normalizedTenant + "] " + description
        );
    }

    public static String normalizeTenantId(final String tenantId) {
        return tenantId == null ? "" : tenantId.trim();
    }

    private static String scopedKey(final String tenantId, final String key) {
        return TENANT_KEY_PREFIX + tenantId + "." + key;
    }

    private static String readTenantContextId(final HttpSession session) {
        if (session == null) {
            return "";
        }
        final Object tenantContextId = session.getAttribute(SESSION_TENANT_CONTEXT_ID);
        return tenantContextId == null
                ? ""
                : normalizeTenantId(tenantContextId.toString());
    }

    private String resolveTenantContextIdByHost(final HttpServletRequest request) {
        if (request == null || tenantResolverService == null) {
            return "";
        }
        return normalizeTenantId(
                tenantResolverService.resolveTenantCodeByHost(resolveRequestHost(request))
        );
    }

    private static String resolveRequestHost(final HttpServletRequest request) {
        final String forwarded = normalizeTenantId(request.getHeader("X-Forwarded-Host"));
        if (!forwarded.isBlank()) {
            final int comma = forwarded.indexOf(',');
            return comma > -1 ? forwarded.substring(0, comma).trim() : forwarded;
        }
        return normalizeTenantId(request.getServerName());
    }
}
