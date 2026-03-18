package io.github.david.auk.fluid.jdbc.components.tables.utils;

import io.github.david.auk.fluid.jdbc.annotations.table.constructor.TableInherits;
import io.github.david.auk.fluid.jdbc.annotations.table.field.ForeignKey;
import io.github.david.auk.fluid.jdbc.annotations.table.field.TableColumn;
import io.github.david.auk.fluid.jdbc.components.daos.querying.relations.EntityRelation;
import io.github.david.auk.fluid.jdbc.components.daos.querying.relations.RelationKind;
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
        EntityRelation<LC, FC> relation = resolveEntityRelation(localTypedField.owner(), localTypedField.valueType());
        return getColumnName(relation.anchorField());
    }

    public static <LC extends TableEntity, FC extends TableEntity> Object getForeignColumnValue(LC entity, TypedField<LC, FC> field) {
        return getForeignColumnValueRecursive(entity, field, new ArrayList<>());
    }

    private static <LC extends TableEntity, FC extends TableEntity> Object getForeignColumnValueRecursive(TableEntity localEntity, TypedField<? extends TableEntity, ?> field, List<Field> visitedFields) {
        Objects.requireNonNull(localEntity, "localEntity");
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(visitedFields, "visitedFields");

        Field reflectiveField = field.reflect();
        if (visitedFields.contains(reflectiveField)) {
            throw new IllegalStateException(
                    "Detected recursive foreign-key cycle while resolving field '" + reflectiveField.getName() + "' on " + localEntity.getClass().getName()
            );
        }

        visitedFields.add(reflectiveField);

        Object value = getTypedFieldValue(localEntity, field);
        if (value == null) {
            visitedFields.remove(reflectiveField);
            return null;
        }

        if (!(value instanceof TableEntity foreignEntityValue)) {
            visitedFields.remove(reflectiveField);
            return value;
        }

        @SuppressWarnings("unchecked")
        FC foreignEntity = (FC) foreignEntityValue;

        @SuppressWarnings("unchecked")
        Class<LC> localClass = (Class<LC>) localEntity.getClass();
        @SuppressWarnings("unchecked")
        Class<FC> foreignClass = (Class<FC>) foreignEntity.getClass();

        EntityRelation<LC, FC> relation = resolveEntityRelation(localClass, foreignClass);

        if (relation.kind() == RelationKind.INHERITANCE) {
            Object inheritedPrimaryKeyValue = TableUtils.getPrimaryKeyValue(foreignEntity);
            visitedFields.remove(reflectiveField);
            return inheritedPrimaryKeyValue;
        }

        TypedField<FC, ?> foreignAnchorTypedField = relation.anchorField();
        Object anchorValue;
        anchorValue = foreignAnchorTypedField.getValue(foreignEntity);
        Field foreignAnchorField = foreignAnchorTypedField.reflect();

        if (anchorValue instanceof TableEntity nestedForeignEntity) {
            TypedField<FC, ?> nestedField = TypedField.of(
                    foreignClass,
                    foreignAnchorField,
                    foreignAnchorField.getType()
            );
            Object resolved = getForeignColumnValueRecursive(nestedForeignEntity, nestedField, visitedFields);
            visitedFields.remove(reflectiveField);
            return resolved;
        }

        visitedFields.remove(reflectiveField);
        return anchorValue;
    }

    @SuppressWarnings("unchecked")
    private static Object getTypedFieldValue(TableEntity entity, TypedField<?, ?> field) {
        return ((TypedField<TableEntity, Object>) field).get(entity);
    }

    @SuppressWarnings("unchecked")
    private static TypedField<? extends TableEntity, ?> asTableEntityTypedField(TypedField<?, ?> field) {
        return (TypedField<? extends TableEntity, ?>) field;
    }

    // Resolve how one entity type relates to another. The relation may be an explicit
    // @ForeignKey field or a shared-primary-key inheritance link declared through
    // @TableInherits. Keeping that information in EntityRelation avoids scattering
    // relation-kind checks throughout the rest of the resolver logic.
    public static <LC extends TableEntity, FC extends TableEntity> EntityRelation<LC, FC> resolveEntityRelation(
            Class<LC> localClass,
            Class<FC> foreignClass
    ) {
        Objects.requireNonNull(localClass, "localClass");
        Objects.requireNonNull(foreignClass, "foreignClass");

        for (Field field : localClass.getDeclaredFields()) {
            if (!field.isAnnotationPresent(ForeignKey.class)) {
                continue;
            }

            if (!field.getType().isAssignableFrom(foreignClass) && !foreignClass.isAssignableFrom(field.getType())) {
                continue;
            }

            TypedField<LC, FC> localField = TypedField.of(localClass, field, foreignClass);

            TypedField<FC, Object> foreignAnchorField = getForeignValueMatchingTypedField(localField);

            return new EntityRelation<>(
                    localClass,
                    foreignClass,
                    RelationKind.FOREIGN_KEY,
                    TableUtils.getTableName(localClass),
                    TableUtils.getColumnName(localField),
                    TableUtils.getTableName(foreignClass),
                    TableUtils.getColumnName(foreignAnchorField),
                    localField,
                    foreignAnchorField
            );
        }

        if (isInheritedRelation(localClass, foreignClass)) {
            TypedField<FC, Object> foreignAnchorField = TypedField.of(foreignClass, getPrimaryKeyField(foreignClass), Object.class);

            return new EntityRelation<>(
                    localClass,
                    foreignClass,
                    RelationKind.INHERITANCE,
                    TableUtils.getTableName(localClass),
                    TableUtils.getPrimaryKeyColumnName(localClass),
                    TableUtils.getTableName(foreignClass),
                    TableUtils.getPrimaryKeyColumnName(foreignClass),
                    null,
                    foreignAnchorField
            );
        }

        throw new IllegalArgumentException(
                "No relation from " + localClass.getName() + " to " + foreignClass.getName()
        );
    }

    private static <LC extends TableEntity, FC extends TableEntity> TypedField<FC, Object> getForeignValueMatchingTypedField(TypedField<LC, FC> localField) {
        Class<FC> foreignClass = localField.valueType();
        Field localAnchorField = localField.reflect();

        ForeignKey foreignKey = localAnchorField.getAnnotation(ForeignKey.class);

        // Use the overwrite value if defined
        if (foreignKey != null && !foreignKey.overwriteJoinColumnWithUniqueColumnName().isEmpty()) {
            String foreignColumnName = foreignKey.overwriteJoinColumnWithUniqueColumnName();
            return TypedField.of(foreignClass, foreignColumnName, Object.class);
        }

        // Default to the primary key
        Field foreignPrimaryKeyField = getPrimaryKeyField(foreignClass);
        return TypedField.of(foreignClass, foreignPrimaryKeyField, Object.class);
    }

    private static Field getPrimaryKeyField(Class<? extends TableEntity> entityClass) {
        Object primaryKeyMember = TableUtils.getPrimaryKeyMember(entityClass);
        if (primaryKeyMember instanceof Field primaryKeyField) {
            primaryKeyField.setAccessible(true);
            return primaryKeyField;
        }

        throw new IllegalStateException(
                "Primary key member of " + entityClass.getName() + " is not a Field"
        );
    }

    // Shared-PK inheritance is treated as a first-class entity relation. The concrete
    // relation object is produced by resolveEntityRelation(...); this helper only checks
    // whether such an inheritance link exists in the @TableInherits chain.
    private static boolean isInheritedRelation(
            Class<? extends TableEntity> entityClass,
            Class<? extends TableEntity> foreignClass
    ) {
        Class<? extends TableEntity> currentClass = entityClass;

        while (currentClass.isAnnotationPresent(TableInherits.class)) {
            Class<? extends TableEntity> parentClass = resolveParentClass(currentClass.getAnnotation(TableInherits.class));
            if (parentClass.equals(foreignClass) || parentClass.isAssignableFrom(foreignClass) || foreignClass.isAssignableFrom(parentClass)) {
                return true;
            }
            currentClass = parentClass;
        }

        return false;
    }


    // For foreign-key resolution we sometimes need to inspect the columns declared on
    // @TableInherits parent entities as well, because the join-column overwrite may
    // target a base-table column rather than a column declared directly on the child.
    public static <T extends TableEntity> List<TypedField<T, ?>> getAllColumnFields(Class<T> entityClass) {
        List<TypedField<T, ?>> result = new ArrayList<>();
        addDeclaredColumnFields(entityClass, entityClass, result);
        return result;
    }

    public static <T extends TableEntity> List<TypedField<T, ?>> getAllColumnFieldsIncludingInheritedBases(Class<T> entityClass) {
        List<TypedField<T, ?>> result = new ArrayList<>();
        addDeclaredColumnFields(entityClass, entityClass, result);

        Class<? extends TableEntity> currentClass = entityClass;
        while (currentClass.isAnnotationPresent(TableInherits.class)) {
            Class<? extends TableEntity> parentClass = resolveParentClass(currentClass.getAnnotation(TableInherits.class));
            addDeclaredColumnFields(entityClass, parentClass, result);
            currentClass = parentClass;
        }

        return result;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T extends TableEntity> void addDeclaredColumnFields(
            Class<T> ownerClass,
            Class<? extends TableEntity> declaredOnClass,
            List<TypedField<T, ?>> result
    ) {
        for (Field field : declaredOnClass.getDeclaredFields()) {
            if (!field.isAnnotationPresent(TableColumn.class)) {
                continue;
            }

            field.setAccessible(true);
            Class<?> valueType = field.getType();

            result.add((TypedField<T, ?>) TypedField.of((Class) ownerClass, field, valueType));
        }
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

        if (parentClass.isAssignableFrom(entity.getClass())) {
            return entity;
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