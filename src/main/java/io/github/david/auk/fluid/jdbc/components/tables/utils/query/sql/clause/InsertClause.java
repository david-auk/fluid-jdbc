package io.github.david.auk.fluid.jdbc.components.tables.utils.query.sql.clause;

import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.components.tables.utils.TableUtils;
import io.github.david.auk.fluid.jdbc.internal.tables.meta.TypedField;

import java.sql.PreparedStatement;
import java.util.Map;
import java.util.StringJoiner;

public final class InsertClause {

    private InsertClause() {}

    public static <T extends TableEntity> String build(Class<T> clazz) {
        String tableName = TableUtils.getTableName(clazz);
        Map<TypedField<T, Object>, String> fieldToColumnNames = TableUtils.mapFieldToColumnNames(clazz);

        if (fieldToColumnNames.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot build INSERT clause for " + clazz.getName() + ": no @TableColumn fields found"
            );
        }

        StringJoiner columnJoiner = new StringJoiner(", ");
        StringJoiner valueJoiner = new StringJoiner(", ");

        for (String columnName : fieldToColumnNames.values()) {
            columnJoiner.add(columnName);
            valueJoiner.add("?");
        }

        return "INSERT INTO " + tableName + " (" + columnJoiner + ") VALUES (" + valueJoiner + ")";
    }

    public static <TE extends TableEntity> void prepareInsertStatement(PreparedStatement insertStatement, TE entity) {
        if (insertStatement == null) {
            throw new IllegalArgumentException("PreparedStatement cannot be null");
        }
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }

        @SuppressWarnings("unchecked")
        Class<TE> clazz = (Class<TE>) entity.getClass();
        Map<TypedField<TE, Object>, String> fieldToColumnNames = TableUtils.mapFieldToColumnNames(clazz);

        if (fieldToColumnNames.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot prepare INSERT statement for " + clazz.getName() + ": no @TableColumn fields found"
            );
        }

        int parameterIndex = 1;

        try {
            for (TypedField<TE, Object> typedField : fieldToColumnNames.keySet()) {
                Object value = typedField.reflect().get(entity);
                insertStatement.setObject(parameterIndex++, value);
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to prepare INSERT statement for " + clazz.getName(),
                    e
            );
        }
    }
}
