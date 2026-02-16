package io.github.david.auk.fluid.jdbc.support;

import io.github.david.auk.fluid.jdbc.components.Database;
import io.github.david.auk.fluid.jdbc.contracts.crud.CrudContract;
import io.github.david.auk.fluid.jdbc.contracts.foreignkey.ForeignKeyContract;
import io.github.david.auk.fluid.jdbc.contracts.inheritance.InheritanceContract;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractJdbcContainerTest
        implements CrudContract, ForeignKeyContract, InheritanceContract {

    private JdbcDatabaseContainer<?> container;

    private Connection connection;

    /**
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
    void setupSchemaAndDao() {
        this.connection = Database.getConnection();

        try (Statement statement = connection.createStatement()) {
            statement.execute(dropTableSql());
            statement.execute(createTableSql());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void cleanup() throws Exception {
        if (connection != null && !connection.isClosed()) connection.close();
    }

    // ---- contract accessors ----

    public final Connection connection() { return connection; }
}