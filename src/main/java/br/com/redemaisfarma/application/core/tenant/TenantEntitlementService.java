package br.com.redemaisfarma.application.core.tenant;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.AddonCatalogEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PlanCatalogEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PlanFeatureEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.TenantAddonSubscriptionEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.TenantEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.AddonCatalogRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.PlanCatalogRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.PlanFeatureRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.TenantAddonSubscriptionRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.TenantPlanRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.TenantRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantEntitlementService {

    private static final String DEFAULT_PLAN_CODE = "FREE";

    private final TenantRepository tenantRepository;
    private final TenantPlanRepository tenantPlanRepository;
    private final PlanCatalogRepository planCatalogRepository;
    private final PlanFeatureRepository planFeatureRepository;
    private final TenantAddonSubscriptionRepository tenantAddonSubscriptionRepository;
    private final AddonCatalogRepository addonCatalogRepository;

    public TenantEntitlementService(
            final TenantRepository tenantRepositoryValue,
            final TenantPlanRepository tenantPlanRepositoryValue,
            final PlanCatalogRepository planCatalogRepositoryValue,
            final PlanFeatureRepository planFeatureRepositoryValue,
            final TenantAddonSubscriptionRepository tenantAddonSubscriptionRepositoryValue,
            final AddonCatalogRepository addonCatalogRepositoryValue
    ) {
        this.tenantRepository = tenantRepositoryValue;
        this.tenantPlanRepository = tenantPlanRepositoryValue;
        this.planCatalogRepository = planCatalogRepositoryValue;
        this.planFeatureRepository = planFeatureRepositoryValue;
        this.tenantAddonSubscriptionRepository = tenantAddonSubscriptionRepositoryValue;
        this.addonCatalogRepository = addonCatalogRepositoryValue;
    }

    @Transactional(readOnly = true)
    public Optional<TenantEntitlementSnapshot> resolveByTenantIdentifier(final String tenantIdentifier) {
        final Optional<TenantEntity> tenantOpt = resolveTenant(tenantIdentifier);
        if (tenantOpt.isEmpty()) {
            return Optional.empty();
        }

        final TenantEntity tenant = tenantOpt.get();
        final PlanCatalogEntity plan = resolvePlanForTenant(tenant.getId());
        final Set<String> effectiveFeatures = new LinkedHashSet<>();
        final List<String> addonCodes = new ArrayList<>();

        final List<PlanFeatureEntity> planFeatures = this.planFeatureRepository.findAllByPlanIdAndEnabledTrue(plan.getId());
        for (PlanFeatureEntity planFeature : planFeatures) {
            if (planFeature.getFeatureKey() != null && !planFeature.getFeatureKey().isBlank()) {
                effectiveFeatures.add(planFeature.getFeatureKey());
            }
        }

        final List<TenantAddonSubscriptionEntity> activeSubscriptions =
                this.tenantAddonSubscriptionRepository.findAllByTenantIdAndActiveTrue(tenant.getId());
        if (!activeSubscriptions.isEmpty()) {
            final List<Long> addonIds = activeSubscriptions.stream()
                    .map(TenantAddonSubscriptionEntity::getAddonId)
                    .filter(id -> id != null)
                    .distinct()
                    .toList();
            final List<AddonCatalogEntity> addons = this.addonCatalogRepository.findAllByIdInAndActiveTrue(addonIds);
            addons.stream()
                    .filter(addon -> addon.getCode() != null)
                    .map(AddonCatalogEntity::getCode)
                    .sorted()
                    .forEach(addonCodes::add);
            addons.stream()
                    .filter(addon -> addon.getFeatureKey() != null && !addon.getFeatureKey().isBlank())
                    .map(AddonCatalogEntity::getFeatureKey)
                    .forEach(effectiveFeatures::add);
        }

        final List<String> sortedFeatures = effectiveFeatures.stream()
                .sorted(Comparator.naturalOrder())
                .toList();

        return Optional.of(new TenantEntitlementSnapshot(
                tenant.getId(),
                tenant.getCodigo(),
                plan.getCode(),
                sortedFeatures,
                addonCodes
        ));
    }

    private PlanCatalogEntity resolvePlanForTenant(final Long tenantId) {
        return this.tenantPlanRepository.findByTenantId(tenantId)
                .flatMap(tp -> this.planCatalogRepository.findById(tp.getPlanId()))
                .or(() -> this.planCatalogRepository.findByCode(DEFAULT_PLAN_CODE))
                .orElseThrow(() -> new IllegalStateException("Plano padrao FREE nao encontrado no catalogo."));
    }

    private Optional<TenantEntity> resolveTenant(final String tenantIdentifier) {
        if (tenantIdentifier == null || tenantIdentifier.isBlank()) {
            return Optional.empty();
        }
        final String normalized = tenantIdentifier.trim();
        if (isNumeric(normalized)) {
            return this.tenantRepository.findById(Long.valueOf(normalized));
        }
        return this.tenantRepository.findByCodigo(normalized.toLowerCase(Locale.ROOT));
    }

    private static boolean isNumeric(final String value) {
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return !value.isEmpty();
    }
}

