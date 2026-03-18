package io.github.david.auk.fluid.jdbc.components.daos.querying.operator;

public enum NoValueOperator implements Operator {

    IS_NULL("IS NULL"),
    IS_NOT_NULL("IS NOT NULL");

    private final String primary;

    NoValueOperator(String primary) {
        this.primary = primary;
    }

    @Override
    public String primary() {
        return primary;
    }
}