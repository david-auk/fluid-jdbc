package io.github.david.auk.fluid.jdbc.components.tables.utils.query.sql.clause;

public class LimitClause {

    private LimitClause() {}

    public static String build(Integer limitAmount) {
        if (limitAmount == null) {
            return "";
        }

        if (limitAmount <= 0) {
                throw new IllegalArgumentException("limitAmount must be a number above 0");
        }

        return "LIMIT " + limitAmount;
    }
}
