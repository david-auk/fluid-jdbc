package io.github.david.auk.fluid.jdbc.components.tables.utils.query.sql.clause;

public final class FromClause {

    private FromClause() {}

    public static String build(String tableName) {
        return "FROM " + tableName;
    }
}
