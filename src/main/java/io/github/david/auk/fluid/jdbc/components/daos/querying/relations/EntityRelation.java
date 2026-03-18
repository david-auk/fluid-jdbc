package io.github.david.auk.fluid.jdbc.components.daos.querying.relations;

import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.internal.tables.meta.TypedField;

public record EntityRelation<
        LOCAL extends TableEntity,
        FOREIGN extends TableEntity
        >(
        Class<LOCAL> localClass,
        Class<FOREIGN> foreignClass,
        RelationKind kind,
        String localTableName,
        String localJoinColumn,
        String foreignTableName,
        String foreignJoinColumn,
        TypedField<LOCAL, FOREIGN> localField,
        TypedField<FOREIGN, Object> anchorField
) {}