package br.com.lectek.copainsider.adapters.inbound.web.dto;

public record ClienteLookupResponseDTO(
        boolean existe,
        String nome,
        String email
) {
}
