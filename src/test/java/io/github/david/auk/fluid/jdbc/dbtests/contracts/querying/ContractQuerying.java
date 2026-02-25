
package io.github.david.auk.fluid.jdbc.dbtests.contracts.querying;

import io.github.david.auk.fluid.jdbc.components.daos.Dao;
import io.github.david.auk.fluid.jdbc.components.daos.querying.QueryBuilder;
import io.github.david.auk.fluid.jdbc.dbtests.contracts.ContractInterface;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Base contract for query-related test suites.
 *
 * <p>This contract intentionally only provides:</p>
 * <ul>
 *   <li>a deterministic-ish dataset generator (covers LIKE/range/boolean/equality use-cases)</li>
 *   <li>simple helpers to populate/clear and to assert the table starts empty</li>
 * </ul>
 *
 * <p>Concrete query-behavior tests (e.g. WHERE, LIKE, ORDER BY, LIMIT) can be added in additional
 * contracts once your query API is implemented.</p>
 */
public interface ContractQuerying extends ContractInterface {

    /**
     * Creates a fresh entity with a random primary key.
     */
    default EntityQuerying newEntity(String name, String category, int valueInt, boolean enabled) {
        return new EntityQuerying(UUID.randomUUID().toString(), name, category, valueInt, enabled);
    }

    /**
     * Small dataset that is useful to test querying behaviour:
     * <ul>
     *   <li>name contains prefixes/suffixes for LIKE tests</li>
     *   <li>multiple categories for equality filtering</li>
     *   <li>valueInt spans a range for BETWEEN / ordering</li>
     *   <li>enabled true/false split for boolean filtering</li>
     * </ul>
     */
    default List<EntityQuerying> dataset() {
        List<EntityQuerying> rows = new ArrayList<>();

        // category A (mix enabled)
        rows.add(newEntity("alpha", "A", 1, true));
        rows.add(newEntity("alpha-2", "A", 2, false));
        rows.add(newEntity("alphabet", "A", 10, true));

        // category B (mix enabled)
        rows.add(newEntity("beta", "B", 3, true));
        rows.add(newEntity("beta-max", "B", 99, false));

        // category C (edge-ish values)
        rows.add(newEntity("gamma", "C", 0, true));
        rows.add(newEntity("gammadion", "C", 50, true));

        return rows;
    }

    /**
     * Convenience: insert the default dataset.
     */
    default void populate(Dao<EntityQuerying, String> dao) {
        Objects.requireNonNull(dao, "dao");
        for (EntityQuerying row : dataset()) {
            dao.add(row);
        }
    }

    /**
     * Convenience: insert a custom dataset.
     */
    default void populate(Dao<EntityQuerying, String> dao, List<EntityQuerying> rows) {
        Objects.requireNonNull(dao, "dao");
        Objects.requireNonNull(rows, "rows");
        for (EntityQuerying row : rows) {
            dao.add(row);
        }
    }

    /**
     * Reflection helper for record fields.
     */
    default java.lang.reflect.Field field(String name) {
        try {
            java.lang.reflect.Field f = EntityQuerying.class.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Missing field on EntityQuerying: " + name, e);
        }
    }

    /**
     * Minimal sanity check: a fresh DB should start empty.
     */
    @Test
    default void querying_startsEmpty() {
        try (Dao<EntityQuerying, String> dao = dao(EntityQuerying.class, String.class)) {
            assertTrue(dao.getAll().isEmpty(), "table should start empty");
        }
    }

    /**
     * Minimal sanity check: populate inserts exactly the expected number of rows.
     */
    @Test
    default void querying_populate_insertsDataset() {
        try (Dao<EntityQuerying, String> dao = dao(EntityQuerying.class, String.class)) {
            List<EntityQuerying> rows = dataset();
            populate(dao, rows);

            assertEquals(rows.size(), dao.getAll().size(), "populate should insert all dataset rows");
        }
    }


    @Test
    default void querying_where_equals_filtersOnCategory() {
        try (Dao<EntityQuerying, String> dao = dao(EntityQuerying.class, String.class)) {
            populate(dao);

            List<EntityQuerying> results = new QueryBuilder<>(dao)
                    .where(field("category"), "A")
                    .get();

            assertEquals(3, results.size(), "category A should return 3 rows from the default dataset");
            assertTrue(results.stream().allMatch(e -> "A".equals(e.category())), "all results must have category A");
        }
    }

    @Test
    default void querying_where_equals_filtersOnBoolean() {
        try (Dao<EntityQuerying, String> dao = dao(EntityQuerying.class, String.class)) {
            populate(dao);

            List<EntityQuerying> results = new QueryBuilder<>(dao)
                    .where(field("enabled"), true)
                    .get();

            assertEquals(5, results.size(), "enabled=true should return 5 rows from the default dataset");
            assertTrue(results.stream().allMatch(EntityQuerying::enabled), "all results must be enabled");
        }
    }

    @Test
    default void querying_whereLike_supportsPrefixSearch() {
        try (Dao<EntityQuerying, String> dao = dao(EntityQuerying.class, String.class)) {
            populate(dao);

            // Expect: alpha, alpha-2, alphabet
            List<EntityQuerying> results = new QueryBuilder<>(dao)
                    .whereLike(field("name"), "alpha%")
                    .get();

            assertEquals(3, results.size(), "alpha% should match alpha, alpha-2, alphabet");
            assertTrue(results.stream().allMatch(e -> e.name().startsWith("alpha")), "all results must start with 'alpha'");
        }
    }

    @Test
    default void querying_whereLike_supportsContainsSearch() {
        try (Dao<EntityQuerying, String> dao = dao(EntityQuerying.class, String.class)) {
            populate(dao);

            List<EntityQuerying> results = new QueryBuilder<>(dao)
                    .whereLike(field("name"), "%max%")
                    .get();

            assertEquals(1, results.size(), "%max% should match exactly one row in the default dataset");
            assertEquals("beta-max", results.getFirst().name(), "matched row should be beta-max");
        }
    }

    @Test
    default void querying_and_combinesMultipleFilters() {
        try (Dao<EntityQuerying, String> dao = dao(EntityQuerying.class, String.class)) {
            populate(dao);

            // In dataset: category A has alpha(true), alpha-2(false), alphabet(true)
            List<EntityQuerying> results = new QueryBuilder<>(dao)
                    .where(field("category"), "A")
                    .and(field("enabled"), true)
                    .get();

            assertEquals(2, results.size(), "category A AND enabled=true should return 2 rows");
            assertTrue(results.stream().allMatch(e -> "A".equals(e.category()) && e.enabled()), "all results must satisfy both filters");
        }
    }

    @Test
    default void querying_orderBy_asc_sortsByValueInt() {
        try (Dao<EntityQuerying, String> dao = dao(EntityQuerying.class, String.class)) {
            populate(dao);

            List<EntityQuerying> results = new QueryBuilder<>(dao)
                    .orderBy(field("valueInt"))
                    .asc()
                    .get();

            assertEquals(7, results.size(), "default dataset contains 7 rows");

            for (int i = 1; i < results.size(); i++) {
                int prev = results.get(i - 1).valueInt();
                int curr = results.get(i).valueInt();
                assertTrue(prev <= curr, "results must be sorted ascending by valueInt");
            }
        }
    }

    @Test
    default void querying_orderBy_desc_sortsByValueInt() {
        try (Dao<EntityQuerying, String> dao = dao(EntityQuerying.class, String.class)) {
            populate(dao);

            List<EntityQuerying> results = new QueryBuilder<>(dao)
                    .orderBy(field("valueInt"))
                    .desc()
                    .get();

            assertEquals(7, results.size(), "default dataset contains 7 rows");

            for (int i = 1; i < results.size(); i++) {
                int prev = results.get(i - 1).valueInt();
                int curr = results.get(i).valueInt();
                assertTrue(prev >= curr, "results must be sorted descending by valueInt");
            }
        }
    }

    @Test
    default void querying_getUnique_returnsNullWhenNoResults() {
        try (Dao<EntityQuerying, String> dao = dao(EntityQuerying.class, String.class)) {
            populate(dao);

            EntityQuerying result = new QueryBuilder<>(dao)
                    .where(field("category"), "__does_not_exist__")
                    .getUnique();

            assertNull(result, "getUnique should return null when there are no results");
        }
    }

    @Test
    default void querying_getUnique_returnsEntityWhenExactlyOneResult() {
        try (Dao<EntityQuerying, String> dao = dao(EntityQuerying.class, String.class)) {
            populate(dao);

            EntityQuerying result = new QueryBuilder<>(dao)
                    .where(field("name"), "beta")
                    .getUnique();

            assertNotNull(result, "getUnique should return the entity when exactly one result exists");
            assertEquals("beta", result.name(), "result should be the beta row");
        }
    }

    @Test
    default void querying_getUnique_throwsWhenMultipleResults() {
        try (Dao<EntityQuerying, String> dao = dao(EntityQuerying.class, String.class)) {
            populate(dao);

            IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                    new QueryBuilder<>(dao)
                            .where(field("category"), "A")
                            .getUnique()
            );

            assertTrue(ex.getMessage() != null && ex.getMessage().toLowerCase().contains("multiple"),
                    "exception message should mention multiple results");
        }
    }
}
