package br.com.lectek.copainsider.application.service.automation;

import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class NoopCartSnapshotProvider implements CartSnapshotProvider {
    @Override
    public List<CartSnapshot> findAbandonedCarts(Duration olderThan, int limit) {
        return List.of();
    }
}
