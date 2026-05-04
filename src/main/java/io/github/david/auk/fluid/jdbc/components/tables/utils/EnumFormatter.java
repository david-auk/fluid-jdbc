package io.github.david.auk.fluid.jdbc.components.tables.utils;

import io.github.david.auk.fluid.jdbc.annotations.enums.EnumFormat;

import java.util.Locale;
import java.util.Objects;

public final class EnumFormatter {

    private EnumFormatter() {
    }

    public static String format(Enum<?> value) {
        if (value == null) return null;

        EnumFormat enumFormat = value.getDeclaringClass().getAnnotation(EnumFormat.class);
        EnumFormat.Strategy localStrategy = enumFormat != null ? enumFormat.local() : EnumFormat.Strategy.NAME;
        EnumFormat.Strategy dbStrategy = enumFormat != null ? enumFormat.db() : EnumFormat.Strategy.NAME;

        String localValue = apply(value.name(), localStrategy);
        return convert(localValue, localStrategy, dbStrategy);
    }

    public static <E extends Enum<E>> E parse(Class<E> enumClass, String dbValue) {
        Objects.requireNonNull(enumClass, "enumClass");
        if (dbValue == null) return null;

        EnumFormat enumFormat = enumClass.getAnnotation(EnumFormat.class);
        EnumFormat.Strategy localStrategy = enumFormat != null ? enumFormat.local() : EnumFormat.Strategy.NAME;
        EnumFormat.Strategy dbStrategy = enumFormat != null ? enumFormat.db() : EnumFormat.Strategy.NAME;

        String normalizedDbValue = normalize(dbValue, dbStrategy);

        for (E constant : enumClass.getEnumConstants()) {
            String localValue = apply(constant.name(), localStrategy);
            String expectedDbValue = convert(localValue, localStrategy, dbStrategy);

            if (normalize(expectedDbValue, dbStrategy).equals(normalizedDbValue)) {
                return constant;
            }
        }

        throw new IllegalArgumentException(
                "Unknown enum value '" + dbValue + "' for enum " + enumClass.getSimpleName()
        );
    }

    private static String convert(
            String value,
            EnumFormat.Strategy from,
            EnumFormat.Strategy to
    ) {
        String canonicalName = toCanonicalName(value, from);
        return apply(canonicalName, to);
    }

    private static String toCanonicalName(String value, EnumFormat.Strategy strategy) {
        return switch (strategy) {
            case NAME, UPPERCASE, UPPER_SNAKE_CASE -> value.toUpperCase(Locale.ROOT);
            case LOWERCASE -> value.toUpperCase(Locale.ROOT);
            case lower_snake_case -> value.toUpperCase(Locale.ROOT);
        };
    }

    private static String apply(String value, EnumFormat.Strategy strategy) {
        return switch (strategy) {
            case NAME -> value;
            case LOWERCASE -> value.toLowerCase(Locale.ROOT);
            case UPPERCASE -> value.toUpperCase(Locale.ROOT);
            case lower_snake_case -> toSnake(value).toLowerCase(Locale.ROOT);
            case UPPER_SNAKE_CASE -> toSnake(value).toUpperCase(Locale.ROOT);
        };
    }

    private static String normalize(String value, EnumFormat.Strategy strategy) {
        return switch (strategy) {
            case NAME, UPPERCASE, LOWERCASE, UPPER_SNAKE_CASE, lower_snake_case -> value.toUpperCase(Locale.ROOT);
        };
    }

    private static String toSnake(String input) {
        return input
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .replaceAll("[-\\s]+", "_");
    }
}