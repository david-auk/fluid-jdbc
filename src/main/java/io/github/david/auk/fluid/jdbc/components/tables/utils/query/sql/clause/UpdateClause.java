package io.github.david.auk.fluid.jdbc.components.tables.utils.query.sql.clause;

import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.components.tables.utils.TableUtils;
import io.github.david.auk.fluid.jdbc.internal.tables.meta.TypedField;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.util.Map;
import java.util.StringJoiner;

public final class UpdateClause {

    private UpdateClause() {}

    public static <T extends TableEntity> String build(Class<T> clazz) {
        return build(clazz, false);
    }

    public static <T extends TableEntity> String build(Class<T> clazz, boolean allowPrimaryKeyUpdate) {
        String tableName = TableUtils.getTableName(clazz);
        Map<TypedField<T, Object>, String> fieldToColumnNames = TableUtils.mapFieldToColumnNames(clazz);

        if (fieldToColumnNames.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot build UPDATE clause for " + clazz.getName() + ": no @TableColumn fields found"
            );
        }

        AccessibleObject pkMember = TableUtils.getPrimaryKeyMember(clazz);

        StringJoiner setJoiner = new StringJoiner(", ");
        StringJoiner whereJoiner = new StringJoiner(" AND ");

        if (pkMember instanceof Field pkField) {
            String pkColumn = fieldToColumnNames.entrySet().stream()
                    .filter(e -> e.getKey().name().equals(pkField.getName()))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Primary key column not found for " + clazz.getName()));

            for (Map.Entry<TypedField<T, Object>, String> entry : fieldToColumnNames.entrySet()) {
                if (allowPrimaryKeyUpdate || !entry.getValue().equals(pkColumn)) {
                    setJoiner.add(entry.getValue() + " = ?");
                }
            }

            whereJoiner.add(pkColumn + " = ?");
        }
        else if (pkMember instanceof Method) {
            for (String column : fieldToColumnNames.values()) {
                setJoiner.add(column + " = ?");
                whereJoiner.add(column + " = ?");
            }
        }
        else {
            throw new IllegalStateException("Unsupported primary key member for " + clazz.getName());
        }

        return "UPDATE " + tableName + " SET " + setJoiner + " WHERE " + whereJoiner;
    }

    public static <TE extends TableEntity> void prepareUpdateStatement(PreparedStatement updateStatement, TE entity) {
        prepareUpdateStatement(updateStatement, entity, entity, false);
    }

    public static <TE extends TableEntity> void prepareUpdateStatement(
            PreparedStatement updateStatement,
            TE originalEntity,
            TE updatedEntity,
            boolean allowPrimaryKeyUpdate
    ) {
        if (updateStatement == null) {
            throw new IllegalArgumentException("PreparedStatement cannot be null");
        }
        if (originalEntity == null) {
            throw new IllegalArgumentException("Original entity cannot be null");
        }
        if (updatedEntity == null) {
            throw new IllegalArgumentException("Updated entity cannot be null");
        }

        @SuppressWarnings("unchecked")
        Class<TE> clazz = (Class<TE>) updatedEntity.getClass();

        if (!clazz.equals(originalEntity.getClass())) {
            throw new IllegalArgumentException(
                    "Original entity and updated entity must be of the same class"
            );
        }

        Map<TypedField<TE, Object>, String> fieldToColumnNames = TableUtils.mapFieldToColumnNames(clazz);
        AccessibleObject pkMember = TableUtils.getPrimaryKeyMember(clazz);

        int parameterIndex = 1;

        try {
            if (pkMember instanceof Field pkField) {
                Object originalPkValue = null;

                for (TypedField<TE, Object> typedField : fieldToColumnNames.keySet()) {
                    Field reflectField = typedField.reflect();
                    Object updatedValue = reflectField.get(updatedEntity);

                    if (reflectField.getName().equals(pkField.getName())) {
                        originalPkValue = reflectField.get(originalEntity);

                        if (allowPrimaryKeyUpdate) {
                            updateStatement.setObject(parameterIndex++, updatedValue);
                        }
                    }
                    else {
                        updateStatement.setObject(parameterIndex++, updatedValue);
                    }
                }

                if (originalPkValue == null) {
                    throw new IllegalStateException(
                            "Primary key value resolved to null for " + clazz.getName()
                    );
                }

                updateStatement.setObject(parameterIndex, originalPkValue);
            }
            else if (pkMember instanceof Method) {
                for (TypedField<TE, Object> typedField : fieldToColumnNames.keySet()) {
                    Object updatedValue = typedField.reflect().get(updatedEntity);
                    updateStatement.setObject(parameterIndex++, updatedValue);
                }

                for (TypedField<TE, Object> typedField : fieldToColumnNames.keySet()) {
                    Object originalValue = typedField.reflect().get(originalEntity);
                    updateStatement.setObject(parameterIndex++, originalValue);
                }
            }
            else {
                throw new IllegalStateException("Unsupported primary key member for " + clazz.getName());
            }
        }
        catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to prepare UPDATE statement for " + clazz.getName(),
                    e
            );
        }
    }
}
