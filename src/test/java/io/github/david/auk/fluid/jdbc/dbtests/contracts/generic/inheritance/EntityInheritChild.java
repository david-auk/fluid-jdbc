package io.github.david.auk.fluid.jdbc.dbtests.contracts.generic.inheritance;

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

    @TableColumn
    private final Boolean enabled;

    @TableColumn
    private final Integer score;

    @TableColumn
    private final String description;

    @TableConstructor
    public EntityInheritChild(EntityInheritBase base, Integer valueInt, Boolean enabled, Integer score, String description) {
        super(base.getId(), base.isActive(), base.getAmount(), base.getName());
        this.valueInt = Objects.requireNonNull(valueInt, "valueInt");
        this.enabled = enabled;
        this.score = score;
        this.description = Objects.requireNonNull(description, "description");
    }

    public Integer getValueInt() {
        return valueInt;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public Integer getScore() {
        return score;
    }

    public String getDescription() {
        return description;
    }
}