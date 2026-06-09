// src/main/java/br/com/redemaisfarma/adapters/outbound/messaging/NoopProductImagePublisher.java
package br.com.redemaisfarma.adapters.outbound.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix="kafka", name="enabled", havingValue="false", matchIfMissing=true)
public class NoopProductImagePublisher implements ProductImagePublisher {
    private static final Logger log = LoggerFactory.getLogger(NoopProductImagePublisher.class);
    @Override public void publish(ProductImageRequestedEvent e) {
        log.info("[NO-KAFKA] Simulando publicação ProductImageRequestedEvent key={}", e.productId());
    }
}
