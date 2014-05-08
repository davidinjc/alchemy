package com.rtr.alchemy.caching;

import com.google.common.collect.Maps;
import com.rtr.alchemy.db.ExperimentsStore;
import com.rtr.alchemy.db.Filter;
import com.rtr.alchemy.models.Experiment;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Caches experiments
 */
public class ExperimentsCache implements Closeable {
    private final ExperimentsStore store;
    private volatile Map<String, Experiment> experiments;
    private volatile long sequence;
    private final ExecutorService executorService;
    private final boolean defaultExecutorService;

    public ExperimentsCache(ExperimentsStore store, ExecutorService executorService) {
        this.store = store;
        this.defaultExecutorService = executorService == null;
        this.executorService = executorService != null ? executorService : Executors.newSingleThreadExecutor();
    }

    /**
     * Retrieves currently cached list of active experiments
     */
    public Map<String, Experiment> getActiveExperiments() {
        return experiments;
    }

    /**
     * Invalidates cache, reloading all active experiments
     */
    public void invalidateAll(boolean async) {
        if (!async) {
            invalidateAll();
        } else {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    invalidateAll();
                }
            });
        }
    }

    private void invalidateAll() {
        final Map<String, Experiment> map = Maps.newConcurrentMap();
        final Iterable<Experiment> result =
            store.find(
                Filter.criteria().build(),
                new Experiment.BuilderFactory(store)
            );

        for (final Experiment experiment : result) {
            // TODO: use filtering to get active experiments
            if (!experiment.isActive()) {
                continue;
            }
            map.put(experiment.getName(), experiment);
            updateSequence(experiment);
        }

        experiments = map;
    }

    /**
     * Invalidate experiment, reloading it
     */
    public void invalidateExperiment(final String experimentName, boolean async) {
        if (!async) {
            invalidateExperiment(experimentName);
        } else {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    invalidateExperiment(experimentName);
                }
            });
        }
    }

    private void invalidateExperiment(String experimentName) {
        final Experiment experiment = store.load(experimentName, new Experiment.Builder(store));
        experimentSaved(experiment);
    }

    public void experimentSaved(Experiment experiment) {
        if (experiment.isActive()) {
            experiments.put(experiment.getName(), experiment);
            updateSequence(experiment);
        } else {
            experiments.remove(experiment.getName());
        }
    }

    public void experimentDeleted(String experimentName) {
        experiments.remove(experimentName);
    }

    /**
     * Globally checks if any experiments are stale
     */
    public boolean checkIfStale() {
        return sequence < store.currentSequenceNumber();
    }

    /**
     * Check if a given experiment is stale
     */
    public boolean checkIfStale(String experimentName) {
        final Long expSequence = store.sequenceNumber(experimentName);
        return expSequence != null && sequence < expSequence;
    }

    private void updateSequence(Experiment experiment) {
        if (experiment.getSequence() > sequence) {
            sequence = experiment.getSequence();
        }
    }

    @Override
    public void close() throws IOException {
        if (defaultExecutorService) {
            executorService.shutdown();
        }
    }
}
