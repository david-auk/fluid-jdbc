package io.github.david.auk.fluid.jdbc.components.tables.utils.query.sql;

import io.github.david.auk.fluid.jdbc.annotations.enums.EnumFormat;
import io.github.david.auk.fluid.jdbc.components.tables.utils.EnumFormatter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class ObjectSetter {

    public static void setObject(
            PreparedStatement preparedStatement,
            int parameterIndex,
            Object value
    ) throws SQLException {

        DatabaseType databaseType = resolveDatabaseType(preparedStatement);

        if (value instanceof Enum<?> enumValue) {

            EnumFormat enumFormat = enumValue.getDeclaringClass().getAnnotation(EnumFormat.class);

            String dbValue = enumValue.name();

            if (enumFormat != null && enumFormat.db() != EnumFormat.Strategy.NAME && enumFormat.local() != EnumFormat.Strategy.NAME) {
                dbValue = EnumFormatter.format(enumValue);
            }

            if (databaseType == DatabaseType.POSTGRESQL) {
                preparedStatement.setObject(parameterIndex, dbValue, Types.OTHER);
            } else {
                preparedStatement.setObject(parameterIndex, dbValue);
            }

            return;
        }

        if (value != null) {
            Class<?> supportedType = SupportedByDatabase.getSupportedType(databaseType, value.getClass());

            if (!supportedType.equals(value.getClass())
                    && ObjectTransformer.canTransform(value.getClass(), supportedType)) {
                value = ObjectTransformer.transform(value, supportedType);
            }
        }

        preparedStatement.setObject(parameterIndex, value);
    }

    private static DatabaseType resolveDatabaseType(PreparedStatement preparedStatement) throws SQLException {
        String databaseProductName = preparedStatement
                .getConnection()
                .getMetaData()
                .getDatabaseProductName();

        if (databaseProductName == null) {
            throw new IllegalStateException("Unknown database type");
        }

        String normalized = databaseProductName.toLowerCase();

        if (normalized.contains("postgresql")) {
            return DatabaseType.POSTGRESQL;
        }

        if (normalized.contains("mysql")) {
            return DatabaseType.MYSQL;
        }

        throw new IllegalStateException("Unsupported database type: " + databaseProductName);
    }
}
