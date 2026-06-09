/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
 *  io.github.resilience4j.retry.RetryConfig
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 */
package br.com.lectek.copainsider.adapters.outbound.http.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.RetryConfig;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceConfig {
    @Bean
    public CircuitBreakerConfig defaultCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom().failureRateThreshold(50.0f).waitDurationInOpenState(Duration.ofSeconds(30L)).slidingWindowSize(10).build();
    }

    @Bean
    public RetryConfig defaultRetryConfig() {
        return RetryConfig.custom().maxAttempts(3).waitDuration(Duration.ofMillis(500L)).build();
    }
}

