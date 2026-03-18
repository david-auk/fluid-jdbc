package io.github.david.auk.fluid.jdbc.components.daos.querying.operator;

public enum RangeOperator implements ValueOperator {

    BETWEEN("BETWEEN"),
    NOT_BETWEEN("NOT BETWEEN");

    private final String primary;

    RangeOperator(String primary) {
        this.primary = primary;
    }

    @Override
    public String primary() {
        return primary;
    }
}
