package com.rtr.alchemy.db;

import com.google.common.base.Objects;

public class Filter {
    private final FilterableFields field;
    private final Object value;

    public Filter(FilterableFields field, Object value) {
        this.field = field;
        this.value = value;
    }

    public FilterableFields getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Filter)) {
            return false;
        }

        final Filter other = (Filter) obj;
        return
            Objects.equal(field, other.field) &&
            Objects.equal(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(field, value);
    }
}
