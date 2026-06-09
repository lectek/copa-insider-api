/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.context.annotation.Primary
 */
package br.com.lectek.copainsider.config;

import br.com.lectek.copainsider.adapters.outbound.auth.jwt.store.RefreshTokenJpaStore;
import br.com.lectek.copainsider.adapters.outbound.auth.jwt.store.RefreshTokenStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RefreshTokenStoreConfig {
    @Bean
    @Primary
    public RefreshTokenStore refreshTokenStore(RefreshTokenJpaStore jpaStore) {
        return jpaStore;
    }
}

