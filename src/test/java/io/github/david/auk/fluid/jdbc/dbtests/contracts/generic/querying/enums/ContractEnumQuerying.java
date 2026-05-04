package io.github.david.auk.fluid.jdbc.dbtests.contracts.generic.querying.enums;

import io.github.david.auk.fluid.jdbc.components.daos.Dao;
import io.github.david.auk.fluid.jdbc.components.daos.QueryBuilder;
import io.github.david.auk.fluid.jdbc.components.daos.querying.operator.MultiOperator;
import io.github.david.auk.fluid.jdbc.components.daos.querying.operator.NoValueOperator;
import io.github.david.auk.fluid.jdbc.components.daos.querying.operator.SingleValueOperator;
import io.github.david.auk.fluid.jdbc.dbtests.contracts.ContractInterface;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public interface ContractEnumQuerying extends ContractInterface {

    default EntityEnumQuerying newEnumEntity(
            String name,
            QueryingStatus status,
            QueryingStatus nullableStatus
    ) {
        return new EntityEnumQuerying(
                UUID.randomUUID().toString(),
                name,
                status,
                nullableStatus
        );
    }

    default List<EntityEnumQuerying> enumDataset() {
        return List.of(
                newEnumEntity("draft-1", QueryingStatus.DRAFT, QueryingStatus.DRAFT),
                newEnumEntity("draft-2", QueryingStatus.DRAFT, null),

                newEnumEntity("active-1", QueryingStatus.ACTIVE, QueryingStatus.ACTIVE),
                newEnumEntity("active-2", QueryingStatus.ACTIVE, QueryingStatus.TO_ARCHIVE),
                newEnumEntity("active-3", QueryingStatus.ACTIVE, null),

                newEnumEntity("archived-1", QueryingStatus.TO_ARCHIVE, QueryingStatus.TO_ARCHIVE),
                newEnumEntity("deleted-1", QueryingStatus.DELETED, QueryingStatus.DELETED)
        );
    }

    default void populateEnumQuerying(Dao<EntityEnumQuerying, String> dao) {
        Objects.requireNonNull(dao, "dao");

        for (EntityEnumQuerying row : enumDataset()) {
            dao.add(row);
        }
    }

    default Field enumField(String name) {
        try {
            Field field = EntityEnumQuerying.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Missing field on EntityEnumQuerying: " + name, e);
        }
    }

    @Test
    default void enumQuerying_startsEmpty() {
        try (Dao<EntityEnumQuerying, String> dao = dao(EntityEnumQuerying.class, String.class)) {
            assertTrue(dao.getAll().isEmpty(), "enum query table should start empty");
        }
    }

    @Test
    default void enumQuerying_populate_insertsDataset() {
        try (Dao<EntityEnumQuerying, String> dao = dao(EntityEnumQuerying.class, String.class)) {
            populateEnumQuerying(dao);

            assertEquals(7, dao.getAll().size(), "enum dataset should insert all rows");
        }
    }

    @Test
    default void enumQuerying_where_equals_filtersOnEnum() {
        try (Dao<EntityEnumQuerying, String> dao = dao(EntityEnumQuerying.class, String.class)) {
            populateEnumQuerying(dao);

            List<EntityEnumQuerying> results = new QueryBuilder<>(dao)
                    .where(enumField("status"), SingleValueOperator.EQUALS, QueryingStatus.ACTIVE)
                    .get();

            assertEquals(3, results.size(), "status TO_ACTIVE should return 3 rows");
            assertTrue(results.stream().allMatch(e -> e.status() == QueryingStatus.ACTIVE));
        }
    }

    @Test
    default void enumQuerying_where_notEquals_filtersOnEnum() {
        try (Dao<EntityEnumQuerying, String> dao = dao(EntityEnumQuerying.class, String.class)) {
            populateEnumQuerying(dao);

            List<EntityEnumQuerying> results = new QueryBuilder<>(dao)
                    .where(enumField("status"), SingleValueOperator.NOT_EQUALS, QueryingStatus.ACTIVE)
                    .get();

            assertEquals(4, results.size(), "status <> TO_ACTIVE should return the other 4 rows");
            assertTrue(results.stream().allMatch(e -> e.status() != QueryingStatus.ACTIVE));
        }
    }

    @Test
    default void enumQuerying_where_in_filtersOnEnum() {
        try (Dao<EntityEnumQuerying, String> dao = dao(EntityEnumQuerying.class, String.class)) {
            populateEnumQuerying(dao);

            List<EntityEnumQuerying> results = new QueryBuilder<>(dao)
                    .where(enumField("status"), MultiOperator.IN, List.of(
                            QueryingStatus.DRAFT,
                            QueryingStatus.DELETED
                    ))
                    .get();

            assertEquals(3, results.size(), "status IN (DRAFT, DELETED) should return 3 rows");
            assertTrue(results.stream().allMatch(e ->
                    e.status() == QueryingStatus.DRAFT ||
                            e.status() == QueryingStatus.DELETED
            ));
        }
    }

    @Test
    default void enumQuerying_where_notIn_filtersOnEnum() {
        try (Dao<EntityEnumQuerying, String> dao = dao(EntityEnumQuerying.class, String.class)) {
            populateEnumQuerying(dao);

            List<EntityEnumQuerying> results = new QueryBuilder<>(dao)
                    .where(enumField("status"), MultiOperator.NOT_IN, List.of(
                            QueryingStatus.DRAFT,
                            QueryingStatus.DELETED
                    ))
                    .get();

            assertEquals(4, results.size(), "status NOT IN (DRAFT, DELETED) should return TO_ACTIVE and TO_ARCHIVE rows");
            assertTrue(results.stream().allMatch(e ->
                    e.status() == QueryingStatus.ACTIVE ||
                            e.status() == QueryingStatus.TO_ARCHIVE
            ));
        }
    }

    @Test
    default void enumQuerying_where_isNull_filtersOnNullableEnum() {
        try (Dao<EntityEnumQuerying, String> dao = dao(EntityEnumQuerying.class, String.class)) {
            populateEnumQuerying(dao);

            List<EntityEnumQuerying> results = new QueryBuilder<>(dao)
                    .where(enumField("nullableStatus"), NoValueOperator.IS_NULL)
                    .get();

            assertEquals(2, results.size(), "nullableStatus IS NULL should return 2 rows");
            assertTrue(results.stream().allMatch(e -> e.nullableStatus() == null));
        }
    }

    @Test
    default void enumQuerying_where_isNotNull_filtersOnNullableEnum() {
        try (Dao<EntityEnumQuerying, String> dao = dao(EntityEnumQuerying.class, String.class)) {
            populateEnumQuerying(dao);

            List<EntityEnumQuerying> results = new QueryBuilder<>(dao)
                    .where(enumField("nullableStatus"), NoValueOperator.IS_NOT_NULL)
                    .get();

            assertEquals(5, results.size(), "nullableStatus IS NOT NULL should return 5 rows");
            assertTrue(results.stream().allMatch(e -> e.nullableStatus() != null));
        }
    }

    @Test
    default void enumQuerying_getUnique_returnsSingleEnumMatch() {
        try (Dao<EntityEnumQuerying, String> dao = dao(EntityEnumQuerying.class, String.class)) {
            populateEnumQuerying(dao);

            EntityEnumQuerying result = new QueryBuilder<>(dao)
                    .where(enumField("status"), SingleValueOperator.EQUALS, QueryingStatus.TO_ARCHIVE)
                    .getUnique();

            assertNotNull(result);
            assertEquals(QueryingStatus.TO_ARCHIVE, result.status());
            assertEquals("archived-1", result.name());
        }
    }
}