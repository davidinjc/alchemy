package com.rtr.alchemy.service.util;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.rtr.alchemy.db.FilterableFields;
import com.rtr.alchemy.db.Query;
import com.rtr.alchemy.db.SortDirection;
import com.rtr.alchemy.db.SortableFields;

import javax.ws.rs.core.MultivaluedMap;
import java.util.List;
import java.util.Map;

public class QueryUtil {
    private static final Map<String, FilterableFields> FILTER_FIELDS;
    private static final Map<FilterableFields, Class<?>> FIELD_TYPES;
    private static final Map<String, SortableFields> SORT_FIELDS;
    private static final String OFFSET_FIELD = "offset";
    private static final String LIMIT_FIELD = "limit";
    private static final String SORT_FIELD = "sort";

    static {
        FILTER_FIELDS = Maps.newConcurrentMap();
        FILTER_FIELDS.put("active", FilterableFields.ACTIVE);
        FILTER_FIELDS.put("name", FilterableFields.NAME);
        FILTER_FIELDS.put("description", FilterableFields.DESCRIPTION);
        FILTER_FIELDS.put("identity_type", FilterableFields.IDENTITY_TYPE);

        FIELD_TYPES = Maps.newConcurrentMap();
        FIELD_TYPES.put(FilterableFields.ACTIVE, Boolean.class);
        FIELD_TYPES.put(FilterableFields.NAME, String.class);
        FIELD_TYPES.put(FilterableFields.DESCRIPTION, String.class);
        FIELD_TYPES.put(FilterableFields.IDENTITY_TYPE, String.class);

        SORT_FIELDS = Maps.newConcurrentMap();
        SORT_FIELDS.put("active", SortableFields.ACTIVE);
        SORT_FIELDS.put("name", SortableFields.NAME);
        SORT_FIELDS.put("identity_type", SortableFields.IDENTITY_TYPE);
        SORT_FIELDS.put("created", SortableFields.CREATED);

    }

    private static void addOrderings(Query.Builder query, String sort) {
        if (sort == null) {
            return;
        }

        for (final String column : Splitter.on(",").split(sort)) {
            if (column == null || column.isEmpty()) {
                continue;
            }

            final boolean desc = column.startsWith("-");
            final SortableFields field = SORT_FIELDS.get(desc ? column.substring(1) : column);

            if (field != null) {
                query.orderBy(field, desc ? SortDirection.DESCENDING : SortDirection.ASCENDING);
            }
        }
    }

    private static String extractParam(MultivaluedMap<String, String> queryParams, String name) {
        final List<String> values = queryParams.remove(name);

        if (values == null) {
            return null;
        }

        if (values.size() > 1) {
            throw new IllegalArgumentException(String.format("%s may only be specified once", name));
        }

        return values.get(0);
    }

    private static Object convertField(FilterableFields field, String value) {
        final Class<?> type = FIELD_TYPES.get(field);
        if (type.equals(Boolean.class)) {
            return Boolean.parseBoolean(value);
        }

        return value;
    }

    public static Query buildQuery(MultivaluedMap<String, String> queryParams) {
        final String offset = extractParam(queryParams, OFFSET_FIELD);
        final String limit = extractParam(queryParams, LIMIT_FIELD);
        final String sort = extractParam(queryParams, SORT_FIELD);
        final Query.Builder query = Query.criteria();

        addOrderings(query, sort);

        if (offset != null) {
            query.offset(Integer.parseInt(offset));
        }

        if (limit != null) {
            query.limit(Integer.parseInt(limit));
        }

        for (final Map.Entry<String, List<String>> filter : queryParams.entrySet()) {
            final FilterableFields field = FILTER_FIELDS.get(filter.getKey());

            if (field == null) {
                throw new IllegalArgumentException(String.format("unknown filter %s", filter.getKey()));
            }

            if (filter.getValue().size() > 1) {
                throw new IllegalArgumentException(String.format("%s may only be specified once", filter.getKey()));
            }

            query.filter(field, convertField(field, filter.getValue().get(0)));
        }

        return query.build();
    }
}
