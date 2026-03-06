package io.github.david.auk.fluid.jdbc.dbtests.contracts.inheritance;

import io.github.david.auk.fluid.jdbc.annotations.table.TableName;
import io.github.david.auk.fluid.jdbc.annotations.table.constructor.TableConstructor;
import io.github.david.auk.fluid.jdbc.annotations.table.constructor.TableInherits;
import io.github.david.auk.fluid.jdbc.annotations.table.field.TableColumn;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

import java.util.Objects;

@TableName("inherit_child")
@TableInherits(EntityInheritBase.class)
public class EntityInheritChild extends EntityInheritBase implements TableEntity {

    @TableColumn(columnName = "value_int")
    private final Integer valueInt;

    @TableConstructor
    public EntityInheritChild(EntityInheritBase base, Integer valueInt) {
        super(base.getId());
        this.valueInt = Objects.requireNonNull(valueInt, "valueInt");
    }

    public Integer getValueInt() {
        return valueInt;
    }
}