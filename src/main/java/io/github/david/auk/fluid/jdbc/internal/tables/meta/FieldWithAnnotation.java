package io.github.david.auk.fluid.jdbc.internal.tables.meta;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Objects;
import io.github.david.auk.fluid.jdbc.internal.tables.meta.TypedField;

/**
 * A small runtime-checked "token" that guarantees a {@link Field} carries a specific annotation.
 *
 * <p>Java generics cannot enforce annotations at compile time. This wrapper enforces the constraint
 * at construction time (fail-fast) and then allows you to pass the validated field around safely.
 */
public final class FieldWithAnnotation<A extends Annotation> {

    private final Field field;
    private final Class<A> requiredAnnotation;

    private FieldWithAnnotation(Field field, Class<A> requiredAnnotation) {
        this.field = Objects.requireNonNull(field, "field");
        this.requiredAnnotation = Objects.requireNonNull(requiredAnnotation, "requiredAnnotation");

        // ensure we can read annotations even for private fields
        this.field.setAccessible(true);

        if (!field.isAnnotationPresent(requiredAnnotation)) {
            throw new IllegalArgumentException(
                    "Field '" + field.getDeclaringClass().getName() + "." + field.getName() +
                            "' must be annotated with @" + requiredAnnotation.getSimpleName());
        }
    }

    /**
     * Create a wrapper for {@code field} that must have {@code requiredAnnotation}.
     */
    public static <A extends Annotation> FieldWithAnnotation<A> of(Field field, Class<A> requiredAnnotation) {
        return new FieldWithAnnotation<>(field, requiredAnnotation);
    }

    /**
     * Create a wrapper from a {@link TypedField} by resolving its reflective field and validating the annotation.
     */
    public static <C, V, A extends Annotation> FieldWithAnnotation<A> of(TypedField<C, V> typedField,
                                                                         Class<A> requiredAnnotation) {
        Objects.requireNonNull(typedField, "typedField");
        return of(typedField.reflect(), requiredAnnotation);
    }

    /**
     * Create a wrapper from a provided reflective {@link Field} and validate that it matches the provided
     * {@link TypedField} (owner + name + valueType), then validate the required annotation.
     */
    public static <C, V, A extends Annotation> FieldWithAnnotation<A> of(TypedField<C, V> typedField,
                                                                         Field field,
                                                                         Class<A> requiredAnnotation) {
        Objects.requireNonNull(typedField, "typedField");
        Objects.requireNonNull(field, "field");

        // Ensure the field matches the TypedField contract.
        if (!field.getName().equals(typedField.name())) {
            throw new IllegalArgumentException(
                    "Field name mismatch: expected '" + typedField.name() + "', got '" + field.getName() + "'");
        }

        if (!field.getDeclaringClass().isAssignableFrom(typedField.owner())) {
            throw new IllegalArgumentException(
                    "Field '" + field.getName() + "' is not a member of " + typedField.owner().getName() +
                            " (declared by " + field.getDeclaringClass().getName() + ")"
            );
        }

        if (!typedField.valueType().isAssignableFrom(field.getType())) {
            throw new IllegalArgumentException(
                    "Field type " + field.getType().getName() +
                            " is not assignable to " + typedField.valueType().getName()
            );
        }

        return of(field, requiredAnnotation);
    }

    /**
     * Convenience: look up a declared field by name and validate it has the annotation.
     */
    public static <A extends Annotation> FieldWithAnnotation<A> of(Class<?> declaringClass,
                                                                    String fieldName,
                                                                    Class<A> requiredAnnotation) {
        try {
            Field f = declaringClass.getDeclaredField(fieldName);
            return of(f, requiredAnnotation);
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException(
                    "No such field '" + fieldName + "' on class '" + declaringClass.getName() + "'", e);
        }
    }

    /**
     * @return the wrapped field.
     */
    public Field field() {
        return field;
    }

    /**
     * @return the required annotation type.
     */
    public Class<A> requiredAnnotation() {
        return requiredAnnotation;
    }

    /**
     * @return the annotation instance present on the field.
     */
    public A annotation() {
        return field.getAnnotation(requiredAnnotation);
    }

    /**
     * @return Declaring class of the wrapped field.
     */
    public Class<?> declaringClass() {
        return field.getDeclaringClass();
    }

    /**
     * @return Field name.
     */
    public String name() {
        return field.getName();
    }

    @Override
    public String toString() {
        return "FieldWithAnnotation[field=" + field.getDeclaringClass().getName() + "." + field.getName()
                + ", required=@" + requiredAnnotation.getSimpleName() + "]";
    }
}
