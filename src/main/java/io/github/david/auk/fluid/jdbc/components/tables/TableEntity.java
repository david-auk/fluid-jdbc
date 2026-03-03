package io.github.david.auk.fluid.jdbc.components.tables;


import io.github.david.auk.fluid.jdbc.annotations.ValidatedBody;
import io.github.david.auk.fluid.jdbc.annotations.table.*;
import io.github.david.auk.fluid.jdbc.annotations.table.constructor.TableConstructor;
import io.github.david.auk.fluid.jdbc.annotations.table.field.ForeignKey;
import io.github.david.auk.fluid.jdbc.annotations.table.field.PrimaryKey;
import io.github.david.auk.fluid.jdbc.annotations.table.field.TableColumn;
import io.github.david.auk.fluid.jdbc.components.daos.Dao;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import static io.github.david.auk.fluid.jdbc.components.tables.TableUtilsOld.getPrimaryKeyMember;

/**
 * Marker interface for classes that can be used with {@link Dao} and {@link ValidatedBody}.
 * <p>
 * Classes should have a constructor annotated with {@link TableConstructor},
 * and use {@link TableName}, {@link PrimaryKey}, {@link TableColumn}, etc.
 */
public interface TableEntity {
    /**
     * Validates that a class is a well-formed TableEntity.
     * @param clazz the entity class to validate
     */
    static <T extends TableEntity> void validateEntity(Class<T> clazz) {

        // Check for @TableName
        if (!clazz.isAnnotationPresent(TableName.class)) {
            throw new IllegalStateException("Entity class " + clazz.getName() + " is missing @TableName");
        }

        // Find the primary key: field-level preferred (ignoring duplicated record accessors)
        AccessibleObject pkMember = getPrimaryKeyMember(clazz);

        // Ensure a constructor is annotated @TableConstructor
        boolean hasTc = Arrays.stream(clazz.getDeclaredConstructors())
                .anyMatch(c -> c.isAnnotationPresent(TableConstructor.class));
        if (!hasTc) {
            throw new IllegalStateException("Entity class " + clazz.getName() +
                    " must have a constructor annotated @TableConstructor");
        }

        // If PK is a FIELD, ensure that field has @TableColumn.
        // Method-based @PrimaryKey is supported and does not need @TableColumn on the method.
        if (pkMember instanceof Field pkField && !pkField.isAnnotationPresent(TableColumn.class)) {
            throw new IllegalStateException("Primary key field " + pkField.getName() +
                    " in " + clazz.getName() + " must be annotated @TableColumn");
        }

        // If PK is method-based, ensure it returns a non-primitive type
        if (pkMember instanceof Method pkMethod) {
            if (pkMethod.getReturnType().isPrimitive()) {
                throw new IllegalStateException("Primary key method " + pkMethod.getName() +
                        " in " + clazz.getName() + " must not return a primitive type");
            }
        }

        // Validate @ForeignKey usage: field type must implement TableEntity
        for (Field f : clazz.getDeclaredFields()) {
            if (f.isAnnotationPresent(ForeignKey.class)) {
                if (!TableEntity.class.isAssignableFrom(f.getType())) {
                    throw new IllegalStateException("Field " + f.getName() +
                            " in " + clazz.getName() +
                            " annotated @ForeignKey must have a type implementing TableEntity");
                }
            }

            // Ensure all @TableColumn fields are non-primitive types
            if (f.isAnnotationPresent(TableColumn.class)) {
                if (f.getType().isPrimitive()) {
                    throw new IllegalStateException("Field " + f.getName() +
                            " in " + clazz.getName() +
                            " annotated @TableColumn must not be a primitive type");
                }
            }
        }
    }
}