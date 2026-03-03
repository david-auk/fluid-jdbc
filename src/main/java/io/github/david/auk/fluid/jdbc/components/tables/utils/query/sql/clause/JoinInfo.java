package io.github.david.auk.fluid.jdbc.components.tables.utils.query.sql.clause;

import java.lang.reflect.Field;
import java.util.Map;

public record JoinInfo(
        String refTableName,
        String baseFkColumn,
        String refPkColumn,
        Map<Field, String> refFieldToColumn
) {}