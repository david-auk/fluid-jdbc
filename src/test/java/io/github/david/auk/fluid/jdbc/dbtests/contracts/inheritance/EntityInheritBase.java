package io.github.david.auk.fluid.jdbc.dbtests.contracts.inheritance;

import io.github.david.auk.fluid.jdbc.annotations.table.TableName;
import io.github.david.auk.fluid.jdbc.annotations.table.constructor.TableConstructor;
import io.github.david.auk.fluid.jdbc.annotations.table.field.PrimaryKey;
import io.github.david.auk.fluid.jdbc.annotations.table.field.TableColumn;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

import java.util.Objects;

@TableName("inherit_base")
public class EntityInheritBase implements TableEntity {

    @PrimaryKey
    @TableColumn
    private final String id;

    @TableConstructor
    public EntityInheritBase(String id) {
        this.id = Objects.requireNonNull(id, "Non null");
    }

    public String getId() {
        return id;
    }
}