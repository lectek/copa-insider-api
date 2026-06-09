package br.com.redemaisfarma.adapters.outbound.messaging;

public interface ProductImageEventPublisher {
    void publish(ProductImageRequestedEvent event);
}
