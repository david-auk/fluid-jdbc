package io.github.david.auk.fluid.jdbc.components.results;

import io.github.david.auk.fluid.jdbc.annotations.table.field.TableColumn;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A lightweight "partial" representation of a {@link TableEntity}.
 * <p>
 * All mapped attributes are optional and backed by a map. This is useful when
 * you only select a subset of columns (e.g., in projections) and don't want to
 * construct a full entity.
 * <p>
 * Keys are the resolved column names:
 * - If {@link TableColumn#name()} is set, that value is used.
 * - Otherwise the Java field / record component name is used.
 * <p>
 * Values are considered "present" when non-null.
 */
public class PartialResult<TE extends TableEntity> {

    private final Class<TE> entityClass;

    /**
     * Backing storage for values. All known keys are preloaded with null.
     */
    private final Map<String, Object> values;

    public PartialResult(Class<TE> entityClass) {
        this.entityClass = Objects.requireNonNull(entityClass, "entityClass");
        this.values = new LinkedHashMap<>();
        preloadAllKeys(entityClass, this.values);
    }

    /**
     * @return the entity class this partial result is based on.
     */
    public Class<TE> getEntityClass() {
        return entityClass;
    }

    /**
     * Set a value for a column key.
     *
     * @param key   resolved column key (see class javadoc)
     * @param value value to set (null means "not present")
     */
    public PartialResult<TE> set(String key, Object value) {
        Objects.requireNonNull(key, "key");
        ensureKnownKey(key);
        values.put(key, value);
        return this;
    }

    /**
     * Convenience setter for a {@link Field}. The key is resolved the same way as preload.
     */
    public PartialResult<TE> set(Field field, Object value) {
        Objects.requireNonNull(field, "field");
        return set(resolveColumnKey(field), value);
    }

    /**
     * @return true if the key exists AND the value is present (non-null)
     */
    public boolean has(String key) {
        Objects.requireNonNull(key, "key");
        ensureKnownKey(key);
        return values.get(key) != null;
    }

    /**
     * @return optional value for the key (empty if not present / null)
     */
    public Optional<Object> get(String key) {
        Objects.requireNonNull(key, "key");
        ensureKnownKey(key);
        return Optional.ofNullable(values.get(key));
    }

    /**
     * Typed getter.
     *
     * @throws ClassCastException if a non-null value is not assignable to {@code type}
     */
    public <T> Optional<T> get(String key, Class<T> type) {
        Objects.requireNonNull(type, "type");
        return get(key).map(type::cast);
    }

    /**
     * @return an unmodifiable view of all keys and their (possibly null) values.
     */
    public Map<String, Object> asMap() {
        return Collections.unmodifiableMap(values);
    }

    /**
     * @return all known keys, in deterministic (insertion) order.
     */
    public Iterable<String> keys() {
        return asMap().keySet();
    }

    private void ensureKnownKey(String key) {
        if (!values.containsKey(key)) {
            throw new IllegalArgumentException(
                    "Unknown column key '" + key + "' for entity " + entityClass.getName() +
                            ". Known keys: " + values.keySet()
            );
        }
    }

    private static <TE extends TableEntity> void preloadAllKeys(Class<TE> clazz, Map<String, Object> out) {
        // Records: use record components
        if (clazz.isRecord()) {
            for (RecordComponent rc : clazz.getRecordComponents()) {
                // Record components can carry annotations
                TableColumn tc = rc.getAnnotation(TableColumn.class);
                String key = resolveColumnKey(rc.getName(), tc);
                out.putIfAbsent(key, null);
            }
        }

        // Fields: walk inheritance chain to include superclasses (except Object)
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Field f : current.getDeclaredFields()) {
                // Skip synthetic / compiler-generated fields
                if (f.isSynthetic()) continue;

                TableColumn tc = f.getAnnotation(TableColumn.class);
                String key = resolveColumnKey(f.getName(), tc);
                out.putIfAbsent(key, null);
            }
            current = current.getSuperclass();
        }
    }

    private static String resolveColumnKey(Field field) {
        TableColumn tc = field.getAnnotation(TableColumn.class);
        return resolveColumnKey(field.getName(), tc);
    }

    private static String resolveColumnKey(String javaName, TableColumn tc) {
        if (tc == null) {
            return javaName;
        }
        String annotated = tc.name();
        return (annotated == null || annotated.isBlank()) ? javaName : annotated;
    }
}
