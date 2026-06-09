package br.com.redemaisfarma.application.service.delivery;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.redemaisfarma.application.config.AppProps;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.application.view.DeliveryEstimateVM;
import br.com.redemaisfarma.domain.enums.ModoEntrega;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PublicDeliveryEstimateServiceTest {

    private PedidoRepository pedidoRepository;
    private UsuarioRepository usuarioRepository;
    private DeliveryRouteService deliveryRouteService;
    private AppSettingService appSettingService;
    private AppProps appProps;
    private Clock clock;
    private PublicDeliveryEstimateService service;

    @BeforeEach
    void setUp() {
        pedidoRepository = mock(PedidoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        deliveryRouteService = mock(DeliveryRouteService.class);
        appSettingService = mock(AppSettingService.class);
        appProps = mock(AppProps.class);
        clock = Clock.fixed(
                Instant.parse("2026-03-11T08:00:00Z"),
                ZoneOffset.UTC
        );
        service = new PublicDeliveryEstimateService(
                pedidoRepository,
                usuarioRepository,
                deliveryRouteService,
                appSettingService,
                appProps,
                clock
        );
    }

    @Test
    void estimateForRetornaMensagemQuandoUsuarioNaoTemEndereco() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "cliente@example.com",
                "senha"
        );
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setEmail("cliente@example.com");
        when(usuarioRepository.findByEmailOrCpf("cliente@example.com"))
                .thenReturn(Optional.of(usuario));

        DeliveryEstimateVM estimate = service.estimateFor(authentication);

        assertThat(estimate.available()).isFalse();
        assertThat(estimate.summary()).isEqualTo("Veja a entrega no seu CEP");
    }

    @Test
    void estimateForCalculaEtaAPartirDaRotaOtimizada() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "cliente@example.com",
                "senha"
        );
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setEmail("cliente@example.com");
        usuario.setEndereco("Rua do Cliente, 100, Joao Pessoa/PB");
        when(usuarioRepository.findByEmailOrCpf("cliente@example.com"))
                .thenReturn(Optional.of(usuario));

        PedidoEntity pedido = new PedidoEntity();
        pedido.setId(25L);
        pedido.setModoEntrega(ModoEntrega.ENTREGA);
        pedido.setStatus(StatusPedido.PAGO);
        pedido.setData(LocalDateTime.of(2026, 3, 11, 7, 30));
        pedido.setEnderecoEntrega("Rua da Entrega, 50, Joao Pessoa/PB");
        ClienteEntity cliente = new ClienteEntity();
        cliente.setNome("Maria");
        cliente.setEmail("maria@example.com");
        pedido.setCliente(cliente);

        when(pedidoRepository.findByModoEntregaAndStatusInOrderByDataAsc(
                eq(ModoEntrega.ENTREGA),
                eq(List.of(StatusPedido.PAGO, StatusPedido.ENVIADO)),
                eq(PageRequest.of(0, 12))
        )).thenReturn(List.of(pedido));
        when(appSettingService.getInt("entrega.rota.max_paradas", 12)).thenReturn(12);
        when(appSettingService.getInt("entrega.rota.velocidade_media_kmh", 28)).thenReturn(30);
        when(appSettingService.getInt("entrega.rota.minutos_por_parada", 6)).thenReturn(5);
        when(appSettingService.getOrDefault("entrega.rota.horario_saida", "09:00"))
                .thenReturn("09:00");
        when(appProps.getAddressQuery()).thenReturn("Base da Loja, Joao Pessoa/PB");
        when(deliveryRouteService.plan(eq("Base da Loja, Joao Pessoa/PB"), any()))
                .thenReturn(new DeliveryRouteService.PlannedRoute(
                        "Base da Loja, Joao Pessoa/PB",
                        BigDecimal.valueOf(2100.0d),
                        BigDecimal.valueOf(15.0d),
                        List.of(
                                new DeliveryRouteService.DeliveryStopPlan(
                                        1,
                                        25L,
                                        "Maria",
                                        "Rua da Entrega, 50, Joao Pessoa/PB",
                                        "123456",
                                        "PAGO",
                                        BigDecimal.valueOf(6.0d),
                                        BigDecimal.valueOf(6.0d),
                                        -7.0d,
                                        -34.0d,
                                        600L,
                                        600L
                                ),
                                new DeliveryRouteService.DeliveryStopPlan(
                                        2,
                                        Long.MIN_VALUE,
                                        "Seu endereco",
                                        "Rua do Cliente, 100, Joao Pessoa/PB",
                                        "PREVISAO",
                                        "PREVIEW",
                                        BigDecimal.valueOf(9.0d),
                                        BigDecimal.valueOf(15.0d),
                                        -7.1d,
                                        -34.1d,
                                        1200L,
                                        1800L
                                )
                        ),
                        "https://maps.example"
                ));

        DeliveryEstimateVM estimate = service.estimateFor(authentication);

        assertThat(estimate.available()).isTrue();
        assertThat(estimate.summary()).isEqualTo("Entrega hoje por volta de 09:35");
        assertThat(estimate.detail()).isEqualTo("Parada 2 de 2 na rota que sai as 09:00.");
        verify(deliveryRouteService).plan(eq("Base da Loja, Joao Pessoa/PB"), any());
    }
}
