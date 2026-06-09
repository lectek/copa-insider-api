/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
 *  org.springframework.context.annotation.Bean
 *  org.springframework.context.annotation.Configuration
 *  org.springframework.context.annotation.Primary
 *  org.springframework.context.annotation.Profile
 */
package br.com.lectek.copainsider.config;

import br.com.lectek.copainsider.adapters.outbound.auth.jwt.store.InMemoryTokenBlacklist;
import br.com.lectek.copainsider.adapters.outbound.auth.jwt.store.TokenBlacklist;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile(value={"docker"})
public class TokenBlacklistFallbackConfig {
    @Bean(name={"inMemoryTokenBlacklist"})
    @Primary
    @ConditionalOnMissingBean(value={TokenBlacklist.class})
    public TokenBlacklist inMemoryTokenBlacklist() {
        return new InMemoryTokenBlacklist();
    }
}

