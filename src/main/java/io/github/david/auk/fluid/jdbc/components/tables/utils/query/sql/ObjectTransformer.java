package io.github.david.auk.fluid.jdbc.components.tables.utils.query.sql;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class ObjectTransformer {

    private static final Map<TransformationKey<?, ?>, Function<?, ?>> TRANSFORMERS = new ConcurrentHashMap<>();

    static {
        register(Instant.class, Timestamp.class, Timestamp::from);
        register(Timestamp.class, Instant.class, Timestamp::toInstant);
        register(Object.class, String.class, Object::toString);
    }

    private ObjectTransformer() {
    }

    public static <FROM, TO> void register(
            Class<FROM> fromClass,
            Class<TO> toClass,
            Function<FROM, TO> transformer
    ) {
        Objects.requireNonNull(fromClass, "fromClass");
        Objects.requireNonNull(toClass, "toClass");
        Objects.requireNonNull(transformer, "transformer");

        TRANSFORMERS.put(new TransformationKey<>(fromClass, toClass), transformer);
    }

    public static <TO> TO transform(Object value, Class<TO> toClass) {
        Objects.requireNonNull(toClass, "toClass");

        if (value == null) {
            return null;
        }

        if (toClass.isInstance(value)) {
            return toClass.cast(value);
        }

        return transform(value.getClass(), toClass, value);
    }

    public static <FROM, TO> TO transform(
            Class<FROM> fromClass,
            Class<TO> toClass,
            Object value
    ) {
        Objects.requireNonNull(fromClass, "fromClass");
        Objects.requireNonNull(toClass, "toClass");

        if (value == null) {
            return null;
        }

        if (!fromClass.isInstance(value)) {
            throw new IllegalArgumentException(
                    "Value of type " + value.getClass().getName()
                            + " is not assignable to declared fromClass " + fromClass.getName()
            );
        }

        if (toClass.isInstance(value)) {
            return toClass.cast(value);
        }

        Function<FROM, TO> transformer = findTransformer(fromClass, toClass);

        if (transformer == null) {
            throw new IllegalArgumentException(
                    "No object transformer registered from "
                            + fromClass.getName()
                            + " to "
                            + toClass.getName()
            );
        }

        return transformer.apply(fromClass.cast(value));
    }

    public static boolean canTransform(Class<?> fromClass, Class<?> toClass) {
        Objects.requireNonNull(fromClass, "fromClass");
        Objects.requireNonNull(toClass, "toClass");

        return toClass.isAssignableFrom(fromClass)
                || findTransformer(fromClass, toClass) != null;
    }

    @SuppressWarnings("unchecked")
    private static <FROM, TO> Function<FROM, TO> findTransformer(
            Class<FROM> fromClass,
            Class<TO> toClass
    ) {
        Function<?, ?> exactTransformer = TRANSFORMERS.get(new TransformationKey<>(fromClass, toClass));

        if (exactTransformer != null) {
            return (Function<FROM, TO>) exactTransformer;
        }

        for (Map.Entry<TransformationKey<?, ?>, Function<?, ?>> entry : TRANSFORMERS.entrySet()) {
            TransformationKey<?, ?> key = entry.getKey();

            if (key.fromClass().isAssignableFrom(fromClass)
                    && toClass.isAssignableFrom(key.toClass())) {
                return (Function<FROM, TO>) entry.getValue();
            }
        }

        return null;
    }

    private record TransformationKey<FROM, TO>(
            Class<FROM> fromClass,
            Class<TO> toClass
    ) {
    }
}
