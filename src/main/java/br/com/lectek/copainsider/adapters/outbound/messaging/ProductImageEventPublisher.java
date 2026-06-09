package br.com.lectek.copainsider.adapters.outbound.messaging;

public interface ProductImageEventPublisher {
    void publish(ProductImageRequestedEvent event);
}
