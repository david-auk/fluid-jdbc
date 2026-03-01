package io.github.david.auk.fluid.jdbc.components.daos.querying.FilterCriterion;

import io.github.david.auk.fluid.jdbc.components.daos.querying.operator.NoValueOperator;
import io.github.david.auk.fluid.jdbc.components.daos.querying.operator.Operator;
import io.github.david.auk.fluid.jdbc.components.daos.querying.operator.ValueOperator;

import java.lang.reflect.Field;

public class FilterCriterion {

    final private Field field;
    final private Operator operator;
    final private Object value;

    public FilterCriterion(Field field, ValueOperator operator, Object value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }
    public FilterCriterion(Field field, NoValueOperator operator) {
        this.field = field;
        this.operator = operator;
        this.value = null;
    }

    public Field getField() {
        return field;
    }

    public Operator getOperator() {
        return operator;
    }

    public Object getValue() {
        if (NoValueOperator.class.isAssignableFrom(operator.getClass())) {
            throw new  IllegalArgumentException("Filter operator type: " + operator.getClass().getName()+ " does not support values");
        }

        return value;
    }
}
