package io.github.david.auk.fluid.jdbc.components.daos;

import io.github.david.auk.fluid.jdbc.components.daos.querying.operator.MultiOperator;
import io.github.david.auk.fluid.jdbc.components.daos.querying.operator.NoValueOperator;
import io.github.david.auk.fluid.jdbc.components.daos.querying.operator.RangeOperator;
import io.github.david.auk.fluid.jdbc.components.daos.querying.operator.SingleValueOperator;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

public interface QueryInterface<T extends TableEntity, K> {
    QueryInterface<T, K> where(Field field, SingleValueOperator valueOperator, Object value);
    QueryInterface<T, K> and(Field field, SingleValueOperator valueOperator, Object value);

    QueryInterface<T, K> where(Field field, RangeOperator valueOperator, Integer from, Integer to);
    QueryInterface<T, K> and(Field field, RangeOperator valueOperator, Integer from, Integer to);

    QueryInterface<T, K> where(Field field, RangeOperator valueOperator, Timestamp startDate, Timestamp endDate);
    QueryInterface<T, K> and(Field field, RangeOperator valueOperator, Timestamp startDate, Timestamp endDate);

    QueryInterface<T, K> where(Field field, RangeOperator valueOperator, String startString, String endString);
    QueryInterface<T, K> and(Field field, RangeOperator valueOperator, String startString, String endString);

    QueryInterface<T, K> where(Field field, MultiOperator valueOperator, Collection<?> values);
    QueryInterface<T, K> and(Field field, MultiOperator valueOperator, Collection<?> values);

    QueryBuilder<T, K> where(Field field, NoValueOperator noValueOperator);
    QueryBuilder<T, K> and(Field field, NoValueOperator noValueOperator);

    // Order
    QueryBuilder<T, K> orderBy(Field field);
    QueryBuilder<T, K> desc();
    QueryBuilder<T, K> asc();

    // Actions
    List<T> get();
    T getUnique();
    // TODO Add delete options here
}
