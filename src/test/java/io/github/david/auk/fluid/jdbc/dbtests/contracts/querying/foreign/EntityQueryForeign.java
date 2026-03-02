package io.github.david.auk.fluid.jdbc.dbtests.contracts.querying.foreign;

import io.github.david.auk.fluid.jdbc.annotations.table.TableName;
import io.github.david.auk.fluid.jdbc.annotations.table.constructor.TableConstructor;
import io.github.david.auk.fluid.jdbc.annotations.table.field.PrimaryKey;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.annotations.table.field.TableColumn;

import java.util.Objects;

@TableName("foreign_query_entity")
record EntityQueryForeign(
        @PrimaryKey
        @TableColumn(name = "id")
        String id,

        @TableColumn(name = "name")
        String name
) implements TableEntity {
    @TableConstructor
    public EntityQueryForeign(String id, String name) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
    }
}