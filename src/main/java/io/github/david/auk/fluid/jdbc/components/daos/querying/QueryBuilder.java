package io.github.david.auk.fluid.jdbc.components.daos.querying;

import io.github.david.auk.fluid.jdbc.components.daos.Dao;
import io.github.david.auk.fluid.jdbc.components.daos.querying.FilterCriterion.FilterCriterion;
import io.github.david.auk.fluid.jdbc.components.daos.querying.operator.NoValueOperator;
import io.github.david.auk.fluid.jdbc.components.daos.querying.operator.ValueOperator;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class QueryBuilder<T extends TableEntity, K> {
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
    public <D> QueryBuilder<T, K> where(Field field, ValueOperator valueOperator, D value) {
        filters.add(new FilterCriterion(field, valueOperator, value));
        return this;
    }


    public <D> QueryBuilder<T, K> where(Field field, NoValueOperator noValueOperator) {
        filters.add(new FilterCriterion(field, noValueOperator));
        return this;
    }

    /**
     * Alias for where(...).
     */
    public <D> QueryBuilder<T, K> and(Field field, ValueOperator valueOperator, D value) {
        return where(field, valueOperator, value);
    }

    public <D> QueryBuilder<T, K> and(Field field, NoValueOperator valueOperator) {
        return where(field, valueOperator);
    }

    /**
     * Specify ORDER BY field.
     */
    public QueryBuilder<T, K> orderBy(Field field) {
        this.orderByField = field;
        return this;
    }

    /** Subsequent .get() will sort ascending. */
    public QueryBuilder<T, K> asc() {
        this.ascending = true;
        return this;
    }

    /** Subsequent .get() will sort descending. */
    public QueryBuilder<T, K> desc() {
        this.ascending = false;
        return this;
    }

    /**
     * Execute the query and return matched entities.
     */
    public List<T> get() {
        return dao.get(filters, orderByField, ascending);
    }

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

