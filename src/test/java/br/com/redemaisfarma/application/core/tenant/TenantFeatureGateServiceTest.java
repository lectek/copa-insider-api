package br.com.redemaisfarma.application.core.tenant;

import br.com.redemaisfarma.application.core.settings.AppSettingService;
import java.lang.reflect.Field;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantFeatureGateServiceTest {

    @Mock
    private AppSettingService appSettingService;

    @Mock
    private TenantResolverService tenantResolverService;

    @InjectMocks
    private TenantFeatureGateService service;

    @Test
    void isEnabledUsesTenantSpecificOverrideWhenPresent() {
        when(appSettingService.get("tenant.9.feature.mod.receita_controlada")).thenReturn(Optional.of("false"));
        when(appSettingService.getBoolean("tenant.9.feature.mod.receita_controlada", true)).thenReturn(false);

        boolean enabled = service.isEnabled(9L, TenantFeature.MOD_RECEITA_CONTROLADA, true);

        assertThat(enabled).isFalse();
    }

    @Test
    void isEnabledFallsBackToGlobalFlagWhenTenantOverrideIsMissing() {
        when(appSettingService.get("tenant.9.feature.mod.receita_controlada")).thenReturn(Optional.empty());
        when(appSettingService.getBoolean("feature.mod.receita_controlada", false)).thenReturn(true);

        boolean enabled = service.isEnabled(9L, TenantFeature.MOD_RECEITA_CONTROLADA, false);

        assertThat(enabled).isTrue();
    }

    @Test
    void isEnabledForCurrentTenantUsesResolvedTenantId() throws Exception {
        injectTenantResolver();
        when(tenantResolverService.resolveDefaultTenantId()).thenReturn(15L);
        when(appSettingService.get("tenant.15.feature.mod.receita_controlada")).thenReturn(Optional.of("true"));
        when(appSettingService.getBoolean("tenant.15.feature.mod.receita_controlada", false)).thenReturn(true);

        boolean enabled = service.isEnabledForCurrentTenant(TenantFeature.MOD_RECEITA_CONTROLADA, false);

        assertThat(enabled).isTrue();
    }

    private void injectTenantResolver() throws Exception {
        Field field = TenantFeatureGateService.class.getDeclaredField("tenantResolverService");
        field.setAccessible(true);
        field.set(service, tenantResolverService);
    }
}
