package io.github.david.auk.fluid.jdbc.internal.tables.meta;

import java.lang.annotation.Annotation;
import java.util.Objects;

/**
 * A small runtime-checked "type token" that guarantees a {@link Class} carries a specific annotation.
 *
 * <p>Important: Java generics cannot enforce annotations at compile time. This wrapper enforces the
 * constraint at construction time (fail-fast) and then allows you to pass the pair around safely.
 */
public final class ClassWithAnnotation<T, A extends Annotation> {

    private final Class<T> type;
    private final Class<A> requiredAnnotation;

    private ClassWithAnnotation(Class<T> type, Class<A> requiredAnnotation) {
        this.type = Objects.requireNonNull(type, "type");
        this.requiredAnnotation = Objects.requireNonNull(requiredAnnotation, "requiredAnnotation");

        if (!type.isAnnotationPresent(requiredAnnotation)) {
            throw new IllegalArgumentException(
                    "Class '" + type.getName() + "' must be annotated with @" + requiredAnnotation.getSimpleName());
        }
    }

    /**
     * Create a wrapper for {@code type} that must have {@code requiredAnnotation}.
     */
    public static <T, A extends Annotation> ClassWithAnnotation<T, A> of(Class<T> type, Class<A> requiredAnnotation) {
        return new ClassWithAnnotation<>(type, requiredAnnotation);
    }

    /**
     * @return the wrapped class.
     */
    public Class<T> type() {
        return type;
    }

    /**
     * @return the required annotation type.
     */
    public Class<A> requiredAnnotation() {
        return requiredAnnotation;
    }

    @Override
    public String toString() {
        return "ClassWithAnnotation[type=" + type.getName() + ", required=@" + requiredAnnotation.getSimpleName() + "]";
    }
}
