package br.com.lectek.copainsider.adapters.inbound.web.security;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * In-memory rate limiter for the login endpoint, keyed by client IP.
 *
 * Allows up to MAX_ATTEMPTS failures within WINDOW before blocking for BLOCK_DURATION.
 * Entries are evicted every 10 minutes to prevent unbounded memory growth.
 */
@Service
public class LoginAttemptService {

    static final int MAX_ATTEMPTS = 10;
    private static final Duration WINDOW        = Duration.ofMinutes(10);
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(15);

    private final ConcurrentHashMap<String, AttemptRecord> cache = new ConcurrentHashMap<>();

    private record AttemptRecord(int count, Instant firstAttempt, Instant blockedUntil) {
        boolean isBlocked()  { return blockedUntil != null && Instant.now().isBefore(blockedUntil); }
        boolean isExpired()  { return !isBlocked() && Duration.between(firstAttempt, Instant.now()).compareTo(WINDOW) > 0; }
    }

    public boolean isBlocked(String ip) {
        AttemptRecord r = cache.get(ip);
        return r != null && r.isBlocked();
    }

    public void loginSucceeded(String ip) {
        cache.remove(ip);
    }

    public void loginFailed(String ip) {
        cache.compute(ip, (key, existing) -> {
            if (existing == null || existing.isExpired()) {
                return new AttemptRecord(1, Instant.now(), null);
            }
            int count = existing.count() + 1;
            Instant blockedUntil = count >= MAX_ATTEMPTS
                    ? Instant.now().plus(BLOCK_DURATION)
                    : existing.blockedUntil();
            return new AttemptRecord(count, existing.firstAttempt(), blockedUntil);
        });
    }

    @Scheduled(fixedDelay = 10 * 60 * 1_000)
    public void evictExpired() {
        cache.entrySet().removeIf(e -> e.getValue().isExpired());
    }
}
