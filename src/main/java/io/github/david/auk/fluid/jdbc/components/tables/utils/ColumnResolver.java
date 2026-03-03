package io.github.david.auk.fluid.jdbc.components.tables.utils;

import io.github.david.auk.fluid.jdbc.annotations.table.field.TableColumn;

import java.lang.reflect.Field;

final class ColumnResolver {
    private ColumnResolver() {}

    /**
     * Resolve the physical column name for any field annotated with {@link TableColumn}.
     * If the annotation is present and defines a non-empty name, that name is used.
     * Otherwise, the Java field name is returned.
     */
    public static String getColumnName(Field field) {
        TableColumn tableColumn = field.getAnnotation(TableColumn.class);
        if (tableColumn != null && !tableColumn.name().isEmpty()) {
            return tableColumn.name();
        }
        return field.getName();
    }
}
