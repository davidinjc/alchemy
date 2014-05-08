package com.rtr.alchemy.db;

import com.rtr.alchemy.caching.ExperimentsCache;
import com.rtr.alchemy.models.Experiment;

/**
 * Used to associate an experiment to a store and cache
 */
public class ExperimentsContext {
    private final ExperimentsStore store;
    private final ExperimentsCache cache;

    public ExperimentsContext(ExperimentsStore store, ExperimentsCache cache) {
        this.store = store;
        this.cache = cache;
    }

    public void save(Experiment experiment) {
        store.save(experiment);
        cache.experimentSaved(experiment);
    }

    public void delete(String experimentName) {
        store.delete(experimentName);
        cache.experimentDeleted(experimentName);
    }

    public long nextSequenceNumber() {
        return store.nextSequenceNumber();
    }
}
