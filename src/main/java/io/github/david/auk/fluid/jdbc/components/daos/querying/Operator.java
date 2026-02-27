package io.github.david.auk.fluid.jdbc.components.daos.querying;

public enum Operator {

    EQUALS("="),
    NOT_EQUALS("<>", "!="),

    GREATER_THAN(">"),
    GREATER_THAN_OR_EQUAL(">="),
    LESS_THAN("<"),
    LESS_THAN_OR_EQUAL("<="),

    LIKE("LIKE"),
    NOT_LIKE("NOT LIKE"),

    IN("IN"),
    NOT_IN("NOT IN"),

    BETWEEN("BETWEEN"),
    NOT_BETWEEN("NOT BETWEEN"),

    IS_NULL("IS NULL"),
    IS_NOT_NULL("IS NOT NULL");

    private final String primary;
    private final String[] aliases;

    Operator(String primary, String... aliases) {
        this.primary = primary;
        this.aliases = aliases;
    }

    public static Operator fromString(String operatorString) {
        String normalized = operatorString.trim().toUpperCase();

        for (Operator operator : values()) {
            if (operator.primary.equals(normalized)) {
                return operator;
            }
            for (String alias : operator.aliases) {
                if (alias.equals(normalized)) {
                    return operator;
                }
            }
        }

        throw new IllegalArgumentException("Unknown SQL operator: " + operatorString);
    }

    @Override
    public String toString() {
        return primary;
    }
}
