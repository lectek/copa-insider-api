package br.com.redemaisfarma.adapters.inbound.messaging.kafka.config;

import br.com.redemaisfarma.adapters.inbound.messaging.kafka.model.PedidoCreatedEventPayLoad;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

@Configuration
@ConditionalOnProperty(prefix = "kafka", name = "enabled", havingValue = "true")
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers:${kafka.bootstrap-servers:localhost:9092}}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, PedidoCreatedEventPayLoad> pedidoConsumerFactory() {
        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class,
                JsonDeserializer.TRUSTED_PACKAGES, "br.com.redemaisfarma.*",
                JsonDeserializer.USE_TYPE_INFO_HEADERS, false,
                JsonDeserializer.VALUE_DEFAULT_TYPE, PedidoCreatedEventPayLoad.class
        );

        // JsonDeserializer tipado e seguro
        JsonDeserializer<PedidoCreatedEventPayLoad> valueDeserializer =
                new JsonDeserializer<>(PedidoCreatedEventPayLoad.class);
        valueDeserializer.addTrustedPackages("br.com.redemaisfarma");
        valueDeserializer.ignoreTypeHeaders(); // vamos usar o default type acima

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                valueDeserializer
        );
    }

    @Bean(name = {"pedidoKafkaListenerContainerFactory", "pedidoListenerContainerFactory"})
    public ConcurrentKafkaListenerContainerFactory<String, PedidoCreatedEventPayLoad>
    pedidoListenerContainerFactory(ConsumerFactory<String, PedidoCreatedEventPayLoad> cf) {

        ConcurrentKafkaListenerContainerFactory<String, PedidoCreatedEventPayLoad> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        return factory;
    }
}
