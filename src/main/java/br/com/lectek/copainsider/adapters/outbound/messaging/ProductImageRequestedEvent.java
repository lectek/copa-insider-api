package br.com.lectek.copainsider.adapters.outbound.messaging;

public record ProductImageRequestedEvent(
        Long productId,
        String nome,
        String marca,
        String categoria,
        String slug
) {}
