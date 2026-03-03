package io.github.david.auk.fluid.jdbc.components.tables.utils.query.sql.clause;

import java.lang.reflect.Field;

public final class OrderByClause {

    private OrderByClause() {}

    public static String build(String tableName, Field field, boolean ascending) {
        if (field == null) {
            return "";
        }

        String column = tableName + "." + field.getName();
        return "ORDER BY " + column + (ascending ? " ASC" : " DESC");
    }
}
