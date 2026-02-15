package io.github.david.auk.fluid.jdbc.contracts.crud;

import io.github.david.auk.fluid.jdbc.annotations.table.TableName;
import io.github.david.auk.fluid.jdbc.annotations.table.constructor.TableConstructor;
import io.github.david.auk.fluid.jdbc.annotations.table.field.PrimaryKey;
import io.github.david.auk.fluid.jdbc.annotations.table.field.TableColumn;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

import java.sql.Timestamp;
import java.util.Objects;

@TableName("crud_test_table")
public record CrudEntity(
        @PrimaryKey @TableColumn String id,
        @TableColumn String name,
        @TableColumn(name = "value_int") Integer valueInt,
        @TableColumn(name = "updated_at") Timestamp updatedAt
) implements TableEntity {

    @TableConstructor
    public CrudEntity(String id, String name, Integer valueInt, Timestamp updatedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.valueInt = Objects.requireNonNull(valueInt, "valueInt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }
}