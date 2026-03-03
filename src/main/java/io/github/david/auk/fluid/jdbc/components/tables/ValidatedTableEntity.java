package io.github.david.auk.fluid.jdbc.components.tables;

public abstract class ValidatedTableEntity implements TableEntity {
    public ValidatedTableEntity() {
        TableEntity.validateEntity(this.getClass());
    }
}
