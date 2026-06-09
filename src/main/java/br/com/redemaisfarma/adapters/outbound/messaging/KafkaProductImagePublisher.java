package br.com.redemaisfarma.adapters.outbound.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "kafka", name = "enabled", havingValue = "true", matchIfMissing = false)
public class KafkaProductImagePublisher implements ProductImagePublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaProductImagePublisher.class);

    private final KafkaTemplate<String, ProductImageRequestedEvent> kafka;
    private final String topic;

    public KafkaProductImagePublisher(
            KafkaTemplate<String, ProductImageRequestedEvent> kafka,
            @Value("${app.kafka.topics.product-image-requested:product-image-requested}") String topic) {
        this.kafka = kafka;
        this.topic = topic;
    }

    @Override
    public void publish(ProductImageRequestedEvent event) {
        String key = String.valueOf(event.productId());
        kafka.send(topic, key, event).whenComplete((res, ex) -> {
            if (ex != null) {
                log.error("Kafka publish FAIL topic={} key={} msg={}", topic, key, ex.getMessage());
            } else if (res != null && res.getRecordMetadata() != null) {
                var md = res.getRecordMetadata();
                log.info("Kafka publish OK topic={} partition={} offset={} key={}",
                        md.topic(), md.partition(), md.offset(), key);
                if (log.isDebugEnabled()) log.debug("Event payload: {}", event);
            } else {
                log.info("Kafka publish OK topic={} key={} (no metadata)", topic, key);
            }
        });
    }
}

