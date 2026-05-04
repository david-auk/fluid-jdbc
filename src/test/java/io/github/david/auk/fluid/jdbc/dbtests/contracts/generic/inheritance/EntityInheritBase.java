package io.github.david.auk.fluid.jdbc.dbtests.contracts.generic.inheritance;

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

    @TableColumn
    private final Boolean active;

    @TableColumn
    private final Integer amount;

    @TableColumn
    private final String name;

    @TableConstructor
    public EntityInheritBase(String id, Boolean active, Integer amount, String name) {
        this.id = Objects.requireNonNull(id, "Non null");
        this.active = active;
        this.amount = amount;
        this.name = Objects.requireNonNull(name, "name");
    }

    public String getId() {
        return id;
    }

    public Boolean isActive() {
        return active;
    }

    public Integer getAmount() {
        return amount;
    }

    public String getName() {
        return name;
    }
}