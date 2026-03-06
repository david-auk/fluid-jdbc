package io.github.david.auk.fluid.jdbc.dbtests.contracts.foreignkey;

import io.github.david.auk.fluid.jdbc.annotations.table.TableName;
import io.github.david.auk.fluid.jdbc.annotations.table.constructor.TableConstructor;
import io.github.david.auk.fluid.jdbc.annotations.table.field.ForeignKey;
import io.github.david.auk.fluid.jdbc.annotations.table.field.PrimaryKey;
import io.github.david.auk.fluid.jdbc.annotations.table.field.TableColumn;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

import java.util.Objects;

@TableName("local_test_table")
public record EntityLocal(
        @PrimaryKey @TableColumn String name,
        @TableColumn(columnName = "foreign_entity_name") @ForeignKey EntityForeign foreignEntity
) implements TableEntity {

    @TableConstructor
    public EntityLocal(String name, EntityForeign foreignEntity) {
        this.name = Objects.requireNonNull(name, "columnName");
        this.foreignEntity = Objects.requireNonNull(foreignEntity, "foreignEntity");
    }
}