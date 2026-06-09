package br.com.redemaisfarma.adapters.inbound.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;

public record VendaRapidaFinalizarRequestDTO(
        @NotEmpty @Valid List<VendaRapidaItemRequestDTO> itens,
        String clienteNome,
        String clienteCpf,
        String clienteEmail,
        String clienteTelefone,
        Boolean criarCliente,
        String pagamentoTipo,
        BigDecimal trocoPara,
        String notaOpcao
) {
}
