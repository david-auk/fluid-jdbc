package io.github.david.auk.fluid.jdbc.dbtests.contracts.generic.crud;

import io.github.david.auk.fluid.jdbc.components.daos.Dao;
import io.github.david.auk.fluid.jdbc.dbtests.contracts.ContractInterface;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public interface ContractCrud extends ContractInterface {

    @Test
    default void crud_startsEmpty() {
        try (Dao<EntityCrud, String> dao = dao(EntityCrud.class, String.class)) {
            assertTrue(dao.getAll().isEmpty(), "table should start empty");
        }
    }

    @Test
    default void crud_add_insertsRecord() {
        String id = UUID.randomUUID().toString();
        try (Dao<EntityCrud, String> dao = dao(EntityCrud.class, String.class)) {
            dao.add(new EntityCrud(id, "first", 1, Instant.now()));

            assertEquals(1, dao.getAll().size(), "insert should add exactly one row");
            assertTrue(dao.existsByPrimaryKey(id), "existsByPrimaryKey should be true after insert");

            EntityCrud read = dao.get(id);
            assertNotNull(read, "dao.get(...) should return a ResultEntity (possibly empty), not null");
            assertEquals(1, read.valueInt(), "inserted value_int should be readable");
        }

    }

    @Test
    default void crud_update_updatesRecord() {
        String id = UUID.randomUUID().toString();

        try (Dao<EntityCrud, String> dao = dao(EntityCrud.class, String.class)) {
            dao.add(new EntityCrud(id, "first", 1, Instant.now()));
            
            dao.update(new EntityCrud(id, "first", 42, Instant.now()));

            EntityCrud after = dao.get(id);
            assertEquals(42, after.valueInt(), "updated value_int should be readable");
        }
    }

    @Test
    default void crud_updatePrimaryKey_updatesIdAndKeepsSingleRow() {
        String oldId = UUID.randomUUID().toString();
        String newId = UUID.randomUUID().toString();

        try (Dao<EntityCrud, String> dao = dao(EntityCrud.class, String.class)) {
            EntityCrud oldEntity = new EntityCrud(oldId, "first", 1, Instant.now());
            dao.add(oldEntity);

            EntityCrud newEntity = new EntityCrud(newId, "first", 1, Instant.now());
            dao.update(oldEntity, newEntity);

            assertFalse(dao.existsByPrimaryKey(oldId), "old primary key should no longer exist after update(old,new)");
            assertTrue(dao.existsByPrimaryKey(newId), "new primary key should exist after update(old,new)");
            assertEquals(1, dao.getAll().size(), "updating the primary key should not create extra rows");

            EntityCrud read = dao.get(newId);
            assertEquals(newId, read.id(), "entity should be readable by the new primary key");
            assertEquals(1, read.valueInt(), "non-PK fields should remain readable after PK update");
        }
    }

    @Test
    default void crud_updatePrimaryKey_alsoUpdatesOtherAttributes() {
        String oldId = UUID.randomUUID().toString();
        String newId = UUID.randomUUID().toString();

        try (Dao<EntityCrud, String> dao = dao(EntityCrud.class, String.class)) {
            // initial row
            EntityCrud oldEntity = new EntityCrud(oldId, "first", 1, Instant.now());
            dao.add(oldEntity);

            // new PK + changed non-PK attributes
            EntityCrud newEntity = new EntityCrud(newId, "second", 99, Instant.now());
            dao.update(oldEntity, newEntity);

            // sanity: PK changed
            assertFalse(dao.existsByPrimaryKey(oldId), "old primary key should no longer exist after update(old,new)");
            assertTrue(dao.existsByPrimaryKey(newId), "new primary key should exist after update(old,new)");

            // verify non-PK attributes are updated to match newEntity
            EntityCrud after = dao.get(newId);
            assertEquals(newId, after.id(), "id should match the new primary key");
            assertEquals("second", after.name(), "columnName should be updated by update(old,new)");
            assertEquals(99, after.valueInt(), "valueInt should be updated by update(old,new)");
        }
    }

    @Test
    default void crud_updatePrimaryKey_throwsWhenOldEntityDoesNotExist() {
        String missingId = UUID.randomUUID().toString();
        String newId = UUID.randomUUID().toString();

        try (Dao<EntityCrud, String> dao = dao(EntityCrud.class, String.class)) {
            EntityCrud oldEntity = new EntityCrud(missingId, "missing", 1, Instant.now());
            EntityCrud newEntity = new EntityCrud(newId, "missing", 1, Instant.now());

            RuntimeException ex = assertThrows(RuntimeException.class, () -> dao.update(oldEntity, newEntity));
            assertTrue(ex.getMessage() != null && ex.getMessage().toLowerCase().contains("does not exist"),
                    "exception message should mention entity does not exist");
        }
    }

    @Test
    default void crud_delete_removesRecord() {
        String id = UUID.randomUUID().toString();
        try (Dao<EntityCrud, String> dao = dao(EntityCrud.class, String.class)) {
            dao.add(new EntityCrud(id, "first", 1, Instant.now()));

            dao.delete(id);

            assertFalse(dao.existsByPrimaryKey(id), "existsByPrimaryKey should be false after delete");
            assertTrue(dao.getAll().isEmpty(), "table should be empty after delete");
        }
    }
}