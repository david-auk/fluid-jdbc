package io.github.david.auk.fluid.jdbc.components.daos.querying.filters;

import io.github.david.auk.fluid.jdbc.components.daos.querying.relations.EntityRelation;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.components.tables.utils.TableUtils;
import io.github.david.auk.fluid.jdbc.internal.tables.meta.TypedField;

import java.lang.reflect.Field;

public final class FilterFieldFactory {

    private FilterFieldFactory() {}

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static FilterTypedField<? extends TableEntity, ?> buildForeignFilterTypedField(
            Class<? extends TableEntity> entityToBeQueried,
            TypedField<? extends TableEntity, Object> foreignTypedFieldToBeQueried
    ) {
        Class<? extends TableEntity> foreignClass = foreignTypedFieldToBeQueried.owner();
        EntityRelation<?, ?> entityRelation = TableUtils.resolveEntityRelation(
                (Class) entityToBeQueried,
                (Class) foreignClass
        );

        return new ForeignFilterTypedField(entityRelation, foreignTypedFieldToBeQueried);
    }

    static FilterTypedField<? extends TableEntity, ?> buildFilterTypedField(Class<? extends TableEntity> entityToBeQueried, TypedField<? extends TableEntity, Object> typedFieldToBeFiltered) {

        boolean fieldIsOfEntityToBeQueried = entityToBeQueried.isAssignableFrom(typedFieldToBeFiltered.owner());

        // Is local field
        if (fieldIsOfEntityToBeQueried) {
            return new LocalFilterTypedField<>(typedFieldToBeFiltered);
        }

        // Is foreign field
        return buildForeignFilterTypedField(entityToBeQueried, typedFieldToBeFiltered);
    }

    public static FilterTypedField<? extends TableEntity, ?> buildFilterTypedField(Class<? extends TableEntity> entityToBeQueried, Field fieldToBeFiltered) {
        TypedField<? extends TableEntity, Object> typedField = getTypedField(fieldToBeFiltered);

        return buildFilterTypedField(entityToBeQueried, typedField);
    }

    private static TypedField<? extends TableEntity, Object> getTypedField(Field field) {
        final Class<?> rawType = field.getDeclaringClass();

        final Class<? extends TableEntity> tableEntityClass;
        try {
            // avoids an unchecked cast; throws ClassCastException if the declaring class is not a TableEntity
            tableEntityClass = rawType.asSubclass(TableEntity.class);
        } catch (ClassCastException e) {
            String fieldId = field.getDeclaringClass().getName() + "#" + field.getName();
            throw new IllegalArgumentException(
                    "Field " + fieldId + " has type " + rawType.getName() + ", which does not implement " + TableEntity.class.getSimpleName(),
                    e
            );
        }

        return TypedField.of(tableEntityClass, field, Object.class);
    }
}
