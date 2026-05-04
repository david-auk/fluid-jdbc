package io.github.david.auk.fluid.jdbc.dbtests.contracts.generic.querying;

import io.github.david.auk.fluid.jdbc.components.daos.Dao;
import io.github.david.auk.fluid.jdbc.components.daos.QueryBuilder;
import io.github.david.auk.fluid.jdbc.components.daos.querying.operator.SingleValueOperator;
import io.github.david.auk.fluid.jdbc.dbtests.contracts.ContractInterface;
import io.github.david.auk.fluid.jdbc.dbtests.contracts.generic.foreignkey.EntityForeign;
import io.github.david.auk.fluid.jdbc.dbtests.contracts.generic.foreignkey.EntityForeignSecond;
import io.github.david.auk.fluid.jdbc.dbtests.contracts.generic.foreignkey.EntityLocal;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contract tests for querying across {@link io.github.david.auk.fluid.jdbc.annotations.table.field.ForeignKey}
 * references using JOINs in the generated WHERE clause.
 */
public interface ContractQueryingForeign extends ContractInterface {

    default EntityForeign newForeign(String name, Integer value) {
        return new EntityForeign(null, name, value);
    }

    default EntityForeignSecond newForeignSecond(Boolean isActive, Integer value) {
        return new EntityForeignSecond(null, isActive, value);
    }

    default EntityLocal newLocal(EntityForeign foreign, EntityForeignSecond foreignSecond, Integer value) {
        return new EntityLocal(null, foreign, foreignSecond, value);
    }

    default void populate(
            Dao<EntityForeign, UUID> foreignDao,
            Dao<EntityForeignSecond, UUID> foreignSecondDao,
            Dao<EntityLocal, UUID> localDao,
            List<EntityForeign> foreignRows,
            List<EntityForeignSecond> foreignSecondRows,
            List<EntityLocal> localRows
    ) {
        Objects.requireNonNull(foreignDao, "foreignDao");
        Objects.requireNonNull(foreignSecondDao, "foreignSecondDao");
        Objects.requireNonNull(localDao, "localDao");
        Objects.requireNonNull(foreignRows, "foreignRows");
        Objects.requireNonNull(foreignSecondRows, "foreignSecondRows");
        Objects.requireNonNull(localRows, "localRows");

        for (EntityForeign foreign : foreignRows) {
            foreignDao.add(foreign);
        }
        for (EntityForeignSecond foreignSecond : foreignSecondRows) {
            foreignSecondDao.add(foreignSecond);
        }
        for (EntityLocal local : localRows) {
            localDao.add(local);
        }
    }

    @Test
    default void queryingForeign_startsEmpty() {
        try (
                Dao<EntityForeign, UUID> foreignDao = dao(EntityForeign.class, UUID.class);
                Dao<EntityForeignSecond, UUID> foreignSecondDao = dao(EntityForeignSecond.class, UUID.class);
                Dao<EntityLocal, UUID> localDao = dao(EntityLocal.class, UUID.class)
        ) {
            assertTrue(foreignDao.getAll().isEmpty(), "foreign table should start empty");
            assertTrue(foreignSecondDao.getAll().isEmpty(), "second foreign table should start empty");
            assertTrue(localDao.getAll().isEmpty(), "local table should start empty");
        }
    }

    @Test
    default void queryingForeign_where_foreign_name_equals_filtersLocalRows() throws NoSuchFieldException {
        try (
                Dao<EntityForeign, UUID> foreignDao = dao(EntityForeign.class, UUID.class);
                Dao<EntityForeignSecond, UUID> foreignSecondDao = dao(EntityForeignSecond.class, UUID.class);
                Dao<EntityLocal, UUID> localDao = dao(EntityLocal.class, UUID.class)
        ) {
            EntityForeign netherlands = newForeign("netherlands", 10);
            EntityForeign germany = newForeign("germany", 20);

            EntityForeignSecond active = newForeignSecond(true, 100);
            EntityForeignSecond inactive = newForeignSecond(false, 200);

            List<EntityLocal> locals = List.of(
                    newLocal(netherlands, active, 1),
                    newLocal(netherlands, inactive, 2),
                    newLocal(germany, active, 3)
            );

            populate(
                    foreignDao,
                    foreignSecondDao,
                    localDao,
                    List.of(netherlands, germany),
                    List.of(active, inactive),
                    locals
            );

            List<EntityLocal> results = new QueryBuilder<>(localDao)
                    .where(EntityForeign.class.getDeclaredField("name"), SingleValueOperator.EQUALS, "netherlands")
                    .get();

            assertEquals(2, results.size(), "foreign.name = netherlands should match two local rows");
        }
    }

    @Test
    default void queryingForeign_where_foreign_and_local_combinesFilters() throws NoSuchFieldException {
        try (
                Dao<EntityForeign, UUID> foreignDao = dao(EntityForeign.class, UUID.class);
                Dao<EntityForeignSecond, UUID> foreignSecondDao = dao(EntityForeignSecond.class, UUID.class);
                Dao<EntityLocal, UUID> localDao = dao(EntityLocal.class, UUID.class)
        ) {
            EntityForeign netherlands = newForeign("netherlands", 10);
            EntityForeign germany = newForeign("germany", 20);

            EntityForeignSecond active = newForeignSecond(true, 100);

            List<EntityLocal> locals = List.of(
                    newLocal(netherlands, active, 1),
                    newLocal(netherlands, active, 50),
                    newLocal(germany, active, 60)
            );

            populate(
                    foreignDao,
                    foreignSecondDao,
                    localDao,
                    List.of(netherlands, germany),
                    List.of(active),
                    locals
            );

            List<EntityLocal> results = new QueryBuilder<>(localDao)
                    .where(EntityForeign.class.getDeclaredField("name"), SingleValueOperator.EQUALS, "netherlands")
                    .and(EntityLocal.class.getDeclaredField("value"), SingleValueOperator.GREATER_THAN, 10)
                    .get();

            assertEquals(1, results.size(), "foreign.name=netherlands AND value>10 should match one row");
            assertEquals(50, results.getFirst().value(), "matched row should have local value 50");
        }
    }

    @Test
    default void queryingForeign_where_multipleFieldsOnSameForeignEntity_combinesFilters() throws NoSuchFieldException {
        try (
                Dao<EntityForeign, UUID> foreignDao = dao(EntityForeign.class, UUID.class);
                Dao<EntityForeignSecond, UUID> foreignSecondDao = dao(EntityForeignSecond.class, UUID.class);
                Dao<EntityLocal, UUID> localDao = dao(EntityLocal.class, UUID.class)
        ) {
            EntityForeign netherlandsLow = newForeign("netherlands", 10);
            EntityForeign norway = newForeign("norway", 20);
            EntityForeign netherlandsHigh = newForeign("netherlands", 99);

            EntityForeignSecond active = newForeignSecond(true, 100);

            List<EntityLocal> locals = List.of(
                    newLocal(netherlandsLow, active, 1),
                    newLocal(norway, active, 2),
                    newLocal(netherlandsHigh, active, 3)
            );

            populate(
                    foreignDao,
                    foreignSecondDao,
                    localDao,
                    List.of(netherlandsLow, norway, netherlandsHigh),
                    List.of(active),
                    locals
            );

            List<EntityLocal> results = new QueryBuilder<>(localDao)
                    .where(EntityForeign.class.getDeclaredField("name"), SingleValueOperator.EQUALS, "netherlands")
                    .and(EntityForeign.class.getDeclaredField("value"), SingleValueOperator.EQUALS, 99)
                    .get();

            assertEquals(1, results.size(), "query using two fields on the same foreign entity should match one row");
            assertEquals(3, results.getFirst().value(), "matched local row should have local value 3");
        }
    }

    @Test
    default void queryingForeign_where_secondForeignField_equals_filtersLocalRows() throws NoSuchFieldException {
        try (
                Dao<EntityForeign, UUID> foreignDao = dao(EntityForeign.class, UUID.class);
                Dao<EntityForeignSecond, UUID> foreignSecondDao = dao(EntityForeignSecond.class, UUID.class);
                Dao<EntityLocal, UUID> localDao = dao(EntityLocal.class, UUID.class)
        ) {
            EntityForeign netherlands = newForeign("netherlands", 10);
            EntityForeign norway = newForeign("norway", 20);

            EntityForeignSecond active = newForeignSecond(true, 100);
            EntityForeignSecond inactive = newForeignSecond(false, 200);

            List<EntityLocal> locals = List.of(
                    newLocal(netherlands, active, 1),
                    newLocal(norway, inactive, 2)
            );

            populate(
                    foreignDao,
                    foreignSecondDao,
                    localDao,
                    List.of(netherlands, norway),
                    List.of(active, inactive),
                    locals
            );

            List<EntityLocal> results = new QueryBuilder<>(localDao)
                    .where(EntityForeignSecond.class.getDeclaredField("isActive"), SingleValueOperator.EQUALS, false)
                    .get();

            assertEquals(1, results.size(), "foreignSecond.isActive = false should match one local row");
            assertEquals(2, results.getFirst().value(), "matched local row should have local value 2");
        }
    }

    @Test
    default void queryingForeign_where_fieldsAcrossTwoForeignEntities_combinesFilters() throws NoSuchFieldException {
        try (
                Dao<EntityForeign, UUID> foreignDao = dao(EntityForeign.class, UUID.class);
                Dao<EntityForeignSecond, UUID> foreignSecondDao = dao(EntityForeignSecond.class, UUID.class);
                Dao<EntityLocal, UUID> localDao = dao(EntityLocal.class, UUID.class)
        ) {
            EntityForeign netherlands = newForeign("netherlands", 10);
            EntityForeign germany = newForeign("germany", 20);

            EntityForeignSecond activeLow = newForeignSecond(true, 100);
            EntityForeignSecond activeHigh = newForeignSecond(true, 999);
            EntityForeignSecond inactive = newForeignSecond(false, 300);

            List<EntityLocal> locals = List.of(
                    newLocal(netherlands, activeLow, 1),
                    newLocal(netherlands, inactive, 2),
                    newLocal(germany, activeHigh, 3)
            );

            populate(
                    foreignDao,
                    foreignSecondDao,
                    localDao,
                    List.of(netherlands, germany),
                    List.of(activeLow, activeHigh, inactive),
                    locals
            );

            List<EntityLocal> results = new QueryBuilder<>(localDao)
                    .where(EntityForeign.class.getDeclaredField("name"), SingleValueOperator.EQUALS, "germany")
                    .and(EntityForeignSecond.class.getDeclaredField("isActive"), SingleValueOperator.EQUALS, true)
                    .and(EntityForeignSecond.class.getDeclaredField("value"), SingleValueOperator.EQUALS, 999)
                    .get();

            assertEquals(1, results.size(), "query across two foreign entities should match one local row");
            assertEquals(3, results.getFirst().value(), "matched local row should have local value 3");
        }
    }
}
