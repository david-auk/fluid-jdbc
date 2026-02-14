package io.github.david.auk.fluid.jdbc.containertests;

import io.github.david.auk.fluid.jdbc.annotations.table.TableName;
import io.github.david.auk.fluid.jdbc.annotations.table.constructor.TableConstructor;
import io.github.david.auk.fluid.jdbc.annotations.table.field.PrimaryKey;
import io.github.david.auk.fluid.jdbc.annotations.table.field.TableColumn;
import io.github.david.auk.fluid.jdbc.components.Database;
import io.github.david.auk.fluid.jdbc.components.daos.DAO;
import io.github.david.auk.fluid.jdbc.components.results.ResultEntity;
import io.github.david.auk.fluid.jdbc.components.tables.TableEntity;
import io.github.david.auk.fluid.jdbc.factories.DAOFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Shared CRUD contract tests for any JDBC database container.
 *
 * Subclasses only provide:
 *  - the Testcontainers container
 *  - the table DDL (kept portable if possible)
 */
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractCrudContainerTest<C extends JdbcDatabaseContainer<?>> {

    protected abstract C container();

    /**
     * Keep this DDL as portable as possible so multiple DBs can share the same TestItem mapping.
     * (e.g. use VARCHAR(36) instead of UUID when you want MySQL/MariaDB/H2 etc.)
     */
    protected String createTableSql() {
        return """
                CREATE TABLE test_items (
                    id VARCHAR(36) PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    value_int INTEGER NOT NULL,
                    updated_at TIMESTAMP NOT NULL
                )
                """;
    }

    protected String dropTableSql() {
        // Many DBs support IF EXISTS; if one doesn't, override in that subclass.
        return "DROP TABLE IF EXISTS test_items";
    }

    private Connection connection;
    private DAO<TestItem, String> dao;

    @BeforeAll
    void beforeAll() {
        // The container is managed by the Testcontainers JUnit extension via the subclass' @Container field.
        // We only configure our Database component to point at the running container.
        System.setProperty("datasource.url", container().getJdbcUrl());
        System.setProperty("datasource.username", container().getUsername());
        System.setProperty("datasource.password", container().getPassword());

        // If Database caches a DataSource/connection pool, ensure it re-reads the new properties for each test class.
        resetDatabaseSingletonIfPresent();
    }

    @BeforeEach
    void resetState() {
        this.connection = Database.getConnection();

        try (Statement statement = connection.createStatement()) {
            statement.execute(dropTableSql());
            statement.execute(createTableSql());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        this.dao = DAOFactory.createDAO(connection, TestItem.class);
    }



    @AfterEach
    void afterEach() throws Exception {
        if (dao != null) dao.close();
        if (connection != null) connection.close();
    }

    @Test
    void validateTestEntity() {
        assertDoesNotThrow(() -> TableEntity.validateEntity(TestItem.class));
    }

    @Test
    void startsEmpty() {
        assertTrue(dao.getAll().isEmpty(), "table should start empty");
    }

    @Test
    void add_insertsRecord() {
        String id = UUID.randomUUID().toString();
        dao.add(new TestItem(id, "first", 1, now()));

        assertEquals(1, dao.getAll().size(), "insert should add exactly one row");
        assertTrue(dao.existsByPrimaryKey(id), "existsByPrimaryKey should be true after insert");

        ResultEntity<TestItem> read = dao.get(id);
        assertNotNull(read, "dao.get(...) should return a ResultEntity (possibly empty), not null");
        assertTrue(read.isPresent(), "inserted entity should be readable");
        assertEquals(1, read.require().valueInt(), "inserted value_int should be readable");
    }

    @Test
    void update_updatesRecord() {
        String id = UUID.randomUUID().toString();
        dao.add(new TestItem(id, "first", 1, now()));

        dao.update(new TestItem(id, "first", 42, now()));

        TestItem after = dao.get(id).require();
        assertEquals(42, after.valueInt(), "updated value_int should be readable");
    }

    @Test
    void delete_removesRecord() {
        String id = UUID.randomUUID().toString();
        dao.add(new TestItem(id, "first", 1, now()));

        dao.delete(id);

        assertFalse(dao.existsByPrimaryKey(id), "existsByPrimaryKey should be false after delete");
        assertTrue(dao.getAll().isEmpty(), "table should be empty after delete");
    }

    /**
     * Some Database implementations cache a DataSource/connection pool the first time it is used.
     * When running multiple container-backed test classes (e.g. Postgres + MySQL), that cache can
     * keep pointing at the previous container port and cause "Connection refused".
     *
     * We try to call a reset-like method reflectively if it exists.
     */
    private static void resetDatabaseSingletonIfPresent() {
        // Try a few common names without introducing a hard dependency.
        for (String methodName : new String[]{"reset", "close", "shutdown", "clear", "invalidate"}) {
            try {
                var m = Database.class.getDeclaredMethod(methodName);
                m.setAccessible(true);
                m.invoke(null);
                return;
            } catch (NoSuchMethodException ignored) {
                // Try the next name.
            } catch (Exception e) {
                // If a reset method exists but fails, surface it early for easier debugging.
                throw new RuntimeException("Failed to call Database." + methodName + "()", e);
            }
        }
    }

    private static Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }

    // ---------------------------------------------------------------------
    // Minimal mapped entity for the test (portable mapping)
    // ---------------------------------------------------------------------

    @TableName("test_items")
    public record TestItem(
            @PrimaryKey @TableColumn String id,
            @TableColumn String name,
            @TableColumn(name = "value_int") Integer valueInt,
            @TableColumn(name = "updated_at") Timestamp updatedAt
    ) implements TableEntity {

        @TableConstructor
        public TestItem(String id, String name, Integer valueInt, Timestamp updatedAt) {
            this.id = Objects.requireNonNull(id, "id");
            this.name = Objects.requireNonNull(name, "name");
            this.valueInt = Objects.requireNonNull(valueInt, "valueInt");
            this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
        }
    }
}