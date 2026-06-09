package br.com.lectek.copainsider.adapters.outbound.messaging.kafka.producer;

import br.com.lectek.copainsider.adapters.outbound.messaging.kafka.model.PedidoCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnProperty(prefix = "kafka", name = "enabled", havingValue = "true", matchIfMissing = false)
public class PedidoEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PedidoEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PedidoEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publica o evento e aguarda o envio para poder logar metadados (partição/offset).
     * Em caso de falha, lança RuntimeException.
     */
    public void shPedidoCreated(String topic, PedidoCreatedEvent event) {
        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, event);
            // aguarda com timeout razoável para não travar indefinidamente
            SendResult<String, Object> result = future.get(10, TimeUnit.SECONDS);

            if (result != null && result.getRecordMetadata() != null) {
                log.info(
                    "Kafka: enviado 'PedidoCreated' para topic={}, partition={}, offset={}",
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
                );
            } else {
                log.info("Kafka: enviado 'PedidoCreated' para topic={} (sem metadados)", topic);
            }
        } catch (Exception e) {
            log.error("Kafka: falha ao enviar 'PedidoCreated' para topic={}: {}", topic, e.getMessage(), e);
            throw new RuntimeException("Falha ao publicar evento no Kafka", e);
        }
    }

    /**
     * Publica o evento de forma assíncrona, apenas logando sucesso/erro (não lança).
     */
    public void shPedidoCreatedQuiet(String topic, PedidoCreatedEvent event) {
        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, event);
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.warn("Kafka: erro ao enviar 'PedidoCreated' para topic={}: {}", topic, ex.getMessage());
                    return;
                }
                if (result != null && result.getRecordMetadata() != null) {
                    log.debug(
                        "Kafka: enviado 'PedidoCreated' (quiet) topic={}, partition={}, offset={}",
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                    );
                } else {
                    log.debug("Kafka: enviado 'PedidoCreated' (quiet) topic={} (sem metadados)", topic);
                }
            });
        } catch (Exception e) {
            // não propaga erro no modo quiet
            log.warn("Kafka: exceção síncrona ao enfileirar 'PedidoCreated' (quiet) topic={}: {}", topic, e.getMessage());
        }
    }
}