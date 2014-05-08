package com.rtr.alchemy.caching;

public class NoOpRefreshStrategy implements RefreshStrategy {
    @Override
    public void accessAll(ExperimentsCache cache) {
    }

    @Override
    public void accessExperiment(String experimentName, ExperimentsCache cache) {
    }
}
