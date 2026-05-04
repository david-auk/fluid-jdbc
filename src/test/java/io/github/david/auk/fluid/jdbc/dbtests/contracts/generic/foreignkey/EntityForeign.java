package io.github.david.auk.fluid.jdbc.dbtests.contracts.generic.foreignkey;

import io.github.david.auk.fluid.jdbc.annotations.table.TableName;
import io.github.david.auk.fluid.jdbc.annotations.table.constructor.TableConstructor;
import io.github.david.auk.fluid.jdbc.annotations.table.field.PrimaryKey;
import io.github.david.auk.fluid.jdbc.annotations.table.field.TableColumn;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

import java.util.Objects;
import java.util.UUID;

@TableName("foreign_test_table")
public record EntityForeign(
        @PrimaryKey @TableColumn String id,
        @TableColumn String name,
        @TableColumn Integer value
        ) implements TableEntity {

    @TableConstructor
    public EntityForeign(String id, String name, Integer value) {
        this.id = Objects.requireNonNullElse(id, UUID.randomUUID().toString());
        this.name = Objects.requireNonNull(name, "columnName");
        this.value = Objects.requireNonNull(value, "value");
    }
}