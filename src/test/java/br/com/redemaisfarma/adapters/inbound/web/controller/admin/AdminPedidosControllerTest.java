package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EntregaParadaEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EntregaRotaEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoFiscalSnapshotEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteNotificacaoRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.EntregaParadaRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.application.service.PaymentMethodService;
import br.com.redemaisfarma.application.service.fiscal.FiscalOrderEmissionService;
import br.com.redemaisfarma.application.service.fiscal.PedidoFiscalPresentationService;
import br.com.redemaisfarma.application.service.fiscal.PedidoFiscalSnapshotService;
import br.com.redemaisfarma.application.service.otp.OtpServicePort;
import br.com.redemaisfarma.domain.enums.MotivoCancelamentoPedido;
import br.com.redemaisfarma.domain.enums.ModoEntrega;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import br.com.redemaisfarma.domain.enums.TipoPagamento;
import br.com.redemaisfarma.domain.fiscal.FiscalDocumentModel;
import br.com.redemaisfarma.domain.fiscal.FiscalDocumentStatus;
import br.com.redemaisfarma.domain.fiscal.FiscalPrintChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = AdminPedidosController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminPedidosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setupAuthentication() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test-admin", "N/A",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        when(thymeleafViewResolver.resolveViewName(any(), any()))
                .thenReturn(new MappingJackson2JsonView());
        when(pedidoRepository.contarItensPorPedidos(anyList())).thenReturn(List.of());
        when(pedidoFiscalSnapshotService.findByPedidoId(anyLong()))
                .thenReturn(Optional.empty());
        when(entregaParadaRepository.findByPedidoIdInRouteStatuses(anyLong(), anyList()))
                .thenReturn(List.of());
    }

    @AfterEach
    void cleanupAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @MockitoBean
    private PedidoRepository pedidoRepository;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private ClienteNotificacaoRepository clienteNotificacaoRepository;

    @MockitoBean
    private EntregaParadaRepository entregaParadaRepository;

    @MockitoBean
    private PaymentMethodService paymentMethodService;

    @MockitoBean
    private FiscalOrderEmissionService fiscalOrderEmissionService;

    @MockitoBean
    private PedidoFiscalSnapshotService pedidoFiscalSnapshotService;

    @MockitoBean
    private PedidoFiscalPresentationService pedidoFiscalPresentationService;

    @MockitoBean
    private AppSettingService appSettingService;

    @MockitoBean
    private OtpServicePort otpServicePort;

    @MockitoBean
    private ThymeleafViewResolver thymeleafViewResolver;

    @Test
    void listarCarregaPedidosNoModel() throws Exception {
        PedidoEntity pedido = new PedidoEntity();
        pedido.setId(10L);
        pedido.setData(LocalDateTime.now().minusDays(1));
        pedido.setTotal(new BigDecimal("120.50"));
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        pedido.setTipoPagamento(TipoPagamento.PIX);
        pedido.setModoEntrega(ModoEntrega.ENTREGA);
        pedido.setMetodoPagamento("pix");

        ClienteEntity cliente = new ClienteEntity();
        cliente.setNome("Maria Silva");
        cliente.setEmail("maria@example.com");
        cliente.setTelefone("88999990000");
        pedido.setCliente(cliente);

        when(pedidoRepository.listarRecentes(any(Pageable.class))).thenReturn(List.of(pedido));
        when(pedidoRepository.contarItensPorPedidos(anyList()))
                .thenReturn(List.of(itemCountRow(10L, 3L)));
        when(paymentMethodService.resolveLabel("pix")).thenReturn("Pix");

        MvcResult result = mockMvc.perform(get("/admin/pedidos").param("status", "AGUARDANDO"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/pedidos/lista"))
                .andReturn();

        Object attr = result.getModelAndView().getModel().get("pedidos");
        assertNotNull(attr);
        List<?> pedidos = (List<?>) attr;
        assertEquals(1, pedidos.size());

        AdminPedidosController.PedidoListaView viewModel =
                (AdminPedidosController.PedidoListaView) pedidos.get(0);
        assertEquals("Maria Silva", viewModel.clienteNome());
        assertEquals("maria@example.com", viewModel.clienteEmail());
        assertEquals("88999990000", viewModel.clienteTelefone());
        assertEquals("Entrega", viewModel.modoEntrega());
        assertEquals("Pix", viewModel.metodoPagamento());
        assertEquals(3L, viewModel.totalItens());
        assertEquals("AGUARDANDO_PAGAMENTO", viewModel.status());
        assertEquals("Aguardando pagamento", viewModel.statusLabel());
        assertEquals(false, viewModel.canMarkReadyForDelivery());
        assertEquals(false, viewModel.canOpenDeliveryPanel());
    }

    @Test
    void detalheCarregaModoRetiradaNoModel() throws Exception {
        PedidoEntity pedido = new PedidoEntity();
        pedido.setId(10L);
        pedido.setData(LocalDateTime.now().minusDays(1));
        pedido.setTotal(new BigDecimal("120.50"));
        pedido.setStatus(StatusPedido.PAGO);
        pedido.setTipoPagamento(TipoPagamento.PIX);
        pedido.setModoEntrega(ModoEntrega.RETIRADA);
        pedido.setCodigoEntrega("654321");

        ClienteEntity cliente = new ClienteEntity();
        cliente.setNome("Maria Silva");
        cliente.setEmail("maria@example.com");
        pedido.setCliente(cliente);
        PedidoFiscalSnapshotEntity snapshot = new PedidoFiscalSnapshotEntity();
        snapshot.setRecipientName("Maria Silva");
        snapshot.setRecipientEmail("maria@example.com");
        snapshot.setRecipientPhone("88999990000");
        snapshot.setRecipientDocument("12345678901");
        snapshot.setRecipientAddress("Rua das Flores, 100");

        when(pedidoRepository.buscarDetalheAdmin(10L)).thenReturn(java.util.Optional.of(pedido));
        when(pedidoFiscalSnapshotService.findByPedidoId(10L))
                .thenReturn(Optional.of(snapshot));
        when(pedidoFiscalPresentationService.build(pedido))
                .thenReturn(new PedidoFiscalPresentationService.PedidoFiscalView(
                        10L,
                        FiscalPrintChannel.IMMEDIATE,
                        "Imprimir na hora",
                        "Via fisica para impressao imediata no caixa ou na retirada.",
                        true,
                        "maria@example.com",
                        "Envio por e-mail pendente para maria@example.com.",
                        FiscalDocumentModel.NFCE_65,
                        "NFC-e 65",
                        FiscalDocumentStatus.SUBMITTED,
                        "Em processamento",
                        null,
                        null,
                        null,
                        null
                ));

        MvcResult result = mockMvc.perform(get("/admin/pedidos/10"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/pedidos/detalhe"))
                .andReturn();

        AdminPedidosController.PedidoAdminDetalheView viewModel =
                (AdminPedidosController.PedidoAdminDetalheView) result.getModelAndView()
                        .getModel()
                        .get("pedido");
        PedidoFiscalPresentationService.PedidoFiscalView fiscal =
                (PedidoFiscalPresentationService.PedidoFiscalView) result.getModelAndView()
                        .getModel()
                        .get("fiscal");
        assertNotNull(viewModel);
        assertNotNull(fiscal);
        assertEquals("Retirada na loja", viewModel.modoEntrega());
        assertEquals("88999990000", viewModel.clienteTelefone());
        assertEquals("12345678901", viewModel.clienteCpf());
        assertEquals("Imprimir na hora", fiscal.printChannelLabel());
        assertEquals(false, viewModel.canMarkReadyForDelivery());
        assertEquals(false, viewModel.canOpenDeliveryPanel());
    }

    @Test
    void listarExibeAbertoQuandoPagamentoEhNaEntregaMesmoComStatusLegado() throws Exception {
        PedidoEntity pedido = new PedidoEntity();
        pedido.setId(12L);
        pedido.setData(LocalDateTime.now().minusHours(3));
        pedido.setTotal(new BigDecimal("35.40"));
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        pedido.setTipoPagamento(TipoPagamento.DINHEIRO);
        pedido.setModoEntrega(ModoEntrega.ENTREGA);
        pedido.setMetodoPagamento("dinheiro");

        ClienteEntity cliente = new ClienteEntity();
        cliente.setNome("Jose da Entrega");
        cliente.setEmail("jose@example.com");
        pedido.setCliente(cliente);

        when(pedidoRepository.listarRecentes(any(Pageable.class))).thenReturn(List.of(pedido));
        when(pedidoRepository.contarItensPorPedidos(anyList()))
                .thenReturn(List.of(itemCountRow(12L, 1L)));
        when(paymentMethodService.resolveLabel("dinheiro")).thenReturn("Dinheiro");
        when(paymentMethodService.isOfflineValue("dinheiro")).thenReturn(true);

        MvcResult result = mockMvc.perform(get("/admin/pedidos"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/pedidos/lista"))
                .andReturn();

        List<?> pedidos = (List<?>) result.getModelAndView().getModel().get("pedidos");
        assertNotNull(pedidos);
        assertEquals(1, pedidos.size());

        AdminPedidosController.PedidoListaView viewModel =
                (AdminPedidosController.PedidoListaView) pedidos.get(0);
        assertEquals("AGUARDANDO_PAGAMENTO", viewModel.status());
        assertEquals("Aberto", viewModel.statusLabel());
        assertEquals(true, viewModel.canMarkReadyForDelivery());
        assertEquals(false, viewModel.canOpenDeliveryPanel());
    }

    @Test
    void detalheExponeAtalhoParaRotaAtivaQuandoPedidoEstaEmExecucao() throws Exception {
        PedidoEntity pedido = new PedidoEntity();
        pedido.setId(15L);
        pedido.setData(LocalDateTime.now().minusHours(2));
        pedido.setTotal(new BigDecimal("42.10"));
        pedido.setStatus(StatusPedido.SAIU_PARA_ENTREGA);
        pedido.setTipoPagamento(TipoPagamento.PIX);
        pedido.setModoEntrega(ModoEntrega.ENTREGA);

        ClienteEntity cliente = new ClienteEntity();
        cliente.setNome("Cliente em rota");
        pedido.setCliente(cliente);

        EntregaRotaEntity rota = new EntregaRotaEntity();
        org.springframework.test.util.ReflectionTestUtils.setField(rota, "id", 77L);
        EntregaParadaEntity parada = new EntregaParadaEntity();
        parada.setRota(rota);

        when(pedidoRepository.buscarDetalheAdmin(15L)).thenReturn(Optional.of(pedido));
        when(entregaParadaRepository.findByPedidoIdInRouteStatuses(anyLong(), anyList()))
                .thenReturn(List.of(parada));

        MvcResult result = mockMvc.perform(get("/admin/pedidos/15"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/pedidos/detalhe"))
                .andReturn();

        assertEquals(77L, result.getModelAndView().getModel().get("activeRouteId"));

        AdminPedidosController.PedidoAdminDetalheView viewModel =
                (AdminPedidosController.PedidoAdminDetalheView) result.getModelAndView()
                        .getModel()
                        .get("pedido");
        assertNotNull(viewModel);
        assertEquals(false, viewModel.canMarkReadyForDelivery());
        assertEquals(true, viewModel.canOpenDeliveryPanel());
    }

    @Test
    void atualizarStatusNotificaClienteQuandoPedidoFicaProntoParaEntrega() throws Exception {
        PedidoEntity pedido = new PedidoEntity();
        pedido.setId(16L);
        pedido.setData(LocalDateTime.now().minusHours(1));
        pedido.setTotal(new BigDecimal("32.00"));
        pedido.setStatus(StatusPedido.PAGO);
        pedido.setTipoPagamento(TipoPagamento.PIX);
        pedido.setModoEntrega(ModoEntrega.ENTREGA);
        pedido.setCodigoEntrega("123456");

        ClienteEntity cliente = new ClienteEntity();
        cliente.setNome("Entrega pronta");
        cliente.setEmail("entrega@example.com");
        pedido.setCliente(cliente);

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(5L);
        usuario.setEmail("entrega@example.com");

        when(pedidoRepository.buscarDetalheAdmin(16L)).thenReturn(Optional.of(pedido));
        when(usuarioRepository.findByEmailOrCpf("entrega@example.com"))
                .thenReturn(Optional.of(usuario));

        mockMvc.perform(post("/admin/pedidos/16/status")
                        .param("status", "PRONTO_PARA_ENTREGA"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/pedidos/16"))
                .andExpect(flash().attribute(
                        "successMessage",
                        "Status atualizado e cliente avisado que o pedido esta pronto para entrega."
                ));

        verify(clienteNotificacaoRepository).save(any());
    }

    @Test
    void atualizarStatusNotificaSemUsarClienteDoRetornoDoSave() throws Exception {
        PedidoEntity pedido = new PedidoEntity();
        pedido.setId(5L);
        pedido.setData(LocalDateTime.now().minusDays(1));
        pedido.setTotal(new BigDecimal("120.50"));
        pedido.setStatus(StatusPedido.PAGO);
        pedido.setTipoPagamento(TipoPagamento.PIX);
        pedido.setModoEntrega(ModoEntrega.RETIRADA);
        pedido.setCodigoEntrega("654321");

        ClienteEntity clienteCarregado = new ClienteEntity();
        clienteCarregado.setNome("Maria Silva");
        clienteCarregado.setEmail("maria@example.com");
        clienteCarregado.setCpf("12345678901");
        pedido.setCliente(clienteCarregado);

        PedidoEntity retornoSave = new PedidoEntity();
        retornoSave.setId(5L);
        retornoSave.setData(pedido.getData());
        retornoSave.setTotal(pedido.getTotal());
        retornoSave.setStatus(StatusPedido.PRONTO_PARA_RETIRADA);
        retornoSave.setTipoPagamento(TipoPagamento.PIX);
        retornoSave.setModoEntrega(ModoEntrega.RETIRADA);
        retornoSave.setCodigoEntrega("654321");
        retornoSave.setCliente(new ClienteEntity() {
            @Override
            public String getEmail() {
                throw new IllegalStateException("cliente lazy nao deveria ser usado");
            }

            @Override
            public String getCpf() {
                throw new IllegalStateException("cliente lazy nao deveria ser usado");
            }
        });

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(9L);
        usuario.setEmail("maria@example.com");

        when(pedidoRepository.buscarDetalheAdmin(5L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(PedidoEntity.class))).thenReturn(retornoSave);
        when(usuarioRepository.findByEmailOrCpf("maria@example.com"))
                .thenReturn(Optional.of(usuario));

        mockMvc.perform(post("/admin/pedidos/5/status")
                        .param("status", "PRONTO_PARA_RETIRADA"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/pedidos/5"))
                .andExpect(flash().attribute(
                        "successMessage",
                        "Status atualizado e cliente avisado para retirada."
                ));

        verify(clienteNotificacaoRepository).save(any());
    }

    @Test
    void atualizarStatusCancelaPedidoNaoPagoComMotivoObrigatorio() throws Exception {
        PedidoEntity pedido = new PedidoEntity();
        pedido.setId(7L);
        pedido.setData(LocalDateTime.now().minusHours(2));
        pedido.setTotal(new BigDecimal("89.90"));
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        pedido.setTipoPagamento(TipoPagamento.PIX);
        pedido.setModoEntrega(ModoEntrega.ENTREGA);
        pedido.setCliente(new ClienteEntity());

        when(pedidoRepository.buscarDetalheAdmin(7L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(PedidoEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/admin/pedidos/7/status")
                        .param("status", "CANCELADO")
                        .param("cancelReason", "ESTOQUE_MENOR_QUE_1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/pedidos/7"))
                .andExpect(flash().attribute(
                        "successMessage",
                        "Status do pedido atualizado."
                ));

        ArgumentCaptor<PedidoEntity> captor = ArgumentCaptor.forClass(PedidoEntity.class);
        verify(pedidoRepository).save(captor.capture());
        assertEquals(StatusPedido.CANCELADO, captor.getValue().getStatus());
        assertEquals(
                MotivoCancelamentoPedido.ESTOQUE_MENOR_QUE_1,
                captor.getValue().getCancelamentoMotivo()
        );
        assertNotNull(captor.getValue().getCanceladoEm());
    }

    @Test
    void atualizarStatusRecusaCancelamentoSemMotivo() throws Exception {
        PedidoEntity pedido = new PedidoEntity();
        pedido.setId(8L);
        pedido.setData(LocalDateTime.now().minusHours(1));
        pedido.setTotal(new BigDecimal("59.90"));
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        pedido.setTipoPagamento(TipoPagamento.PIX);
        pedido.setModoEntrega(ModoEntrega.ENTREGA);
        pedido.setCliente(new ClienteEntity());

        when(pedidoRepository.buscarDetalheAdmin(8L)).thenReturn(Optional.of(pedido));

        mockMvc.perform(post("/admin/pedidos/8/status")
                        .param("status", "CANCELADO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/pedidos/8"))
                .andExpect(flash().attribute(
                        "errorMessage",
                        "Informe o motivo do cancelamento."
                ));

        verify(pedidoRepository, never()).save(any(PedidoEntity.class));
    }

    @Test
    void atualizarStatusRecusaCancelamentoDePedidoPago() throws Exception {
        PedidoEntity pedido = new PedidoEntity();
        pedido.setId(9L);
        pedido.setData(LocalDateTime.now().minusHours(1));
        pedido.setTotal(new BigDecimal("59.90"));
        pedido.setStatus(StatusPedido.PAGO);
        pedido.setTipoPagamento(TipoPagamento.PIX);
        pedido.setModoEntrega(ModoEntrega.ENTREGA);
        pedido.setCliente(new ClienteEntity());

        when(pedidoRepository.buscarDetalheAdmin(9L)).thenReturn(Optional.of(pedido));

        mockMvc.perform(post("/admin/pedidos/9/status")
                        .param("status", "CANCELADO")
                        .param("cancelReason", "CLIENTE_SOLICITOU"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/pedidos/9"))
                .andExpect(flash().attribute(
                        "errorMessage",
                        "Somente pedidos nao pagos podem ser cancelados pelo admin."
                ));

        verify(pedidoRepository, never()).save(any(PedidoEntity.class));
    }

    private PedidoRepository.PedidoItensCountRow itemCountRow(
            final Long id,
            final Long totalItens
    ) {
        return new PedidoRepository.PedidoItensCountRow() {
            @Override
            public Long getId() {
                return id;
            }

            @Override
            public Long getTotalItens() {
                return totalItens;
            }
        };
    }
}
