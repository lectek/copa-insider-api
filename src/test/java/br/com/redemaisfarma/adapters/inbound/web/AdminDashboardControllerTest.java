package br.com.redemaisfarma.adapters.inbound.web;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.application.dto.response.PainelAdminResponseDTO;
import br.com.redemaisfarma.application.service.AdminMetricsService;
import br.com.redemaisfarma.application.service.MetaVendaDashboardService;
import br.com.redemaisfarma.application.service.otp.OtpServicePort;
import br.com.redemaisfarma.domain.enums.ModoEntrega;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import br.com.redemaisfarma.domain.enums.TipoPagamento;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = AdminDashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminMetricsService adminMetricsService;

    @MockitoBean
    private MetaVendaDashboardService metaVendaDashboardService;

    @MockitoBean
    private ObjectProvider<PedidoRepository> pedidoRepositoryProvider;

    @MockitoBean
    private PedidoRepository pedidoRepository;

    @MockitoBean
    private AppSettingService appSettingService;

    @MockitoBean
    private OtpServicePort otpServicePort;

    @MockitoBean
    private ThymeleafViewResolver thymeleafViewResolver;

    @BeforeEach
    void setUp() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "test-admin",
                        "N/A",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );
        when(thymeleafViewResolver.resolveViewName(any(), any()))
                .thenReturn(new MappingJackson2JsonView());
        when(adminMetricsService.montarPainel()).thenReturn(buildPainel());
        when(metaVendaDashboardService.carregarPainel(any(LocalDate.class)))
                .thenReturn(buildMetaVendaPainel());
        when(pedidoRepositoryProvider.getIfAvailable()).thenReturn(pedidoRepository);
        when(pedidoRepository.listarRecentes(nullable(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(buildPedido(15L, "Maria Silva")));
        when(pedidoRepository.contarItensPorPedidos(anyList()))
                .thenReturn(List.of(itemCountRow(15L, 3L)));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void dashboardCarregaUltimosPedidosNoInicioDaPagina() throws Exception {
        MvcResult result = mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/dashboard"))
                .andReturn();

        Object attr = result.getModelAndView().getModel().get("ultimosPedidos");
        assertNotNull(attr);
        List<?> pedidos = (List<?>) attr;
        assertEquals(1, pedidos.size());
        assertNotNull(result.getModelAndView().getModel().get("metaVendaPainel"));
    }

    @Test
    void indexAdminUsaMesmoResumoDePedidosDoDashboard() throws Exception {
        MvcResult result = mockMvc.perform(get("/admin/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/index"))
                .andReturn();

        Object attr = result.getModelAndView().getModel().get("ultimosPedidos");
        assertNotNull(attr);
        List<?> pedidos = (List<?>) attr;
        assertEquals(1, pedidos.size());
        assertNotNull(result.getModelAndView().getModel().get("metaVendaPainel"));
    }

    @Test
    void atualizarMetaDiariaRedirecionaComMensagemDeSucesso() throws Exception {
        when(metaVendaDashboardService.atualizarMetaDiaria("2500.00"))
                .thenReturn(new BigDecimal("2500.00"));

        mockMvc.perform(
                        post("/admin/dashboard/meta-diaria")
                                .param("valor", "2500.00")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"))
                .andExpect(flash().attributeExists("success"));
    }

    @Test
    void atualizarMetaDiariaPermiteRetornoParaIndex() throws Exception {
        when(metaVendaDashboardService.atualizarMetaDiaria("1800.00"))
                .thenReturn(new BigDecimal("1800.00"));

        mockMvc.perform(
                        post("/admin/dashboard/meta-diaria")
                                .param("valor", "1800.00")
                                .param("returnTo", "index")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/index"));
    }

    private PainelAdminResponseDTO buildPainel() {
        PainelAdminResponseDTO painel = new PainelAdminResponseDTO();
        painel.setQtdProdutos(7L);
        painel.setTotalPedidos(15L);
        painel.setClientesAtivos(4L);
        painel.setQtdPedidosPendentes(2L);
        EnumMap<StatusPedido, Long> porStatus = new EnumMap<>(StatusPedido.class);
        porStatus.put(StatusPedido.AGUARDANDO_PAGAMENTO, 2L);
        porStatus.put(StatusPedido.PAGO, 5L);
        painel.setPedidosPorStatus(porStatus);
        return painel;
    }

    private MetaVendaDashboardService.MetaVendaPainel buildMetaVendaPainel() {
        return new MetaVendaDashboardService.MetaVendaPainel(
                new BigDecimal("2000.00"),
                new BigDecimal("1450.00"),
                new BigDecimal("550.00"),
                false,
                "Quase la.",
                "Faltou R$ 550,00 para bater a meta diaria.",
                "badge--warning",
                "Meta em andamento",
                "Quase la, faltou R$ 550,00 para bater a meta do dia.",
                List.of()
        );
    }

    private PedidoEntity buildPedido(final Long id, final String nomeCliente) {
        PedidoEntity pedido = new PedidoEntity();
        pedido.setId(id);
        pedido.setData(LocalDateTime.now().minusMinutes(30));
        pedido.setTotal(new BigDecimal("129.90"));
        pedido.setStatus(StatusPedido.PAGO);
        pedido.setTipoPagamento(TipoPagamento.PIX);
        pedido.setModoEntrega(ModoEntrega.ENTREGA);
        ClienteEntity cliente = new ClienteEntity();
        cliente.setNome(nomeCliente);
        pedido.setCliente(cliente);
        return pedido;
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
