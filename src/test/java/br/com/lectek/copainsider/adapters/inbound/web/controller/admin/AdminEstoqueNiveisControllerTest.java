package br.com.lectek.copainsider.adapters.inbound.web.controller.admin;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import br.com.lectek.copainsider.application.service.otp.OtpServicePort;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = AdminEstoqueNiveisController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminEstoqueNiveisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProdutoRepository produtoRepository;

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
                        "estoque-admin",
                        "N/A",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );
        when(thymeleafViewResolver.resolveViewName(any(), any()))
                .thenReturn(new MappingJackson2JsonView());
        when(appSettingService.getInt("app.estoque.alerta.limite", 2))
                .thenReturn(8);
        when(produtoRepository.findComEstoqueBaixoScoped(isNull(), eq(8))).thenReturn(List.of());
    }

    @AfterEach
    void cleanupAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listarCarregaItensComLimitePadraoNoModel() throws Exception {
        final ProdutoEntity produto = new ProdutoEntity();
        produto.setId(7L);
        produto.setNome("Dipirona");
        produto.setEstoque(5);
        produto.setAlertaEstoqueLimite(null);

        when(produtoRepository.findPaginaNiveisEstoqueScoped(isNull(), eq(8), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(produto), PageRequest.of(0, 100), 1));

        final MvcResult result = mockMvc.perform(get("/admin/estoque/niveis"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/estoque/niveis"))
                .andReturn();

        final Object attr = result.getModelAndView().getModel().get("itens");
        assertNotNull(attr);
        final List<?> itens = (List<?>) attr;
        assertEquals(1, itens.size());
        assertEquals(1L, result.getModelAndView().getModel().get("totalItens"));
        assertEquals(100, result.getModelAndView().getModel().get("tamanhoPagina"));

        final AdminEstoqueNiveisController.EstoqueNivelItemView item =
                (AdminEstoqueNiveisController.EstoqueNivelItemView) itens.get(0);
        assertEquals("Dipirona", item.nome());
        assertEquals(5, item.estoque());
        assertEquals(8, item.minimo());
        assertEquals(true, item.critico());
        assertEquals(false, item.personalizado());
    }

    @Test
    void listarNaoMarcaProdutoZeradoComoCritico() throws Exception {
        final ProdutoEntity produto = new ProdutoEntity();
        produto.setId(9L);
        produto.setNome("Estoque zerado");
        produto.setEstoque(0);

        when(produtoRepository.findPaginaNiveisEstoqueScoped(isNull(), eq(8), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(produto), PageRequest.of(0, 100), 1));

        final MvcResult result = mockMvc.perform(get("/admin/estoque/niveis"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/estoque/niveis"))
                .andReturn();

        final List<?> itens = (List<?>) result.getModelAndView().getModel().get("itens");
        final AdminEstoqueNiveisController.EstoqueNivelItemView item =
                (AdminEstoqueNiveisController.EstoqueNivelItemView) itens.get(0);

        assertEquals(false, item.critico());
    }

    @Test
    void atualizarMinimoSalvaValorCustomizado() throws Exception {
        final ProdutoEntity produto = new ProdutoEntity();
        produto.setId(11L);
        produto.setNome("Ibuprofeno");

        when(produtoRepository.findById(11L)).thenReturn(Optional.of(produto));

        mockMvc.perform(post("/admin/estoque/11/minimo")
                        .param("minimo", "4")
                        .param("page", "2")
                        .param("size", "50"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/estoque/niveis?page=2&size=50"))
                .andExpect(flash().attribute("success", "Limite minimo atualizado para Ibuprofeno: 4."));

        verify(produtoRepository).save(argThat(saved ->
                saved.getId().equals(11L)
                        && Integer.valueOf(4).equals(saved.getAlertaEstoqueLimite())
        ));
    }

    @Test
    void atualizarMinimoSemValorRemoveLimiteCustomizado() throws Exception {
        final ProdutoEntity produto = new ProdutoEntity();
        produto.setId(12L);
        produto.setNome("Paracetamol");
        produto.setAlertaEstoqueLimite(6);

        when(produtoRepository.findById(12L)).thenReturn(Optional.of(produto));

        mockMvc.perform(post("/admin/estoque/12/minimo")
                        .param("minimo", "")
                        .param("page", "1")
                        .param("size", "200"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/estoque/niveis?page=1&size=200"))
                .andExpect(flash().attribute(
                        "success",
                        "Limite personalizado removido para Paracetamol."
                ));

        verify(produtoRepository).save(argThat(saved ->
                saved.getId().equals(12L)
                        && saved.getAlertaEstoqueLimite() == null
        ));
    }
}
