package io.github.david.auk.fluid.jdbc.components.tables.utils.query.sql.factories;

import io.github.david.auk.fluid.jdbc.components.daos.querying.filters.FilterCriterion;
import io.github.david.auk.fluid.jdbc.components.daos.querying.operator.ValueOperator;
import io.github.david.auk.fluid.jdbc.components.tables.utils.EnumFormatter;
import io.github.david.auk.fluid.jdbc.components.tables.utils.query.sql.ObjectSetter;
import io.github.david.auk.fluid.jdbc.components.tables.utils.query.sql.clause.*;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Builds SELECT SQL and prepares a PreparedStatement with bound parameters.
 *
 * Clause builders stay "dumb"; this class orchestrates join planning + parameter binding.
 */
public final class SelectQueryFactory {

    private SelectQueryFactory() {}

    /**
     * 1) Generates a complete SELECT query:
     * SELECT base.* FROM base [JOIN ...] [WHERE ...] [ORDER BY ...]
     */
    public static String build(
            String tableName,
            List<FilterCriterion<?, ?>> filterCriteria,
            Field orderByField,
            boolean ascending
    ) {
        String sql = String.join(" ",
                SelectClause.build(tableName),
                FromClause.build(tableName),
                WhereClause.build(filterCriteria),
                OrderByClause.build(tableName, orderByField, ascending)
        ).trim().replaceAll(" +", " ");

        return sql;
    }

    public static void prepareSelectStatement(
            PreparedStatement preparedStatement,
            List<FilterCriterion<?, ?>> filterCriteria
    ) {
        if (preparedStatement == null) {
            throw new IllegalArgumentException("PreparedStatement cannot be null");
        }

        if (filterCriteria == null || filterCriteria.isEmpty()) {
            return;
        }

        int parameterIndex = 1;

        try {
            for (FilterCriterion<?, ?> filterCriterion : filterCriteria) {
                if (filterCriterion.getOperator() instanceof ValueOperator) {
                    Object value = filterCriterion.getValue();

                    if (value instanceof Collection<?> collection) {
                        for (Object item : collection) {
                            ObjectSetter.setObject(preparedStatement, parameterIndex++, item);
                            //preparedStatement.setObject(parameterIndex++, item);
                        }
                    } else {
                        ObjectSetter.setObject(preparedStatement, parameterIndex++, value);
//                        preparedStatement.setObject(parameterIndex++, value);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}