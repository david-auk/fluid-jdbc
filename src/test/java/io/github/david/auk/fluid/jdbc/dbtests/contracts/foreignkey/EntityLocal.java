package io.github.david.auk.fluid.jdbc.dbtests.contracts.foreignkey;

import io.github.david.auk.fluid.jdbc.annotations.table.TableName;
import io.github.david.auk.fluid.jdbc.annotations.table.constructor.TableConstructor;
import io.github.david.auk.fluid.jdbc.annotations.table.field.ForeignKey;
import io.github.david.auk.fluid.jdbc.annotations.table.field.PrimaryKey;
import io.github.david.auk.fluid.jdbc.annotations.table.field.TableColumn;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

import java.util.Objects;
import java.util.UUID;

@TableName("local_test_table")
public record EntityLocal(
        @PrimaryKey @TableColumn String id,
        @TableColumn(columnName = "foreign_entity_id") @ForeignKey EntityForeign foreignEntity,
        @TableColumn(columnName = "foreign_second_entity_id") @ForeignKey EntityForeignSecond foreignEntitySecond,
        @TableColumn Integer value
) implements TableEntity {

    @TableConstructor
    public EntityLocal(String id, EntityForeign foreignEntity, EntityForeignSecond foreignEntitySecond, Integer value) {
        this.id = Objects.requireNonNullElse(id, UUID.randomUUID().toString());
        this.foreignEntity = Objects.requireNonNull(foreignEntity, "foreignEntity");
        this.foreignEntitySecond = Objects.requireNonNull(foreignEntitySecond, "foreignEntitySecond");
        this.value = value;
    }
}