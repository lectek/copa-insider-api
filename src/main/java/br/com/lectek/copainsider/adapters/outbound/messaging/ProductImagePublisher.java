package br.com.lectek.copainsider.adapters.outbound.messaging;

public interface ProductImagePublisher {
    void publish(ProductImageRequestedEvent event);
}
