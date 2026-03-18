package io.github.david.auk.fluid.jdbc.components.tables.utils.query.sql.factories;

import io.github.david.auk.fluid.jdbc.components.daos.querying.filters.FilterCriterion;
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
            String tableName,
            List<FilterCriterion<?, ?>> filterCriteria
    ) {
        return String.join(" ",
                "DELETE",
                FromClause.build(tableName),
                WhereClause.build(filterCriteria)
        ).trim().replaceAll(" +", " ");
    }
}