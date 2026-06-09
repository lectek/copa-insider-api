/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 *  org.springframework.stereotype.Repository
 */
package br.com.redemaisfarma.adapters.outbound.cache.adapter;

import br.com.redemaisfarma.adapters.outbound.auth.jwt.store.TokenBlacklist;
import java.time.Instant;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository(value="noopTokenBlacklistRepository")
@ConditionalOnProperty(prefix="jwt.blacklist", name={"strategy"}, havingValue="noop")
public class NoopTokenBlacklistRepository
implements TokenBlacklist {
    @Override
    public boolean isBlacklisted(String jti) {
        return false;
    }

    @Override
    public void blacklist(String jti, Instant expiresAt) {
    }

    @Override
    public void purgeExpired() {
    }
}

