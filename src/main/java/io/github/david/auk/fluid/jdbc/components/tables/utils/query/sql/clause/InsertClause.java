package io.github.david.auk.fluid.jdbc.components.tables.utils.query.sql.clause;

import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.components.tables.utils.TableUtils;
import io.github.david.auk.fluid.jdbc.internal.tables.meta.TypedField;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.StringJoiner;

public final class InsertClause {

    private InsertClause() {
    }

    public static <T extends TableEntity> String build(Class<T> clazz) {
        String tableName = TableUtils.getTableName(clazz);
        Map<TypedField<? extends TableEntity, ?>, String> fieldToColumnNames = TableUtils.mapFieldToColumnNames(clazz);

        if (fieldToColumnNames.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot build INSERT clause for " + clazz.getName() + ": no @TableColumn fields found"
            );
        }

        StringJoiner columnJoiner = new StringJoiner(", ");
        StringJoiner valueJoiner = new StringJoiner(", ");

        for (Map.Entry<TypedField<? extends TableEntity, ?>, String> entry : fieldToColumnNames.entrySet()) {
            columnJoiner.add(entry.getValue());
            valueJoiner.add("?");
        }

        return "INSERT INTO " + tableName + " (" + columnJoiner + ") VALUES (" + valueJoiner + ")";
    }

    public static <TE extends TableEntity> void prepareInsertStatement(
            PreparedStatement insertStatement,
            TE entity
    ) throws SQLException {
        if (insertStatement == null) {
            throw new IllegalArgumentException("PreparedStatement cannot be null");
        }
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }

        @SuppressWarnings("unchecked")
        Class<TE> clazz = (Class<TE>) entity.getClass();

        Map<TypedField<? extends TableEntity, ?>, String> fieldToColumnNames = TableUtils.mapFieldToColumnNames(clazz);

        if (fieldToColumnNames.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot prepare INSERT statement for " + clazz.getName() + ": no @TableColumn fields found"
            );
        }

        int parameterIndex = 1;

        for (Map.Entry<TypedField<? extends TableEntity, ?>, String> entry : fieldToColumnNames.entrySet()) {
            TypedField<? extends TableEntity, ?> typedField = entry.getKey();
            Object sqlValue = resolveSqlValue(entity, typedField.reflect());

            insertStatement.setObject(parameterIndex++, sqlValue);
        }
    }

    private static <LC extends TableEntity, FC extends TableEntity> Object resolveSqlValue(LC entity, Field field) {
        Object value;
        try {
            value = field.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        // If the field has another Table entity as DataType
        if (value instanceof TableEntity) {

            @SuppressWarnings("unchecked")
            Class<LC> localClass = (Class<LC>) entity.getClass();

            @SuppressWarnings("unchecked")
            Class<FC> foreignClass = (Class<FC>) field.getType().asSubclass(TableEntity.class);

            TypedField<LC, FC> localField = TypedField.of(localClass, field, foreignClass);
            Object foreignValue = TableUtils.getForeignColumnValue(entity, localField);

            if (foreignValue instanceof TableEntity) {
                throw new IllegalArgumentException("Foreign value can not be of type TableEntity");
            }

            return foreignValue;
        }
        return value;
    }
}