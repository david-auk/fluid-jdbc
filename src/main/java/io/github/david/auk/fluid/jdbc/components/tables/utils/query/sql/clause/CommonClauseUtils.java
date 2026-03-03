package io.github.david.auk.fluid.jdbc.components.tables.utils.query.sql.clause;

import io.github.david.auk.fluid.jdbc.annotations.table.field.ForeignKey;
import io.github.david.auk.fluid.jdbc.components.daos.querying.FilterCriterion.FilterCriterion;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.components.tables.utils.TableUtils;

import java.lang.reflect.Field;

final class CommonClauseUtils {
    private CommonClauseUtils() {
    }

    static void validate(Class<? extends TableEntity> baseEntityClass, Field filterField) {
        if (!filterField.getDeclaringClass().isAssignableFrom(baseEntityClass)) {
            throw new IllegalArgumentException("Field " + filterField.getName() + " is not declared in " + baseEntityClass.getName());
        }
    }

    static String getForeignTableName(Class<? extends TableEntity> baseEntityClass, FilterCriterion filterCriterion) {
        Field filterField = filterCriterion.getField();

        validate(baseEntityClass, filterField);

        if (filterField.isAnnotationPresent(ForeignKey.class) &&
                filterCriterion.getValue() instanceof TableEntity foreignEntity) {
            return TableUtils.getTableName(foreignEntity.getClass());
        }

        return TableUtils.getTableName(baseEntityClass);
    }

    static boolean isForeignKey(Class<? extends TableEntity> baseEntityClass, FilterCriterion filterCriterion) {
        Field filterField = filterCriterion.getField();
        validate(baseEntityClass, filterField);

        return filterField.isAnnotationPresent(ForeignKey.class) &&
                filterCriterion.getValue() instanceof TableEntity foreignEntity;
    }
}
