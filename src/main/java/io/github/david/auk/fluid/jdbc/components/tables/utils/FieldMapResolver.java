package io.github.david.auk.fluid.jdbc.components.tables.utils;

import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.internal.tables.meta.TypedField;
import io.github.david.auk.fluid.jdbc.annotations.table.field.TableColumn;
import io.github.david.auk.fluid.jdbc.annotations.table.constructor.TableInherits;
import io.github.david.auk.fluid.jdbc.annotations.table.field.PrimaryKey;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class FieldMapResolver {

    private FieldMapResolver() {}

    public static <TE extends TableEntity> Map<TypedField<? extends TableEntity, ?>, String> mapFieldToColumnNames(Class<TE> tableEntityClass) {
        Objects.requireNonNull(tableEntityClass, "tableEntityClass");

        Map<Field, String> fieldToColumnNames = new LinkedHashMap<>();

        for (Field field : tableEntityClass.getDeclaredFields()) {
            if (!field.isAnnotationPresent(TableColumn.class)) {
                continue;
            }

            field.setAccessible(true);
            fieldToColumnNames.putIfAbsent(field, TableUtils.getColumnName(field));
        }

        // Only map columns declared on the current entity class.
        // For @TableInherits chains, the child table should only contribute its own columns
        // plus the shared parent primary key column used for the join/shared identifier.
        addInheritedParentPrimaryKeyColumn(tableEntityClass, fieldToColumnNames);

        Map<TypedField<? extends TableEntity, ?>, String> typedFieldToColumnNames = new LinkedHashMap<>();

        for (Map.Entry<Field, String> entry : fieldToColumnNames.entrySet()) {
            Field field = entry.getKey();
            String columnName = entry.getValue();

            @SuppressWarnings("unchecked")
            TypedField<? extends TableEntity, Object> typedField = (TypedField<? extends TableEntity, Object>) TypedField.of(field);

            typedFieldToColumnNames.put(typedField, columnName);
        }

        return typedFieldToColumnNames;
    }

    private static void addInheritedParentPrimaryKeyColumn(
            Class<? extends TableEntity> tableEntityClass,
            Map<Field, String> fieldToColumnNames
    ) {
        if (!tableEntityClass.isAnnotationPresent(TableInherits.class)) {
            return;
        }

        Field parentPrimaryKeyField = findInheritedParentPrimaryKeyField(tableEntityClass);
        if (parentPrimaryKeyField == null) {
            return;
        }

        parentPrimaryKeyField.setAccessible(true);
        fieldToColumnNames.putIfAbsent(parentPrimaryKeyField, TableUtils.getColumnName(parentPrimaryKeyField));
    }

    private static Field findInheritedParentPrimaryKeyField(Class<? extends TableEntity> tableEntityClass) {
        TableInherits tableInherits = tableEntityClass.getAnnotation(TableInherits.class);
        if (tableInherits == null) {
            return null;
        }

        Class<? extends TableEntity> parentClass = resolveParentClass(tableInherits);

        for (Field field : parentClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(PrimaryKey.class)) {
                return field;
            }
        }

        if (parentClass.isAnnotationPresent(TableInherits.class)) {
            return findInheritedParentPrimaryKeyField(parentClass);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends TableEntity> resolveParentClass(TableInherits tableInherits) {
        return (Class<? extends TableEntity>) tableInherits.value();
    }
}
