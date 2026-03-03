package io.github.david.auk.fluid.jdbc.components.tables.utils.query.sql.factories;

import io.github.david.auk.fluid.jdbc.components.daos.querying.FilterCriterion.FilterCriterion;
import io.github.david.auk.fluid.jdbc.components.tables.Table;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.components.tables.utils.query.sql.clause.*;

import java.util.*;

/**
 * Builds SELECT SQL and prepares a PreparedStatement with bound parameters.
 *
 * Clause builders stay "dumb"; this class orchestrates join planning + parameter binding.
 */
public final class DeleteQueryFactory {

    private DeleteQueryFactory() {}

    /**
     * 1) Generates a complete DELETE query:
     * DELETE FROM base [JOIN ...] [WHERE ...] [ORDER BY ...]
     */
    public static String buildSelectSql(
            Table<? extends TableEntity, ?> table,
            List<FilterCriterion> filterCriteria
    ) {

        String tableName = table.getTableName();
        Class<? extends TableEntity> baseEntityClass = table.getTableEntityClass();

        String whereSql = WhereClause.build(baseEntityClass, filterCriteria);

        return String.join(" ",
                "DELETE",
                FromClause.build(tableName),
                whereSql
        ).trim().replaceAll(" +", " ");
    }
}