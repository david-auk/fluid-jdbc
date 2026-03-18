package io.github.david.auk.fluid.jdbc.components.tables.utils.query.sql.clause;

import io.github.david.auk.fluid.jdbc.components.daos.querying.filters.FilterCriterion;
import io.github.david.auk.fluid.jdbc.components.daos.querying.filters.ForeignFilterTypedField;
import io.github.david.auk.fluid.jdbc.components.daos.querying.relations.EntityRelation;
import io.github.david.auk.fluid.jdbc.components.tables.utils.TableUtils;

import java.util.List;
import java.util.StringJoiner;
import java.util.HashSet;
import java.util.Set;

public final class JoinClause {

    private JoinClause() {}

    public static String build(List<FilterCriterion<?, ?>> criteria) {

        if (criteria == null || criteria.isEmpty())
            return "";


        StringJoiner joinClauses = new StringJoiner(" ");
        Set<String> addedTables = new HashSet<>();

        for (FilterCriterion<?, ?> criterion : criteria) {
            appendJoin(joinClauses, addedTables, criterion);
        }

        return joinClauses.toString();
    }

    private static void appendJoin(StringJoiner joinClauses, Set<String> addedTables, FilterCriterion<?, ?> criterion) {

        if (criterion.getFilterTypedField() instanceof ForeignFilterTypedField<?, ?> foreignFilterTypedField) {

            EntityRelation<?, ?> relation = foreignFilterTypedField.entityRelation();
            String foreignTableName = relation.foreignTableName();

            // Check if we already joined the table
            if (addedTables.contains(foreignTableName)) {
                return;
            }

            joinClauses.add(generateJoin(foreignFilterTypedField.entityRelation()));

            // Mark this reference table as "joined" for new join appends
            addedTables.add(foreignTableName);
        }
    }

    private static String generateJoin(EntityRelation<?, ?> entityRelation) {
        return generateJoin(
                entityRelation.foreignTableName(),
                entityRelation.foreignJoinColumn(),
                entityRelation.localTableName(),
                entityRelation.localJoinColumn()
        );
    }

    private static String generateJoin(String foreignTableName, String foreignTableJoinColumn, String localTableName, String localTableJoinColumn) {
        return "JOIN " + foreignTableName + " ON "
                + foreignTableName + "." + foreignTableJoinColumn
                + " = "
                + localTableName + "." + localTableJoinColumn;
    }
}