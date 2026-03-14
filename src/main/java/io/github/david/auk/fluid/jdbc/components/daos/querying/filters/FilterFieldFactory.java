package io.github.david.auk.fluid.jdbc.components.daos.querying.filters;

import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.components.tables.utils.TableUtils;
import io.github.david.auk.fluid.jdbc.internal.tables.meta.TypedField;

import java.lang.reflect.Field;

public final class FilterFieldFactory {

    private FilterFieldFactory() {}

    private static <LC extends TableEntity, FC extends TableEntity> ForeignFilterTypedField<LC, FC> buildForeignTypedField(
            Class<LC> entityToBeQueried,
            TypedField<FC, Object> foreignTypedFieldToBeQueried
    ) {
        // Define the foreign class
        Class<FC> foreignClass = foreignTypedFieldToBeQueried.owner();

        // Get the field of the local class referencing the foreign class
        TypedField<LC, FC> localTypedFieldOfForeignEntity = TableUtils.getLocalFieldOfTypeForeignEntity(entityToBeQueried, foreignClass);

        // Return the new ForeignFilterTypedField
        return new ForeignFilterTypedField<>(localTypedFieldOfForeignEntity, foreignTypedFieldToBeQueried);
    }

    static FilterTypedField<? extends TableEntity, ?> buildFilterTypedField(Class<? extends TableEntity> entityToBeQueried, TypedField<? extends TableEntity, Object> typedFieldToBeFiltered) {

        boolean fieldIsOfEntityToBeQueried = entityToBeQueried.isAssignableFrom(typedFieldToBeFiltered.owner());

        // Is local field
        if (fieldIsOfEntityToBeQueried) {
            return new LocalFilterTypedField<>(typedFieldToBeFiltered);
        }

        // Is foreign field
        return buildForeignTypedField(entityToBeQueried, typedFieldToBeFiltered);
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
