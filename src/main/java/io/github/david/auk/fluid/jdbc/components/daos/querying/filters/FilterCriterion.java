package io.github.david.auk.fluid.jdbc.components.daos.querying.filters;

import io.github.david.auk.fluid.jdbc.components.daos.querying.operator.NoValueOperator;
import io.github.david.auk.fluid.jdbc.components.daos.querying.operator.Operator;
import io.github.david.auk.fluid.jdbc.components.daos.querying.operator.ValueOperator;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

import java.lang.reflect.Field;

public class FilterCriterion<TE extends TableEntity, V> {

    final private FilterTypedField<TE, V> filterTypedField;
    final private Operator operator;
    final private V value;

    public FilterCriterion(FilterTypedField<TE, V> filterTypedField, ValueOperator operator, V value) {
        this.filterTypedField = filterTypedField;
        this.operator = operator;
        this.value = value;
    }
    public FilterCriterion(FilterTypedField<TE, V> filterTypedField, NoValueOperator operator) {
        this.filterTypedField = filterTypedField;
        this.operator = operator;
        this.value = null;
    }

    public static <TE extends TableEntity, V> FilterCriterion<TE, V> of(
            FilterTypedField<TE, V> filterTypedField,
            NoValueOperator operator
    ) {
        return new FilterCriterion<>(filterTypedField, operator);
    }

    public static <TE extends TableEntity, V> FilterCriterion<TE, V> of(
            FilterTypedField<TE, V> filterTypedField,
            ValueOperator operator,
            V value
    ) {
        return new FilterCriterion<>(filterTypedField, operator, value);
    }

    public FilterTypedField<TE, V> getFilterTypedField() {
        return filterTypedField;
    }

    public Field getField() {
        return filterTypedField.typedField().reflect();
    }

    public Operator getOperator() {
        return operator;
    }

    public V getValue() {
        if (NoValueOperator.class.isAssignableFrom(operator.getClass())) {
            throw new  IllegalArgumentException("Filter operator type: " + operator.getClass().getName()+ " does not support values");
        }

        return value;
    }
}
