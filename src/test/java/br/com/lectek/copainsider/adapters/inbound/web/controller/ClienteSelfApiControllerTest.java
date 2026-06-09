package br.com.lectek.copainsider.adapters.inbound.web.controller;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.PedidoJPARepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ClienteFavoritoRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ClienteNotificacaoRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.lectek.copainsider.application.core.media.ImageStorageService;
import br.com.lectek.copainsider.application.service.CartService;
import br.com.lectek.copainsider.application.service.PaymentMethodService;
import br.com.lectek.copainsider.application.service.fiscal.PedidoFiscalSnapshotService;
import br.com.lectek.copainsider.application.service.validation.CartValidationService;
import br.com.lectek.copainsider.application.view.CartItemVM;
import br.com.lectek.copainsider.domain.financeiro.mercadopago.MercadoPagoCheckoutService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClienteSelfApiControllerTest {

    private UsuarioRepository usuarioRepository;
    private PedidoRepository pedidoRepository;
    private PaymentMethodService paymentMethodService;
    private ImageStorageService imageStorageService;
    private CartService cartService;
    private ClienteRepository clienteRepository;
    private PedidoJPARepository pedidoJPARepository;
    private ClienteFavoritoRepository favoritoRepository;
    private ClienteNotificacaoRepository notificacaoRepository;
    private ProdutoRepository produtoRepository;
    private PedidoFiscalSnapshotService pedidoFiscalSnapshotService;
    private MercadoPagoCheckoutService mercadoPagoCheckoutService;
    private ClienteSelfApiController controller;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        pedidoRepository = mock(PedidoRepository.class);
        paymentMethodService = mock(PaymentMethodService.class);
        imageStorageService = mock(ImageStorageService.class);
        cartService = mock(CartService.class);
        clienteRepository = mock(ClienteRepository.class);
        pedidoJPARepository = mock(PedidoJPARepository.class);
        favoritoRepository = mock(ClienteFavoritoRepository.class);
        notificacaoRepository = mock(ClienteNotificacaoRepository.class);
        produtoRepository = mock(ProdutoRepository.class);
        pedidoFiscalSnapshotService = mock(PedidoFiscalSnapshotService.class);
        mercadoPagoCheckoutService = mock(MercadoPagoCheckoutService.class);
        controller = new ClienteSelfApiController(
                usuarioRepository,
                pedidoRepository,
                paymentMethodService,
                imageStorageService,
                cartService,
                clienteRepository,
                pedidoJPARepository,
                favoritoRepository,
                notificacaoRepository,
                produtoRepository,
                pedidoFiscalSnapshotService,
                mercadoPagoCheckoutService
        );
    }

    @Test
    void adicionarRetornaBadRequestQuandoProdutoNaoEstaPublicavel() {
        HttpSession session = mock(HttpSession.class);
        when(cartService.validateAdd(session, 10L, 1))
                .thenReturn(new CartValidationService.CartValidationResult(false, "Produto indisponivel para venda."));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> controller.adicionar(new ClienteSelfApiController.CartItemRequest(10L, 1), session)
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getReason()).isEqualTo("Produto indisponivel para venda.");
        verify(cartService, never()).addItem(any(), anyLong(), anyInt());
    }

    @Test
    void finalizarRetornaBadRequestQuandoCarrinhoTemItensInvalidos() {
        Authentication auth = mock(Authentication.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(auth.getName()).thenReturn("cliente@example.com");
        when(paymentMethodService.isActiveValue("pix")).thenReturn(true);
        ClienteEntity cliente = new ClienteEntity();
        cliente.setEmail("cliente@example.com");
        cliente.setCpf("12345678901");
        when(clienteRepository.findByEmailIgnoreCase("cliente@example.com"))
                .thenReturn(Optional.of(cliente));
        when(clienteRepository.findByCpf("cliente@example.com")).thenReturn(Optional.empty());
        CartItemVM invalid = new CartItemVM(
                10L,
                "Produto Bloqueado",
                "/img/p10.png",
                BigDecimal.TEN,
                1,
                BigDecimal.TEN,
                true,
                "Produto indisponivel para venda.",
                0
        );
        CartService.CartOrderData orderData = new CartService.CartOrderData(List.of(), BigDecimal.ZERO, List.of(invalid));
        when(cartService.buildOrderData(session)).thenReturn(orderData);
        ClienteSelfApiController.CheckoutFinalizarRequest req =
                new ClienteSelfApiController.CheckoutFinalizarRequest(
                        "Cliente Teste",
                        "12345678901",
                        "cliente@example.com",
                        "pix",
                        "Rua A, 100"
                );

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> controller.finalizar(req, auth, request, session)
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getReason()).isEqualTo("Existem itens indisponiveis no carrinho.");
        verify(pedidoJPARepository, never()).save(any());
        verify(cartService, never()).clear(session);
    }

    @Test
    void adicionarFavoritoRetornaNotFoundQuandoProdutoNaoEhPublico() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("cliente@example.com");
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(7L);
        usuario.setEmail("cliente@example.com");
        when(usuarioRepository.findByEmailOrCpf("cliente@example.com")).thenReturn(Optional.of(usuario));
        when(favoritoRepository.existsByUsuarioIdAndProdutoId(7L, 20L)).thenReturn(false);
        when(produtoRepository.findPublicById(20L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> controller.adicionarFavorito(new ClienteSelfApiController.FavoritoRequest(20L), auth)
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getReason()).isEqualTo("Produto nao encontrado ou indisponivel.");
        verify(favoritoRepository, never()).save(any());
    }

    @Test
    void atualizarAvatarRemoveArquivoQuandoSalvarUsuarioFalha() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("cliente@example.com");

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(7L);
        usuario.setEmail("cliente@example.com");
        usuario.setAvatarUrl("/media/users/old.png");
        when(usuarioRepository.findByEmailOrCpf("cliente@example.com")).thenReturn(Optional.of(usuario));
        when(imageStorageService.saveUserAvatar(eq(7L), any()))
                .thenReturn("/media/users/new.png");
        doThrow(new IllegalStateException("db")).when(usuarioRepository).save(any(UsuarioEntity.class));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                MediaType.IMAGE_PNG_VALUE,
                "avatar".getBytes()
        );

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> controller.atualizarAvatar(file, auth)
        );

        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(ex.getReason()).isEqualTo("Falha ao salvar avatar.");
        verify(imageStorageService).deleteUserAvatarByUrl("/media/users/new.png");
    }
}
