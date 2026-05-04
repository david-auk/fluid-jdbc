package io.github.david.auk.fluid.jdbc.components.tables.utils.query.sql;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class SupportedByDatabase {

    private static final Map<DatabaseType, Map<Class<?>, Class<?>>> TYPE_MAPPINGS = new ConcurrentHashMap<>();

    static {
        register(DatabaseType.POSTGRESQL, Instant.class, Timestamp.class);
        register(DatabaseType.POSTGRESQL, LocalDateTime.class, Timestamp.class);

        register(DatabaseType.MYSQL, Instant.class, Timestamp.class);
        register(DatabaseType.MYSQL, LocalDateTime.class, Timestamp.class);
    }

    private SupportedByDatabase() {
    }

    public static void register(
            DatabaseType databaseType,
            Class<?> unsupportedType,
            Class<?> supportedType
    ) {
        Objects.requireNonNull(databaseType, "databaseType");
        Objects.requireNonNull(unsupportedType, "unsupportedType");
        Objects.requireNonNull(supportedType, "supportedType");

        TYPE_MAPPINGS
                .computeIfAbsent(databaseType, ignored -> new ConcurrentHashMap<>())
                .put(unsupportedType, supportedType);
    }

    public static boolean isSupported(DatabaseType databaseType, Class<?> type) {
        Objects.requireNonNull(databaseType, "databaseType");
        Objects.requireNonNull(type, "type");

        return getSupportedType(databaseType, type).equals(type);
    }

    public static boolean hasMapping(DatabaseType databaseType, Class<?> type) {
        Objects.requireNonNull(databaseType, "databaseType");
        Objects.requireNonNull(type, "type");

        return TYPE_MAPPINGS
                .getOrDefault(databaseType, Map.of())
                .containsKey(type);
    }

    public static Class<?> getSupportedType(DatabaseType databaseType, Class<?> type) {
        Objects.requireNonNull(databaseType, "databaseType");
        Objects.requireNonNull(type, "type");

        return TYPE_MAPPINGS
                .getOrDefault(databaseType, Map.of())
                .getOrDefault(type, type);
    }
}
