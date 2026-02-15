package io.github.david.auk.fluid.jdbc.contracts.crud;

import io.github.david.auk.fluid.jdbc.components.daos.DAO;
import io.github.david.auk.fluid.jdbc.components.results.ResultEntity;
import io.github.david.auk.fluid.jdbc.contracts.EntityTest;
import io.github.david.auk.fluid.jdbc.support.AbstractJdbcContainerTest;
import io.github.david.auk.fluid.jdbc.support.TestScenario;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public interface CrudContract extends EntityTest<CrudEntity> {

    AbstractJdbcContainerTest env();

    @Override
    TestScenario<CrudEntity> scenario();

    @Test
    default void validateTestEntity() {
        assertDoesNotThrow(() -> io.github.david.auk.fluid.jdbc.components.tables.TableEntity
                .validateEntity(scenario().entityClass()));
    }

    @Test
    default void startsEmpty() throws Exception {
        try (DAO<CrudEntity, String> dao = env().prepareDao(scenario())) {
            assertTrue(dao.getAll().isEmpty(), "table should start empty");
        }
    }

    @Test
    default void add_insertsRecord() throws Exception {
        try (DAO<CrudEntity, String> dao = env().prepareDao(scenario())) {
            String id = UUID.randomUUID().toString();
            dao.add(new CrudEntity(id, "first", 1, now()));

            assertEquals(1, dao.getAll().size(), "insert should add exactly one row");
            assertTrue(dao.existsByPrimaryKey(id), "existsByPrimaryKey should be true after insert");

            ResultEntity<CrudEntity> read = dao.get(id);
            assertNotNull(read, "dao.get(...) should return a ResultEntity (possibly empty), not null");
            assertTrue(read.isPresent(), "inserted entity should be readable");
            assertEquals(1, read.require().valueInt(), "inserted value_int should be readable");
        }
    }

    @Test
    default void update_updatesRecord() throws Exception {
        try (DAO<CrudEntity, String> dao = env().prepareDao(scenario())) {
            String id = UUID.randomUUID().toString();
            dao.add(new CrudEntity(id, "first", 1, now()));

            dao.update(new CrudEntity(id, "first", 42, now()));

            CrudEntity after = dao.get(id).require();
            assertEquals(42, after.valueInt(), "updated value_int should be readable");
        }
    }

    @Test
    default void delete_removesRecord() throws Exception {
        try (DAO<CrudEntity, String> dao = env().prepareDao(scenario())) {
            String id = UUID.randomUUID().toString();
            dao.add(new CrudEntity(id, "first", 1, now()));

            dao.delete(id);

            assertFalse(dao.existsByPrimaryKey(id), "existsByPrimaryKey should be false after delete");
            assertTrue(dao.getAll().isEmpty(), "table should be empty after delete");
        }
    }

    private static Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }
}