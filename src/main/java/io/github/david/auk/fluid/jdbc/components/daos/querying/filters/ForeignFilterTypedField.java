package io.github.david.auk.fluid.jdbc.components.daos.querying.filters;

import io.github.david.auk.fluid.jdbc.components.daos.querying.relations.EntityRelation;
import io.github.david.auk.fluid.jdbc.components.daos.querying.relations.RelationKind;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.components.tables.utils.TableUtils;
import io.github.david.auk.fluid.jdbc.internal.tables.meta.TypedField;

import java.util.Objects;

/**
 * A wrapper around {@link TypedField} that guarantees the underlying reflective {@link Field}
 * is annotated with {@link UniqueColumn}.
 *
 * <p>This is intentionally validated at construction time so APIs can accept a
 * {@code ForeignFilterTypedField} parameter and rely on the invariant.</p>
 */
public final class ForeignFilterTypedField<LC extends TableEntity, FC extends TableEntity> implements FilterTypedField<FC, Object> {

    private final EntityRelation<LC, FC> entityRelation;
    private final TypedField<FC, Object> foreignReferenceField;

    ForeignFilterTypedField(EntityRelation<LC, FC> entityRelation, TypedField<FC, Object> foreignReferenceField) {
        this.entityRelation = entityRelation;
        this.foreignReferenceField = Objects.requireNonNull(foreignReferenceField, "foreignReferenceField");


        // TODO Require @ForeignKey annotation on local column
        // TODO Require @UniqueColumn OR @PrimaryKey annotation on foreign column

        // TODO Read values in @ForeignKey annotation (ForeignKey(columnName = "foreignColumnName")) else overwrite them with @PrimaryKey
    }

    @Override
    public TypedField<FC, Object> typedField() {
        return foreignReferenceField;
    }

    public EntityRelation<LC, FC> entityRelation() {
        return entityRelation;
    }
}
