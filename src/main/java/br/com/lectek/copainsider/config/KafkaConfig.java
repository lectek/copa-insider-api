/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.kafka.annotation.EnableKafka
 *  org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
 */
package br.com.lectek.copainsider.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;

@EnableKafka
@Configuration
@ConditionalOnProperty(prefix = "kafka", name = "enabled", havingValue = "true")
public class KafkaConfig {
    @Bean(name={"pedidoKafkaListenerContainerFactory"})
    public ConcurrentKafkaListenerContainerFactory<?, ?> pedidoKafkaListenerContainerFactory(ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory) {
        return kafkaListenerContainerFactory;
    }
}
