package com.rtr.alchemy.db.memory;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.rtr.alchemy.db.ExperimentsStore;
import com.rtr.alchemy.db.Filter;
import com.rtr.alchemy.db.Ordering;
import com.rtr.alchemy.db.Query;
import com.rtr.alchemy.db.SortDirection;
import com.rtr.alchemy.models.Experiment;
import org.apache.commons.math3.analysis.function.Exp;

import java.util.Collections;
import java.util.Comparator;
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

    private static boolean stringMatches(String value, Object filter) {
        return
            filter instanceof String &&
            value.toLowerCase().contains(((String) filter).toLowerCase());

    }

    private static boolean filtersMatch(Experiment experiment, Query query) {
        if (query.getFilters().size() == 0) {
            return true;
        }

        for (final Filter filter : query.getFilters()) {
            switch (filter.getField()) {
                case NAME:
                    if (!stringMatches(experiment.getName(), filter.getValue())) {
                        return false;
                    }
                    break;
                case DESCRIPTION:
                    if (!stringMatches(experiment.getDescription(), filter.getValue())) {
                        return false;
                    }
                    break;
                case IDENTITY_TYPE:
                    if (!stringMatches(experiment.getIdentityType(), filter.getValue())) {
                        return false;
                    }
                    break;
                case ACTIVE:
                    if (!(filter.getValue() instanceof  Boolean) ||
                        experiment.isActive() != filter.getValue()) {
                        return false;
                    }
                    break;
            }
        }

        return true;
    }

    @Override
    public void close() {

    }

    private static class QueryComparator implements Comparator<Experiment> {
        private final Query query;

        private QueryComparator(Query query) {
            this.query = query;
        }

        @Override
        public int compare(Experiment left, Experiment right) {
            ComparisonChain chain = ComparisonChain.start();

            for (final Ordering ordering : query.getOrderings()) {
                final boolean ascending = (ordering.getDirection() == SortDirection.ASCENDING);
                final Experiment lhs = ascending ? left : right;
                final Experiment rhs = ascending ? right : left;

                switch (ordering.getField()) {
                    case NAME:
                        chain = chain.compare(lhs.getName(), rhs.getName());
                        break;
                    case ACTIVE:
                        // reverse comparison for a more intuitive 'active first' in ascending order
                        chain = chain.compare(rhs.isActive(), lhs.isActive());
                        break;
                    case IDENTITY_TYPE:
                        chain = chain.compare(lhs.getIdentityType(), rhs.getIdentityType());
                        break;
                    case CREATED:
                        chain = chain.compare(lhs.getCreated(), rhs.getCreated());
                        break;
                }
            }

            return chain.result();
        }
    }

    @Override
    public Iterable<Experiment> find(Query query, Experiment.BuilderFactory factory) {
        int limit = 0;
        int offset = 0;
        final List<Experiment> result = Lists.newArrayList();
        final Comparator<Experiment> comparator = new QueryComparator(query);

        synchronized (db) {
            for (final Experiment experiment : db.values()) {
                if (query.getOffset() != null && offset++ < query.getOffset()) {
                    continue;
                }

                if (filtersMatch(experiment, query)) {
                    if (query.getLimit() != null && ++limit > query.getLimit()) {
                        break;
                    }

                    result.add(Experiment.copyOf(experiment));
                }
            }
        }

        if (query.getOrderings().size() > 0) {
            Collections.sort(result, comparator);
        }
        return result;
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
