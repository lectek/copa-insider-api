package br.com.redemaisfarma.adapters.inbound.web.dto;

public record VendaRapidaFinalizarResponseDTO(
        boolean ok,
        String message,
        Long pedidoId,
        String reciboUrl,
        boolean emailEnviado
) {
}
