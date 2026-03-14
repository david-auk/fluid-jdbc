package io.github.david.auk.fluid.jdbc.components.daos.querying.filters;

import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.annotations.table.field.UniqueColumn;
import io.github.david.auk.fluid.jdbc.components.tables.utils.TableUtils;
import io.github.david.auk.fluid.jdbc.internal.tables.meta.TypedField;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * A wrapper around {@link TypedField} that guarantees the underlying reflective {@link Field}
 * is annotated with {@link UniqueColumn}.
 *
 * <p>This is intentionally validated at construction time so APIs can accept a
 * {@code ForeignFilterTypedField} parameter and rely on the invariant.</p>
 */
public final class ForeignFilterTypedField<LC extends TableEntity, FC extends TableEntity> implements FilterTypedField<FC, Object> {

    private final TypedField<LC, FC> localField;
    private final TypedField<FC, Object> foreignReferenceField;
    private final Class<LC> localClass;
    private final Class<FC> foreignClass;
    private final String foreignKeyColumn;
    private final String referencedColumn;

    ForeignFilterTypedField(TypedField<LC, FC> localField, TypedField<FC, Object> foreignReferenceField) {
        this.localField = Objects.requireNonNull(localField, "typedField");
        this.foreignReferenceField = Objects.requireNonNull(foreignReferenceField, "foreignReferenceField");

        this.localClass = localField.owner();
        this.foreignClass = foreignReferenceField.owner();

        this.foreignKeyColumn = TableUtils.getColumnName(localField);
        this.referencedColumn = TableUtils.getColumnNameOfForeignTableEntity(localField);

        // TODO Require @ForeignKey annotation on local column
        // TODO Require @UniqueColumn OR @PrimaryKey annotation on foreign column

        // TODO Read values in @ForeignKey annotation (ForeignKey(columnName = "foreignColumnName")) else overwrite them with @PrimaryKey
    }

    @Override
    public TypedField<FC, Object> typedField() {
        return foreignReferenceField;
    }

    public Class<LC> getLocalClass() {
        return localClass;
    }

    public Class<FC> getForeignClass() {
        return foreignClass;
    }

    public String getForeignKeyColumn() {
        return foreignKeyColumn;
    }

    public String getReferencedColumn() {
        return referencedColumn;
    }
}
