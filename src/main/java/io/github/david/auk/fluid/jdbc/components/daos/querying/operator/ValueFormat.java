package io.github.david.auk.fluid.jdbc.components.daos.querying.operator;

public enum ValueFormat {
    SINGLE_VALUE,     // =, >, <, LIKE, etc.
    RANGE,            // BETWEEN
    MULTI_VALUE,      // IN
}
