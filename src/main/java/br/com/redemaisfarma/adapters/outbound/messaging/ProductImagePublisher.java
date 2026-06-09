package br.com.redemaisfarma.adapters.outbound.messaging;

public interface ProductImagePublisher {
    void publish(ProductImageRequestedEvent event);
}
