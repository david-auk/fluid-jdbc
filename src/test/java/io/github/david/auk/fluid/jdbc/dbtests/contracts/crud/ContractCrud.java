package io.github.david.auk.fluid.jdbc.dbtests.contracts.crud;

import io.github.david.auk.fluid.jdbc.components.daos.DAO;
import io.github.david.auk.fluid.jdbc.components.results.ResultEntity;
import io.github.david.auk.fluid.jdbc.dbtests.contracts.ContractInterface;
import io.github.david.auk.fluid.jdbc.factories.DAOFactory;
import io.github.david.auk.fluid.jdbc.dbtests.support.AbstractJdbcContainerTest;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public interface ContractCrud extends ContractInterface {

    @Test
    default void startsEmpty() {
        try (DAO<EntityCrud, String> dao = dao(EntityCrud.class, String.class)) {
            assertTrue(dao.getAll().isEmpty(), "table should start empty");
        }
    }

    @Test
    default void add_insertsRecord() {
        String id = UUID.randomUUID().toString();
        try (DAO<EntityCrud, String> dao = dao(EntityCrud.class, String.class)) {
            dao.add(new EntityCrud(id, "first", 1));

            assertEquals(1, dao.getAll().size(), "insert should add exactly one row");
            assertTrue(dao.existsByPrimaryKey(id), "existsByPrimaryKey should be true after insert");

            ResultEntity<EntityCrud> read = dao.get(id);
            assertNotNull(read, "dao.get(...) should return a ResultEntity (possibly empty), not null");
            assertTrue(read.isPresent(), "inserted entity should be readable");
            assertEquals(1, read.require().valueInt(), "inserted value_int should be readable");
        }

    }

    @Test
    default void update_updatesRecord() {
        String id = UUID.randomUUID().toString();

        try (DAO<EntityCrud, String> dao = dao(EntityCrud.class, String.class)) {
            dao.add(new EntityCrud(id, "first", 1));
            
            dao.update(new EntityCrud(id, "first", 42));

            EntityCrud after = dao.get(id).require();
            assertEquals(42, after.valueInt(), "updated value_int should be readable");
        }
    }

    @Test
    default void delete_removesRecord() {
        String id = UUID.randomUUID().toString();
        try (DAO<EntityCrud, String> dao = dao(EntityCrud.class, String.class)) {
            dao.add(new EntityCrud(id, "first", 1));

            dao.delete(id);

            assertFalse(dao.existsByPrimaryKey(id), "existsByPrimaryKey should be false after delete");
            assertTrue(dao.getAll().isEmpty(), "table should be empty after delete");
        }
    }
}