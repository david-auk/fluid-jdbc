package io.github.david.auk.fluid.jdbc.components.tables.utils.query.sql.clause;

import io.github.david.auk.fluid.jdbc.annotations.table.field.ForeignKey;
import io.github.david.auk.fluid.jdbc.components.daos.querying.FilterCriterion.FilterCriterion;
import io.github.david.auk.fluid.jdbc.components.tables.Table;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.components.tables.utils.TableUtils;

import java.lang.reflect.Field;
import java.util.*;

public final class JoinPlanner {

    private JoinPlanner() {}

    public static Map<Class<?>, JoinInfo> buildRequiredJoins(
            String baseTableName,
            Class<? extends TableEntity> baseEntityClass,
            Map<Field, String> baseFieldToColumn,
            List<FilterCriterion> filters
    ) {
        if (filters == null || filters.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Class<?>, JoinInfo> joins = new LinkedHashMap<>();

        for (FilterCriterion criterion : filters) {
            Field f = criterion.getField();
            if (f == null) continue;

            Class<?> declaring = f.getDeclaringClass();
            if (declaring.equals(baseEntityClass)) continue;
            if (joins.containsKey(declaring)) continue;

            if (!TableEntity.class.isAssignableFrom(declaring)) {
                throw new IllegalArgumentException(
                        "Filter field " + f.getName() + " is declared on " + declaring.getName()
                                + " which is not a TableEntity; cannot join");
            }

            @SuppressWarnings("unchecked")
            Class<? extends TableEntity> refClass = (Class<? extends TableEntity>) declaring;

            List<Field> matchingFkFields = Arrays.stream(baseEntityClass.getDeclaredFields())
                    .filter(local -> local.isAnnotationPresent(ForeignKey.class))
                    .filter(local -> local.getType().equals(refClass))
                    .toList();

            if (matchingFkFields.isEmpty()) {
                throw new IllegalArgumentException(
                        "No @ForeignKey field on " + baseEntityClass.getName() + " points to " + refClass.getName());
            }
            if (matchingFkFields.size() > 1) {
                throw new IllegalArgumentException(
                        "Multiple @ForeignKey fields on " + baseEntityClass.getName()
                                + " point to " + refClass.getName() + "; join target is ambiguous");
            }

            Field fkField = matchingFkFields.get(0);
            String baseFkColumn = baseFieldToColumn.get(fkField);
            if (baseFkColumn == null) {
                throw new IllegalArgumentException(
                        "Missing FK field mapping for " + fkField.getName() + " in table " + baseTableName);
            }

            String refTableName = TableUtils.getTableName(refClass);

            Table<? extends TableEntity, Object> refTable = new Table<>(refClass);
            final String refPkColumn;
            try {
                refPkColumn = refTable.getPrimaryKeyColumnName();
            } catch (UnsupportedOperationException e) {
                throw new UnsupportedOperationException(
                        "Joining on composite/method-based PK is not supported yet for " + refClass.getName(), e);
            }

            Map<Field, String> refFieldToColumn = TableUtils.mapFieldToColumnNames(refClass);

            joins.put(refClass, new JoinInfo(refTableName, baseFkColumn, refPkColumn, refFieldToColumn));
        }

        return joins;
    }
}