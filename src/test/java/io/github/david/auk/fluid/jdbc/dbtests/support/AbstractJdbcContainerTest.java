package io.github.david.auk.fluid.jdbc.dbtests.support;

import io.github.david.auk.fluid.jdbc.components.Database;
import io.github.david.auk.fluid.jdbc.dbtests.contracts.ContractInterface;
import io.github.david.auk.fluid.jdbc.dbtests.contracts.crud.ContractCrud;
import io.github.david.auk.fluid.jdbc.dbtests.contracts.foreignkey.ContractForeignKey;
import io.github.david.auk.fluid.jdbc.dbtests.contracts.inheritance.ContractInheritance;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractJdbcContainerTest implements ContractCrud, ContractForeignKey, ContractInheritance {

    private JdbcDatabaseContainer<?> container;

    protected Connection connection;

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

                // Inheritance (placeholder)
                "DROP TABLE IF EXISTS inheritance_test_table"};
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

                // Inheritance (placeholder)
                """
                CREATE TABLE inheritance_test_table (
                    id VARCHAR(36) PRIMARY KEY
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

        this.connection = Database.getConnection();

        execAll(this.connection, createAllTablesSql());
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
}