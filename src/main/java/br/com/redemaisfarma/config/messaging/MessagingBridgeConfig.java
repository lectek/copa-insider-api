package br.com.redemaisfarma.config.messaging;

import br.com.redemaisfarma.adapters.outbound.messaging.ProductImageEventPublisher;
import br.com.redemaisfarma.adapters.outbound.messaging.ProductImagePublisher;
import br.com.redemaisfarma.adapters.outbound.messaging.ProductImageRequestedEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.*;

@Configuration
public class MessagingBridgeConfig {

    @Bean("productImageEventPublisherBridge")
    @ConditionalOnMissingBean(ProductImageEventPublisher.class)
    @ConditionalOnBean(ProductImagePublisher.class)
    public ProductImageEventPublisher productImageEventPublisherBridge(ProductImagePublisher delegate) {
        return new ProductImageEventPublisher() {
            @Override public void publish(ProductImageRequestedEvent event) { delegate.publish(event); }
        };
    }
}
