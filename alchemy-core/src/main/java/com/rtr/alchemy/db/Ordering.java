package com.rtr.alchemy.db;

import com.google.common.base.Objects;

public class Ordering {
    private final SortableFields field;
    private final SortDirection direction;

    public Ordering(SortableFields field, SortDirection direction) {
        this.field = field;
        this.direction = direction;
    }

    public SortableFields getField() {
        return field;
    }

    public SortDirection getDirection() {
        return direction;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Ordering)) {
            return false;
        }

        final Ordering other = (Ordering) obj;
        return
            Objects.equal(field, other.field) &&
            Objects.equal(direction, other.direction);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(field, direction);
    }
}
