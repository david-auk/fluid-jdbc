package io.github.david.auk.fluid.jdbc.components.tables.utils;

import io.github.david.auk.fluid.jdbc.annotations.table.field.ForeignKey;
import io.github.david.auk.fluid.jdbc.annotations.table.field.TableColumn;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.internal.tables.meta.TypedField;

import java.lang.reflect.Field;

final class ColumnResolver {
    private ColumnResolver() {}

    /**
     * Resolve the physical column columnName for any field annotated with {@link TableColumn}.
     * If the annotation is present and defines a non-empty columnName, that columnName is used.
     * Otherwise, the Java field columnName is returned.
     */
    public static String getColumnName(TypedField<? extends TableEntity, ?> typedField) {

        Field field = typedField.reflect();

        TableColumn tableColumn = field.getAnnotation(TableColumn.class);
        if (tableColumn != null && !tableColumn.columnName().isEmpty()) {
            return tableColumn.columnName();
        }
        return field.getName();
    }

    public static <LC extends TableEntity, FC extends TableEntity> String getForeignColumnName(TypedField<LC, ?> field, Class<FC> foreignEntityClass) {


        ForeignKey foreignKey = field.reflect().getAnnotation(ForeignKey.class);

        // Try the overwrite if filled
        if (foreignKey != null && !foreignKey.overwriteJoinColumnWithUniqueColumnName().isEmpty()) {
            return foreignKey.overwriteJoinColumnWithUniqueColumnName();
        }

        // Default to primaryKey column name
        return TableUtils.getPrimaryKeyColumnName(foreignEntityClass);
    }

    public static <LC extends TableEntity, FC extends TableEntity> Field getLocalFieldOfTypeForeignEntity(Class<LC> entityToBeQueried, Class<FC> foreignClass) {
        for (Field field : entityToBeQueried.getDeclaredFields()) {
            if (!field.isAnnotationPresent(ForeignKey.class)) {
                continue;
            }

            if (!field.getType().equals(foreignClass)) {
                continue;
            }

            field.setAccessible(true);
            return field;
        }

        throw new IllegalArgumentException(
                "No field annotated with @ForeignKey of type " + foreignClass.getName() + " found on " + entityToBeQueried.getName()
        );
    }
}
