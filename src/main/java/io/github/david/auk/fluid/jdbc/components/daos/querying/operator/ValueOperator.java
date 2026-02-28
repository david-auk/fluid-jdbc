package io.github.david.auk.fluid.jdbc.components.daos.querying.operator;

public enum ValueOperator implements Operator {

    EQUALS("=", ValueFormat.SINGLE_VALUE),
    NOT_EQUALS("<>", ValueFormat.SINGLE_VALUE, "!="),

    GREATER_THAN(">", ValueFormat.SINGLE_VALUE),
    GREATER_THAN_OR_EQUAL(">=", ValueFormat.SINGLE_VALUE),
    LESS_THAN("<", ValueFormat.SINGLE_VALUE),
    LESS_THAN_OR_EQUAL("<=", ValueFormat.SINGLE_VALUE),

    LIKE("LIKE", ValueFormat.SINGLE_VALUE),
    NOT_LIKE("NOT LIKE", ValueFormat.SINGLE_VALUE),

    IN("IN", ValueFormat.MULTI_VALUE),
    NOT_IN("NOT IN", ValueFormat.MULTI_VALUE),

    BETWEEN("BETWEEN", ValueFormat.RANGE),
    NOT_BETWEEN("NOT BETWEEN", ValueFormat.RANGE),;

    private final String primary;
    private final ValueFormat valueFormat;
    private final String[] aliases;

    ValueOperator(String primary, ValueFormat valueFormat, String... aliases) {
        this.primary = primary;
        this.valueFormat = valueFormat;
        this.aliases = aliases;
    }

    @Override
    public String primary() {
        return primary;
    }

    public ValueFormat valueFormat() {
        return valueFormat;
    }

    @Override
    public String[] aliases() {
        return aliases;
    }

    public static ValueOperator fromString(String input) {
        return Operator.fromString(ValueOperator.class, input);
    }
}