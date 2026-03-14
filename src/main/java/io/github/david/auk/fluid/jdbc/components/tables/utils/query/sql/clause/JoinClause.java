package io.github.david.auk.fluid.jdbc.components.tables.utils.query.sql.clause;

import io.github.david.auk.fluid.jdbc.components.daos.querying.filters.FilterCriterion;
import io.github.david.auk.fluid.jdbc.components.daos.querying.filters.ForeignFilterTypedField;
import io.github.david.auk.fluid.jdbc.components.tables.utils.TableUtils;

import java.util.List;
import java.util.StringJoiner;

public final class JoinClause {

    private JoinClause() {}

    public static String build(List<FilterCriterion<?, ?>> criteria) {

        if (criteria == null || criteria.isEmpty())
            return "";


        StringJoiner joinClauses = new StringJoiner(" ");

        for (FilterCriterion<?, ?> criterion : criteria) {
            appendJoin(joinClauses, criterion);
        }

        return joinClauses.toString();
    }

    private static void appendJoin(StringJoiner joinClauses, FilterCriterion<?, ?> criterion) {

        if (criterion.getFilterTypedField() instanceof ForeignFilterTypedField<?, ?> foreignFilterTypedField) {
            String referencedTableName = TableUtils.getTableName(foreignFilterTypedField.getForeignClass());
            String referencedColumn = foreignFilterTypedField.getReferencedColumn();

            String localTableName = TableUtils.getTableName(foreignFilterTypedField.getLocalClass());
            String foreignKeyColumnName = foreignFilterTypedField.getForeignKeyColumn();

            joinClauses.add(generateJoin(referencedTableName, referencedColumn, localTableName, foreignKeyColumnName));
        }
    }

    private static String generateJoin(String referencedTableName, String referencedColumn, String localTableName, String foreignKeyColumnName) {
        return "JOIN " + referencedTableName + " ON "
                + referencedTableName + "." + referencedColumn
                + " = "
                + localTableName + "." + foreignKeyColumnName;
    }
}