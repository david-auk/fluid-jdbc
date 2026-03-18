package io.github.david.auk.fluid.jdbc.dbtests.contracts.foreignkey;

import io.github.david.auk.fluid.jdbc.components.daos.Dao;
import io.github.david.auk.fluid.jdbc.dbtests.contracts.ContractInterface;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public interface ContractForeignKey extends ContractInterface {

    private static void assertFkHydratedOrNull(EntityLocal local) {
        if (local.foreignEntity() != null) {
            assertNotNull(local.foreignEntity().id(), "hydrated foreignEntity must have id");
            assertNotNull(local.foreignEntity().name(), "hydrated foreignEntity must have name");
            assertNotNull(local.foreignEntity().value(), "hydrated foreignEntity must have value");
        }

        if (local.foreignEntitySecond() != null) {
            assertNotNull(local.foreignEntitySecond().id(), "hydrated foreignEntitySecond must have id");
            assertNotNull(local.foreignEntitySecond().isActive(), "hydrated foreignEntitySecond must have isActive");
            assertNotNull(local.foreignEntitySecond().value(), "hydrated foreignEntitySecond must have value");
        }
    }

    private static void assertThrowsFkViolation(RuntimeException ex) {
        // We can’t depend on vendor-specific message text, but we can still ensure “it failed”.
        assertNotNull(ex.getMessage(), "exception should have a message");
    }

    // --- tests ---

    @Test
    default void foreignKey_startsEmpty() {
        try (
                Dao<EntityForeign, String> foreignDao = dao(EntityForeign.class, String.class);
                Dao<EntityForeignSecond, String> foreignSecondDao = dao(EntityForeignSecond.class, String.class);
                Dao<EntityLocal, String> localDao = dao(EntityLocal.class, String.class)
        ) {
            assertTrue(foreignDao.getAll().isEmpty(), "foreign table should start empty");
            assertTrue(foreignSecondDao.getAll().isEmpty(), "foreign second table should start empty");
            assertTrue(localDao.getAll().isEmpty(), "local table should start empty");
        }
    }

    @Test
    default void foreignKey_insertLocal_requiresExistingForeignRows() {
        try (Dao<EntityLocal, String> localDao = dao(EntityLocal.class, String.class)) {
            EntityForeign missingForeign = new EntityForeign(null, "missing_fk", 123);
            EntityForeignSecond missingForeignSecond = new EntityForeignSecond(null, true, 456);
            EntityLocal local = new EntityLocal(null, missingForeign, missingForeignSecond, 99);

            RuntimeException ex = assertThrows(
                    RuntimeException.class,
                    () -> localDao.add(local),
                    "inserting local with missing foreign rows must fail"
            );
            assertThrowsFkViolation(ex);
        }
    }

    @Test
    default void foreignKey_insertForeignThenLocal_succeeds_andHydratesOnRead() {
        try (
                Dao<EntityForeign, String> foreignDao = dao(EntityForeign.class, String.class);
                Dao<EntityForeignSecond, String> foreignSecondDao = dao(EntityForeignSecond.class, String.class);
                Dao<EntityLocal, String> localDao = dao(EntityLocal.class, String.class)
        ) {
            EntityForeign foreign = new EntityForeign(null, "fk_1", 7);
            EntityForeignSecond foreignSecond = new EntityForeignSecond(null, true, 11);
            foreignDao.add(foreign);
            foreignSecondDao.add(foreignSecond);

            EntityLocal inserted = new EntityLocal(null, foreign, foreignSecond, 42);
            localDao.add(inserted);

            EntityLocal local = localDao.get(inserted.id());
            assertNotNull(local, "local should have been inserted");
            assertEquals(inserted.id(), local.id(), "local PK should match");
            assertEquals(42, local.value(), "local value should match");

            assertFkHydratedOrNull(local);

            assertNotNull(local.foreignEntity(), "foreignEntity should be hydrated");
            assertEquals(foreign.id(), local.foreignEntity().id(), "foreignEntity id should match");
            assertEquals("fk_1", local.foreignEntity().name(), "foreignEntity name should match");
            assertEquals(7, local.foreignEntity().value(), "foreignEntity value should match");

            assertNotNull(local.foreignEntitySecond(), "foreignEntitySecond should be hydrated");
            assertEquals(foreignSecond.id(), local.foreignEntitySecond().id(), "foreignEntitySecond id should match");
            assertEquals(true, local.foreignEntitySecond().isActive(), "foreignEntitySecond isActive should match");
            assertEquals(11, local.foreignEntitySecond().value(), "foreignEntitySecond value should match");
        }
    }

    @Test
    default void foreignKey_updateLocalForeignKeys_toExistingForeignRows_succeeds() {
        try (
                Dao<EntityForeign, String> foreignDao = dao(EntityForeign.class, String.class);
                Dao<EntityForeignSecond, String> foreignSecondDao = dao(EntityForeignSecond.class, String.class);
                Dao<EntityLocal, String> localDao = dao(EntityLocal.class, String.class)
        ) {
            EntityForeign foreign1 = new EntityForeign(null, "fk_1", 1);
            EntityForeign foreign2 = new EntityForeign(null, "fk_2", 2);
            EntityForeignSecond foreignSecond1 = new EntityForeignSecond(null, true, 10);
            EntityForeignSecond foreignSecond2 = new EntityForeignSecond(null, false, 20);
            foreignDao.add(foreign1);
            foreignDao.add(foreign2);
            foreignSecondDao.add(foreignSecond1);
            foreignSecondDao.add(foreignSecond2);

            EntityLocal before = new EntityLocal(null, foreign1, foreignSecond1, 5);
            localDao.add(before);

            EntityLocal afterUpdate = new EntityLocal(before.id(), foreign2, foreignSecond2, 6);
            localDao.update(afterUpdate);

            EntityLocal after = localDao.get(before.id());
            assertNotNull(after, "updated local row should exist");
            assertFkHydratedOrNull(after);

            assertEquals(6, after.value(), "local value should be updated");
            assertNotNull(after.foreignEntity(), "foreignEntity should be present after update");
            assertEquals(foreign2.id(), after.foreignEntity().id(), "local foreignEntity should update to new foreign row");
            assertEquals("fk_2", after.foreignEntity().name(), "updated foreignEntity name should match");
            assertEquals(2, after.foreignEntity().value(), "updated foreignEntity value should match");

            assertNotNull(after.foreignEntitySecond(), "foreignEntitySecond should be present after update");
            assertEquals(foreignSecond2.id(), after.foreignEntitySecond().id(), "local foreignEntitySecond should update to new foreign row");
            assertEquals(false, after.foreignEntitySecond().isActive(), "updated foreignEntitySecond isActive should match");
            assertEquals(20, after.foreignEntitySecond().value(), "updated foreignEntitySecond value should match");
        }
    }

    @Test
    default void foreignKey_updateLocalForeignKeys_toMissingForeignRows_fails() {
        try (
                Dao<EntityForeign, String> foreignDao = dao(EntityForeign.class, String.class);
                Dao<EntityForeignSecond, String> foreignSecondDao = dao(EntityForeignSecond.class, String.class);
                Dao<EntityLocal, String> localDao = dao(EntityLocal.class, String.class)
        ) {
            EntityForeign existingForeign = new EntityForeign(null, "fk_1", 1);
            EntityForeignSecond existingForeignSecond = new EntityForeignSecond(null, true, 10);
            foreignDao.add(existingForeign);
            foreignSecondDao.add(existingForeignSecond);

            EntityLocal before = new EntityLocal(null, existingForeign, existingForeignSecond, 5);
            localDao.add(before);

            EntityForeign missingForeign = new EntityForeign(null, "missing_fk", 999);
            EntityForeignSecond missingForeignSecond = new EntityForeignSecond(null, false, 999);

            RuntimeException ex = assertThrows(
                    RuntimeException.class,
                    () -> localDao.update(new EntityLocal(before.id(), missingForeign, missingForeignSecond, 6)),
                    "updating local to missing foreign rows must fail"
            );
            assertThrowsFkViolation(ex);
        }
    }

    @Test
    default void foreignKey_deleteReferencedForeignRow_failsWhenLocalDependsOnIt() {
        try (
                Dao<EntityForeign, String> foreignDao = dao(EntityForeign.class, String.class);
                Dao<EntityForeignSecond, String> foreignSecondDao = dao(EntityForeignSecond.class, String.class);
                Dao<EntityLocal, String> localDao = dao(EntityLocal.class, String.class)
        ) {
            EntityForeign foreign = new EntityForeign(null, "fk_1", 1);
            EntityForeignSecond foreignSecond = new EntityForeignSecond(null, true, 10);
            foreignDao.add(foreign);
            foreignSecondDao.add(foreignSecond);
            EntityLocal local = new EntityLocal(null, foreign, foreignSecond, 5);
            localDao.add(local);

            RuntimeException ex = assertThrows(
                    RuntimeException.class,
                    () -> foreignDao.delete(foreign.id()),
                    "deleting a referenced foreign row should fail because schema uses RESTRICT"
            );
            assertThrowsFkViolation(ex);

            assertTrue(foreignDao.existsByPrimaryKey(foreign.id()), "foreign row should still exist after failed delete");
            assertTrue(localDao.existsByPrimaryKey(local.id()), "local row should still exist after failed delete");
        }
    }

    @Test
    default void foreignKey_deleteSecondReferencedForeignRow_failsWhenLocalDependsOnIt() {
        try (
                Dao<EntityForeign, String> foreignDao = dao(EntityForeign.class, String.class);
                Dao<EntityForeignSecond, String> foreignSecondDao = dao(EntityForeignSecond.class, String.class);
                Dao<EntityLocal, String> localDao = dao(EntityLocal.class, String.class)
        ) {
            EntityForeign foreign = new EntityForeign(null, "fk_1", 1);
            EntityForeignSecond foreignSecond = new EntityForeignSecond(null, true, 10);
            foreignDao.add(foreign);
            foreignSecondDao.add(foreignSecond);
            EntityLocal local = new EntityLocal(null, foreign, foreignSecond, 5);
            localDao.add(local);

            RuntimeException ex = assertThrows(
                    RuntimeException.class,
                    () -> foreignSecondDao.delete(foreignSecond.id()),
                    "deleting a referenced second foreign row should fail because schema uses RESTRICT"
            );
            assertThrowsFkViolation(ex);

            assertTrue(foreignSecondDao.existsByPrimaryKey(foreignSecond.id()), "second foreign row should still exist after failed delete");
            assertTrue(localDao.existsByPrimaryKey(local.id()), "local row should still exist after failed delete");
        }
    }
}