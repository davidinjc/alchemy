package com.rtr.alchemy.caching;

public class DefaultRefreshStrategy implements RefreshStrategy {
    @Override
    public void accessAll(ExperimentsCache cache) {
        if (cache.checkIfStale()) {
            cache.invalidateAll(true);
        }
    }

    @Override
    public void accessExperiment(String experimentName, ExperimentsCache cache) {

    }
}
