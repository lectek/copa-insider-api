package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoAdminServiceTest {

    @Mock
    private ProdutoRepository repository;

    @InjectMocks
    private ProdutoAdminService service;

    @Test
    void buscarPaginaDeveEncontrarCodigoDeBarrasNormalizadoDoLeitor() {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(15L);
        produto.setNome("Dipirona");
        produto.setCategoria("Medicacoes");
        produto.setCodigoBarras("7891000001112");
        PageRequest pageable = PageRequest.of(0, 20);

        when(repository.findByAnyCodigo("7891000001112")).thenReturn(Optional.of(produto));

        Page<ProdutoEntity> result = service.buscarPagina("78910 0000 1112\r\n", null, pageable);

        assertThat(result.getContent()).containsExactly(produto);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(repository).findByAnyCodigo("7891000001112");
        verify(repository, never()).searchPageByCategoria(eq("78910 0000 1112\r\n"), eq(null), eq(pageable));
    }

    @Test
    void buscarPaginaDeveFazerFallbackParaBuscaGenericaQuandoBarcodeNaoExiste() {
        PageRequest pageable = PageRequest.of(0, 20);
        Page<ProdutoEntity> expected = new PageImpl<>(List.of(), pageable, 0);

        when(repository.findByAnyCodigo("7891000001112")).thenReturn(Optional.empty());
        when(repository.searchAdminPage("7891000001112", null, null, null, 2, pageable)).thenReturn(expected);

        Page<ProdutoEntity> result = service.buscarPagina("78910 0000 1112", null, pageable);

        assertThat(result).isSameAs(expected);
        verify(repository).findByAnyCodigo("7891000001112");
        verify(repository).searchAdminPage("7891000001112", null, null, null, 2, pageable);
    }

    @Test
    void buscarPaginaDeveRespeitarCategoriaNoMatchExatoPorCodigoDeBarras() {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(22L);
        produto.setNome("Vitamina C");
        produto.setCategoria("Suplementos");
        produto.setCodigoBarras("7891234567890");
        PageRequest pageable = PageRequest.of(0, 20);
        Page<ProdutoEntity> expected = new PageImpl<>(List.of(), pageable, 0);

        when(repository.findByAnyCodigo("7891234567890")).thenReturn(Optional.of(produto));
        when(repository.searchAdminPage("7891234567890", "Medicacoes", null, null, 2, pageable)).thenReturn(expected);

        Page<ProdutoEntity> result = service.buscarPagina("7891234567890", "Medicacoes", pageable);

        assertThat(result).isSameAs(expected);
        verify(repository).searchAdminPage("7891234567890", "Medicacoes", null, null, 2, pageable);
    }

    @Test
    void buscarPaginaDeveEncontrarProdutoPeloCodigoOriginalQuandoBarcodeEstiverVazio() {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(31L);
        produto.setNome("Cetoprofeno");
        produto.setCategoria("Medicacoes");
        produto.setCodigoOriginal(78912345678901L);
        PageRequest pageable = PageRequest.of(0, 20);

        when(repository.findByAnyCodigo("78912345678901")).thenReturn(Optional.of(produto));

        Page<ProdutoEntity> result = service.buscarPagina("78912345678901", null, pageable);

        assertThat(result.getContent()).containsExactly(produto);
        verify(repository).findByAnyCodigo("78912345678901");
        verify(repository, never()).searchPageByCategoria("78912345678901", null, pageable);
    }
}
