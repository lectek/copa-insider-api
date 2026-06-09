package br.com.lectek.copainsider.adapters.inbound.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record VendaRapidaItemRequestDTO(
        @NotNull Long produtoId,
        @NotNull @Min(1) Integer quantidade
) {
}
