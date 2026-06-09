package br.com.lectek.copainsider.application.core.tenant;

import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TenantFeatureGateService {

    private static final String GLOBAL_PREFIX = "feature.";
    private static final String TENANT_PREFIX = "tenant.";

    private final AppSettingService appSettingService;
    @Autowired(required = false)
    private TenantResolverService tenantResolverService;

    public TenantFeatureGateService(final AppSettingService appSettingServiceValue) {
        this.appSettingService = appSettingServiceValue;
    }

    public boolean isEnabledForCurrentTenant(final TenantFeature feature, final boolean defaultValue) {
        return isEnabled(resolveCurrentTenantId(), feature, defaultValue);
    }

    public boolean isEnabled(final Long tenantId, final TenantFeature feature, final boolean defaultValue) {
        if (feature == null) {
            return defaultValue;
        }
        final String globalKey = GLOBAL_PREFIX + feature.key();
        if (tenantId == null) {
            return appSettingService.getBoolean(globalKey, defaultValue);
        }
        final String tenantKey = TENANT_PREFIX + tenantId + "." + globalKey;
        if (appSettingService.get(tenantKey).isPresent()) {
            return appSettingService.getBoolean(tenantKey, defaultValue);
        }
        return appSettingService.getBoolean(globalKey, defaultValue);
    }

    public Long resolveCurrentTenantId() {
        if (tenantResolverService == null) {
            return null;
        }
        return tenantResolverService.resolveDefaultTenantId();
    }
}
