package io.github.david.auk.fluid.jdbc.components.tables.utils.query.sql.clause;

import io.github.david.auk.fluid.jdbc.annotations.table.field.ForeignKey;
import io.github.david.auk.fluid.jdbc.components.daos.querying.FilterCriterion.FilterCriterion;
import io.github.david.auk.fluid.jdbc.components.tables.Table;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.components.tables.utils.TableUtils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

public final class JoinClause {

    private JoinClause() {}

    public static String build(Class<? extends TableEntity> baseTableEntityClass, List<FilterCriterion> filterCriteria) {

        if (filterCriteria == null || filterCriteria.isEmpty())
            return "";


        StringJoiner joiner = new StringJoiner(" ");

        for (FilterCriterion filterCriterion : filterCriteria) {
            appendJoin(joiner, baseTableEntityClass, filterCriterion);
        }

        return joiner.toString();
    }

    private static void appendJoin(StringJoiner joiner, Class<? extends TableEntity> baseTableEntityClass, FilterCriterion filterCriterion) {

        if (!isForeignKey(baseTableEntityClass, filterCriterion)) return;

        String foreignTableName = getForeignTableName(baseTableEntityClass, filterCriterion);
        String foreignColumnName = getForeignColumnName(baseTableEntityClass, filterCriterion);

        String localTableName = TableUtils.getTableName(baseTableEntityClass);
        String localColumnName = TableUtils.getColumnName(filterCriterion.getField());

        joiner.add(generateJoin(foreignTableName, foreignColumnName, localTableName, localColumnName));
    }

    private static String generateJoin(String foreignTableName, String foreignColumnName, String localTableName, String localColumnName) {
        return "JOIN " + foreignTableName + " ON "
                + foreignTableName + "." + foreignColumnName
                + " = "
                + localTableName + "." + localColumnName;
    }

    private static void validate(Class<? extends TableEntity> baseEntityClass, Field filterField) {
        if (!filterField.getDeclaringClass().isAssignableFrom(baseEntityClass)) {
            throw new IllegalArgumentException("Field " + filterField.getName() + " is not declared in " + baseEntityClass.getName());
        }
    }

    private static String getForeignTableName(Class<? extends TableEntity> baseEntityClass, FilterCriterion filterCriterion) {
        Field filterField = filterCriterion.getField();

        validate(baseEntityClass, filterField);

        if (filterField.isAnnotationPresent(ForeignKey.class) &&
                filterCriterion.getValue() instanceof TableEntity foreignEntity) {
            return TableUtils.getTableName(foreignEntity.getClass());
        }

        throw new IllegalArgumentException("Field " + filterField.getName() + " is not annotated with " + ForeignKey.class.getName());
    }

    private static String getForeignColumnName(Class<? extends TableEntity> baseEntityClass, FilterCriterion filterCriterion) {
        Field filterField = filterCriterion.getField();

        validate(baseEntityClass, filterField);

        if (filterField.isAnnotationPresent(ForeignKey.class) &&
                filterCriterion.getValue() instanceof TableEntity foreignEntity) {
            return TableUtils.getPrimaryKeyColumnName(foreignEntity.getClass());
        }

        throw new IllegalArgumentException("Field " + filterField.getName() + " is not annotated with " + ForeignKey.class.getName());
    }

    private static boolean isForeignKey(Class<? extends TableEntity> baseEntityClass, FilterCriterion filterCriterion) {
        Field filterField = filterCriterion.getField();
        validate(baseEntityClass, filterField);

        return filterField.isAnnotationPresent(ForeignKey.class) &&
                filterCriterion.getValue() instanceof TableEntity;
    }
}