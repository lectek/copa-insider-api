package unit;

import br.com.lectek.copainsider.application.service.PedidoService;
import br.com.lectek.copainsider.domain.Pedido;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    // Usa CALLS_REAL_METHODS para executar os métodos default da interface;
    // os métodos abstratos (findById, list, etc.) serão stubados.
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private PedidoService pedidoService;

    @Mock
    private Pedido pedido;

    private final Long ID = 1L;

    @Test
    void findByIdOptional_quandoEncontrar() {
        // arrange
        when(pedidoService.findById(ID)).thenReturn(pedido);

        // act
        Optional<Pedido> result = pedidoService.findByIdOptional(ID);

        // assert
        assertTrue(result.isPresent());
        assertSame(pedido, result.get());
        verify(pedidoService).findById(ID); // default delega para findById
    }

    @Test
    void findByIdOptional_quandoFindByIdLancaIllegalArgument() {
        // arrange
        when(pedidoService.findById(ID)).thenThrow(new IllegalArgumentException("não encontrado"));

        // act
        Optional<Pedido> result = pedidoService.findByIdOptional(ID);

        // assert
        assertTrue(result.isEmpty());
        verify(pedidoService).findById(ID);
    }

    @Test
    @SuppressWarnings("deprecation")
    void buscarPorId_deveDelegarParaFindById() {
        when(pedidoService.findById(ID)).thenReturn(pedido);

        Pedido r = pedidoService.buscarPorId(ID); // método default (deprecated) da interface

        assertSame(pedido, r);
        verify(pedidoService).findById(ID);
    }

    @Test
    @SuppressWarnings("deprecation")
    void listarTodos_deveDelegarParaList() {
        List<Pedido> lista = List.of(pedido);
        when(pedidoService.list()).thenReturn(lista);

        List<Pedido> r = pedidoService.listarTodos(); // default (deprecated)

        assertEquals(lista, r);
        verify(pedidoService).list();
    }

    @Test
    @SuppressWarnings("deprecation")
    void salvar_deveDelegarParaCreate() {
        when(pedidoService.create(pedido)).thenReturn(pedido);

        Pedido r = pedidoService.salvar(pedido); // default (deprecated)

        assertSame(pedido, r);
        verify(pedidoService).create(pedido);
    }

    @Test
    @SuppressWarnings("deprecation")
    void atualizar_deveDelegarParaUpdate() {
        when(pedidoService.update(ID, pedido)).thenReturn(pedido);

        Pedido r = pedidoService.atualizar(ID, pedido); // default (deprecated)

        assertSame(pedido, r);
        verify(pedidoService).update(ID, pedido);
    }

    @Test
    @SuppressWarnings("deprecation")
    void deletar_deveDelegarParaDelete() {
        // act
        pedidoService.deletar(ID); // default (deprecated)

        // assert
        verify(pedidoService).delete(ID);
    }
}
