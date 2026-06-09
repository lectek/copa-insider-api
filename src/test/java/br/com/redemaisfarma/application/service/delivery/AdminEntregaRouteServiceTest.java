package br.com.redemaisfarma.application.service.delivery;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteNotificacaoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EntregaParadaEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EntregaRotaEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteNotificacaoRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.EntregaParadaRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.EntregaRotaRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.redemaisfarma.application.config.AppProps;
import br.com.redemaisfarma.application.service.PaymentMethodService;
import br.com.redemaisfarma.domain.enums.EntregaParadaStatus;
import br.com.redemaisfarma.domain.enums.EntregaRotaStatus;
import br.com.redemaisfarma.domain.enums.ModoEntrega;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import br.com.redemaisfarma.domain.enums.TipoPagamento;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminEntregaRouteServiceTest {

    private PedidoRepository pedidoRepository;
    private UsuarioRepository usuarioRepository;
    private DeliveryRouteService deliveryRouteService;
    private EntregaRotaRepository entregaRotaRepository;
    private EntregaParadaRepository entregaParadaRepository;
    private ClienteNotificacaoRepository notificacaoRepository;
    private PaymentMethodService paymentMethodService;
    private AppProps appProps;
    private AdminEntregaRouteService service;

    @BeforeEach
    void setUp() {
        pedidoRepository = mock(PedidoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        deliveryRouteService = mock(DeliveryRouteService.class);
        entregaRotaRepository = mock(EntregaRotaRepository.class);
        entregaParadaRepository = mock(EntregaParadaRepository.class);
        notificacaoRepository = mock(ClienteNotificacaoRepository.class);
        paymentMethodService = mock(PaymentMethodService.class);
        appProps = mock(AppProps.class);
        service = new AdminEntregaRouteService(
                pedidoRepository,
                usuarioRepository,
                deliveryRouteService,
                entregaRotaRepository,
                entregaParadaRepository,
                notificacaoRepository,
                paymentMethodService,
                appProps
        );
    }

    @Test
    void startRouteAtualizaPrimeiraParadaEStatusDosPedidos() {
        EntregaRotaEntity route = routeEntity(90L, EntregaRotaStatus.PLANEJADA);
        PedidoEntity firstOrder = deliveryOrder(1001L, StatusPedido.PAGO, "pix");
        PedidoEntity secondOrder = deliveryOrder(
                1002L,
                StatusPedido.PRONTO_PARA_ENTREGA,
                "dinheiro"
        );
        EntregaParadaEntity firstStop = stopEntity(
                1L,
                1,
                EntregaParadaStatus.PENDENTE,
                firstOrder,
                "Rua A, 10"
        );
        EntregaParadaEntity secondStop = stopEntity(
                2L,
                2,
                EntregaParadaStatus.PENDENTE,
                secondOrder,
                "Rua B, 20"
        );

        UsuarioEntity rider = new UsuarioEntity();
        rider.setNome("Motoboy 1");
        rider.setEmail("motoboy@example.com");
        rider.setCpf("12345678901");
        rider.setSenha("segredo123");

        when(entregaRotaRepository.findById(90L)).thenReturn(Optional.of(route));
        when(entregaParadaRepository.findByRotaIdWithPedidoOrderByOrdemAsc(90L))
                .thenReturn(List.of(firstStop, secondStop));
        when(usuarioRepository.findByEmailOrCpf("motoboy@example.com"))
                .thenReturn(Optional.of(rider));
        when(usuarioRepository.findByEmailOrCpf("cliente1001@example.com"))
                .thenReturn(Optional.of(userFor("cliente1001@example.com")));
        when(usuarioRepository.findByEmailOrCpf("cliente1002@example.com"))
                .thenReturn(Optional.of(userFor("cliente1002@example.com")));

        AdminEntregaRouteService.RouteDetailView detail =
                service.startRoute(90L, "motoboy@example.com");

        assertThat(route.getStatus()).isEqualTo(EntregaRotaStatus.EM_EXECUCAO);
        assertThat(route.getEntregador()).isEqualTo(rider);
        assertThat(firstStop.getStatus()).isEqualTo(EntregaParadaStatus.A_CAMINHO);
        assertThat(secondStop.getStatus()).isEqualTo(EntregaParadaStatus.PENDENTE);
        assertThat(firstOrder.getStatus()).isEqualTo(StatusPedido.SAIU_PARA_ENTREGA);
        assertThat(secondOrder.getStatus()).isEqualTo(StatusPedido.SAIU_PARA_ENTREGA);
        assertThat(detail.proximaParada()).isNotNull();
        assertThat(detail.proximaParada().id()).isEqualTo(1L);

        verify(entregaRotaRepository).save(route);
        verify(entregaParadaRepository).saveAll(List.of(firstStop, secondStop));
        verify(pedidoRepository).saveAll(List.of(firstOrder, secondOrder));
        verify(notificacaoRepository).saveAll(argThat(items -> {
            List<ClienteNotificacaoEntity> notifications =
                    ((List<ClienteNotificacaoEntity>) items);
            return notifications.size() == 2
                    && notifications.stream()
                    .map(ClienteNotificacaoEntity::getTitulo)
                    .allMatch("Pedido saiu para entrega"::equals);
        }));
    }

    @Test
    void confirmStopRegistraPagamentoEAcionaProximaParada() {
        EntregaRotaEntity route = routeEntity(91L, EntregaRotaStatus.EM_EXECUCAO);
        PedidoEntity firstOrder = deliveryOrder(
                2001L,
                StatusPedido.SAIU_PARA_ENTREGA,
                "dinheiro"
        );
        firstOrder.setCodigoEntrega("123456");
        PedidoEntity secondOrder = deliveryOrder(
                2002L,
                StatusPedido.SAIU_PARA_ENTREGA,
                "pix"
        );

        EntregaParadaEntity firstStop = stopEntity(
                11L,
                1,
                EntregaParadaStatus.CHEGOU,
                firstOrder,
                "Rua C, 30"
        );
        firstStop.setLatitude(BigDecimal.valueOf(-7.120001d));
        firstStop.setLongitude(BigDecimal.valueOf(-34.880001d));
        EntregaParadaEntity secondStop = stopEntity(
                12L,
                2,
                EntregaParadaStatus.PENDENTE,
                secondOrder,
                "Rua D, 40"
        );

        when(entregaRotaRepository.findById(91L)).thenReturn(Optional.of(route));
        when(entregaParadaRepository.findByRotaIdWithPedidoOrderByOrdemAsc(91L))
                .thenReturn(List.of(firstStop, secondStop));
        when(paymentMethodService.isOfflineValue("dinheiro")).thenReturn(true);
        when(paymentMethodService.resolveLabel("pix")).thenReturn("Pix");

        AdminEntregaRouteService.DriverRouteView driverView =
                service.confirmStop(
                        91L,
                        11L,
                        new AdminEntregaRouteService.DeliveryClosureInput(
                                "PIX",
                                4,
                                List.of("RACISMO", "OUTRO"),
                                "Cliente preferiu pagar no pix."
                        )
                );

        assertThat(firstStop.getStatus()).isEqualTo(EntregaParadaStatus.ENTREGUE);
        assertThat(firstStop.getFormaPagamentoRecebida()).isEqualTo("PIX");
        assertThat(firstStop.getPagamentoDivergente()).isTrue();
        assertThat(firstStop.getAvaliacaoEntrega()).isEqualTo(4);
        assertThat(firstStop.getOcorrencias()).isEqualTo("RACISMO,OUTRO");
        assertThat(firstStop.getObservacao()).isEqualTo("Cliente preferiu pagar no pix.");
        assertThat(firstOrder.getStatus()).isEqualTo(StatusPedido.ENTREGUE);
        assertThat(firstOrder.getCodigoEntregaConfirmadoEm()).isNotNull();
        assertThat(secondStop.getStatus()).isEqualTo(EntregaParadaStatus.A_CAMINHO);
        assertThat(route.getStatus()).isEqualTo(EntregaRotaStatus.EM_EXECUCAO);
        assertThat(driverView.proximaParada()).isNotNull();
        assertThat(driverView.proximaParada().id()).isEqualTo(12L);
        assertThat(driverView.proximaParada().googleMapsUrl()).isNotBlank();
        assertThat(driverView.proximaParada().wazeUrl()).isNotBlank();

        verify(pedidoRepository).save(firstOrder);
        verify(entregaParadaRepository).saveAll(List.of(firstStop, secondStop));
        verify(entregaRotaRepository).save(route);
    }

    @Test
    void markStopArrivedAtualizaStatusParaChegou() {
        EntregaRotaEntity route = routeEntity(90L, EntregaRotaStatus.EM_EXECUCAO);
        PedidoEntity order = deliveryOrder(
                2010L,
                StatusPedido.SAIU_PARA_ENTREGA,
                "pix"
        );
        EntregaParadaEntity stop = stopEntity(
                31L,
                1,
                EntregaParadaStatus.A_CAMINHO,
                order,
                "Rua E, 50"
        );

        when(entregaRotaRepository.findById(90L)).thenReturn(Optional.of(route));
        when(entregaParadaRepository.findByRotaIdWithPedidoOrderByOrdemAsc(90L))
                .thenReturn(List.of(stop));
        when(usuarioRepository.findByEmailOrCpf("cliente2010@example.com"))
                .thenReturn(Optional.of(userFor("cliente2010@example.com")));

        AdminEntregaRouteService.DriverStopDetailView detail =
                service.markStopArrived(90L, 31L);

        assertThat(stop.getStatus()).isEqualTo(EntregaParadaStatus.CHEGOU);
        assertThat(detail.podeRegistrarChegada()).isFalse();
        assertThat(detail.podeConfirmarEntrega()).isTrue();
        assertThat(detail.paradaAtual()).isTrue();

        verify(entregaParadaRepository).save(stop);
        verify(notificacaoRepository).save(any(ClienteNotificacaoEntity.class));
    }

    @Test
    void confirmStopBloqueiaQuandoAindaNaoRegistrouChegada() {
        EntregaRotaEntity route = routeEntity(95L, EntregaRotaStatus.EM_EXECUCAO);
        PedidoEntity order = deliveryOrder(
                2011L,
                StatusPedido.SAIU_PARA_ENTREGA,
                "dinheiro"
        );
        EntregaParadaEntity stop = stopEntity(
                41L,
                1,
                EntregaParadaStatus.A_CAMINHO,
                order,
                "Rua F, 60"
        );

        when(entregaRotaRepository.findById(95L)).thenReturn(Optional.of(route));
        when(entregaParadaRepository.findByRotaIdWithPedidoOrderByOrdemAsc(95L))
                .thenReturn(List.of(stop));
        when(paymentMethodService.isOfflineValue("dinheiro")).thenReturn(true);

        assertThatThrownBy(() -> service.confirmStop(
                95L,
                41L,
                new AdminEntregaRouteService.DeliveryClosureInput(
                        "DINHEIRO",
                        5,
                        List.of(),
                        null
                )
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Registre que chegou ao local antes de confirmar");
    }

    @Test
    void registerStopFailureMarcaReagendamentoELiberaProximaParada() {
        EntregaRotaEntity route = routeEntity(96L, EntregaRotaStatus.EM_EXECUCAO);
        PedidoEntity firstOrder = deliveryOrder(
                2012L,
                StatusPedido.SAIU_PARA_ENTREGA,
                "pix"
        );
        PedidoEntity secondOrder = deliveryOrder(
                2013L,
                StatusPedido.SAIU_PARA_ENTREGA,
                "pix"
        );
        EntregaParadaEntity firstStop = stopEntity(
                51L,
                1,
                EntregaParadaStatus.CHEGOU,
                firstOrder,
                "Rua G, 70"
        );
        EntregaParadaEntity secondStop = stopEntity(
                52L,
                2,
                EntregaParadaStatus.PENDENTE,
                secondOrder,
                "Rua H, 80"
        );

        when(entregaRotaRepository.findById(96L)).thenReturn(Optional.of(route));
        when(entregaParadaRepository.findByRotaIdWithPedidoOrderByOrdemAsc(96L))
                .thenReturn(List.of(firstStop, secondStop));
        when(usuarioRepository.findByEmailOrCpf("cliente2012@example.com"))
                .thenReturn(Optional.of(userFor("cliente2012@example.com")));
        when(paymentMethodService.resolveLabel("pix")).thenReturn("Pix");

        AdminEntregaRouteService.DriverRouteView driverView =
                service.registerStopFailure(
                        96L,
                        51L,
                        new AdminEntregaRouteService.DeliveryFailureInput(
                                "REAGENDAR",
                                "AUSENTE",
                                "Cliente pediu nova tentativa."
                        )
                );

        assertThat(firstStop.getStatus()).isEqualTo(EntregaParadaStatus.REAGENDAR);
        assertThat(firstStop.getMotivoFalha()).isEqualTo("AUSENTE");
        assertThat(firstStop.getObservacao()).isEqualTo("Cliente pediu nova tentativa.");
        assertThat(firstOrder.getStatus()).isEqualTo(StatusPedido.PRONTO_PARA_ENTREGA);
        assertThat(secondStop.getStatus()).isEqualTo(EntregaParadaStatus.A_CAMINHO);
        assertThat(driverView.proximaParada()).isNotNull();
        assertThat(driverView.proximaParada().id()).isEqualTo(52L);

        verify(pedidoRepository).save(firstOrder);
        verify(entregaParadaRepository).saveAll(List.of(firstStop, secondStop));
        verify(entregaRotaRepository).save(route);
        verify(notificacaoRepository).save(any(ClienteNotificacaoEntity.class));
    }

    @Test
    void registerStopFailureConcluiRotaQuandoNaoHouverNovaParada() {
        EntregaRotaEntity route = routeEntity(97L, EntregaRotaStatus.EM_EXECUCAO);
        PedidoEntity order = deliveryOrder(
                2014L,
                StatusPedido.SAIU_PARA_ENTREGA,
                "pix"
        );
        EntregaParadaEntity stop = stopEntity(
                61L,
                1,
                EntregaParadaStatus.CHEGOU,
                order,
                "Rua I, 90"
        );

        when(entregaRotaRepository.findById(97L)).thenReturn(Optional.of(route));
        when(entregaParadaRepository.findByRotaIdWithPedidoOrderByOrdemAsc(97L))
                .thenReturn(List.of(stop));
        when(usuarioRepository.findByEmailOrCpf("cliente2014@example.com"))
                .thenReturn(Optional.of(userFor("cliente2014@example.com")));
        when(paymentMethodService.resolveLabel("pix")).thenReturn("Pix");

        AdminEntregaRouteService.DriverRouteView driverView =
                service.registerStopFailure(
                        97L,
                        61L,
                        new AdminEntregaRouteService.DeliveryFailureInput(
                                "TENTATIVA_SEM_SUCESSO",
                                "ENDERECO_RISCO",
                                "Equipe vai revisar a area."
                        )
                );

        assertThat(stop.getStatus()).isEqualTo(EntregaParadaStatus.TENTATIVA_SEM_SUCESSO);
        assertThat(order.getStatus()).isEqualTo(StatusPedido.PRONTO_PARA_ENTREGA);
        assertThat(route.getStatus()).isEqualTo(EntregaRotaStatus.CONCLUIDA);
        assertThat(route.getFinalizadaEm()).isNotNull();
        assertThat(driverView.proximaParada()).isNull();
    }

    @Test
    void getRouteDetailResumeFinanceiroDaRota() {
        EntregaRotaEntity route = routeEntity(98L, EntregaRotaStatus.EM_EXECUCAO);
        PedidoEntity firstOrder = deliveryOrder(
                2015L,
                StatusPedido.ENTREGUE,
                "dinheiro"
        );
        firstOrder.setTotal(BigDecimal.valueOf(30));
        PedidoEntity secondOrder = deliveryOrder(
                2016L,
                StatusPedido.ENTREGUE,
                "dinheiro"
        );
        secondOrder.setTotal(BigDecimal.valueOf(40));
        PedidoEntity thirdOrder = deliveryOrder(
                2017L,
                StatusPedido.PRONTO_PARA_ENTREGA,
                "pix"
        );

        EntregaParadaEntity firstStop = stopEntity(
                71L,
                1,
                EntregaParadaStatus.ENTREGUE,
                firstOrder,
                "Rua J, 100"
        );
        firstStop.setFormaPagamentoRecebida("PIX");
        firstStop.setPagamentoDivergente(true);

        EntregaParadaEntity secondStop = stopEntity(
                72L,
                2,
                EntregaParadaStatus.ENTREGUE,
                secondOrder,
                "Rua K, 110"
        );
        secondStop.setFormaPagamentoRecebida("DINHEIRO");
        secondStop.setPagamentoDivergente(false);

        EntregaParadaEntity thirdStop = stopEntity(
                73L,
                3,
                EntregaParadaStatus.REAGENDAR,
                thirdOrder,
                "Rua L, 120"
        );
        thirdStop.setMotivoFalha("AUSENTE");

        when(entregaRotaRepository.findById(98L)).thenReturn(Optional.of(route));
        when(entregaParadaRepository.findByRotaIdWithPedidoOrderByOrdemAsc(98L))
                .thenReturn(List.of(firstStop, secondStop, thirdStop));
        when(paymentMethodService.isOfflineValue("dinheiro")).thenReturn(true);
        when(paymentMethodService.isOfflineValue("pix")).thenReturn(false);

        AdminEntregaRouteService.RouteDetailView detail = service.getRouteDetail(98L);

        assertThat(detail.financeiro().cobrancasNoLocal()).isEqualTo(2);
        assertThat(detail.financeiro().cobrancasConcluidas()).isEqualTo(2);
        assertThat(detail.financeiro().divergenciasPagamento()).isEqualTo(1);
        assertThat(detail.financeiro().paradasComInsucesso()).isEqualTo(1);
        assertThat(detail.financeiro().valorCobrarNaEntrega()).isEqualByComparingTo("70");
        assertThat(detail.financeiro().valorRecebidoConfirmado()).isEqualByComparingTo("70");
        assertThat(detail.financeiro().valorRecebidoPix()).isEqualByComparingTo("30");
        assertThat(detail.financeiro().valorRecebidoDinheiro()).isEqualByComparingTo("40");
        assertThat(detail.financeiro().valorRecebidoCartao()).isEqualByComparingTo("0");
        assertThat(detail.financeiro().valorRecebidoOutro()).isEqualByComparingTo("0");
    }

    @Test
    void getCustomerTrackingViewMarcaQuandoMotoboyJaChegou() {
        EntregaRotaEntity route = routeEntity(99L, EntregaRotaStatus.EM_EXECUCAO);
        PedidoEntity order = deliveryOrder(
                2018L,
                StatusPedido.SAIU_PARA_ENTREGA,
                "pix"
        );
        EntregaParadaEntity stop = stopEntity(
                81L,
                1,
                EntregaParadaStatus.CHEGOU,
                order,
                "Rua M, 130"
        );
        stop.setRota(route);

        when(entregaParadaRepository.findByPedidoIdInRouteStatuses(eq(2018L), anyList()))
                .thenReturn(List.of(stop));
        when(entregaParadaRepository.findByRotaIdWithPedidoOrderByOrdemAsc(99L))
                .thenReturn(List.of(stop));

        AdminEntregaRouteService.CustomerTrackingView tracking =
                service.getCustomerTrackingView(2018L);

        assertThat(tracking.disponivel()).isTrue();
        assertThat(tracking.pedidoEhProximaParada()).isTrue();
        assertThat(tracking.motoboyChegou()).isTrue();
        assertThat(tracking.etaMinutos()).isZero();
        assertThat(tracking.etaLabel()).isEqualTo("Motoboy no local");
        assertThat(tracking.mensagem()).contains("chegou ao seu endereco");
    }

    @Test
    void getCustomerTrackingViewCalculaEtaEEntregasAntesDaSua() {
        EntregaRotaEntity route = routeEntity(100L, EntregaRotaStatus.EM_EXECUCAO);
        PedidoEntity firstOrder = deliveryOrder(
                2019L,
                StatusPedido.SAIU_PARA_ENTREGA,
                "pix"
        );
        PedidoEntity customerOrder = deliveryOrder(
                2020L,
                StatusPedido.SAIU_PARA_ENTREGA,
                "pix"
        );
        PedidoEntity thirdOrder = deliveryOrder(
                2021L,
                StatusPedido.SAIU_PARA_ENTREGA,
                "pix"
        );

        EntregaParadaEntity firstStop = stopEntity(
                82L,
                1,
                EntregaParadaStatus.A_CAMINHO,
                firstOrder,
                "Rua N, 140"
        );
        EntregaParadaEntity customerStop = stopEntity(
                83L,
                2,
                EntregaParadaStatus.PENDENTE,
                customerOrder,
                "Rua O, 150"
        );
        EntregaParadaEntity thirdStop = stopEntity(
                84L,
                3,
                EntregaParadaStatus.PENDENTE,
                thirdOrder,
                "Rua P, 160"
        );
        firstStop.setRota(route);
        customerStop.setRota(route);
        thirdStop.setRota(route);

        when(entregaParadaRepository.findByPedidoIdInRouteStatuses(eq(2020L), anyList()))
                .thenReturn(List.of(customerStop));
        when(entregaParadaRepository.findByRotaIdWithPedidoOrderByOrdemAsc(100L))
                .thenReturn(List.of(firstStop, customerStop, thirdStop));

        AdminEntregaRouteService.CustomerTrackingView tracking =
                service.getCustomerTrackingView(2020L);

        assertThat(tracking.disponivel()).isTrue();
        assertThat(tracking.pedidoEhProximaParada()).isFalse();
        assertThat(tracking.entregasAntesDaSua()).isEqualTo(1);
        assertThat(tracking.etaMinutos()).isEqualTo(10L);
        assertThat(tracking.etaLabel()).isEqualTo("Cerca de 10 min");
        assertThat(tracking.mensagem()).contains("1 entrega antes da sua");
    }

    @Test
    void getLatestCustomerDeliveryIncidentRetornaUltimaFalhaDaEntrega() {
        PedidoEntity order = deliveryOrder(
                2022L,
                StatusPedido.PRONTO_PARA_ENTREGA,
                "pix"
        );
        EntregaParadaEntity stop = stopEntity(
                85L,
                1,
                EntregaParadaStatus.REAGENDAR,
                order,
                "Rua Q, 170"
        );
        stop.setMotivoFalha("AUSENTE");
        stop.setObservacao("Cliente pediu nova tentativa no fim da tarde.");

        when(entregaParadaRepository.findLatestByPedidoIdAndStatuses(
                eq(2022L),
                anyList(),
                any(org.springframework.data.domain.Pageable.class)
        )).thenReturn(List.of(stop));

        AdminEntregaRouteService.CustomerDeliveryIncidentView incident =
                service.getLatestCustomerDeliveryIncident(2022L);

        assertThat(incident.disponivel()).isTrue();
        assertThat(incident.status()).isEqualTo(EntregaParadaStatus.REAGENDAR.name());
        assertThat(incident.statusLabel()).isEqualTo("Reagendar");
        assertThat(incident.titulo()).isEqualTo("Entrega reagendada");
        assertThat(incident.motivoFalha()).isEqualTo("Cliente ausente");
        assertThat(incident.observacao()).isEqualTo("Cliente pediu nova tentativa no fim da tarde.");
    }

    @Test
    void confirmStopBloqueiaParadaQuandoRotaAindaNaoFoiIniciada() {
        EntregaRotaEntity route = routeEntity(92L, EntregaRotaStatus.PLANEJADA);
        when(entregaRotaRepository.findById(92L)).thenReturn(Optional.of(route));

        assertThatThrownBy(() -> service.confirmStop(
                92L,
                21L,
                new AdminEntregaRouteService.DeliveryClosureInput(
                        "PIX",
                        5,
                        List.of(),
                        null
                )
        ))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Inicie a rota antes de confirmar a parada");
    }

    @Test
    void listDeliveryRequestsExibeTodasAsSolicitacoesEQuilometragemSequencial() {
        PedidoEntity readyOrder = deliveryOrder(
                3001L,
                StatusPedido.PRONTO_PARA_ENTREGA,
                "pix"
        );
        PedidoEntity openOrder = deliveryOrder(
                3002L,
                StatusPedido.ABERTO,
                "dinheiro"
        );

        when(entregaParadaRepository.findPedidoIdsInRouteStatuses(anyList()))
                .thenReturn(List.of(3002L));
        when(pedidoRepository.listarPorModoEntregaExcluindoStatusComCliente(
                org.mockito.ArgumentMatchers.eq(ModoEntrega.ENTREGA),
                anyList(),
                any()
        )).thenReturn(List.of(readyOrder, openOrder));
        when(deliveryRouteService.estimateSequentialDistances(
                org.mockito.ArgumentMatchers.anyString(),
                anyList()
        )).thenReturn(List.of(
                new DeliveryRouteService.LegDistanceEstimate("Base", BigDecimal.valueOf(2.35d)),
                new DeliveryRouteService.LegDistanceEstimate(
                        "Endereco 3001",
                        BigDecimal.valueOf(1.10d)
                )
        ));
        when(appProps.getAddressQuery()).thenReturn("Base da loja");

        List<AdminEntregaRouteService.DeliveryRequestView> rows =
                service.listDeliveryRequests(null);

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).numero()).isEqualTo("3001");
        assertThat(rows.get(0).elegivelParaRota()).isTrue();
        assertThat(rows.get(0).distanciaAnteriorKm()).isEqualByComparingTo("2.35");
        assertThat(rows.get(1).numero()).isEqualTo("3002");
        assertThat(rows.get(1).emRota()).isTrue();
        assertThat(rows.get(1).elegivelParaRota()).isFalse();
        assertThat(rows.get(1).distanciaAnteriorKm()).isEqualByComparingTo("1.10");
    }

    @Test
    void listDeliveryRequestsTrataSaiuParaEntregaSemRotaAtivaComoRoteirizavel() {
        PedidoEntity orphanOrder = deliveryOrder(
                3101L,
                StatusPedido.SAIU_PARA_ENTREGA,
                "pix"
        );
        orphanOrder.setCodigoEntrega("654321");
        PedidoEntity readyOrder = deliveryOrder(
                3102L,
                StatusPedido.PRONTO_PARA_ENTREGA,
                "dinheiro"
        );

        when(entregaParadaRepository.findPedidoIdsInRouteStatuses(anyList()))
                .thenReturn(List.of());
        when(pedidoRepository.listarPorModoEntregaExcluindoStatusComCliente(
                eq(ModoEntrega.ENTREGA),
                anyList(),
                any()
        )).thenReturn(List.of(orphanOrder, readyOrder));
        when(deliveryRouteService.estimateSequentialDistances(
                org.mockito.ArgumentMatchers.anyString(),
                anyList()
        )).thenReturn(List.of(
                new DeliveryRouteService.LegDistanceEstimate("Base", BigDecimal.valueOf(1.20d)),
                new DeliveryRouteService.LegDistanceEstimate("Endereco 3101", BigDecimal.valueOf(0.90d))
        ));
        when(appProps.getAddressQuery()).thenReturn("Base da loja");

        List<AdminEntregaRouteService.DeliveryRequestView> rows =
                service.listDeliveryRequests(null);

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).status()).isEqualTo(StatusPedido.SAIU_PARA_ENTREGA.name());
        assertThat(rows.get(0).elegivelParaRota()).isTrue();
        assertThat(rows.get(0).emRota()).isFalse();
        assertThat(rows.get(1).elegivelParaRota()).isTrue();
    }

    @Test
    void previewRouteRetornaOrdemPlanejadaPeloOtimizador() {
        PedidoEntity firstOrder = deliveryOrder(
                4001L,
                StatusPedido.PRONTO_PARA_ENTREGA,
                "pix"
        );
        firstOrder.setCodigoEntrega("111111");
        PedidoEntity secondOrder = deliveryOrder(
                4002L,
                StatusPedido.PRONTO_PARA_ENTREGA,
                "dinheiro"
        );
        secondOrder.setCodigoEntrega("222222");
        PedidoEntity thirdOrder = deliveryOrder(
                4003L,
                StatusPedido.PRONTO_PARA_ENTREGA,
                "pix"
        );
        thirdOrder.setCodigoEntrega("333333");

        when(pedidoRepository.buscarPorIdsComCliente(List.of(4001L, 4002L, 4003L)))
                .thenReturn(List.of(firstOrder, secondOrder, thirdOrder));
        when(appProps.getAddressQuery()).thenReturn("Base da loja");
        when(deliveryRouteService.plan(eq("Base da loja"), anyList()))
                .thenReturn(new DeliveryRouteService.PlannedRoute(
                        "Base da loja",
                        BigDecimal.valueOf(8.9d),
                        BigDecimal.valueOf(7.4d),
                        List.of(
                                new DeliveryRouteService.DeliveryStopPlan(
                                        1,
                                        4002L,
                                        "Cliente 4002",
                                        "Endereco 4002",
                                        "222222",
                                        StatusPedido.PRONTO_PARA_ENTREGA.name(),
                                        BigDecimal.valueOf(1.9d),
                                        BigDecimal.valueOf(1.9d),
                                        -7.12d,
                                        -34.88d,
                                        420L,
                                        420L
                                ),
                                new DeliveryRouteService.DeliveryStopPlan(
                                        2,
                                        4003L,
                                        "Cliente 4003",
                                        "Endereco 4003",
                                        "333333",
                                        StatusPedido.PRONTO_PARA_ENTREGA.name(),
                                        BigDecimal.valueOf(2.1d),
                                        BigDecimal.valueOf(4.0d),
                                        -7.11d,
                                        -34.86d,
                                        360L,
                                        780L
                                ),
                                new DeliveryRouteService.DeliveryStopPlan(
                                        3,
                                        4001L,
                                        "Cliente 4001",
                                        "Endereco 4001",
                                        "111111",
                                        StatusPedido.PRONTO_PARA_ENTREGA.name(),
                                        BigDecimal.valueOf(3.4d),
                                        BigDecimal.valueOf(7.4d),
                                        -7.13d,
                                        -34.87d,
                                        600L,
                                        1380L
                                )
                        ),
                        "https://maps.example/optimized"
                ));

        AdminEntregaRouteService.PreviewRouteView preview =
                service.previewRoute(List.of(4001L, 4002L, 4003L), null);

        assertThat(preview.getOrigem()).isEqualTo("Base da loja");
        assertThat(preview.getDistanciaTotalKm()).isEqualByComparingTo("7.4");
        assertThat(preview.getParadas())
                .extracting(AdminEntregaRouteService.PreviewStopView::getPedidoId)
                .containsExactly(4002L, 4003L, 4001L);
        assertThat(preview.getParadas().getFirst().getClienteNome()).isEqualTo("Cliente 4002");
        assertThat(preview.getParadas().getFirst().getEnderecoEntrega()).isEqualTo("Endereco 4002");
        assertThat(preview.getParadas().getFirst().getDistanciaAnteriorKm()).isEqualByComparingTo("1.9");
        assertThat(preview.getMapaUrl()).isEqualTo("https://maps.example/optimized");
    }

    @Test
    void previewRouteRejeitaRotaSemDistanciasCalculadas() {
        PedidoEntity firstOrder = deliveryOrder(
                4201L,
                StatusPedido.PRONTO_PARA_ENTREGA,
                "pix"
        );
        firstOrder.setCodigoEntrega("111111");
        PedidoEntity secondOrder = deliveryOrder(
                4202L,
                StatusPedido.PRONTO_PARA_ENTREGA,
                "dinheiro"
        );
        secondOrder.setCodigoEntrega("222222");
        PedidoEntity thirdOrder = deliveryOrder(
                4203L,
                StatusPedido.PRONTO_PARA_ENTREGA,
                "pix"
        );
        thirdOrder.setCodigoEntrega("333333");

        when(pedidoRepository.buscarPorIdsComCliente(List.of(4201L, 4202L, 4203L)))
                .thenReturn(List.of(firstOrder, secondOrder, thirdOrder));
        when(appProps.getAddressQuery()).thenReturn("Base da loja");
        when(deliveryRouteService.plan(eq("Base da loja"), anyList()))
                .thenReturn(new DeliveryRouteService.PlannedRoute(
                        "Base da loja",
                        BigDecimal.valueOf(8.9d),
                        null,
                        List.of(
                                new DeliveryRouteService.DeliveryStopPlan(
                                        1,
                                        4201L,
                                        "Cliente 4201",
                                        "Endereco 4201",
                                        "111111",
                                        StatusPedido.PRONTO_PARA_ENTREGA.name(),
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null
                                ),
                                new DeliveryRouteService.DeliveryStopPlan(
                                        2,
                                        4202L,
                                        "Cliente 4202",
                                        "Endereco 4202",
                                        "222222",
                                        StatusPedido.PRONTO_PARA_ENTREGA.name(),
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null
                                ),
                                new DeliveryRouteService.DeliveryStopPlan(
                                        3,
                                        4203L,
                                        "Cliente 4203",
                                        "Endereco 4203",
                                        "333333",
                                        StatusPedido.PRONTO_PARA_ENTREGA.name(),
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null
                                )
                        ),
                        "https://maps.example/fallback"
                ));

        assertThatThrownBy(() -> service.previewRoute(List.of(4201L, 4202L, 4203L), null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("todas as distancias forem calculadas");
    }

    @Test
    void previewRouteUsaCoordenadasDaOrigemQuandoInformadas() {
        PedidoEntity firstOrder = deliveryOrder(
                4101L,
                StatusPedido.PRONTO_PARA_ENTREGA,
                "pix"
        );
        firstOrder.setCodigoEntrega("111111");
        PedidoEntity secondOrder = deliveryOrder(
                4102L,
                StatusPedido.PRONTO_PARA_ENTREGA,
                "dinheiro"
        );
        secondOrder.setCodigoEntrega("222222");
        PedidoEntity thirdOrder = deliveryOrder(
                4103L,
                StatusPedido.PRONTO_PARA_ENTREGA,
                "pix"
        );
        thirdOrder.setCodigoEntrega("333333");

        when(pedidoRepository.buscarPorIdsComCliente(List.of(4101L, 4102L, 4103L)))
                .thenReturn(List.of(firstOrder, secondOrder, thirdOrder));
        when(deliveryRouteService.planFromOrigin(
                argThat(origin -> origin != null
                        && "Localizacao atual do dispositivo".equals(origin.displayLabel())
                        && origin.hasCoordinates()
                        && origin.latitude().equals(-7.1695d)
                        && origin.longitude().equals(-34.8393d)),
                anyList()))
                .thenReturn(new DeliveryRouteService.PlannedRoute(
                        "Localizacao atual do dispositivo",
                        BigDecimal.valueOf(8.9d),
                        BigDecimal.valueOf(7.4d),
                        List.of(
                                new DeliveryRouteService.DeliveryStopPlan(
                                        1,
                                        4102L,
                                        "Cliente 4102",
                                        "Endereco 4102",
                                        "222222",
                                        StatusPedido.PRONTO_PARA_ENTREGA.name(),
                                        BigDecimal.valueOf(1.9d),
                                        BigDecimal.valueOf(1.9d),
                                        -7.12d,
                                        -34.88d,
                                        null,
                                        null
                                ),
                                new DeliveryRouteService.DeliveryStopPlan(
                                        2,
                                        4103L,
                                        "Cliente 4103",
                                        "Endereco 4103",
                                        "333333",
                                        StatusPedido.PRONTO_PARA_ENTREGA.name(),
                                        BigDecimal.valueOf(2.1d),
                                        BigDecimal.valueOf(4.0d),
                                        -7.11d,
                                        -34.86d,
                                        null,
                                        null
                                ),
                                new DeliveryRouteService.DeliveryStopPlan(
                                        3,
                                        4101L,
                                        "Cliente 4101",
                                        "Endereco 4101",
                                        "111111",
                                        StatusPedido.PRONTO_PARA_ENTREGA.name(),
                                        BigDecimal.valueOf(3.4d),
                                        BigDecimal.valueOf(7.4d),
                                        -7.13d,
                                        -34.87d,
                                        null,
                                        null
                                )
                        ),
                        "https://maps.example/gps"
                ));

        AdminEntregaRouteService.PreviewRouteView preview =
                service.previewRoute(
                        List.of(4101L, 4102L, 4103L),
                        "Localizacao atual do dispositivo",
                        BigDecimal.valueOf(-7.1695d),
                        BigDecimal.valueOf(-34.8393d)
                );

        assertThat(preview.getOrigem()).isEqualTo("Localizacao atual do dispositivo");
        assertThat(preview.getMapaUrl()).isEqualTo("https://maps.example/gps");
    }

    @Test
    void previewRouteBloqueiaQuandoHouverMenosDeTresPedidos() {
        assertThatThrownBy(() -> service.previewRoute(List.of(5001L, 5002L), null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Informe ao menos 3 pedidos para roteirizar");
    }

    private static EntregaRotaEntity routeEntity(
            final Long id,
            final EntregaRotaStatus status
    ) {
        EntregaRotaEntity route = new EntregaRotaEntity();
        ReflectionTestUtils.setField(route, "id", id);
        route.setStatus(status);
        route.setDataOperacao(LocalDate.of(2026, 3, 13));
        route.setOrigem("Base da loja");
        route.setMapaUrl("https://maps.example/route");
        route.setDistanciaTotalKm(BigDecimal.valueOf(6.5d));
        route.setCustoTotal(BigDecimal.valueOf(50));
        return route;
    }

    private static PedidoEntity deliveryOrder(
            final Long id,
            final StatusPedido status,
            final String metodoPagamento
    ) {
        PedidoEntity pedido = new PedidoEntity();
        ReflectionTestUtils.setField(pedido, "id", id);
        pedido.setStatus(status);
        pedido.setData(java.time.LocalDateTime.of(2026, 3, 13, 10, 0));
        pedido.setTotal(BigDecimal.valueOf(25.90d));
        pedido.setModoEntrega(ModoEntrega.ENTREGA);
        pedido.setTipoPagamento(
                "dinheiro".equals(metodoPagamento)
                        ? TipoPagamento.DINHEIRO
                        : TipoPagamento.PIX
        );
        pedido.setMetodoPagamento(metodoPagamento);
        pedido.setEnderecoEntrega("Endereco " + id);
        ClienteEntity cliente = new ClienteEntity();
        cliente.setNome("Cliente " + id);
        cliente.setEmail("cliente" + id + "@example.com");
        pedido.setCliente(cliente);
        return pedido;
    }

    private static EntregaParadaEntity stopEntity(
            final Long id,
            final int ordem,
            final EntregaParadaStatus status,
            final PedidoEntity pedido,
            final String endereco
    ) {
        EntregaParadaEntity stop = new EntregaParadaEntity();
        ReflectionTestUtils.setField(stop, "id", id);
        stop.setPedido(pedido);
        stop.setOrdem(ordem);
        stop.setStatus(status);
        stop.setClienteNomeSnapshot("Cliente parada " + ordem);
        stop.setEnderecoSnapshot(endereco);
        stop.setCodigoEntregaSnapshot("123456");
        stop.setDistanciaAnteriorKm(BigDecimal.ONE);
        stop.setDistanciaAcumuladaKm(BigDecimal.valueOf(ordem));
        stop.setDuracaoAnteriorSegundos(300L);
        stop.setDuracaoAcumuladaSegundos((long) ordem * 300L);
        return stop;
    }

    private static UsuarioEntity userFor(final String email) {
        UsuarioEntity user = new UsuarioEntity();
        user.setNome(email);
        user.setEmail(email);
        user.setCpf("12345678901");
        user.setSenha("segredo123");
        return user;
    }
}
