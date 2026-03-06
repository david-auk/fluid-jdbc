package io.github.david.auk.fluid.jdbc.components.tables.utils;

import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.internal.tables.meta.TypedField;
import io.github.david.auk.fluid.jdbc.annotations.table.field.TableColumn;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class FieldMapResolver {

    private FieldMapResolver() {}

    public static <TE extends TableEntity> Map<TypedField<TE, Object>, String> mapFieldToColumnNames(Class<TE> tableEntityClass) {
        Objects.requireNonNull(tableEntityClass, "tableEntityClass");

        Map<Field, String> fieldToColumnNames = new LinkedHashMap<>();

        Class<?> current = tableEntityClass;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (!field.isAnnotationPresent(TableColumn.class)) {
                    continue;
                }

                field.setAccessible(true);

                TableColumn column = field.getAnnotation(TableColumn.class);
                String columnName = column.columnName().isBlank()
                        ? field.getName()
                        : column.columnName();

                fieldToColumnNames.put(field, columnName);
            }

            current = current.getSuperclass();
        }

        Map<TypedField<TE, Object>, String> typedFieldToColumnNames = new LinkedHashMap<>();

        for (Map.Entry<Field, String> entry : fieldToColumnNames.entrySet()) {
            Field field = entry.getKey();
            String columnName = entry.getValue();

            TypedField<TE, Object> typedField = TypedField.of(tableEntityClass, field.getName(), Object.class);
            typedFieldToColumnNames.put(typedField, columnName);
        }

        return typedFieldToColumnNames;
    }
}
