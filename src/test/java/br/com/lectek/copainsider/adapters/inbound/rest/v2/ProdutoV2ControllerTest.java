package br.com.lectek.copainsider.adapters.inbound.rest.v2;

import br.com.lectek.copainsider.application.dto.request.CadastroProdutoRequestDTO;
import br.com.lectek.copainsider.application.service.ProdutoService;
import br.com.lectek.copainsider.domain.Produto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("deprecation")
class ProdutoV2ControllerTest {

    @Mock
    private ProdutoService produtoService;

    @InjectMocks
    private ProdutoV2Controller controller;

    @Test
    void updateUsaServicoUpdateSemCriarProdutoIntermediario() {
        CadastroProdutoRequestDTO dto = new CadastroProdutoRequestDTO();
        dto.setNome("Dipirona");
        dto.setDescricao("500mg");
        dto.setPrecoVenda(BigDecimal.valueOf(9.90));
        dto.setCategoria("MEDICACAO");

        Produto retorno = new Produto();
        retorno.setId(10L);
        retorno.setNome("Dipirona");
        when(produtoService.update(eq(10L), any(Produto.class))).thenReturn(retorno);

        ResponseEntity<Produto> response = controller.update(10L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(10L);

        ArgumentCaptor<Produto> captor = ArgumentCaptor.forClass(Produto.class);
        verify(produtoService).update(eq(10L), captor.capture());
        verify(produtoService, never()).createFromDto(any(CadastroProdutoRequestDTO.class));
        Produto enviado = captor.getValue();
        assertThat(enviado.getId()).isEqualTo(10L);
        assertThat(enviado.getNome()).isEqualTo("Dipirona");
        assertThat(enviado.getPrecoVenda()).isEqualByComparingTo("9.90");
    }

    @Test
    void createUsaServicoCreate() {
        CadastroProdutoRequestDTO dto = new CadastroProdutoRequestDTO();
        dto.setNome("Vitamina C");
        dto.setDescricao("Comprimidos");
        dto.setPrecoVenda(BigDecimal.valueOf(19.90));
        dto.setCategoria("SUPLEMENTO");

        Produto salvo = new Produto();
        salvo.setId(55L);
        salvo.setNome("Vitamina C");
        when(produtoService.create(any(Produto.class))).thenReturn(salvo);

        ResponseEntity<Produto> response = controller.create(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).hasToString("/api/v2/produtos/55");
        verify(produtoService).create(any(Produto.class));
        verify(produtoService, never()).createFromDto(any(CadastroProdutoRequestDTO.class));
    }
}
