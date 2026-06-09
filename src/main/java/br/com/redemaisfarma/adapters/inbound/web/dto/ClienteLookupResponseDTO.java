package br.com.redemaisfarma.adapters.inbound.web.dto;

public record ClienteLookupResponseDTO(
        boolean existe,
        String nome,
        String email
) {
}
