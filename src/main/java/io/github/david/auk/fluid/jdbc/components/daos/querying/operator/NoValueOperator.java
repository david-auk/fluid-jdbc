package io.github.david.auk.fluid.jdbc.components.daos.querying.operator;

public enum NoValueOperator implements Operator {

    IS_NULL("IS NULL"),
    IS_NOT_NULL("IS NOT NULL");

    private final String primary;
    private final String[] aliases;

    NoValueOperator(String primary, String... aliases) {
        this.primary = primary;
        this.aliases = aliases;
    }

    @Override
    public String primary() {
        return primary;
    }

    @Override
    public String[] aliases() {
        return aliases;
    }

    public static NoValueOperator fromString(String input) {
        return Operator.fromString(NoValueOperator.class, input);
    }
}