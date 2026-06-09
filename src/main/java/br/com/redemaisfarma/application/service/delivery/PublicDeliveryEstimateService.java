package br.com.redemaisfarma.application.service.delivery;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.redemaisfarma.application.config.AppProps;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.application.view.DeliveryEstimateVM;
import br.com.redemaisfarma.domain.enums.ModoEntrega;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicDeliveryEstimateService {

    private static final Logger log = LoggerFactory.getLogger(
            PublicDeliveryEstimateService.class
    );

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm", PT_BR);
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM 'às' HH:mm", PT_BR);
    private static final String SETTING_ROUTE_DEPARTURE = "entrega.rota.horario_saida";
    private static final String SETTING_ROUTE_SPEED = "entrega.rota.velocidade_media_kmh";
    private static final String SETTING_ROUTE_STOP_MINUTES = "entrega.rota.minutos_por_parada";
    private static final String SETTING_ROUTE_MAX_STOPS = "entrega.rota.max_paradas";
    private static final String DEFAULT_ROUTE_DEPARTURE = "09:00";
    private static final int DEFAULT_ROUTE_SPEED_KMH = 28;
    private static final int DEFAULT_STOP_MINUTES = 6;
    private static final int DEFAULT_MAX_STOPS = 12;
    private static final long PREVIEW_STOP_ID = Long.MIN_VALUE;
    private static final List<StatusPedido> ACTIVE_DELIVERY_STATUSES = List.of(
            StatusPedido.PAGO,
            StatusPedido.PRONTO_PARA_ENTREGA,
            StatusPedido.SAIU_PARA_ENTREGA,
            StatusPedido.ENVIADO
    );

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final DeliveryRouteService deliveryRouteService;
    private final AppSettingService appSettingService;
    private final AppProps appProps;
    private final Clock clock;

    public PublicDeliveryEstimateService(
            final PedidoRepository pedidoRepository,
            final UsuarioRepository usuarioRepository,
            final DeliveryRouteService deliveryRouteService,
            final AppSettingService appSettingService,
            final AppProps appProps,
            final Clock clock
    ) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.deliveryRouteService = deliveryRouteService;
        this.appSettingService = appSettingService;
        this.appProps = appProps;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public DeliveryEstimateVM estimateFor(final Authentication authentication) {
        final String customerAddress = resolveCustomerAddress(authentication);
        if (customerAddress.isBlank()) {
            return DeliveryEstimateVM.unavailable(
                    "Veja a entrega no seu CEP",
                    "Entre na sua conta e salve o endereco para calcular a rota."
            );
        }

        final int maxStops = readPositiveInt(
                SETTING_ROUTE_MAX_STOPS,
                DEFAULT_MAX_STOPS
        );
        final List<PedidoEntity> activeOrders = pedidoRepository
                .findByModoEntregaAndStatusInOrderByDataAsc(
                        ModoEntrega.ENTREGA,
                        ACTIVE_DELIVERY_STATUSES,
                        PageRequest.of(0, maxStops)
                );

        final List<DeliveryRouteService.DeliveryStopInput> inputs =
                new ArrayList<>();
        for (PedidoEntity order : activeOrders) {
            final String orderAddress = resolveOrderAddress(order);
            if (orderAddress.isBlank()) {
                continue;
            }
            inputs.add(
                    new DeliveryRouteService.DeliveryStopInput(
                            order.getId(),
                            resolveCustomerName(order),
                            orderAddress,
                            order.getCodigoEntrega(),
                            order.getStatus() == null
                                    ? "DESCONHECIDO"
                                    : order.getStatus().name()
                    )
            );
        }
        inputs.add(
                new DeliveryRouteService.DeliveryStopInput(
                        PREVIEW_STOP_ID,
                        "Seu endereco",
                        customerAddress,
                        "PREVISAO",
                        "PREVIEW"
                )
        );

        try {
            final DeliveryRouteService.PlannedRoute planned =
                    deliveryRouteService.plan(appProps.getAddressQuery(), inputs);
            final DeliveryRouteService.DeliveryStopPlan stop = planned.paradas()
                    .stream()
                    .filter(item -> PREVIEW_STOP_ID == item.pedidoId())
                    .findFirst()
                    .orElseThrow();
            return buildEstimate(stop, planned.paradas().size());
        } catch (RuntimeException ex) {
            log.warn("Nao foi possivel calcular a previsao publica de entrega: {}", ex.getMessage());
            return DeliveryEstimateVM.unavailable(
                    "Previsao calculada no checkout",
                    "Nao foi possivel montar a rota agora. Tente novamente em instantes."
            );
        }
    }

    private DeliveryEstimateVM buildEstimate(
            final DeliveryRouteService.DeliveryStopPlan stop,
            final int totalStops
    ) {
        final LocalDateTime now = LocalDateTime.now(clock);
        final LocalDateTime departureAt = nextDeparture(
                now,
                readDepartureTime()
        );
        final int averageSpeedKmh = readPositiveInt(
                SETTING_ROUTE_SPEED,
                DEFAULT_ROUTE_SPEED_KMH
        );
        final int minutesPerStop = readPositiveInt(
                SETTING_ROUTE_STOP_MINUTES,
                DEFAULT_STOP_MINUTES
        );
        final long travelMinutes = resolveTravelMinutes(stop, averageSpeedKmh);
        final long handlingMinutes = Math.max(0, stop.ordem() - 1) * (long) minutesPerStop;
        final LocalDateTime eta = departureAt.plusMinutes(travelMinutes + handlingMinutes);
        return DeliveryEstimateVM.available(
                buildSummary(now.toLocalDate(), eta),
                buildDetail(stop, totalStops, departureAt)
        );
    }

    private long resolveTravelMinutes(
            final DeliveryRouteService.DeliveryStopPlan stop,
            final int averageSpeedKmh
    ) {
        if (stop.duracaoAcumuladaSegundos() != null
                && stop.duracaoAcumuladaSegundos() > 0L) {
            return Math.max(1L, Math.round(stop.duracaoAcumuladaSegundos() / 60.0d));
        }
        return Math.max(
                1L,
                Math.round(stop.distanciaAcumuladaKm().doubleValue()
                        / averageSpeedKmh * 60.0d)
        );
    }

    private String buildSummary(
            final LocalDate today,
            final LocalDateTime eta
    ) {
        if (eta.toLocalDate().isEqual(today)) {
            return "Entrega hoje por volta de " + eta.format(TIME_FORMAT);
        }
        if (eta.toLocalDate().isEqual(today.plusDays(1))) {
            return "Entrega amanha por volta de " + eta.format(TIME_FORMAT);
        }
        return "Entrega prevista para " + eta.format(DATE_TIME_FORMAT);
    }

    private String buildDetail(
            final DeliveryRouteService.DeliveryStopPlan stop,
            final int totalStops,
            final LocalDateTime departureAt
    ) {
        return "Parada "
                + stop.ordem()
                + " de "
                + totalStops
                + " na rota que sai as "
                + departureAt.format(TIME_FORMAT)
                + ".";
    }

    private LocalDateTime nextDeparture(
            final LocalDateTime now,
            final LocalTime departureTime
    ) {
        LocalDateTime departure = LocalDateTime.of(now.toLocalDate(), departureTime);
        if (now.isAfter(departure)) {
            departure = departure.plusDays(1);
        }
        return departure;
    }

    private LocalTime readDepartureTime() {
        final String raw = appSettingService.getOrDefault(
                SETTING_ROUTE_DEPARTURE,
                DEFAULT_ROUTE_DEPARTURE
        );
        try {
            return LocalTime.parse(normalize(raw), TIME_FORMAT);
        } catch (RuntimeException ex) {
            return LocalTime.parse(DEFAULT_ROUTE_DEPARTURE, TIME_FORMAT);
        }
    }

    private int readPositiveInt(final String key, final int defaultValue) {
        final int value = appSettingService.getInt(key, defaultValue);
        return value > 0 ? value : defaultValue;
    }

    private String resolveCustomerAddress(final Authentication authentication) {
        final Optional<UsuarioEntity> user = resolveUser(authentication);
        if (user.isEmpty()) {
            return "";
        }
        return normalize(user.get().getEndereco());
    }

    private Optional<UsuarioEntity> resolveUser(final Authentication authentication) {
        if (authentication == null) {
            return Optional.empty();
        }
        final String authName = normalize(authentication.getName());
        if (authName.isBlank() || "anonymousUser".equalsIgnoreCase(authName)) {
            return Optional.empty();
        }
        final Optional<UsuarioEntity> directMatch = usuarioRepository.findByEmailOrCpf(authName);
        if (directMatch.isPresent()) {
            return directMatch;
        }
        final String email = extractEmail(authentication);
        if (email.isBlank()) {
            return Optional.empty();
        }
        return usuarioRepository.findByEmailIgnoreCase(email);
    }

    private String resolveOrderAddress(final PedidoEntity order) {
        final String orderAddress = normalize(order.getEnderecoEntrega());
        if (!orderAddress.isBlank()) {
            return orderAddress;
        }
        if (order.getCliente() == null) {
            return "";
        }
        final String email = normalize(order.getCliente().getEmail());
        if (email.isBlank()) {
            return "";
        }
        return usuarioRepository.findByEmailIgnoreCase(email)
                .map(UsuarioEntity::getEndereco)
                .map(this::normalize)
                .orElse("");
    }

    private String resolveCustomerName(final PedidoEntity order) {
        if (order.getCliente() == null) {
            return "Cliente";
        }
        final String name = normalize(order.getCliente().getNome());
        return name.isBlank() ? "Cliente" : name;
    }

    private String extractEmail(final Authentication authentication) {
        final Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return normalize(userDetails.getUsername()).toLowerCase(PT_BR);
        }
        if (principal instanceof OAuth2User oauth2User) {
            final Object email = oauth2User.getAttributes().get("email");
            return email == null ? "" : normalize(email.toString()).toLowerCase(PT_BR);
        }
        return "";
    }

    private String normalize(final String value) {
        return value == null ? "" : value.trim();
    }
}
