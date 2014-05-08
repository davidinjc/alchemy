package com.rtr.alchemy.models;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.rtr.alchemy.caching.DefaultRefreshStrategy;
import com.rtr.alchemy.caching.ExperimentsCache;
import com.rtr.alchemy.caching.RefreshStrategy;
import com.rtr.alchemy.db.ExperimentsStore;
import com.rtr.alchemy.db.Query;
import com.rtr.alchemy.identities.Identity;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * The main class for accessing experiments
 */
public class Experiments implements Closeable {
    private final ExperimentsStore store;
    private final ExperimentsCache cache;
    private final RefreshStrategy refreshStrategy;

    private Experiments(ExperimentsStore store,
                        RefreshStrategy refreshStrategy,
                        ExecutorService executorService) {
        Preconditions.checkNotNull(store, "store cannot be null");
        this.store = store;
        this.cache = new ExperimentsCache(store, executorService);
        this.cache.invalidateAll(false);
        this.refreshStrategy = refreshStrategy != null ? refreshStrategy : new DefaultRefreshStrategy();
    }

    public static ExperimentsBuilder using(ExperimentsStore store) {
        return new ExperimentsBuilder(store);
    }

    public ExperimentsCache getCache() {
        return cache;
    }

    public synchronized Treatment getActiveTreatment(String experimentName, Identity identity) {
        refreshStrategy.accessExperiment(experimentName, cache);

        final Experiment experiment = cache.getActiveExperiments().get(experimentName);
        if (experiment == null) {
            return null;
        }

        if (experiment.getIdentityType() != null && !experiment.getIdentityType().equals(identity.getType())) {
            return null;
        }

        final TreatmentOverride override = experiment.getOverride(identity);
        return override != null ? override.getTreatment() : experiment.getTreatment(identity);
    }

    public synchronized Map<Experiment, Treatment> getActiveTreatments(Identity ... identities) {
        refreshStrategy.accessAll(cache);

        final Map<String, Identity> identitiesByType = Maps.newHashMap();
        for (final Identity identity : identities) {
            identitiesByType.put(identity.getType(), identity);
        }

        final Map<Experiment, Treatment> result = Maps.newHashMap();
        for (final Experiment experiment : cache.getActiveExperiments().values()) {
            if (experiment.getIdentityType() == null) {
                for (final Identity identity : identities) {
                    final TreatmentOverride override = experiment.getOverride(identity);
                    final Treatment treatment = override == null ? experiment.getTreatment(identity) : override.getTreatment();

                    if (treatment != null) {
                        result.put(experiment, treatment);
                        break;
                    }
                }
            } else {
                final Identity identity = identitiesByType.get(experiment.getIdentityType());
                if (identity == null) {
                    continue;
                }

                final TreatmentOverride override = experiment.getOverride(identity);
                final Treatment treatment = override == null ? experiment.getTreatment(identity) : override.getTreatment();

                if (treatment == null) {
                    continue;
                }

                result.put(experiment, treatment);
            }
        }

        return result;
    }

    public synchronized Iterable<Experiment> find(Query query) {
        return store.find(query, new Experiment.BuilderFactory(store));
    }

    public synchronized Iterable<Experiment> find() {
        return find(Query.criteria().build());
    }

    public synchronized Experiment get(String experimentName) {
        return store.load(
            experimentName,
            new Experiment.Builder(store)
        );
    }

    public synchronized void delete(String experimentName) {
        store.delete(experimentName);
    }

    public synchronized Experiment create(String name) {
        return new Experiment(store, name);
    }

    @Override
    public void close() throws IOException {
        cache.close();
        store.close();
    }

    public static class ExperimentsBuilder {
        private final ExperimentsStore store;
        private RefreshStrategy strategy;
        private ExecutorService executorService;

        private ExperimentsBuilder(ExperimentsStore store) {
            this.store = store;
        }

        public ExperimentsBuilder using(RefreshStrategy strategy) {
            this.strategy = strategy;
            return this;
        }

        public ExperimentsBuilder using(ExecutorService executorService) {
            this.executorService = executorService;
            return this;
        }

        public Experiments build() {
            return new Experiments(
                store,
                strategy,
                executorService
            );
        }

    }
}
