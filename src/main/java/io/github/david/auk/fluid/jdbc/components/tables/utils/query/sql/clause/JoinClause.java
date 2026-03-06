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
            String foreignTableName = TableUtils.getTableName(foreignFilterTypedField.getForeignClass());
            String localTableName = TableUtils.getTableName(foreignFilterTypedField.getLocalClass());

            String foreignColumnName = foreignFilterTypedField.getForeignColumnName();
            String localColumnName = foreignFilterTypedField.getLocalColumnName();

            joinClauses.add(generateJoin(foreignTableName, foreignColumnName, localTableName, localColumnName));
        }
    }

    private static String generateJoin(String foreignTableName, String foreignColumnName, String localTableName, String localColumnName) {
        return "JOIN " + foreignTableName + " ON "
                + foreignTableName + "." + foreignColumnName
                + " = "
                + localTableName + "." + localColumnName;
    }
}