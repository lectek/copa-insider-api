package br.com.lectek.copainsider.adapters.inbound.web.controller;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import br.com.lectek.copainsider.application.service.PaymentMethodService;
import br.com.lectek.copainsider.application.service.delivery.AdminEntregaRouteService;
import br.com.lectek.copainsider.application.service.fiscal.PedidoFiscalPresentationService;
import br.com.lectek.copainsider.application.service.otp.OtpServicePort;
import br.com.lectek.copainsider.domain.financeiro.mercadopago.MercadoPagoCheckoutService;
import br.com.lectek.copainsider.domain.enums.ModoEntrega;
import br.com.lectek.copainsider.domain.enums.StatusPedido;
import br.com.lectek.copainsider.domain.enums.TipoPagamento;
import br.com.lectek.copainsider.domain.fiscal.FiscalDocumentModel;
import br.com.lectek.copainsider.domain.fiscal.FiscalDocumentStatus;
import br.com.lectek.copainsider.domain.fiscal.FiscalPrintChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = ClientePedidosController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClientePedidosControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoRepository pedidoRepository;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private PaymentMethodService paymentMethodService;

    @MockitoBean
    private PedidoFiscalPresentationService pedidoFiscalPresentationService;

    @MockitoBean
    private AdminEntregaRouteService adminEntregaRouteService;

    @MockitoBean
    private MercadoPagoCheckoutService mercadoPagoCheckoutService;

    @MockitoBean
    private AppSettingService appSettingService;

    @MockitoBean
    private OtpServicePort otpServicePort;

    @MockitoBean
    private ThymeleafViewResolver thymeleafViewResolver;
    @BeforeEach
    void setupAuthentication() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("cliente-test", "N/A",
                        List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))));
        when(thymeleafViewResolver.resolveViewName(any(), any()))
                .thenReturn(new MappingJackson2JsonView());
    }

    @AfterEach
    void cleanupAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listarUsaPrincipalQuandoUsuarioNaoEncontrado() throws Exception {
        String principal = "cliente@example.com";

        PedidoEntity pedido = new PedidoEntity();
        pedido.setId(5L);
        pedido.setData(LocalDateTime.now().minusHours(2));
        pedido.setTotal(new BigDecimal("45.90"));
        pedido.setStatus(StatusPedido.PAGO);
        pedido.setTipoPagamento(TipoPagamento.PIX);

        ClienteEntity cliente = new ClienteEntity();
        cliente.setNome("Cliente Teste");
        pedido.setCliente(cliente);

        when(usuarioRepository.findByEmailOrCpf(principal)).thenReturn(Optional.empty());
        when(pedidoRepository.listarPorCliente(principal, principal)).thenReturn(List.of(pedido));

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, "N/A");

        MvcResult result = mockMvc.perform(get("/cliente/pedidos").principal(auth))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/cliente/pedidos/lista"))
                .andReturn();

        Object attr = result.getModelAndView().getModel().get("pedidos");
        assertNotNull(attr);
        List<?> pedidos = (List<?>) attr;
        assertEquals(1, pedidos.size());
    }

    @Test
    void detalheExibeRetiradaNaLojaNoModel() throws Exception {
        String principal = "cliente@example.com";

        PedidoEntity pedido = new PedidoEntity();
        pedido.setId(7L);
        pedido.setData(LocalDateTime.now().minusHours(1));
        pedido.setTotal(new BigDecimal("19.90"));
        pedido.setStatus(StatusPedido.PAGO);
        pedido.setTipoPagamento(TipoPagamento.PIX);
        pedido.setModoEntrega(ModoEntrega.RETIRADA);
        pedido.setCodigoEntrega("123456");

        ClienteEntity cliente = new ClienteEntity();
        cliente.setNome("Cliente Teste");
        cliente.setEmail(principal);
        pedido.setCliente(cliente);

        when(usuarioRepository.findByEmailOrCpf(principal)).thenReturn(Optional.empty());
        when(pedidoRepository.buscarDetalhePorCliente(7L, principal, principal))
                .thenReturn(Optional.of(pedido));
        when(adminEntregaRouteService.getCustomerTrackingView(7L))
                .thenReturn(AdminEntregaRouteService.CustomerTrackingView.unavailable(7L));
        when(adminEntregaRouteService.getLatestCustomerDeliveryIncident(7L))
                .thenReturn(AdminEntregaRouteService.CustomerDeliveryIncidentView.unavailable());
        when(pedidoFiscalPresentationService.build(pedido))
                .thenReturn(new PedidoFiscalPresentationService.PedidoFiscalView(
                        7L,
                        FiscalPrintChannel.IMMEDIATE,
                        "Imprimir na hora",
                        "Via fisica para impressao imediata no caixa ou na retirada.",
                        true,
                        principal,
                        "Envio por e-mail pendente para " + principal + ".",
                        FiscalDocumentModel.NFCE_65,
                        "NFC-e 65",
                        FiscalDocumentStatus.SUBMITTED,
                        "Em processamento",
                        null,
                        null,
                        null,
                        null
                ));

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, "N/A");

        MvcResult result = mockMvc.perform(get("/cliente/pedidos/7").principal(auth))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/cliente/pedidos/detalhe"))
                .andReturn();

        ClientePedidosController.PedidoDetalheView viewModel =
                (ClientePedidosController.PedidoDetalheView) result.getModelAndView()
                        .getModel()
                        .get("pedido");
        PedidoFiscalPresentationService.PedidoFiscalView fiscal =
                (PedidoFiscalPresentationService.PedidoFiscalView) result.getModelAndView()
                        .getModel()
                        .get("fiscal");
        assertNotNull(viewModel);
        assertNotNull(fiscal);
        assertEquals("Retirada na loja", viewModel.modoEntrega());
        assertEquals("Imprimir na hora", fiscal.printChannelLabel());
    }

    @Test
    void detalheConverteAguardandoPagamentoEmPedidoRecebidoQuandoPagamentoEhNaEntrega() throws Exception {
        String principal = "cliente@example.com";

        PedidoEntity pedido = new PedidoEntity();
        pedido.setId(8L);
        pedido.setData(LocalDateTime.now().minusMinutes(30));
        pedido.setTotal(new BigDecimal("22.90"));
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        pedido.setTipoPagamento(TipoPagamento.DINHEIRO);
        pedido.setMetodoPagamento("dinheiro");
        pedido.setModoEntrega(ModoEntrega.ENTREGA);
        pedido.setEnderecoEntrega("Rua das Acacias, 10");

        ClienteEntity cliente = new ClienteEntity();
        cliente.setNome("Cliente Teste");
        cliente.setEmail(principal);
        pedido.setCliente(cliente);

        when(usuarioRepository.findByEmailOrCpf(principal)).thenReturn(Optional.empty());
        when(pedidoRepository.buscarDetalhePorCliente(8L, principal, principal))
                .thenReturn(Optional.of(pedido));
        when(adminEntregaRouteService.getCustomerTrackingView(8L))
                .thenReturn(AdminEntregaRouteService.CustomerTrackingView.unavailable(8L));
        when(adminEntregaRouteService.getLatestCustomerDeliveryIncident(8L))
                .thenReturn(AdminEntregaRouteService.CustomerDeliveryIncidentView.unavailable());
        when(paymentMethodService.isOfflineValue("dinheiro")).thenReturn(true);
        when(paymentMethodService.resolveLabel("dinheiro")).thenReturn("Dinheiro");

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, "N/A");

        MvcResult result = mockMvc.perform(get("/cliente/pedidos/8").principal(auth))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/cliente/pedidos/detalhe"))
                .andReturn();

        ClientePedidosController.PedidoDetalheView viewModel =
                (ClientePedidosController.PedidoDetalheView) result.getModelAndView()
                        .getModel()
                        .get("pedido");
        assertNotNull(viewModel);
        assertEquals("Pedido recebido", viewModel.status());
        assertEquals("Dinheiro", viewModel.metodoPagamento());
        assertNotNull(viewModel.googleMapsUrl());
        assertEquals(4, viewModel.timeline().size());
        assertEquals(
                "Seu pedido foi recebido e esta sendo preparado. O pagamento sera confirmado no recebimento.",
                viewModel.statusMensagem()
        );
    }
}
