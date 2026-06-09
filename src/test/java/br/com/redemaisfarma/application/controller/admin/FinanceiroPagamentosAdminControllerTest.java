package br.com.redemaisfarma.application.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.application.service.PaymentMethodService;
import br.com.redemaisfarma.application.service.otp.OtpServicePort;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import br.com.redemaisfarma.domain.enums.TipoPagamento;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = FinanceiroPagamentosAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class FinanceiroPagamentosAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoRepository pedidoRepository;

    @MockitoBean
    private PaymentMethodService paymentMethodService;

    @MockitoBean
    private AppSettingService appSettingService;

    @MockitoBean
    private OtpServicePort otpServicePort;

    @MockitoBean
    private ThymeleafViewResolver thymeleafViewResolver;

    @BeforeEach
    void setupAuthentication() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "financeiro-admin",
                        "N/A",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );
        when(thymeleafViewResolver.resolveViewName(any(), any()))
                .thenReturn(new MappingJackson2JsonView());
    }

    @AfterEach
    void cleanupAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listarCarregaTransacoesFiltradasNoModel() throws Exception {
        final PedidoEntity pago = new PedidoEntity();
        pago.setId(21L);
        pago.setData(LocalDateTime.now().minusHours(2));
        pago.setTotal(new BigDecimal("150.00"));
        pago.setStatus(StatusPedido.PAGO);
        pago.setTipoPagamento(TipoPagamento.PIX);
        final ClienteEntity clientePago = new ClienteEntity();
        clientePago.setNome("Maria Silva");
        clientePago.setEmail("maria@teste.com");
        pago.setCliente(clientePago);

        final PedidoEntity cancelado = new PedidoEntity();
        cancelado.setId(22L);
        cancelado.setData(LocalDateTime.now().minusHours(1));
        cancelado.setTotal(new BigDecimal("70.00"));
        cancelado.setStatus(StatusPedido.CANCELADO);
        cancelado.setTipoPagamento(TipoPagamento.BOLETO);
        final ClienteEntity clienteCancelado = new ClienteEntity();
        clienteCancelado.setNome("Carlos Lima");
        clienteCancelado.setEmail("carlos@teste.com");
        cancelado.setCliente(clienteCancelado);

        when(pedidoRepository.listarRecentes(any(Pageable.class)))
                .thenReturn(List.of(pago, cancelado));

        final MvcResult result = mockMvc.perform(
                        get("/admin/financeiro/pagamentos")
                                .param("status", "PAGO")
                                .param("q", "maria")
                )
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/financeiro/pagamentos"))
                .andReturn();

        final Object attr = result.getModelAndView().getModel().get("transacoes");
        assertNotNull(attr);
        final List<?> transacoes = (List<?>) attr;
        assertEquals(1, transacoes.size());

        final FinanceiroPagamentosAdminController.PagamentoItemView item =
                (FinanceiroPagamentosAdminController.PagamentoItemView)
                        transacoes.get(0);
        assertEquals(21L, item.pedidoId());
        assertEquals("Maria Silva", item.clienteNome());
        assertEquals("Pago", item.statusLabel());
    }

    @Test
    void listarMontaResumoComRecebidoPendentesECancelados() throws Exception {
        final PedidoEntity pago = new PedidoEntity();
        pago.setId(31L);
        pago.setData(LocalDateTime.now().minusDays(1));
        pago.setTotal(new BigDecimal("100.00"));
        pago.setStatus(StatusPedido.PAGO);
        pago.setTipoPagamento(TipoPagamento.PIX);
        final ClienteEntity clientePago = new ClienteEntity();
        clientePago.setNome("Pago");
        pago.setCliente(clientePago);

        final PedidoEntity pendente = new PedidoEntity();
        pendente.setId(32L);
        pendente.setData(LocalDateTime.now().minusHours(6));
        pendente.setTotal(new BigDecimal("40.00"));
        pendente.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);
        pendente.setTipoPagamento(TipoPagamento.CARTAO_CREDITO);
        final ClienteEntity clientePendente = new ClienteEntity();
        clientePendente.setNome("Pendente");
        pendente.setCliente(clientePendente);

        final PedidoEntity cancelado = new PedidoEntity();
        cancelado.setId(33L);
        cancelado.setData(LocalDateTime.now().minusHours(3));
        cancelado.setTotal(new BigDecimal("20.00"));
        cancelado.setStatus(StatusPedido.CANCELADO);
        cancelado.setTipoPagamento(TipoPagamento.BOLETO);
        final ClienteEntity clienteCancelado = new ClienteEntity();
        clienteCancelado.setNome("Cancelado");
        cancelado.setCliente(clienteCancelado);

        when(pedidoRepository.listarRecentes(any(Pageable.class)))
                .thenReturn(List.of(pago, pendente, cancelado));

        final MvcResult result = mockMvc.perform(get("/admin/financeiro/pagamentos"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/financeiro/pagamentos"))
                .andReturn();

        final FinanceiroPagamentosAdminController.PagamentoResumoView resumo =
                (FinanceiroPagamentosAdminController.PagamentoResumoView)
                        result.getModelAndView().getModel().get("resumo");

        assertNotNull(resumo);
        assertEquals(1L, resumo.pendentes());
        assertEquals(1L, resumo.cancelados());
        assertEquals("R$\u00a0100,00", resumo.recebido());
    }
}
