package io.github.david.auk.fluid.jdbc.components.tables.utils;

import io.github.david.auk.fluid.jdbc.annotations.table.constructor.TableInherits;
import io.github.david.auk.fluid.jdbc.annotations.table.field.ForeignKey;
import io.github.david.auk.fluid.jdbc.annotations.table.field.TableColumn;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.internal.tables.meta.TypedField;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class ColumnResolver {
    private ColumnResolver() {}

    /**
     * Resolve the physical column columnName for any field annotated with {@link TableColumn}.
     * If the annotation is present and defines a non-empty columnName, that columnName is used.
     * Otherwise, the Java field columnName is returned.
     */
    public static String getColumnName(Field field) {

        TableColumn tableColumn = field.getAnnotation(TableColumn.class);
        if (tableColumn != null && !tableColumn.columnName().isEmpty()) {
            return tableColumn.columnName();
        }
        return field.getName();
    }

    public static String getColumnName(TypedField<? extends TableEntity, ?> field) {
        return getColumnName(field.reflect());
    }

    public static <LC extends TableEntity, FC extends TableEntity> String getForeignColumnName(TypedField<LC, FC> localTypedField) {

        Class<FC> foreignEntityClass = localTypedField.valueType();

        ForeignKey foreignKey = localTypedField.reflect().getAnnotation(ForeignKey.class);

        // Try the overwrite if filled
        if (foreignKey != null && !foreignKey.overwriteJoinColumnWithUniqueColumnName().isEmpty()) {
            return foreignKey.overwriteJoinColumnWithUniqueColumnName();
        }

        // Default to primaryKey column name
        return TableUtils.getPrimaryKeyColumnName(foreignEntityClass);
    }

    public static <LC extends TableEntity, FC extends TableEntity> Object getForeignColumnValue(LC entity, TypedField<LC, FC> field) {
        return getForeignColumnValueRecursive(entity, field, new ArrayList<>());
    }

    private static Object getForeignColumnValueRecursive(TableEntity entity, TypedField<?, ?> field, List<Field> visitedFields) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(visitedFields, "visitedFields");

        Field reflectiveField = field.reflect();
        if (visitedFields.contains(reflectiveField)) {
            throw new IllegalStateException(
                    "Detected recursive foreign-key cycle while resolving field '" + reflectiveField.getName() + "' on " + entity.getClass().getName()
            );
        }

        visitedFields.add(reflectiveField);

        Object value = getTypedFieldValue(entity, field);
        if (value == null) {
            visitedFields.remove(reflectiveField);
            return null;
        }

        if (!(value instanceof TableEntity foreignEntity)) {
            visitedFields.remove(reflectiveField);
            return value;
        }

        ForeignKey foreignKey = reflectiveField.getAnnotation(ForeignKey.class);

        if (foreignKey != null && !foreignKey.overwriteJoinColumnWithUniqueColumnName().isEmpty()) {
            String foreignColumnName = foreignKey.overwriteJoinColumnWithUniqueColumnName();

            for (TypedField<?, ?> foreignField : getAllColumnFields(foreignEntity.getClass())) {
                if (!getColumnName(asTableEntityTypedField(foreignField)).equals(foreignColumnName)) {
                    continue;
                }

                Object nestedValue = getTypedFieldValue(foreignEntity, foreignField);

                if (nestedValue instanceof TableEntity) {
                    Object resolved = getForeignColumnValueRecursive(foreignEntity, foreignField, visitedFields);
                    visitedFields.remove(reflectiveField);
                    return resolved;
                }

                visitedFields.remove(reflectiveField);
                return nestedValue;
            }

            visitedFields.remove(reflectiveField);
            throw new IllegalArgumentException(
                    "No foreign field with column name '" + foreignColumnName + "' found on " + foreignEntity.getClass().getName()
            );
        }

        Object primaryKeyValue = TableUtils.getPrimaryKeyValue(foreignEntity);
        visitedFields.remove(reflectiveField);
        return primaryKeyValue;
    }

    @SuppressWarnings("unchecked")
    private static Object getTypedFieldValue(TableEntity entity, TypedField<?, ?> field) {
        return ((TypedField<TableEntity, Object>) field).get(entity);
    }

    @SuppressWarnings("unchecked")
    private static TypedField<? extends TableEntity, ?> asTableEntityTypedField(TypedField<?, ?> field) {
        return (TypedField<? extends TableEntity, ?>) field;
    }

    public static <LC extends TableEntity, FC extends TableEntity> TypedField<LC, FC> getLocalFieldOfTypeForeignEntity(Class<LC> entityToBeQueried, Class<FC> foreignClass) {
        for (Field field : entityToBeQueried.getDeclaredFields()) {
            if (!field.isAnnotationPresent(ForeignKey.class)) {
                continue;
            }

            if (!field.getType().equals(foreignClass)) {
                continue;
            }

            field.setAccessible(true);
            return TypedField.of(entityToBeQueried, field, foreignClass);
        }

        throw new IllegalArgumentException(
                "No field annotated with @ForeignKey of type " + foreignClass.getName() + " found on " + entityToBeQueried.getName()
        );
    }

    public static <T extends TableEntity> List<TypedField<T, ?>> getAllColumnFields(Class<T> entityClass) {
        List<TypedField<T, ?>> result = new ArrayList<>();

        for (Field field : entityClass.getDeclaredFields()) {
            if (!field.isAnnotationPresent(TableColumn.class)) {
                continue;
            }

            field.setAccessible(true);

            Class<?> valueType = field.getType();

            result.add(TypedField.of(entityClass, field, valueType));
        }

        return result;
    }

    // --- Inherited primary key resolution ---
    // See: TableInherits and inherited PK logic.
    static Object resolveInheritedPrimaryKeyIfNeeded(Class<? extends TableEntity> clazz, TableEntity entity) {
        if (clazz.isAnnotationPresent(TableInherits.class) && TableUtils.getPrimaryKeyValue(entity) == null) {
            return getInheritedPrimaryKeyValue(entity, clazz);
        }
        return null;
    }

    private static Object getInheritedPrimaryKeyValue(TableEntity entity, Class<? extends TableEntity> clazz) {
        Objects.requireNonNull(entity, "entity");
        Objects.requireNonNull(clazz, "clazz");

        TableInherits tableInherits = clazz.getAnnotation(TableInherits.class);
        if (tableInherits == null) {
            return null;
        }

        Object parentEntity = getParentEntityInstance(entity, clazz, tableInherits);
        if (!(parentEntity instanceof TableEntity parentTableEntity)) {
            return null;
        }

        Object parentPrimaryKeyValue = TableUtils.getPrimaryKeyValue(parentTableEntity);
        if (parentPrimaryKeyValue != null) {
            return parentPrimaryKeyValue;
        }

        if (parentTableEntity.getClass().isAnnotationPresent(TableInherits.class)) {
            return getInheritedPrimaryKeyValue(parentTableEntity, parentTableEntity.getClass());
        }

        return null;
    }

    private static Object getParentEntityInstance(
            TableEntity entity,
            Class<? extends TableEntity> clazz,
            TableInherits tableInherits
    ) {
        Class<? extends TableEntity> parentClass = resolveParentClass(tableInherits);

        for (Field field : clazz.getDeclaredFields()) {
            if (!parentClass.isAssignableFrom(field.getType())) {
                continue;
            }

            field.setAccessible(true);
            try {
                return field.get(entity);
            } catch (IllegalAccessException exception) {
                throw new RuntimeException(
                        "Failed to access inherited parent field '" + field.getName() + "' on " + clazz.getName(),
                        exception
                );
            }
        }

        throw new IllegalArgumentException(
                "Could not find a parent entity field of type " + parentClass.getName() + " on " + clazz.getName()
        );
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends TableEntity> resolveParentClass(TableInherits tableInherits) {
        try {
            Method valueMethod = tableInherits.annotationType().getMethod("value");
            Object resolvedValue = valueMethod.invoke(tableInherits);
            if (resolvedValue instanceof Class<?> resolvedClass && TableEntity.class.isAssignableFrom(resolvedClass)) {
                return (Class<? extends TableEntity>) resolvedClass;
            }
        } catch (ReflectiveOperationException ignored) {
            // Fall through to the explicit error below.
        }

        throw new IllegalStateException(
                "@TableInherits must expose a value() that resolves to a TableEntity parent class"
        );
    }

}
