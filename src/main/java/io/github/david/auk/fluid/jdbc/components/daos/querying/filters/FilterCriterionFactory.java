package io.github.david.auk.fluid.jdbc.components.daos.querying.filters;

import io.github.david.auk.fluid.jdbc.components.daos.querying.operator.NoValueOperator;
import io.github.david.auk.fluid.jdbc.components.daos.querying.operator.ValueOperator;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

public class FilterCriterionFactory {

    public static FilterCriterion<? extends TableEntity, ?> buildFilterCriterion(FilterTypedField<? extends TableEntity, ?> filterTypedField, ValueOperator valueOperator, Object value) {

        return buildFilterCriterionInternal(filterTypedField, valueOperator, value);
    }

    private static <V> FilterCriterion<? extends TableEntity, V> buildFilterCriterionInternal(
            FilterTypedField<? extends TableEntity, V> filterTypedField,
            ValueOperator valueOperator,
            Object value
    ) {
        Class<V> expectedType = filterTypedField.typedField().valueType();

        if (!expectedType.isInstance(value)) {
            throw new IllegalArgumentException(
                    "Invalid value type for field '" + filterTypedField.typedField().name() +
                            "'. Expected: " + expectedType.getName() +
                            ", got: " + (value == null ? "null" : value.getClass().getName())
            );
        }

        V typedValue = expectedType.cast(value);

        return FilterCriterion.of(
                filterTypedField,
                valueOperator,
                typedValue
        );
    }

    public static FilterCriterion<? extends TableEntity, ?> buildFilterCriterion(FilterTypedField<? extends TableEntity, ?> filterTypedField, NoValueOperator valueOperator) {
        return FilterCriterion.of(
                filterTypedField,
                valueOperator
        );
    }
}
