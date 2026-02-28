package io.github.david.auk.fluid.jdbc.components.daos.querying.operator;

/**
 * Common contract for SQL operators.
 */
public interface Operator {

    /**
     * Canonical SQL representation (used for rendering).
     */
    String primary();

    /**
     * Accepted aliases for parsing.
     */
    String[] aliases();

    default String toSql() {
        return primary();
    }

    /**
     * Generic parsing logic shared by all operator enums.
     */
    static <E extends Enum<E> & Operator> E fromString(Class<E> enumType, String operatorString) {
        if (operatorString == null) {
            throw new IllegalArgumentException("Unknown SQL operator: null");
        }

        String normalized = operatorString.trim().toUpperCase();

        for (E operator : enumType.getEnumConstants()) {
            if (operator.primary().equalsIgnoreCase(normalized)) {
                return operator;
            }
            for (String alias : operator.aliases()) {
                if (alias != null && alias.equalsIgnoreCase(normalized)) {
                    return operator;
                }
            }
        }

        throw new IllegalArgumentException("Unknown SQL operator: " + operatorString);
    }
}