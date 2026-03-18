package io.github.david.auk.fluid.jdbc.dbtests.contracts.inheritance;

import io.github.david.auk.fluid.jdbc.components.daos.Dao;
import io.github.david.auk.fluid.jdbc.components.daos.DaoTransactional;
import io.github.david.auk.fluid.jdbc.dbtests.contracts.ContractInterface;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public interface ContractInheritance extends ContractInterface {

    @Test
    default void inheritance_startsEmpty_baseAndChild() {
        try (Dao<EntityInheritBase, UUID> baseDao = dao(EntityInheritBase.class, UUID.class);
             Dao<EntityInheritChild, UUID> childDao = dao(EntityInheritChild.class, UUID.class)) {

            assertTrue(baseDao.getAll().isEmpty(), "inherit_base should start empty");
            assertTrue(childDao.getAll().isEmpty(), "inherit_child should start empty");
        }
    }

    @Test
    default void inheritance_addBase_thenAddChild_succeedsAndIsReadable() throws SQLException {
        String id = UUID.randomUUID().toString();

        // Use transactional connection because the child and parent have to be created in the same commit
        try (DaoTransactional<EntityInheritBase, String> baseDao = daoTransactional(EntityInheritBase.class, String.class);
             DaoTransactional<EntityInheritChild, String> childDao = daoTransactional(EntityInheritChild.class, String.class)) {

            EntityInheritBase base = new EntityInheritBase(id, true, 42, "base-name");
            baseDao.add(base);

            EntityInheritChild child = new EntityInheritChild(base, 7, false, 99, "child-description");
            childDao.add(child);

            commit();

            assertTrue(baseDao.existsByPrimaryKey(id), "base should exist after insert");
            assertTrue(childDao.existsByPrimaryKey(id), "child should exist after insert");

            EntityInheritChild read = childDao.get(id);
            assertNotNull(read, "childDao.get(id) should not return null");
            assertEquals(id, read.getId(), "child should share the same PK as base");
            assertTrue(read.isActive(), "child should expose inherited base boolean field");
            assertEquals(42, read.getAmount(), "child should expose inherited base int field");
            assertEquals("base-name", read.getName(), "child should expose inherited base string field");
            assertEquals(7, read.getValueInt(), "child Integer column should be readable");
            assertFalse(read.isEnabled(), "child boolean column should be readable");
            assertEquals(99, read.getScore(), "child int column should be readable");
            assertEquals("child-description", read.getDescription(), "child string column should be readable");
        }
    }

    @Test
    default void inheritance_addChildWithoutBase_throws() {
        String id = UUID.randomUUID().toString();

        try (Dao<EntityInheritChild, String> childDao = dao(EntityInheritChild.class, String.class)) {
            EntityInheritBase phantomBase = new EntityInheritBase(id, false, 10, "phantom-base");
            EntityInheritChild child = new EntityInheritChild(phantomBase, 1, true, 15, "phantom-child");

            assertThrows(RuntimeException.class, () -> childDao.add(child),
                    "adding child should fail when base row does not exist (FK constraint)");
        }
    }

    @Test
    default void inheritance_deleteBase_cascadesToChild_whenSchemaUsesCascade() {
        String id = UUID.randomUUID().toString();

        try (DaoTransactional<EntityInheritBase, String> baseDao = daoTransactional(EntityInheritBase.class, String.class);
             DaoTransactional<EntityInheritChild, String> childDao = daoTransactional(EntityInheritChild.class, String.class)) {

            EntityInheritBase base = new EntityInheritBase(id, true, 5, "cascade-base");
            EntityInheritChild child = new EntityInheritChild(base, 5, true, 77, "cascade-child");

            baseDao.add(base);
            childDao.add(child);

            commit();

            // delete parent
            baseDao.delete(id);

            assertFalse(baseDao.existsByPrimaryKey(id), "base should be deleted");
            assertFalse(childDao.existsByPrimaryKey(id),
                    "child should be deleted if schema uses ON DELETE CASCADE");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}