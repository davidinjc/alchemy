package com.rtr.alchemy.caching;

/**
 * Defines an interface for implementing a strategy that allows you to control when cache invalidation happens
 * in various places
 */
public interface RefreshStrategy {
    /**
     * Invoked when all active experiments are accessed
     */
    void accessAll(ExperimentsCache cache);

    /**
     * Invoked when a specific experiment is accessed
     */
    void accessExperiment(String experimentName, ExperimentsCache cache);
}
