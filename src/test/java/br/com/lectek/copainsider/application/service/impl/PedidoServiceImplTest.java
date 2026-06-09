package br.com.lectek.copainsider.application.service.impl;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.PedidoJPARepository;
import br.com.lectek.copainsider.application.mapper.PedidoMapper;
import br.com.lectek.copainsider.application.service.fiscal.FiscalOrderEmissionService;
import br.com.lectek.copainsider.domain.Pedido;
import br.com.lectek.copainsider.domain.enums.StatusPedido;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceImplTest {

    @Mock
    private PedidoJPARepository repo;

    @Mock
    private PedidoMapper mapper;

    @Mock
    private FiscalOrderEmissionService fiscalOrderEmissionService;

    private PedidoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PedidoServiceImpl(repo, mapper, fiscalOrderEmissionService);
    }

    @Test
    void createTriggersFiscalEmissionWhenPedidoIsPaid() {
        final Pedido input = new Pedido();
        final Pedido output = new Pedido();

        final PedidoEntity mapped = new PedidoEntity();
        mapped.setStatus(StatusPedido.PAGO);

        final PedidoEntity saved = new PedidoEntity();
        saved.setId(21L);
        saved.setStatus(StatusPedido.PAGO);

        when(mapper.toEntity(input)).thenReturn(mapped);
        when(repo.save(mapped)).thenReturn(saved);
        when(mapper.toDomain(saved)).thenReturn(output);

        service.create(input);

        verify(fiscalOrderEmissionService)
                .processPaidOrder(21L, "PEDIDO_SERVICE_CREATE");
    }

    @Test
    void updateTriggersFiscalEmissionWhenPedidoBecomesPaid() {
        final Pedido input = new Pedido();
        final Pedido output = new Pedido();

        final PedidoEntity current = new PedidoEntity();
        current.setId(22L);
        current.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);

        final PedidoEntity source = new PedidoEntity();
        source.setStatus(StatusPedido.PAGO);

        when(repo.findById(22L)).thenReturn(Optional.of(current));
        when(mapper.toEntity(input)).thenReturn(source);
        when(repo.save(current)).thenReturn(current);
        when(mapper.toDomain(current)).thenReturn(output);

        service.update(22L, input);

        verify(fiscalOrderEmissionService)
                .processPaidOrder(22L, "PEDIDO_SERVICE_UPDATE");
    }

    @Test
    void updateDoesNotTriggerFiscalEmissionWhenPedidoIsNotPaid() {
        final Pedido input = new Pedido();
        final Pedido output = new Pedido();

        final PedidoEntity current = new PedidoEntity();
        current.setId(23L);
        current.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);

        final PedidoEntity source = new PedidoEntity();
        source.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);

        when(repo.findById(23L)).thenReturn(Optional.of(current));
        when(mapper.toEntity(input)).thenReturn(source);
        when(repo.save(current)).thenReturn(current);
        when(mapper.toDomain(current)).thenReturn(output);

        service.update(23L, input);

        verify(fiscalOrderEmissionService, never())
                .processPaidOrder(23L, "PEDIDO_SERVICE_UPDATE");
    }
}
