package com.rtr.alchemy.db.memory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rtr.alchemy.db.ExperimentsStore;
import com.rtr.alchemy.db.Filter;
import com.rtr.alchemy.models.Experiment;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class MemoryExperimentsStore implements ExperimentsStore {
    private final Map<String, Experiment> db = Maps.newConcurrentMap();
    private final AtomicLong sequence = new AtomicLong(Long.MIN_VALUE);

    public MemoryExperimentsStore() {
    }

    @Override
    public void save(Experiment experiment) {
        db.put(experiment.getName(), Experiment.copyOf(experiment));
    }

    @Override
    public Experiment load(String experimentName, Experiment.Builder builder) {
        return Experiment.copyOf(db.get(experimentName));
    }

    @Override
    public void delete(String experimentName) {
        db.remove(experimentName);
    }

    private static boolean filterMatches(String filter, Object ... values) {
        if (filter == null || filter.isEmpty()) {
            return true;
        }

        for (final Object obj : values) {
            if (obj == null) {
                continue;
            }

            final String value = String.valueOf(obj);
            if (value.toLowerCase().contains(filter.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Iterable<Experiment> find(Filter filter, Experiment.BuilderFactory factory) {
        int limit = 0;
        int offset = 0;
        final List<Experiment> result = Lists.newArrayList();

        synchronized (db) {
            for (final Experiment experiment : db.values()) {
                if (filter.getOffset() != null && offset++ < filter.getOffset()) {
                    continue;
                }

                if (filterMatches(
                    filter.getFilter(),
                    experiment.getName(),
                    experiment.getDescription()
                )) {
                    if (filter.getLimit() != null && ++limit > filter.getLimit()) {
                        break;
                    }

                    result.add(Experiment.copyOf(experiment));
                }
            }
        }

        return result;
    }

    @Override
    public void close() {
        db.clear();
    }

    @Override
    public long nextSequenceNumber() {
        return sequence.incrementAndGet();
    }

    @Override
    public Long currentSequenceNumber() {
        final long num = sequence.get();

        if (num == Long.MIN_VALUE) {
            return null;
        }

        return num;
    }

    @Override
    public Long sequenceNumber(String experimentName) {
        final Experiment experiment = db.get(experimentName);
        return experiment != null ? experiment.getSequence() : null;
    }

    public void resetDatabase() {
        db.clear();
    }
}
