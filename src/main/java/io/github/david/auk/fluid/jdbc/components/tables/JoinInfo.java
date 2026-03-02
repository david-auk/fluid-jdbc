package io.github.david.auk.fluid.jdbc.components.tables;

import java.lang.reflect.Field;
import java.util.Map;

record JoinInfo(String refTableName, String baseFkColumn, String refPkColumn, Map<Field, String> refFieldToColumn) {
}