package io.github.david.auk.fluid.jdbc.components.tables.utils;

import io.github.david.auk.fluid.jdbc.annotations.table.TableName;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

final class TableInfoResolver {
    private TableInfoResolver() {
    }

    public static String getTableName(Class<? extends TableEntity> tableEntityClass) {
        TableName annotation = tableEntityClass.getAnnotation(TableName.class);

        if (annotation == null) {
            throw new IllegalArgumentException(
                    "TableEntity validation did not catch " +
                    "@TableName annotation is null");
        }

        return annotation.value();
    }
}
