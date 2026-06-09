package br.com.lectek.copainsider.application.core.tenant;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.AddonCatalogEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.PlanCatalogEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.PlanFeatureEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.TenantAddonSubscriptionEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.TenantEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.TenantPlanEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.AddonCatalogRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.PlanCatalogRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.PlanFeatureRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.TenantAddonSubscriptionRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.TenantPlanRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.TenantRepository;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantEntitlementServiceTest {

    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private TenantPlanRepository tenantPlanRepository;
    @Mock
    private PlanCatalogRepository planCatalogRepository;
    @Mock
    private PlanFeatureRepository planFeatureRepository;
    @Mock
    private TenantAddonSubscriptionRepository tenantAddonSubscriptionRepository;
    @Mock
    private AddonCatalogRepository addonCatalogRepository;

    @InjectMocks
    private TenantEntitlementService service;

    @Test
    void resolveByTenantIdentifierCombinaFeaturesDePlanoEAddon() throws Exception {
        TenantEntity tenant = tenant(10L, "embalando");
        PlanCatalogEntity plan = plan(2L, "START");
        TenantPlanEntity tenantPlan = tenantPlan(10L, 2L);
        PlanFeatureEntity planFeature = planFeature(2L, "ads_enabled");
        TenantAddonSubscriptionEntity addonSub = addonSubscription(10L, 7L);
        AddonCatalogEntity addon = addon(7L, "ADDON_AI_OPS", "ai_ops");

        when(tenantRepository.findById(10L)).thenReturn(Optional.of(tenant));
        when(tenantPlanRepository.findByTenantId(10L)).thenReturn(Optional.of(tenantPlan));
        when(planCatalogRepository.findById(2L)).thenReturn(Optional.of(plan));
        when(planFeatureRepository.findAllByPlanIdAndEnabledTrue(2L)).thenReturn(List.of(planFeature));
        when(tenantAddonSubscriptionRepository.findAllByTenantIdAndActiveTrue(10L)).thenReturn(List.of(addonSub));
        when(addonCatalogRepository.findAllByIdInAndActiveTrue(anyList())).thenReturn(List.of(addon));

        Optional<TenantEntitlementSnapshot> result = service.resolveByTenantIdentifier("10");

        assertThat(result).isPresent();
        assertThat(result.get().tenantCode()).isEqualTo("embalando");
        assertThat(result.get().planCode()).isEqualTo("START");
        assertThat(result.get().featureKeys()).containsExactly("ads_enabled", "ai_ops");
        assertThat(result.get().addonCodes()).containsExactly("ADDON_AI_OPS");
    }

    @Test
    void resolveByTenantIdentifierUsaPlanoFreeComoFallback() throws Exception {
        TenantEntity tenant = tenant(20L, "default");
        PlanCatalogEntity freePlan = plan(1L, "FREE");

        when(tenantRepository.findByCodigo("default")).thenReturn(Optional.of(tenant));
        when(tenantPlanRepository.findByTenantId(20L)).thenReturn(Optional.empty());
        when(planCatalogRepository.findByCode("FREE")).thenReturn(Optional.of(freePlan));
        when(planFeatureRepository.findAllByPlanIdAndEnabledTrue(1L)).thenReturn(List.of(planFeature(1L, "commission_enabled")));
        when(tenantAddonSubscriptionRepository.findAllByTenantIdAndActiveTrue(20L)).thenReturn(List.of());

        Optional<TenantEntitlementSnapshot> result = service.resolveByTenantIdentifier("default");

        assertThat(result).isPresent();
        assertThat(result.get().planCode()).isEqualTo("FREE");
        assertThat(result.get().featureKeys()).containsExactly("commission_enabled");
    }

    private static TenantEntity tenant(Long id, String codigo) throws Exception {
        TenantEntity tenant = new TenantEntity();
        setField(tenant, "id", id);
        setField(tenant, "codigo", codigo);
        setField(tenant, "nome", "Tenant " + codigo);
        setField(tenant, "ativo", true);
        return tenant;
    }

    private static PlanCatalogEntity plan(Long id, String code) throws Exception {
        PlanCatalogEntity plan = new PlanCatalogEntity();
        setField(plan, "id", id);
        setField(plan, "code", code);
        setField(plan, "name", "Plano " + code);
        setField(plan, "active", true);
        return plan;
    }

    private static TenantPlanEntity tenantPlan(Long tenantId, Long planId) throws Exception {
        TenantPlanEntity tenantPlan = new TenantPlanEntity();
        setField(tenantPlan, "tenantId", tenantId);
        setField(tenantPlan, "planId", planId);
        return tenantPlan;
    }

    private static PlanFeatureEntity planFeature(Long planId, String featureKey) throws Exception {
        PlanFeatureEntity feature = new PlanFeatureEntity();
        setField(feature, "planId", planId);
        setField(feature, "featureKey", featureKey);
        setField(feature, "enabled", true);
        return feature;
    }

    private static TenantAddonSubscriptionEntity addonSubscription(Long tenantId, Long addonId) throws Exception {
        TenantAddonSubscriptionEntity subscription = new TenantAddonSubscriptionEntity();
        setField(subscription, "tenantId", tenantId);
        setField(subscription, "addonId", addonId);
        setField(subscription, "active", true);
        return subscription;
    }

    private static AddonCatalogEntity addon(Long id, String code, String featureKey) throws Exception {
        AddonCatalogEntity addon = new AddonCatalogEntity();
        setField(addon, "id", id);
        setField(addon, "code", code);
        setField(addon, "name", code);
        setField(addon, "featureKey", featureKey);
        setField(addon, "active", true);
        return addon;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

