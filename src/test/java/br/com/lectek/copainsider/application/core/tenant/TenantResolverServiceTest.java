package br.com.lectek.copainsider.application.core.tenant;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.TenantEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.TenantRepository;
import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantResolverServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private AppSettingService appSettingService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private TenantResolverService service;

    @Test
    void resolveDefaultTenantIdCachesResolvedTenant() throws Exception {
        TenantEntity tenant = tenant(7L, "default");
        when(tenantRepository.findByCodigo(TenantResolverService.DEFAULT_TENANT_CODE))
                .thenReturn(Optional.of(tenant));

        Long first = service.resolveDefaultTenantId();
        Long second = service.resolveDefaultTenantId();

        assertThat(first).isEqualTo(7L);
        assertThat(second).isEqualTo(7L);
        verify(tenantRepository).findByCodigo(TenantResolverService.DEFAULT_TENANT_CODE);
    }

    @Test
    void resolveTenantCodeByHostUsesConfiguredHostAndStripsPort() {
        when(appSettingService.getOrDefault("tenant.host.embalando.com.br", "")).thenReturn("embalando");

        String result = service.resolveTenantCodeByHost("Embalando.com.br:8443");

        assertThat(result).isEqualTo("embalando");
    }

    @Test
    void resolveTenantCodeByHostFallsBackFromWww() {
        when(appSettingService.getOrDefault("tenant.host.www.embalando.com.br", "")).thenReturn("");
        when(appSettingService.getOrDefault("tenant.host.embalando.com.br", "")).thenReturn("embalando");

        String result = service.resolveTenantCodeByHost("www.embalando.com.br");

        assertThat(result).isEqualTo("embalando");
    }

    @Test
    void resolveTenantIdByHostSupportsNumericConfiguredTenant() throws Exception {
        TenantEntity tenant = tenant(11L, "embalando");
        when(appSettingService.getOrDefault("tenant.host.embalando.com.br", "")).thenReturn("11");
        when(tenantRepository.findById(11L)).thenReturn(Optional.of(tenant));
        when(tenantRepository.findByCodigo("embalando")).thenReturn(Optional.of(tenant));

        Long result = service.resolveTenantIdByHost("embalando.com.br");

        assertThat(result).isEqualTo(11L);
    }

    @Test
    void resolveTenantIdForRequestFallsBackToDefaultTenantWhenHostIsUnknown() throws Exception {
        TenantEntity defaultTenant = tenant(3L, "default");
        when(request.getServerName()).thenReturn("unknown.local");
        when(appSettingService.getOrDefault("tenant.host.unknown.local", "")).thenReturn("");
        when(tenantRepository.findByCodigo(TenantResolverService.DEFAULT_TENANT_CODE))
                .thenReturn(Optional.of(defaultTenant));

        Long result = service.resolveTenantIdForRequest(request);

        assertThat(result).isEqualTo(3L);
    }

    private static TenantEntity tenant(Long id, String codigo) throws Exception {
        TenantEntity tenant = new TenantEntity();
        setField(tenant, "id", id);
        setField(tenant, "codigo", codigo);
        setField(tenant, "nome", "Tenant " + codigo);
        setField(tenant, "ativo", true);
        return tenant;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
