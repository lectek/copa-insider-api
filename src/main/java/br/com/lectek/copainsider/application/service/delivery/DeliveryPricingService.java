package br.com.lectek.copainsider.application.service.delivery;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.lectek.copainsider.application.config.AppProps;
import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import br.com.lectek.copainsider.application.view.DeliveryQuoteVM;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliveryPricingService {

    private static final Logger log = LoggerFactory.getLogger(
            DeliveryPricingService.class
    );

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final BigDecimal DEFAULT_FREE_RADIUS_KM =
            new BigDecimal("5.00");
    private static final BigDecimal DEFAULT_RATE_PER_KM =
            new BigDecimal("2.00");
    private static final BigDecimal DEFAULT_PRIORITY_SURCHARGE =
            new BigDecimal("20.00");
    private static final String SETTING_FREE_RADIUS_KM =
            "entrega.frete.gratis_km";
    private static final String SETTING_RATE_PER_KM =
            "entrega.frete.valor_km_excedente";
    private static final String SETTING_PRIORITY_SURCHARGE =
            "entrega.frete.prioritario.acrescimo";
    private static final String SHIPPING_UNAVAILABLE_SUMMARY =
            "Nao foi possivel calcular o frete para esse endereco.";
    private static final String SHIPPING_UNAVAILABLE_DETAIL =
            "Revise a rua e tente novamente.";

    private final DeliveryRouteService deliveryRouteService;
    private final AppSettingService appSettingService;
    private final AppProps appProps;
    private final UsuarioRepository usuarioRepository;

    public DeliveryPricingService(
            final DeliveryRouteService deliveryRouteServiceValue,
            final AppSettingService appSettingServiceValue,
            final AppProps appPropsValue,
            final UsuarioRepository usuarioRepositoryValue
    ) {
        this.deliveryRouteService = deliveryRouteServiceValue;
        this.appSettingService = appSettingServiceValue;
        this.appProps = appPropsValue;
        this.usuarioRepository = usuarioRepositoryValue;
    }

    @Transactional(readOnly = true)
    public DeliveryQuoteVM quoteForCheckout(
            final String typedAddress,
            final Authentication authentication
    ) {
        final String resolvedAddress = resolveAddress(typedAddress, authentication);
        return quoteForAddress(resolvedAddress);
    }

    @Transactional(readOnly = true)
    public DeliveryQuoteVM quoteForAddress(final String rawAddress) {
        final BigDecimal freeRadiusKm = readPositiveDecimal(
                SETTING_FREE_RADIUS_KM,
                DEFAULT_FREE_RADIUS_KM
        );
        final BigDecimal ratePerKm = readNonNegativeDecimal(
                SETTING_RATE_PER_KM,
                DEFAULT_RATE_PER_KM
        );
        final BigDecimal prioritySurcharge = readNonNegativeDecimal(
                SETTING_PRIORITY_SURCHARGE,
                DEFAULT_PRIORITY_SURCHARGE
        );

        final String address = normalize(rawAddress);
        if (address.isBlank()) {
            return DeliveryQuoteVM.unavailable(
                    freeRadiusKm,
                    ratePerKm,
                    prioritySurcharge,
                    "Informe o endereco para calcular o frete",
                    buildPolicyDetail(freeRadiusKm, ratePerKm)
            );
        }

        final String origin = normalize(appProps.getAddressQuery());
        if (origin.isBlank()) {
            return DeliveryQuoteVM.unavailable(
                    freeRadiusKm,
                    ratePerKm,
                    prioritySurcharge,
                    "Nao foi possivel preparar a cotacao de entrega",
                    "A origem da loja nao esta configurada para calcular a rota."
            );
        }

        try {
            final BigDecimal distanceKm = deliveryRouteService.estimateDistanceBetween(
                    origin,
                    address
            );
            if (distanceKm == null) {
                return DeliveryQuoteVM.unavailable(
                        freeRadiusKm,
                        ratePerKm,
                        prioritySurcharge,
                        SHIPPING_UNAVAILABLE_SUMMARY,
                        SHIPPING_UNAVAILABLE_DETAIL
                );
            }

            final BigDecimal billableDistanceKm = max(
                    distanceKm.subtract(freeRadiusKm),
                    BigDecimal.ZERO
            ).setScale(2, RoundingMode.HALF_UP);
            final BigDecimal standardShippingAmount = billableDistanceKm
                    .multiply(ratePerKm)
                    .setScale(2, RoundingMode.HALF_UP);
            final BigDecimal priorityShippingAmount = standardShippingAmount
                    .add(prioritySurcharge)
                    .setScale(2, RoundingMode.HALF_UP);

            return DeliveryQuoteVM.available(
                    address,
                    distanceKm,
                    freeRadiusKm,
                    billableDistanceKm,
                    ratePerKm,
                    standardShippingAmount,
                    prioritySurcharge,
                    priorityShippingAmount,
                    buildSummary(standardShippingAmount),
                    buildAvailableDetail(
                            distanceKm,
                            freeRadiusKm,
                            ratePerKm
                    )
            );
        } catch (RuntimeException ex) {
            log.warn(
                    "Nao foi possivel calcular o frete para '{}': {}",
                    address,
                    ex.getMessage()
            );
            return DeliveryQuoteVM.unavailable(
                    freeRadiusKm,
                    ratePerKm,
                    prioritySurcharge,
                    SHIPPING_UNAVAILABLE_SUMMARY,
                    SHIPPING_UNAVAILABLE_DETAIL
            );
        }
    }

    public BigDecimal resolveShippingAmount(
            final String address,
            final boolean priority
    ) {
        final DeliveryQuoteVM quote = quoteForAddress(address);
        if (!quote.available()) {
            throw new IllegalArgumentException(
                    SHIPPING_UNAVAILABLE_SUMMARY
            );
        }
        return priority
                ? quote.priorityShippingAmount()
                : quote.standardShippingAmount();
    }

    private String resolveAddress(
            final String typedAddress,
            final Authentication authentication
    ) {
        final String explicitAddress = normalize(typedAddress);
        if (!explicitAddress.isBlank()) {
            return explicitAddress;
        }
        if (authentication == null || authentication.getName() == null) {
            return "";
        }
        return usuarioRepository.findByEmailOrCpf(authentication.getName())
                .map(UsuarioEntity::getEndereco)
                .map(this::normalize)
                .orElse("");
    }

    private String buildSummary(final BigDecimal shippingAmount) {
        if (shippingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return "Entrega gratis para este endereco";
        }
        return "Frete padrao em " + formatCurrency(shippingAmount);
    }

    private String buildAvailableDetail(
            final BigDecimal distanceKm,
            final BigDecimal freeRadiusKm,
            final BigDecimal ratePerKm
    ) {
        return "Distancia estimada de "
                + formatDistance(distanceKm)
                + ". "
                + buildPolicyDetail(freeRadiusKm, ratePerKm);
    }

    private String buildPolicyDetail(
            final BigDecimal freeRadiusKm,
            final BigDecimal ratePerKm
    ) {
        return "Ate "
                + formatDistance(freeRadiusKm)
                + " a entrega e gratis; depois cobramos "
                + formatCurrency(ratePerKm)
                + " por km excedente.";
    }

    private BigDecimal readPositiveDecimal(
            final String key,
            final BigDecimal defaultValue
    ) {
        final BigDecimal value = appSettingService.getDecimal(key, defaultValue);
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            return defaultValue;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal readNonNegativeDecimal(
            final String key,
            final BigDecimal defaultValue
    ) {
        final BigDecimal value = appSettingService.getDecimal(key, defaultValue);
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            return defaultValue;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal max(
            final BigDecimal left,
            final BigDecimal right
    ) {
        return left.compareTo(right) >= 0 ? left : right;
    }

    private String formatCurrency(final BigDecimal value) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(value);
    }

    private String formatDistance(final BigDecimal value) {
        if (value == null) {
            return "0 km";
        }
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString() + " km";
    }

    private String normalize(final String value) {
        return value == null ? "" : value.trim();
    }
}
