package io.github.david.auk.fluid.jdbc.components.tables.utils.query.sql.clause;

import io.github.david.auk.fluid.jdbc.annotations.table.field.ForeignKey;
import io.github.david.auk.fluid.jdbc.components.daos.querying.FilterCriterion.FilterCriterion;
import io.github.david.auk.fluid.jdbc.components.daos.querying.operator.*;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.components.tables.utils.TableUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

public final class WhereClause {

    private WhereClause() {}

    public static String build(
            Class<? extends TableEntity> baseEntityClass,
            List<FilterCriterion> filterCriteria
    ) {
        if (filterCriteria == null || filterCriteria.isEmpty()) {
            return "";
        }

        // Build the necessary join statement for this query that might use foreign tables
        String joinSql = JoinClause.build(baseEntityClass, filterCriteria);

        StringJoiner joiner = new StringJoiner(" AND ");
        for (FilterCriterion filterCriterion : filterCriteria) {
            joiner.add(buildPredicate(
                    baseEntityClass,
                    filterCriterion
            ));
        }

        String whereSql = "WHERE " + joiner;

        StringJoiner completeQuery = new StringJoiner(" ");

        if (joinSql != null && !joinSql.isBlank()) {
            completeQuery.add(joinSql);
        }

        completeQuery.add(whereSql);

        return completeQuery.toString();
    }

    private static String buildPredicate(
            Class<? extends TableEntity> baseEntityClass,
            FilterCriterion filterCriterion
    ) {
        String qualifiedCol = qualifyColumn(
                baseEntityClass,
                filterCriterion
        );


        Operator operator = filterCriterion.getOperator();

        // no-value operator: "col IS NULL"
        if (operator instanceof NoValueOperator) {
            return qualifiedCol + " " + operator.primary();
        }

        // value operator: "col IN (?, ?, ?)" etc.
        if (operator instanceof ValueOperator valueOperator) {
            return qualifiedCol + " " + operator.primary() + " " + valuePlaceholder(valueOperator, filterCriterion.getValue());
        }

        throw new IllegalStateException("Unsupported operator type: " + operator.getClass());
    }

    private static String qualifyColumn(
            Class<? extends TableEntity> baseEntityClass,
            FilterCriterion filterCriterion
    ) {
        String tableName = TableUtils.getTableName(baseEntityClass);
        String columnName = TableUtils.getColumnName(filterCriterion.getField());

        return tableName + "." + columnName;
    }

    private static String valuePlaceholder(ValueOperator op, Object value) {
        if (op instanceof SingleValueOperator) {
            return "?";
        }

        if (!(value instanceof Collection<?> values)) {
            throw new IllegalArgumentException(
                    "Non-collection value is not supported for " + op.getClass().getSimpleName());
        }

        if (values.isEmpty()) {
            throw new IllegalArgumentException("Must have at least one value");
        }

        if (op instanceof RangeOperator) {
            if (values.size() != 2) throw new IllegalArgumentException("Must have 2 values for RANGE operators");
            return "? AND ?";
        }

        if (op instanceof MultiOperator) {
            StringBuilder sb = new StringBuilder("(");
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append("?");
            }
            sb.append(")");
            return sb.toString();
        }

        throw new IllegalStateException("Unsupported ValueOperator type: " + op.getClass());
    }
}