package io.github.david.auk.fluid.jdbc.components.tables.utils.query.sql.clause;

public class OffsetClause {
    private OffsetClause() {}

    public static String build(Integer offsetAmount) {
        if (offsetAmount == null) {
            return "";
        }

        if (offsetAmount < 0) {
            throw new IllegalArgumentException("offsetAmount must be 0 or above");
        }

        return "OFFSET " + offsetAmount;
    }
}

