package io.github.david.auk.fluid.jdbc.components.daos.querying.filters;

import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.internal.tables.meta.TypedField;

public interface FilterTypedField<LC extends TableEntity, V> {

    /**
     * The actual field on the entity being queried (LC).
     * This is what FilterCriterion should use.
     */
    TypedField<LC, V> typedField();
}