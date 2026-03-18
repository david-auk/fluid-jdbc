package io.github.david.auk.fluid.jdbc.components.daos.querying.operator;

public enum SingleValueOperator implements ValueOperator {

    EQUALS("="),
    NOT_EQUALS("!="),

    GREATER_THAN(">"),
    GREATER_THAN_OR_EQUAL(">="),
    LESS_THAN("<"),
    LESS_THAN_OR_EQUAL("<="),

    LIKE("LIKE"),
    NOT_LIKE("NOT LIKE");

    private final String primary;

    SingleValueOperator(String primary) {
        this.primary = primary;
    }

    @Override
    public String primary() {
        return primary;
    }
}