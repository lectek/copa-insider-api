package br.com.lectek.copainsider.application.service.impl;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.lectek.copainsider.application.core.tenant.TenantResolverService;
import br.com.lectek.copainsider.application.service.fiscal.PedidoFiscalSnapshotService;
import br.com.lectek.copainsider.domain.service.EstoqueService;
import jakarta.persistence.EntityNotFoundException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VendaRapidaServiceImplTest {

    @Mock
    private ClienteRepository clienteRepo;

    @Mock
    private ProdutoRepository produtoRepo;

    @Mock
    private PedidoRepository pedidoRepo;

    @Mock
    private EstoqueService estoque;

    @Mock
    private PedidoFiscalSnapshotService pedidoFiscalSnapshotService;

    @Mock
    private TenantResolverService tenantResolverService;

    @InjectMocks
    private VendaRapidaServiceImpl service;

    @Test
    void criarNaoFazFallbackGlobalPorNomeQuandoTenantEstaResolvido() throws Exception {
        injectTenantResolver();
        when(tenantResolverService.resolveDefaultTenantId()).thenReturn(9L);
        when(clienteRepo.findByEmailIgnoreCase("cliente@embalando.com")).thenReturn(Optional.of(cliente()));
        when(produtoRepo.findByAnyCodigo(9L, "Produto Teste")).thenReturn(Optional.empty());
        when(produtoRepo.findFirstByTenantIdAndNomeContainingIgnoreCase(9L, "Produto Teste"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar("cliente@embalando.com", "Produto Teste", 1))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Produto não encontrado.");

        verify(produtoRepo).findFirstByTenantIdAndNomeContainingIgnoreCase(9L, "Produto Teste");
        verify(produtoRepo, never()).findFirstByNomeContainingIgnoreCase("Produto Teste");
    }

    private void injectTenantResolver() throws Exception {
        Field field = VendaRapidaServiceImpl.class.getDeclaredField("tenantResolverService");
        field.setAccessible(true);
        field.set(service, tenantResolverService);
    }

    private static ClienteEntity cliente() {
        ClienteEntity cliente = new ClienteEntity();
        cliente.setNome("Cliente Teste");
        cliente.setEmail("cliente@embalando.com");
        cliente.setCpf("12345678900");
        return cliente;
    }

    @SuppressWarnings("unused")
    private static ProdutoEntity produto() {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setNome("Produto Teste");
        produto.setEstoque(10);
        produto.setPrecoVenda(BigDecimal.TEN);
        return produto;
    }
}
