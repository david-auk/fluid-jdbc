package io.github.david.auk.fluid.jdbc.contracts.crud;

import io.github.david.auk.fluid.jdbc.components.daos.DAO;
import io.github.david.auk.fluid.jdbc.components.results.ResultEntity;
import io.github.david.auk.fluid.jdbc.contracts.ContractInterface;
import io.github.david.auk.fluid.jdbc.factories.DAOFactory;
import io.github.david.auk.fluid.jdbc.support.AbstractJdbcContainerTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public interface CrudContract extends ContractInterface {

    /**
     * The implementing test class must also extend AbstractJdbcContainerTest.
     * This is the only “constraint” we need for accessing dao()/connection().
     */
    default AbstractJdbcContainerTest self() {
        if (this instanceof AbstractJdbcContainerTest t) {
            return t;
        }
        throw new IllegalStateException(
                getClass().getName() + " must extend " + AbstractJdbcContainerTest.class.getName()
        );
    }

    @Override
    default DAO<CrudEntity, String> dao() {
        return DAOFactory.createDAO(self().connection(), CrudEntity.class);
    }

    @Test
    default void startsEmpty() {
        assertTrue(dao().getAll().isEmpty(), "table should start empty");
    }

    @Test
    default void add_insertsRecord() {
        String id = UUID.randomUUID().toString();
        dao().add(new CrudEntity(id, "first", 1));

        assertEquals(1, dao().getAll().size(), "insert should add exactly one row");
        assertTrue(dao().existsByPrimaryKey(id), "existsByPrimaryKey should be true after insert");

        ResultEntity<CrudEntity> read = dao().get(id);
        assertNotNull(read, "dao.get(...) should return a ResultEntity (possibly empty), not null");
        assertTrue(read.isPresent(), "inserted entity should be readable");
        assertEquals(1, read.require().valueInt(), "inserted value_int should be readable");
    }

    @Test
    default void update_updatesRecord() {
        String id = UUID.randomUUID().toString();
        dao().add(new CrudEntity(id, "first", 1));

        dao().update(new CrudEntity(id, "first", 42));

        CrudEntity after = dao().get(id).require();
        assertEquals(42, after.valueInt(), "updated value_int should be readable");
    }

    @Test
    default void delete_removesRecord() {
        String id = UUID.randomUUID().toString();
        dao().add(new CrudEntity(id, "first", 1));

        dao().delete(id);

        assertFalse(dao().existsByPrimaryKey(id), "existsByPrimaryKey should be false after delete");
        assertTrue(dao().getAll().isEmpty(), "table should be empty after delete");
    }

    @Override
    default String createTableSql() {
        return """
                CREATE TABLE crud_test_table (
                    id VARCHAR(36) PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    value_int INTEGER NOT NULL
                )
                """;
    }

    @Override
    default String dropTableSql() {
        return "DROP TABLE IF EXISTS crud_test_table";
    }
}