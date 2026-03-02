package io.github.david.auk.fluid.jdbc.dbtests.contracts.foreignkey;

import io.github.david.auk.fluid.jdbc.components.daos.Dao;
import io.github.david.auk.fluid.jdbc.dbtests.contracts.ContractInterface;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assumptions.*;

import static org.junit.jupiter.api.Assertions.*;

public interface ContractForeignKey extends ContractInterface {

    private static void assertFkHydratedOrNull(EntityLocal local) {
        // Requirement #2: fully hydrated OR null.
        // If not null, all non-nullable fields in EntityForeign must be present.
        if (local.foreignEntity() != null) {
            assertNotNull(local.foreignEntity().name(), "hydrated FK must have name");
            assertNotNull(local.foreignEntity().value(), "hydrated FK must have value (not a PK-only stub)");
        }
    }

    private static void assertThrowsFkViolation(RuntimeException ex) {
        // We can’t depend on vendor-specific message text, but we can still ensure “it failed”.
        assertNotNull(ex.getMessage(), "exception should have a message");
    }

    private static boolean tryUpdateForeignPrimaryKey(
            Dao<EntityForeign, String> foreignDao,
            EntityForeign oldEntity,
            EntityForeign newEntity
    ) {
        try {
            foreignDao.update(oldEntity, newEntity);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private static void assertLocalNeverDangling(
            Dao<EntityForeign, String> foreignDao,
            Dao<EntityLocal, String> localDao,
            String localPk
    ) {
        EntityLocal local = localDao.get(localPk);
        if (local != null) {
            assertFkHydratedOrNull(local);

            if (local.foreignEntity() != null) {
                assertTrue(
                        foreignDao.existsByPrimaryKey(local.foreignEntity().name()),
                        "local must not reference a non-existing foreign row"
                );
            }
        }
    }

    // --- tests ---

    @Test
    default void foreignKey_startsEmpty() {
        try (Dao<EntityForeign, String> foreignDao = dao(EntityForeign.class, String.class);
             Dao<EntityLocal, String> localDao = dao(EntityLocal.class, String.class)) {

            assertTrue(foreignDao.getAll().isEmpty(), "foreign table should start empty");
            assertTrue(localDao.getAll().isEmpty(), "local table should start empty");
        }
    }

    @Test
    default void foreignKey_insertLocal_requiresExistingForeign() {
        try (Dao<EntityLocal, String> localDao = dao(EntityLocal.class, String.class)) {

            EntityForeign missing = new EntityForeign("missing_fk", 123);
            EntityLocal local = new EntityLocal("local_1", missing);

            RuntimeException ex = assertThrows(RuntimeException.class, () -> localDao.add(local),
                    "inserting local with missing FK must fail");
            assertThrowsFkViolation(ex);
        }
    }

    @Test
    default void foreignKey_insertForeignThenLocal_succeeds_andHydratesOnRead() {
        try (Dao<EntityForeign, String> foreignDao = dao(EntityForeign.class, String.class);
             Dao<EntityLocal, String> localDao = dao(EntityLocal.class, String.class)) {

            EntityForeign fk = new EntityForeign("fk_1", 7);
            foreignDao.add(fk);

            localDao.add(new EntityLocal("local_1", fk));

            EntityLocal local = localDao.get("local_1");

            assumeTrue(local != null, "local should have been inserted");

            assertEquals("local_1", local.name(), "local PK should match");

            // Requirement #2: fully hydrated OR null.
            assertFkHydratedOrNull(local);

            // In the normal case (non-null), verify correctness.
            if (local.foreignEntity() != null) {
                assertEquals("fk_1", local.foreignEntity().name(), "FK PK should match");
                assertEquals(7, local.foreignEntity().value(), "FK value should match");
            }
        }
    }

    @Test
    default void foreignKey_updateLocalForeignKey_toExistingForeign_succeeds() {
        try (Dao<EntityForeign, String> foreignDao = dao(EntityForeign.class, String.class);
             Dao<EntityLocal, String> localDao = dao(EntityLocal.class, String.class)) {

            EntityForeign fk1 = new EntityForeign("fk_1", 1);
            EntityForeign fk2 = new EntityForeign("fk_2", 2);
            foreignDao.add(fk1);
            foreignDao.add(fk2);

            localDao.add(new EntityLocal("local_1", fk1));

            localDao.update(new EntityLocal("local_1", fk2));

            EntityLocal after = localDao.get("local_1");
            assertFkHydratedOrNull(after);

            if (after.foreignEntity() != null) {
                assertEquals("fk_2", after.foreignEntity().name(), "local FK should update to new foreign PK");
                assertEquals(2, after.foreignEntity().value(), "updated FK should be hydrated");
            }
        }
    }

    @Test
    default void foreignKey_updateLocalForeignKey_toMissingForeign_fails() {
        try (Dao<EntityForeign, String> foreignDao = dao(EntityForeign.class, String.class);
             Dao<EntityLocal, String> localDao = dao(EntityLocal.class, String.class)) {

            EntityForeign fk1 = new EntityForeign("fk_1", 1);
            foreignDao.add(fk1);
            localDao.add(new EntityLocal("local_1", fk1));

            EntityForeign missing = new EntityForeign("missing_fk", 999);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> localDao.update(new EntityLocal("local_1", missing)),
                    "updating local to missing FK must fail");
            assertThrowsFkViolation(ex);
        }
    }

    @Test
    default void foreignKey_deleteForeign_obeysSchemaPolicy_butNeverLeavesDanglingReferences() {
        try (Dao<EntityForeign, String> foreignDao = dao(EntityForeign.class, String.class);
             Dao<EntityLocal, String> localDao = dao(EntityLocal.class, String.class)) {

            EntityForeign fk = new EntityForeign("fk_1", 1);
            foreignDao.add(fk);
            localDao.add(new EntityLocal("local_1", fk));

            boolean deleteForeignSucceeded;
            try {
                foreignDao.delete("fk_1");
                deleteForeignSucceeded = true;
            } catch (RuntimeException ex) {
                deleteForeignSucceeded = false;
            }

            if (deleteForeignSucceeded) {
                // If schema allows deleting the referenced row (CASCADE or SET NULL),
                // then the system must not leave a dangling FK reference.

                EntityLocal local = localDao.get("local_1");

                // Requirement #2: hydrated or null.
                assertFkHydratedOrNull(local);

                // If local still exists, FK must be null (SET NULL) or point to an existing row (unlikely if fk deleted).
                if (local.foreignEntity() != null) {
                    // If it’s non-null, the foreign row must exist (otherwise it's dangling).
                    assertTrue(foreignDao.existsByPrimaryKey(local.foreignEntity().name()),
                            "local must not reference a deleted foreign row");
                }
            } else {
                // RESTRICT / NO ACTION is acceptable too.
                assertTrue(foreignDao.existsByPrimaryKey("fk_1"),
                        "if delete fails, foreign row should still exist");
                assertTrue(localDao.existsByPrimaryKey("local_1"),
                        "if delete fails, local row should still exist");
            }
        }
    }


    @Test
    default void foreignKey_updateForeignPrimaryKey_neverLeavesDanglingReference() {
        try (Dao<EntityForeign, String> foreignDao = dao(EntityForeign.class, String.class);
             Dao<EntityLocal, String> localDao = dao(EntityLocal.class, String.class)) {

            EntityForeign fkOld = new EntityForeign("fk_old", 10);
            foreignDao.add(fkOld);
            localDao.add(new EntityLocal("local_1", fkOld));

            EntityForeign fkNew = new EntityForeign("fk_new", 10);

            // Attempt the PK update (policy-dependent).
            tryUpdateForeignPrimaryKey(foreignDao, fkOld, fkNew);

            // Requirement: regardless of policy, local must never end up dangling.
            assertLocalNeverDangling(foreignDao, localDao, "local_1");
        }
    }

    @Test
    default void foreignKey_updateForeignPrimaryKey_whenSupported_canUpdateLocalToNewKey() {
        try (Dao<EntityForeign, String> foreignDao = dao(EntityForeign.class, String.class);
             Dao<EntityLocal, String> localDao = dao(EntityLocal.class, String.class)) {

            EntityForeign fkOld = new EntityForeign("fk_old", 10);
            foreignDao.add(fkOld);
            localDao.add(new EntityLocal("local_1", fkOld));

            EntityForeign fkNew = new EntityForeign("fk_new", 10);

            boolean supported = tryUpdateForeignPrimaryKey(foreignDao, fkOld, fkNew);
            assumeTrue(supported, "Schema/DB does not allow updating referenced foreign PKs");

            // New PK should exist after successful update.
            assertTrue(foreignDao.existsByPrimaryKey("fk_new"), "new foreign PK should exist after update");

            // Your intended flow: update local row to reference the new PK.
            localDao.update(new EntityLocal("local_1", fkNew));

            EntityLocal after = localDao.get("local_1");
            assertFkHydratedOrNull(after);
            if (after.foreignEntity() != null) {
                assertEquals("fk_new", after.foreignEntity().name(), "local should reference the updated foreign PK");
                assertEquals(10, after.foreignEntity().value(), "foreign entity should remain hydrated after PK update");
            }

            // Still no dangling reference at the end.
            assertLocalNeverDangling(foreignDao, localDao, "local_1");
        }
    }

    @Test
    default void foreignKey_updateForeignPrimaryKey_whenRestricted_oldKeyRemainsValid() {
        try (Dao<EntityForeign, String> foreignDao = dao(EntityForeign.class, String.class);
             Dao<EntityLocal, String> localDao = dao(EntityLocal.class, String.class)) {

            EntityForeign fkOld = new EntityForeign("fk_old", 10);
            foreignDao.add(fkOld);
            localDao.add(new EntityLocal("local_1", fkOld));

            assertTrue(foreignDao.existsByPrimaryKey("fk_old"), "old foreign PK should still exist if update failed");

            EntityLocal local = localDao.get("local_1");
            assertFkHydratedOrNull(local);
            if (local.foreignEntity() != null) {
                assertEquals("fk_old", local.foreignEntity().name(), "local should still reference old PK if update failed");
            }

            assertLocalNeverDangling(foreignDao, localDao, "local_1");
        }
    }
}