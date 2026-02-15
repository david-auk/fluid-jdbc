package io.github.david.auk.fluid.jdbc.contracts.crud;

import io.github.david.auk.fluid.jdbc.components.daos.DAO;
import io.github.david.auk.fluid.jdbc.components.results.ResultEntity;
import io.github.david.auk.fluid.jdbc.support.AbstractJdbcContainerTest;
import io.github.david.auk.fluid.jdbc.support.TestScenario;

import java.sql.Timestamp;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public final class CrudContractLogic {
    private CrudContractLogic() {}

    public static void run(AbstractJdbcContainerTest env, TestScenario<CrudEntity> scenario) throws Exception {
        startsEmpty(env, scenario);
        add_insertsRecord(env, scenario);
        update_updatesRecord(env, scenario);
        delete_removesRecord(env, scenario);
    }

    private static void startsEmpty(AbstractJdbcContainerTest env, TestScenario<CrudEntity> scenario) throws Exception {
        try (DAO<CrudEntity, String> dao = env.prepareDao(scenario)) {
            assertTrue(dao.getAll().isEmpty(), "table should start empty");
        }
    }

    private static void add_insertsRecord(AbstractJdbcContainerTest env, TestScenario<CrudEntity> scenario) throws Exception {
        try (DAO<CrudEntity, String> dao = env.prepareDao(scenario)) {
            String id = UUID.randomUUID().toString();
            dao.add(new CrudEntity(id, "first", 1, now()));

            assertEquals(1, dao.getAll().size(), "insert should add exactly one row");
            assertTrue(dao.existsByPrimaryKey(id), "existsByPrimaryKey should be true after insert");

            ResultEntity<CrudEntity> read = dao.get(id);
            assertNotNull(read);
            assertTrue(read.isPresent());
            assertEquals(1, read.require().valueInt());
        }
    }

    private static void update_updatesRecord(AbstractJdbcContainerTest env, TestScenario<CrudEntity> scenario) throws Exception {
        try (DAO<CrudEntity, String> dao = env.prepareDao(scenario)) {
            String id = UUID.randomUUID().toString();
            dao.add(new CrudEntity(id, "first", 1, now()));

            dao.update(new CrudEntity(id, "first", 42, now()));

            CrudEntity after = dao.get(id).require();
            assertEquals(42, after.valueInt());
        }
    }

    private static void delete_removesRecord(AbstractJdbcContainerTest env, TestScenario<CrudEntity> scenario) throws Exception {
        try (DAO<CrudEntity, String> dao = env.prepareDao(scenario)) {
            String id = UUID.randomUUID().toString();
            dao.add(new CrudEntity(id, "first", 1, now()));

            dao.delete(id);

            assertFalse(dao.existsByPrimaryKey(id));
            assertTrue(dao.getAll().isEmpty());
        }
    }

    private static Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }
}