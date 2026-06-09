package br.com.redemaisfarma.application.controller;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ItemPedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.PedidoJPARepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.redemaisfarma.application.service.CartService;
import br.com.redemaisfarma.application.service.PaymentMethodService;
import br.com.redemaisfarma.application.service.delivery.DeliveryPricingService;
import br.com.redemaisfarma.application.service.fiscal.PedidoFiscalSnapshotService;
import br.com.redemaisfarma.application.view.DeliveryQuoteVM;
import br.com.redemaisfarma.domain.enums.ModoEntrega;
import br.com.redemaisfarma.domain.financeiro.mercadopago.MercadoPagoCheckoutService;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutControllerTest {

    @Mock
    private PaymentMethodService paymentMethodService;

    @Mock
    private CartService cartService;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PedidoJPARepository pedidoRepository;

    @Mock
    private PedidoFiscalSnapshotService pedidoFiscalSnapshotService;

    @Mock
    private Authentication authentication;

    @Mock
    private DeliveryPricingService deliveryPricingService;

    @Mock
    private MercadoPagoCheckoutService mercadoPagoCheckoutService;

    private CheckoutController controller;

    @BeforeEach
    void setUp() {
        controller = new CheckoutController(
                paymentMethodService,
                cartService,
                clienteRepository,
                usuarioRepository,
                pedidoRepository,
                pedidoFiscalSnapshotService,
                deliveryPricingService,
                mercadoPagoCheckoutService
        );
        when(authentication.getName()).thenReturn("cliente@teste.com");
        lenient().when(deliveryPricingService.quoteForAddress(anyString()))
                .thenReturn(deliveryQuote(BigDecimal.ZERO, new BigDecimal("20.00")));
    }

    @Test
    void finalizarPedidoRetornaErroQuandoTrocoNaoInformaValor() {
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        MockHttpSession session = new MockHttpSession();

        when(paymentMethodService.isActiveValue("dinheiro")).thenReturn(true);
        when(paymentMethodService.resolveLabel("dinheiro")).thenReturn("Dinheiro na entrega");
        when(clienteRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(cliente()));
        when(cartService.buildOrderData(any(HttpSession.class))).thenReturn(orderData("39.90"));

        String view = controller.finalizarPedido(
                "Joao da Silva",
                "123.456.789-00",
                "cliente@teste.com",
                "Rua Exemplo, 123",
                "entrega",
                "padrao",
                "dinheiro",
                "sim",
                "",
                null,
                authentication,
                session,
                redirect
        );

        assertThat(view).isEqualTo("redirect:/checkout");
        assertThat(redirect.getFlashAttributes()).containsKey("error");
        assertThat(redirect.getFlashAttributes().get("error"))
                .isEqualTo("Informe quanto sera pago em dinheiro para calcular o troco.");
        verify(pedidoRepository, never()).save(any(PedidoEntity.class));
    }

    @Test
    void finalizarPedidoRetornaErroQuandoTrocoNaoCobreTotal() {
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        MockHttpSession session = new MockHttpSession();

        when(paymentMethodService.isActiveValue("dinheiro")).thenReturn(true);
        when(paymentMethodService.resolveLabel("dinheiro")).thenReturn("Dinheiro na entrega");
        when(clienteRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(cliente()));
        when(cartService.buildOrderData(any(HttpSession.class))).thenReturn(orderData("39.90"));

        String view = controller.finalizarPedido(
                "Joao da Silva",
                "123.456.789-00",
                "cliente@teste.com",
                "Rua Exemplo, 123",
                "entrega",
                "padrao",
                "dinheiro",
                "sim",
                "20,00",
                null,
                authentication,
                session,
                redirect
        );

        assertThat(view).isEqualTo("redirect:/checkout");
        assertThat(redirect.getFlashAttributes()).containsKey("error");
        assertThat(redirect.getFlashAttributes().get("error"))
                .isEqualTo("O valor em dinheiro deve ser maior ou igual ao total do pedido.");
        verify(pedidoRepository, never()).save(any(PedidoEntity.class));
    }

    @Test
    void finalizarPedidoPersisteTrocoQuandoPagamentoEhDinheiro() {
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        MockHttpSession session = new MockHttpSession();
        ArgumentCaptor<PedidoEntity> pedidoCaptor = ArgumentCaptor.forClass(PedidoEntity.class);

        when(paymentMethodService.isActiveValue("dinheiro")).thenReturn(true);
        when(paymentMethodService.resolveLabel("dinheiro")).thenReturn("Dinheiro na entrega");
        when(clienteRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(cliente()));
        when(cartService.buildOrderData(any(HttpSession.class))).thenReturn(orderData("39.90"));
        when(pedidoRepository.save(any(PedidoEntity.class))).thenAnswer(invocation -> {
            PedidoEntity pedido = invocation.getArgument(0);
            pedido.setId(99L);
            return pedido;
        });

        String view = controller.finalizarPedido(
                "Joao da Silva",
                "123.456.789-00",
                "cliente@teste.com",
                "Rua Exemplo, 123",
                "entrega",
                "padrao",
                "dinheiro",
                "sim",
                "100,00",
                null,
                authentication,
                session,
                redirect
        );

        verify(pedidoRepository).save(pedidoCaptor.capture());
        verify(pedidoFiscalSnapshotService).capture(any(PedidoEntity.class), any());
        verify(cartService).clear(session);

        assertThat(view).isEqualTo("redirect:/checkout/confirmacao");
        assertThat(pedidoCaptor.getValue().getMetodoPagamento())
                .contains("Dinheiro na entrega")
                .contains("troco para")
                .contains("100,00");
        assertThat(session.getAttribute("checkoutPaymentValue")).isEqualTo("dinheiro");
        assertThat(session.getAttribute("checkoutPaymentLabel").toString())
                .contains("Dinheiro na entrega")
                .contains("100,00");
    }

    @Test
    void finalizarPedidoPersisteEnderecoInformadoNoCheckout() {
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        MockHttpSession session = new MockHttpSession();
        ArgumentCaptor<PedidoEntity> pedidoCaptor = ArgumentCaptor.forClass(PedidoEntity.class);

        when(paymentMethodService.isActiveValue("credito")).thenReturn(true);
        when(paymentMethodService.resolveLabel("credito")).thenReturn("Cartao de credito");
        when(clienteRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(cliente()));
        when(cartService.buildOrderData(any(HttpSession.class))).thenReturn(orderData("39.90"));
        when(pedidoRepository.save(any(PedidoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String view = controller.finalizarPedido(
                "Joao da Silva",
                "123.456.789-00",
                "cliente@teste.com",
                "Rua Exemplo, 123",
                "entrega",
                "padrao",
                "credito",
                null,
                null,
                null,
                authentication,
                session,
                redirect
        );

        verify(pedidoRepository).save(pedidoCaptor.capture());
        verify(pedidoFiscalSnapshotService).capture(any(PedidoEntity.class), any());

        assertThat(view).isEqualTo("redirect:/checkout/confirmacao");
        assertThat(pedidoCaptor.getValue().getModoEntrega()).isEqualTo(ModoEntrega.ENTREGA);
        assertThat(pedidoCaptor.getValue().getEnderecoEntrega()).isEqualTo("Rua Exemplo, 123");
    }

    @Test
    void finalizarPedidoRetornaErroQuandoEntregaNaoTemEndereco() {
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        MockHttpSession session = new MockHttpSession();

        when(paymentMethodService.isActiveValue("credito")).thenReturn(true);
        when(paymentMethodService.resolveLabel("credito")).thenReturn("Cartao de credito");
        when(clienteRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(cliente()));

        String view = controller.finalizarPedido(
                "Joao da Silva",
                "123.456.789-00",
                "cliente@teste.com",
                "",
                "entrega",
                "padrao",
                "credito",
                null,
                null,
                null,
                authentication,
                session,
                redirect
        );

        assertThat(view).isEqualTo("redirect:/checkout");
        assertThat(redirect.getFlashAttributes().get("error"))
                .isEqualTo("Informe o endereco para entrega.");
        verify(pedidoRepository, never()).save(any(PedidoEntity.class));
    }

    @Test
    void finalizarPedidoRetornaErroQuandoFreteNaoPuderSerCalculado() {
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        MockHttpSession session = new MockHttpSession();

        when(paymentMethodService.isActiveValue("credito")).thenReturn(true);
        when(paymentMethodService.resolveLabel("credito")).thenReturn("Cartao de credito");
        when(clienteRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(cliente()));
        when(deliveryPricingService.quoteForAddress("Rua Exemplo, 123"))
                .thenReturn(DeliveryQuoteVM.unavailable(
                        new BigDecimal("5.00"),
                        new BigDecimal("2.00"),
                        new BigDecimal("20.00"),
                        "Nao foi possivel calcular o frete para esse endereco.",
                        "Revise a rua e tente novamente."
                ));

        String view = controller.finalizarPedido(
                "Joao da Silva",
                "123.456.789-00",
                "cliente@teste.com",
                "Rua Exemplo, 123",
                "entrega",
                "padrao",
                "credito",
                null,
                null,
                null,
                authentication,
                session,
                redirect
        );

        assertThat(view).isEqualTo("redirect:/checkout");
        assertThat(redirect.getFlashAttributes().get("error"))
                .isEqualTo("Nao foi possivel calcular o frete para esse endereco. Revise a rua e tente novamente.");
        verify(pedidoRepository, never()).save(any(PedidoEntity.class));
    }

    @Test
    void finalizarPedidoUsaEnderecoDoUsuarioQuandoCheckoutNaoInformar() {
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        MockHttpSession session = new MockHttpSession();
        ArgumentCaptor<PedidoEntity> pedidoCaptor = ArgumentCaptor.forClass(PedidoEntity.class);

        when(paymentMethodService.isActiveValue("credito")).thenReturn(true);
        when(paymentMethodService.resolveLabel("credito")).thenReturn("Cartao de credito");
        when(clienteRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(cliente()));
        when(usuarioRepository.findByEmailOrCpf(anyString())).thenReturn(Optional.of(usuarioComEndereco()));
        when(cartService.buildOrderData(any(HttpSession.class))).thenReturn(orderData("39.90"));
        when(pedidoRepository.save(any(PedidoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String view = controller.finalizarPedido(
                "Joao da Silva",
                "123.456.789-00",
                "cliente@teste.com",
                "",
                "entrega",
                "padrao",
                "credito",
                null,
                null,
                null,
                authentication,
                session,
                redirect
        );

        verify(pedidoRepository).save(pedidoCaptor.capture());
        verify(pedidoFiscalSnapshotService).capture(any(PedidoEntity.class), any());

        assertThat(view).isEqualTo("redirect:/checkout/confirmacao");
        assertThat(pedidoCaptor.getValue().getModoEntrega()).isEqualTo(ModoEntrega.ENTREGA);
        assertThat(pedidoCaptor.getValue().getEnderecoEntrega()).isEqualTo("Rua do Cadastro, 50");
    }

    @Test
    void finalizarPedidoRetiradaNaoPersisteEndereco() {
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        MockHttpSession session = new MockHttpSession();
        ArgumentCaptor<PedidoEntity> pedidoCaptor = ArgumentCaptor.forClass(PedidoEntity.class);

        when(paymentMethodService.isActiveValue("pix")).thenReturn(true);
        when(paymentMethodService.resolveLabel("pix")).thenReturn("PIX");
        when(clienteRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(cliente()));
        when(cartService.buildOrderData(any(HttpSession.class))).thenReturn(orderData("39.90"));
        when(pedidoRepository.save(any(PedidoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String view = controller.finalizarPedido(
                null,
                null,
                null,
                "Rua Exemplo, 123",
                "retirada",
                null,
                "pix",
                null,
                null,
                null,
                authentication,
                session,
                redirect
        );

        verify(pedidoRepository).save(pedidoCaptor.capture());
        verify(pedidoFiscalSnapshotService).capture(any(PedidoEntity.class), any());

        assertThat(view).isEqualTo("redirect:/checkout/confirmacao");
        assertThat(pedidoCaptor.getValue().getModoEntrega()).isEqualTo(ModoEntrega.RETIRADA);
        assertThat(pedidoCaptor.getValue().getEnderecoEntrega()).isNull();
        assertThat(session.getAttribute("checkoutFulfillmentMode")).isEqualTo("retirada");
        assertThat(session.getAttribute("checkoutFulfillmentLabel")).isEqualTo("Retirada na loja");
    }

    @Test
    void finalizarPedidoComFretePrioritarioSomaVinteReaisAoTotal() {
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        MockHttpSession session = new MockHttpSession();
        ArgumentCaptor<PedidoEntity> pedidoCaptor = ArgumentCaptor.forClass(PedidoEntity.class);

        when(paymentMethodService.isActiveValue("pix")).thenReturn(true);
        when(paymentMethodService.resolveLabel("pix")).thenReturn("PIX");
        when(clienteRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(cliente()));
        when(cartService.buildOrderData(any(HttpSession.class))).thenReturn(orderData("39.90"));
        when(pedidoRepository.save(any(PedidoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String view = controller.finalizarPedido(
                null,
                null,
                null,
                "Rua Exemplo, 123",
                "entrega",
                "prioritario",
                "pix",
                null,
                null,
                null,
                authentication,
                session,
                redirect
        );

        verify(pedidoRepository).save(pedidoCaptor.capture());
        verify(pedidoFiscalSnapshotService).capture(any(PedidoEntity.class), any());

        assertThat(view).isEqualTo("redirect:/checkout/confirmacao");
        assertThat(pedidoCaptor.getValue().getTotal()).isEqualByComparingTo("59.90");
        assertThat(session.getAttribute("checkoutShippingLabel")).isEqualTo("Frete prioritario");
        assertThat((BigDecimal) session.getAttribute("checkoutShippingValue"))
                .isEqualByComparingTo(BigDecimal.valueOf(20L));
        assertThat((BigDecimal) session.getAttribute("checkoutOrderTotal"))
                .isEqualByComparingTo(new BigDecimal("59.90"));
    }

    @Test
    void finalizarPedidoCalculaFreteComExcedenteDeQuilometragem() {
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        MockHttpSession session = new MockHttpSession();
        ArgumentCaptor<PedidoEntity> pedidoCaptor = ArgumentCaptor.forClass(PedidoEntity.class);

        when(paymentMethodService.isActiveValue("pix")).thenReturn(true);
        when(paymentMethodService.resolveLabel("pix")).thenReturn("PIX");
        when(clienteRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(cliente()));
        when(cartService.buildOrderData(any(HttpSession.class))).thenReturn(orderData("39.90"));
        when(deliveryPricingService.quoteForAddress("Rua Exemplo, 123"))
                .thenReturn(deliveryQuote(new BigDecimal("4.50"), new BigDecimal("24.50")));
        when(pedidoRepository.save(any(PedidoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String view = controller.finalizarPedido(
                null,
                null,
                null,
                "Rua Exemplo, 123",
                "entrega",
                "padrao",
                "pix",
                null,
                null,
                null,
                authentication,
                session,
                redirect
        );

        verify(pedidoRepository).save(pedidoCaptor.capture());

        assertThat(view).isEqualTo("redirect:/checkout/confirmacao");
        assertThat(pedidoCaptor.getValue().getTotal()).isEqualByComparingTo("44.40");
        assertThat((BigDecimal) session.getAttribute("checkoutShippingValue"))
                .isEqualByComparingTo("4.50");
        assertThat((BigDecimal) session.getAttribute("checkoutOrderTotal"))
                .isEqualByComparingTo("44.40");
    }

    @Test
    void finalizarPedidoCartaoSemDadosObrigatoriosRetornaErro() {
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        MockHttpSession session = new MockHttpSession();

        ClienteEntity clienteSemDados = new ClienteEntity();
        clienteSemDados.setNome("");
        clienteSemDados.setEmail("");
        clienteSemDados.setCpf("");

        when(paymentMethodService.isActiveValue("credito")).thenReturn(true);
        when(paymentMethodService.resolveLabel("credito")).thenReturn("Cartao de credito");
        when(clienteRepository.findByEmailIgnoreCase(anyString()))
                .thenReturn(Optional.of(clienteSemDados));

        String view = controller.finalizarPedido(
                "",
                "",
                "",
                "Rua Exemplo, 123",
                "entrega",
                "padrao",
                "credito",
                null,
                null,
                null,
                authentication,
                session,
                redirect
        );

        assertThat(view).isEqualTo("redirect:/checkout");
        assertThat(redirect.getFlashAttributes().get("error"))
                .isEqualTo("Preencha nome, CPF e e-mail para pagamentos em cartao.");
        verify(pedidoRepository, never()).save(any(PedidoEntity.class));
    }

    @Test
    void finalizarPedidoPropagaTenantIdParaCheckoutOnline() {
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        MockHttpSession session = new MockHttpSession();
        ArgumentCaptor<MercadoPagoCheckoutService.CheckoutRequest> readyCaptor =
                ArgumentCaptor.forClass(MercadoPagoCheckoutService.CheckoutRequest.class);
        ArgumentCaptor<MercadoPagoCheckoutService.CheckoutRequest> checkoutCaptor =
                ArgumentCaptor.forClass(MercadoPagoCheckoutService.CheckoutRequest.class);

        when(paymentMethodService.isActiveValue("pix")).thenReturn(true);
        when(paymentMethodService.resolveLabel("pix")).thenReturn("PIX");
        when(clienteRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(cliente()));
        when(cartService.buildOrderData(any(HttpSession.class))).thenReturn(orderData("39.90"));
        when(pedidoRepository.save(any(PedidoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String view = controller.finalizarPedido(
                null,
                null,
                null,
                "Rua Exemplo, 123",
                "entrega",
                "padrao",
                "pix",
                null,
                null,
                "tenant-centro",
                authentication,
                session,
                redirect
        );

        verify(mercadoPagoCheckoutService)
                .assertReadyForOnlineCheckout(readyCaptor.capture());
        verify(mercadoPagoCheckoutService)
                .createCheckoutForPedido(any(PedidoEntity.class), checkoutCaptor.capture());

        assertThat(view).isEqualTo("redirect:/checkout/confirmacao");
        assertThat(readyCaptor.getValue().tenantId()).isEqualTo("tenant-centro");
        assertThat(checkoutCaptor.getValue().tenantId()).isEqualTo("tenant-centro");
        assertThat(session.getAttribute("tenantContextId")).isEqualTo("tenant-centro");
        assertThat(redirect.getFlashAttributes().get("checkoutInputTenantId"))
                .isEqualTo("tenant-centro");
    }

    @Test
    void finalizarPedidoUsaTenantIdDaSessaoQuandoPostNaoInformar() {
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("tenantContextId", "tenant-sessao");
        ArgumentCaptor<MercadoPagoCheckoutService.CheckoutRequest> readyCaptor =
                ArgumentCaptor.forClass(MercadoPagoCheckoutService.CheckoutRequest.class);
        ArgumentCaptor<MercadoPagoCheckoutService.CheckoutRequest> checkoutCaptor =
                ArgumentCaptor.forClass(MercadoPagoCheckoutService.CheckoutRequest.class);

        when(paymentMethodService.isActiveValue("pix")).thenReturn(true);
        when(paymentMethodService.resolveLabel("pix")).thenReturn("PIX");
        when(clienteRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(cliente()));
        when(cartService.buildOrderData(any(HttpSession.class))).thenReturn(orderData("39.90"));
        when(pedidoRepository.save(any(PedidoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String view = controller.finalizarPedido(
                null,
                null,
                null,
                "Rua Exemplo, 123",
                "entrega",
                "padrao",
                "pix",
                null,
                null,
                null,
                authentication,
                session,
                redirect
        );

        verify(mercadoPagoCheckoutService)
                .assertReadyForOnlineCheckout(readyCaptor.capture());
        verify(mercadoPagoCheckoutService)
                .createCheckoutForPedido(any(PedidoEntity.class), checkoutCaptor.capture());

        assertThat(view).isEqualTo("redirect:/checkout/confirmacao");
        assertThat(readyCaptor.getValue().tenantId()).isEqualTo("tenant-sessao");
        assertThat(checkoutCaptor.getValue().tenantId()).isEqualTo("tenant-sessao");
        assertThat(redirect.getFlashAttributes().get("checkoutInputTenantId"))
                .isEqualTo("tenant-sessao");
    }

    private ClienteEntity cliente() {
        ClienteEntity cliente = new ClienteEntity();
        cliente.setNome("Joao da Silva");
        cliente.setEmail("cliente@teste.com");
        cliente.setCpf("12345678900");
        return cliente;
    }

    private UsuarioEntity usuarioComEndereco() {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setEndereco("Rua do Cadastro, 50");
        return usuario;
    }

    private CartService.CartOrderData orderData(String total) {
        return new CartService.CartOrderData(
                List.of(new ItemPedidoEntity()),
                new BigDecimal(total),
                List.of()
        );
    }

    private DeliveryQuoteVM deliveryQuote(
            final BigDecimal standardAmount,
            final BigDecimal priorityAmount
    ) {
        return DeliveryQuoteVM.available(
                "Rua Exemplo, 123",
                new BigDecimal("7.25"),
                new BigDecimal("5.00"),
                new BigDecimal("2.25"),
                new BigDecimal("2.00"),
                standardAmount,
                priorityAmount.subtract(standardAmount),
                priorityAmount,
                "Frete calculado",
                "Ate 5 km gratis; depois R$ 2,00 por km."
        );
    }
}
