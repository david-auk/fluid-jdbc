package io.github.david.auk.fluid.jdbc.dbtests.contracts.querying;

import io.github.david.auk.fluid.jdbc.components.daos.Dao;
import io.github.david.auk.fluid.jdbc.components.daos.querying.QueryBuilder;
import io.github.david.auk.fluid.jdbc.components.daos.querying.operator.NoValueOperator;
import io.github.david.auk.fluid.jdbc.components.daos.querying.operator.ValueOperator;
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
     * Single unified dataset intended for operator coverage.
     *
     * <p>This set is designed so you can write tests for:</p>
     * <ul>
     *   <li>equality/inequality on category/name</li>
     *   <li>numeric comparisons on valueInt (>, >=, <, <=)</li>
     *   <li>LIKE / NOT LIKE on name</li>
     *   <li>IN / NOT IN on category</li>
     *   <li>BETWEEN / NOT BETWEEN on valueInt</li>
     *   <li>IS NULL / IS NOT NULL on nullable columns (name/category)</li>
     * </ul>
     *
     * <p>Note: this dataset includes a few <code>null</code> values for <code>name</code> and
     * <code>category</code>. Your table schema must allow NULLs for these columns if you want
     * to test IS NULL / IS NOT NULL.</p>
     */
    default List<EntityQuerying> dataset() {
        List<EntityQuerying> rows = new ArrayList<>();

        // A: multiple rows (mix enabled) + LIKE targets
        rows.add(newEntity("alpha", "A", 1, true));
        rows.add(newEntity("alpha-2", "A", 2, false));
        rows.add(newEntity("alphabet", "A", 10, true));
        rows.add(newEntity("alpha-max", "A", 100, false));

        // B: values around common BETWEEN boundaries
        rows.add(newEntity("beta", "B", 3, true));
        rows.add(newEntity("beta-mid", "B", 20, true));
        rows.add(newEntity("beta-max", "B", 99, false));

        // C: edge-ish values including negatives/zero
        rows.add(newEntity("gamma", "C", 0, true));
        rows.add(newEntity("gammadion", "C", 50, true));
        rows.add(newEntity("gamma-low", "C", -10, false));

        // D/E: extra categories for IN/NOT IN testing
        rows.add(newEntity("delta", "D", 5, true));
        rows.add(newEntity("epsilon", "E", 101, true));

        // NULL coverage for IS NULL / IS NOT NULL (schema now allows name/category NULL)
        rows.add(newEntity(null, "N", 7, true));
        rows.add(newEntity("null-category", null, 8, false));

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
                    .where(field("category"), ValueOperator.EQUALS, "A")
                    .get();

            assertEquals(4, results.size(), "category A should return 4 rows from the unified dataset");
            assertTrue(results.stream().allMatch(e -> "A".equals(e.category())), "all results must have category A");
        }
    }

    @Test
    default void querying_where_equals_filtersOnBoolean() {
        try (Dao<EntityQuerying, String> dao = dao(EntityQuerying.class, String.class)) {
            populate(dao);

            List<EntityQuerying> results = new QueryBuilder<>(dao)
                    .where(field("enabled"), ValueOperator.EQUALS, true)
                    .get();

            assertEquals(9, results.size(), "enabled=true should return 9 rows from the unified dataset");
            assertTrue(results.stream().allMatch(EntityQuerying::enabled), "all results must be enabled");
        }
    }

    @Test
    default void querying_whereLike_supportsPrefixSearch() {
        try (Dao<EntityQuerying, String> dao = dao(EntityQuerying.class, String.class)) {
            populate(dao);

            // Expect: alpha, alpha-2, alphabet, alpha-max
            List<EntityQuerying> results = new QueryBuilder<>(dao)
                    .where(field("name"), ValueOperator.LIKE, "alpha%")
                    .get();

            assertEquals(4, results.size(), "alpha% should match alpha, alpha-2, alphabet, alpha-max");
            assertTrue(results.stream().allMatch(e -> e.name().startsWith("alpha")), "all results must start with 'alpha'");
        }
    }

    @Test
    default void querying_whereLike_supportsContainsSearch() {
        try (Dao<EntityQuerying, String> dao = dao(EntityQuerying.class, String.class)) {
            populate(dao);

            List<EntityQuerying> results = new QueryBuilder<>(dao)
                    .where(field("name"), ValueOperator.LIKE, "%max%")
                    .get();

            assertEquals(2, results.size(), "%max% should match exactly two rows in the unified dataset");
            assertTrue(results.stream().map(EntityQuerying::name).allMatch(n -> n != null && n.contains("max")), "all results must contain 'max'");
            assertTrue(results.stream().anyMatch(e -> "alpha-max".equals(e.name())), "results should include alpha-max");
            assertTrue(results.stream().anyMatch(e -> "beta-max".equals(e.name())), "results should include beta-max");
        }
    }

    @Test
    default void querying_and_combinesMultipleFilters() {
        try (Dao<EntityQuerying, String> dao = dao(EntityQuerying.class, String.class)) {
            populate(dao);

            // In dataset: category A has alpha(true), alpha-2(false), alphabet(true), alpha-max(false)
            List<EntityQuerying> results = new QueryBuilder<>(dao)
                    .where(field("category"), ValueOperator.EQUALS, "A")
                    .and(field("enabled"), ValueOperator.EQUALS, true)
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

            assertEquals(14, results.size(), "unified dataset contains 14 rows");

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

            assertEquals(14, results.size(), "unified dataset contains 14 rows");

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
                    .where(field("category"), ValueOperator.EQUALS, "__does_not_exist__")
                    .getUnique();

            assertNull(result, "getUnique should return null when there are no results");
        }
    }

    @Test
    default void querying_getUnique_returnsEntityWhenExactlyOneResult() {
        try (Dao<EntityQuerying, String> dao = dao(EntityQuerying.class, String.class)) {
            populate(dao);

            EntityQuerying result = new QueryBuilder<>(dao)
                    .where(field("name"), ValueOperator.EQUALS, "beta")
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
                            .where(field("category"), ValueOperator.EQUALS, "A")
                            .getUnique()
            );

            assertTrue(ex.getMessage() != null && ex.getMessage().toLowerCase().contains("multiple"),
                    "exception message should mention multiple results");
        }
    }

    @Test
    default void querying_where_notEquals_filtersOnValueInt() {
        try (Dao<EntityQuerying, String> dao = dao(EntityQuerying.class, String.class)) {
            populate(dao);

            List<EntityQuerying> results = new QueryBuilder<>(dao)
                    .where(field("valueInt"), ValueOperator.NOT_EQUALS, 1)
                    .get();

            // Only "alpha" has valueInt=1
            assertEquals(13, results.size(), "valueInt <> 1 should return all but the alpha row");
            assertTrue(results.stream().allMatch(e -> e.valueInt() != 1), "no result may have valueInt=1");
        }
    }

    @Test
    default void querying_where_greaterThan_filtersOnValueInt() {
        try (Dao<EntityQuerying, String> dao = dao(EntityQuerying.class, String.class)) {
            populate(dao);

            List<EntityQuerying> results = new QueryBuilder<>(dao)
                    .where(field("valueInt"), ValueOperator.GREATER_THAN, 50)
                    .get();

            assertEquals(3, results.size(), "valueInt > 50 should match alpha-max(100), beta-max(99), epsilon(101)");
            assertTrue(results.stream().allMatch(e -> e.valueInt() > 50), "all results must have valueInt > 50");
        }
    }

    @Test
    default void querying_where_greaterThanOrEqual_filtersOnValueInt() {
        try (Dao<EntityQuerying, String> dao = dao(EntityQuerying.class, String.class)) {
            populate(dao);

            List<EntityQuerying> results = new QueryBuilder<>(dao)
                    .where(field("valueInt"), ValueOperator.GREATER_THAN_OR_EQUAL, 50)
                    .get();

            assertEquals(4, results.size(), "valueInt >= 50 should include gammadion(50) as well");
            assertTrue(results.stream().allMatch(e -> e.valueInt() >= 50), "all results must have valueInt >= 50");
        }
    }

    @Test
    default void querying_where_lessThan_filtersOnValueInt() {
        try (Dao<EntityQuerying, String> dao = dao(EntityQuerying.class, String.class)) {
            populate(dao);

            List<EntityQuerying> results = new QueryBuilder<>(dao)
                    .where(field("valueInt"), ValueOperator.LESS_THAN, 0)
                    .get();

            assertEquals(1, results.size(), "valueInt < 0 should match only gamma-low(-10)");
            assertEquals("gamma-low", results.getFirst().name(), "matched row should be gamma-low");
        }
    }

    @Test
    default void querying_where_lessThanOrEqual_filtersOnValueInt() {
        try (Dao<EntityQuerying, String> dao = dao(EntityQuerying.class, String.class)) {
            populate(dao);

            List<EntityQuerying> results = new QueryBuilder<>(dao)
                    .where(field("valueInt"), ValueOperator.LESS_THAN_OR_EQUAL, 0)
                    .get();

            assertEquals(2, results.size(), "valueInt <= 0 should match gamma(0) and gamma-low(-10)");
            assertTrue(results.stream().allMatch(e -> e.valueInt() <= 0), "all results must have valueInt <= 0");
        }
    }

    @Test
    default void querying_where_notLike_excludesPrefix_whenNameNotNull() {
        try (Dao<EntityQuerying, String> dao = dao(EntityQuerying.class, String.class)) {
            populate(dao);

            // Avoid NULL semantics by explicitly requiring name IS NOT NULL.
            List<EntityQuerying> results = new QueryBuilder<>(dao)
                    .where(field("name"), NoValueOperator.IS_NOT_NULL)
                    .and(field("name"), ValueOperator.NOT_LIKE, "alpha%")
                    .get();

            assertTrue(results.stream().allMatch(e -> e.name() != null && !e.name().startsWith("alpha")),
                    "all results must have a non-null name that does not start with 'alpha'");
        }
    }

    @Test
    default void querying_where_in_filtersOnCategory() {
        try (Dao<EntityQuerying, String> dao = dao(EntityQuerying.class, String.class)) {
            populate(dao);

            List<EntityQuerying> results = new QueryBuilder<>(dao)
                    .where(field("category"), ValueOperator.IN, List.of("A", "B"))
                    .get();

            assertEquals(7, results.size(), "category IN (A,B) should match all A and B rows");
            assertTrue(results.stream().allMatch(e -> "A".equals(e.category()) || "B".equals(e.category())),
                    "all results must have category A or B");
        }
    }

    @Test
    default void querying_where_notIn_filtersOnCategory_whenCategoryNotNull() {
        try (Dao<EntityQuerying, String> dao = dao(EntityQuerying.class, String.class)) {
            populate(dao);

            // Avoid NULL semantics by explicitly requiring category IS NOT NULL.
            List<EntityQuerying> results = new QueryBuilder<>(dao)
                    .where(field("category"), NoValueOperator.IS_NOT_NULL)
                    .and(field("category"), ValueOperator.NOT_IN, List.of("A", "B"))
                    .get();

            assertEquals(6, results.size(), "category NOT IN (A,B) and category IS NOT NULL should match C/D/E/N rows");
            assertTrue(results.stream().allMatch(e -> e.category() != null && !List.of("A", "B").contains(e.category())),
                    "no result may have category A or B, and category must be non-null");
        }
    }

    @Test
    default void querying_where_between_filtersOnValueInt() {
        try (Dao<EntityQuerying, String> dao = dao(EntityQuerying.class, String.class)) {
            populate(dao);

            // Inclusive semantics expected for BETWEEN.
            List<EntityQuerying> results = new QueryBuilder<>(dao)
                    .where(field("valueInt"), ValueOperator.BETWEEN, List.of(2, 20))
                    .get();

            assertEquals(7, results.size(), "valueInt BETWEEN 2 AND 20 should match 7 rows in the unified dataset");
            assertTrue(results.stream().allMatch(e -> e.valueInt() >= 2 && e.valueInt() <= 20),
                    "all results must be within [2, 20] inclusive");
        }
    }

    @Test
    default void querying_where_notBetween_filtersOnValueInt() {
        try (Dao<EntityQuerying, String> dao = dao(EntityQuerying.class, String.class)) {
            populate(dao);

            List<EntityQuerying> results = new QueryBuilder<>(dao)
                    .where(field("valueInt"), ValueOperator.NOT_BETWEEN, List.of(2, 20))
                    .get();

            assertEquals(7, results.size(), "valueInt NOT BETWEEN 2 AND 20 should match the remaining 7 rows");
            assertTrue(results.stream().allMatch(e -> e.valueInt() < 2 || e.valueInt() > 20),
                    "all results must be outside [2, 20]");
        }
    }

    @Test
    default void querying_where_isNull_filtersOnName() {
        try (Dao<EntityQuerying, String> dao = dao(EntityQuerying.class, String.class)) {
            populate(dao);

            List<EntityQuerying> results = new QueryBuilder<>(dao)
                    .where(field("name"), NoValueOperator.IS_NULL)
                    .get();

            assertEquals(1, results.size(), "name IS NULL should match exactly one row");
            assertNull(results.getFirst().name(), "matched row must have name=null");
        }
    }

    @Test
    default void querying_where_isNotNull_filtersOnCategory() {
        try (Dao<EntityQuerying, String> dao = dao(EntityQuerying.class, String.class)) {
            populate(dao);
            int expected = dataset().stream().map(EntityQuerying::category).filter(Objects::nonNull).toList().size();

            List<EntityQuerying> results = new QueryBuilder<>(dao)
                    .where(field("category"), NoValueOperator.IS_NOT_NULL)
                    .get();

            for (EntityQuerying e : results) {
                System.out.println(e.category());
            }

            assertEquals(expected, results.size(), "category IS NOT NULL should match exactly the rows with a non-null category");
            assertTrue(results.stream().allMatch(e -> e.category() != null), "all matched rows must have a non-null category");
        }
    }
}
