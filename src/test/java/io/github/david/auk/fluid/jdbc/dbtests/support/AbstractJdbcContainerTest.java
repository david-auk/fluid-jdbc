package io.github.david.auk.fluid.jdbc.dbtests.support;

import io.github.david.auk.fluid.jdbc.components.Database;
import io.github.david.auk.fluid.jdbc.dbtests.contracts.ContractInterface;
import io.github.david.auk.fluid.jdbc.dbtests.contracts.crud.ContractCrud;
import io.github.david.auk.fluid.jdbc.dbtests.contracts.foreignkey.ContractForeignKey;
import io.github.david.auk.fluid.jdbc.dbtests.contracts.inheritance.ContractInheritance;
import io.github.david.auk.fluid.jdbc.dbtests.contracts.querying.ContractQuerying;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractJdbcContainerTest implements ContractCrud, ContractForeignKey, ContractInheritance, ContractQuerying {

    private JdbcDatabaseContainer<?> container;

    protected Connection connection;
    protected Connection connectionTransactional;

    private static void execAll(Connection c, String... sqlStatements) {
        try (java.sql.Statement s = c.createStatement()) {
            for (String sql : sqlStatements) {
                if (sql == null) continue;
                String trimmed = sql.trim();
                if (trimmed.isEmpty()) continue;
                s.execute(trimmed);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Drop ALL tables used by any contract tests.
     * IMPORTANT: order matters when foreign keys exist.
     */
    protected String[] dropAllTablesSql() {
        return new String[]{
                // FK tables first
                "DROP TABLE IF EXISTS local_test_table", "DROP TABLE IF EXISTS foreign_test_table",

                // CRUD
                "DROP TABLE IF EXISTS crud_test_table",

                // Querying
                "DROP TABLE IF EXISTS query_test_table",

                // Inheritance (placeholder)
                "DROP TABLE IF EXISTS inherit_child", "DROP TABLE IF EXISTS inherit_base"};
    }

    /**
     * Create ALL tables used by any contract tests.
     * IMPORTANT: order matters when foreign keys exist.
     */
    protected String[] createAllTablesSql() {
        return new String[]{
                // CRUD
                """
                CREATE TABLE crud_test_table (
                    id VARCHAR(36) PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    value_int INTEGER NOT NULL
                )
                """,

                // Querying
                """
                CREATE TABLE query_test_table (
                    id VARCHAR(36) PRIMARY KEY,
                    name VARCHAR(255),
                    category VARCHAR(255),
                    value_int INTEGER NOT NULL,
                    enabled BOOLEAN NOT NULL
                )
                """,

                // Foreign key tables: referenced table first
                """
                CREATE TABLE foreign_test_table (
                    name  VARCHAR(255) NOT NULL,
                    value INTEGER      NOT NULL,
                    CONSTRAINT pk_foreign_test_table PRIMARY KEY (name)
                )
                """, """
                CREATE TABLE local_test_table (
                    name                 VARCHAR(255) NOT NULL,
                    foreign_entity_name  VARCHAR(255) NOT NULL,

                    CONSTRAINT pk_local_test_table PRIMARY KEY (name),
                    CONSTRAINT fk_local_foreign_entity
                        FOREIGN KEY (foreign_entity_name)
                        REFERENCES foreign_test_table (name)
                        ON UPDATE CASCADE
                        ON DELETE RESTRICT
                )
                """,

                // Inheritance
                // Child `inherits` base by sharing the same PK and referencing base(id)
                """
                CREATE TABLE inherit_base (
                    id VARCHAR(255) PRIMARY KEY
                )""", """
                CREATE TABLE inherit_child (
                    id VARCHAR(255) PRIMARY KEY,
                    value_int INT NOT NULL,
                    CONSTRAINT fk_inherit_child_base
                        FOREIGN KEY (id)
                        REFERENCES inherit_base(id)
                        ON DELETE CASCADE
                )
                """};
    }

    /**
     * /**
     * Implemented by subclasses: pinned vs latest spec.
     */
    protected abstract ContainerSpec spec();

    @BeforeAll
    void startContainerAndConfigureDatabase() {
        this.container = spec().factory().get();
        this.container.start();

        System.setProperty("datasource.url", container.getJdbcUrl());
        System.setProperty("datasource.username", container.getUsername());
        System.setProperty("datasource.password", container.getPassword());
    }

    @AfterAll
    void stopContainer() {
        if (container != null) container.stop();
    }

    @BeforeEach
    void setup() {

        connection = Database.getConnection();
        connectionTransactional = Database.getConnection();

        try {
            connectionTransactional.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        execAll(connection, createAllTablesSql());
    }

    @AfterEach
    void cleanup() throws Exception {
        execAll(this.connection, dropAllTablesSql());
        if (connection != null && !connection.isClosed()) connection.close();
    }

    // ---- contract accessors ----

    public final Connection connection() {
        return connection;
    }
    public final Connection connectionTransactional() {return connectionTransactional;}
}