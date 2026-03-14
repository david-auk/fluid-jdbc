package io.github.david.auk.fluid.jdbc.internal.tables.meta;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * A strongly-typed reference to a field that belongs to {@code C}.
 *
 * <p>This is a replacement for using {@link Field} directly in APIs where you want the
 * type system to remember the owning class and the field value type.</p>
 *
 * <p>Invariants:</p>
 * <ul>
 *   <li>The reflective {@link Field} must be declared on {@code C} or one of its superclasses.</li>
 *   <li>The reflective {@link Field} type must be assignable to {@code V}.</li>
 * </ul>
 */
public final class TypedField<C, V> {

    private final Class<C> owner;
    private final String name;
    private final Class<V> valueType;

    // Lazily resolved, but you can also eagerly resolve in the ctor if you prefer.
    private volatile Field resolved;

    private TypedField(Class<C> owner, String name, Class<V> valueType) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.name = Objects.requireNonNull(name, "name");
        this.valueType = Objects.requireNonNull(valueType, "valueType");
    }

    /**
     * Creates a typed field reference by name.
     *
     * <p>Resolution/validation happens immediately to fail fast.</p>
     */
    public static <C, V> TypedField<C, V> of(Class<C> owner, String fieldName, Class<V> valueType) {
        TypedField<C, V> tf = new TypedField<>(owner, fieldName, valueType);
        tf.resolve(); // fail-fast validation
        return tf;
    }

    /**
     * Creates a typed field reference from an already-resolved reflective {@link Field}.
     *
     * <p>This is useful if you discovered fields by scanning/annotations but still want
     * a typed handle that cannot be accidentally associated with the wrong owner.</p>
     */
    public static <C, V> TypedField<C, V> of(Class<C> owner, Field field, Class<V> valueType) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(valueType, "valueType");

        // Owner constraint: field must belong to owner (declared on owner or superclass)
        if (!field.getDeclaringClass().isAssignableFrom(owner)) {
            throw new IllegalArgumentException(
                    "Field '" + field.getName() + "' is not a member of " + owner.getName() +
                            " (declared by " + field.getDeclaringClass().getName() + ")"
            );
        }

        // Value constraint
        if (!valueType.isAssignableFrom(field.getType())) {
            throw new IllegalArgumentException(
                    "Field type " + field.getType().getName() +
                            " is not assignable to " + valueType.getName()
            );
        }

        TypedField<C, V> tf = new TypedField<>(owner, field.getName(), valueType);
        tf.resolved = field;
        tf.resolved.setAccessible(true);
        return tf;
    }

    public static TypedField<?, ?> of(Field field) {
        Objects.requireNonNull(field, "field");
        Class<?> owner = field.getDeclaringClass();
        Class<?> valueType = field.getType();

        return of(owner, field, valueType);
    }

    public Class<C> owner() {
        return owner;
    }

    public String name() {
        return name;
    }

    public Class<V> valueType() {
        return valueType;
    }
    /**
     * Returns the underlying reflective field (resolved and validated).
     */
    public Field reflect() {
        return resolve();
    }

    public V get(C instance) {
        try {
            if (instance != null && !owner.isInstance(instance)) {
                throw new IllegalArgumentException(
                        "Instance is not of expected owner type: expected " + owner.getName() +
                                ", got " + instance.getClass().getName()
                );
            }
            return (V) resolve().get(instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    public V getValue(ResultSet resultSet) throws SQLException {
        Objects.requireNonNull(resultSet, "resultSet");
        return resultSet.getObject(name, valueType);
    }

    public V getValue(C instance) {
        return get(instance);
    }

    public void set(C instance, V value) {
        try {
            if (instance != null && !owner.isInstance(instance)) {
                throw new IllegalArgumentException(
                        "Instance is not of expected owner type: expected " + owner.getName() +
                                ", got " + instance.getClass().getName()
                );
            }
            resolve().set(instance, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    private Field resolve() {
        Field f = resolved;
        if (f != null) return f;

        synchronized (this) {
            f = resolved;
            if (f != null) return f;

            try {
                // If you want inherited fields too, you can walk superclasses here instead of getDeclaredField.
                f = owner.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException(
                        "Field '" + name + "' does not exist on " + owner.getName(), e
                );
            }

            // Value constraint (again, for the by-name constructor path)
            if (!valueType.isAssignableFrom(f.getType())) {
                throw new IllegalArgumentException(
                        "Field type " + f.getType().getName() +
                                " is not assignable to " + valueType.getName()
                );
            }

            // Owner constraint: since we resolved from owner.getDeclaredField, it’s declared on owner.
            // If you switch to superclass-walking lookup, keep the declaringClass check.
            f.setAccessible(true);

            resolved = f;
            return f;
        }
    }
}