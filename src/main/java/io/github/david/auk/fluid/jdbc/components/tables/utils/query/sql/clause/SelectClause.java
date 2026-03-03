package io.github.david.auk.fluid.jdbc.components.tables.utils.query.sql.clause;

public final class SelectClause {

    private SelectClause() {
    }

    public static String build(String tableName) {
        return "SELECT " + tableName + ".*";
    }
}
