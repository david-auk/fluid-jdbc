package io.github.david.auk.fluid.jdbc.components.daos;

import io.github.david.auk.fluid.jdbc.components.daos.querying.filters.FilterCriterion;
import io.github.david.auk.fluid.jdbc.components.daos.querying.filters.FilterCriterionFactory;
import io.github.david.auk.fluid.jdbc.components.daos.querying.filters.FilterFieldFactory;
import io.github.david.auk.fluid.jdbc.components.daos.querying.filters.FilterTypedField;
import io.github.david.auk.fluid.jdbc.components.daos.querying.operator.*;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QueryBuilder<TE extends TableEntity, PK> implements QueryInterface<TE, PK> {
    private final Dao<TE, PK> dao;
    private final List<FilterCriterion<?, ?>> filters = new ArrayList<>();
    @Nullable
    private Field orderByField;
    private boolean ascending = true;

    public QueryBuilder(Dao<TE, PK> dao) {
        this.dao = dao;
    }

    private FilterTypedField<? extends TableEntity, ?> getFilterTypedField(Field field) {
        return FilterFieldFactory.buildFilterTypedField(dao.getTableEntityClass(), field);
    }

    private FilterCriterion<? extends TableEntity, ?> buildFilterCriterion(Field field, ValueOperator valueOperator, Object value) {
        return FilterCriterionFactory.buildFilterCriterion(getFilterTypedField(field), valueOperator, value);
    }

    private FilterCriterion<? extends TableEntity, ?> buildFilterCriterion(Field field, NoValueOperator noValueOperator) {
        return FilterCriterionFactory.buildFilterCriterion(getFilterTypedField(field), noValueOperator);
    }

    /**
     * Add an “= value” filter.
     */
    @Override
    public QueryBuilder<TE, PK> where(Field field, SingleValueOperator valueOperator, Object value) {
        filters.add(buildFilterCriterion(field, valueOperator, value));
        return this;
    }

    /**
     * Add an “= value” filter.
     */
    @Override
    public QueryBuilder<TE, PK> where(Field field, RangeOperator valueOperator, Integer from, Integer to) {
        filters.add(buildFilterCriterion(field, valueOperator, List.of(from, to)));
        return this;
    }

    @Override
    public QueryBuilder<TE, PK> and(Field field, RangeOperator valueOperator, Integer from, Integer to) {
        return where(field, valueOperator, from, to);
    }

    /**
     * Add an “= value” filter.
     */
    @Override
    public QueryBuilder<TE, PK> where(Field field, RangeOperator valueOperator, Timestamp startDate, Timestamp endDate) {
        filters.add(buildFilterCriterion(field, valueOperator, List.of(startDate, endDate)));
        return this;
    }

    @Override
    public QueryInterface<TE, PK> and(Field field, RangeOperator valueOperator, Timestamp startDate, Timestamp endDate) {
        return where(field, valueOperator, startDate, endDate);
    }

    @Override
    public QueryInterface<TE, PK> where(Field field, RangeOperator valueOperator, String startString, String endString) {
        filters.add(buildFilterCriterion(field, valueOperator, List.of(startString, endString)));
        return this;
    }

    @Override
    public QueryInterface<TE, PK> and(Field field, RangeOperator valueOperator, String startString, String endString) {
        return where(field, valueOperator, startString, endString);
    }

    @Override
    public QueryInterface<TE, PK> where(Field field, MultiOperator valueOperator, Collection<?> values) {
        filters.add(buildFilterCriterion(field, valueOperator, values));
        return this;
    }

    @Override
    public QueryInterface<TE, PK> and(Field field, MultiOperator valueOperator, Collection<?> values) {
        return where(field, valueOperator, values);
    }

    @Override
    public QueryBuilder<TE, PK> where(Field field, NoValueOperator noValueOperator) {
        filters.add(buildFilterCriterion(field, noValueOperator));
        return this;
    }

    /**
     * Alias for where(...).
     */
    @Override
    public QueryBuilder<TE, PK> and(Field field, SingleValueOperator valueOperator, Object value) {
        return where(field, valueOperator, value);
    }

    @Override
    public QueryBuilder<TE, PK> and(Field field, NoValueOperator valueOperator) {
        return where(field, valueOperator);
    }

    /**
     * Specify ORDER BY field.
     */
    @Override
    public QueryBuilder<TE, PK> orderBy(Field field) {
        this.orderByField = field;
        return this;
    }

    /** Subsequent .get() will sort ascending. */
    @Override
    public QueryBuilder<TE, PK> asc() {
        this.ascending = true;
        return this;
    }

    /** Subsequent .get() will sort descending. */
    @Override
    public QueryBuilder<TE, PK> desc() {
        this.ascending = false;
        return this;
    }

    /**
     * Execute the query and return matched entities.
     */
    @Override
    public List<TE> get() {
        return dao.get(filters, orderByField, ascending);
    }

    @Override
    public TE getUnique() {
        List<TE> results = dao.get(filters, orderByField, ascending);
        if (results.size() > 1) {
            throw new IllegalStateException("Multiple results found for query: " + filters);
        } else if (results.isEmpty()) {
            return null;
        } else {
            return results.getFirst();
        }
    }
}
