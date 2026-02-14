package io.github.david.auk.fluid.jdbc.factories;
import io.github.david.auk.fluid.jdbc.components.tables.Table;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Table Registry (Singleton)
 */
public class TableRegistry {

    private static final ConcurrentHashMap<Class<? extends TableEntity>, Table<?, ?>> cache =
            new ConcurrentHashMap<>();

    /**
     * Clears the cached Table metadata.
     *
     * Useful for tests that run against multiple databases/containers in the same JVM.
     * This registry should only cache metadata, but clearing it helps rule out stale state.
     */
    public static void clearCache() {
        cache.clear();
    }

    /**
     * @return current number of cached table entries (debug/testing).
     */
    public static int cacheSize() {
        return cache.size();
    }

    /**
     * When a table instance is created it is not expected that a table changes for a specific instance during runtime.
     * @param clazz The class-type of the AutoTableEntity
     * @return A cased or newly created table instance of clazz
     */
    public static <T extends TableEntity, K> Table<T, K> getTable(Class<T> clazz) {
        @SuppressWarnings("unchecked")
        Table<T, K> table = (Table<T, K>) cache.computeIfAbsent(
                clazz,
                c -> new Table<>((Class) c)
        );
        return table;
    }

    private TableRegistry() {
        // prevent instantiation
    }
}
