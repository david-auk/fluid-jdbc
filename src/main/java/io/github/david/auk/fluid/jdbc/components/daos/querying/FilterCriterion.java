package io.github.david.auk.fluid.jdbc.components.daos.querying;

import java.lang.reflect.Field;

/**
 * A single filter condition: “field [=|LIKE] value”.
 */
public record FilterCriterion<T>(Field field, Operator operator, T value) {
}
