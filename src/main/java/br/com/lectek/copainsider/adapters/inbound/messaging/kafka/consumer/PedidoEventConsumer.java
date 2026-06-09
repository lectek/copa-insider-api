/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.kafka.clients.consumer.ConsumerRecord
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 *  org.springframework.kafka.annotation.KafkaListener
 *  org.springframework.messaging.handler.annotation.Payload
 *  org.springframework.stereotype.Component
 */
package br.com.lectek.copainsider.adapters.inbound.messaging.kafka.consumer;

import br.com.lectek.copainsider.adapters.inbound.messaging.kafka.model.PedidoCreatedEventPayLoad;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "kafka", name = "enabled", havingValue = "true")
public class PedidoEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(PedidoEventConsumer.class);

    @KafkaListener(topics={"#{T(br.com.lectek.copainsider.adapters.outbound.messaging.kafka.producer.KafkaTopics).PEDIDO_CREATED}"}, groupId="${app.kafka.groups.pedidos:pedidos-consumer}", containerFactory="pedidoKafkaListenerContainerFactory")
    public void onPedidoCreated(@Payload PedidoCreatedEventPayLoad payload, ConsumerRecord<String, PedidoCreatedEventPayLoad> record) {
        try {
            log.info("\ud83d\udce5 Recebido PedidoCreatedEvent: pedidoId={}, clienteId={}, total={}, status={}, parti\u00e7\u00e3o={}, offset={}", new Object[]{payload.getPedidoId(), payload.getClienteId(), payload.getTotal(), payload.getStatus(), record.partition(), record.offset()});
        }
        catch (Exception e) {
            log.error("Erro ao processar evento de pedido criado. recordKey={}", record.key(), (Object)e);
            throw e;
        }
    }
}
