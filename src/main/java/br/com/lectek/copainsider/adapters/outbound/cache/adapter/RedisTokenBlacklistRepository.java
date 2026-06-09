package br.com.lectek.copainsider.adapters.outbound.cache.adapter;

import br.com.lectek.copainsider.adapters.outbound.auth.jwt.store.TokenBlacklist;
import br.com.lectek.copainsider.adapters.outbound.cache.config.RedisProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

@Repository("redisTokenBlacklist")
@ConditionalOnProperty(prefix = "jwt.blacklist", name = {"strategy"}, havingValue = "redis")
public class RedisTokenBlacklistRepository implements TokenBlacklist {

    private final StringRedisTemplate redis;
    private final RedisProperties props;

    public RedisTokenBlacklistRepository(StringRedisTemplate redis, RedisProperties props) {
        this.redis = Objects.requireNonNull(redis);
        this.props = Objects.requireNonNull(props);
    }

    @Override
    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) return false;
        String key = buildKey(token);
        Boolean exists = redis.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public void blacklist(String token, Instant expiresAt) {
        if (token == null || token.isBlank()) return;
        String key = buildKey(token);

        Instant now = Instant.now();
        Duration ttl = (expiresAt != null) ? Duration.between(now, expiresAt) : Duration.ofHours(24);
        if (ttl.isNegative() || ttl.isZero()) ttl = Duration.ofSeconds(1);

        ValueOperations<String, String> ops = redis.opsForValue();
        // armazena qualquer valor “truthy”; só nos interessa a chave existir
        ops.set(key, "1", ttl);
    }

    private String buildKey(String raw) {
        return props.getKeyPrefix()
                + props.getBlacklistNamespace()
                + sha256Url(raw);
    }

    private static String sha256Url(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception e) {
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(s.getBytes(StandardCharsets.UTF_8));
        }
    }
}
