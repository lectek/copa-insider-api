/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer
 *  org.springframework.boot.context.properties.EnableConfigurationProperties
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.data.redis.connection.RedisConnectionFactory
 *  org.springframework.data.redis.connection.RedisPassword
 *  org.springframework.data.redis.connection.RedisStandaloneConfiguration
 *  org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
 *  org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration$LettuceClientConfigurationBuilder
 *  org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
 *  org.springframework.data.redis.core.StringRedisTemplate
 */
package br.com.redemaisfarma.adapters.outbound.cache.config;

import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@EnableConfigurationProperties(value={RedisProperties.class})
@ConditionalOnProperty(prefix="redis", name={"enabled"}, havingValue="true")
public class RedisConfig {
    @Bean
    public RedisStandaloneConfiguration redisStandaloneConfiguration(RedisProperties props) {
        RedisStandaloneConfiguration cfg = new RedisStandaloneConfiguration();
        cfg.setHostName(props.getHost());
        cfg.setPort(props.getPort());
        cfg.setDatabase(props.getDatabase());
        if (props.getPassword() != null && !props.getPassword().isBlank()) {
            cfg.setPassword(RedisPassword.of((String)props.getPassword()));
        }
        return cfg;
    }

    @Bean
    public LettuceClientConfiguration lettuceClientConfiguration(RedisProperties props) {
        LettuceClientConfiguration.LettuceClientConfigurationBuilder b = LettuceClientConfiguration.builder().commandTimeout(Duration.ofMillis(props.getTimeoutMs()));
        if (props.isSsl()) {
            b.useSsl();
        }
        return b.build();
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(RedisStandaloneConfiguration standalone, LettuceClientConfiguration lettuce) {
        return new LettuceConnectionFactory(standalone, lettuce);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory cf) {
        return new StringRedisTemplate(cf);
    }

    @Bean
    public LettuceClientConfigurationBuilderCustomizer noopCustomizer() {
        return builder -> {};
    }
}

