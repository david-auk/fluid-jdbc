package io.github.david.auk.fluid.jdbc.dbtests.contracts.querying.foreign;

import io.github.david.auk.fluid.jdbc.components.daos.Dao;
import io.github.david.auk.fluid.jdbc.components.daos.QueryBuilder;
import io.github.david.auk.fluid.jdbc.components.daos.querying.operator.SingleValueOperator;
import io.github.david.auk.fluid.jdbc.dbtests.contracts.ContractInterface;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contract tests for querying across a {@link io.github.david.auk.fluid.jdbc.annotations.table.field.ForeignKey}
 * reference using JOINs in the generated WHERE clause.
 */
public interface ContractQueryingForeign extends ContractInterface {

    default EntityQueryForeign newForeign(String name) {
        return new EntityQueryForeign(UUID.randomUUID().toString(), name);
    }

    default EntityQueryLocal newLocal(String name, EntityQueryForeign foreign, Integer valueInt) {
        return new EntityQueryLocal(UUID.randomUUID().toString(), name, foreign, valueInt);
    }

    default void populate(
            Dao<EntityQueryForeign, String> foreignDao,
            Dao<EntityQueryLocal, String> localDao,
            List<EntityQueryForeign> foreignRows,
            List<EntityQueryLocal> localRows
    ) {
        Objects.requireNonNull(foreignDao, "foreignDao");
        Objects.requireNonNull(localDao, "localDao");
        Objects.requireNonNull(foreignRows, "foreignRows");
        Objects.requireNonNull(localRows, "localRows");

        // referenced rows first
        for (EntityQueryForeign f : foreignRows) {
            foreignDao.add(f);
        }
        for (EntityQueryLocal l : localRows) {
            localDao.add(l);
        }
    }

    @Test
    default void queryingForeign_startsEmpty() {
        try (
                Dao<EntityQueryForeign, String> foreignDao = dao(EntityQueryForeign.class, String.class);
                Dao<EntityQueryLocal, String> localDao = dao(EntityQueryLocal.class, String.class)
        ) {
            assertTrue(foreignDao.getAll().isEmpty(), "foreign table should start empty");
            assertTrue(localDao.getAll().isEmpty(), "local table should start empty");
        }
    }

    @Test
    default void queryingForeign_where_foreign_equals_filtersLocalRows() throws NoSuchFieldException {
        try (
                Dao<EntityQueryForeign, String> foreignDao = dao(EntityQueryForeign.class, String.class);
                Dao<EntityQueryLocal, String> localDao = dao(EntityQueryLocal.class, String.class)
        ) {
            EntityQueryForeign nl = newForeign("netherlands");
            EntityQueryForeign de = newForeign("germany");

            List<EntityQueryLocal> locals = List.of(
                    newLocal("a", nl, 1),
                    newLocal("b", nl, 2),
                    newLocal("c", de, 3)
            );

            populate(foreignDao, localDao, List.of(nl, de), locals);

            List<EntityQueryLocal> results = new QueryBuilder<>(localDao)
                    .where(EntityQueryForeign.class.getDeclaredField("name"), SingleValueOperator.EQUALS, "netherlands")
                    .get();

            assertEquals(2, results.size(), "foreign.columnName = netherlands should match two local rows");
            assertTrue(results.stream().map(EntityQueryLocal::name).toList().containsAll(List.of("a", "b")),
                    "results should include local rows a and b");
        }
    }

    @Test
    default void queryingForeign_where_foreign_and_local_combinesFilters() throws NoSuchFieldException {
        try (
                Dao<EntityQueryForeign, String> foreignDao = dao(EntityQueryForeign.class, String.class);
                Dao<EntityQueryLocal, String> localDao = dao(EntityQueryLocal.class, String.class)
        ) {
            EntityQueryForeign nl = newForeign("netherlands");
            EntityQueryForeign de = newForeign("germany");

            List<EntityQueryLocal> locals = List.of(
                    newLocal("a", nl, 1),
                    newLocal("b", nl, 50),
                    newLocal("c", de, 60)
            );

            populate(foreignDao, localDao, List.of(nl, de), locals);

            List<EntityQueryLocal> results = new QueryBuilder<>(localDao)
                    .where(EntityQueryForeign.class.getDeclaredField("name"), SingleValueOperator.EQUALS, "netherlands")
                    .and(EntityQueryLocal.class.getDeclaredField("valueInt"), SingleValueOperator.GREATER_THAN, 10)
                    .get();

            assertEquals(1, results.size(), "foreign.columnName=netherlands AND valueInt>10 should match one row");
            assertEquals("b", results.getFirst().name(), "matched row should be b");
        }
    }

    @Test
    default void queryingForeign_where_foreign_like_filtersLocalRows() throws NoSuchFieldException {
        try (
                Dao<EntityQueryForeign, String> foreignDao = dao(EntityQueryForeign.class, String.class);
                Dao<EntityQueryLocal, String> localDao = dao(EntityQueryLocal.class, String.class)
        ) {
            EntityQueryForeign nl = newForeign("netherlands");
            EntityQueryForeign no = newForeign("norway");

            List<EntityQueryLocal> locals = List.of(
                    newLocal("a", nl, 1),
                    newLocal("b", no, 2)
            );

            populate(foreignDao, localDao, List.of(nl, no), locals);

            List<EntityQueryLocal> results = new QueryBuilder<>(localDao)
                    .where(EntityQueryForeign.class.getDeclaredField("name"), SingleValueOperator.LIKE, "nor%")
                    .get();

            assertEquals(1, results.size(), "foreign.columnName LIKE nor% should match one row");
            assertEquals("b", results.getFirst().name(), "matched local row should be b");
        }
    }
}
