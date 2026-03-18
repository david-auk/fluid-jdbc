package io.github.david.auk.fluid.jdbc.components.daos.querying.operator;

/**
 * Common contract for SQL operators.
 */
public interface Operator {

    /**
     * Canonical SQL representation (used for rendering).
     */
    String primary();
}