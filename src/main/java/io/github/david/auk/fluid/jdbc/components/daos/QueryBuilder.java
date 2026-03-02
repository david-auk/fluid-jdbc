package io.github.david.auk.fluid.jdbc.components.daos;

import io.github.david.auk.fluid.jdbc.components.daos.querying.FilterCriterion.FilterCriterion;
import io.github.david.auk.fluid.jdbc.components.daos.querying.operator.MultiOperator;
import io.github.david.auk.fluid.jdbc.components.daos.querying.operator.NoValueOperator;
import io.github.david.auk.fluid.jdbc.components.daos.querying.operator.RangeOperator;
import io.github.david.auk.fluid.jdbc.components.daos.querying.operator.SingleValueOperator;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QueryBuilder<T extends TableEntity, K> implements QueryInterface<T, K> {
    private final Dao<T, K> dao;
    private final List<FilterCriterion> filters = new ArrayList<>();
    @Nullable
    private Field orderByField;
    private boolean ascending = true;

    public QueryBuilder(Dao<T, K> dao) {
        this.dao = dao;
    }

    /**
     * Add an “= value” filter.
     */
    @Override
    public QueryBuilder<T, K> where(Field field, SingleValueOperator valueOperator, Object value) {
        filters.add(new FilterCriterion(field, valueOperator, value));
        return this;
    }

    /**
     * Add an “= value” filter.
     */
    @Override
    public QueryBuilder<T, K> where(Field field, RangeOperator valueOperator, Integer from, Integer to) {
        filters.add(new FilterCriterion(field, valueOperator, List.of(from, to)));
        return this;
    }

    @Override
    public QueryInterface<T, K> and(Field field, RangeOperator valueOperator, Integer from, Integer to) {
        return where(field, valueOperator, from, to);
    }

    /**
     * Add an “= value” filter.
     */
    @Override
    public QueryBuilder<T, K> where(Field field, RangeOperator valueOperator, Timestamp startDate, Timestamp endDate) {
        filters.add(new FilterCriterion(field, valueOperator, List.of(startDate, endDate)));
        return this;
    }

    @Override
    public QueryInterface<T, K> and(Field field, RangeOperator valueOperator, Timestamp startDate, Timestamp endDate) {
        return where(field, valueOperator, startDate, endDate);
    }

    @Override
    public QueryInterface<T, K> where(Field field, RangeOperator valueOperator, String startString, String endString) {
        filters.add(new FilterCriterion(field, valueOperator, List.of(startString, endString)));
        return this;
    }

    @Override
    public QueryInterface<T, K> and(Field field, RangeOperator valueOperator, String startString, String endString) {
        return where(field, valueOperator, startString, endString);
    }

    @Override
    public QueryInterface<T, K> where(Field field, MultiOperator valueOperator, Collection<?> values) {
        filters.add(new FilterCriterion(field, valueOperator, values));
        return this;
    }

    @Override
    public QueryInterface<T, K> and(Field field, MultiOperator valueOperator, Collection<?> values) {
        return where(field, valueOperator, values);
    }

    @Override
    public QueryBuilder<T, K> where(Field field, NoValueOperator noValueOperator) {
        filters.add(new FilterCriterion(field, noValueOperator));
        return this;
    }

    /**
     * Alias for where(...).
     */
    @Override
    public QueryBuilder<T, K> and(Field field, SingleValueOperator valueOperator, Object value) {
        return where(field, valueOperator, value);
    }

    @Override
    public QueryBuilder<T, K> and(Field field, NoValueOperator valueOperator) {
        return where(field, valueOperator);
    }

    /**
     * Specify ORDER BY field.
     */
    @Override
    public QueryBuilder<T, K> orderBy(Field field) {
        this.orderByField = field;
        return this;
    }

    /** Subsequent .get() will sort ascending. */
    @Override
    public QueryBuilder<T, K> asc() {
        this.ascending = true;
        return this;
    }

    /** Subsequent .get() will sort descending. */
    @Override
    public QueryBuilder<T, K> desc() {
        this.ascending = false;
        return this;
    }

    /**
     * Execute the query and return matched entities.
     */
    @Override
    public List<T> get() {
        return dao.get(filters, orderByField, ascending);
    }

    @Override
    public T getUnique() {
        List<T> results = dao.get(filters, orderByField, ascending);
        if (results.size() > 1) {
            throw new IllegalStateException("Multiple results found for query: " + filters);
        } else if (results.isEmpty()) {
            return null;
        } else {
            return results.getFirst();
        }
    }
}

