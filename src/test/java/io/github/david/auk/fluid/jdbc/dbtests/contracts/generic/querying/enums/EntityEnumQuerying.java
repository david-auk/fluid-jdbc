package io.github.david.auk.fluid.jdbc.dbtests.contracts.generic.querying.enums;

import io.github.david.auk.fluid.jdbc.annotations.table.TableName;
import io.github.david.auk.fluid.jdbc.annotations.table.constructor.TableConstructor;
import io.github.david.auk.fluid.jdbc.annotations.table.field.Nullable;
import io.github.david.auk.fluid.jdbc.annotations.table.field.PrimaryKey;
import io.github.david.auk.fluid.jdbc.annotations.table.field.TableColumn;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

import java.util.Objects;

@TableName("query_enum_test_table")
public record EntityEnumQuerying(
        @PrimaryKey @TableColumn String id,
        @TableColumn String name,
        @TableColumn QueryingStatus status,
        @TableColumn(columnName = "nullable_status") @Nullable QueryingStatus nullableStatus
) implements TableEntity {

    @TableConstructor
    public EntityEnumQuerying(
            String id,
            String name,
            QueryingStatus status,
            QueryingStatus nullableStatus
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.status = Objects.requireNonNull(status, "status");
        this.nullableStatus = nullableStatus;
    }
}