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
        if (value instanceof Enum<?> enumValue) {

            EnumFormat enumFormat = enumValue.getDeclaringClass().getAnnotation(EnumFormat.class);

            String dbValue = enumValue.name();

            if (enumFormat != null && enumFormat.db() != EnumFormat.Strategy.NAME && enumFormat.local() != EnumFormat.Strategy.NAME) {
                dbValue = EnumFormatter.format(enumValue);
            }

            if (isPostgres(preparedStatement)) {
                preparedStatement.setObject(parameterIndex, dbValue, Types.OTHER);
            } else {
                preparedStatement.setObject(parameterIndex, dbValue);
            }

            return;
        }

        preparedStatement.setObject(parameterIndex, value);
    }

    private static boolean isPostgres(PreparedStatement preparedStatement) throws SQLException {
        String databaseProductName = preparedStatement
                .getConnection()
                .getMetaData()
                .getDatabaseProductName();

        return databaseProductName != null
                && databaseProductName.toLowerCase().contains("postgresql");
    }
}
