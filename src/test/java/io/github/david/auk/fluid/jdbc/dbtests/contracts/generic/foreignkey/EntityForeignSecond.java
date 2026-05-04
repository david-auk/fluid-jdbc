package io.github.david.auk.fluid.jdbc.dbtests.contracts.generic.foreignkey;

import io.github.david.auk.fluid.jdbc.annotations.table.TableName;
import io.github.david.auk.fluid.jdbc.annotations.table.constructor.TableConstructor;
import io.github.david.auk.fluid.jdbc.annotations.table.field.PrimaryKey;
import io.github.david.auk.fluid.jdbc.annotations.table.field.TableColumn;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

import java.util.Objects;
import java.util.UUID;

@TableName("foreign_second_test_table")
public record EntityForeignSecond(
        @PrimaryKey @TableColumn String id,
        @TableColumn(columnName = "is_active") Boolean isActive,
        @TableColumn Integer value
) implements TableEntity {

    @TableConstructor
    public EntityForeignSecond(String id, Boolean isActive, Integer value) {
        this.id = Objects.requireNonNullElse(id, UUID.randomUUID().toString());
        this.isActive = isActive;
        this.value = value;
    }
}
