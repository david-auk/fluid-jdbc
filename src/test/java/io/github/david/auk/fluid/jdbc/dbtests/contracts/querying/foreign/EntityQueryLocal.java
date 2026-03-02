package io.github.david.auk.fluid.jdbc.dbtests.contracts.querying.foreign;

import io.github.david.auk.fluid.jdbc.annotations.table.TableName;
import io.github.david.auk.fluid.jdbc.annotations.table.constructor.TableConstructor;
import io.github.david.auk.fluid.jdbc.annotations.table.field.ForeignKey;
import io.github.david.auk.fluid.jdbc.annotations.table.field.PrimaryKey;
import io.github.david.auk.fluid.jdbc.annotations.table.field.TableColumn;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

import java.util.Objects;

@TableName("local_query_entity")
record EntityQueryLocal(
        @PrimaryKey
        @TableColumn(name = "id")
        String id,

        @TableColumn(name = "name")
        String name,

        @ForeignKey
        @TableColumn(name = "foreign_id")
        EntityQueryForeign foreign,

        @TableColumn(name = "value_int")
        Integer valueInt
) implements TableEntity {
    @TableConstructor
    public EntityQueryLocal(String id, String name, EntityQueryForeign foreign, Integer valueInt) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.foreign = Objects.requireNonNull(foreign, "foreign");
        this.valueInt = valueInt;
    }
}