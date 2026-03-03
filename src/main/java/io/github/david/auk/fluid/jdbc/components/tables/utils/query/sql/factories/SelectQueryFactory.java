package io.github.david.auk.fluid.jdbc.components.tables.utils.query.sql.factories;

import io.github.david.auk.fluid.jdbc.components.daos.querying.FilterCriterion.FilterCriterion;
import io.github.david.auk.fluid.jdbc.components.tables.Table;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.components.tables.utils.query.sql.clause.*;

import java.lang.reflect.Field;
import java.util.*;

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
    public static String buildSelectSql(
            Table<? extends TableEntity, ?> table,
            List<FilterCriterion> filterCriteria,
            Field orderByField,
            boolean ascending
    ) {

        String tableName = table.getTableName();
        Class<? extends TableEntity> baseEntityClass = table.getTableEntityClass();

        String whereSql = WhereClause.build(baseEntityClass, filterCriteria);
        String orderSql = OrderByClause.build(tableName, orderByField, ascending);

        return String.join(" ",
                SelectClause.build(tableName),
                FromClause.build(tableName),
                whereSql,
                orderSql
        ).trim().replaceAll(" +", " ");
    }
}