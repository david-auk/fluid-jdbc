package io.github.david.auk.fluid.jdbc.components.daos.querying.filters;

import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.internal.tables.meta.TypedField;

import java.util.Objects;

public record LocalFilterTypedField<LC extends TableEntity, V>(
        TypedField<LC, V> typedField) implements FilterTypedField<LC, V> {

    public LocalFilterTypedField(TypedField<LC, V> typedField) {
        this.typedField = Objects.requireNonNull(typedField, "typedField");
    }
}