package io.github.david.auk.fluid.jdbc.components.daos.querying.operator;

public enum MultiOperator implements ValueOperator {
    IN("IN"),
    NOT_IN("NOT IN");

    private final String primary;

    MultiOperator(String primary) {
        this.primary = primary;
    }

    @Override
    public String primary() {
        return primary;
    }
}
