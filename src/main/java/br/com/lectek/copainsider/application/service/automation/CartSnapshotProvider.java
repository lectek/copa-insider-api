package br.com.lectek.copainsider.application.service.automation;

import java.time.Duration;
import java.util.List;

public interface CartSnapshotProvider {
    List<CartSnapshot> findAbandonedCarts(Duration olderThan, int limit);
}
