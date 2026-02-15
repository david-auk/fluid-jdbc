package io.github.david.auk.fluid.jdbc.contracts;

import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

public interface EntityUtil<TE extends TableEntity> {
    String createTableSql();
    String dropTableSql();
}
