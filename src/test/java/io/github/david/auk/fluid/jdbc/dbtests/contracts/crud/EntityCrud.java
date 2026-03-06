package io.github.david.auk.fluid.jdbc.dbtests.contracts.crud;

import io.github.david.auk.fluid.jdbc.annotations.table.TableName;
import io.github.david.auk.fluid.jdbc.annotations.table.constructor.TableConstructor;
import io.github.david.auk.fluid.jdbc.annotations.table.field.PrimaryKey;
import io.github.david.auk.fluid.jdbc.annotations.table.field.TableColumn;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

import java.util.Objects;

@TableName("crud_test_table")
public record EntityCrud(
        @PrimaryKey @TableColumn String id,
        @TableColumn String name,
        @TableColumn(columnName = "value_int") Integer valueInt
) implements TableEntity {

    @TableConstructor
    public EntityCrud(String id, String name, Integer valueInt) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "columnName");
        this.valueInt = Objects.requireNonNull(valueInt, "valueInt");
    }
}