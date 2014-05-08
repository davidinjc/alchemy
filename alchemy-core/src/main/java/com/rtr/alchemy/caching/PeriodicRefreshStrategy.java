package com.rtr.alchemy.caching;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.concurrent.atomic.AtomicReference;

public class PeriodicRefreshStrategy implements RefreshStrategy {
    public final Duration duration;
    private final AtomicReference<DateTime> lastRefresh;

    public PeriodicRefreshStrategy(Duration duration) {
        this.duration = duration;
        this.lastRefresh = new AtomicReference<>(DateTime.now());
    }

    private boolean shouldRefresh() {
        final DateTime now = DateTime.now();
        final Duration elapsed = new Duration(lastRefresh.get(), now);

        if (elapsed.isLongerThan(duration)) {
            lastRefresh.set(now);
            return true;
        }

        return false;
    }

    @Override
    public void accessAll(ExperimentsCache cache) {
        if (shouldRefresh() && cache.checkIfStale()) {
            cache.invalidateAll(true);
        }
    }

    @Override
    public void accessExperiment(String experimentName, ExperimentsCache cache) {
    }
}
