package com.rtr.alchemy.db;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Filter criteria for a list query, allowing for pagination and filtering of items
 */
public class Query {
    public static final Query NONE = Query.criteria().build();
    private final Integer offset;
    private final Integer limit;
    private final List<Ordering> orderings;
    private final List<Filter> filters;

    private Query(Integer offset, Integer limit, List<Ordering> orderings, List<Filter> filters) {
        this.offset = offset;
        this.limit = limit;
        this.orderings = orderings;
        this.filters = filters;
    }

    public static Builder criteria() {
        return new Builder();
    }

    public static class Builder {
        private Integer offset;
        private Integer limit;
        private final List<Ordering> orderings = Lists.newArrayList();
        private final List<Filter> filters = Lists.newArrayList();

        public Builder filter(FilterableFields field, Object value) {
            filters.add(new Filter(field, value));
            return this;
        }

        public Builder offset(Integer offset) {
            this.offset = offset;
            return this;
        }

        public Builder limit(Integer limit) {
            this.limit = limit;
            return this;
        }

        public Builder orderBy(SortableFields field, SortDirection direction) {
            orderings.add(new Ordering(field, direction));
            return this;
        }

        public Query build() {
            return new Query(offset, limit, orderings, filters);
        }
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public List<Ordering> getOrderings() {
        return orderings;
    }

    public Integer getOffset() {
        return offset;
    }

    public Integer getLimit() {
        return limit;
    }
}
